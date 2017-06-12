package com.dongdongxia.myfastjson.serializer;
/**
 * 
 * <P>Description: 全局序列化</P>
 * @ClassName: ContextObjectSerializer
 * @author java_liudong@163.com  2017年6月12日 上午11:42:32
 */
public interface ContextObjectSerializer extends ObjectSerializer{

	/**
	 * 
	 * <p>Title: write</p>
	 * <p>Description: 序列化整个字段对象</p>
	 * @param serializer 序列化输出流
	 * @param object 序列化对象
	 * @param context 序列化全局对象
	 * @author java_liudong@163.com  2017年6月12日 上午11:43:18
	 */
	void write(JSONSerializer serializer, Object object, BeanContext context);
	
}
