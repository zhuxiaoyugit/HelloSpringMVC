package com.xrun.user.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xrun.user.feignService.SocialService;
import com.xrun.user.kafka.ProducerService;
import com.xrun.user.mapper.UserAccountMapper;
import com.xrun.user.mapper.UserInfoMapper;
import com.xrun.user.mapper.UserPhotoMapper;
import com.xrun.user.mapper.UserThirdAccountMapper;
import com.xrun.user.pojo.UserAccount;
import com.xrun.user.pojo.UserFriend;
import com.xrun.user.pojo.UserInfo;
import com.xrun.user.pojo.UserPhoto;
import com.xrun.user.pojo.UserStatInfo;
import com.xrun.user.pojo.UserThirdAccount;
import com.xrun.user.util.BaseResponse;
import com.xrun.user.util.Constant;
import com.xrun.user.util.DateUtil;
import com.xrun.user.util.ErrorCode;
import com.xrun.user.util.HttpUtil;
import com.xrun.user.util.MD5;
import com.xrun.user.util.MiGuAuthResponse;

@RequestMapping("/user")
@RestController
public class UserController {
	@Autowired
	private UserThirdAccountMapper userThirdAccountMapper;
	@Autowired
	private UserInfoMapper userInfoMapper;
	@Autowired
	private UserAccountMapper userAccountMapper;
	@Autowired
	private UserPhotoMapper userPhotoMapper;
	@Autowired
	private SocialService socialService;
	@Autowired
	private ProducerService producerService;
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	
	@RequestMapping("/miguUserLogin")
	public BaseResponse<Map<String, Object>> miguUserLogin(HttpServletRequest request) {
		//TODO
		Constant.accessLogger.info("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/miguUserLogin", "in miguUserLogin");
		long starttime = System.currentTimeMillis();
		BaseResponse<Map<String, Object>> response = new BaseResponse<Map<String, Object>>();

		// 请求参数校验
		String migu_token = request.getParameter("migu_token");
		String version = request.getParameter("version");
		String apptype = request.getParameter("apptype");
		if (StringUtils.isEmpty(migu_token) || StringUtils.isEmpty(version) || StringUtils.isEmpty(apptype)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "one or more of the request_parameters is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}", HttpUtil.getIpAddr(request),"/user/miguUserLogin", ErrorCode.REQUEST_PARAMETER_IS_NULL,
					"one or more of the request_parameters is null");
			return response;
		}
		//根据migu_token获取
		String uuid = null;
		String thirdToken = null;
		String thirdType = null;
		String nickname = null;
		String email = null;
		try {
			//获取客户端ip
			String userip = HttpUtil.getIpAddr(request);
			MiGuAuthResponse resp = HttpUtil.miGuAuth(version, apptype, migu_token, userip);
			uuid = resp.getBody().getPassid();
			thirdToken = resp.getBody().getUsessionid();
			thirdType = String.valueOf(resp.getBody().getLoginidtype());

			nickname = resp.getBody().getNickname();
			email = resp.getBody().getEmail();
		}
		catch (Exception e1) {
			e1.printStackTrace();
		}

