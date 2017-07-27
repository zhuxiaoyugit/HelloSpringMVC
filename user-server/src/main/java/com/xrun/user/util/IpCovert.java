package com.xrun.user.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * logback IP配置
 * @author yaojianbo
 *
 */
public class IpCovert extends ClassicConverter
{

	@Override
	public String convert(ILoggingEvent arg0)
	{
		try
        {
            return InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
            return null;
        }

	}
	
}
