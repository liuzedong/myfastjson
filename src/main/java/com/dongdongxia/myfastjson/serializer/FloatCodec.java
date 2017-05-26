package com.dongdongxia.myfastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.NumberFormat;
/**
 * 
 * <P>Description: 单精度序列化和反序列化</P>
 * @ClassName: FloatCodec
 * @author java_liudong@163.com  2017年5月26日 上午10:29:41
 */
public class FloatCodec implements ObjectSerializer{
	
	private NumberFormat decimalFormat;

	public static FloatCodec instance = new FloatCodec();
	
	public FloatCodec() {
		
	}
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: 指定格式化对象</p>
	 * @param decimalFormat
	 */
	public FloatCodec(DecimalFormat decimalFormat) { // 是NumberFormat的子类
		this.decimalFormat = decimalFormat;
	}
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: 指定格式化对象</p>
	 * @param decimalFormat
	 */
	public FloatCodec(String decimalFormat) {
		this(new DecimalFormat(decimalFormat));
	}
	
	@Override
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
		SerializeWriter out = serializer.out;
		
		if (object == null) {
			out.writeNull(SerializerFeature.WriteNullNumberAsZero);
			return ;
		}
		
		float value = ((Float) object).floatValue();
		if (decimalFormat != null) {
			String floatText = decimalFormat.format(value);
			out.write(floatText);
		} else {
			out.writeFloat(value, true);
		}
	}

}