		if (StringUtils.isEmpty(uuid)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "uuid is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/miguUserLogin", ErrorCode.REQUEST_PARAMETER_IS_NULL, "uuid is null");
			return response;
		}
		if (StringUtils.isEmpty(thirdToken)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "third_token is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/miguUserLogin", ErrorCode.REQUEST_PARAMETER_IS_NULL, "third_token is null");
			return response;
		}
		String names[] = nickname.split(" ");
		String first_name = null;
		String last_name = null;
		if (names.length > 0) {
			first_name = names[0];
		}
		if (names.length > 1) {
			last_name = names[1];
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("uuid", uuid);
		map.put("thirdType", thirdType);

		// 查询第三方库(user_third_account)
		UserThirdAccount loginUserInfo = userThirdAccountMapper.queryUserThirdAccount4Login(map);

		// 第三方账号库有该用户---用户登陆
		if (null != loginUserInfo) {
			// 获取user_id
			Long user_id = loginUserInfo.getUser_id();

			// 查询禁止登陆状态
			UserAccount account = userAccountMapper.queryIsAllowLogin(user_id);
			Integer is_allow_login = account.getIs_allow_login();
			Long ban_over_time = account.getBan_over_time();
			Long now = System.currentTimeMillis();

			// 用户禁止登陆
			if (is_allow_login == 0) {
				if (ban_over_time == 0 || ban_over_time >= now) {
					mkResponse(response, ErrorCode.BAN_LOGIN, "ban login", null);
					Constant.accessLogger.info("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/miguUserLogin", ErrorCode.BAN_LOGIN, "ban login",
							"{user_id = " + user_id + "}");
					return response;
				}
			}

			// 允许登陆
			if (is_allow_login == 1 || (is_allow_login == 0 && ban_over_time < now)) {

				// 查询用户信息(user_info)
				UserInfo userInfo = userInfoMapper.queryUserInfoByUserId(String.valueOf(user_id));
				String user_token = request.getSession().getId();

				try {
					// 更新最近登录时间
					Map<String, Object> baseMap = new HashMap<>();
					baseMap.put("sys_time", System.currentTimeMillis());
					baseMap.put("user_id", user_id);
					int num = userAccountMapper.updateLastLoginTime(baseMap);
				}
				catch (Exception e) {
					e.printStackTrace();
					mkResponse(response, ErrorCode.ERROR, "update last login time error", null);
					Constant.operateLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/miguUserLogin", ErrorCode.ERROR, "update last login time error",
							" {user_id = " + user_id + ",uuid = " + uuid + ",third_token = " + thirdToken + ",third_type = " + thirdType
									+ "}");
				}

				if (null == userInfo) {
					userInfo = new UserInfo();
					userInfo.setUser_id(user_id);
					userInfo.setEmail(email);
					userInfo.setFirst_name(first_name);
					userInfo.setLast_name(last_name);
					userInfoMapper.addUserInfo(userInfo);

				}

				if (null != userInfo) {
					// 查询注册时间
					Long add_time = userAccountMapper.getAddTimeByUserId(String.valueOf(user_id));

					Map<String, Object> resultMap = new HashMap<>();
					resultMap.put("user_id", userInfo.getUser_id());
					resultMap.put("first_name", userInfo.getFirst_name());
					resultMap.put("last_name", userInfo.getLast_name());
					if (StringUtils.isEmpty(userInfo.getFirst_name()) && StringUtils.isEmpty(userInfo.getLast_name())) {
						resultMap.put("user_name", null);
					}
					else if (StringUtils.isEmpty(userInfo.getLast_name())) {
						resultMap.put("user_name", userInfo.getFirst_name());
					}
					else {
						resultMap.put("user_name", userInfo.getFirst_name() + " " + userInfo.getLast_name());
					}

					resultMap.put("email", userInfo.getEmail());
					resultMap.put("country", userInfo.getCountry());
					resultMap.put("city", userInfo.getCity());
					resultMap.put("positions", userInfo.getPositions());
					resultMap.put("intro", userInfo.getIntro());
					resultMap.put("avatar_url", userInfo.getAvatar_url());
					resultMap.put("gender", userInfo.getGender());
					resultMap.put("birthday", userInfo.getBirthday());
					resultMap.put("height", userInfo.getHeight());
					resultMap.put("weight", userInfo.getWeight());
					resultMap.put("user_token", user_token);
					resultMap.put("third_type", Integer.parseInt(thirdType));
					resultMap.put("add_time", add_time);

					try {
						String redis_key = "xrun-users:" + user_id;

						String user_info_complete = (String) stringRedisTemplate.opsForHash().get(redis_key, "user_info_complete");
						if (StringUtils.isEmpty(user_info_complete)) {
							resultMap.put("user_info_complete", 0);
						}
						else {
							resultMap.put("user_info_complete", Integer.parseInt(user_info_complete));
						}

						// 更新redis用户会话信息
						stringRedisTemplate.opsForHash().put(redis_key, "user_token", user_token);
						stringRedisTemplate.opsForHash().put(redis_key, "third_type", thirdType);

						mkResponse(response, ErrorCode.SUCCESS, "login success", resultMap);

						long tc = System.currentTimeMillis() - starttime;
						Constant.accessLogger.info("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/miguUserLogin", ErrorCode.SUCCESS, "SUCCESS",
								" {user_id = " + user_id + ",uuid = " + uuid + ",third_token = " + thirdToken + ",third_type = " + thirdType
										+ ",ms = " + tc + "}");
					}
					catch (Exception e) {
						e.printStackTrace();
						mkResponse(response, ErrorCode.UPDATE_REDIS_ERROR, "update redis error", null);
						Constant.operateLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/miguUserLogin", ErrorCode.UPDATE_REDIS_ERROR,
								"update redis error", "{user_id = " + user_id + "}");
					}
				}

			}
		}
		// 第三方账号库没有该用户---用户注册
		else {
			// 获取header信息
			String app_version = request.getHeader("app_version");
			String pc_type = request.getHeader("pc_type");
			String sys_type = request.getHeader("sys_type");
			String sys_version = request.getHeader("sys_version");
			String app_channel = request.getHeader("app_channel");
			String user_token = request.getSession().getId();
			String mac_id = request.getHeader("mac_id");
			// 设置用户账号初始信息
			UserAccount userAccount = new UserAccount();
			userAccount.setApp_channel(app_channel);
			userAccount.setApp_version(app_version);
			userAccount.setPc_type(pc_type);
			userAccount.setSys_type(sys_type);
			userAccount.setSys_version(sys_version);
			userAccount.setUser_token(user_token);
			userAccount.setMac_id(mac_id);
			userAccount.setIs_allow_login(1);

			int num1 = 0;
			try {
				num1 = userAccountMapper.addUserAccountFirst(userAccount);
			}
			catch (Exception e) {
				e.printStackTrace();
				mkResponse(response, ErrorCode.ERROR, "add user account error", null);
				Constant.operateLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/miguUserLogin", ErrorCode.ERROR, "add user account error",
						"{uuid = " + uuid + ",third_type = " + thirdType + "}");
			}
			// 插入用户登陆信息后 ，获取user_id
			Long user_id = userAccount.getId();
			if (num1 > 0) {
				// 设置第三方登陆初始信息
				UserThirdAccount userThirdAccount = new UserThirdAccount();
				userThirdAccount.setThird_token(thirdToken);
				userThirdAccount.setThird_type(Integer.parseInt(thirdType));
				userThirdAccount.setUser_id(user_id);
				userThirdAccount.setUuid(uuid);
				// 插入第三方登陆信息(user_third_account)
				int num2 = 0;
				try {
					num2 = userThirdAccountMapper.addUserThirdAccount(userThirdAccount);
				}
				catch (Exception e) {
					e.printStackTrace();
					mkResponse(response, ErrorCode.ERROR, "add user third account error", null);
					Constant.operateLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/miguUserLogin", ErrorCode.ERROR, "add user third account error",
							"{user_id = " + user_id + "}");
				}

				if (num2 > 0) {
					// 设置用户初始信息

					UserInfo userInfo = new UserInfo();
					userInfo.setUser_id(user_id);
					userInfo.setGender(0);

					userInfo.setFirst_name(first_name);
					userInfo.setLast_name(last_name);

					userInfo.setEmail(email);
					// 插入用户信息
					int num3 = 0;
					try {
						num3 = userInfoMapper.addUserInfo(userInfo);
					}
					catch (Exception e) {
						e.printStackTrace();
						mkResponse(response, ErrorCode.ERROR, "add user info error", null);
						Constant.operateLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/miguUserLogin", ErrorCode.ERROR, "add user info error",
								"{user_id = " + user_id + "}");
					}

					if (num3 > 0) {
						// 查询注册时间
						Long add_time = userAccountMapper.getAddTimeByUserId(String.valueOf(user_id));

						Map<String, Object> resultMap = new HashMap<>();
						resultMap.put("user_id", userInfo.getUser_id());
						resultMap.put("first_name", userInfo.getFirst_name());
						resultMap.put("last_name", userInfo.getLast_name());
						if (StringUtils.isEmpty(userInfo.getFirst_name()) && StringUtils.isEmpty(userInfo.getLast_name())) {
							resultMap.put("user_name", null);
						}
						else if (StringUtils.isEmpty(userInfo.getLast_name())) {
							resultMap.put("user_name", userInfo.getFirst_name());
						}
						else {
							resultMap.put("user_name", userInfo.getFirst_name() + " " + userInfo.getLast_name());
						}
						resultMap.put("email", userInfo.getEmail());
						resultMap.put("country", userInfo.getCountry());
						resultMap.put("city", userInfo.getCity());
						resultMap.put("positions", userInfo.getPositions());
						resultMap.put("intro", userInfo.getIntro());
						resultMap.put("avatar_url", userInfo.getAvatar_url());
						resultMap.put("gender", userInfo.getGender());
						resultMap.put("birthday", userInfo.getBirthday());
						resultMap.put("height", userInfo.getHeight());
						resultMap.put("weight", userInfo.getWeight());
						resultMap.put("user_token", user_token);
						resultMap.put("third_type", Integer.parseInt(thirdType));
						resultMap.put("user_info_complete", 0);
						resultMap.put("add_time", add_time);

						mkResponse(response, ErrorCode.SUCCESS, "register success", resultMap);

						//发送KAFKA消息
//						producerService.sendMessage(
//								"{\"msg_type\": 0,\"user_id\": " + user_id + ",\"add_time\": " + System.currentTimeMillis() + "}");
						
						String message="{\"msg_type\": 0,\"user_id\": " + user_id + ",\"add_time\": " + System.currentTimeMillis() + "}";
						kafkaTemplate.send(Constant.kafka_topic, message);
						
						// 更新redis用户会话信息
						try {
							String redis_key = "xrun-users:" + String.valueOf(user_id);
							stringRedisTemplate.opsForHash().put(redis_key, "user_token", user_token);
							stringRedisTemplate.opsForHash().put(redis_key, "third_type", thirdType);

							if (StringUtils.isEmpty(first_name) && StringUtils.isEmpty(last_name)) {
								stringRedisTemplate.opsForHash().put(redis_key, "user_name", "");
							}
							else if (null == last_name) {
								stringRedisTemplate.opsForHash().put(redis_key, "user_name", first_name);
							}
							else {
								stringRedisTemplate.opsForHash().put(redis_key, "user_name", first_name + " " + last_name);
							}
							long tc = System.currentTimeMillis() - starttime;
							Constant.accessLogger.info("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/miguUserLogin", ErrorCode.SUCCESS, "SUCCESS",
									"{user_id = " + user_id + ",uuid = " + uuid + ",third_token = " + thirdToken + ",third_type = "
											+ thirdType + " ,ms = " + tc + "}");
						}
						catch (Exception e) {
							e.printStackTrace();
							mkResponse(response, ErrorCode.UPDATE_REDIS_ERROR, "update redis error", null);
							Constant.operateLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/miguUserLogin", ErrorCode.UPDATE_REDIS_ERROR,
									"update redis error", "{user_id = " + user_id + "}");
						}
					}
				}
			}

		}

		return response;
	}

	@RequestMapping("/login3")
	public BaseResponse<Map<String, Object>> login3(HttpServletRequest request) {
		// TODO
		Constant.accessLogger.info("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/login3", "in login3");
		long starttime = System.currentTimeMillis();

		BaseResponse<Map<String, Object>> response = new BaseResponse<Map<String, Object>>();

		String uuid = request.getParameter("uuid");
		String thirdToken = request.getParameter("third_token");
		String thirdType = request.getParameter("third_type");

		if (StringUtils.isEmpty(uuid)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "uuid is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/login3", ErrorCode.REQUEST_PARAMETER_IS_NULL, "uuid is null");
			return response;
		}
		if (StringUtils.isEmpty(thirdToken)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "third_token is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}", HttpUtil.getIpAddr(request),"/user/login3", ErrorCode.REQUEST_PARAMETER_IS_NULL, "third_token is null");
			return response;
		}

		Map<String, String> map = new HashMap<String, String>();
		map.put("uuid", uuid);
		map.put("thirdType", thirdType);

		// 查询第三方库(user_third_account)
		UserThirdAccount loginUserInfo = userThirdAccountMapper.queryUserThirdAccount4Login(map);

		// 第三方账号库有该用户---用户登陆
		if (null != loginUserInfo) {
			// 获取user_id
			Long user_id = loginUserInfo.getUser_id();

			// 查询禁止登陆状态
			UserAccount account = userAccountMapper.queryIsAllowLogin(user_id);
			Integer is_allow_login = account.getIs_allow_login();
			Long ban_over_time = account.getBan_over_time();
			Long now = System.currentTimeMillis();
			// 用户禁止登陆
			if (is_allow_login == 0) {
				if (ban_over_time == 0 || ban_over_time >= now) {
					mkResponse(response, ErrorCode.BAN_LOGIN, "ban login", null);
					Constant.accessLogger.info("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/login3", ErrorCode.BAN_LOGIN, "ban login",
							"{user_id = " + user_id + "}");
					return response;
				}
			}
			// 允许登陆
			if (is_allow_login == 1 || (is_allow_login == 0 && ban_over_time < now)) {

				// 查询用户信息(user_info)
				UserInfo userInfo = userInfoMapper.queryUserInfoByUserId(String.valueOf(user_id));
				String user_token = request.getSession().getId();

				try {
					// 更新最近登录时间
					Map<String, Object> baseMap = new HashMap<>();
					baseMap.put("sys_time", System.currentTimeMillis());
					baseMap.put("user_id", user_id);
					int num = userAccountMapper.updateLastLoginTime(baseMap);
				}
				catch (Exception e) {
					e.printStackTrace();
					mkResponse(response, ErrorCode.ERROR, "update last login time error", null);
					Constant.operateLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/login3", ErrorCode.ERROR, "update last login time error",
							" {user_id = " + user_id + ",uuid = " + uuid + ",third_token = " + thirdToken + ",third_type = " + thirdType
									+ "}");
				}
				
				if (null == userInfo) {
					userInfo = new UserInfo();
					userInfo.setUser_id(user_id);
					userInfoMapper.addUserInfo(userInfo);

				}
				if (null != userInfo) {
					// 查询注册时间
					Long add_time = userAccountMapper.getAddTimeByUserId(String.valueOf(user_id));

					Map<String, Object> resultMap = new HashMap<>();
					resultMap.put("user_id", userInfo.getUser_id());
					resultMap.put("first_name", userInfo.getFirst_name());
					resultMap.put("last_name", userInfo.getLast_name());
					if (StringUtils.isEmpty(userInfo.getFirst_name()) && StringUtils.isEmpty(userInfo.getLast_name())) {
						resultMap.put("user_name", null);
					}
					else if (StringUtils.isEmpty(userInfo.getLast_name())) {
						resultMap.put("user_name", userInfo.getFirst_name());
					}
					else {
						resultMap.put("user_name", userInfo.getFirst_name() + " " + userInfo.getLast_name());
					}
					resultMap.put("email", userInfo.getEmail());
					resultMap.put("country", userInfo.getCountry());
					resultMap.put("city", userInfo.getCity());
					resultMap.put("positions", userInfo.getPositions());
					resultMap.put("intro", userInfo.getIntro());
					resultMap.put("avatar_url", userInfo.getAvatar_url());
					resultMap.put("gender", userInfo.getGender());
					resultMap.put("birthday", userInfo.getBirthday());
					resultMap.put("height", userInfo.getHeight());
					resultMap.put("weight", userInfo.getWeight());
					resultMap.put("user_token", user_token);
					resultMap.put("third_type", Integer.parseInt(thirdType));
					resultMap.put("add_time", add_time);
					try {
						String redis_key = "xrun-users:" + user_id;

						String user_info_complete = (String) stringRedisTemplate.opsForHash().get(redis_key, "user_info_complete");
						if (StringUtils.isEmpty(user_info_complete)) {
							resultMap.put("user_info_complete", 0);
						}
						else {
							resultMap.put("user_info_complete", Integer.parseInt(user_info_complete));
						}

						// 更新redis用户会话信息
						stringRedisTemplate.opsForHash().put(redis_key, "user_token", user_token);
						stringRedisTemplate.opsForHash().put(redis_key, "third_type", thirdType);

						mkResponse(response, ErrorCode.SUCCESS, "login success", resultMap);

						long tc = System.currentTimeMillis() - starttime;
						Constant.accessLogger.info("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/login3", ErrorCode.SUCCESS, "SUCCESS",
								" {user_id = " + user_id + ",uuid = " + uuid + ",third_token = " + thirdToken + ",third_type = " + thirdType
										+ ",ms = " + tc + "}");
					}
					catch (Exception e) {
						e.printStackTrace();
						mkResponse(response, ErrorCode.UPDATE_REDIS_ERROR, "update redis error", null);
						Constant.operateLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/login3", ErrorCode.UPDATE_REDIS_ERROR,
								"update redis error", "{user_id = " + user_id + "}");
					}
				}
			}
		}
		// 第三方账号库没有该用户---用户注册
		else {
			// 获取header信息
			String app_version = request.getHeader("app_version");
			String pc_type = request.getHeader("pc_type");
			String sys_type = request.getHeader("sys_type");
			String sys_version = request.getHeader("sys_version");
			String app_channel = request.getHeader("app_channel");
			String user_token = request.getSession().getId();
			String mac_id = request.getHeader("mac_id");
			// 设置用户账号初始信息
			UserAccount userAccount = new UserAccount();
			userAccount.setApp_channel(app_channel);
			userAccount.setApp_version(app_version);
			userAccount.setPc_type(pc_type);
			userAccount.setSys_type(sys_type);
			userAccount.setSys_version(sys_version);
			userAccount.setUser_token(user_token);
			userAccount.setMac_id(mac_id);
			userAccount.setIs_allow_login(1);

			int num1 = 0;
			try {
				num1 = userAccountMapper.addUserAccountFirst(userAccount);
			}
			catch (Exception e) {
				e.printStackTrace();
				mkResponse(response, ErrorCode.ERROR, "add user account error", null);
				Constant.operateLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/login3", ErrorCode.ERROR, "add user account error",
						"{uuid = " + uuid + ",third_type = " + thirdType + "}");
			}
			// 插入用户登陆信息后 ，获取user_id
			Long user_id = userAccount.getId();
			if (num1 > 0) {
				// 设置第三方登陆初始信息
				UserThirdAccount userThirdAccount = new UserThirdAccount();
				userThirdAccount.setThird_token(thirdToken);
				userThirdAccount.setThird_type(Integer.parseInt(thirdType));
				userThirdAccount.setUser_id(user_id);
				userThirdAccount.setUuid(uuid);
				// 插入第三方登陆信息(user_third_account)
				int num2 = 0;
				try {
					num2 = userThirdAccountMapper.addUserThirdAccount(userThirdAccount);
				}
				catch (Exception e) {
					e.printStackTrace();
					mkResponse(response, ErrorCode.ERROR, "add user third account error", null);
					Constant.operateLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/login3", ErrorCode.ERROR, "add user third account error",
							"{user_id = " + user_id + "}");
				}

				if (num2 > 0) {
					// 设置用户初始信息
					UserInfo userInfo = new UserInfo();
					userInfo.setUser_id(user_id);
					userInfo.setGender(0);
					//					userInfo.setFirst_name("MRer");
					//					userInfo.setLast_name(String.valueOf(user_id));
					// 插入用户信息
					int num3 = 0;
					try {
						num3 = userInfoMapper.addUserInfo(userInfo);
					}
					catch (Exception e) {
						e.printStackTrace();
						mkResponse(response, ErrorCode.ERROR, "add user info error", null);
						Constant.operateLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/login3", ErrorCode.ERROR, "add user info error",
								"{user_id = " + user_id + "}");
					}

					if (num3 > 0) {
						// 查询注册时间
						Long add_time = userAccountMapper.getAddTimeByUserId(String.valueOf(user_id));

						Map<String, Object> resultMap = new HashMap<>();
						resultMap.put("user_id", userInfo.getUser_id());
						resultMap.put("first_name", userInfo.getFirst_name());
						resultMap.put("last_name", userInfo.getLast_name());
						if (StringUtils.isEmpty(userInfo.getFirst_name()) && StringUtils.isEmpty(userInfo.getLast_name())) {
							resultMap.put("user_name", null);
						}
						else if (StringUtils.isEmpty(userInfo.getLast_name())) {
							resultMap.put("user_name", userInfo.getFirst_name());
						}
						else {
							resultMap.put("user_name", userInfo.getFirst_name() + " " + userInfo.getLast_name());
						}
						resultMap.put("email", userInfo.getEmail());
						resultMap.put("country", userInfo.getCountry());
						resultMap.put("city", userInfo.getCity());
						resultMap.put("positions", userInfo.getPositions());
						resultMap.put("intro", userInfo.getIntro());
						resultMap.put("avatar_url", userInfo.getAvatar_url());
						resultMap.put("gender", userInfo.getGender());
						resultMap.put("birthday", userInfo.getBirthday());
						resultMap.put("height", userInfo.getHeight());
						resultMap.put("weight", userInfo.getWeight());
						resultMap.put("user_token", user_token);
						resultMap.put("third_type", Integer.parseInt(thirdType));
						resultMap.put("user_info_complete", 0);
						resultMap.put("add_time", add_time);

						mkResponse(response, ErrorCode.SUCCESS, "register success", resultMap);

						//发送KAFKA消息
//						producerService.sendMessage(
//								"{\"msg_type\": 0,\"user_id\": " + user_id + ",\"add_time\": " + System.currentTimeMillis() + "}");
						
						String message="{\"msg_type\": 0,\"user_id\": " + user_id + ",\"add_time\": " + System.currentTimeMillis() + "}";
						kafkaTemplate.send(Constant.kafka_topic, message);
						// 更新redis用户会话信息
						try {
							String redis_key = "xrun-users:" + String.valueOf(user_id);
							stringRedisTemplate.opsForHash().put(redis_key, "user_token", user_token);
							stringRedisTemplate.opsForHash().put(redis_key, "third_type", thirdType);
							if (StringUtils.isEmpty(userInfo.getFirst_name()) && StringUtils.isEmpty(userInfo.getLast_name())) {
								stringRedisTemplate.opsForHash().put(redis_key, "user_name", "");
							}
							else if (StringUtils.isEmpty(userInfo.getLast_name())) {
								stringRedisTemplate.opsForHash().put(redis_key, "user_name", userInfo.getFirst_name());
							}
							else {
								stringRedisTemplate.opsForHash().put(redis_key, "user_name",
										userInfo.getFirst_name() + " " + userInfo.getLast_name());
							}
							long tc = System.currentTimeMillis() - starttime;
							Constant.accessLogger.info("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/login3", ErrorCode.SUCCESS, "SUCCESS",
									"{user_id = " + user_id + ",uuid = " + uuid + ",third_token = " + thirdToken + ",third_type = "
											+ thirdType + " ,ms = " + tc + "}");
						}
						catch (Exception e) {
							e.printStackTrace();
							mkResponse(response, ErrorCode.UPDATE_REDIS_ERROR, "update redis error", null);
							Constant.operateLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/login3", ErrorCode.UPDATE_REDIS_ERROR,
									"update redis error", "{user_id = " + user_id + "}");
						}
					}
				}
			}

		}

		return response;
	}

	@RequestMapping("/updateUserInfo")
	public BaseResponse<Map<String, Object>> updateUserInfo(HttpServletRequest request) {
		// TODO
		long starttime = System.currentTimeMillis();
		BaseResponse<Map<String, Object>> response = new BaseResponse<Map<String, Object>>();

		// 获取用户信息
		String user_id = request.getParameter("user_id");

		if (StringUtils.isEmpty(user_id)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_id is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/updateUserInfo", ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_id is null");
			return response;
		}
		// 验证该user_id 是否有记录
		UserInfo userInfo = userInfoMapper.queryUserInfoByUserId(user_id);

		if (null == userInfo) {
			mkResponse(response, ErrorCode.QUERY_USER_INFO_BY_ID_AND_TOKEN_ERROR, "no user", null);
			Constant.accessLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/updateUserInfo", ErrorCode.QUERY_USER_INFO_BY_ID_AND_TOKEN_ERROR, "no user",
					"{user_id = " + user_id + "}");
			return response;
		}

		String first_name = request.getParameter("first_name");
		if (StringUtils.isEmpty(first_name)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "first_name is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/updateUserInfo", ErrorCode.REQUEST_PARAMETER_IS_NULL, "first_name is null");
			return response;
		}
		String last_name = request.getParameter("last_name");
		if (StringUtils.isEmpty(last_name)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "last_name is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/updateUserInfo", ErrorCode.REQUEST_PARAMETER_IS_NULL, "last_name is null");
			return response;
		}

		String email = request.getParameter("email");
		String country = request.getParameter("country");
		String city = request.getParameter("city");
		String positions = request.getParameter("positions");
		String intro = request.getParameter("intro");
		String gender = request.getParameter("gender");
		String birthday = request.getParameter("birthday");
		String height = request.getParameter("height");
		String weight = request.getParameter("weight");
		String avatar_url = request.getParameter("avatar_url");

		Map<String, String> map = new HashMap<>();
		map.put("user_id", user_id);
		map.put("first_name", first_name);
		map.put("last_name", last_name);
		map.put("email", email);
		map.put("country", country);
		map.put("city", city);
		map.put("positions", positions);
		map.put("intro", intro);
		map.put("gender", gender);
		map.put("birthday", birthday);
		map.put("height", height);
		map.put("weight", weight);
		map.put("avatar_url", avatar_url);

		// 更新用户信息(user_info)
		try {
			int num = userInfoMapper.updateUserInfoByUserId(map);

			long tc = System.currentTimeMillis() - starttime;
			if (num == 1) {
				userInfo = userInfoMapper.queryUserInfoByUserId(user_id);
				Long add_time = userAccountMapper.getAddTimeByUserId(user_id);
				// userInfo.setUser_token(user_token);

				Map<String, Object> resultMap = new HashMap<>();
				resultMap.put("user_id", userInfo.getUser_id());
				resultMap.put("first_name", userInfo.getFirst_name());
				resultMap.put("last_name", userInfo.getLast_name());
				resultMap.put("user_name", userInfo.getFirst_name() + " " + userInfo.getLast_name());
				resultMap.put("email", userInfo.getEmail());
				resultMap.put("country", userInfo.getCountry());
				resultMap.put("city", userInfo.getCity());
				resultMap.put("positions", userInfo.getPositions());
				resultMap.put("intro", userInfo.getIntro());
				resultMap.put("avatar_url", userInfo.getAvatar_url());
				resultMap.put("gender", userInfo.getGender());
				resultMap.put("birthday", userInfo.getBirthday());
				resultMap.put("height", userInfo.getHeight());
				resultMap.put("weight", userInfo.getWeight());
				resultMap.put("add_time", add_time);
				resultMap.put("user_info_complete", 1);
				mkResponse(response, ErrorCode.SUCCESS, "update info success", resultMap);

				// 更新redis用户会话信息
				String redis_key = "xrun-users:" + user_id;

				if (StringUtils.isEmpty(avatar_url)) {
					stringRedisTemplate.opsForHash().put(redis_key, "avatar_url", "");
				}
				else {
					stringRedisTemplate.opsForHash().put(redis_key, "avatar_url", avatar_url);
				}

				if (StringUtils.isEmpty(first_name) && StringUtils.isEmpty(last_name)) {
					stringRedisTemplate.opsForHash().put(redis_key, "user_name", "");
				}
				else {
					stringRedisTemplate.opsForHash().put(redis_key, "user_name", first_name + " " + last_name);
				}

				if (StringUtils.isEmpty(country)) {
					stringRedisTemplate.opsForHash().put(redis_key, "country", "");
				}
				else {
					stringRedisTemplate.opsForHash().put(redis_key, "country", country);
				}

				if (StringUtils.isEmpty(city)) {
					stringRedisTemplate.opsForHash().put(redis_key, "city", "");
				}
				else {
					stringRedisTemplate.opsForHash().put(redis_key, "city", city);
				}
				stringRedisTemplate.opsForHash().put(redis_key, "user_info_complete", "1");

				Constant.accessLogger.info("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/updateUserInfo", ErrorCode.SUCCESS, "SUCCESS",
						"{user_id = " + user_id + ",ms = " + tc + "}");
			}
			else {
				mkResponse(response, ErrorCode.UPDATE_USER_INFO_ERROR, "update info failed", null);
				Constant.operateLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/updateUserInfo", ErrorCode.UPDATE_USER_INFO_ERROR,
						"update userInfo failed", "{user_id = " + user_id + ",ms = " + tc + "}");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			mkResponse(response, ErrorCode.ERROR, "error", null);
			Constant.operateLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/updateUserInfo", ErrorCode.ERROR, "ERROR", "{user_id = " + user_id + "}");
		}

		return response;

	}

	@RequestMapping("/queryUserInfo")
	public BaseResponse<Map<String, Object>> queryUserInfo(HttpServletRequest request) {
		// TODO
		long starttime = System.currentTimeMillis();
		BaseResponse<Map<String, Object>> response = new BaseResponse<Map<String, Object>>();

		String user_id = request.getParameter("user_id");
		String user_token = request.getParameter("user_token");
		String dest_user_id = request.getParameter("dest_user_id");

		if (StringUtils.isEmpty(user_id)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_id is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}", HttpUtil.getIpAddr(request),"/user/queryUserInfo", ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_id is null");
			return response;
		}
		if (StringUtils.isEmpty(dest_user_id)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "dest_user_id is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/queryUserInfo", ErrorCode.REQUEST_PARAMETER_IS_NULL, "dest_user_id is null");
			return response;
		}

		// 查询参数
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("user_token", user_token);
		paramMap.put("dest_user_id", dest_user_id);

		// 查询用户信息
		UserInfo userInfo = userInfoMapper.queryUserInfoByUserIdAndUserToken(paramMap);

		if (null == userInfo) {
			mkResponse(response, ErrorCode.QUERY_USER_INFO_BY_ID_AND_TOKEN_ERROR, "no this user", null);

			Constant.accessLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/queryUserInfo", ErrorCode.QUERY_USER_INFO_BY_ID_AND_TOKEN_ERROR,
					"no this user", "{user_id:" + user_id + ",dest_user_id:" + dest_user_id + "}");
			return response;
		}
		else {
			Integer friend_status = null;
			// 查询好友关系
			if (user_id.equals(dest_user_id)) {
				friend_status = 2;
			}
			else {
				String responseData = socialService.queryIsFriend(user_id, user_token, dest_user_id);
				JSONObject obj = JSONObject.parseObject(responseData);
				Object obj2 = obj.get("data");
				UserFriend uu = ((JSON) obj2).toJavaObject(UserFriend.class);
				friend_status = uu.getStatus();
			}

			// 查询注册时间
			Long add_time = userAccountMapper.getAddTimeByUserId(dest_user_id);

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("user_id", userInfo.getUser_id());

			if (StringUtils.isEmpty(userInfo.getFirst_name()) && StringUtils.isEmpty(userInfo.getLast_name())) {
				resultMap.put("user_name", null);
			}
			else if (StringUtils.isEmpty(userInfo.getLast_name())) {
				resultMap.put("user_name", userInfo.getFirst_name());
			}
			else {
				resultMap.put("user_name", userInfo.getFirst_name() + " " + userInfo.getLast_name());
			}
			resultMap.put("email", userInfo.getEmail());
			resultMap.put("country", userInfo.getCountry());
			resultMap.put("city", userInfo.getCity());
			resultMap.put("positions", userInfo.getPositions());
			resultMap.put("intro", userInfo.getIntro());
			resultMap.put("avatar_url", userInfo.getAvatar_url());
			resultMap.put("gender", userInfo.getGender());
			resultMap.put("birthday", userInfo.getBirthday());
			resultMap.put("height", userInfo.getHeight());
			resultMap.put("weight", userInfo.getWeight());
			resultMap.put("add_time", add_time);
			resultMap.put("friend_status", friend_status);
			mkResponse(response, ErrorCode.SUCCESS, "success", resultMap);
		}

		long tc = System.currentTimeMillis() - starttime;
		Constant.operateLogger.info("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/queryUserInfo", ErrorCode.SUCCESS, "SUCCESS",
				"{user_id = " + user_id + " ,user_token = " + user_token + ",dest_user_id = " + dest_user_id + ",ms = " + tc + "}");
		return response;
	}

	@RequestMapping("/queryUserPhoto")
	public BaseResponse<List<UserPhoto>> queryUserPhoto(HttpServletRequest request) {
		// TODO
		long starttime = System.currentTimeMillis();
		BaseResponse<List<UserPhoto>> response = new BaseResponse<List<UserPhoto>>();

		String user_id = request.getParameter("user_id");
		String user_token = request.getParameter("user_token");
		String dest_user_id = request.getParameter("dest_user_id");
		String offset = request.getParameter("offset");
		String length = request.getParameter("length");

		if (StringUtils.isEmpty(user_id)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_id is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/queryUserPhoto", ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_id is null");
			return response;
		}
		if (StringUtils.isEmpty(dest_user_id)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "dest_user_id is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/queryUserPhoto", ErrorCode.REQUEST_PARAMETER_IS_NULL, "dest_user_id is null");
			return response;
		}
		if (StringUtils.isEmpty(offset)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "offset is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/queryUserPhoto", ErrorCode.REQUEST_PARAMETER_IS_NULL, "offset is null");
			return response;
		}
		if (StringUtils.isEmpty(length)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "length is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/queryUserPhoto", ErrorCode.REQUEST_PARAMETER_IS_NULL, "length is null");
			return response;
		}

		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("user_id", user_id);
		paramMap.put("user_token", user_token);
		paramMap.put("dest_user_id", dest_user_id);
		paramMap.put("offset", Integer.parseInt(offset));
		paramMap.put("length", Integer.parseInt(length));
		// 查询用户相册
		List<UserPhoto> userPhotos = userPhotoMapper.queryUserPhotoByUserIdAndToken(paramMap);

		mkResponse(response, ErrorCode.SUCCESS, "success", userPhotos);

		long tc = System.currentTimeMillis() - starttime;

		Constant.accessLogger.info("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/queryUserPhoto", ErrorCode.SUCCESS, "SUCCESS",
				"{user_id = " + user_id + " ,user_token = " + user_token + ",dest_user_id = " + dest_user_id + ",ms = " + tc + "}");
		return response;
	}

	@RequestMapping("/logout")
	public BaseResponse<String> logout(HttpServletRequest request) {
		// TODO
		long starttime = System.currentTimeMillis();
		BaseResponse<String> response = new BaseResponse<>();

		String user_id = request.getParameter("user_id");

		if (StringUtils.isEmpty(user_id)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_id is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/logout", ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_id is null");
			return response;
		}
		try {
			String redis_key = "xrun-users:" + user_id;
			Long a = stringRedisTemplate.opsForHash().delete(redis_key, "user_token");

			if (a > 0) {
				long tc = System.currentTimeMillis() - starttime;
				mkResponse(response, ErrorCode.SUCCESS, "logout success", null);
				Constant.accessLogger.info("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/logout", ErrorCode.SUCCESS, "SUCCESS",
						"{user_id = " + user_id + ",ms = " + tc + "}");
			}
			else {
				mkResponse(response, ErrorCode.LOGIN_OUT_ERROR, "logout failed", null);
				Constant.operateLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/logout", ErrorCode.REQUEST_PARAMETER_IS_NULL, "logout failed",
						"{user_id = " + user_id + "}");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			mkResponse(response, ErrorCode.UPDATE_REDIS_ERROR, "update redis error", null);
			Constant.operateLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/logout", ErrorCode.UPDATE_REDIS_ERROR, "update redis error",
					"{ user_id = " + user_id + "}");
		}

		return response;
	}

	@RequestMapping("/userBind")
	public BaseResponse<Map<String, Object>> userBind(HttpServletRequest request) {
		// TODO
		long starttime = System.currentTimeMillis();
		BaseResponse<Map<String, Object>> response = new BaseResponse<Map<String, Object>>();

		String user_id = request.getParameter("user_id");
		String user_token = request.getParameter("user_token");
		String bind_third_type = request.getParameter("bind_third_type");
		String uuid = request.getParameter("uuid");
		String third_token = request.getParameter("third_token");

		if (StringUtils.isEmpty(user_id)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_id is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/userBind", ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_id is null");
			return response;
		}
		if (StringUtils.isEmpty(bind_third_type)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "bind_third_type is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/userBind", ErrorCode.REQUEST_PARAMETER_IS_NULL,
					"bind_third_type is null");
			return response;
		}
		if (StringUtils.isEmpty(uuid)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "uuid is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/userBind", ErrorCode.REQUEST_PARAMETER_IS_NULL, "uuid is null");
			return response;
		}
		if (StringUtils.isEmpty(third_token)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "third_token is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/userBind", ErrorCode.REQUEST_PARAMETER_IS_NULL, "third_token is null");
			return response;
		}

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("user_id", user_id);
		paramMap.put("user_token", user_token);
		paramMap.put("third_type", bind_third_type);
		// 查询第三方绑定信息
		UserThirdAccount thirdAccount = userThirdAccountMapper.queryThirdAccount4Bind(paramMap);

		// 第三方账号已被绑定
		if (null != thirdAccount) {
			mkResponse(response, ErrorCode.USER_THIRD_ACCOUNT_HAS_BINDED, "this third account has been binded", null);
			Constant.accessLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/userBind", ErrorCode.USER_THIRD_ACCOUNT_HAS_BINDED,
					"this third account has been binded", "{user_id = " + user_id + ",user_token = " + user_token + ",bind_third_type = "
							+ bind_third_type + ",third_token = " + third_token + ",uuid = " + uuid + "}");
		}
		// 第三方账号未被绑定
		else {
			thirdAccount = new UserThirdAccount();
			thirdAccount.setUser_id(Long.parseLong(user_id));
			thirdAccount.setThird_token(third_token);
			thirdAccount.setThird_type(Integer.parseInt(bind_third_type));
			thirdAccount.setUuid(uuid);

			try {
				// 账号绑定
				int num = userThirdAccountMapper.addUserThirdAccount(thirdAccount);

				if (num == 1) {
					Map<String, String> map = new HashMap<>();
					map.put("dest_user_id", user_id);
					map.put("user_token", user_token);

					// 查询用户信息 ，并返回
					UserInfo userInfo = userInfoMapper.queryUserInfoByUserIdAndUserToken(map);

					if (null == userInfo) {
						mkResponse(response, ErrorCode.SUCCESS, "success", null);
					}
					else {
						Map<String, Object> resultMap = new HashMap<>();

						resultMap.put("user_id", userInfo.getUser_id());
						if (StringUtils.isEmpty(userInfo.getFirst_name()) && StringUtils.isEmpty(userInfo.getLast_name())) {
							resultMap.put("user_name", null);
						}
						else if (StringUtils.isEmpty(userInfo.getLast_name())) {
							resultMap.put("user_name", userInfo.getFirst_name());
						}
						else {
							resultMap.put("user_name", userInfo.getFirst_name() + " " + userInfo.getLast_name());
						}
						resultMap.put("email", userInfo.getEmail());
						resultMap.put("country", userInfo.getCountry());
						resultMap.put("city", userInfo.getCity());
						resultMap.put("positions", userInfo.getPositions());
						resultMap.put("intro", userInfo.getIntro());
						resultMap.put("avatar_url", userInfo.getAvatar_url());
						resultMap.put("gender", userInfo.getGender());
						resultMap.put("birthday", userInfo.getBirthday());
						resultMap.put("height", userInfo.getHeight());
						resultMap.put("weight", userInfo.getWeight());

						mkResponse(response, ErrorCode.SUCCESS, "success", resultMap);
					}

					long tc = System.currentTimeMillis() - starttime;
					Constant.accessLogger.info("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/userBind", ErrorCode.SUCCESS, "SUCCESS",
							"{user_id = " + user_id + ",user_token = " + user_token + ",bind_third_type = " + bind_third_type
									+ ",third_token = " + third_token + ",uuid = " + uuid + ",ms = " + tc + "}");
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				mkResponse(response, ErrorCode.ERROR, "error", null);
				Constant.operateLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/userBind", ErrorCode.ERROR, "ERROR",
						"{user_id = " + user_id + "}");
			}

		}

		return response;
	}

	@RequestMapping("/queryUserBind")
	public BaseResponse<List<UserThirdAccount>> queryUserBind(HttpServletRequest request) {
		// TODO
		long starttime = System.currentTimeMillis();
		BaseResponse<List<UserThirdAccount>> response = new BaseResponse<>();

		String user_id = request.getParameter("user_id");
		String user_token = request.getParameter("user_token");

		if (StringUtils.isEmpty(user_id)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_id is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/queryUserBind", ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_id is null");
			return response;
		}

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("user_id", user_id);
		paramMap.put("user_token", user_token);

		// 根据user_id ，user_token 查询账号绑定信息
		List<UserThirdAccount> thirdAccountsList = userThirdAccountMapper.selectUserBindThirdAccount(paramMap);

		mkResponse(response, ErrorCode.SUCCESS, "success", thirdAccountsList);

		long tc = System.currentTimeMillis() - starttime;
		Constant.accessLogger.info("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/queryUserBind", ErrorCode.SUCCESS,
				"{user_id =" + user_id + ",user_token = " + user_token + ",ms = " + tc + "}");

		return response;
	}

	@RequestMapping("/queryUserInfo4List")
	public BaseResponse<List<Map<String, Object>>> queryUserInfo4List(HttpServletRequest request) {
		// TODO
		long starttime = System.currentTimeMillis();
		BaseResponse<List<Map<String, Object>>> response = new BaseResponse<List<Map<String, Object>>>();

		String user_id = request.getParameter("user_id");
		String user_list = request.getParameter("user_list");

		if (StringUtils.isEmpty(user_id)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_id is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/queryUserInfo4List", ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_id is null");
			return response;
		}
		if (StringUtils.isEmpty(user_list)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_list is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/queryUserInfo4List", ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_list is null");
			return response;
		}
		// 获取要查询的user_id 集合
		List<String> list = Arrays.asList(user_list.split(","));

		// 查询用户信息集合
		List<UserInfo> userInfos = userInfoMapper.queryUserInfo4List(list);

		Map<String, Object> resultMap = null;
		List<Map<String, Object>> resultList = new ArrayList<>();
		for (int i = 0; i < userInfos.size(); i++) {
			// 查询注册时间
			Long add_time = userAccountMapper.getAddTimeByUserId(String.valueOf(userInfos.get(i).getUser_id()));

			resultMap = new HashMap<>();

			resultMap.put("user_id", userInfos.get(i).getUser_id());

			// 拼接user_name
			if (StringUtils.isEmpty(userInfos.get(i).getFirst_name()) && StringUtils.isEmpty(userInfos.get(i).getLast_name())) {
				resultMap.put("user_name", null);
			}
			else if (StringUtils.isEmpty(userInfos.get(i).getLast_name())) {
				resultMap.put("user_name", userInfos.get(i).getFirst_name());
			}
			else {
				resultMap.put("user_name", userInfos.get(i).getFirst_name() + " " + userInfos.get(i).getLast_name());
			}
			resultMap.put("email", userInfos.get(i).getEmail());
			resultMap.put("country", userInfos.get(i).getCountry());
			resultMap.put("city", userInfos.get(i).getCity());
			resultMap.put("positions", userInfos.get(i).getPositions());
			resultMap.put("intro", userInfos.get(i).getIntro());
			resultMap.put("avatar_url", userInfos.get(i).getAvatar_url());
			resultMap.put("gender", userInfos.get(i).getGender());
			resultMap.put("birthday", userInfos.get(i).getBirthday());
			resultMap.put("height", userInfos.get(i).getHeight());
			resultMap.put("weight", userInfos.get(i).getWeight());
			resultMap.put("add_time", add_time);

			resultList.add(resultMap);
		}
		mkResponse(response, ErrorCode.SUCCESS, "success", resultList);

		long tc = System.currentTimeMillis() - starttime;
		Constant.accessLogger.info("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/queryUserInfo4List", ErrorCode.SUCCESS, "SUCCESS",
				"{user_id = " + user_id + ",user_list = " + user_list + ",ms = " + tc + "}");

		return response;
	}

	@RequestMapping("/userUnbind")
	public BaseResponse<Map<String, String>> userUnbind(HttpServletRequest request) {
		// TODO
		long starttime = System.currentTimeMillis();
		BaseResponse<Map<String, String>> response = new BaseResponse<>();

		String user_id = request.getParameter("user_id");
		String user_token = request.getParameter("user_token");
		String bind_third_type = request.getParameter("bind_third_type");

		if (StringUtils.isEmpty(user_id)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_id is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/userUnbind", ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_id is null");
			return response;
		}

		if (StringUtils.isEmpty(bind_third_type)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "bind_third_type is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/userUnbind", ErrorCode.REQUEST_PARAMETER_IS_NULL,
					"bind_third_type is null");
			return response;
		}

		// 从redis获取当前登陆的 third_type
		String redis_key = "xrun-users:" + user_id;
		String login_third_type = (String) stringRedisTemplate.opsForHash().get(redis_key, "third_type");

		if (null == login_third_type) {
			mkResponse(response, ErrorCode.GET_REDIS_HASH_VALUE_ERROR, "error", null);
			Constant.accessLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/userUnbind", ErrorCode.GET_REDIS_HASH_VALUE_ERROR,
					"get redis hash value error", "{user_id = " + user_id + ",third_type = " + bind_third_type + "}");
		}
		else {
			// 判断要解绑的账号是否是登陆账号
			if (Integer.parseInt(login_third_type) == Integer.parseInt(bind_third_type)) {
				mkResponse(response, ErrorCode.THE_CURRENT_ACCOUNT_CANNOT_BE_UNBIND, "The current account cannot be unbind", null);
				Constant.accessLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/userUnbind", ErrorCode.THE_CURRENT_ACCOUNT_CANNOT_BE_UNBIND,
						"This current account cannot be unbind", "{user_id = " + user_id + ",third_type = " + bind_third_type + "}");
			}
			else {
				// 验证表中是否有要解绑的账号
				Map<String, String> paramMap = new HashMap<>();
				paramMap.put("user_id", user_id);
				paramMap.put("user_token", user_token);
				paramMap.put("third_type", bind_third_type);
				// 查询第三方绑定信息
				UserThirdAccount thirdAccount = userThirdAccountMapper.queryThirdAccount4Bind(paramMap);

				if (null == thirdAccount) {
					mkResponse(response, ErrorCode.THE_ACCOUNT_WAS_NOT_FOUND, "This account was not found", null);

					Constant.accessLogger.error("{}|{}|{}|{}|{}", HttpUtil.getIpAddr(request),"/user/userUnbind", ErrorCode.THE_ACCOUNT_WAS_NOT_FOUND,
							"This account was not found", "{user_id = " + user_id + ",third_type = " + bind_third_type + "}");
				}
				else {
					Map<String, String> map = new HashMap<>();
					map.put("user_id", user_id);
					map.put("third_type", bind_third_type);

					try {
						// 解绑账号 删除user_third_account 记录
						int num = userThirdAccountMapper.delete4Unbind(map);
						// 解绑成功
						if (num == 1) {
							mkResponse(response, ErrorCode.SUCCESS, "success", map);

							long tc = System.currentTimeMillis() - starttime;
							Constant.accessLogger.info("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/userUnbind", ErrorCode.SUCCESS, "SUCCESS",
									"{user_id = " + user_id + ",third_type = " + bind_third_type + ",ms = " + tc + "}");
						}
						// 解绑失败
						if (num == 0) {
							mkResponse(response, ErrorCode.THE_ACCOUNT_HAS_BEEN_UNBIND, "Unbind error", null);

							Constant.operateLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/userUnbind", ErrorCode.THE_ACCOUNT_HAS_BEEN_UNBIND,
									"Unbind error", "{user_id = " + user_id + ",third_type = " + bind_third_type + "}");
						}

					}
					catch (Exception e) {
						e.printStackTrace();
						mkResponse(response, ErrorCode.ERROR, "unbind error", null);
						Constant.operateLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/userUnbind", ErrorCode.ERROR, "Unbind error",
								"{user_id =" + user_id + ",third_type =" + bind_third_type + "}");
					}
				}

			}
		}

		return response;
	}

	@RequestMapping("/checkUser4Friend")
	public BaseResponse<List<UserFriend>> checkUser4Friend(HttpServletRequest request) {
		// TODO
		long starttime = System.currentTimeMillis();
		BaseResponse<List<UserFriend>> response = new BaseResponse<>();

		String user_id = request.getParameter("user_id");
		String user_token = request.getParameter("user_token");
		String str_uuid_list = request.getParameter("uuid_list");

		if (StringUtils.isEmpty(user_id)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_id is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/checkUser4Friend", ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_id is null");
			return response;
		}
		if (StringUtils.isEmpty(str_uuid_list)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "uuid_list is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/checkUser4Friend", ErrorCode.REQUEST_PARAMETER_IS_NULL, "uuid_list is null");
			return response;
		}
		// request 中的传参uuid_list
		List<String> uuid_list = Arrays.asList(str_uuid_list.split(","));
		// 已注册的uuid对应的userId集合
		Set<Long> userIdSet = new HashSet<>();
		// 已注册的uuid 集合
		List<String> registerUuids = new ArrayList<>();
		// 返回的userfriend list
		List<UserFriend> userfriendList = new ArrayList<>();

		UserFriend userFriend = null;

		// 根据uuid查询user_third_account,获取user_id
		for (int i = 0; i < uuid_list.size(); i++) {
			UserThirdAccount userThirdAccount = userThirdAccountMapper.queryThirdAccountByUuid(uuid_list.get(i));
			// uuid 未注册
			if (null == userThirdAccount) {
				userFriend = new UserFriend();
				userFriend.setUser_id(Long.parseLong(user_id));
				userFriend.setFriend_user_id(null);
				userFriend.setStatus(-2);
				userFriend.setUuid(uuid_list.get(i));

				userfriendList.add(userFriend);
			}
			// uuid 已注册
			else {
				userIdSet.add(userThirdAccount.getUser_id());
				registerUuids.add(uuid_list.get(i));
			}
		}

		List<Long> userIdList = new ArrayList<>(userIdSet);
		// 拼接user_id集合字符串
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < userIdList.size(); i++) {
			if (i < userIdList.size() - 1) {
				sb.append(userIdList.get(i) + ",");
				continue;
			}
			sb.append(userIdList.get(i));
		}
		// Feign调用social/queryIsFrienf4List接口,获取好友状态列表
		List<UserFriend> userFriendsDate = null;
		JSONArray obj2 = null;
		try {
			String responseData = socialService.queryIsFriend4List(user_id, user_token, sb.toString());
			JSONObject obj = JSONObject.parseObject(responseData);
			obj2 = obj.getJSONArray("data");
			userFriendsDate = obj2.toJavaList(UserFriend.class);

		}
		catch (Exception e) {
			e.printStackTrace();
			mkResponse(response, ErrorCode.GET_SOCIAL_SERVICE_ERROR, "get social service error", null);
			Constant.operateLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/checkUser4Friend", ErrorCode.REQUEST_PARAMETER_IS_NULL,
					"get social service error", "{user_id =" + user_id + ",str_uuid_list =" + str_uuid_list + "}");
			return response;
		}

		// 拼装好友列表
		for (int i = 0; i < userFriendsDate.size(); i++) {
			userFriend = new UserFriend();

			userFriend.setFriend_user_id(userFriendsDate.get(i).getFriend_user_id());
			if (userFriendsDate.get(i).getFriend_user_id().equals(userFriendsDate.get(i).getUser_id())) {
				userFriend.setStatus(2);
			}
			else {
				userFriend.setStatus(userFriendsDate.get(i).getStatus());
			}
			userFriend.setUser_id(userFriendsDate.get(i).getUser_id());
			userFriend.setUuid(registerUuids.get(i));

			userfriendList.add(userFriend);
		}

		long tc = System.currentTimeMillis() - starttime;
		mkResponse(response, ErrorCode.SUCCESS, "success", userfriendList);
		Constant.accessLogger.info("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/checkUser4Friend", ErrorCode.SUCCESS, "SUCCESS",
				"{user_id =" + user_id + ",uuid_list = " + str_uuid_list + ",ms = " + tc + "}");
		return response;
	}

	@RequestMapping("/queryUserStatInfo")
	public BaseResponse<UserStatInfo> queryUserStatInfo(HttpServletRequest request) {
		// TODO
		long starttime = System.currentTimeMillis();
		BaseResponse<UserStatInfo> response = new BaseResponse<UserStatInfo>();

		String user_id = request.getParameter("user_id");
		String time_zone = request.getParameter("time_zone");
		String dest_user_id = request.getParameter("dest_user_id");

		if (StringUtils.isEmpty(user_id)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_id is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/queryUserStatInfo", ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_id is null");
			return response;
		}
		if (StringUtils.isEmpty(dest_user_id)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "dest_user_id is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/queryUserStatInfo", ErrorCode.REQUEST_PARAMETER_IS_NULL, "dest_user_id is null");
			return response;
		}
		if (StringUtils.isEmpty(time_zone)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "time_zone is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/queryUserStatInfo", ErrorCode.REQUEST_PARAMETER_IS_NULL, "time_zone is null");
			return response;
		}
		// redis-key
		String redis_key = "xrun-users:" + dest_user_id;
		String hash_key = DateUtil.getYearWeekStrOfYearByDate(new Date(), time_zone);
		String user_name = null;
		String user_friends = null;
		String user_photos = null;
		String week_run_goal = null;
		String week_run_mileages = null;
		String last_motion_time = null;
		String user_avatar = null;
		String country = null;
		String city = null;
		String user_challengs = null;
		String user_posts = null;
		String user_activitys = null;
		try {
			// 从redis hash里获取数据
			user_name = (String) stringRedisTemplate.opsForHash().get(redis_key, "user_name");
			if (user_name.contains("null")) {
				String[] name = user_name.split(" ");
				user_name = name[0];
			}
			user_friends = (String) stringRedisTemplate.opsForHash().get(redis_key, "user_friends");
			user_photos = (String) stringRedisTemplate.opsForHash().get(redis_key, "user_photos");
			week_run_goal = (String) stringRedisTemplate.opsForHash().get(redis_key, "week_run_goal");
			week_run_mileages = (String) stringRedisTemplate.opsForHash().get("xrun-wkms:" + dest_user_id, hash_key);
			last_motion_time = (String) stringRedisTemplate.opsForHash().get(redis_key, "last_motion_time");
			user_avatar = (String) stringRedisTemplate.opsForHash().get(redis_key, "avatar_url");
			country = (String) stringRedisTemplate.opsForHash().get(redis_key, "country");
			city = (String) stringRedisTemplate.opsForHash().get(redis_key, "city");
			user_challengs = (String) stringRedisTemplate.opsForHash().get(redis_key, "user_challengs");
			user_posts = (String) stringRedisTemplate.opsForHash().get(redis_key, "user_posts");
			user_activitys = (String) stringRedisTemplate.opsForHash().get(redis_key, "user_activitys");
		}
		catch (Exception e) {
			e.printStackTrace();
			mkResponse(response, ErrorCode.GET_REDIS_HASH_VALUE_ERROR, "get redis value error", null);
			Constant.accessLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/queryUserStatInfo", ErrorCode.GET_REDIS_HASH_VALUE_ERROR,
					"get redis value error", "{user_id = " + user_id + ",dest_user_id = " + dest_user_id + "}");
			return response;
		}

		// 拼接返回对象
		UserStatInfo userStatInfo = new UserStatInfo();

		userStatInfo.setUser_id(Long.parseLong(user_id));
		userStatInfo.setUser_name(user_name);
		if (!StringUtils.isEmpty(user_friends)) {
			userStatInfo.setUser_friends(Integer.parseInt(user_friends));
		}
		if (!StringUtils.isEmpty(user_photos)) {
			userStatInfo.setUser_photos(Integer.parseInt(user_photos));
		}
		if (!StringUtils.isEmpty(week_run_goal)) {
			userStatInfo.setWeek_run_goal(Integer.parseInt(week_run_goal));
		}
		if (!StringUtils.isEmpty(week_run_mileages)) {
			userStatInfo.setWeek_run_mileages(Double.parseDouble(week_run_mileages));
		}
		if (!StringUtils.isEmpty(last_motion_time)) {
			userStatInfo.setLast_motion_time(Long.parseLong(last_motion_time));
		}
		if (!StringUtils.isEmpty(user_avatar)) {
			userStatInfo.setUser_avatar(user_avatar);
		}
		if (!StringUtils.isEmpty(country)) {
			userStatInfo.setCountry(country);
		}
		if (!StringUtils.isEmpty(city)) {
			userStatInfo.setCity(city);
		}
		if (!StringUtils.isEmpty(user_challengs)) {
			userStatInfo.setUser_challengs(Integer.parseInt(user_challengs));;
		}
		if (!StringUtils.isEmpty(user_posts)) {
			userStatInfo.setUser_posts(Integer.parseInt(user_posts));
		}
		if (!StringUtils.isEmpty(user_activitys)) {
			userStatInfo.setUser_activitys(Integer.parseInt(user_activitys));
		}

		mkResponse(response, ErrorCode.SUCCESS, "success", userStatInfo);

		long tc = System.currentTimeMillis() - starttime;
		Constant.accessLogger.info("{}|{}|{}|{}|{}", HttpUtil.getIpAddr(request),"/user/queryUserStatInfo", ErrorCode.SUCCESS, "SUCCESS",
				"{user_id = " + user_id + ",dest_user_id = " + dest_user_id + ",ms = " + tc + "}");
		return response;
	}

	@RequestMapping("/queryRecommandFriend")
	public BaseResponse<List<Map<String, Object>>> queryRecommandFriend(HttpServletRequest request) {
		// TODO
		long starttime = System.currentTimeMillis();
		BaseResponse<List<Map<String, Object>>> response = new BaseResponse<>();

		String user_id = request.getParameter("user_id");
		String user_token = request.getParameter("user_token");

		if (StringUtils.isEmpty(user_id)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_id is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/queryRecommandFriend", ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_id is null");
			return response;
		}

		// Feign调用social/queryIsFrienf4List接口,获取好友状态列表
		List<UserFriend> userFriendList = null;
		JSONArray obj2 = null;
		try {
			//			Integer offset = 0;
			//			Integer length = 1000;
			String responseData = socialService.queryUserFriend(user_id, user_token, user_id);
			JSONObject obj = JSONObject.parseObject(responseData);
			obj2 = obj.getJSONArray("data");
			userFriendList = obj2.toJavaList(UserFriend.class);

		}
		catch (Exception e) {
			e.printStackTrace();
			mkResponse(response, ErrorCode.GET_SOCIAL_SERVICE_ERROR, "get social service error", null);
			Constant.operateLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/queryRecommandFriend", ErrorCode.REQUEST_PARAMETER_IS_NULL,
					"get social service error", "{user_id = " + user_id + "}");
			return response;
		}

		Integer top = 10;
		String country = request.getParameter("country");
		String gender = request.getParameter("gender");

		if (!StringUtils.isEmpty(request.getParameter("top"))) {
			top = Integer.parseInt(request.getParameter("top"));
		}

		// 查询条件
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("top", top);
		paramMap.put("country", country);
		paramMap.put("gender", gender);
		paramMap.put("user_id", user_id);
		if (userFriendList != null && userFriendList.size() > 0) {
			paramMap.put("user_friend_list", userFriendList);
		}

		// 按条件查询用户信息集合
		List<UserInfo> userInfoList = userInfoMapper.queryUserInfos(paramMap);

		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		for (UserInfo info : userInfoList) {
			Map<String, Object> map = new HashMap<>();
			map.put("user_id", info.getUser_id());

			if (StringUtils.isEmpty(info.getFirst_name()) && StringUtils.isEmpty(info.getLast_name())) {
				map.put("user_name", null);
			}
			else if (StringUtils.isEmpty(info.getLast_name())) {
				map.put("user_name", info.getFirst_name());
			}
			else {
				map.put("user_name", info.getFirst_name() + " " + info.getLast_name());
			}
			map.put("country", info.getCountry());
			map.put("gender", info.getGender());
			map.put("avatar_url", info.getAvatar_url());
			map.put("friend_status", -1);

			resultList.add(map);
		}

		mkResponse(response, ErrorCode.SUCCESS, "success", resultList);

		long tc = System.currentTimeMillis() - starttime;
		Constant.accessLogger.info("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/queryRecommandFriend", ErrorCode.SUCCESS, "SUCCESS",
				"{user_id = " + user_id + ",ms = " + tc + "}");
		return response;
	}

	@RequestMapping("/weekGoalSet")
	public BaseResponse<Map<String, Object>> weekGoalSet(HttpServletRequest request) {
		// TODO
		long starttime = System.currentTimeMillis();

		BaseResponse<Map<String, Object>> response = new BaseResponse<>();

		String user_id = request.getParameter("user_id");
		String param_key = request.getParameter("param_key");
		String param_value = request.getParameter("param_value");

		if (StringUtils.isEmpty(user_id)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_id is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/weekGoalSet", ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_id is null");
			return response;
		}
		if (StringUtils.isEmpty(param_key)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "param_key is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/weekGoalSet", ErrorCode.REQUEST_PARAMETER_IS_NULL, "param_key is null");
			return response;
		}
		if (StringUtils.isEmpty(param_value)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "param_value is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/weekGoalSet", ErrorCode.REQUEST_PARAMETER_IS_NULL, "param_value is null");
			return response;
		}

		// redis key
		String redis_key = "xrun-users:" + user_id;
		try {
			stringRedisTemplate.opsForHash().put(redis_key, param_key, param_value);

			param_value = (String) stringRedisTemplate.opsForHash().get(redis_key, param_key);
		}
		catch (Exception e) {
			e.printStackTrace();
			Constant.operateLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/weekGoalSet", ErrorCode.REQUEST_PARAMETER_IS_NULL,
					"get redis value error", "{user_id = " + user_id + "}");
		}

		// 拼接返回结果
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("user_id", Long.parseLong(user_id));
		resultMap.put("param_key", param_key);
		resultMap.put("param_value", param_value);

		mkResponse(response, ErrorCode.SUCCESS, "success", resultMap);
		long tc = System.currentTimeMillis() - starttime;

		Constant.accessLogger.info("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/weekGoalSet", ErrorCode.SUCCESS, "SUCCESS",
				"{user_id = " + user_id + ",param_key = " + param_key + ",param_value = " + param_value + ",ms = " + tc + "}");
		return response;
	}

	@RequestMapping("/checkUser")
	public BaseResponse<Map<String, Object>> checkUser(HttpServletRequest request) {
		//TODO
		long starttime = System.currentTimeMillis();

		BaseResponse<Map<String, Object>> response = new BaseResponse<>();
		String user_id = request.getParameter("user_id");
		String user_token_dig = request.getParameter("user_token_dig");
		String nonce = request.getParameter("nonce");

		if (StringUtils.isEmpty(user_id)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_id is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/checkUser", ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_id is null");
			return response;
		}
		if (StringUtils.isEmpty(user_token_dig)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_token_dig is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/checkUser", ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_token_dig is null");
			return response;
		}
		if (StringUtils.isEmpty(nonce)) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "nonce is null", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/checkUser", ErrorCode.REQUEST_PARAMETER_IS_NULL, "nonce is null");
			return response;
		}

		//redis Key
		String redis_key = "xrun-users:" + user_id;
		String user_token = null;
		try {
			user_token = (String) stringRedisTemplate.opsForHash().get(redis_key, "user_token");
		}
		catch (Exception e) {
			e.printStackTrace();
			mkResponse(response, ErrorCode.GET_REDIS_HASH_VALUE_ERROR, "get user_token error.", null);
			Constant.operateLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/checkUser", ErrorCode.GET_REDIS_HASH_VALUE_ERROR, "get user_token error");
			return response;
		}

		if (!user_token_dig.equalsIgnoreCase(MD5.MD5Encode(user_token, nonce))) {
			mkResponse(response, ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_token_dig error", null);
			Constant.accessLogger.error("{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/checkUser", ErrorCode.REQUEST_PARAMETER_IS_NULL, "user_token_dig error");
			return response;
		}

		// 查询参数
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("user_token", user_token);
		paramMap.put("dest_user_id", user_id);

		// 查询用户信息
		UserInfo userInfo = userInfoMapper.queryUserInfoByUserIdAndUserToken(paramMap);

		if (null == userInfo) {
			mkResponse(response, ErrorCode.QUERY_USER_INFO_BY_ID_AND_TOKEN_ERROR, "no this user", null);

			Constant.accessLogger.error("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/checkUser", ErrorCode.QUERY_USER_INFO_BY_ID_AND_TOKEN_ERROR,
					"no this user", "{user_id:" + user_id + "}");
			return response;
		}
		else {

			// 查询注册时间
			Long add_time = userAccountMapper.getAddTimeByUserId(user_id);

			Map<String, Object> resultMap = new HashMap<>();

			resultMap.put("user_id", userInfo.getUser_id());
			if (StringUtils.isEmpty( userInfo.getFirst_name()) && StringUtils.isEmpty( userInfo.getLast_name())) {
				resultMap.put("user_name", null);
			}
			else if (StringUtils.isEmpty( userInfo.getLast_name())) {
				resultMap.put("user_name", userInfo.getFirst_name());
			}
			else {
				resultMap.put("user_name", userInfo.getFirst_name() + " " + userInfo.getLast_name());
			}
			resultMap.put("email", userInfo.getEmail());
			resultMap.put("country", userInfo.getCountry());
			resultMap.put("city", userInfo.getCity());
			resultMap.put("intro", userInfo.getIntro());
			resultMap.put("gender", userInfo.getGender());
			resultMap.put("birthday", userInfo.getBirthday());
			resultMap.put("height", userInfo.getHeight());
			resultMap.put("weight", userInfo.getWeight());
			resultMap.put("avatar_url", userInfo.getAvatar_url());
			resultMap.put("add_time", add_time);
			mkResponse(response, ErrorCode.SUCCESS, "success", resultMap);
		}
		long tc = System.currentTimeMillis() - starttime;
		Constant.operateLogger.info("{}|{}|{}|{}|{}",HttpUtil.getIpAddr(request), "/user/checkUser", ErrorCode.SUCCESS, "SUCCESS",
				"{user_id = " + user_id + " ,user_token = " + user_token + ",ms = " + tc + "}");
		return response;
	}

	protected <T> void mkResponse(BaseResponse<T> response, String returnCode, String message, T data) {
		response.setReturn_code(returnCode);
		response.setMessage(message);
		response.setData(data);
	}

}
