package com.dongdongxia.myfastjson.serializer;

import com.dongdongxia.myfastjson.annotation.JSONType;
import com.dongdongxia.myfastjson.util.FieldInfo;

/**
 * 
 * <P>Description: 封装序列化对象的类</P>
 * @ClassName: SerializeBeanInfo
 * @author java_liudong@163.com  2017年6月7日 下午5:42:23
 */
public class SerializeBeanInfo {
	
	/**
	 * 需要序列化的JavaBean
	 */
	protected final Class<?> beanType;
	/**
	 * 序列化对象的字段上面加载 JSONType注解中的一个属性
	 */
	protected final String typeName;
	/**
	 * 序列化对象注解
	 */
	protected final  JSONType jsonType;
	/**
	 * 未排序的字段集合
	 */
	protected final FieldInfo[] fields;
	/**
	 * 排序后的字段集合, 默认使用自然排序,按照字母
	 */
	protected final FieldInfo[] sortedFields;
	/**
	 * 序列化功能, SerializeFeature 中的功能
	 */
	protected int features;
	
	public 	SerializeBeanInfo(Class<?> beanType, JSONType jsonType, String typeName, int features, FieldInfo[] fields, FieldInfo[] sortedFields) {
		this.beanType = beanType;
		this.jsonType = jsonType;
		this.typeName = typeName;
		this.features = features;
		this.fields = fields;
		this.sortedFields = sortedFields;
	}
}
