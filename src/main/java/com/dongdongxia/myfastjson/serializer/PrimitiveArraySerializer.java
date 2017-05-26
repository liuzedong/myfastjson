package com.dongdongxia.myfastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 
 * <P>Description: 基础类型数组序列化</P>
 * @ClassName: PrimitiveArraySerializer
 * @author java_liudong@163.com  2017年5月26日 下午2:10:34
 */
public class PrimitiveArraySerializer implements ObjectSerializer{

	public static PrimitiveArraySerializer instance = new PrimitiveArraySerializer();
	
	@Override
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
		SerializeWriter out = serializer.out;
		
		if (object == null) {
			out.writeNull(SerializerFeature.WriteNullListAsEmpty);
			return ;
		}
		
		/* 以 [1,2,3] 进行显示*/ 
		if (object instanceof int[]) {
			int[] array = (int[]) object;
			out.write('[');
			for (int i = 0; i < array.length; ++i) {
				if (i != 0) {
					out.write(',');
				}
				out.writeInt(array[i]);
			}
			out.write(']');
			return ;
		}
		
		/* 以 [1,2,3] 进行显示*/ 
		if (object instanceof short[]) {
			short[] array = (short[]) object;
			out.write('[');
			for (int i = 0; i < array.length; ++i) {
				if (i != 0) {
					out.write(',');
				}
				out.writeInt(array[i]);
			}
			out.write(']');
			return ;
		}
		
		if (object instanceof long[]) {
			long[] array = (long[]) object;
			
			out.write('[');
			for (int i = 0; i < array.length; ++i) {
				if (i != 0) {
					out.write(',');
				}
				out.writeLong(array[i]);
			}
			out.write(']');
			return ;
		}
		
		if (object instanceof boolean[]) {
			boolean[] array = (boolean[]) object;
			out.write('[');
			for (int i = 0; i < array.length; ++i) {
				if (i != 0) {
					out.write(',');
				}
				out.write(array[i]);
			}
			out.write(']');
			return ;
		}
		
		if (object instanceof float[]) {
			float[] array = (float[]) object;
			out.write('[');
			for (int i = 0; i < array.length; ++i) {
				if (i != 0) {
					out.write(',');
				}
				
				float item = array[i];
				if (Float.isNaN(item)) {
					out.writeNull();
				} else {
					out.append(Float.toString(item));
				}
			}
			out.write(']');
			return ;
		}
		
		if (object instanceof double[]) {
			double[] array = (double[]) object;
			out.write('[');
			for (int i = 0; i < array.length; ++i) {
				if (i != 0) {
					out.write(',');
				}
				
				double item = array[i];
				if (Double.isNaN(item)) {
					out.writeNull();
				} else {
					out.append(Double.toString(item));
				}
			}
			out.write(']');
			return ;
		}
		
		if (object instanceof byte[]) {
			byte[] array = (byte[]) object;
			out.writeByteArray(array);
			return ;
		}
		
		char[] chars = (char[]) object;
		out.writeString(new String(chars));
	}

}
