package com.xrun.user.pojo;

/**
 * 用户登陆信息
 * 
 * @author zgy
 *
 */
public class UserAccount
{
	private Long id;
	private String user_account;
	private String password;
	private String user_token;
	private Integer is_allow_login;
	private Long last_login_time;
	private Long ban_over_time ;
	private String mac_id;
	private String app_version;
	private String pc_type;
	private String sys_type;
	private String sys_version;
	private String app_channel;



	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getApp_channel()
	{
		return app_channel;
	}

	public void setApp_channel(String app_channel)
	{
		this.app_channel = app_channel;
	}

	public String getUser_account()
	{
		return user_account;
	}

	public void setUser_account(String user_account)
	{
		this.user_account = user_account;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getUser_token()
	{
		return user_token;
	}

	public void setUser_token(String user_token)
	{
		this.user_token = user_token;
	}

	public Integer getIs_allow_login()
	{
		return is_allow_login;
	}

	public void setIs_allow_login(Integer is_allow_login)
	{
		this.is_allow_login = is_allow_login;
	}

	public Long getLast_login_time()
	{
		return last_login_time;
	}

	public void setLast_login_time(Long last_login_time)
	{
		this.last_login_time = last_login_time;
	}

	public Long getBan_over_time()
	{
		return ban_over_time;
	}

	public void setBan_over_time(Long ban_over_time)
	{
		this.ban_over_time = ban_over_time;
	}

	public String getMac_id()
	{
		return mac_id;
	}

	public void setMac_id(String mac_id)
	{
		this.mac_id = mac_id;
	}

	public String getApp_version()
	{
		return app_version;
	}

	public void setApp_version(String app_version)
	{
		this.app_version = app_version;
	}

	public String getPc_type()
	{
		return pc_type;
	}

	public void setPc_type(String pc_type)
	{
		this.pc_type = pc_type;
	}

	public String getSys_type()
	{
		return sys_type;
	}

	public void setSys_type(String sys_type)
	{
		this.sys_type = sys_type;
	}

	public String getSys_version()
	{
		return sys_version;
	}

	public void setSys_version(String sys_version)
	{
		this.sys_version = sys_version;
	}

}
