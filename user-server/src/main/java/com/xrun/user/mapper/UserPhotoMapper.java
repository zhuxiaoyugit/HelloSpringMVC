package com.xrun.user.mapper;


import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.xrun.user.pojo.UserPhoto;


/*
 * 用户运动拍照图片Mapper
 */
@Mapper
public interface UserPhotoMapper
{
	/**
	 * 查询相册列表
	 * @param map
	 * @return
	 */
	List<UserPhoto> queryUserPhotoByUserIdAndToken(Map<String, Object> map);
}
