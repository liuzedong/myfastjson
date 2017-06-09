package com.dongdongxia.myfastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 
 * <P>Description: 将对象直接toString进行输出</P>
 * @ClassName: ToStringSerializer
 * @author java_liudong@163.com  2017年6月9日 下午2:29:12
 */
public class ToStringSerializer implements ObjectSerializer{

	@Override
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
		SerializeWriter out = serializer.out;
		
		if (object == null) {
			out.writeNull();
			return ;
		}
		
		String strVal = object.toString();
		out.writeString(strVal);
	}

}
