package com.xrun.user.util;

public class BaseResponse<T>
{

	public String return_code = "";

	public String message;
	
	public T data;
	
	

	public Object getData()
	{
		return data;
	}


	public void setData(T data)
	{
		this.data = data;
	}


	public String getReturn_code()
	{
		return return_code;
	}

	public void setReturn_code(String return_code)
	{
		this.return_code = return_code;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	



}
