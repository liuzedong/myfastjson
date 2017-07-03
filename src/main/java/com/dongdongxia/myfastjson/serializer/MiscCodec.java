package com.dongdongxia.myfastjson.serializer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import com.dongdongxia.myfastjson.JSON;
import com.dongdongxia.myfastjson.JSONException;
import com.dongdongxia.myfastjson.JSONStreamAware;
import com.dongdongxia.myfastjson.parser.DefaultJSONParser;
import com.dongdongxia.myfastjson.parser.deserializer.ObjectDeserializer;

/**
 * 
 * <P>Description: 混杂的对象序列化和反序列化</P>
 * @ClassName: MiscCodec
 * @author java_liudong@163.com  2017年6月8日 上午10:24:09
 */
public class MiscCodec implements ObjectSerializer, ObjectDeserializer{
	
	public final static MiscCodec instance = new MiscCodec();

	/**
	 * 
	 * <p>Title: write</p>
	 * <p>Description: 序列化对象</p>
	 * @param serializer 输出对象
	 * @param object 需要序列化的对象
	 * @param fieldName 序列化的字段
	 * @param fieldType 字段类型
	 * @param features 序列化功能
	 * @throws IOException
	 * @author java_liudong@163.com  2017年6月8日 上午10:25:37
	 * @see com.dongdongxia.myfastjson.serializer.ObjectSerializer#write(com.dongdongxia.myfastjson.serializer.JSONSerializer, java.lang.Object, java.lang.Object, java.lang.reflect.Type, int)
	 */
	@Override
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
		SerializeWriter out = serializer.out;
		
		if (object == null) {
			out.writeNull();
			return ;
		}
		
		Class<?> objClass = object.getClass();
		
		String strVal;
		if (objClass == SimpleDateFormat.class) { // 输出SimpleDateFormat 对象的数据
			String pattern = ((SimpleDateFormat) object).toPattern(); // 获取日期格式化的模式
			
			if (out.isEnable(SerializerFeature.WriteClassName)) { // 是否输出对象的名称
				if (object.getClass() != fieldType) { // 默认的情况下,fieldType为空 {"@type":"java.text.SimpleDateFormat","val":"yyyy-MM-dd HH:mm:ss"}
					out.write('{');
					out.writeFieldName(JSON.DEFAULT_TYPE_KEY); // "@type"
					serializer.write(object.getClass().getName());
					out.writeFieldValue(',', "val", pattern);
					out.write('}');
					return ;
				}
			}
			
			strVal = pattern;
		} else if (objClass == Class.class) { // 入参的对象是Class
			Class<?> clazz = (Class<?>) object;
			strVal = clazz.getName(); 
		} else if (objClass == InetSocketAddress.class) { // 入参的对象是InetSocketAddress 网络地址对象, {"address":"192.168.0.1","port":80}
			InetSocketAddress address = (InetSocketAddress) object;
			
			InetAddress inetAddress = address.getAddress(); // 获取网络的地址
			
			out.write('{');
			if (inetAddress != null) {
				out.writeFieldName("address");
				serializer.write(inetAddress); // 此处会在对象中获取 InetAddress对象的序列化对象的
				out.write(',');
			}
			out.writeFieldName("port");
			out.writeInt(address.getPort());
			out.write('}');
			return ;
		} else if (object instanceof File) { // 入参对象是File
			strVal = ((File) object).getPath(); 
		} else if (object instanceof InetAddress) { // 入参地址对象 InetAddress
			strVal = ((InetAddress) object).getHostAddress();
		} else if (object instanceof TimeZone) { // 入参是时区偏移量对象 TimeZone , 获取时区, 中国的话: Asia/Shanghai
			TimeZone timeZone = (TimeZone) object;
			strVal = timeZone.getID();
		} else if (object instanceof Currency) { // 入参是Currency, 货币对象, getCurrencyCode() , 中国的话: CNY
			Currency currency = (Currency) object;
			strVal = currency.getCurrencyCode();
		} else if (object instanceof JSONStreamAware) { // 入参是一个自动实现的接口
			JSONStreamAware aware = (JSONStreamAware) object;
			aware.writeJSONString(out);
			return ;
		} else if (object instanceof Iterator) { // 入参是一个迭代接口的实现类
			Iterator<?> it = (Iterator<?>) object;
			writeIterator(serializer, out, it);
			return ;
		} else if (object instanceof Iterable) { // 入参是一个迭代接口的实现类
			Iterator<?> it = (Iterator<?>) object;
			writeIterator(serializer, out, it);
			return ;
		} else if (object instanceof Map.Entry) { // 入参是一个Map集合
			Map.Entry entry = (Map.Entry) object;
			Object objKey = entry.getKey();
			Object objVal = entry.getValue();
			
			if (objKey instanceof String) {
				String key = (String) objKey;
				
				if (objVal instanceof String) {
					String value = (String) objVal;
					out.writeFieldValueStringWithDoubleQuote('{', key, value);
				} else {
					out.write('{');
					out.writeFieldName(key);
					serializer.write(objVal);
				}
			} else { // key 和 value 都不是 String字符串的情况下
				out.write('{');
				serializer.write(objKey);
				out.write(':');
				serializer.write(objVal);
			}
			out.write('}');
			return ;
		} else if (object.getClass().getName().equals("net.sf.json.JSONNull")) { // 入参是JSONNull 对象
			out.writeNull();
			return ;
		} else {
			throw new JSONException("not suport class" + objClass); // 不支持入参的对象
		}
		
		out.writeString(strVal);
	}

	/**
	 * 
	 * <p>Title: writeIterator</p>
	 * <p>Description: 向流中写入迭代接口实现类中的数据</p>
	 * <p>输出的结果: ["name":"张三","age",32]</p>
	 * @param serializer 输出流的控制器
	 * @param out 输出流功能类
	 * @param it 迭代的对象
	 * @author java_liudong@163.com  2017年6月8日 上午11:41:13
	 */
	protected void writeIterator(JSONSerializer serializer, SerializeWriter out, Iterator<?> it) {
		int i = 0;
		out.write('[');
		while (it.hasNext()) {
			if (i != 0) {
				out.write(',');
			}
			Object item = it.next();
			serializer.write(item);
			++i;
		}
		out.write(']');
		return ;
	}

	@Override
	public <T> T deserialze(DefaultJSONParser parser, Type type,
			Object fieldName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getFastMatchToken() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	/*public static void main(String[] args) {
		Currency currency = Currency.getInstance(Locale.getDefault());
		String strVal = currency.getCurrencyCode();
		System.out.println(strVal);
	}*/
	
}
