package com.dongdongxia.myfastjson.util;

import java.lang.reflect.AccessibleObject;
import java.security.AccessControlException;

/**
 * 
 * <P>Description: 类型工具类</P>
 * @ClassName: TypeUtils
 * @author java_liudong@163.com  2017年5月23日 下午5:53:56
 */
public class TypeUtils {

	private static boolean setAccessibleEnable = true;
	
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
	
}
