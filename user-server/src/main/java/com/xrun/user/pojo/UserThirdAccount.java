package com.xrun.user.pojo;
/*
 * 第三方账号
 */
public class UserThirdAccount
{
	private Integer third_type;
	private Long user_id;
	private String uuid;  //唯一
	private String third_token;
	
	public Integer getThird_type()
	{
		return third_type;
	}
	public void setThird_type(Integer third_type)
	{
		this.third_type = third_type;
	}

	public Long getUser_id()
	{
		return user_id;
	}
	public void setUser_id(Long user_id)
	{
		this.user_id = user_id;
	}
	public String getUuid()
	{
		return uuid;
	}
	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}
	public String getThird_token()
	{
		return third_token;
	}
	public void setThird_token(String third_token)
	{
		this.third_token = third_token;
	}
	
	
	
	
}
