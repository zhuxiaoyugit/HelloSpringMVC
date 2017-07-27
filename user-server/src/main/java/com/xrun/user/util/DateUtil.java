package com.xrun.user.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil
{
	public static String getYearWeekStrOfYearByDate(Date date, String time_zone)
	{
		TimeZone.setDefault(TimeZone.getTimeZone(time_zone));
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.setFirstDayOfWeek(Calendar.SUNDAY);
		c.setMinimalDaysInFirstWeek(1);
		
		int week = c.get(Calendar.WEEK_OF_YEAR);
		int yearNow = c.get(Calendar.YEAR);
		
		c.add(Calendar.DAY_OF_MONTH, -7);
		int year_2 = c.get(Calendar.YEAR);
		if(week < c.get(Calendar.WEEK_OF_YEAR))
		{  
		    yearNow = year_2 + 1;
		}
		
		if (week < 10)
		{
			return yearNow + "0" + week;
		}
		else 
		{
			return yearNow + "" + week;
		}
		
	}
}
