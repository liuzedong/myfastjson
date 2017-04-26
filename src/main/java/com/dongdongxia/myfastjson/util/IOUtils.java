package com.dongdongxia.myfastjson.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

/**
 * 
 * <P>Description: </P>
 * @ClassName: IOUtils
 * @author java_liudong@163.com  2017年4月26日 下午4:30:29
 */
public class IOUtils {

	/**
	 * 默认的配置文件
	 */
	public final static String MYFASTJSON_PROPERTIES = "myfastjson.properties";
	/**
	 * 默认加载的myfastjson.properties 配置文件,使用此默认Properties 对象进行加载
	 */
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
	
	// 初始化是否,加载默认的properties文件
	static {
		try {
			loadPropertiesFromFile();
		} catch (Throwable e){
			// skip
		}
	}
	
	/**
	 * 
	 * <p>Title: loadPropertiesFromFile</p>
	 * <p>Description: 加载myfastjson.properties配置文件</p>
	 * @author java_liudong@163.com  2017年4月26日 下午6:44:23
	 */
	public static void loadPropertiesFromFile(){
		InputStream inputStream = AccessController.doPrivileged(new PrivilegedAction<InputStream>() {
			@Override
			public InputStream run() {
				ClassLoader cl = Thread.currentThread().getContextClassLoader();
				if (cl != null){
					return cl.getResourceAsStream(MYFASTJSON_PROPERTIES);
				} else {
					return ClassLoader.getSystemResourceAsStream(MYFASTJSON_PROPERTIES);
				}
			}
		});
		
		if (null != inputStream){
			try {
				DEFAULT_PROPERTIES.load(inputStream);
				inputStream.close();
			} catch (IOException e) {
				// skip
			}
		}
	}
	
	/**
	 * 
	 * <p>Title: getStringProperty</p>
	 * <p>Description: 先获取系统的环境变量, 没有找到就去myfastjson.properties中查找</p>
	 * @param name key
	 * @return  value
	 * @author java_liudong@163.com  2017年4月26日 下午6:42:10
	 */
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
