package com.shield.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.shield.domain.Appointment;
import com.shield.domain.Region;
import com.shield.domain.ShipPlan;
import com.shield.domain.User;
import com.shield.domain.enumeration.AppointmentStatus;
import com.shield.repository.AppointmentRepository;
import com.shield.repository.RegionRepository;
import com.shield.repository.ShipPlanRepository;
import com.shield.service.dto.AppointmentDTO;
import com.shield.service.mapper.AppointmentMapper;
import io.github.jhipster.config.JHipsterConstants;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.shield.domain.enumeration.AppointmentStatus.START;
import static com.shield.domain.enumeration.AppointmentStatus.START_CHECK;
import static com.shield.service.impl.AppointmentServiceImpl.REGION_ID_HUACHAN;

@Service
@Slf4j
@Profile(JHipsterConstants.SPRING_PROFILE_PRODUCTION)
public class HuachanCarWhitelistService {
    private String token = null;
    private ZonedDateTime tokenIssueTime;
    private final long TOKEN_VALID_SECONDS = 3600L;

    private final String AUTH_USER_NAME = "btkf";
    //    private final String AUTH_USER_PASSWORD = "btkf@666";
    private final String AUTH_USER_PASSWORD = "EB71A80D1BA26EE0F9760E3B206036533F3E57FEC74D0E8E9FCECCA8017F89D4";

    private final String AUTH_API = "https://cg.meowpapa.com/backend/login";
    private final String REGISTER_CAR_API = "https://cg.meowpapa.com/backend/api/deliver";
    private final String REGISTER_CAR_QUERY_API = "https://cg.meowpapa.com/backend/api/query?codes=%s";

    private RestTemplate restTemplate;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ShipPlanRepository shipPlanRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private WxMpMsgService wxMpMsgService;

    private PasswordEncoder passwordEncoder;

    public HuachanCarWhitelistService() {
        this.restTemplate = new RestTemplate();
        this.passwordEncoder = new MessageDigestPasswordEncoder("SHA-256");
    }

    @Data
    public static class Response {
        private boolean success;
        private String result;
        private String message;
    }

    private String getToken() {
        if (token != null && tokenIssueTime.plusSeconds(TOKEN_VALID_SECONDS).isAfter(ZonedDateTime.now())) {
            return token;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", AUTH_USER_NAME, AUTH_USER_PASSWORD);
        log.info("[HUACHAN] start to get token, api: {}", AUTH_API);
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<Response> result = restTemplate.postForEntity(AUTH_API, request, Response.class);
        if (result.getStatusCode().equals(HttpStatus.OK) && result.getBody() != null && result.getBody().success) {
            Response authResult = result.getBody();
            log.info("[HUACHAN] authentication result: success: {}, result: {}, message: {}", authResult.success, authResult.result, authResult.message);
            token = authResult.getResult();
            tokenIssueTime = ZonedDateTime.now();
        } else {
            log.error("failed to get token, status code : {}", result.getStatusCode());
        }
        return token;
    }

    /**
     * * 参数：
     * *     - `applier_name`*: 申请人姓名
     * *     - `company_name`*: 车辆公司名称
     * *     - `enter_door`*: 入厂门号（1-9）
     * *     - `out_door`*: 出厂门号（1-9）
     * *     - `enter_time`*: 入厂时间，精确到分钟（不得早于当前时间）
     * *     - `out_time`*: 出厂时间，精确到分钟（不得早于当前时间和入厂时间）
     * *     - `reason`*: 入厂事由（送货、取货、其他）
     * *     - `remark`: 备注信息
     * *     - `license`*: 入厂车牌号
     * *     - `license_color`*: 入厂车牌颜色( "白色" => 0, "黄色" => 1, "蓝色" => 2, "黑色" => 3, "黄绿" => 5, "绿色" => 6)
     * *     - `driver`*: 入厂司机姓名
     * *     - `mobile`*: 入厂司机手机号
     * <p>
     * * {
     * *         applier_name: '王小二',
     * *         company_name: '二号车厂',
     * *         enter_door: 3,
     * *         out_door: 7,
     * *         enter_time: '2019-05-26 10:00',
     * *         out_time: '2019-05-26 16:00',
     * *         reason: '送货',
     * *         remark: '这里是备注信息',
     * *         license: "沪A1234",
     * *         license_color: 2,
     * *         driver: "赵钱孙",
     * *         mobile: "13412312312"
     * *     }
     */
    @Data
    public static class RegisterCarInfo {
        private String applier_name;
        private String company_name;
        private Integer enter_door = 7;
        private Integer out_door = 7;
        private String enter_time;
        private String out_time;
        private String reason = "取货";
        private String license;
        private Integer license_color = 1;
        private String driver;
        private String mobile;
    }


    @Scheduled(fixedRate = 20 * 1000)
    public void autoRegisterCar() {
        Region region = regionRepository.findById(REGION_ID_HUACHAN).get();
        if (!region.isOpen()) {
            return;
        }
        List<Appointment> appointments = appointmentRepository.findAllByRegionId(REGION_ID_HUACHAN, START_CHECK, true, ZonedDateTime.now().minusHours(6));
        appointments = appointments.stream().filter(it -> it.getHsCode() == null).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(appointments)) {
            log.info("[HUACHAN] find {} appointments need to register car", appointments.size());
            for (Appointment appointment : appointments) {
                try {
                    log.info("[HUACHAN] start to register car {}, applyId: {}", appointment.getLicensePlateNumber(), appointment.getApplyId());
                    registerCar(appointmentMapper.toDto(appointment));
                } catch (Exception e) {
                    log.error("[HUACHAN] failed to register car {}", appointment.getLicensePlateNumber(), e);
                }
            }
        }
    }

