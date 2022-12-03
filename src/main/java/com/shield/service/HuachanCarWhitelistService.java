package com.shield.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shield.chepaipark.service.CarWhiteListService;
import com.shield.config.ApplicationProperties;
import com.shield.domain.*;
import com.shield.domain.enumeration.RecordType;
import com.shield.repository.AppointmentRepository;
import com.shield.repository.GateRecordRepository;
import com.shield.repository.RegionRepository;
import com.shield.repository.ShipPlanRepository;
import com.shield.service.dto.AppointmentDTO;
import com.shield.service.dto.ShipPlanDTO;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
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

    private final String AUTH_API = "https://vm.bwhk.net/backend/login";
    private final String REGISTER_CAR_API = "https://vm.bwhk.net/backend/api/deliver";
    private final String REGISTER_CAR_QUERY_API = "https://vm.bwhk.net/backend/api/query?codes=%s";

    private RestTemplate restTemplate;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ShipPlanRepository shipPlanRepository;

    @Autowired
    private ShipPlanService shipPlanService;

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
    private AppointmentService appointmentService;

    @Autowired
    ApplicationProperties applicationProperties;


    @Autowired
    @Qualifier("redisLongTemplate")
    private RedisTemplate<String, Long> redisLongTemplate;

    private static final String OUT_APPLICATION_QUEUE = "OUT_APPLICATION_QUEUE";

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


    private boolean isForbiddenTimePeriod(ZonedDateTime now) {
        // 2个时间段禁行： AM：7:30- 8:30 ， PM：4:30- 5:30
        return !Sets.newHashSet(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).contains(now.getDayOfWeek())
            && (
            (now.getHour() == 7 && now.getMinute() >= 0)
                || (now.getHour() == 8 && now.getMinute() <= 30)
                || (now.getHour() == 16 && now.getMinute() >= 0)
                || (now.getHour() == 17 && now.getMinute() <= 30));
    }


    @Scheduled(fixedDelay = 5000)
    public void scheduleRegisterOutApplication() {

        ZonedDateTime now = ZonedDateTime.now();
        if (isForbiddenTimePeriod(now)) {
            return;
        }
        log.info("scheduleRegisterOutApplication start..");
        try {
            Long planId = redisLongTemplate.opsForList().rightPop(OUT_APPLICATION_QUEUE);
            if (planId == null) {
                return;
            }
            log.info("popRight planId: {} from OUT_APPLICATION_QUEUE", planId);
            Long eventTimeInSeconds = redisLongTemplate.opsForValue().get(OUT_APPLICATION_QUEUE + ":" + planId);
            if (eventTimeInSeconds == null) {
                log.info("planId: {}, expired to register out application", planId);
                return;
            }
            ZonedDateTime eventTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(eventTimeInSeconds), ZoneId.systemDefault());
            shipPlanService.findOne(planId).ifPresent(plan -> this.doRegisterOutApplication(plan, eventTime));
        } catch (Exception e) {
            log.error("scheduleRegisterOutApplication failed", e);
        }
    }


    private void doRegisterOutApplication(ShipPlanDTO plan, ZonedDateTime eventTime) {
        log.info("OUT_APPLY {}: registerOutApplication: {}, eventTime: {}", plan.getApplyId(), plan, eventTime);
        if (env.acceptsProfiles(Profiles.of("dev"))) {
            log.info("[DEV] ignore registerOutApplication");
            return;
        }

        // 如果在15min之内，忽略离场时间/状态带来的脏数据
        boolean dontIgnoreLeaveNoise = eventTime.plusMinutes(30).isBefore(ZonedDateTime.now());
        // 2个时间段禁行： AM：7:30- 8:30 ， PM：4:30- 5:30
        ZonedDateTime now = ZonedDateTime.now();
        if (isForbiddenTimePeriod(now.minusMinutes(5))) {
            // 如果是刚从禁行时段出来，很多离场时间是系统自动补的，不可信。无脑开出门证
            dontIgnoreLeaveNoise = false;
        }
        if (dontIgnoreLeaveNoise && plan.getLeaveTime() != null) {
            log.info("OUT_APPLY {}: leave time is not null, ignore registerOutApplication", plan.getApplyId());
            return;
        }

        AppointmentDTO appointment = appointmentService.findLastByApplyId(plan.getApplyId());
        if (appointment == null) {
            log.info("OUT_APPLY {}: appointment is null, ignore registerOutApplication", plan.getApplyId());
            return;
        }
        if (dontIgnoreLeaveNoise && appointment.getStatus().equals(LEAVE)) {
            log.info("OUT_APPLY {}: appointment status is LEAVE, ignore registerOutApplication", plan.getApplyId());
            return;
        }
        if (appointment.getStatus().equals(LEAVE)) {
            log.info("appointment status is LEAVE, ignore registerOutApplication");
            return;
        }
        User user = userService.getUserWithAuthorities(appointment.getUserId()).get();

        String productNameFixed; // 化产出门证上可以选项：铁粒子、S95矿粉；排队系统内部的命名不太一致，需要做一下转换

        if (plan.getProductName().contains("铁粒子")) {
            productNameFixed = "铁粒子";
        } else if (plan.getProductName().contains("S95")) {
            productNameFixed = "S95矿粉";
        } else {
            log.warn("OUT_APPLY {}: productName not in choices: [铁粒子、S95矿粉], 跳过提交出门证", plan.getApplyId());
            return;
        }

        Map<String, Map<String, String>> customFields = Maps.newHashMap();
        customFields.put("ID", ImmutableBiMap.of("S95矿粉", "17e87b9444a84937b3649953951fca9b", "铁粒子", "59ee2611df56409688c8cdeb9b9eae27"));
        customFields.put("FIELD2", ImmutableBiMap.of("S95矿粉", "SY62", "铁粒子", "SY61"));
        customFields.put("ALLOW_OMG_LIST", ImmutableBiMap.of("S95矿粉", "7#", "铁粒子", "6#"));

        int validTimeInMinutes = applicationProperties.getRegion().getOutApplicationConfig().getValidTimeInMinutes();
        int startTimeOffsetInMinutes = applicationProperties.getRegion().getOutApplicationConfig().getStartTimeOffsetInMinutes();
        ZonedDateTime startTime = ZonedDateTime.now().plusMinutes(startTimeOffsetInMinutes);
        ZonedDateTime endTime = ZonedDateTime.now().plusMinutes(startTimeOffsetInMinutes + validTimeInMinutes);

        String body = "{\n" +
            "  \"ID\": \"" + customFields.get("ID").get(productNameFixed) + "\",\n" +
            "  \"CAR_NO\": \"" + plan.getTruckNumber() + "\",\n" +
            "  \"CAR_COLOR\": \"1\",\n" +
            "  \"CAR_TYPE\": \"car_02\",\n" +
            "  \"CAR_DRIVER_NAME\": \"" + appointment.getDriver() + "\",\n" +
            "  \"CAR_DRIVER_PHONE\": \"\",\n" +
            "  \"WEIGH_NO\": \"" + plan.getApplyId() + "\",\n" +
            "  \"PLAN_NUM\": \"" + plan.getApplyId() + "\",\n" +
            "  \"FIEID3\": \"\",\n" +
            "  \"OUT_VALID_STIME\": \"" + startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\",\n" +
            "  \"OUT_VALID_ETIME\": \"" + endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "\",\n" +
            "  \"MBTYPE_CODE\": \"CM05\",\n" +
            "  \"FIEID2\": \"" + customFields.get("FIELD2").get(productNameFixed) + "\",\n" +
            "  \"FIEID1\": \"宝武环科\",\n" +
            "  \"PRODUCT_NAME\": \"" + productNameFixed + "\",\n" +
            "  \"MAT_MUNIT\": \"t\",\n" +
            "  \"file\": \"\",\n" +
            "  \"BILLING_EMP_CODE\": \"550843\",\n" +
            "  \"BILLING_EMP_PHONE\": \"" + user.getPhone() + "\",\n" +
            "  \"BILLING_EMP_NAME\": \"俞伟\",\n" +
            "  \"BILLING_ORG_CODE\": \"BSHZ\",\n" +
            "  \"BILLING_ORG_NAME\": \"宝武环科\",\n" +
            "  \"REMARK\": \"排队系统提交出门证\",\n" +
            "  \"IS_NEED_WEIGH\": false,\n" +
            "  \"ALLOW_OMG_LIST\": \"" + customFields.get("ALLOW_OMG_LIST").get(productNameFixed) + "\",\n" +
            "  \"TMioBillMDetailEntitys\": [\n" +
            "    {\n" +
            "      \"PRODUCT_NAME\": \"" + plan.getProductName() + "\",\n" +
            "      \"MAT_MTYPE_CODE\": null,\n" +
            "      \"MAT_MTYPE_NAME\": null,\n" +
            "      \"MAT_SPEC\": null,\n" +
            "      \"MAT_NUMBER\": 0,\n" +
            "      \"FIEID1\": \"0\",\n" +
            "      \"MAT_WEIGHT\": 33.1,\n" +
            "      \"MAT_MUNIT\": \"t\",\n" +
            "      \"MAT_NO\": null,\n" +
            "      \"LAY_TABLE_INDEX\": 0\n" +
            "    }\n" +
            "  ],\n" +
            "  \"TMioAnnexListEntitys\": []\n" +
            "}";

        String api = "http://10.70.16.101/MIOS.Web/mio/OutFactory/OutFactory_Info?$ResTargetFun=add&billId=" + customFields.get("ID").get(productNameFixed);
        if (env.acceptsProfiles(Profiles.of("dev"))) {
            // 本地测试用
            api = "http://127.0.0.1:8000/MIOS.Web/mio/OutFactory/OutFactory_Info?$ResTargetFun=add&billId=" + customFields.get("ID").get(productNameFixed);
        }

        String cookie = null;
        cookie = loginAndGetSessionId();

        if (cookie == null) {
            log.warn("ignore, cookie is null");
            return;
        }

        URI uri = URI.create(api);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Cookie", String.format("ASP.NET_SessionId=%s", cookie));
        headers.add("Referer", "http://10.70.16.101/MIOS.Web/mio/outfactory/outfactory_info?$ResTargetFun=add");
        headers.add("Accept", "application/json, text/javascript, */*; q=0.01");
        headers.add("Host", "10.70.16.101");
        headers.add("Connection", "keep-alive");
        headers.add("Origin", "http://10.70.16.101");
        headers.add("Accept-Encoding", "gzip, deflate");
        headers.add("Accept-Language", "en-US,en;q=0.9");
        headers.add("X-Requested-With", "XMLHttpRequest");
        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36");

        log.info("OUT_APPLY {}: Start to invoke outfactory_info api: {}, data: {}", plan.getApplyId(), api, body.replaceAll("\n", ""));
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> result;
        try {
            result = restTemplate.exchange(uri, HttpMethod.POST, request, String.class);
        } catch (HttpClientErrorException e) {
            loginSessionId = null;
            return;
        }
        log.info("OUT_APPLY {}: response, status:{}, body: {}", plan.getApplyId(), result.getStatusCode(), result.getBody());
        if (result.getStatusCode() == HttpStatus.OK) {
            // 返回body示例：
            // {"id":null,"state":"success","message":"保存出厂单成功","data":{"ID":"0ecb3d2c15cf474a9ff062b7318eec5b","BILL_CODE":"OEU2211050156","IS_NEED_WEIGH":false},"jsondata":null}
            JsonObject json = new JsonParser().parse(result.getBody()).getAsJsonObject();
            if (Objects.equals("success", json.get("state").getAsString())) {
                String outApplyId = json.getAsJsonObject("data").get("ID").getAsString();
                if (this.submitOutApplication(plan, outApplyId)) { // 确认提交出门证
                    wxMpMsgService.sendOutgateApplicationSuccessMsg(plan, appointment, startTime, endTime, customFields.get("ALLOW_OMG_LIST").get(productNameFixed));
                }
            }
        }
    }

    /**
     * 提货之后，提交出门证申请
     * 需要分两步：1、申请；2、提交
     */
    public void registerOutApplication(ShipPlanDTO plan) {
        // 加入出门证申请队列
        redisLongTemplate.opsForList().leftPush(OUT_APPLICATION_QUEUE, plan.getId());
        redisLongTemplate.opsForValue().set(OUT_APPLICATION_QUEUE + ":" + plan.getId(), ZonedDateTime.now().toEpochSecond(), 2, TimeUnit.HOURS);
        log.info("registerOutApplication, add to queue: {}", plan);
    }

    /**
     * 确认提交出门证
     */
    public boolean submitOutApplication(ShipPlanDTO plan, String outApplyId) {
        String api = "http://10.70.16.101/MIOS.Web/mio/outfactory/outfactory_confirm?";

        String cookie = null;
        cookie = loginAndGetSessionId();

        if (cookie == null) {
            log.warn("ignore, cookie is null");
            return false;
        }

        URI uri = URI.create(api);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Cookie", String.format("ASP.NET_SessionId=%s", cookie));
        headers.add("Referer", "http://10.70.16.101/MIOS.Web/mio/outfactory/outfactory_qry");
        headers.add("Accept", "application/json, text/javascript, */*; q=0.01");
        headers.add("Host", "10.70.16.101");
        headers.add("Connection", "keep-alive");
        headers.add("Origin", "http://10.70.16.101");
        headers.add("Accept-Encoding", "gzip, deflate");
        headers.add("Accept-Language", "en-US,en;q=0.9");
        headers.add("X-Requested-With", "XMLHttpRequest");
        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36");

        String body = String.format("[\"%s\"]", outApplyId);

        log.info("OUT_APPLY {}: invoke: submitOutApplication: {}", plan.getApplyId(), api);
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> result;
        try {
            result = restTemplate.exchange(uri, HttpMethod.POST, request, String.class);
        } catch (HttpClientErrorException e) {
            loginSessionId = null;
            return false;
        }
        log.info("OUT_APPLY {}: submitOutApplication response, status:{}, body: {}", plan.getApplyId(), result.getStatusCode(), result.getBody());
        // {
        //  "id": null,
        //  "state": "success",
        //  "message": "成功提交1条数据！",
        //  "data": [
        //    "663531784fd84c7ca3704e79f51f262d"
        //  ],
        //  "jsondata": null
        //}
        if (result.getStatusCode() == HttpStatus.OK) {
            JsonObject json = new JsonParser().parse(result.getBody()).getAsJsonObject();
            return Objects.equals("success", json.get("state").getAsString());
        }

        return false;
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
        ZonedDateTime enterTimeStart = ZonedDateTime.now().plusMinutes(3);
        if (appointmentDTO.getStartTime() != null && appointmentDTO.getStartTime().isAfter(enterTimeStart)) {
            enterTimeStart = appointmentDTO.getStartTime();
        }
        registerCarInfo.setEnter_time(enterTimeStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        registerCarInfo.setOut_time(enterTimeStart.plusHours(region.getValidTime()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))); // 最晚进厂时间
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
        List<AppointmentDTO> appointments = appointmentRepository
            .findAllByRegionId(REGION_ID_HUACHAN, START_CHECK, true, ZonedDateTime.now().minusHours(24))
            .stream().map(it -> appointmentMapper.toDto(it)).collect(Collectors.toList());
        List<AppointmentDTO> appointmentsNotSend = appointments.stream().filter(it -> StringUtils.isBlank(it.getHsCode())).collect(Collectors.toList());
        if (appointmentsNotSend.size() > 0) {
            log.warn("Find {} appointments in START_CHECK status without hs_code, need to send...", appointmentsNotSend.size());
            for (AppointmentDTO appointment : appointmentsNotSend) {
                try {
                    this.registerCar(appointment);
                    Thread.sleep(1000);
                } catch (JsonProcessingException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        appointments = appointments.stream().filter(it -> StringUtils.isNotBlank(it.getHsCode())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(appointments)) {
            log.info("[HUACHAN] find {} appointments need to check register status, cars: {}",
                appointments.size(), Joiner.on(",").join(appointments.stream().map(AppointmentDTO::getLicensePlateNumber).collect(Collectors.toList()))
            );

            List<String> codes = appointments.stream().map(AppointmentDTO::getHsCode).collect(Collectors.toList());
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
                    List<AppointmentDTO> changedAppointments = Lists.newArrayList();
                    for (AppointmentDTO appointment : appointments) {
                        CheckResult check = code2Status.get(appointment.getHsCode());
                        if (check == null) {
                            continue;
                        }
                        log.info("[HUACHAN] check car register status, truckNumber: {}, code: {}, bill_status: {}, bill_code: {}",
                            appointment.getLicensePlateNumber(), appointment.getHsCode(), check.getBill_status(), check.getBill_code());
                        if (check.bill_status == 2 || check.bill_status == -1) {
                            // -1 是重复授权, 等同于生效
                            appointment.setStatus(START);
                            appointment.setUpdateTime(ZonedDateTime.now());
                            changedAppointments.add(appointment);

                            // 发送预约成功消息
//                            wxMpMsgService.sendAppointmentSuccessMsg(appointmentMapper.toDto(appointment));
                        } else if (check.bill_status == 3 || check.bill_status == 4 || check.bill_status == 5) {
                            appointment.setValid(Boolean.FALSE);
                            appointment.setUpdateTime(ZonedDateTime.now());
                            changedAppointments.add(appointment);
                        } else if (check.bill_status == 6) {
                            // 已完成
                            appointment.setStatus(ENTER);
                            appointment.setEnterTime(ZonedDateTime.now());
                            appointment.setUpdateTime(ZonedDateTime.now());
                            changedAppointments.add(appointment);

                            if (appointment.getApplyId() != null) {
                                ShipPlan shipPlan = shipPlanRepository.findOneByApplyId(appointment.getApplyId());
                                if (shipPlan != null && shipPlan.getLoadingEndTime() != null) {
                                    List<GateRecord> outRecords = gateRecordRepository.findByTruckNumber(appointment.getRegionId(), RecordType.OUT, appointment.getLicensePlateNumber(), shipPlan.getLoadingEndTime());
                                    if (outRecords.size() > 0) {
                                        appointment.setStatus(LEAVE);
                                        appointment.setLeaveTime(ZonedDateTime.now());
                                    } else {
                                        appointment.setValid(Boolean.FALSE);
                                    }
                                }
                            }
                        } else if (check.bill_status == 1 && appointment.getStartTime().plusHours(region.getValidTime()).isBefore(ZonedDateTime.now())) {
                            // 长时间未审批，超过三小时有效期，直接将预约置为过期
                            log.info("化产：{}, 长时间未审批，超过三小时有效期，直接将预约置为过期", appointment.getLicensePlateNumber());
                            appointment.setValid(false);
                            appointment.setStatus(EXPIRED);
                            appointment.setUpdateTime(ZonedDateTime.now());
                            appointment.setExpireTime(ZonedDateTime.now());
                            changedAppointments.add(appointment);
                        }
                    }
                    if (!CollectionUtils.isEmpty(changedAppointments)) {
                        for (AppointmentDTO appointmentDTO : appointments) {
                            appointmentService.save(appointmentDTO);
                        }
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
            api = "http://127.0.0.1:8000/MIOS.Web/account/login";
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

    /**
     * 从本地MySQL中拿化产区域车辆的出入场数据，并更新预约/计划的状态，出入场时间
     */
    public void updateAppointmentStatusByGateRecords() {
        if (env.acceptsProfiles(Profiles.of("dev"))) {
            log.info("[DEV] ignore updateAppointmentStatusByGateRecords()");
            return;
        }
        // 未进厂预约单（预约成功 --> 进厂)
//        List<Appointment> appointments = appointmentRepository.findAllByStatusAndStartTime(REGION_ID_HUACHAN, START, true, ZonedDateTime.now().minusHours(24));
        List<Appointment> appointments = appointmentRepository.findAllByStatusInAndStartTime(REGION_ID_HUACHAN, Lists.newArrayList(START, START_CHECK), true, ZonedDateTime.now().minusHours(24));
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
            api = "http://127.0.0.1:8000/MIOS.Web/odata/MIOS/T_MIO_CAR_RECORDEntity";
        }
        String today = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String lastDay = ZonedDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Integer skip = 0;
        Integer maxSyncRecords = 1000;
        Integer pageSize = 10;
        Page<GateRecord> maxRecords = gateRecordRepository.findByModifyTime(ZonedDateTime.now().minusDays(1), PageRequest.of(0, 1, Sort.Direction.DESC, "modifyTime"));
        ZonedDateTime maxModifyTime = maxRecords.getContent().size() > 0 ? maxRecords.getContent().get(0).getModifyTime() : null;
        String cookie = null;
        if (env.acceptsProfiles(Profiles.of("dev"))) {
            cookie = "g2ekdusav4zgznx0tjfvfljo";
        } else {
            cookie = loginAndGetSessionId();
        }
        if (cookie == null) {
            return;
        }
        while (true) {
            String param =
                "?$top=" + pageSize +
                    "&$count=true" +
                    "&$skip=" + skip.toString() +
                    "&$orderby=MODIFY_TIME+desc" +
                    "&$filter=IS_DELETE+eq+false+" +
                    "and+RECORD_YMD+ge+" + lastDay + "+" +
                    "and+RECORD_YMD+le+" + today + "+" +
                    "and+MG_NO+eq+'7%23'" +  // 指定7号门
                    "&_=" + ZonedDateTime.now().toEpochSecond();

            String queryApi = api + param;
            URI uri = URI.create(queryApi);
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

            log.info("Start to query car IN/OUT records: {}, skip: {}", queryApi, skip);
            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<String> result;
            try {
                result = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
            } catch (HttpClientErrorException e) {
                loginSessionId = null;
                return;
            }
            log.info("response, status code: {}", result.getStatusCode());
            JsonObject json = new JsonParser().parse(result.getBody()).getAsJsonObject();
            List<GateRecord> records = Lists.newArrayList();
            JsonArray arr = json.getAsJsonArray("value");
            if (arr.size() == 0) {
                break;
            }
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
                ZonedDateTime modifyTime = ZonedDateTime.parse(log.get("MODIFY_TIME").getAsString().substring(0, 19), DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault()));
                modifyTimes.add(modifyTime);
                GateRecord gateRecord = new GateRecord();
                if (exists.containsKey(rid)) {
                    if (exists.get(rid).getDataMd5().equals(dataMd5)) {
                        continue;
                    } else {
                        gateRecord = exists.get(rid);
                    }
                }
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

            if (maxModifyTime != null && modifyTimes.size() > 0) {
                Collections.sort(modifyTimes);
                log.info("Sync last maxModifyTime: {}, now minModifyTime {}", maxModifyTime, modifyTimes.get(0));
                if (modifyTimes.get(0).isBefore(maxModifyTime)) {
                    break;
                }
            }

            if (json.get("@odata.count").getAsInt() < skip + pageSize) {
                break;
            }
            if (skip + arr.size() >= maxSyncRecords) {
                break;
            }
            skip += pageSize;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
