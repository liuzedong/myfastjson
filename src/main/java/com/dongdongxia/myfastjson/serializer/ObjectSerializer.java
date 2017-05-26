package com.dongdongxia.myfastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 
 * <P>Description: 对象序列化成为字符接口</P>
 * @ClassName: ObjectSerializer
 * @author java_liudong@163.com  2017年5月26日 上午9:45:57
 */
public interface ObjectSerializer {

	/**
	 * 
	 * <p>Title: write</p>
	 * <p>Description: myfastjson调用这个回调方法在序列化的时候遇到一个指定类型的字段。</p>
	 * @param serializer 
	 * @param object 需要转换成json的对象
	 * @param fieldName 父对象的字段名称
	 * @param fieldType 父对象的字段类型
	 * @param features 父对象字段序列化器功能
	 * @throws IOException
	 * @author java_liudong@163.com  2017年5月26日 上午9:48:01
	 */
	void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException;
	
}
