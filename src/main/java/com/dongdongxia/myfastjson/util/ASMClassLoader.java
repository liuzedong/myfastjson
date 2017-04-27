package com.dongdongxia.myfastjson.util;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

import com.dongdongxia.myfastjson.JSON;

/**
 * 
 * <P>Description: 自定义类加载器, 字节码的</P>
 * @ClassName: ASMClassLoader
 * @author java_liudong@163.com  2017年4月27日 下午4:16:18
 */
public class ASMClassLoader extends ClassLoader{

	private static ProtectionDomain DOMAIN;
	
	private static Map<String, Class<?>> classMapping = new HashMap<String, Class<?>>();
	
	static {
		DOMAIN = (ProtectionDomain) AccessController.doPrivileged(new PrivilegedAction<Object>() {
			@Override
			public Object run() {
				return ASMClassLoader.class.getProtectionDomain();
			}
		});
		
		Class<?> jsonClasses[] = new Class<?>[] {
				
		};
		
		// 初始化 需要的类
		for (Class<?> clazz : jsonClasses){
			classMapping.put(clazz.getName(), clazz);
		}
	}
	
	public ASMClassLoader(){
		super(getParentClassLoader());
	}
	
	public ASMClassLoader(ClassLoader parent){
		super(parent);
	}
	
	/**
	 * 
	 * <p>Title: getParentClassLoader</p>
	 * <p>Description: 获取父类, 就是获取JSON.class</p>
	 * @return
	 * @author java_liudong@163.com  2017年4月27日 下午4:24:16
	 */
	static ClassLoader getParentClassLoader(){
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		if (contextClassLoader != null){
			try {
				contextClassLoader.loadClass(JSON.class.getName());
				return contextClassLoader;
			} catch (ClassNotFoundException e){
				// skip
			}
		}
		return JSON.class.getClassLoader();
	}
	
	/**
	 * 获取Map中指定的对象
	 * @throws ClassNotFoundException 
	 */
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class<?> mappingClass = classMapping.get(name);
		if (mappingClass != null){
			return mappingClass;
		}
		try {
			return super.loadClass(name, resolve);
		} catch (ClassNotFoundException e){
			throw e;
		}
	}
	
	public Class<?> defineClassPublic(String name, byte[] b, int off, int len) throws ClassFormatError{
		Class<?> clazz = defineClass(name, b, off, len, DOMAIN);
		return clazz;
	}
	
	public boolean isExternal(Class<?> clazz){
		ClassLoader classLoader = clazz.getClassLoader();
		if (classLoader == null){
			return false;
		}
		
		ClassLoader current = this;
		while (current != null){
			if (current == classLoader){
				return false;
			}
			current = current.getParent();
		}
		return true;
	}
	
}
