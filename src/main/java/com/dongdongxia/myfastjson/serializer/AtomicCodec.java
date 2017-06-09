package com.dongdongxia.myfastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * 
 * <P>Description: 原数据进行序列化,反序列化</P>
 * @ClassName: AtomicCodec
 * @author java_liudong@163.com  2017年6月9日 下午2:35:00
 */
public class AtomicCodec implements ObjectSerializer{

	public static final AtomicCodec instance = new AtomicCodec();
	
	@Override
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
		SerializeWriter out = serializer.out;
		
		// 下面就是过滤所有的元数据类型,  这些原数据类型都是线程安全的
		if (object instanceof AtomicInteger) { // Integer 类型
			AtomicInteger val = (AtomicInteger) object;
			out.writeInt(val.get());
			return ;
		}
		
		if (object instanceof AtomicBoolean) { // Boolean 类型
			AtomicBoolean val = (AtomicBoolean) object;
			out.append(val.get() ? "true" : "false");
			return ;
		}
		
		if (object == null) {
			out.writeNull(SerializerFeature.WriteNullListAsEmpty);
			return ;
		}
		
		if (object instanceof AtomicIntegerArray) { // IntegerArray   数组
			AtomicIntegerArray array = (AtomicIntegerArray) object;
			int len = array.length();
			
			out.write('[');
			for (int i = 0; i < len; ++i) {
				int val = array.get(i);
				if (i != 0) {
					out.write(',');
				}
				out.write(val);
			}
			out.write(']');
			return ;
		}
		
		
		AtomicLongArray array = (AtomicLongArray) object;
		int len = array.length();
		out.write('[');
		for (int i = 0; i < len; ++i) {
			long val = array.get(i);
			if (i != 0) {
				out.write(',');
			}
			out.writeLong(val);
		}
		out.write(']');
	}

}
