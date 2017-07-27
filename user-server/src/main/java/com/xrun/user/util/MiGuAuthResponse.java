package com.xrun.user.util;

/**
 * 咪咕授权token验证 返回参数 Created by zhaobo on 2017/5/23.
 */

public class MiGuAuthResponse
{
	private Body body;
	private Head header;

	public static class Head
	{
		private String inresponseto;
		private int resultcode;
		private String resultstring;
		private String systemtime;
		private String version;

		public String getInresponseto()
		{
			return inresponseto;
		}

		public void setInresponseto(String inresponseto)
		{
			this.inresponseto = inresponseto;
		}

		public int getResultcode()
		{
			return resultcode;
		}

		public void setResultcode(int resultcode)
		{
			this.resultcode = resultcode;
		}

		public String getResultstring()
		{
			return resultstring;
		}

		public void setResultstring(String resultstring)
		{
			this.resultstring = resultstring;
		}

		public String getSystemtime()
		{
			return systemtime;
		}

		public void setSystemtime(String systemtime)
		{
			this.systemtime = systemtime;
		}

		public String getVersion()
		{
			return version;
		}

		public void setVersion(String version)
		{
			this.version = version;
		}
	}

	public static class Body
	{
		/**
		 * 咪咕一级用户中心为用户生成的单点登录会话标识，咪咕帐号为USessionID，如果是非咪咕帐号，则为空
		 */
		private String usessionid;
		/**
		 * 用户咪咕帐号的系统标识；如果是非咪咕帐号，则为空。
		 */
		private String passid;
		/**
		 * 表示手机号码
		 */
		private String msisdn;
		/**
		 * 表示邮箱地址
		 */
		private String email;
		/**
		 * 登录使用的用户标识：0：手机号码1：邮箱2：普通用户名（字母数字组合）3：第三方帐号
		 */
		private int loginidtype;
		/**
		 * 手机号码的归属运营商：0：中国移动1：中国电信2：中国联通99：未知的异网手机号码
		 */
		private String msisdntype;
		/**
		 * 用户所属省份
		 */
		private String province;
		/**
		 * 认证方式，取值参见4.1
		 */
		private String authtype;
		/**
		 * 咪咕一级用户中心认证用户的时间
		 */
		private String authtime;
		/**
		 * 业务平台为该用户的最近一次报活时间
		 */
		private String lastactivetime;
		/**
		 * 用户在本业务平台的帐号是否已经关联到咪咕帐号，若已关联，与咪咕帐号中手机/邮箱相同的业务帐号不能再登录 0：已经关联,1：未关联
		 */
		private String relateToMiguPassport;
		/**
		 * 昵称
		 */
		private String nickname;
		/**
		 * 是否为隐式咪咕帐号， 0：不是 1：是
		 */
		private String implicit;
		/**
		 * 用户登录时，输入的用户名，可能是以下几种：手机号码邮箱普通用户名（字母数字组合）第三方帐号的openID
		 */
		private String loginid;

		public String getUsessionid()
		{
			return usessionid;
		}

		public void setUsessionid(String usessionid)
		{
			this.usessionid = usessionid;
		}

		public String getPassid()
		{
			return passid;
		}

		public void setPassid(String passid)
		{
			this.passid = passid;
		}

		public String getMsisdn()
		{
			return msisdn;
		}

		public void setMsisdn(String msisdn)
		{
			this.msisdn = msisdn;
		}

		public String getEmail()
		{
			return email;
		}

		public void setEmail(String email)
		{
			this.email = email;
		}

		public int getLoginidtype()
		{
			return loginidtype;
		}

		public void setLoginidtype(int loginidtype)
		{
			this.loginidtype = loginidtype;
		}

		public String getMsisdntype()
		{
			return msisdntype;
		}

		public void setMsisdntype(String msisdntype)
		{
			this.msisdntype = msisdntype;
		}

		public String getProvince()
		{
			return province;
		}

		public void setProvince(String province)
		{
			this.province = province;
		}

		public String getAuthtype()
		{
			return authtype;
		}

		public void setAuthtype(String authtype)
		{
			this.authtype = authtype;
		}

		public String getAuthtime()
		{
			return authtime;
		}

		public void setAuthtime(String authtime)
		{
			this.authtime = authtime;
		}

		public String getLastactivetime()
		{
			return lastactivetime;
		}

		public void setLastactivetime(String lastactivetime)
		{
			this.lastactivetime = lastactivetime;
		}

		public String getRelateToMiguPassport()
		{
			return relateToMiguPassport;
		}

		public void setRelateToMiguPassport(String relateToMiguPassport)
		{
			this.relateToMiguPassport = relateToMiguPassport;
		}

		public String getNickname()
		{
			return nickname;
		}

		public void setNickname(String nickname)
		{
			this.nickname = nickname;
		}

		public String getImplicit()
		{
			return implicit;
		}

		public void setImplicit(String implicit)
		{
			this.implicit = implicit;
		}

		public String getLoginid()
		{
			return loginid;
		}

		public void setLoginid(String loginid)
		{
			this.loginid = loginid;
		}
	}

	public Body getBody()
	{
		return body;
	}

	public void setBody(Body body)
	{
		this.body = body;
	}

	public Head getHeader()
	{
		return header;
	}

	public MiGuAuthResponse setHeader(Head header)
	{
		this.header = header;
		return this;
	}
}
