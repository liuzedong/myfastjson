package com.dongdongxia.myfastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 
 * <P>Description: StringBuffer, Appendable, StringBuilder 等连接的序列化对象</P>
 * @ClassName: AppendableSerializer
 * @author java_liudong@163.com  2017年6月9日 下午2:24:11
 */
public class AppendableSerializer implements ObjectSerializer{
	
	public static final AppendableSerializer instance = new AppendableSerializer();

	@Override
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
		if (object == null) { // 为null  就输出""
			SerializeWriter out = serializer.out;
			out.writeNull(SerializerFeature.WriteNullStringAsEmpty);
			return ;
		}
		
		serializer.write(object.toString());
		
	}

}
