package com.xrun.user.mapper;


import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.xrun.user.pojo.UserAccount;


/*
 * 用户登陆信息mapper
 */
@Mapper
public interface UserAccountMapper
{
	/**
	 * 新增用户账户
	 * @param userAccount
	 * @return
	 */
	int addUserAccountFirst(UserAccount userAccount);
	/**
	 * 根据user_id 获取user_token
	 * @param user_id
	 * @return
	 */
	String queryUserTokenByUserId(String user_id);
	/**
	 * 根据user_id 更新用户最近一次登陆时间
	 * @param user_id
	 * @return
	 */
	int updateLastLoginTime(Map<String, Object> map);
	/**
	 * 根据user_id 查询注册时间add_time
	 */
	Long getAddTimeByUserId(String user_id);
	
	/**
	 * 查询禁止登陆状态
	 */
	UserAccount queryIsAllowLogin(Long user_id);
}
