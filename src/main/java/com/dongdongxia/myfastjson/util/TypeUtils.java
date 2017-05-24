package com.dongdongxia.myfastjson.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.security.AccessControlException;

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
}
