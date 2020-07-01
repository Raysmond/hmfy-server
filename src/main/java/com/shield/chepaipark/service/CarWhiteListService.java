package com.shield.chepaipark.service;


import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.shield.chepaipark.domain.CardValidDateRange;
import com.shield.chepaipark.domain.GateIO;
import com.shield.chepaipark.domain.ParkCard;
import com.shield.chepaipark.domain.SameBarriarCard;
import com.shield.chepaipark.repository.CardValidDateRangeRepository;
import com.shield.chepaipark.repository.GateIORepository;
import com.shield.chepaipark.repository.ParkCardRepository;
import com.shield.chepaipark.repository.SameBarriarCardRepository;
import com.shield.domain.Appointment;
import com.shield.domain.Region;
import com.shield.domain.enumeration.AppointmentStatus;
import com.shield.domain.enumeration.ParkingConnectMethod;
import com.shield.domain.enumeration.RecordType;
import com.shield.repository.AppointmentRepository;
import com.shield.repository.RegionRepository;
import com.shield.repository.ShipPlanRepository;
import com.shield.service.AppointmentService;
import com.shield.service.ShipPlanService;
import com.shield.service.dto.AppointmentDTO;
import com.shield.service.dto.ShipPlanDTO;
import com.shield.service.mapper.AppointmentMapper;
import com.shield.service.mapper.ShipPlanMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.shield.config.Constants.*;
import static com.shield.service.ParkingHandlerService.AUTO_DELETE_PLAN_ID_QUEUE;
import static com.shield.service.ParkingHandlerService.REDIS_KEY_TRUCK_NUMBER_CARD_ID;

/**
 * 数据库对接停车场门禁
 */
@Service
@Slf4j
public class CarWhiteListService {

    @Autowired
    private ParkCardRepository parkCardRepository;

    @Autowired
    private SameBarriarCardRepository sameBarriarCardRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private CardValidDateRangeRepository cardValidDateRangeRepository;

    @Autowired
    @Qualifier("redisLongTemplate")
    private RedisTemplate<String, Long> redisLongTemplate;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    private GateIORepository gateIORepository;

    @Autowired
    private ShipPlanRepository shipPlanRepository;

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private ShipPlanService shipPlanService;

    @Autowired
    private ShipPlanMapper shipPlanMapper;

    private static final String REDIS_KEY_MAX_PARK_CARD_CID = "next_park_card_cid";
    private static final Long INITIAL_PARK_CARD_CID = 10000000L;
    private static final long DEFAULT_WHITELIST_VALID_HOURS = 6;
    private static final long DEFAULT_VIP_WHITELIST_VALID_HOURS = 24;
    public static final String AUTO_DELETE_PLAN_ID_QUEUE_DB = "auto_delete_ship_plan_ids_queue_db";


    @Async
    public void deleteCarWhiteList(String truckNumber) {
        List<ParkCard> parkCards = parkCardRepository.findByCardNo(truckNumber);
        if (!parkCards.isEmpty()) {
            parkCardRepository.deleteAll(parkCards);
        }

        List<CardValidDateRange> validDateRanges = cardValidDateRangeRepository.findByCardNo(truckNumber);
        if (!validDateRanges.isEmpty()) {
            cardValidDateRangeRepository.deleteAll(validDateRanges);
        }
        log.info("deleted car whitelist, truckNumber: {}", truckNumber);
    }

    private Long generateNextParkCardCid() {
        if (redisLongTemplate.hasKey(REDIS_KEY_MAX_PARK_CARD_CID) == Boolean.FALSE) {
            redisLongTemplate.opsForValue().increment(REDIS_KEY_MAX_PARK_CARD_CID, INITIAL_PARK_CARD_CID);
        }
        return redisLongTemplate.opsForValue().increment(REDIS_KEY_MAX_PARK_CARD_CID, 1L);
    }

    private String generateUniqueHcardNo() {
        return ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHMMSS")) + RandomStringUtils.randomNumeric(3);
    }

    @Async
    @Transactional
    public void registerCarWhiteListByAppointmentId(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId).get();
        Region region = appointment.getRegion();

