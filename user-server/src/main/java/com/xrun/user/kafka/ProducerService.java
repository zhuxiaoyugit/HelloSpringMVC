package com.xrun.user.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;

/**
 * @Input注解定义了一个输出通道，发布的消息通过该通道离开应用
 * @Input注解可以使用通道名称作为参数；如果没有名称，会使用带注解的方法的名字作为参数
 * @author wgm
 */

@EnableBinding(Source.class)
public class ProducerService {
	
	@Autowired
    private Source source;
	
	/**
	 * 写output通道的数据
	 * 
	 * @param msg
	 */
	
	public void sendMessage(String message) {
		source.output().send(MessageBuilder.withPayload(message).build());
	}
}
