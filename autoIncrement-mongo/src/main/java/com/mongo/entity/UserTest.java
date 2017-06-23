package com.mongo.entity;

public class UserTest {

	private long id;
	private String name;
	
	public UserTest()
	{
		super();
	}
	public UserTest(long id, String name)
	{
		super();
		this.id = id;
		this.name = name;
	}
	public long getId()
	{
		return id;
	}
	public void setId(long id)
	{
		this.id = id;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	@Override
	public String toString()
	{
		// TODO Auto-generated method stub
		return "User{id = "+id+", name='"+name+'\''+'}';
	}
	
	
}
