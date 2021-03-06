package com.dongdongxia.myfastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.dongdongxia.myfastjson.JSONException;
import com.dongdongxia.myfastjson.annotation.JSONField;
import com.dongdongxia.myfastjson.util.FieldInfo;
import com.dongdongxia.myfastjson.util.TypeUtils;
/**
 * 
 * <P>Description: 序列化JavaBean的功能</P>
 * @ClassName: JavaBeanSerializer
 * @author java_liudong@163.com  2017年6月12日 下午2:08:22
 */
public class JavaBeanSerializer extends SerializerFilterable implements ObjectSerializer{

	protected final FieldSerializer[] getters;
	protected final FieldSerializer[] sortedGetters;
	
	protected SerializeBeanInfo beanInfo;
	
	public JavaBeanSerializer(Class<?> beanType) {
		this(beanType, (Map<String, String>)null);
	}
	
	public JavaBeanSerializer(Class<?> beanType, String... aliasList) {
		this(beanType, createAliasMap(aliasList));
	}

	/**
	 * 
	 * <p>Title: createAMap</p>
	 * <p>Description: 将可变数组中的值, 转换成为MAP形式</p>
	 * @param aliasList
	 * @return
	 * @author java_liudong@163.com  2017年6月12日 下午2:14:16
	 */
	static Map<String, String> createAliasMap(String... aliasList) {
		Map<String, String> aliasMap = new HashMap<String, String>();
		for (String alias : aliasList) {
			aliasMap.put(alias, alias);
		}
		return aliasMap;
	}
	
	public JavaBeanSerializer(Class<?> beanType, Map<String, String> aliasMap) {
		this(TypeUtils.buildBeanInfo(beanType, aliasMap, null));
	}
	
	public JavaBeanSerializer(SerializeBeanInfo beanInfo) {
		this.beanInfo = beanInfo;
		
		sortedGetters = new FieldSerializer[beanInfo.sortedFields.length];
		for (int i = 0; i < sortedGetters.length; ++i) {
			sortedGetters[i] = new FieldSerializer(beanInfo.beanType, beanInfo.sortedFields[i]);
		}
		
		if (beanInfo.fields == beanInfo.sortedFields) {
			getters = sortedGetters;
		} else {
			getters = new FieldSerializer[beanInfo.fields.length];
			for (int i = 0; i < getters.length; ++i) {
				getters[i] = getFieldSerializer(beanInfo.fields[i].name);
			}
		}
	}
	
	/**
	 * 
	 * <p>Title: writeDirectNonContext</p>
	 * <p>Description: 直接将对象中的某个字段写入到缓存中</p>
	 * @param serializer
	 * @param object
	 * @param fieldName
	 * @param fieldType
	 * @param features
	 * @throws IOException
	 * @author java_liudong@163.com  2017年6月14日 上午9:21:28
	 */
	public void writeDirectNonContext(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
		write(serializer, object, fieldName, fieldType, features);
	}
	
	/**
	 * 
	 * <p>Title: writeAsArray</p>
	 * <p>Description: 写入的字段是数组类型的</p>
	 * @param serializer
	 * @param object
	 * @param fieldName
	 * @param fieldType
	 * @param features
	 * @throws IOException
	 * @author java_liudong@163.com  2017年6月14日 上午9:24:59
	 */
	public void writeAsArray(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException{
		write(serializer, object, fieldName, fieldType, features);
	}
	
	/**
	 * 
	 * <p>Title: writeAsArrayNonContext</p>
	 * <p>Description: 将对象中的数组字段直接写入到缓存流中</p>
	 * @param serializer
	 * @param object
	 * @param fieldName
	 * @param fieldType
	 * @param features
	 * @throws IOException
	 * @author java_liudong@163.com  2017年6月14日 上午9:28:41
	 */
	public void writeAsArrayNonContext(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
		write(serializer, object, fieldName, fieldType, features);
	}
	
	public FieldSerializer getFieldSerializer(String key) {
		if (key == null) {
			return null;
		}
		
		int low = 0;
		int high = sortedGetters.length - 1;
		
		while (low <= high) {
			int mid = (low + high) >>> 1;
			
			String fieldName = sortedGetters[mid].fieldInfo.name;
			
			int cmp = fieldName.compareTo(key);
			
			if (cmp < 0) {
				low = mid + 1;
			} else if (cmp > 0) {
				high = mid - 1;
			} else {
				return sortedGetters[mid];
			}
		}
		
		return null;
	}
	
	
	@Override
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
		write(serializer, object, fieldName, fieldType, features, false);
	}
	
