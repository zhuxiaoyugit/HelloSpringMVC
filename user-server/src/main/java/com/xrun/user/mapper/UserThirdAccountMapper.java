package com.xrun.user.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.xrun.user.pojo.UserThirdAccount;

/*
 * 第三方登陆mapper
 */
@Mapper
public interface UserThirdAccountMapper
{
	/**
	 * 查询用户第三方账户信息
	 * @param map
	 * @return
	 */
	UserThirdAccount queryUserThirdAccount4Login(Map<String, String> map);
	/**
	 * 用户注册，新增用户第三方账户信息
	 * @param userThirdAccount
	 * @return
	 */
	int addUserThirdAccount(UserThirdAccount userThirdAccount);
	/**
	 * 用户绑定时，查询第三方账户信息
	 * @param map
	 * @return
	 */
	UserThirdAccount queryThirdAccount4Bind(Map<String, String> map);
	/**
	 * 获取用户绑定的第三方账户列表
	 * @param map
	 * @return
	 */
	List<UserThirdAccount> selectUserBindThirdAccount(Map<String, String> map);
	/**
	 * 用户解除第三方账户绑定
	 * @param map
	 * @return
	 */
	int delete4Unbind(Map<String, String> map);
	/**
	 * 根据uuid 查询用户第三方账户信息
	 * @param uuid
	 * @return
	 */
//	List<UserThirdAccount> queryThirdAccountByUuid(List<String> list);
	UserThirdAccount queryThirdAccountByUuid(String uuid);
}
