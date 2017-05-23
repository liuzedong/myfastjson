package com.dongdongxia.myfastjson.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
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
}
