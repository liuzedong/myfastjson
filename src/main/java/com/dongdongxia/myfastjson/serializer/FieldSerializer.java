package com.dongdongxia.myfastjson.serializer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import com.dongdongxia.myfastjson.JSON;
import com.dongdongxia.myfastjson.annotation.JSONField;
import com.dongdongxia.myfastjson.util.FieldInfo;

/**
 * 
 * <P>Description: 对象中的字段进行序列化</P>
 * @ClassName: FieldSerializer
 * @author java_liudong@163.com  2017年6月9日 下午3:44:59
 */
public class FieldSerializer implements Comparable<FieldSerializer>{

	public final FieldInfo fieldInfo;
	protected final boolean writeNull;
	protected int features;
	
	/**
	 * 前缀使用双引号
	 */
	private final String double_quote_fieldPrefix;
	/**
	 * 前缀使用单引号
	 */
	private String single_quoted_fieldPrefix;
	/**
	 * 前缀不使用引号
	 */
	private String un_quoted_fieldPrefix;
	/**
	 * 对象的信息, Class, FieldInfo, format(时间格式化)
	 */
	protected BeanContext fieldContext;
	
	private String format;
	/**
	 * 枚举使用字符串写入, 默认为false
	 */
	protected boolean writeEnumUsingToString = false;
	/**
	 * 枚举的字段名称使用字符串写入
	 */
	protected boolean writeEnumUsingName = false;
	/**
	 * 序列化容器使用
	 */
	protected boolean serializeUsing = false;
	/**
	 * 保存当前运行时 的序列化类的信息
	 */
	private RuntimeSerializerInfo runtimeInfo;
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: 构造方法,初始所有的类中的字段信息</p>
	 * @param beanType
	 * @param fieldInfo
	 */
	public FieldSerializer(Class<?> beanType, FieldInfo fieldInfo) {
		this.fieldInfo = fieldInfo;
		this.fieldContext = new BeanContext(beanType, fieldInfo);
		
		fieldInfo.setAccessible();
		
		this.double_quote_fieldPrefix = '"' + fieldInfo.name + "\":"; // "name":
		
		boolean writeNull = false;
		JSONField annotation = fieldInfo.getAnnotation();
		if (annotation != null) {
			/**----------------- 检测功能信息 ---------------------*/
			for (SerializerFeature feature : annotation.serialzeFeatures()) { // 获取所有序列化的功能
				if ((feature.getMask() & SerializerFeature.WRITE_MAP_NULL_FEATURES) != 0) { // 检测, 是否包含所有入参为空的情况
					writeNull = true;
					break ;
				}
			}
			
			
			/**----------------- 获取格式化信息 ---------------------*/
			format = annotation.format();
			
			if (format.trim().length() == 0) {
				format = null;
			}
			
			
			/**----------------- 检测功能信息 ---------------------*/
			for (SerializerFeature feature : annotation.serialzeFeatures()) {
				if (feature == SerializerFeature.WriteEnumUsingToString) {
					writeEnumUsingToString = true;
				} else if (feature == SerializerFeature.WriteEnumUsingName) {
					writeEnumUsingName = true;
				}
			}
			features = SerializerFeature.of(annotation.serialzeFeatures()); // 检测, 包含的功能, 将功能都写入到功能的枚举里面
		}
		this.writeNull = writeNull;
	}
	
	/**
	 * 
	 * <p>Title: writePrefix</p>
	 * <p>Description: 写入前缀</p>
	 * @param serializer
	 * @throws IOException
	 * @author java_liudong@163.com  2017年6月9日 下午4:20:10
	 */
	public void writePrefix(JSONSerializer serializer) throws IOException {
		SerializeWriter out = serializer.out;
		
		if (out.quoteFieldNames) {
			if (out.useSingleQuotes) {
				if (single_quoted_fieldPrefix == null) {
					single_quoted_fieldPrefix = '\'' + fieldInfo.name + "\':"; // 使用单引号 'name':
				}
				out.write(single_quoted_fieldPrefix);
			} else {
				out.write(double_quote_fieldPrefix);
			}
		} else {
			if (un_quoted_fieldPrefix == null) {
				this.un_quoted_fieldPrefix = fieldInfo.name + ":"; // 没有引号的情况, name:
			}
			out.write(un_quoted_fieldPrefix);
		}
	}
	
	/**
	 * 
	 * <p>Title: getPropertyValueDirect</p>
	 * <p>Description: 获取字段上面的对象</p>
	 * @param object
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @author java_liudong@163.com  2017年6月9日 下午4:30:51
	 */
	public Object getPropertyValueDirect(Object object) throws InvocationTargetException, IllegalAccessException{
		return fieldInfo.get(object);
	}
	
	/**
	 * 
	 * <p>Title: getPropertyValue</p>
	 * <p>Description: 获取对象上面的值</p>
	 * @param object
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @author java_liudong@163.com  2017年6月9日 下午4:41:00
	 */
	public Object getPropertyValue(Object object) throws InvocationTargetException, IllegalAccessException {
		Object propertyValue = fieldInfo.get(object);
		if (format != null && propertyValue != null) {
			if (fieldInfo.fieldClass == Date.class) {
				SimpleDateFormat dateFormat = new SimpleDateFormat(format);
				dateFormat.setTimeZone(JSON.defaultTimeZone);
				return dateFormat.format(propertyValue);
			}
		}
		return propertyValue;
	}
	
	@Override
	public int compareTo(FieldSerializer o) {
		return this.fieldInfo.compareTo(o.fieldInfo);
	}
	
