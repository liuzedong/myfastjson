package com.dongdongxia.myfastjson.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
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
	
	/**
	 * 
	 * <p>Title: encodeUTF8</p>
	 * <p>Description: 将char[] 数组中的字符,以UTF8的编码格式转换为 byte[] 数组中, 如果目标源中有数据, 会清空滴</p>
	 * <p>FastJson 中, ASCCI直接转换, 中文由两个字节,转换为三个字节, 根据UTF8原理进行转换</p>
	 * <p>本人能力有限, 根据JDK自带的转换吧</p>
	 * @param sa char[] 转换源
	 * @param sp 转换源的起始位置
	 * @param len 需要转换的长度
	 * @param da 转换到的byte[] 数组
	 * @return 返回 目标源中的最后元素的位置
	 * @author java_liudong@163.com  2017年5月11日 下午5:22:38
	 */
	public static int encodeUTF8(char[] sa, int sp, int len, byte[] da) {
		int dp = 0;
		byte[] da2 = String.valueOf(sa).substring(sp, sp + len).getBytes(Charset.forName("UTF-8"));
		dp = da2.length;
		
		System.arraycopy(da2, 0, da, 0, dp);
		
		return dp;
	}
	
	/**
	 * 
	 * <p>Title: decodeUTF8</p>
	 * <p>Description: 将字节数组以UTF8的形式,转换为char字符, 和上面一样的, char里面的有值的话,那么就直接删除啦</p>
	 * <p>FastJson 中, ASCCI直接转换, 中文由两个字节,转换为三个字节, 根据UTF8原理进行转换, 源码中定义的, 没有定义的字符编码, 就会返回-1</p>
	 * <p>本人能力有限, 根据JDK自带的转换吧</p>
	 * @param sa 源数据
	 * @param sp 源数据起始位置
	 * @param len 转换源数据长度
	 * @param da 目标容器
	 * @return 目标最后元素的数据
	 * @author java_liudong@163.com  2017年5月11日 下午5:44:16
	 */
	public static int decodeUTF8(byte[] sa, int sp, int len, char[] da) {
		int dp = 0;
		
		final int sl = sp + len;
		// 先截取
		byte[] desSa = new byte[len];
		while(sp < sl) {
			desSa[dp++] = sa[sp++];
		}
		
		char[] strDa = new String(desSa, Charset.forName("UTF-8")).toCharArray();
		System.arraycopy(strDa, 0, da, 0, dp);
		
		return dp;
	}
}
