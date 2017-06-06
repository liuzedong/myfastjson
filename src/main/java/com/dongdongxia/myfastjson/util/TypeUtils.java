package com.dongdongxia.myfastjson.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.dongdongxia.myfastjson.PropertyNamingStrategy;
import com.dongdongxia.myfastjson.annotation.JSONField;
import com.dongdongxia.myfastjson.annotation.JSONType;
import com.dongdongxia.myfastjson.parser.Feature;
import com.dongdongxia.myfastjson.serializer.SerializerFeature;

/**
 * 
 * <P>Description: 类型工具类</P>
 * @ClassName: TypeUtils
 * @author java_liudong@163.com  2017年5月23日 下午5:53:56
 */
public class TypeUtils {

	private static boolean setAccessibleEnable = true;
	
	private static boolean transientClassInited = false;
	private static Class<? extends Annotation> transientClass;
	
	/** 根据field name 的大小写输出输入数据*/
	public static boolean compatibleWithFieldName = false;
	
	static {
		try {
			TypeUtils.compatibleWithFieldName = "true".equals(IOUtils.getStringProperty(IOUtils.MYFASTJSON_COMPATIBLEWITHFIELDBEAN));
		} catch (Throwable e) {
			// skip
		}
	}
	
	/**
	 * 
	 * <p>Title: setAccessible</p>
	 * <p>Description: 将对象中的私有字段, 改为可访问类型的</p>
	 * @param obj
	 * @author java_liudong@163.com  2017年5月23日 下午5:56:57
	 */
	static void setAccessible(AccessibleObject obj) {
		if (!setAccessibleEnable) {
			return ;
		}
		
		// 检测 类型是否是可访问的  public
		if (obj.isAccessible()) {
			return ;
		}
		
		try {
			obj.setAccessible(true);
		} catch (AccessControlException error) {
			setAccessibleEnable = false;
		}
	}
	
	/**
	 * 
	 * <p>Title: isTransient</p>
	 * <p>Description: TODO</p>
	 * @param method
	 * @return
	 * @author java_liudong@163.com  2017年5月23日 下午6:17:27
	 */
	@SuppressWarnings("unchecked")
	public static boolean isTransient(Method method) {
		if (method == null) {
			return false;
		}
		
		if (!transientClassInited) {
			try {
				transientClass = (Class<? extends Annotation>) Class.forName("java.beans.Transient"); // 检测这个枚举
			} catch (Exception e) {
				// skip
			} finally {
				transientClassInited = true;
			}
		}
		
		if (transientClass != null) {
			Annotation annotation = method.getAnnotation(transientClass);
			return annotation != null;
		}
		return false;
	}
	
	/**
	 * 
	 * <p>Title: getClass</p>
	 * <p>Description: 获取类型的反射对象</p>
	 * @param type
	 * @return
	 * @author java_liudong@163.com  2017年5月23日 下午6:48:54
	 */
	public static Class<?> getClass(Type type) {
		if (type.getClass() == Class.class) {
			return (Class<?>) type;
		}
		
		if (type instanceof ParameterizedType) {
			return getClass(((ParameterizedType) type).getRawType());
		}
		
		if (type instanceof TypeVariable) {
			Type boundType = ((TypeVariable<?>) type).getBounds()[0];
			return (Class<?>) boundType;
		}
		
		return Object.class;
	}
	
	/**
	 * 
	 * <p>Title: isGenericParamType</p>
	 * <p>Description: 是否 有父类, Object 不算</p>
	 * @param type
	 * @return
	 * @author java_liudong@163.com  2017年5月24日 上午10:40:02
	 */
	public static boolean isGenericParamType(Type type) {
		if (type instanceof ParameterizedType) {
			return true;
		}
		
		if (type instanceof Class) {
			Type superType = ((Class<?>) type).getGenericSuperclass();
			if (superType == Object.class) {
				return false;
			}
			return isGenericParamType(superType);
		}
		
		return false;
	}
	
	/**
	 * 
	 * <p>Title: getGenericParamType</p>
	 * <p>Description: 获取父类</p>
	 * @param type
	 * @return
	 * @author java_liudong@163.com  2017年5月24日 上午10:46:50
	 */
	public static Type getGenericParamType(Type type) {
		if (type instanceof ParameterizedType) {
			return type;
		}
		
		if (type instanceof Class) {
			return getGenericParamType(((Class<?>) type).getGenericSuperclass());
		}
		
		return type;
	}
	