	/**
	 * 
	 * <p>Title: writeValue</p>
	 * <p>Description: 将字段写入到流中</p>
	 * @param serializer 写入的流
	 * @param propertyValue 字段值
	 * @throws Exception
	 * @author java_liudong@163.com  2017年6月12日 上午9:48:42
	 */
	public void writeValue(JSONSerializer serializer, Object propertyValue) throws Exception {
		if (runtimeInfo == null) {
			
			Class<?> runtimeFieldClass;
			if (propertyValue == null) {
				runtimeFieldClass = this.fieldInfo.fieldClass; // 字段的对象
			} else {
				runtimeFieldClass = propertyValue.getClass(); 
			}
			
			ObjectSerializer fieldSerializer = null;
			JSONField fieldAnnotation = fieldInfo.getAnnotation();
			
			if (fieldAnnotation != null && fieldAnnotation.serializeUsing() != Void.class) {
				fieldSerializer = (ObjectSerializer) fieldAnnotation.serializeUsing().newInstance();
				serializeUsing = true;
			} else {
				if (format != null) { // 双进度, 数字格式化
					if (runtimeFieldClass == double.class || runtimeFieldClass == Double.class) {
						fieldSerializer = new DoubleSerializer(format);
					} else if (runtimeFieldClass == float.class || runtimeFieldClass == Float.class) {
						fieldSerializer = new FloatCodec(format);
					}
				}
				if (fieldSerializer == null) {
					fieldSerializer = serializer.getObjectWriter(runtimeFieldClass); // 获取字段序列化的实现对象
				}
			}
			
			runtimeInfo = new RuntimeSerializerInfo(fieldSerializer, runtimeFieldClass); // 参数一: 字段序列化实现类, 参数二: 需要序列化的对象
		}
		
		// 下面是 runtimeSerializer不为空的情况
		final RuntimeSerializerInfo runtimeInfo = this.runtimeInfo;
		
		final int fieldFeatures = fieldInfo.serializeFeatures; // 序列化功能
		
		if (propertyValue == null) { // 当字段的值为null(空) 的情况, 根据不同的类型,输出不同的空值
			Class<?> runtimeFieldClass = runtimeInfo.runtimeFieldClass;
			SerializeWriter out = serializer.out;
			if (Number.class.isAssignableFrom(runtimeFieldClass)) { // 检测需要序列化对象-> 是否是 Number的子类或者当前类
				out.writeNull(features, SerializerFeature.WriteNullNumberAsZero.mask);
				return ;
			} else if (String.class == runtimeFieldClass) {
				out.writeNull(features,SerializerFeature.WriteNullStringAsEmpty.mask);
				return ;
			} else if (Boolean.class == runtimeFieldClass) {
				out.writeNull(features, SerializerFeature.WriteNullBooleanAsFalse.mask);
				return ;
			} else if (Collection.class.isAssignableFrom(runtimeFieldClass)) { // 集合
				out.writeNull(features, SerializerFeature.WriteNullListAsEmpty.mask);
				return ;
			}
			
			ObjectSerializer fieldSerializer = runtimeInfo.fieldSerializer; // 序列化实现对象
			
			if (out.isEnable(SerializerFeature.WRITE_MAP_NULL_FEATURES) && fieldSerializer instanceof JavaBeanSerializer) {
				out.writeNull();
				return ;
			}
			
			fieldSerializer.write(serializer, null, fieldInfo.name, fieldInfo.fieldType, fieldFeatures);
			return ;
		}
		
		// 下面是 字段的值不为空的情况
		if (fieldInfo.isEnum) {
			if (writeEnumUsingName) {
				serializer.out.writeString(((Enum<?>) propertyValue).name()); // 写入枚举的name
				return ;
			}
			
			if (writeEnumUsingToString) {
				serializer.out.writeString(((Enum<?>) propertyValue).toString());
				return ;
			}
		}
		
		
		Class<?> valueClass = propertyValue.getClass();
		ObjectSerializer valueSerializer;
		if (valueClass == runtimeInfo.runtimeFieldClass || serializeUsing) {
			valueSerializer = runtimeInfo.fieldSerializer;
		} else {
			valueSerializer = serializer.getObjectWriter(valueClass);
		}
		
		if (format != null && !(valueSerializer instanceof DoubleSerializer || valueSerializer instanceof FloatCodec)) { // 
			if (valueSerializer instanceof ContextObjectSerializer) {
				((ContextObjectSerializer) valueSerializer).write(serializer, propertyValue, this.fieldContext);
			} else {
				serializer.writeWithFormat(propertyValue, format);
			}
			return ;
		}
		
		if (fieldInfo.unwrapped && valueSerializer instanceof JavaBeanSerializer) {
			JavaBeanSerializer javaBeanSerializer = (JavaBeanSerializer) valueSerializer;
			javaBeanSerializer.write(serializer, propertyValue, fieldInfo.name, fieldInfo.fieldType, fieldFeatures, true);
			return ;
		}
		
		valueSerializer.write(serializer, propertyValue, fieldInfo.name, fieldInfo.fieldType, fieldFeatures);
	}

	/**
	 * 
	 * <P>Description: 运行时 序列化信息, 保存当前, 对象和运行时字段</P>
	 * @ClassName: RuntimeSerializerInfo
	 * @author java_liudong@163.com  2017年6月9日 下午3:53:50
	 */
	static class RuntimeSerializerInfo {
		final ObjectSerializer fieldSerializer;
		final Class<?> runtimeFieldClass;
		
		public RuntimeSerializerInfo (ObjectSerializer fieldSerializer, Class<?> runtimeFieldClass) {
			this.fieldSerializer = fieldSerializer;
			this.runtimeFieldClass = runtimeFieldClass;
		}
	}
	
}
