package com.dongdongxia.myfastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;

/**
 * 
 * <P>Description: BigInteger 序列化和反序列化</P>
 * @ClassName: BigIntegerCodec
 * @author java_liudong@163.com  2017年5月26日 上午11:25:45
 */
public class BigIntegerCodec implements ObjectSerializer{

	public static final BigIntegerCodec instance = new BigIntegerCodec();
	
	@Override
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
		SerializeWriter out = serializer.out;
		
		if (object == null) {
			out.writeNull(SerializerFeature.WriteNullNumberAsZero); //
			return ;
		}
		
		BigInteger value = (BigInteger) object;
		out.write(value.toString());
	}

}
