package com.dongdongxia.myfastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 
 * <P>Description: 字符对象序列化和反序列化</P>
 * @ClassName: CharacterCodec
 * @author java_liudong@163.com  2017年5月26日 上午9:59:52
 */
public class CharacterCodec implements ObjectSerializer{

	public static final CharacterCodec instance = new CharacterCodec();
	
	@Override
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
		SerializeWriter out = serializer.out;
		
		Character value = (Character) object;
		
		if (value ==  null) {
			out.write("");
			return ;
		}
		
		char c = value.charValue();
		if (c == 0) {
			out.writeString("\u0000");
		} else {
			out.writeString(value.toString());
		}
	}

}
