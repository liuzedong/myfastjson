package com.dongdongxia.myfastjson.parser;

import java.lang.reflect.Field;
import java.util.Map;

import com.dongdongxia.myfastjson.util.IOUtils;

/**
 * 
 * <P>Description: 解析的默认配置</P>
 * @ClassName: ParserConfig
 * @author java_liudong@163.com  2017年4月26日 下午6:59:14
 */
public class ParserConfig {

	public final static String DENY_PROPERTY = "myfastjson.parser.deny";
	public final static String AUTOTYPE_ACCEPT = "myfastjson.parser.autoTypeAccept";
	public final static String AUTOTYPE_SUPPORT_PROPERTY = "myfastjson.parser.autoTypeSupport";
	
	public static final String[] DENYS;
	private static final String[] AUTO_TYPE_ACCEPT_LIST;
	public static final boolean AUTO_SUPPORT;
	
	
	static {
		{
			String property = IOUtils.getStringProperty(DENY_PROPERTY);
			DENYS = splitItemsFormProperty(property);
		}
		{
			String property = IOUtils.getStringProperty(AUTOTYPE_SUPPORT_PROPERTY);
			AUTO_SUPPORT = "true".equals(property);
		}
		{
			String property = IOUtils.getStringProperty(AUTOTYPE_ACCEPT);
			String items[] = splitItemsFormProperty(property);
			if (items == null){
				items = new String[0];
			}
			AUTO_TYPE_ACCEPT_LIST = items;
		}
	}

	/**
	 * 
	 * <p>Title: getGlobalInstance</p>
	 * <p>Description: 单例,获取对象的方法</p>
	 * @return
	 * @author java_liudong@163.com  2017年4月26日 下午7:12:18
	 */
	public static ParserConfig getGlobalInstance() {
		return global;
	}
	
	public static ParserConfig global = new ParserConfig();
	
	/**
	 * 
	 * <p>Title: splitItemsFormProperty</p>
	 * <p>Description: 将字符串按照, 进行分割</p>
	 * @param property myfastjson.properties中的value
	 * @return
	 * @author java_liudong@163.com  2017年4月26日 下午7:07:24
	 */
	private static String[] splitItemsFormProperty(final String property){
		if (property!= null && property.length() > 0){
			return property.split(",");
		}
		return null;
	}
	
	/**
	 * 
	 * <p>Title: parserAllFieldToCache</p>
	 * <p>Description: 将对象中的字段, 装入到Map中,进行缓存,
	 * 生成fieldName的快照,减少之后的findField的轮循</p>
	 * @param clazz
	 * @param fieldCacheMap
	 * @author java_liudong@163.com  2017年6月5日 下午4:04:49
	 */
	public static void parserAllFieldToCache(Class<?> clazz, Map<String, Field> fieldCacheMap) {
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			String fieldName = field.getName(); // 获取字段名称
			if (!fieldCacheMap.containsKey(fieldName)) {
				fieldCacheMap.put(fieldName, field);
			}
		}
		if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) { // 检测是否有父类, 如果有, 则把父类中的字段也进行缓存,迭代
			parserAllFieldToCache(clazz.getSuperclass(), fieldCacheMap);
		}
	}
	
	/**
	 * 
	 * <p>Title: getFieldFromCache</p>
	 * <p>Description: 从缓存中获取,指定对象的Field属性</p>
	 * @param fieldName 字段名
	 * @param fieldCacheMap 缓存
	 * @return
	 * @author java_liudong@163.com  2017年6月5日 下午6:34:22
	 */
	public static Field getFieldFromCache(String fieldName, Map<String, Field> fieldCacheMap) {
		Field field = fieldCacheMap.get(fieldName);
		if (field == null) {
			field = fieldCacheMap.get("_" + fieldName); // _name
		}
		
		if (field == null) {
			field = fieldCacheMap.get("m_" + fieldName); // m_name
		}
		
		if (field == null) {
			char c0 = fieldName.charAt(0);
			if (c0 >= 'a' && c0 <= 'z') { // 小写变成大写,再查询缓存
				char[] chars = fieldName.toCharArray();
				chars[0] -= 32; // lower
				String fieldNameX = new String(chars);
				field = fieldCacheMap.get(fieldNameX);
			}
		}
		
		return field;
	}
}
