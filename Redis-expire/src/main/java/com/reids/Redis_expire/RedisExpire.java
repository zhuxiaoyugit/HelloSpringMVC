package com.reids.Redis_expire;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;

import redis.clients.jedis.Jedis;

/**
 * Hello world!
 *
 */
//@ContextConfiguration(locations={"classpath*:applicationContext.xml"})
public class RedisExpire 
{
//	private static Jedis jedis;  
//    @Autowired  
//    @Qualifier("jedisConnectionFactory")  
//    private JedisConnectionFactory jedisConnectionFactory; 
    
    
    public static void main( String[] args ) throws InterruptedException
    {
       ApplicationContext app = new ClassPathXmlApplicationContext("applicationContext.xml");
       StringRedisTemplate template= (StringRedisTemplate) app.getBean("redisTemplate");
       
       template.opsForValue().set("bb", "bb");
       System.out.println(template.opsForValue().get("bb"));
    }
    
    
  
}
