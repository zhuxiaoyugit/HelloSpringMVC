package com.first.test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main
{
	public static void main(String[] args)
	{
		ApplicationContext context= new ClassPathXmlApplicationContext("SpringBeans.xml");
		HelloWorld h=(HelloWorld) context.getBean("helloBean");
		h.printAge();
		h.printHello();
		
	}
}