	/**
	 * 
	 * <p>Title: getSuperMethodAnnotation</p>
	 * <p>Description: 检测是否父类接口字段上有这个注解</p>
	 * @param clazz
	 * @param method
	 * @return
	 * @author java_liudong@163.com  2017年6月5日 下午5:06:31
	 */
	public static JSONField getSuperMethodAnnotation(final Class<?> clazz, final Method method) {
		/** ---------------------------检测接口的方法上面是否有枚举 begin--------------------------------------*/
		Class<?>[] interfaces = clazz.getInterfaces(); // 校验接口上面的方法,是否有注解
		if (interfaces.length > 0) {
			Class<?>[] types = method.getParameterTypes(); // 获取方法的所有入参类型
			for (Class<?> interfaceClass : interfaces) {
				for (Method interfaceMethod : interfaceClass.getMethods()) { // 获取接口的所有方法
					Class<?>[] interfaceTypes = interfaceMethod.getParameterTypes(); 
					// 两个方法的参数,和方法名称不一样, 就不是一个方法
					if (interfaceTypes.length != types.length) {
						continue ;
					}
					if (!interfaceMethod.getName().equals(method.getName())) { 
						continue ;
					}
					
					boolean match = true;
					for (int i = 0; i < types.length; ++i) {
						if (!interfaceTypes[i].equals(types[i])) { // 方法参数类型是否一直
							match = false;
							break ;
						}
					}
					
					if (!match) {
						continue ;
					}
					
					JSONField annotation = interfaceMethod.getAnnotation(JSONField.class);
					if (annotation != null) {
						return annotation;
					}
				}
			}
		}
		/** ---------------------------检测接口的方法上面是否有枚举 end--------------------------------------*/

		
		/** ---------------------------检测抽象类的方法上面是否有枚举 begin--------------------------------------*/
		// 下面是检测 抽象父类上面的字段是否有注解
		Class<?> superClass = clazz.getSuperclass();
		if (superClass == null) {
			return null;
		}
		
		if (Modifier.isAbstract(superClass.getModifiers())) { // 检测是否是抽象类
			Class<?>[] types = method.getParameterTypes();
			
			for (Method interfaceMethod : superClass.getMethods()) {
				Class<?>[] interfaceTypes = interfaceMethod.getParameterTypes();
				if (interfaceTypes.length != types.length) {
					continue ;
				}
				
				if (!interfaceMethod.getName().equals(method.getName())) {
					continue ;
				}
				
				boolean match = true;
				for (int i = 0; i < types.length; ++i) {
					if (!interfaceTypes[i].equals(types[i])) { // 参数类型比较
						match = false;
						break;
					}
				}
				
				if (!match) {
					continue ;
				}
				
				JSONField annotation = interfaceMethod.getAnnotation(JSONField.class);
				if (annotation != null) {
					return annotation;
				}
			}
		}
		/** ---------------------------检测抽象类的方法上面是否有枚举 end--------------------------------------*/

		return null;
	}
	
