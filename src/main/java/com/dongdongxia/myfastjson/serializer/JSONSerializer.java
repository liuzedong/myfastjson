package com.dongdongxia.myfastjson.serializer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import com.dongdongxia.myfastjson.JSON;

/**
 * 
 * <P>Description: JSON序列化控制器</P>
 * @ClassName: JSONSerializer
 * @author java_liudong@163.com  2017年4月28日 下午2:14:19
 */
public class JSONSerializer extends SerializerFilterable{

	/** 两个主要对象, 用来控制配置和输出, 在构造方法中初始化*/
	protected final SerializeConfig config;
	public final SerializeWriter out;

	private String dateFormatPattern; // 日式格式化模板
	private DateFormat dateFormat; // 日期格式化对象
	
	/**
	 * 获取默认时区
	 */
	protected TimeZone timeZone = JSON.defaultTimeZone;
	/**
	 * 获取默认的环境
	 */
	protected Locale locale = JSON.defaultLocale;
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: 初始化默认的 写入 和 全局配置的对象</p>
	 */
	public JSONSerializer() {
		this(new SerializeWriter(), SerializeConfig.getGlobalConfig());
	}
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: 指定输出构造方法</p>
	 * @param out 序列输出
	 */
	public JSONSerializer(SerializeWriter out) {
		this(out, SerializeConfig.getGlobalConfig());
	}
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: 指定全局配置的构造方法</p>
	 * @param config 全局配置
	 */
	public JSONSerializer(SerializeConfig config) {
		this(new SerializeWriter(), config);
	}
	
	
	
	public JSONSerializer(SerializeWriter out, SerializeConfig config) {
		this.out = out;
		this.config = config;
	}
	
	/**
	 * 
	 * <p>Title: getDateFormatPattern</p>
	 * <p>Description: 获取日期格式化的模式</p>
	 * @return
	 * @author java_liudong@163.com  2017年5月27日 上午10:19:26
	 */
	public String getDateFormatPattern() {
		if (dateFormat instanceof SimpleDateFormat) {
			return ((SimpleDateFormat) dateFormat).toPattern(); // 返回类似: yyyy-MM-dd HH:mm:ss  如果指定勒的话
		}
		return dateFormatPattern; // 没有的话, 就返回指定的 格式化模式
	}
	
	/**
	 * 
	 * <p>Title: getDateFormat</p>
	 * <p>Description: 获取日期格式化对象, 没有的话, 就初始化</p>
	 * @return
	 * @author java_liudong@163.com  2017年5月27日 上午10:27:45
	 */
	public DateFormat getDateFormat() {
		if (dateFormat == null) {
			if (dateFormatPattern != null) {
				dateFormat = new SimpleDateFormat(dateFormatPattern, locale);
				dateFormat.setTimeZone(timeZone);
			}
		}
		return dateFormat;
	}
	
	protected final void writeKeyValue(char seperator, String key, Object value){
		
	}
	
}
