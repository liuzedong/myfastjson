package com.dongdongxia.myfastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

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
		
	}

}
