package com.xrun.user.pojo;

public class UserFriend
{
	private Long user_id;
	private Long friend_user_id;
	private Integer status = -1;
	private String uuid;
	
	
	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public Long getUser_id()
	{
		return user_id;
	}

	public void setUser_id(Long user_id)
	{
		this.user_id = user_id;
	}

	public Long getFriend_user_id()
	{
		return friend_user_id;
	}

	public void setFriend_user_id(Long friend_user_id)
	{
		this.friend_user_id = friend_user_id;
	}

	public Integer getStatus()
	{
		return status;
	}

	public void setStatus(Integer status)
	{
		this.status = status;
	}

}
