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

	/**
	 * Base 64的组成字符串
	 */
	public static final char[] CA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
	
	final static char [] DigitOnes = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        } ;
	
	final static char [] DigitTens = {
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
        '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
        '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
        '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
        '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
        '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
        '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
        '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
        '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
        '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
        } ;
	
	/**
     * All possible chars for representing a number as a String
     * 翻译 : 所有可能的字符数表示为一个字符串
     */
    final static char[] digits = {
        '0' , '1' , '2' , '3' , '4' , '5' ,
        '6' , '7' , '8' , '9' , 'a' , 'b' ,
        'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
        'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
        'o' , 'p' , 'q' , 'r' , 's' , 't' ,
        'u' , 'v' , 'w' , 'x' , 'y' , 'z'
    };
	
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
	
	//  数组中的数据, 用来标识个没十进制 位数的最大值,Int类型的, 主要是用来算出 每个数字的长度
	static final int[] sizeTable = {9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE};
	
	/**
	 * 
	 * <p>Title: stringSize</p>
	 * <p>Description: 计算出x数组的,长度.比如:243114, 长度是6, 这个方法,其实是在Integer里面有的</p>
	 * @param x 计算的数字
	 * @return
	 * @author java_liudong@163.com  2017年5月12日 下午1:55:12
	 */
	public static int stringSize(int x) {
		for (int i = 0; ; i++) {
			if (x < sizeTable[i]) {
				return i + 1; // 因为数组,下标是从0开始的, 所以+1
			}
		}
	}
	
	/**
	 * 
	 * <p>Title: stringSize</p>
	 * <p>Description: 计算Long 类型的数值的长度</p>
	 * @param x
	 * @return
	 * @author java_liudong@163.com  2017年5月16日 上午11:02:10
	 */
	public static int stringSize(long x) {
		long p = 10;
		for (int i = 1; i < 19; i++) {
			if (x < p) return i;
			p = 10 * p;
		}
		return 19;
	}
	
	/**
	 * 
	 * <p>Title: getChars</p>
	 * <p>Description: 将int 每一位,装入到char[] 数组中, 此处直接copy的Integer中的方法, 因为,Integer中没有提供, 是私有的</p>
	 * @param i 装入的数字
	 * @param index 装入的起始位置
	 * @param buf 装入的缓存
	 * @author java_liudong@163.com  2017年5月12日 下午2:19:54
	 */
	public static void getChars(int i, int index, char[] buf) {
		int q, r;
        int charPos = index;
        char sign = 0;

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        // Generate two digits per iteration
        while (i >= 65536) {
            q = i / 100;
        // really: r = i - (q * 100);
            r = i - ((q << 6) + (q << 5) + (q << 2));
            i = q;
            buf [--charPos] = DigitOnes[r];
            buf [--charPos] = DigitTens[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i <= 65536, i);
        for (;;) {
            q = (i * 52429) >>> (16+3);
            r = i - ((q << 3) + (q << 1));  // r = i-(q*10) ...
            buf [--charPos] = digits [r];
            i = q;
            if (i == 0) break;
        }
        if (sign != 0) {
            buf [--charPos] = sign;
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
