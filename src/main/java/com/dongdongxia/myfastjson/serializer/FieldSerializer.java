package com.dongdongxia.myfastjson.serializer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

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
	
	@Override
	public int compareTo(FieldSerializer o) {
		return 0;
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
