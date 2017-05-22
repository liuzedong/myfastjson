package com.dongdongxia.myfastjson.serializer;
/**
 * 
 * <P>Description: 序列化的后置过滤器</P>
 * @ClassName: AfterFilter
 * @author java_liudong@163.com  2017年5月22日 上午10:41:22
 */
public abstract class AfterFilter implements SerializeFilter{
	
	private static final ThreadLocal<JSONSerializer> serializerLocal = new ThreadLocal<JSONSerializer>(); 
	private static final ThreadLocal<Character> seperatorLocal = new ThreadLocal<Character>();
	
	// 一个逗号, 默认的分隔符
	private static final Character COMMA = Character.valueOf(',');
	
	/**
	 * 
	 * <p>Title: writeAfter</p>
	 * <p>Description: 序列化后的方法</p>
	 * @param serializer 序列化功能对象
	 * @param object 被序列化的对象
	 * @param seperator 分隔符
	 * @return 返回当前的分隔符
	 * @author java_liudong@163.com  2017年5月22日 上午10:46:17
	 */
	final char writeAfter(JSONSerializer serializer, Object object, char seperator) {
		serializerLocal.set(serializer);
		seperatorLocal.set(seperator);
		writeAfter(object);
		serializerLocal.set(null); // 清空对象
		return seperatorLocal.get();
	}
	
	/**
	 * 
	 * <p>Title: wrteKeyValue</p>
	 * <p>Description: 后置处理器, 存储键值对</p>
	 * @param key
	 * @param value
	 * @author java_liudong@163.com  2017年5月22日 上午10:53:39
	 */
	protected final void wrteKeyValue(String key, Object value) {
		JSONSerializer serializer = serializerLocal.get();
		char seperator = seperatorLocal.get();
		serializer.writeKeyValue(seperator, key, value); // 主要的写入方法
		if (seperator != ',') {
			seperatorLocal.set(COMMA);
		}
	}
	
	/**
	 * 
	 * <p>Title: writeAfter</p>
	 * <p>Description: 子类实现的方法</p>
	 * @param object 序列化对象
	 * @author java_liudong@163.com  2017年5月22日 上午10:48:11
	 */
	public abstract void writeAfter(Object object);
}
