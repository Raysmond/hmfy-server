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
import com.shield.domain.ShipPlan;
import com.shield.domain.enumeration.AppointmentStatus;
import com.shield.domain.enumeration.ParkingConnectMethod;
import com.shield.repository.AppointmentRepository;
import com.shield.repository.RegionRepository;
import com.shield.repository.ShipPlanRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.shield.service.ParkingTcpHandlerService.AUTO_DELETE_PLAN_ID_QUEUE;
import static com.shield.service.ParkingTcpHandlerService.REDIS_KEY_TRUCK_NUMBER_CARD_ID;
import static com.shield.service.impl.AppointmentServiceImpl.REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN;
import static com.shield.service.impl.AppointmentServiceImpl.REDIS_KEY_SYNC_VIP_GATE_LOG_APPOINTMENT_IDS;

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

    private static final String REDIS_KEY_MAX_PARK_CARD_CID = "next_park_card_cid";
    private static final String REDIS_KEY_MAX_PARK_HCARD_NO = "next_park_h_card_no";
    private static final Long INITIAL_PARK_CARD_CID = 10000000L;
    private static final Long INITIAL_PARK_HCARD_NO = 10000L;


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

    private Long generateUniqueHcardNo() {
        if (redisLongTemplate.hasKey(REDIS_KEY_MAX_PARK_HCARD_NO) == Boolean.FALSE) {
            redisLongTemplate.opsForValue().increment(REDIS_KEY_MAX_PARK_HCARD_NO, INITIAL_PARK_HCARD_NO);
        }
        return redisLongTemplate.opsForValue().increment(REDIS_KEY_MAX_PARK_HCARD_NO, 1L);
    }

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
//            ZonedDateTime validTime = appointment.getStartTime().plusHours(region.getValidTime());
            ZonedDateTime validTime = appointment.getStartTime().plusHours(6);
            if (appointment.isVip() != null && appointment.isVip()) {
                validTime = appointment.getStartTime().plusHours(24);
            }
            registerCarWhiteList(
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

    public void registerCarWhiteList(String truckNumber, ZonedDateTime startTime, ZonedDateTime validTime, String userName) {
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
        parkCard.setAreaId(-1);
        parkCard.setHcardNo(ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHMMSS")) + RandomStringUtils.randomNumeric(3));
        parkCard.setZMCarLocateCount(0);
        parkCard.setZMUsedLocateCount(0);

//        Long maxCid = parkCardRepository.findMaxCid();
//        parkCard.setCid(maxCid + 1L);
        parkCard.setCid(generateNextParkCardCid());
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

    @Scheduled(fixedRate = 5 * 1000)
    public void syncCarGateIOEvents() {
        List<Region> regions = regionRepository.findAll();
        Map<String, Region> regionsEnableDBConnection = Maps.newHashMap();
        for (Region region : regions) {
            if (region.isOpen() && region.getParkingConnectMethod() != null && region.getParkingConnectMethod().equals(ParkingConnectMethod.DATABASE)) {
                regionsEnableDBConnection.put(region.getName(), region);
            }
        }
        if (regionsEnableDBConnection.isEmpty()) {
            return;
        }
        List<GateIO> gates = gateIORepository.findAllNewerThan(lastSyncTime.minusMinutes(5));
        lastSyncTime = ZonedDateTime.now();
        if (CollectionUtils.isEmpty(gates)) {
            return;
        }
        log.info("Find {} GateIO newest history", gates.size());
        for (GateIO gate : gates) {
            if (lastProcessedGateIO.containsKey(gate.getRecordId())) {
                GateIO last = lastProcessedGateIO.get(gate.getRecordId());
                if (last.getUpdatedTime().equals(gate.getUpdatedTime())) {
                    // 没有更新
                    continue;
                }
            }
            lastProcessedGateIO.put(gate.getRecordId(), gate);
            log.info("[DB] Start to process GateIO, RecordID: {} CardNo: {}, GateInTime: {}, GateOutTime: {}, AreaName: {}",
                gate.getRecordId(), gate.getCardNo(), gate.getGateInTime(), gate.getGateOutTime(), gate.getAreaName());
            Region region = null;
            if (gate.getAreaName().equals("宝田本部")) {
                region = regionsEnableDBConnection.getOrDefault("宝田", null);
            } else {
                region = regionsEnableDBConnection.getOrDefault(gate.getAreaName(), null);
            }
            if (region != null) {
                if (region.getParkingConnectMethod() != null && region.getParkingConnectMethod().equals(ParkingConnectMethod.DATABASE)) {
                    this.updateCarInAndOutTime(
                        region.getParkId(),
                        gate.getCardNo(),
                        gate.getGateOutTime() != null ? "uploadcarout" : "uploadcarin",
                        gate.getGateInTime(),
                        gate.getGateOutTime());
                }
            } else {
                log.info("Cannot find region for AreaName: {}", gate.getAreaName());
            }
        }

        for (Long recordId : lastProcessedGateIO.keySet()) {
            GateIO gate = lastProcessedGateIO.get(recordId);
            if (gate.getGateOutTime() != null && gate.getGateOutTime().plusHours(12).isBefore(ZonedDateTime.now())) {
                lastProcessedGateIO.remove(recordId);
            }
        }
    }


    public void updateCarInAndOutTime(String parkId, String truckNumber, String service, ZonedDateTime inTime, ZonedDateTime outTime) {
        Region region = regionRepository.findOneByParkId(parkId);
        if (region == null) {
            log.error("Cannot find region by parkid: {}", parkId);
            return;
        }
        ZonedDateTime today = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        if (service.equals("uploadcarin")) {
            // 车辆入场
            List<ShipPlan> shipPlans = shipPlanRepository.findAllByTruckNumberAndDeliverTime(truckNumber, region.getName(), today)
                .stream().filter(it -> it.getAuditStatus().equals(Integer.valueOf(1)))
                .sorted(Comparator.comparing(ShipPlan::getApplyId)).collect(Collectors.toList());
            if (shipPlans.size() > 1) {
                // 理论上车辆入场，当计划audit_status=1的只有一条
                // 不排除同时建了多个计划的情况，有多个有效计划时，只取最早创建的一条
                log.error("Multiple plans found for truckNumber {}, region: {}, deliverDate: {}, planIds: [{}]",
                    truckNumber, region.getName(), today, Joiner.on(",").join(shipPlans.stream().map(ShipPlan::getId).collect(Collectors.toList())));
            }
            if (!CollectionUtils.isEmpty(shipPlans)) {
                ShipPlan plan = shipPlans.get(0);
                log.info("Find ShipPlan id={} for truckNumber {}, uploadcarin gateTime: {}", plan.getId(), truckNumber, inTime);
                if (plan.getGateTime() == null) {
                    plan.setGateTime(inTime);
                    plan.setUpdateTime(ZonedDateTime.now());
                    shipPlanRepository.save(plan);
                    log.info("update ShipPlan {} gateTime: {}, truckNumber: {}", plan.getApplyId(), inTime, truckNumber);
//                    redisLongTemplate.opsForSet().add(REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN, plan.getId());
                    delayPutSyncShipPlanIdQueue(plan.getId());
                }
            } else {
                log.error("Cannot find ShipPlan for truckNumber {}, uploadcarin gateTime: {}", truckNumber, inTime);
            }
        } else if (service.equals("uploadcarout")) {
            // 车辆出厂
            if (outTime.getYear() < ZonedDateTime.now().getYear()) {
                log.info("uploadcarout leave time error, {}", outTime);
                outTime = ZonedDateTime.now();
            }
            List<ShipPlan> shipPlans = shipPlanRepository.findAllByTruckNumberAndDeliverTime(truckNumber, region.getName(), today)
                .stream().filter(it -> it.getAuditStatus().equals(Integer.valueOf(3)) || (it.getGateTime() != null))
                .sorted(Comparator.comparing(ShipPlan::getApplyId).reversed())
                .collect(Collectors.toList());
            // 取未出场的，最近一个提货完成的计划
            if (!shipPlans.isEmpty()) {
                ShipPlan plan = shipPlans.get(0);
                log.info("Find ShipPlan id={} for truckNumber {}, uploadcarout gateTime: {}, leaveTime: {}", plan.getId(), truckNumber, inTime, outTime);
                plan.setLeaveTime(outTime);
                if (plan.getGateTime() == null && inTime != null) {
                    plan.setGateTime(inTime);
                }
                plan.setUpdateTime(ZonedDateTime.now());
                shipPlanRepository.save(plan);
                log.info("update ShipPlan {} leaveTime: {}, truckNumber: {}", plan.getApplyId(), outTime, truckNumber);

//                redisLongTemplate.opsForSet().add(REDIS_KEY_SYNC_SHIP_PLAN_TO_VEH_PLAN, plan.getId());
//                redisLongTemplate.opsForSet().add(AUTO_DELETE_PLAN_ID_QUEUE, plan.getId());
                delayPutSyncShipPlanIdQueue(plan.getId());
                delayPutDeleteShipPlanIdQueue(plan.getId());
            } else {
                log.error("Cannot find ShipPlan for truckNumber {}, uploadcarout gateTime: {}, leaveTime: {}", truckNumber, inTime, outTime);
            }
        }

        List<Appointment> appointments = appointmentRepository.findLatestByTruckNumber(region.getId(), truckNumber, ZonedDateTime.now().minusHours(12));
        if (!CollectionUtils.isEmpty(appointments)) {
            Appointment appointment = appointments.get(0);
            boolean save = false;
            if (service.equals("uploadcarin")) {
                if (appointment.getStatus().equals(AppointmentStatus.START)) {
                    appointment.setStatus(AppointmentStatus.ENTER);
                    appointment.setEnterTime(inTime);
                    save = true;
                }
            } else if (service.equals("uploadcarout")) {
                if (appointment.getStatus().equals(AppointmentStatus.ENTER)) {
                    appointment.setLeaveTime(outTime);
                    appointment.setStatus(AppointmentStatus.LEAVE);
                    save = true;
                    // redisLongTemplate.opsForSet().add(REDIS_KEY_DELETE_CAR_WHITELIST, appointment.getId());
                }
            }

            if (save) {
                appointment.setUpdateTime(ZonedDateTime.now());
                appointmentRepository.save(appointment);
                log.info("Update appointment [{}] status: {}, inTime: {}, outTime: {}, truckNumber: {}",
                    appointment.getId(), appointment.getStatus(), inTime, outTime, truckNumber);

                if (appointment.isVip() && appointment.getApplyId() == null) {
                    this.deplyPutSyncVipAppointmentGateLog(appointment.getId());
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
     * 出场后延时三分钟删除白名单
     *
     * @param shipPlanId
     */
    public void delayPutDeleteShipPlanIdQueue(Long shipPlanId) {
        threadPoolTaskScheduler.getScheduledExecutor().schedule(() -> {
            log.info("Delayed 180s to put ShipPlan id {} to delete car whitelist queue {}", shipPlanId, AUTO_DELETE_PLAN_ID_QUEUE);
            redisLongTemplate.opsForSet().add(AUTO_DELETE_PLAN_ID_QUEUE, shipPlanId);
        }, 180L, TimeUnit.SECONDS);
    }

    /**
     * 同步手动VIP预约的出入场记录
     */
    public void deplyPutSyncVipAppointmentGateLog(Long appointmentId) {
        threadPoolTaskScheduler.getScheduledExecutor().schedule(() -> {
            log.info("Delayed 60s to put Appointment[id={}] to syc vip gate log queue {}", appointmentId, REDIS_KEY_SYNC_VIP_GATE_LOG_APPOINTMENT_IDS);
            redisLongTemplate.opsForSet().add(REDIS_KEY_SYNC_VIP_GATE_LOG_APPOINTMENT_IDS, appointmentId);
        }, 60L, TimeUnit.SECONDS);
    }
}
