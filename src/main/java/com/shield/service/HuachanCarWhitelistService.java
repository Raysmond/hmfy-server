package com.shield.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shield.chepaipark.service.CarWhiteListService;
import com.shield.domain.*;
import com.shield.domain.enumeration.RecordType;
import com.shield.repository.AppointmentRepository;
import com.shield.repository.GateRecordRepository;
import com.shield.repository.RegionRepository;
import com.shield.repository.ShipPlanRepository;
import com.shield.service.dto.AppointmentDTO;
import com.shield.service.mapper.AppointmentMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.shield.config.Constants.REGION_ID_HUACHAN;
import static com.shield.domain.enumeration.AppointmentStatus.*;

/**
 * 化产区域的出入场数据管理
 */
@Service
@Slf4j
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

    @Autowired
    private Environment env;

    @Autowired
    private GateRecordRepository gateRecordRepository;

    @Autowired
    private CarWhiteListService carWhiteListService;

    @Autowired
    @Qualifier("redisLongTemplate")
    private RedisTemplate<String, Long> redisLongTemplate;


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
        private String applier_name = "智能矿";
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

    public Response registerCar(AppointmentDTO appointmentDTO) throws JsonProcessingException {
        if (env.acceptsProfiles(Profiles.of("dev"))) {
            log.info("[DEV] ignore register car in dev, truckNumber: {} region {}", appointmentDTO.getLicensePlateNumber(), appointmentDTO.getRegionId());
            return null;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", getToken());
        Region region = regionRepository.findById(appointmentDTO.getRegionId()).get();
        List<ShipPlan> shipPlan = shipPlanRepository.findByApplyIdIn(Lists.newArrayList(appointmentDTO.getApplyId()));
        RegisterCarInfo registerCarInfo = new RegisterCarInfo();
        registerCarInfo.setCompany_name(shipPlan.isEmpty() ? "" : shipPlan.get(0).getCompany());
        registerCarInfo.setEnter_time(ZonedDateTime.now().plusMinutes(3).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        registerCarInfo.setOut_time(ZonedDateTime.now().plusHours(region.getValidTime()).plusMinutes(3).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))); // 最晚进厂时间
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
    public void checkRegisterStatus() {
        if (env.acceptsProfiles(Profiles.of("dev"))) {
            log.info("[DEV] ignore checkRegisterStatus()");
            return;
        }
        Region region = regionRepository.findById(REGION_ID_HUACHAN).get();
        if (!region.isOpen()) {
            return;
        }
        List<Appointment> appointments = appointmentRepository.findAllByRegionId(REGION_ID_HUACHAN, START_CHECK, true, ZonedDateTime.now().minusHours(24));
        List<Appointment> appointmentsNotSend = appointments.stream().filter(it -> StringUtils.isBlank(it.getHsCode())).collect(Collectors.toList());
        if (appointmentsNotSend.size() > 0) {
            log.warn("Find {} appointments in START_CHECK status without hs_code, need to send...", appointmentsNotSend.size());
            for (Appointment appointment : appointmentsNotSend) {
                try {
                    this.registerCar(appointmentMapper.toDto(appointment));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }

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
                        log.info("[HUACHAN] check car register status, truckNumber: {}, code: {}, bill_status: {}, bill_code: {}",
                            appointment.getLicensePlateNumber(), appointment.getHsCode(), check.getBill_status(), check.getBill_code());
                        if (check.bill_status == 2) {
                            appointment.setStatus(START);
                            appointment.setStartTime(ZonedDateTime.now());
                            appointment.setUpdateTime(ZonedDateTime.now());
                            changedAppointments.add(appointment);

                            // 发送预约成功消息
                            wxMpMsgService.sendAppointmentSuccessMsg(appointmentMapper.toDto(appointment));
                        } else if (check.bill_status == -1 || check.bill_status == 3 || check.bill_status == 4 || check.bill_status == 5) {
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

    private ZonedDateTime loginTime;
    private String loginSessionId = null;

    /**
     * 获取出入场数据接口 登录
     */
    public String loginAndGetSessionId() {
        if (StringUtils.isNotBlank(loginSessionId) && loginTime.plusHours(6).isAfter(ZonedDateTime.now())) {
            return loginSessionId;
        }
        String api = "http://10.70.16.101/MIOS.Web/account/login";
        if (env.acceptsProfiles(Profiles.of("dev"))) {
            // 本地测试用
            api = "http://127.0.0.1:10180/MIOS.Web/account/login";
        }
        String data = "{" +
            "\"fromurl\" : \"http://10.80.16.101/MIOS.Web\"," +
            "\"LoginAccount\" : \"550843\"," +
            "\"LoginPwd\" : \"3d0355d20070e21744e9d081bca314fe\"," +
            "\"IsAutoLogin\" : false" +
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
        ResponseEntity<String> result = restTemplate.exchange(api, HttpMethod.POST, request, String.class);
        log.info("Login response, api:{}, status code: {}, response: {}", api, result.getStatusCode(), result.getBody());
        if (result.getStatusCode() == HttpStatus.OK) {
            JsonObject res = new JsonParser().parse(result.getBody()).getAsJsonObject();
            if (res.has("state") && res.get("state").getAsString().equals("success")) {
                List<String> cookies = result.getHeaders().get("Set-Cookie");
                for (String cookie : cookies.get(0).split(";")) {
                    if (cookie.split("=")[0].equals("ASP.NET_SessionId")) {
                        String sessionId = cookie.split("=")[1];
                        log.info("ASP.NET_SessionId: {}", sessionId);
                        loginSessionId = sessionId;
                        loginTime = ZonedDateTime.now();
                        return sessionId;
                    }
                }
            }
        }
        return null;
    }

    @Data
    public static class CarInOutResponse {
        @JsonProperty("@odata.context")
        private String context;

        @JsonProperty("@odata.count")
        private String count;

        private List<JsonObject> value = Lists.newArrayList();
    }

    @Data
    public static class CarInOutData {
        @JsonProperty("ID")
        private String ID;
        private String CAR_COLOR;
        private String CAR_NO;
        private String CAR_TYPE;
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
        private String REC_TIME;
        private String REC_VENDOR_CODE;
        private String REMARK;
        private Integer ROAD_NO;
        private String TOKEN_INFO;
    }


    /**
     * 从本地MySQL中拿化产区域车辆的出入场数据，并更新预约/计划的状态，出入场时间
     */
    public void updateAppointmentStatusByGateRecords() {
        if (env.acceptsProfiles(Profiles.of("dev"))) {
            log.info("[DEV] ignore updateAppointmentStatusByGateRecords()");
            return;
        }
        // 未进厂预约单（预约成功 --> 进厂)
        List<Appointment> appointments = appointmentRepository.findAllByStatusAndStartTime(REGION_ID_HUACHAN, START, true, ZonedDateTime.now().minusHours(24));
        if (!CollectionUtils.isEmpty(appointments)) {
            log.info("Start to check appointment GateRecord for {} cars in START status", appointments.size());
            for (Appointment appointment : appointments) {
                List<GateRecord> records = gateRecordRepository.findByTruckNumber(REGION_ID_HUACHAN, RecordType.IN, appointment.getLicensePlateNumber(), appointment.getStartTime());
                if (!records.isEmpty()) {
                    GateRecord record = records.get(0);
                    log.info("Find appointment [id={}, truckNumber={}] GateRecord IN, id={}, recordTime={}", appointment.getId(), appointment.getLicensePlateNumber(), record.getId(), record.getRecordTime());
                    appointment.setEnterTime(record.getRecordTime());
                    appointment.setStatus(ENTER);
                    appointment.setUpdateTime(ZonedDateTime.now());
                    appointmentRepository.save(appointment);
                }
            }
        }

        // 已进厂预约单（进厂 --> 离场)
        List<Appointment> enterAppointments = appointmentRepository.findAllByStatusAndStartTime(REGION_ID_HUACHAN, ENTER, true, ZonedDateTime.now().minusHours(24));
        if (!CollectionUtils.isEmpty(enterAppointments)) {
            log.info("Start to check appointment GateRecord for {} cars in ENTER status", enterAppointments.size());
            for (Appointment appointment : enterAppointments) {
                List<GateRecord> records = gateRecordRepository.findByTruckNumber(REGION_ID_HUACHAN, RecordType.OUT, appointment.getLicensePlateNumber(), appointment.getEnterTime());
                if (!records.isEmpty()) {
                    GateRecord record = records.get(0);
                    log.info("Find appointment [id={}, truckNumber={}] GateRecord OUT, id={}, recordTime={}", appointment.getId(), appointment.getLicensePlateNumber(), record.getId(), record.getRecordTime());
                    appointment.setLeaveTime(record.getRecordTime());
                    appointment.setStatus(LEAVE);
                    appointment.setUpdateTime(ZonedDateTime.now());
                    appointmentRepository.save(appointment);
                }
            }
        }

        Region region = regionRepository.findById(REGION_ID_HUACHAN).get();

        // 待提货且未进厂计划，需要填进厂时间
        List<ShipPlan> shipPlans = shipPlanRepository.findAllByDeliverTime(region.getName(), LocalDate.now().atStartOfDay(ZoneId.systemDefault()).minusDays(1), ZonedDateTime.now(), 1)
            .stream()
            .filter(it -> it.getGateTime() == null)
            .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(shipPlans)) {
            log.info("Start to find gate_time for {} ShipPlan in region {}", shipPlans.size(), region.getId());
            for (ShipPlan shipPlan : shipPlans) {
                List<Appointment> appointmentList = appointmentRepository.findByApplyIdIn(Lists.newArrayList(shipPlan.getApplyId())).stream()
                    .filter(it -> it.getStartTime() != null)
                    .sorted(Comparator.comparing(Appointment::getStartTime).reversed())
                    .collect(Collectors.toList());
                ZonedDateTime beginTime = appointmentList.isEmpty() ? shipPlan.getCreateTime() : appointmentList.get(0).getStartTime();
                List<GateRecord> records = gateRecordRepository.findByTruckNumber(REGION_ID_HUACHAN, RecordType.IN, shipPlan.getTruckNumber(), beginTime);
                if (!records.isEmpty()) {
                    GateRecord record = records.get(0);
                    if (shipPlan.getLoadingStartTime() != null && record.getRecordTime().isAfter(shipPlan.getLoadingStartTime())) {
                        continue;
                    }
                    log.info("Find ShipPlan [applyId={}, truckNumber={}] GateRecord IN, id={}, recordTime={}", shipPlan.getApplyId(), shipPlan.getTruckNumber(), record.getId(), record.getRecordTime());
                    shipPlan.setGateTime(record.getRecordTime());
                    shipPlan.setUpdateTime(ZonedDateTime.now());
                    shipPlanRepository.save(shipPlan);
                    carWhiteListService.delayPutSyncShipPlanIdQueue(shipPlan.getId());
                }
            }
        }

        // 已提货计划，需要填出场时间
        shipPlans = shipPlanRepository
            .findAllByDeliverTime(region.getName(), LocalDate.now().atStartOfDay(ZoneId.systemDefault()).minusDays(1), ZonedDateTime.now(), 3).stream()
            .filter(it -> it.getLeaveTime() == null)
            .filter(it -> it.getLoadingEndTime() != null)
            .sorted(Comparator.comparing(ShipPlan::getLoadingEndTime).reversed())
            .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(shipPlans)) {
            log.info("Start to find leave_time for {} ShipPlan in region {}", shipPlans.size(), region.getId());
            for (ShipPlan shipPlan : shipPlans) {
                List<GateRecord> records = gateRecordRepository.findByTruckNumber(REGION_ID_HUACHAN, shipPlan.getTruckNumber(), ZonedDateTime.now().minusDays(1));
                for (int i = 0; i < records.size() - 1; i++) {
                    if (records.get(i).getRecordType().equals(RecordType.IN)
                        && records.get(i + 1).getRecordType().equals(RecordType.OUT)
//                        && records.get(i).getRecordTime().isBefore(shipPlan.getLoadingStartTime())
                        && records.get(i + 1).getRecordTime().isAfter(shipPlan.getLoadingEndTime())) {
                        GateRecord inRecord = records.get(i);
                        GateRecord outRecord = records.get(i + 1);
                        log.info("Find ShipPlan [applyId={}, truckNumber={}] GateRecord OUT, id={}, recordTime={}", shipPlan.getApplyId(), shipPlan.getTruckNumber(), outRecord.getId(), outRecord.getRecordTime());
                        shipPlan.setGateTime(inRecord.getRecordTime());
                        shipPlan.setLeaveTime(outRecord.getRecordTime());
                        shipPlan.setUpdateTime(ZonedDateTime.now());
                        shipPlanRepository.save(shipPlan);
                        carWhiteListService.delayPutSyncShipPlanIdQueue(shipPlan.getId());
                        break;
                    }
                }
            }
        }
    }

    /**
     * 获取出入场数据接口
     */
    public void syncCarInOutRecords() {
        String api = "http://10.70.16.101/MIOS.Web/odata/MIOS/T_MIO_CAR_RECORDEntity";
        if (env.acceptsProfiles(Profiles.of("dev"))) {
            // 本地测试用
            api = "http://127.0.0.1:10180/MIOS.Web/odata/MIOS/T_MIO_CAR_RECORDEntity";
        }
        String today = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String lastDay = ZonedDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Integer skip = 0;
        Integer total = 10000;
        Page<GateRecord> maxRecords = gateRecordRepository.findByModifyTime(ZonedDateTime.now().minusDays(1), PageRequest.of(0, 1, Sort.Direction.DESC, "modifyTime"));
        ZonedDateTime maxRecTime = maxRecords.getContent().size() > 0 ? maxRecords.getContent().get(0).getModifyTime() : null;

        while (true) {
            String param =
                "?$top=500" +
                    "&$count=true" +
                    "&$skip=" + skip.toString() +
                    "&$orderby=MODIFY_TIME+desc" +
                    "&$filter=IS_DELETE+eq+false+" +
                    "and+RECORD_YMD+ge+" + lastDay + "+" +
                    "and+RECORD_YMD+le+" + today +
                    "&_=" + ZonedDateTime.now().toEpochSecond();

            String queryApi = api + param;
            String cookie = loginAndGetSessionId();
            if (cookie == null) {
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Cookie", String.format("ASP.NET_SessionId=%s", cookie));
            headers.add("Referer", "http://10.70.16.101/MIOS.Web/mio/passrecord/passrecord_qry");
            headers.add("Accept", "application/json, text/javascript, */*; q=0.01");
            headers.add("Host", "10.70.16.101");
            headers.add("Connection", "keep-alive");
            headers.add("Origin", "http://10.70.16.101");
            headers.add("Accept-Encoding", "gzip, deflate");
            headers.add("Accept-Language", "en-US,en;q=0.9");
            headers.add("X-Requested-With", "XMLHttpRequest");
            headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36");

            log.info("Start to query car IN/OUT records: {}", queryApi);
            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<String> result;
            try {
                result = restTemplate.exchange(queryApi, HttpMethod.GET, request, String.class);
            } catch (HttpClientErrorException e) {
                loginSessionId = null;
                return;
//                if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
//
//                }
            }
            log.info("response, status code: {}", result.getStatusCode());
            JsonObject json = new JsonParser().parse(result.getBody()).getAsJsonObject();
            List<GateRecord> records = Lists.newArrayList();
            JsonArray arr = json.getAsJsonArray("value");
            List<String> rids = Lists.newArrayList();
            List<ZonedDateTime> modifyTimes = Lists.newArrayList();
            for (JsonElement ele : arr) {
                JsonObject log = ele.getAsJsonObject();
                rids.add(log.get("ID").getAsString());
            }
            Map<String, GateRecord> exists = gateRecordRepository.findAllByRid(rids).stream().collect(Collectors.toMap(GateRecord::getRid, it -> it));
            for (JsonElement ele : arr) {
                JsonObject log = ele.getAsJsonObject();
                String rid = log.get("ID").getAsString();
                String data = ele.toString();
                String dataMd5 = DigestUtils.md5DigestAsHex(data.getBytes());
                GateRecord gateRecord = new GateRecord();
                if (exists.containsKey(rid)) {
                    if (exists.get(rid).getDataMd5().equals(dataMd5)) {
                        continue;
                    } else {
                        gateRecord = exists.get(rid);
                    }
                }
                ZonedDateTime modifyTime = ZonedDateTime.parse(log.get("MODIFY_TIME").getAsString().substring(0, 19), DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault()));
                modifyTimes.add(modifyTime);
                gateRecord.setRid(rid);
                gateRecord.setData(data);
                gateRecord.setDataMd5(dataMd5);
                gateRecord.setRegionId(REGION_ID_HUACHAN);
                gateRecord.setModifyTime(modifyTime);
                gateRecord.setTruckNumber(log.get("CAR_NO").getAsString());
                if (gateRecord.getTruckNumber().equals("0000000")) {
                    continue;
                }
                gateRecord.setCreateTime(ZonedDateTime.now());
                gateRecord.setRecordType(log.get("INOUT_TYPE").getAsString().equals("IN") ? RecordType.IN : RecordType.OUT);
                gateRecord.setRecordTime(ZonedDateTime.parse(log.get("REC_TIME").getAsString().substring(0, 19), DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault())));
                records.add(gateRecord);
            }

            if (!records.isEmpty()) {
                gateRecordRepository.saveAll(records);
                log.info("Saved {} new GateRecords of regionId {}, offset: {} / {}",
                    records.size(), records.get(0).getRegionId(), skip + records.size(), json.get("@odata.count").getAsInt());
            }

            if (maxRecTime != null && modifyTimes.size() > 0) {
                Collections.sort(modifyTimes);
                log.info("Sync last maxModifyTime: {}, now minModifyTime {}", maxRecTime, modifyTimes.get(0));
                if (modifyTimes.get(0).isBefore(maxRecTime)) {
                    break;
                }
            }

            if (json.get("@odata.count").getAsInt() < skip + 500) {
                break;
            }
            if (skip + arr.size() >= total) {
                break;
            }
            skip += 500;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
