package com.mongo.entity;

public class Report
{

	private String id;
	private String date;
	private String content;
	private String title;

	public Report()
	{
		super();
	}

	public Report( String date, String content, String title)
	{
		super();
		
		this.date = date;
		this.content = content;
		this.title = title;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getDate()
	{
		return date;
	}

	public void setDate(String date)
	{
		this.date = date;
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String toString()
	{
		return String.format("Report[id=%s, date='%s', content='%s', title='%s']", id, date, content, title);
	}
}
