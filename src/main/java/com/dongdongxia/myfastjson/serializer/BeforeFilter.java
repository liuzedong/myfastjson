package com.dongdongxia.myfastjson.serializer;
/**
 * 
 * <P>Description: JSON前置过滤器</P>
 * @ClassName: BeforeFilter
 * @author java_liudong@163.com  2017年4月28日 下午2:12:21
 */
public abstract class BeforeFilter implements SerializeFilter{
	
	private static final ThreadLocal<JSONSerializer> serializerLocal = new ThreadLocal<JSONSerializer>();
	// 分离器 : seperator
	private static final ThreadLocal<Character> seperatorLocal = new ThreadLocal<Character>();
	
	private final static Character COMMA = Character.valueOf(',');
	
	/**
	 * 
	 * <p>Title: writeBefore</p>
	 * <p>Description: 写入的方法</p>
	 * @param serializer
	 * @param object
	 * @param seperator
	 * @return
	 * @author java_liudong@163.com  2017年4月28日 下午2:22:24
	 */
	final char writeBefore(JSONSerializer serializer, Object object, char seperator) {
		serializerLocal.set(serializer);
		seperatorLocal.set(seperator);
		writeBefore(object);
		serializerLocal.set(null);
		return seperatorLocal.get();
	}
	
	
	protected final void writeKeyValue(String key, Object value) {
		JSONSerializer serializer = serializerLocal.get();
		char seperator = seperatorLocal.get();
		serializer.writeKeyValue(seperator, key, value);
		if (seperator != ',') {
			seperatorLocal.set(COMMA);
		}
	}

	/**
	 * 
	 * <p>Title: writeBefore</p>
	 * <p>Description: 子类实现写入的方法</p>
	 * @param object
	 * @author java_liudong@163.com  2017年4月28日 下午2:21:49
	 */
	public abstract void writeBefore(Object object);
}
