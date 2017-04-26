package com.dongdongxia.myfastjson.util;

import java.util.Properties;

/**
 * 
 * <P>Description: </P>
 * @ClassName: IOUtils
 * @author java_liudong@163.com  2017年4月26日 下午4:30:29
 */
public class IOUtils {
	
	public final static Properties DEFAULT_PROPERTIES = new Properties();

	// 用来,在指定的字符上, 标识,这个数字为真, A-Z(65-90), a-z(97-122), _(95), 0-9
	public final static boolean identifierFlags[] = new boolean[256];
	
	
	static {
		// 此处的c=0, 是指,从ASCII码表的 第一个字符开始
		for (char c = 0; c < identifierFlags.length; ++c){
			if (c >= 'A' && c <= 'Z'){
				identifierFlags[c] = true;
			} else if (c >= 'a' && c <= 'z'){
				identifierFlags[c] = true;
			} else if (c == '_'){
				identifierFlags[c] = true;
			} else if (c >= '0' && c <= '9'){
				identifierFlags[c] = true;
			}
		}
	}
	
	public static String getStringProperty(String name){
		String prop = null;
		try {
			prop = System.getProperty(name);
		} catch (SecurityException e){
			// skip, 不管
		}
		return (prop == null) ? DEFAULT_PROPERTIES.getProperty(name) : prop;
	}
	
}
