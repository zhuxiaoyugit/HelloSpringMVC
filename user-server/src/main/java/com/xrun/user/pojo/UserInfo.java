package com.xrun.user.pojo;

/**
 * 用户信息
 * 
 * @author zgy
 *
 */

public class UserInfo
{

	private Long user_id;
	private String first_name;
	private String last_name;
	private String email;
	private String country;
	private String city;
	private String positions;
	private String intro;
	private Integer gender = 0;
	private long birthday = 0;
	private Integer height = 0;
	private double weight = 0.0;
	private String avatar_url;
	private String user_token;

	
	public Long getUser_id()
	{
		return user_id;
	}

	public void setUser_id(Long user_id)
	{
		this.user_id = user_id;
	}

	public String getFirst_name()
	{
		return first_name;
	}

	public void setFirst_name(String first_name)
	{
		this.first_name = first_name;
	}

	public String getLast_name()
	{
		return last_name;
	}

	public void setLast_name(String last_name)
	{
		this.last_name = last_name;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getCountry()
	{
		return country;
	}

	public void setCountry(String country)
	{
		this.country = country;
	}

	public String getCity()
	{
		return city;
	}

	public void setCity(String city)
	{
		this.city = city;
	}

	public String getPositions()
	{
		return positions;
	}

	public void setPositions(String positions)
	{
		this.positions = positions;
	}

	public String getIntro()
	{
		return intro;
	}

	public void setIntro(String intro)
	{
		this.intro = intro;
	}

	public Integer getGender()
	{
		return gender;
	}

	public void setGender(Integer gender)
	{
		this.gender = gender;
	}

	public long getBirthday()
	{
		return birthday;
	}

	public void setBirthday(long birthday)
	{
		this.birthday = birthday;
	}

	public Integer getHeight()
	{
		return height;
	}

	public void setHeight(Integer height)
	{
		this.height = height;
	}

	public double getWeight()
	{
		return weight;
	}

	public void setWeight(double weight)
	{
		this.weight = weight;
	}

	public String getAvatar_url()
	{
		return avatar_url;
	}

	public void setAvatar_url(String avatar_url)
	{
		this.avatar_url = avatar_url;
	}

	public String getUser_token()
	{
		return user_token;
	}

	public void setUser_token(String user_token)
	{
		this.user_token = user_token;
	}

}
