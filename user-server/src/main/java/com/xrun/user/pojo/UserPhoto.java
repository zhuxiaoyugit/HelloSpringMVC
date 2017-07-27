package com.xrun.user.pojo;

/**
 * 用户运动拍照图片
 * @author zgy
 *
 */
public class UserPhoto
{
	private Long user_id;
	private String only_num; //客户端唯一码
	private String longitude;//经度
	private String latitude;//纬度
	private String photo_url;//运动拍照图片相对路径
	private Integer status;//数据状态(1正常  2删除)
	private Integer motion_type; //类型：0=avatar、1=run、2=walk、3=hike、4=bike
	

	public Long getUser_id()
	{
		return user_id;
	}
	public void setUser_id(Long user_id)
	{
		this.user_id = user_id;
	}
	public String getOnly_num()
	{
		return only_num;
	}
	public void setOnly_num(String only_num)
	{
		this.only_num = only_num;
	}
	public String getLongitude()
	{
		return longitude;
	}
	public void setLongitude(String longitude)
	{
		this.longitude = longitude;
	}
	public String getLatitude()
	{
		return latitude;
	}
	public void setLatitude(String latitude)
	{
		this.latitude = latitude;
	}
	public String getPhoto_url()
	{
		return photo_url;
	}
	public void setPhoto_url(String photo_url)
	{
		this.photo_url = photo_url;
	}
	public Integer getStatus()
	{
		return status;
	}
	public void setStatus(Integer status)
	{
		this.status = status;
	}
	public Integer getMotion_type()
	{
		return motion_type;
	}
	public void setMotion_type(Integer motion_type)
	{
		this.motion_type = motion_type;
	}
	
	
}
