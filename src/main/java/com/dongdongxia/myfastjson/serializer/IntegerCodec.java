package com.dongdongxia.myfastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 
 * <P>Description: Byte和Short和Integer对象的序列化和反序列化对象</P>
 * @ClassName: IntegerCodec
 * @author java_liudong@163.com  2017年5月26日 上午10:10:21
 */
public class IntegerCodec implements ObjectSerializer{

	public static final IntegerCodec instance = new IntegerCodec();
	
	@Override
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
		SerializeWriter out = serializer.out;
		
		Number value = (Number) object;
		
		if (value == null) {
			out.writeNull(SerializerFeature.WriteNullNumberAsZero); // 检测是否输出为0
			return ;
		} 
		
		if (object instanceof Long) {
			out.writeLong(value.longValue());
		} else {
			out.writeInt(value.intValue());
		}
		
		if (out.isEnable(SerializerFeature.WriteClassName)) { // 检测是否写入类的名称
			Class<?> clazz = value.getClass(); 
			if (clazz == Byte.class) {
				out.write('B');
			} else if (clazz == Short.class) {
				out.write('S');
			}
		}
	}

}