        if (StringUtils.isBlank(region.getParkId())) {
            log.warn("missing parkId of region {}, ignore generateUploadCarWhiteListMsg", region.getId());
            return;
        }

        if (region.getParkingConnectMethod() == null || !region.getParkingConnectMethod().equals(ParkingConnectMethod.DATABASE)) {
            log.warn("region {} {} is not enabled for database registration", region.getId(), region.getName());
            return;
        }

        log.info("Start to register car white list for appointment : {}, truckNumber: {}", appointmentId, appointment.getLicensePlateNumber());
        try {
            ZonedDateTime validTime = appointment.getStartTime().plusHours(DEFAULT_WHITELIST_VALID_HOURS);
            if (appointment.isVip() != null && appointment.isVip()) {
                validTime = appointment.getStartTime().plusHours(DEFAULT_VIP_WHITELIST_VALID_HOURS);
            }
            registerCarWhiteList(
                region.getId(),
                appointment.getLicensePlateNumber(),
                appointment.getStartTime(),
                validTime,
                appointment.getUser() != null ? appointment.getUser().getFirstName() : appointment.getLicensePlateNumber());
            log.info("Succeed to register car white list for appointment : {}, truckNumber: {}", appointmentId, appointment.getLicensePlateNumber());
        } catch (Exception e) {
            log.error("failed to register car white list for appointment : {}, truckNumber: {}", appointmentId, appointment.getLicensePlateNumber());
        }
    }

    private void putTruckNumber2CardId(String truckNumber, String cardId) {
        log.info("set card_id = {}, car_number: {}", cardId, truckNumber);
        String k = DigestUtils.md5Hex(truckNumber).toUpperCase();
        redisTemplate.opsForHash().put(REDIS_KEY_TRUCK_NUMBER_CARD_ID, k, cardId);
    }

    public void registerCarWhiteList(Long regionId, String truckNumber, ZonedDateTime startTime, ZonedDateTime validTime, String userName) {
        SameBarriarCard barriarCard = findOrCreateBarriarCardByTruckNumber(truckNumber);
        List<ParkCard> parkCards = parkCardRepository.findByCardNo(truckNumber);
        if (!parkCards.isEmpty()) {
            parkCardRepository.deleteAll(parkCards);
        }
        ParkCard parkCard = new ParkCard();
        parkCard.setCardNo(truckNumber);
        parkCard.setCarNo(truckNumber);
        parkCard.setAddress("上海市宝山区");
        parkCard.setPhone("18800000000");
        parkCard.setUserName(userName);
        parkCard.setCtid(1);
        parkCard.setFctCode(1);
        parkCard.setCardState(1);
        parkCard.setStartDate(startTime.truncatedTo(ChronoUnit.SECONDS));
        parkCard.setValidDate(validTime.truncatedTo(ChronoUnit.SECONDS));
        parkCard.setRegisterDate(ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        parkCard.setLastTime(ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(3));
        parkCard.setCDate(ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        parkCard.setCUser("管理员");
        parkCard.setCardMoney(0.0);
        parkCard.setDriveNo("NO000000000");
        parkCard.setCarLocate("A-1-10");
        parkCard.setRemark("（通用接口新增）");
        parkCard.setFeePeriod("月");
        parkCard.setLimitDayType(0);
        parkCard.setAreaId(REGION_ID_2_AREA_ID.getOrDefault(regionId, -1));
        parkCard.setHcardNo(generateUniqueHcardNo());
        parkCard.setZMCarLocateCount(0);
        parkCard.setZMUsedLocateCount(0);

        try {
            Long maxCid = parkCardRepository.findMaxCid();
            parkCard.setCid(maxCid + 1L);
        } catch (Exception e) {
            parkCard.setCid(generateNextParkCardCid());
        }

        parkCardRepository.save(parkCard);
        List<CardValidDateRange> validDateRanges = cardValidDateRangeRepository.findByCardNo(truckNumber);
        if (!validDateRanges.isEmpty()) {
            cardValidDateRangeRepository.deleteAll(validDateRanges);
        }
        CardValidDateRange validDateRange = new CardValidDateRange();
        validDateRange.setCardNo(truckNumber);
        validDateRange.setCreateTime(ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        validDateRange.setStartDate(startTime.truncatedTo(ChronoUnit.SECONDS));
        validDateRange.setEndDate(validTime.truncatedTo(ChronoUnit.SECONDS));
        cardValidDateRangeRepository.save(validDateRange);

        putTruckNumber2CardId(truckNumber, parkCard.getHcardNo());
    }

    private SameBarriarCard findOrCreateBarriarCardByTruckNumber(String truckNumber) {
        List<SameBarriarCard> cardList = sameBarriarCardRepository.findByCardNo(truckNumber);
        if (cardList.isEmpty()) {
            SameBarriarCard card = new SameBarriarCard();
            card.setCardNo(truckNumber);

            card.setCreateTime(ZonedDateTime.now());
            card.setLastTime(ZonedDateTime.now());
            card = sameBarriarCardRepository.save(card);
            return card;
        }
        return cardList.get(0);
    }

    private static ZonedDateTime lastSyncTime = ZonedDateTime.now().minusHours(1);
    private static Map<Long, GateIO> lastProcessedGateIO = Maps.newConcurrentMap();

    public void syncCarGateIOEvents() {
//        Map<Long, Region> regions = regionRepository.findAll()
//            .stream()
//            .filter(it -> it.isOpen() && it.getParkingConnectMethod() != null && it.getParkingConnectMethod().equals(ParkingConnectMethod.DATABASE))
//            .collect(Collectors.toMap(Region::getId, it -> it));
//        if (regions.isEmpty()) {
//            return;
//        }
        List<GateIO> gates = gateIORepository.findAllNewerThan(lastSyncTime.minusMinutes(5));
        lastSyncTime = ZonedDateTime.now();
        if (CollectionUtils.isEmpty(gates)) {
            return;
        }
        log.info("Find {} GateIO newest history", gates.size());
        for (GateIO gate : gates) {
            if (!AREA_ID_2_REGION_ID.containsKey(gate.getAreaId())) {
                continue;
            }
            if (lastProcessedGateIO.containsKey(gate.getRecordId())) {
                GateIO last = lastProcessedGateIO.get(gate.getRecordId());
                if (!((last.getGateInTime() == null && gate.getGateInTime() != null) || (last.getGateOutTime() == null && gate.getGateOutTime() != null))) {
                    // 没有更新
                    continue;
                }
            }
            lastProcessedGateIO.put(gate.getRecordId(), gate);
            Long regionId = AREA_ID_2_REGION_ID.get(gate.getAreaId());
            Region region = regionRepository.findById(regionId).get();

            log.info("[DB] Start to process GateIO, RecordID: {} CardNo: {}, GateInTime: {}, GateOutTime: {}, " +
                    "AreaId: {}, AreaName: {}, regionId: {}, regionName: {}",
                gate.getRecordId(), gate.getCardNo(), gate.getGateInTime(), gate.getGateOutTime(),
                gate.getAreaId(), gate.getAreaName(), regionId, region.getName());

            if (region.getParkingConnectMethod() != null && region.getParkingConnectMethod().equals(ParkingConnectMethod.DATABASE)) {
                this.updateCarInAndOutTime(
                    region.getId(),
                    gate.getCardNo(),
                    gate.getGateOutTime() != null ? RecordType.OUT : RecordType.IN,
                    gate.getGateInTime(),
                    gate.getGateOutTime());
            }
        }

        for (Long recordId : lastProcessedGateIO.keySet()) {
            GateIO gate = lastProcessedGateIO.get(recordId);
            if (gate.getGateOutTime() != null && gate.getGateOutTime().plusDays(1).isBefore(ZonedDateTime.now())) {
                lastProcessedGateIO.remove(recordId);
            }
        }
    }

    public void updateCarInAndOutTime(Long regionId, String truckNumber, RecordType recordType, ZonedDateTime inTime, ZonedDateTime outTime) {
        Region region = regionRepository.findById(regionId).get();
        ZonedDateTime today = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        log.info("Start to find ShipPlan for truckNumber: {}, regionId: {}, regionName: {}, recordType: {}, inTime: {}, outTime: {}, today: {}",
            truckNumber, region.getId(), region.getName(), recordType, inTime, outTime, today);
        if (recordType.equals(RecordType.IN)) {
            // 车辆入场
            List<ShipPlanDTO> allShipPlans = shipPlanRepository.findAllByTruckNumberAndDeliverTime(truckNumber, region.getName(), today)
//            List<ShipPlanDTO> allShipPlans = shipPlanRepository.findAllByTruckNumber(truckNumber, true, PageRequest.of(0, 1, Sort.by(Sort.Order.desc("createTime"))))
                .stream().map(it -> shipPlanMapper.toDto(it)).collect(Collectors.toList());
            List<ShipPlanDTO> shipPlans = allShipPlans.stream()
                .filter(it -> it.getAuditStatus().equals(Integer.valueOf(1)))
                .sorted(Comparator.comparing(ShipPlanDTO::getApplyId)).collect(Collectors.toList());
            if (shipPlans.size() > 1) {
                // 理论上车辆入场，当计划audit_status=1的只有一条
                // 不排除同时建了多个计划的情况，有多个有效计划时，只取最早创建的一条
                log.error("Multiple plans found for truckNumber {}, region: {}, deliverDate: {}, planIds: [{}]",
                    truckNumber, region.getName(), today, Joiner.on(",").join(shipPlans.stream().map(ShipPlanDTO::getId).collect(Collectors.toList())));
            }
            if (!CollectionUtils.isEmpty(shipPlans)) {
                ShipPlanDTO plan = shipPlans.get(0);
                log.info("Find ShipPlan id={} for truckNumber {}, uploadcarin gateTime: {}", plan.getId(), truckNumber, inTime);
                if (plan.getGateTime() == null) {
                    plan.setGateTime(inTime);
                    plan.setUpdateTime(ZonedDateTime.now());
                    shipPlanService.save(plan);
                    log.info("update ShipPlan {} gateTime: {}, truckNumber: {}", plan.getApplyId(), inTime, truckNumber);
                }
            } else {
                log.error("Cannot find ShipPlan for truckNumber {}, uploadcarin gateTime: {}", truckNumber, inTime);
            }
        } else if (recordType.equals(RecordType.OUT)) {
            // 车辆出厂
            if (outTime.getYear() < ZonedDateTime.now().getYear()) {
                log.info("uploadcarout leave time error, {}", outTime);
                outTime = ZonedDateTime.now();
            }
            List<ShipPlanDTO> shipPlans = shipPlanRepository
                .findAllByTruckNumberAndDeliverTime(truckNumber, region.getName(), today).stream()
                .map(it -> shipPlanMapper.toDto(it))
                .filter(it -> it.getAuditStatus().equals(3) || (it.getGateTime() != null))
                .sorted(Comparator.comparing(ShipPlanDTO::getApplyId).reversed())
                .collect(Collectors.toList());
            // 取未出场的，最近一个提货完成的计划
            if (!shipPlans.isEmpty()) {
                ShipPlanDTO plan = shipPlans.get(0);
                log.info("Find ShipPlan applyId={} for truckNumber {}, uploadcarout gateTime: {}, leaveTime: {}", plan.getApplyId(), truckNumber, inTime, outTime);
                plan.setLeaveTime(outTime);
                if (plan.getGateTime() == null && inTime != null) {
                    plan.setGateTime(inTime);
                    if (plan.getLoadingStartTime() != null && inTime.isAfter(plan.getLoadingStartTime())) {
                        log.info("ShipPlan id={} for truckNumber: {}, inTime: {} is after loadingStartTime {}, reset to 30min before loadingStartTime",
                            plan.getId(), plan.getTruckNumber(), inTime, plan.getLoadingStartTime());
                        plan.setGateTime(plan.getLoadingStartTime().minusMinutes(30));
                    }
                }
                plan.setUpdateTime(ZonedDateTime.now());
                shipPlanService.save(plan);
                log.info("update ShipPlan {} leaveTime: {}, truckNumber: {}", plan.getApplyId(), outTime, truckNumber);
            } else {
                log.error("Cannot find ShipPlan for truckNumber {}, uploadcarout gateTime: {}, leaveTime: {}", truckNumber, inTime, outTime);
            }
        }

        List<AppointmentDTO> appointments = appointmentRepository
            .findLatestByTruckNumber(region.getId(), truckNumber, ZonedDateTime.now().minusHours(24)).stream()
            .map(it -> appointmentMapper.toDto(it))
            .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(appointments)) {
            AppointmentDTO appointment = appointments.get(0);
            boolean save = false;
            if (recordType.equals(RecordType.IN)) {
                if (appointment.getStatus().equals(AppointmentStatus.START) && appointment.getStartTime() != null && appointment.getStartTime().isBefore(inTime)) {
                    appointment.setStatus(AppointmentStatus.ENTER);
                    appointment.setEnterTime(inTime);
                    save = true;
                }
            } else if (recordType.equals(RecordType.OUT)) {
                if (appointment.getStatus().equals(AppointmentStatus.ENTER) && appointment.getEnterTime() != null && appointment.getEnterTime().isBefore(outTime)) {
                    appointment.setLeaveTime(outTime);
                    appointment.setStatus(AppointmentStatus.LEAVE);
                    save = true;
                }
            }

            if (save) {
                appointment.setUpdateTime(ZonedDateTime.now());
                appointmentService.save(appointment);
                log.info("Update appointment [{}] status: {}, inTime: {}, outTime: {}, truckNumber: {}",
                    appointment.getId(), appointment.getStatus(), inTime, outTime, truckNumber);
            }

            if (!save) {
                if (appointments.size() > 1) {
                    AppointmentDTO second = appointments.get(1);
                    if (second.isValid() && second.getStatus() == AppointmentStatus.START) {
                        if (recordType.equals(RecordType.IN)) {
                            second.setStatus(AppointmentStatus.ENTER);
                            second.setUpdateTime(ZonedDateTime.now());
                            second.setEnterTime(inTime);
                            appointmentService.save(second);
                            log.info("Update appointment [{}] status: {}, inTime: {}, outTime: {}, truckNumber: {}",
                                second.getId(), second.getStatus(), inTime, outTime, truckNumber);
                        }
                    } else if (second.isValid() && second.getStatus() == AppointmentStatus.ENTER) {
                        if (recordType.equals(RecordType.OUT)) {
                            second.setStatus(AppointmentStatus.LEAVE);
                            second.setUpdateTime(ZonedDateTime.now());
                            second.setLeaveTime(inTime);
                            appointmentService.save(second);
                            log.info("Update appointment [{}] status: {}, inTime: {}, outTime: {}, truckNumber: {}",
                                second.getId(), second.getStatus(), inTime, outTime, truckNumber);
                        }
                    }
                }
            }
        }

    }

    /**
     * 延时一分钟将出入场时间同步到发运
     *
     * @param shipPlanId
     */
    public void delayPutSyncShipPlanIdQueue(Long shipPlanId) {
        threadPoolTaskScheduler.getScheduledExecutor().schedule(() -> {
            log.info("Delayed 60s to put ShipPlan id {} to sync queue {}", shipPlanId, REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN);
            redisLongTemplate.opsForSet().add(REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN, shipPlanId);
        }, 60L, TimeUnit.SECONDS);
    }

    /**
     * 出场后延时两分钟删除白名单(DB)
     *
     * @param shipPlanId
     */
    public void delayPutDeleteShipPlanIdQueueDB(Long shipPlanId) {
        threadPoolTaskScheduler.getScheduledExecutor().schedule(() -> {
            log.info("Delayed 180s to put ShipPlan id {} to delete car whitelist queue {}", shipPlanId, AUTO_DELETE_PLAN_ID_QUEUE_DB);
            redisLongTemplate.opsForSet().add(AUTO_DELETE_PLAN_ID_QUEUE_DB, shipPlanId);
        }, 120L, TimeUnit.SECONDS);
    }
}