	/**
	 * 
	 * <p>Title: isJSONTypeIgnore</p>
	 * <p>Description: 检测对象上面有注解, 忽略对象中的字段</p>
	 * @param clazz 检测的对象
	 * @param propertyName 对象中的属性
	 * @return 
	 * @author java_liudong@163.com  2017年6月5日 下午6:20:12
	 */
	private static boolean isJSONTypeIgnore(Class<?> clazz, String propertyName) {
		JSONType jsonType = clazz.getAnnotation(JSONType.class);
		
		if (jsonType != null) {
			/**
			 * 1, 新增 includes 支持, 如果JSONType 同时设置了includes 和 ignores 属性, 则以includes为准
			 * 2, 个人认为对于大小写敏感的Java和JS而言, 使用equals() 比 equalsIgnoreClase()更好, 改动的唯一风险就是向后兼容的问题
			 * 不过, 相信开发者应该严格按照大小写敏感的方式进行属性设置的
			 */
			String[] fields = jsonType.includes();
			if (fields.length > 0) {
				for (int i = 0; i < fields.length; i++) {
					if (propertyName.equals(fields[i])) {
						return false;
					}
				}
				return true;
			} else {
				fields = jsonType.ignores();
				for (int i = 0; i < fields.length; i++) {
					if (propertyName.equals(fields[i])) {
						return true;
					}
				}
			}
		}
		
		if (clazz.getSuperclass() != Object.class && clazz.getSuperclass() != null) { // 拥有父类,进行 迭代
			if (isJSONTypeIgnore(clazz.getSuperclass(), propertyName)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 
	 * <p>Title: getPropertyNameByCompatibleFieldName</p>
	 * <p>Description: 获取兼容字段名称</p>
	 * @param fieldCacheMap 字段名和字段属性的 缓存
	 * @param methodName 方法名
	 * @param propertyName 字段名
	 * @param fromIdx 截取的位置
	 * @return
	 * @author java_liudong@163.com  2017年6月6日 上午9:31:55
	 */
	private static String getPropertyNameByCompatibleFieldName(Map<String, Field> fieldCacheMap, String methodName, String propertyName, int fromIdx) {
		if (compatibleWithFieldName) {
			if (!fieldCacheMap.containsKey(propertyName)) { // 查找不到,进入
				String tempPropertyName = methodName.substring(fromIdx);
				return fieldCacheMap.containsKey(tempPropertyName) ? tempPropertyName : propertyName;
			}
		}
		
		return propertyName;
	}
	
	/**
	 * 
	 * <p>Title: decapotalize</p>
	 * <p>Description: 将首字母小写</p>
	 * @param name 方法名称
	 * @return
	 * @author java_liudong@163.com  2017年6月6日 上午9:46:06
	 */
	public static String decapotalize(String name) {
		if (name == null || name.length() == 0) {
			return name;
		}
		
		if (name.length() > 1 && Character.isUpperCase(name.charAt(1))/*是否大写*/ && Character.isUpperCase(name.charAt(0)) /*是否大写*/) {
			return name;
		}
		
		char chars[] = name.toCharArray();
		chars[0] = Character.toLowerCase(chars[0]);
		return new String(chars);
	}
	
	/**
	 * 
	 * <p>Title: computeFields</p>
	 * <p>Description: 计算字段名上的注解</p>
	 * @param clazz 类名
	 * @param aliasMap 别名MAP
	 * @param propertyNamingStrategy 命名枚举
	 * @param fieldInfoMap 字段名称的缓存
	 * @param fields 字段数组
	 * @author java_liudong@163.com  2017年6月6日 上午10:05:04
	 */
	private static void computeFields(Class<?> clazz, 
			Map<String, String> aliasMap,
			PropertyNamingStrategy propertyNamingStrategy,
			Map<String, FieldInfo> fieldInfoMap,
			Field[] fields) {
		for (Field field : fields) {
			if (Modifier.isStatic(field.getModifiers())) {
				continue ;
			}
			
			JSONField fieldAnnotation = field.getAnnotation(JSONField.class);
			
			int ordinal = 0, serializeFeatures = 0, parserFeatures = 0;
			String propertyName = field.getName();
			String label = null;
			if (fieldAnnotation != null) {
				if (!fieldAnnotation.serialize()) {
					continue ;
				}
				
				ordinal = fieldAnnotation.ordinal();
				serializeFeatures = SerializerFeature.of(fieldAnnotation.serialzeFeatures());
				parserFeatures = Feature.of(fieldAnnotation.parseFeatures());
				
				if (fieldAnnotation.name().length() != 0) {
					propertyName = fieldAnnotation.name();
				}
				
				if (fieldAnnotation.label().length() != 0) {
					label = fieldAnnotation.label();
				}
			}
			
			if (aliasMap != null) {
				propertyName = aliasMap.get(propertyName);
				if (propertyName == null) {
					continue ;
				}
			}
			
			if (propertyNamingStrategy != null) {
				propertyName = propertyNamingStrategy.translate(propertyName);
			}
			
			if (!fieldInfoMap.containsKey(propertyName)) {
				FieldInfo fieldInfo = new FieldInfo(propertyName, null, field, clazz, null, ordinal, serializeFeatures, parserFeatures, null, fieldAnnotation, label);
				fieldInfoMap.put(propertyName, fieldInfo);
			}
		}
	}
	
	
	/**
	 * 
	 * <p>Title: getFieldInfos</p>
	 * <p>Description: 把缓存中的值,转换成为List, 并检测是否排序</p>
	 * @param clazz 类名
	 * @param sorted 是否排序
	 * @param fieldInfoMap 字段缓存
	 * @return
	 * @author java_liudong@163.com  2017年6月6日 上午10:54:09
	 */
	private static List<FieldInfo> getFieldInfos(Class<?> clazz, boolean sorted, Map<String, FieldInfo> fieldInfoMap) {
		List<FieldInfo> fieldInfoList = new ArrayList<FieldInfo>();
		
		boolean containsAll = false;
		String[] orders = null;
		
		JSONType annotation = clazz.getAnnotation(JSONType.class);
		if (annotation != null) {
			orders = annotation.orders();
			
			if (orders != null && orders.length == fieldInfoMap.size()) {
				containsAll = true;
				for (String item : orders) {
					if (!fieldInfoMap.containsKey(item)) {
						containsAll = false;
						break ;
					}
				}
			} else {
				containsAll = false;
			}
		}
		
		
		if (containsAll) {
			for (FieldInfo fieldInfo : fieldInfoMap.values()) {
				fieldInfoList.add(fieldInfo);
			}
			
			if (sorted) {
				Collections.sort(fieldInfoList);
			}
		}
		return fieldInfoList;
	}
}
