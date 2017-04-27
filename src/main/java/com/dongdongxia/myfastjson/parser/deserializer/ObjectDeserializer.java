package com.dongdongxia.myfastjson.parser.deserializer;

import java.lang.reflect.Type;

import com.dongdongxia.myfastjson.parser.DefaultJSONParser;

/**
 * 
 * <P>Description: 需要注册到ParserConfig 对象中</P>
 * <P>ParserConfig.getGlobalInstance().putDeserializer(OrderActionEnum.class, new OrderActionEnumDeser());   这样调用</P>
 * @ClassName: ObjectDeserializer
 * @author java_liudong@163.com  2017年4月27日 下午6:05:03
 */
public interface ObjectDeserializer {

	/**
	 * 
	 * <p>Title: deserialze</p>
	 * <p>Description: TODO</p>
	 * @param parser 解析 DefaultJSONParser 被泛序列化
	 * @param type 对类型进行反序列化
	 * @param fieldName 对类型中的字段进行反序列化
	 * @return
	 * @author java_liudong@163.com  2017年4月27日 下午6:09:04
	 */
	<T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName);
	
	int getFastMatchToken();
}
