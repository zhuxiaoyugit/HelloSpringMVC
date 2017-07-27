package com.xrun.user.pojo;

public class UserStatInfo
{
	private Long user_id;
	private String user_name;
	private Integer user_friends = 0;
	private Integer user_photos = 0;
	private Integer week_run_goal = 10000;
	private double week_run_mileages = 0;
	private Long last_motion_time = (long) 0;
	private String user_avatar;
	private String country;
	private String city;
	private Integer user_challengs = 0;
	private Integer user_posts = 0;
	private Integer user_activitys = 0;

	public Integer getUser_activitys()
	{
		return user_activitys;
	}

	public void setUser_activitys(Integer user_activitys)
	{
		this.user_activitys = user_activitys;
	}

	public Integer getUser_challengs()
	{
		return user_challengs;
	}

	public void setUser_challengs(Integer user_challengs)
	{
		this.user_challengs = user_challengs;
	}

	public Integer getUser_posts()
	{
		return user_posts;
	}

	public void setUser_posts(Integer user_posts)
	{
		this.user_posts = user_posts;
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

	public Long getUser_id()
	{
		return user_id;
	}

	public void setUser_id(Long user_id)
	{
		this.user_id = user_id;
	}

	public String getUser_name()
	{
		return user_name;
	}

	public void setUser_name(String user_name)
	{
		this.user_name = user_name;
	}

	public Integer getUser_friends()
	{
		return user_friends;
	}

	public void setUser_friends(Integer user_friends)
	{
		this.user_friends = user_friends;
	}

	public Integer getUser_photos()
	{
		return user_photos;
	}

	public void setUser_photos(Integer user_photos)
	{
		this.user_photos = user_photos;
	}

	public Integer getWeek_run_goal()
	{
		return week_run_goal;
	}

	public void setWeek_run_goal(Integer week_run_goal)
	{
		this.week_run_goal = week_run_goal;
	}

	public double getWeek_run_mileages()
	{
		return week_run_mileages;
	}

	public void setWeek_run_mileages(double week_run_mileages)
	{
		this.week_run_mileages = week_run_mileages;
	}

	public Long getLast_motion_time()
	{
		return last_motion_time;
	}

	public void setLast_motion_time(Long last_motion_time)
	{
		this.last_motion_time = last_motion_time;
	}

	public String getUser_avatar()
	{
		return user_avatar;
	}

	public void setUser_avatar(String user_avatar)
	{
		this.user_avatar = user_avatar;
	}

}
