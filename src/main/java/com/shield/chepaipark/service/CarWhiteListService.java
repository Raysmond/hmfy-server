package com.shield.chepaipark.service;


import com.shield.chepaipark.domain.ParkCard;
import com.shield.chepaipark.domain.SameBarriarCard;
import com.shield.chepaipark.repository.ParkCardRepository;
import com.shield.chepaipark.repository.SameBarriarCardRepository;
import com.shield.domain.Appointment;
import com.shield.domain.Region;
import com.shield.repository.AppointmentRepository;
import com.shield.repository.RegionRepository;
import com.shield.service.dto.AppointmentDTO;
import com.shield.service.dto.RegionDTO;
import com.shield.service.tcp.UploadCarWhiteListMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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


    public UploadCarWhiteListMsg generateUploadCarWhiteListMsg(Long appointmentId) {
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId).get();
            Region region = appointment.getRegion();

            if (StringUtils.isBlank(region.getParkId())) {
                log.warn("missing parkId of region {}, ignore generateUploadCarWhiteListMsg", region.getId());
                return null;
            }
//            if (!region.isOpen()) {
//                log.warn("region {} {}  is not opened for appointment", region.getId(), region.getName());
//                return null;
//            }
//            if (region.isAutoAppointment() != null && region.isAutoAppointment()) {
//                // 开启默认注册白名单，不需要预约后再注册
//                log.warn("region {} {}  auto_appointment enabled", region.getId(), region.getName());
//                return null;
//            }
            UploadCarWhiteListMsg carWhiteListMsg = new UploadCarWhiteListMsg();
            String now = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            carWhiteListMsg.setParkid(region.getParkId());
            carWhiteListMsg.setCar_number(appointment.getLicensePlateNumber());
//            carWhiteListMsg.setCard_id(appointment.getLicensePlateNumber());
            if (null != appointment.getUser()) {
                carWhiteListMsg.setCarusername(appointment.getUser().getFirstName());
            }
            carWhiteListMsg.setOperate_type(1); // 注册
            carWhiteListMsg.setStartdate(now);
            carWhiteListMsg.setValiddate(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).plusDays(1).minusSeconds(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            carWhiteListMsg.setCreate_time(now);
            carWhiteListMsg.setModify_time(now);
            return carWhiteListMsg;
        } catch (Exception e) {
            log.error("failed execute uploadCarWhiteList(), appointmentId: {}", appointmentId, e);
            return null;
        }
    }

    @Async
    public void registerCarWhiteListByAppointmentId(Long appointmentId) {
        log.error("Start to register car white list for appointment : {}", appointmentId);
        try {
            registerCarWhiteList(
                generateUploadCarWhiteListMsg(appointmentId)
            );
            log.error("Succeed to register car white list for appointment : {}", appointmentId);
        } catch (Exception e) {
            log.error("failed to register car white list for appointment : {}", appointmentId);
        }
    }

    public void registerCarWhiteList(UploadCarWhiteListMsg uploadCarWhiteListMsg) {
        String truckNumber = uploadCarWhiteListMsg.getCar_number();
        SameBarriarCard barriarCard = findOrCreateBarriarCardByTruckNumber(truckNumber);

        List<ParkCard> parkCards = parkCardRepository.findByCardNo(truckNumber);
        if (!parkCards.isEmpty()) {
            parkCardRepository.deleteAll(parkCards);
        }
        ParkCard parkCard = new ParkCard();
        parkCard.setCardNo(truckNumber);
        parkCard.setCarNo(truckNumber);
        parkCard.setAddress(uploadCarWhiteListMsg.getAddress());
        parkCard.setPhone(uploadCarWhiteListMsg.getCarusertel());
        parkCard.setUserName(uploadCarWhiteListMsg.getCarusername());
        parkCard.setCtid(1);
        parkCard.setFctCode(1);
        parkCard.setCardState(1);
        parkCard.setStartDate(ZonedDateTime.now());
        parkCard.setValidDate(ZonedDateTime.now().plusHours(2));
        parkCard.setRegisterDate(ZonedDateTime.now());
        parkCard.setCDate(ZonedDateTime.now());
        parkCard.setCUser(uploadCarWhiteListMsg.getOperator());
        parkCard.setCardMoney(0.0);
        parkCard.setDriveNo(uploadCarWhiteListMsg.getDrive_no());
        parkCard.setCarLocate(uploadCarWhiteListMsg.getCarlocate());
        parkCard.setRemark("服务器数据导入");
        parkCard.setFeePeriod("月");
        parkCard.setLimitDayType(uploadCarWhiteListMsg.getLimitdaytype());
        parkCard.setAreaId(-1);
        parkCard.setZMCarLocateCount(0);
        parkCard.setZMUsedLocateCount(0);

        List<ParkCard> lastParkCard = parkCardRepository.findLastParkCard(PageRequest.of(0, 1, Sort.Direction.DESC, "cid"));
        if (lastParkCard.isEmpty()) {
            parkCard.setCid(1L);
        } else {
            parkCard.setCid(lastParkCard.get(0).getCid() + 1L);
        }

        parkCardRepository.save(parkCard);
    }

    public void deleteCarWhiteList() {

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
}
