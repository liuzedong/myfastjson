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
