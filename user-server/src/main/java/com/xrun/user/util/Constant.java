package com.xrun.user.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 共享变量
 * @author YAOJIANBO
 *
 */
@Component
@ConfigurationProperties(prefix = "Constant")
public class Constant
{
	public static final Logger runLogger = LoggerFactory.getLogger("x.run.log");
	
	public static final Logger accessLogger = LoggerFactory.getLogger("x.access.log");
	
	public static final Logger operateLogger = LoggerFactory.getLogger("x.operate.log");
	
	public static final String AES_PASSWORD_KEY = "qckcmdbbv4u2hwxq";
	public static final String AES_IV = "0987654321abcdef";
	
	/**
	 * 返回报文的 成功/失败 标志码 
	 */
	public static Integer response_code_success;
	
	public static Integer response_code_fail;
	
	/**
	 * kafka topic
	 */
	public static String kafka_topic;
	
	public static String getKafka_topic() {
		return kafka_topic;
	}

	public static void setKafka_topic(String kafka_topic) {
		Constant.kafka_topic = kafka_topic;
	}

	public static Integer getResponse_code_success()
	{
		return response_code_success;
	}

	public static void setResponse_code_success(Integer response_code_success)
	{
		Constant.response_code_success = response_code_success;
	}

	public static Integer getResponse_code_fail()
	{
		return response_code_fail;
	}

	public static void setResponse_code_fail(Integer response_code_fail)
	{
		Constant.response_code_fail = response_code_fail;
	}
	
	
}
