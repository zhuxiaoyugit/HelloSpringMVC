package com.xrun.user.mapper;


import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.xrun.user.pojo.UserInfo;


/*
 * 用户信息mapper
 */
@Mapper
public interface UserInfoMapper
{
	/**
	 * 根据user_id 查询用户基本信息
	 * @param user_id
	 * @return
	 */
	UserInfo queryUserInfoByUserId(String user_id);
	/**
	 * 新增用户信息
	 * @param userInfo
	 * @return
	 */
	int addUserInfo(UserInfo userInfo);
	/**
	 * 用户修改基本信息 
	 * @param map
	 * @return
	 */
	int updateUserInfoByUserId(Map<String, String> map);
	/**
	 * 查询用户基本信息
	 * @param map
	 * @return
	 */
	UserInfo queryUserInfoByUserIdAndUserToken(Map<String, String> map);
	/**
	 * 根据user_id查询用户信息集合
	 * @param list
	 * @return
	 */
	List<UserInfo> queryUserInfo4List(List<String> list);
	
	/**
	 * 查询前 n条用户信息
	 */
	List<UserInfo> queryUserInfos(Map<String, Object> map);
}
