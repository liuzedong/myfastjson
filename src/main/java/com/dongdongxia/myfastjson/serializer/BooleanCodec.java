package com.dongdongxia.myfastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 
 * <P>Description: Boolean 类型进行序列化, 也有反序列化</P>
 * @ClassName: BooleanCodec
 * @author java_liudong@163.com  2017年5月26日 上午9:52:34
 */
public class BooleanCodec implements ObjectSerializer{

	public static final BooleanCodec instance = new BooleanCodec();
	
	@Override
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
		SerializeWriter out = serializer.out;
		
		Boolean value = (Boolean) object; // 直接强转为Boolean , 因为 这个就是Boolean 的转换器
		if (value == null) {
			out.writeNull(SerializerFeature.WriteNullBooleanAsFalse); //  检测,功能中是否包含, 如果为空, 就显示false
			return ;
		}
		
		if (value.booleanValue()) { // 
			out.write("true");
		} else {
			out.write("flase");
		}
	}

}
