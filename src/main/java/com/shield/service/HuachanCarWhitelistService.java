package com.shield.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;
import com.shield.domain.Appointment;
import com.shield.domain.Region;
import com.shield.domain.ShipPlan;
import com.shield.domain.User;
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
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
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

    public HuachanCarWhitelistService() {
        this.restTemplate = new RestTemplate();
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
        private String applier_name = "高阳";
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
//        Region region = regionRepository.findById(appointmentDTO.getRegionId()).get();
        List<ShipPlan> shipPlan = shipPlanRepository.findByApplyIdIn(Lists.newArrayList(appointmentDTO.getApplyId()));
        RegisterCarInfo registerCarInfo = new RegisterCarInfo();
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
    public void checkRegisterStatus() throws JsonProcessingException {
        Region region = regionRepository.findById(REGION_ID_HUACHAN).get();
        if (!region.isOpen()) {
            return;
        }
        List<Appointment> appointments = appointmentRepository.findAllByRegionId(REGION_ID_HUACHAN, START_CHECK, true, ZonedDateTime.now().minusHours(24));
        appointments = appointments.stream().filter(it -> StringUtils.isNotBlank(it.getHsCode())).collect(Collectors.toList());
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
                api, HttpMethod.GET, new HttpEntity<>(headers), CheckResponse.class);
            if (result.getStatusCode().equals(HttpStatus.OK)) {
                CheckResponse ret = result.getBody();
                if (ret.success && !CollectionUtils.isEmpty(ret.getResult())) {
                    Map<String, CheckResult> code2Status = Maps.newHashMap();
                    for (CheckResult check : ret.getResult()) {
                        log.info("[HUACHAN] check car register status,  code: {}, bill_status: {}", check.getCode(), check.getBill_status());
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
                            appointment.setStartTime(ZonedDateTime.now());
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


    /**
     * 获取出入场数据接口 登录
     */
    public String loginAndGetSessionId() {
        String api = "http://10.70.16.101/MIOS.Web/account/login";
        String data =
            "{" +
                "\"fromurl\" => \"http://10.80.16.101/MIOS.Web\"," +
                "\"LoginAccount\" => \"550843\"," +
                "\"LoginPwd\" => \"3d0355d20070e21744e9d081bca314fe\"," +
                "\"IsAutoLogin\" => false" +
                "}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Accept", "application/json, text/javascript, */*; q=0.01");
        headers.add("Cookie", "");
        headers.add("host", "10.70.16.101");
        headers.add("Connection", "keep-alive");
        headers.add("Origin", "http://10.70.16.101");
        headers.add("Accept-Encoding", "gzip, deflate");
        headers.add("Accept-Language", "en-US,en;q=0.9");
        headers.add("X-Requested-With", "XMLHttpRequest");
        headers.add("Referer", "http://10.70.16.101/MIOS.Web/account/login?fromurl=http%3a%2f%2f10.70.16.101%2fMIOS.Web%2f");
        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36");
        log.info("Start to login and get session id, api: {}", api);
        HttpEntity<String> request = new HttpEntity<>(data, headers);
        ResponseEntity<String> result = restTemplate.postForEntity(REGISTER_CAR_API, request, String.class);
        log.error("Login response, api:{}, status code: {}, response: {}", api, result.getStatusCode(), result.getBody());
        if (result.getStatusCode() == HttpStatus.OK) {
//            JSONObject res = new JSONObject(result.getBody());
//            if (res.getString("state").equals("success")) {
//                List<String> cookies = result.getHeaders().get("Set-Cookie");
//                for (String cookie : cookies.get(0).split(";")) {
//                    if (cookie.split("=")[0].equals("ASP.NET_SessionId")) {
//                        return cookie.split("=")[1];
//                    }
//                }
//            }
        }
        return null;
    }

    @Data
    public static class CarInOutResponse {
        @SerializedName("@odata.context")
        private String context;

        @SerializedName("@odata.count")
        private String count;

        private List<CarInOutData> value = Lists.newArrayList();
    }

    @Data
    public static class CarInOutData {
        private String ID;
        private String CAR_COLOR;
        private String CAR_NO;
        private String Car_TYPE;
        private String CERT_CODE;
        private String CER_TYPE_CODE;
        private String CER_TYPE_NAME;
        private String CLIENT_MODE;
        private String CREATOR_IP;
        private String CREATOR_MAC;
        private String CREATOR_TIME;
        private String CREATOR_UID;
        private String CREATOR_UNAME;
        private String DATA_AREA;
        private String DATA_SOURCE;
        private String DELETE_IP;
        private String DELETE_MAC;
        private String DELETE_TIME;
        private String DELETE_UID;
        private String DELETE_UNAME;
        private String FIELD1;
        private String FIELD2;
        private String INOUT_TYPE;
        private Boolean IS_ALLOW_PASS;
        private Boolean IS_AUTO_ROD;
        private Boolean IS_DELETE;
        private String LIFE_ROD_TIME;
        private Integer LIFE_ROD_YMD;
        private String MG_NO;
        private String MODIFY_IP;
        private String MODIFY_MAC;
        private String MODIFY_TIME;
        private String MODIFY_UNAME;
        private String RECORD_EXPLAIN;
        private String RECORD_MODE;
        private String RECORD_YMD;
        private String REC_CAR_BCOLOR;
        private String REC_CAR_LENGTH;
        private String REC_CAR_NO;
        private String REC_CAR_TYPE;
        private String REC_DEV_ID;
        private String REC_RECORD_ID;
        private String REC_RECORD_TYPE;
        private Integer REC_SPEED;
        private ZonedDateTime REC_TIME;
        private String REC_VENDOR_CODE;
        private String REMARK;
        private Integer ROAD_NO;
        private String TOKEN_INFO;
    }

    /**
     * 获取出入场数据接口
     */
    public void getCarInOutRecords() {
        String api = "http://10.70.16.101/MIOS.Web/odata/MIOS/T_MIO_CAR_RECORDEntity" +
            "?%24top=100&%24count=true&%24skip=#{skip}&%24orderby=MODIFY_TIME+desc&%24" +
            "filter=IS_DELETE+eq+false+and+contains(CAR_NO%2C%27%E6%B2%AABQ4663%27)+" +
            "and+RECORD_YMD+ge+20191001+" +
            "and+RECORD_YMD+le+20191006" +
            "&_=" + ZonedDateTime.now().toEpochSecond();
        String data =
            "{" +
                "\"fromurl\" => \"http://10.80.16.101/MIOS.Web\"," +
                "\"LoginAccount\" => \"550843\"," +
                "\"LoginPwd\" => \"3d0355d20070e21744e9d081bca314fe\"," +
                "\"IsAutoLogin\" => false" +
                "}";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");
        headers.add("Cookie", "");
        headers.add("Referer", "http://10.70.16.101/MIOS.Web/mio/passrecord/passrecord_qry");
        headers.setContentType(MediaType.APPLICATION_JSON);
        log.info("Start to login and get session id, api: {}", api);
        HttpEntity<String> request = new HttpEntity<>(data, headers);
        ResponseEntity<String> result = restTemplate.postForEntity(REGISTER_CAR_API, request, String.class);
        log.error("Login response, api:{}, status code: {}, response: {}", api, result.getStatusCode(), result.getBody());

        try {
            CarInOutResponse carInOutResponse = objectMapper.readValue(result.getBody(), CarInOutResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
