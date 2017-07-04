package com.reids.Redis_expire;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisTemp {
	
	public static StringRedisTemplate getConnect(){
		JedisConnectionFactory connectionFactory=new JedisConnectionFactory();
		connectionFactory.setHostName("localhost");
		connectionFactory.setPort(6379);
		connectionFactory.afterPropertiesSet();
		
		StringRedisTemplate redis=new StringRedisTemplate(connectionFactory);
		
		redis.afterPropertiesSet();
		
		return redis;
	}
	
	public static void main(String[] args) throws Exception {
		StringRedisTemplate srt=getConnect();
		
		srt.opsForValue().set("aa", "aa",2000,TimeUnit.MILLISECONDS);
		
		String str=srt.opsForValue().get("aa");
		System.out.println(str);
		
		
		Thread.sleep(5000);
		
		str=srt.opsForValue().get("aa");
		System.out.println(str);
		
	}
}
