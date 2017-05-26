package com.dongdongxia.myfastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
/**
 * 
 * <P>Description: BigDecimal 序列化和反序列化</P>
 * @ClassName: BigDecimalCodec
 * @author java_liudong@163.com  2017年5月26日 上午10:53:42
 */
public class BigDecimalCodec implements ObjectSerializer{

	public static final BigDecimalCodec instance = new BigDecimalCodec();
	
	@Override
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
		SerializeWriter out = serializer.out;
		
		if (object == null) {
			out.writeNull(SerializerFeature.WriteNullNumberAsZero); //
		} else {
			BigDecimal value = (BigDecimal) object;
			
			String outText;
			if (out.isEnable(SerializerFeature.WriteBigDecimalAsPlain)) {
				outText = value.toPlainString();
			} else {
				outText = value.toString();
			}
			out.write(outText);
			
			if (out.isEnable(SerializerFeature.WriteClassName) && fieldType != BigDecimal.class && value.scale() == 0) { // 标度 是否为0
				out.write('.');
			}
		}
	}

}
