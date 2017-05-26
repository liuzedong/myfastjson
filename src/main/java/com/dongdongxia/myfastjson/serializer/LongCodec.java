package com.dongdongxia.myfastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 
 * <P>Description: 长整形 序列化和反序列化</P>
 * @ClassName: LongCodec
 * @author java_liudong@163.com  2017年5月26日 上午10:19:49
 */
public class LongCodec implements ObjectSerializer{

	public static LongCodec instance = new LongCodec();

	@Override
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
		SerializeWriter out = serializer.out;
		
		if (object == null) {
			out.writeNull(SerializerFeature.WriteNullNumberAsZero); // 数字对象为null 是否写入0
		} else {
			long value = ((Long) object).longValue();
			out.writeLong(value);
			
			if (out.isEnable(SerializerFeature.WriteClassName) // 
					&& value < Integer.MAX_VALUE && value >= Integer.MIN_VALUE // 
					&& fieldType != Long.class
					&& fieldType != long.class) {
				out.write('L');
			}
		}
	}
	
}
