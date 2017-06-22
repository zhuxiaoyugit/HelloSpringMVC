package com.first.test;

public class HelloWorld
{
	private String name;
	private String age;
	
	public void setAge(String age)
	{
		this.age = age;
	}
	public void setName(String name){
		this.name = name;
		
	}
	public void printHello(){
		System.out.println("Spring : Hello "+name);
	}
	
	public void printAge(){
		System.out.println("Spring : Age "+age);
	}
}