	protected void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features, boolean unwrapped) {
		SerializeWriter out = serializer.out;
		
		if (object == null) {
			out.writeNull();
			return ;
		}
		
		if (writeReference(serializer, object, features)) {
			return ;
		}
		
		final FieldSerializer[] getters;
		
		if (out.sortField) {
			getters = this.sortedGetters;
		} else {
			getters = this.getters;
		}
		
		SerialContext parent = serializer.context;
		serializer.setContext(parent, object, fieldName, this.beanInfo.features, features);
		
		final boolean writeAsArray = isWriteAsArray(serializer, features);
		
		try {
			/** JSONzui*/
			final char startSeperator = writeAsArray ? '[' : '{';
			final char endSeperator = writeAsArray ? ']' : '}';
			
			if (!unwrapped) { // 是否使用 {} 或者 [] 进行包装, 数据, 默认是false, 这里使用取反, 所以是使用的
				out.append(startSeperator);
			}
			
			if (getters.length > 0 && out.isEnable(SerializerFeature.PrettyFormat)) { // 如果有字段, 且需要格式化, 就进行格式化
				serializer.incrementIndent();
				serializer.println();
			}
			
			boolean commaFlag = false;
			
			if ((this.beanInfo.features & SerializerFeature.WriteClassName.mask) != 0 || serializer.isWriteClassName(fieldType, object)) {
				Class<?> objClass = object.getClass();
				if (objClass != fieldType) {
					writeClassName(serializer, object);
					commaFlag = true;
				}
			}
			
			char seperator = commaFlag ? ',' : '\0';
			
			final boolean directWritePrefix = out.quoteFieldNames && !out.useSingleQuotes; // 是否使用双引号 
			char newSeperator = this.writeBefore(serializer, object, seperator);
			commaFlag = newSeperator == ',';
			
			final boolean skipTransient = out.isEnable(SerializerFeature.SkipTransientField);
			final boolean ignoreNonFieldGetter = out.isEnable(SerializerFeature.IgnoreNonFieldGetter);
			
			/** 遍历所有get方法, 字段拼接成 JSON字符串 begin */
			for (int i = 0; i < getters.length; ++i) {
				FieldSerializer fieldSerializer = getters[i];
				
				Field field = fieldSerializer.fieldInfo.field; // 当前字段
				FieldInfo fieldInfo = fieldSerializer.fieldInfo; // 当前字段的详细信息
				String fieldInfoName = fieldInfo.name; // 字段名
				Class<?> fieldClass = fieldInfo.fieldClass; // 字段的对象名称
				
				if (skipTransient) { // 可序列化, 并且字段不为空, 那么就看对象上面是否有 不可序列化的注解, 有的话, 那么就不进行转换成为JSON
					if (field != null) {
						if (fieldInfo.fieldTransient) {
							continue ;
						}
					}
				}
				
				if (ignoreNonFieldGetter) { // 默认为false, 如果没有字段对象, 就忽略
					if (field == null) {
						continue ;
					}
				}
				
				if (this.applyName(serializer, object, fieldInfo.name) || !this.applyLabel(serializer, fieldInfo.label)) {
					continue ;
				}
				
				Object propertyValue; // 定义的字段的值
				try {
					propertyValue = fieldSerializer.getPropertyValueDirect(object);
				} catch (InvocationTargetException ex) {
					if (out.isEnable(SerializerFeature.IgnoreErrorGetter)) {
						propertyValue = null;
					} else {
						throw ex;
					}
				}
				
				if (!this.apply(serializer, object, fieldInfoName, propertyValue)) { // 过滤字段值
					continue ;
				}
				
				String key = fieldInfoName;
				key = this.processKey(serializer, object, key, propertyValue); // 过滤key 的值
				
				Object originaValue = propertyValue; // 原始的值, 不是通过上面处理的key值
				propertyValue = this.processValue(serializer, fieldSerializer.fieldContext, object, fieldInfoName, propertyValue); // 过滤value的值
				
				if (propertyValue == null && !writeAsArray) { // 值为空, 并且可以写入数组类型的行卡下
					if ((!fieldSerializer.writeNull) && (!out.isEnable(SerializerFeature.WRITE_MAP_NULL_FEATURES))) {
						continue ;
					}
				}
				
				if (propertyValue != null 
						&& (out.notWriteDefaultValue 
						|| (fieldInfo.serializeFeatures & SerializerFeature.NotWriteDefaultValue.mask) != 0 
						|| (beanInfo.features & SerializerFeature.NotWriteDefaultValue.mask) != 0)) {
					Class<?> fieldCLass = fieldInfo.fieldClass;
					if (fieldCLass == byte.class && propertyValue instanceof Byte && ((Byte) propertyValue).byteValue() == 0) { // byte
						continue ;
					} else if (fieldCLass == short.class && propertyValue instanceof Short && ((Short) propertyValue).shortValue() == 0) { // short
						continue ;
					} else if (fieldCLass == int.class && propertyValue instanceof Integer && ((Integer) propertyValue).intValue() == 0) { // int
						continue ;
					} else if (fieldCLass == long.class && propertyValue instanceof Long && ((Long) propertyValue).longValue() == 0L) { // long
						continue ;
					} else if (fieldCLass == float.class && propertyValue instanceof Float && ((Float) propertyValue).floatValue() == 0F) { // float
						continue ;
					} else if (fieldCLass == double.class && propertyValue instanceof Double && ((Double) propertyValue).doubleValue() == 0D) { // double
						continue ;
					} else if (fieldCLass == boolean.class && propertyValue instanceof Boolean && !((Boolean) propertyValue).booleanValue()) { // double
						continue ;
					}
				}
				
				if (commaFlag) { // 第一次为false, 第二次就为true, 在第一个字段转换JSON字符串后, commaFlag 设置为true, 
					out.write(',');
					if (out.isEnable(SerializerFeature.PrettyFormat)) {
						serializer.println();
					}
				}
				
				if (key != fieldInfoName) {
					if (!writeAsArray) {
						out.writeFieldName(key, true);
					}
					
					serializer.write(propertyValue);
				} else if (originaValue != propertyValue) {
					if (!writeAsArray) {
						fieldSerializer.writePrefix(serializer);
					}
					serializer.write(propertyValue);
				} else {
					if (!writeAsArray) {
						if (!fieldInfo.unwrapped) {
							if (directWritePrefix) {
								out.write(fieldInfo.name_chars, 0, fieldInfo.name_chars.length); // 此处是写入Key值的
							} else {
								fieldSerializer.writePrefix(serializer); // 检测字段是否 使用引号
							}
						}
					}
					
					if (!writeAsArray) {
						JSONField fieldAnnotation = fieldInfo.getAnnotation(); // 获取字段上的注解
						if (fieldClass == String.class && (fieldAnnotation == null || fieldAnnotation.serializeUsing() == Void.class)) {
							if (propertyValue == null) {
								if ((out.features &SerializerFeature.WriteNullStringAsEmpty.mask) != 0
										|| (fieldSerializer.features & SerializerFeature.WriteNullStringAsEmpty.mask) != 0) {
									out.writeString("");
								} else {
									out.writeNull();
								}
							} else { // 将值写入到缓存中
								String propertyValueString = (String) propertyValue;
								
								if (out.useSingleQuotes) { // 使用单引号
									out.writeStringWithSingleQuote(propertyValueString);
								} else { // 使用双引号
									out.writeStringWithDoubleQuote(propertyValueString, (char) 0);
								}
							}
						} else { // 这里判断的是值 对象不是String
							fieldSerializer.writeValue(serializer, propertyValue);
						}
					} else {
						fieldSerializer.writeValue(serializer, propertyValue);
					}
				}
				
				commaFlag = true;
			}
			/** 遍历所有get方法, 字段拼接成 JSON字符串 end */
			
			this.writeAfter(serializer, object, commaFlag ? ',' : '\0');
			
			if (getters.length > 0 && out.isEnable(SerializerFeature.PrettyFormat)) { // 格式化
				serializer.decrementIndent();
				serializer.println();
			}
			
			if (!unwrapped) {
				out.append(endSeperator);
			}
		} catch (Exception e) {
			String errorMessage = "write javaBean error";
			if (object != null) {
				errorMessage += ", class " + object.getClass().getName();
			}
			if (fieldName != null) {
				errorMessage += ", fieldName : " + fieldName;
			}
			if (e.getMessage() != null) {
				errorMessage += (", " + e.getMessage());
			}
			
			throw new JSONException(errorMessage, e);
		} finally {
			serializer.context = parent;
		}
	}

	/**
	 * 
	 * <p>Title: writeAfter</p>
	 * <p>Description: 解析JSON字符串 后过滤</p>
	 * @param jsonBeanDeser
	 * @param object
	 * @param seperator
	 * @return
	 * @author java_liudong@163.com  2017年6月19日 下午6:42:24
	 */
	protected char writeAfter(JSONSerializer jsonBeanDeser, Object object, char seperator) {
		if (jsonBeanDeser.afterFilters != null) {
			for (AfterFilter afterFilter : jsonBeanDeser.afterFilters) {
				seperator = afterFilter.writeAfter(jsonBeanDeser, object, seperator);
			}
		}
		
		if (this.afterFilters != null) {
			for (AfterFilter afterFilter : this.afterFilters) {
				seperator = afterFilter.writeAfter(jsonBeanDeser, object, seperator);
			}
		}
		
		return seperator;
	}
	
	/**
	 * 
	 * <p>Title: applyLabel</p>
	 * <p>Description: 使用Label过滤器</p>
	 * @param jsonBeanDeser
	 * @param label
	 * @return
	 * @author java_liudong@163.com  2017年6月19日 下午5:21:00
	 */
	protected boolean applyLabel(JSONSerializer jsonBeanDeser, String label) {
		if (jsonBeanDeser.labelFilters != null) {
			for (LabelFilter propertyFilter : jsonBeanDeser.labelFilters) {
				if (!propertyFilter.apply(label)) {
					return false;
				}
			}
		}
		
		if (this.labelFilters != null) {
			for (LabelFilter propertyFilter : this.labelFilters) {
				if (!propertyFilter.apply(label)) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * 
	 * <p>Title: writeBefore</p>
	 * <p>Description: 对分词器, 做过滤</p>
	 * @param jsonBeanDeser
	 * @param object
	 * @param seperator
	 * @return
	 * @author java_liudong@163.com  2017年6月19日 下午4:42:04
	 */
	protected char writeBefore(JSONSerializer jsonBeanDeser, Object object, char seperator) {
		if (jsonBeanDeser.beforeFilters != null) {
			for (BeforeFilter beforeFilter : jsonBeanDeser.beforeFilters) {
				seperator = beforeFilter.writeBefore(jsonBeanDeser, object, seperator);
			}
		}
		
		if (this.beforeFilters != null) {
			for (BeforeFilter beforeFilter : this.beforeFilters) {
				seperator = beforeFilter.writeBefore(jsonBeanDeser, object, seperator);
			} 
		}
		
		return seperator;
	}
	
	/**
	 * 
	 * <p>Title: writeClassName</p>
	 * <p>Description: 写入对象的名称, 类似 "@type":"java.lang.Object"</p>
	 * @param serializer
	 * @param object
	 * @author java_liudong@163.com  2017年6月19日 下午4:14:09
	 */
	public void writeClassName(JSONSerializer serializer, Object object) {
		serializer.out.writeFieldName(serializer.config.typeKey, false);
		String typeName = this.beanInfo.typeName;
		if (typeName == null) {
			Class<?> clazz = object.getClass();
			
			if (TypeUtils.isProxy(clazz)) {
				clazz = clazz.getSuperclass();
			}
			
			typeName = clazz.getName();
		}
		serializer.write(typeName);
	}
	
	/**
	 * 
	 * <p>Title: writeReference</p>
	 * <p>Description: 写入引用的方法</p>
	 * @param serializer
	 * @param object
	 * @param fieldFeatures
	 * @return
	 * @author java_liudong@163.com  2017年6月14日 上午9:38:42
	 */
	public boolean writeReference(JSONSerializer serializer, Object object, int fieldFeatures) {
		SerialContext context = serializer.context;
		int mask = SerializerFeature.DisableCircularReferenceDetect.mask;
		if (context == null || (context.features & mask) != 0 || (fieldFeatures & mask) != 0) {
			return false;
		}
		
		if (serializer.references != null && serializer.references.containsKey(object)) {
			serializer.writeReference(object);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 
	 * <p>Title: isWriteAsArray</p>
	 * <p>Description: 检测写入数组,是否使用[]</p>
	 * @param serializer
	 * @return
	 * @author java_liudong@163.com  2017年6月14日 下午2:11:16
	 */
	protected boolean isWriteAsArray(JSONSerializer serializer) {
		return isWriteAsArray(serializer, 0);
	}
	
	/**
	 * 
	 * <p>Title: isWriteAsArray</p>
	 * <p>Description: 是否写成数组, 默认为false</p>
	 * @param serializer
	 * @param fieldFeatures
	 * @return
	 * @author java_liudong@163.com  2017年6月19日 下午2:09:16
	 */
	protected boolean isWriteAsArray(JSONSerializer serializer, int fieldFeatures) {
		final int mask = SerializerFeature.BeanToArray.mask;
		return (beanInfo.features & mask) != 0 || serializer.out.beanToArray || (fieldFeatures & mask) != 0;
	}

}