    public Response registerCar(AppointmentDTO appointmentDTO) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", getToken());
        Region region = regionRepository.findById(appointmentDTO.getRegionId()).get();
        List<ShipPlan> shipPlan = shipPlanRepository.findByApplyIdIn(Lists.newArrayList(appointmentDTO.getApplyId()));
        RegisterCarInfo registerCarInfo = new RegisterCarInfo();
        registerCarInfo.setApplier_name(appointmentDTO.getDriver());
        registerCarInfo.setCompany_name(shipPlan.isEmpty() ? "" : shipPlan.get(0).getCompany());
        registerCarInfo.setEnter_time(ZonedDateTime.now().plusMinutes(3).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        registerCarInfo.setOut_time(ZonedDateTime.now().plusHours(3).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        registerCarInfo.setLicense(appointmentDTO.getLicensePlateNumber());
        registerCarInfo.setDriver(appointmentDTO.getDriver());
        if (StringUtils.isNotBlank(appointmentDTO.getUserLogin())) {
            User user = userService.getUserWithAuthoritiesByLogin(appointmentDTO.getUserLogin()).get();
            registerCarInfo.setMobile(user.getPhone());
        }
        String body = objectMapper.writeValueAsString(registerCarInfo);
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        log.info("[HUACHAN] start to register car , truckNumber: {}, body: {}", appointmentDTO.getLicensePlateNumber(), body);
        ResponseEntity<Response> result = restTemplate.postForEntity(REGISTER_CAR_API, request, Response.class);
        Response ret = result.getBody();
        log.info("[HUACHAN] register car result: truckNumber: {}, success: {}, result: {}, message: {}",
            appointmentDTO.getLicensePlateNumber(), ret.success, ret.result, ret.message);

        Appointment appointment = appointmentRepository.findById(appointmentDTO.getId()).get();
        appointment.setHsCode(ret.getResult());
        appointmentRepository.save(appointment);
        return ret;
    }

    /**
     * - `success`: 请求是否成功
     * - `result`:  查询结果列表，为一个对象数组，数组元素的字段如下
     * - `code`: 系统单据号，也就是查询时发送过来的单据号
     * - `bill_code`: 该预约在股份系统生成的单据号（如尚未同步至股份，则该字段为`null`）
     * - `bill_status`: 单据状态(-1: 同步失败， 0：等待同步， 1：待审批， 2：生效中, 3: 已驳回，4：已作废， 5：已过期， 6：已完成)
     * - `message`: 未查询到数据时的信息，**token无效时直接返回401**
     */
    @Data
    public static class CheckResponse {
        private boolean success;
        private String message;
        private List<CheckResult> result = Lists.newArrayList();
    }

    @Data
    public static class CheckResult {
        private String code;
        private String bill_code;
        private Integer bill_status;
    }

    /**
     * 检查预约状态
     */
    @Scheduled(fixedRate = 20 * 1000)
    public void checkRegisterStatus() {
        Region region = regionRepository.findById(REGION_ID_HUACHAN).get();
        if (!region.isOpen()) {
            return;
        }
        List<Appointment> appointments = appointmentRepository.findAllByRegionId(REGION_ID_HUACHAN, START_CHECK, true, ZonedDateTime.now().minusHours(6));
        appointments = appointments.stream().filter(it -> it.getStatus().equals(START_CHECK) && StringUtils.isNotBlank(it.getHsCode())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(appointments)) {
            log.info("[HUACHAN] find {} appointments need to check register status, cars: {}",
                appointments.size(), Joiner.on(",").join(appointments.stream().map(Appointment::getLicensePlateNumber).collect(Collectors.toList()))
            );

            List<String> codes = appointments.stream().map(Appointment::getHsCode).collect(Collectors.toList());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", getToken());
            String api = String.format(REGISTER_CAR_QUERY_API, Joiner.on(",").join(codes));
            ResponseEntity<CheckResponse> result = restTemplate.exchange(
                api, HttpMethod.GET, new HttpEntity<Object>(headers), CheckResponse.class);

            if (result.getStatusCode().equals(HttpStatus.OK)) {
                CheckResponse ret = result.getBody();
                if (ret.success && CollectionUtils.isEmpty(ret.getResult())) {
                    Map<String, CheckResult> code2Status = Maps.newHashMap();
                    for (CheckResult check : ret.getResult()) {
                        code2Status.put(check.getCode(), check);
                    }
                    List<Appointment> changedAppointments = Lists.newArrayList();
                    for (Appointment appointment : appointments) {
                        CheckResult check = code2Status.get(appointment.getHsCode());
                        if (check == null) {
                            continue;
                        }
                        log.info("[HUACHAN] check car register status, truckNumber: {}, code: {}, status: {}, bill_status: {}, bill_code: {}",
                            appointment.getLicensePlateNumber(), appointment.getHsCode(), check.getBill_status(), check.getBill_code());
                        if (check.bill_status == 2) {
                            appointment.setStatus(START);
                            appointment.setUpdateTime(ZonedDateTime.now());
                            changedAppointments.add(appointment);

                            // 发送预约成功消息
                            wxMpMsgService.sendAppointmentSuccessMsg(appointmentMapper.toDto(appointment));
                        } else if (check.bill_status == 3 || check.bill_status == 4 || check.bill_status == 5) {
                            appointment.setValid(Boolean.FALSE);
                            appointment.setUpdateTime(ZonedDateTime.now());
                            changedAppointments.add(appointment);
                        }
                    }
                    if (!CollectionUtils.isEmpty(changedAppointments)) {
                        appointmentRepository.saveAll(changedAppointments);
                    }
                }
            } else {
                log.error("[HUACHAN] failed to check register status, status code: {}", result.getStatusCode());
            }
        }
    }


}
