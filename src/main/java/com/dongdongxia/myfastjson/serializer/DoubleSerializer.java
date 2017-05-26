package com.dongdongxia.myfastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DecimalFormat;

/**
 * 
 * <P>Description: 双精度对象序列化</P>
 * @ClassName: DoubleSerializer
 * @author java_liudong@163.com  2017年5月26日 上午10:43:53
 */
public class DoubleSerializer implements ObjectSerializer{
	
	public static final DoubleSerializer instance = new DoubleSerializer();
	
	private DecimalFormat decimalFormat = null; // decimal : 小数, 十进制
	
	public DoubleSerializer() {
		
	}
	
	public DoubleSerializer(DecimalFormat decimalFormat) {
		this.decimalFormat = decimalFormat;
	}
	
	public DoubleSerializer(String decimalFormat) {
		this(new DecimalFormat(decimalFormat));
	}

	@Override
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
		SerializeWriter out = serializer.out;
		
		if (object == null) {
			out.writeNull(SerializerFeature.WriteNullNumberAsZero); // 是否输出0
			return ;
		}
		
		double doubleValue = ((Double) object).doubleValue();
		
		if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue)) { // 是否是无线量, 反正就是  检测, 是否这个数字是否无效
			out.writeNull();
		} else {
			if (decimalFormat == null) {
				out.writeDouble(doubleValue, true);
			} else {
				String doubleText = decimalFormat.format(doubleValue);
				out.write(doubleText);
			}
		}
	}

}
