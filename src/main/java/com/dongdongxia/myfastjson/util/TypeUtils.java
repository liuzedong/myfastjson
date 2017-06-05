package com.dongdongxia.myfastjson.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.security.AccessControlException;

import com.dongdongxia.myfastjson.annotation.JSONField;

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
}
