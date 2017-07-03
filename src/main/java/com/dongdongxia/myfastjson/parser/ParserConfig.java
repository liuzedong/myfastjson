package com.dongdongxia.myfastjson.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.security.AccessControlException;
import java.util.IdentityHashMap;
import java.util.Map;

import com.dongdongxia.myfastjson.parser.deserializer.ASMDeserializerFactory;
import com.dongdongxia.myfastjson.parser.deserializer.ObjectDeserializer;
import com.dongdongxia.myfastjson.util.ASMClassLoader;
import com.dongdongxia.myfastjson.util.ASMUtils;
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
	
	public final boolean fieldBase;
	protected ASMDeserializerFactory asmFactory;
	
	/**
	 * 是否使用 字节码 生成对象
	 */
	private boolean asmEnable = !ASMUtils.IS_ANDROID;
	
	/**
	 * 将常见解析对象,进行缓存
	 */
	private final IdentityHashMap<Type, ObjectDeserializer> deserializers = new IdentityHashMap<Type, ObjectDeserializer>();
	
	/**
	 * 
	 */
	public final SymbolTable symbolTable = new SymbolTable(4096);
	
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
	
	public ParserConfig() {
		this(false);
	}
	
	public ParserConfig(boolean fieldBase) {
		this(null, null, fieldBase);
	}
	
	public ParserConfig(ClassLoader parentClassLoader) {
		this(null, parentClassLoader, false);
	}
	
	public ParserConfig(ASMDeserializerFactory asmFactory) {
		this(asmFactory, null, false);
	}
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: 总的构造方法,用来初始化, 基础的反射JSON转对象的缓存</p>
	 * @param asmFactory
	 * @param parentClassLoader
	 * @param fieldBase
	 */
	private ParserConfig(ASMDeserializerFactory asmFactory, ClassLoader parentClassLoader, boolean fieldBase) {
		this.fieldBase = fieldBase;
		if (asmFactory == null && !ASMUtils.IS_ANDROID) {
			try {
				if (parentClassLoader == null) { // 使用类加载器, 初始化ASMDeserializerFactory
					asmFactory = new ASMDeserializerFactory(new ASMClassLoader());
				} else {
					asmFactory = new ASMDeserializerFactory(parentClassLoader);
				}
			} catch (ExceptionInInitializerError error) { // 初始化值或 静态变量初始化的期间发生的异常
				// skip
			} catch (AccessControlException error) { // 权限不够抛出来的异常, 比如访问权限, 网络权限, 数据类型权限等
				// skip
			} catch (NoClassDefFoundError error) { // 无法找到该类的定义时, 抛出此异常
				// skip
			}
		}
		
		this.asmFactory = asmFactory;
		
		if (asmFactory == null) { // 这里创建对象为null , 就不进行字节码, 进行解析, 一般, 不为null, 所以不是android , 就为true
			asmEnable = false; 
		}
		
		
	}
	
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
