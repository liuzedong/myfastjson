package com.dongdongxia.myfastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 
 * <P>Description: String 序列化和反序列化</P>
 * @ClassName: StringCodec
 * @author java_liudong@163.com  2017年5月26日 上午11:30:12
 */
public class StringCodec implements ObjectSerializer{

	public static StringCodec instance = new StringCodec();
	
	@Override
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
		write(serializer, (String)object);
	}
	
	public void write(JSONSerializer serializer, String value) {
		SerializeWriter out = serializer.out;
		
		if (value == null) {
			out.writeNull(SerializerFeature.WriteNullStringAsEmpty); // 字符串为空的情况
			return ;
		}
		
		out.writeString(value);
	}

}
