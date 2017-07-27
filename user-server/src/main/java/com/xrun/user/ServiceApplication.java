package com.xrun.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import com.xrun.user.util.Constant;

/**
 * APP服务启动入口
 * @author YAOJIANBO
 *
 */
@SpringCloudApplication
//@EnableScheduling
@EnableFeignClients
public class ServiceApplication extends SpringBootServletInitializer
{
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder)
	{
		return builder.sources(ServiceApplication.class);
	}

	/**
	 * 主启动方法
	 * @param args
	 */
	public static void main(String[] args)
	{
		Constant.runLogger.info("{}|{}|{}","","","XRUN : user-server Starting to run ......");
		SpringApplication.run(ServiceApplication.class, args);
	}
}
