package com.dongdongxia.myfastjson;

public class StringTest {

	public static void main(String[] args) {
		getChars();
	}
	
	/**
	 * 
	 * <p>Title: getChars</p>
	 * <p>Description: 将字符串, 写入到char数组中</p>
	 * @author java_liudong@163.com  2017年5月10日 上午11:21:09
	 */
	public static void getChars(){
		String ss = "我是栋栋侠,谢谢";
		char[] c = new char[100];
		// c的index = 5, 0 + 95, 5
		ss.getChars(0, 0 + ss.length() , c, 0);
		System.out.println(c);
	}
}
