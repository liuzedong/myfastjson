package com.dongdongxia.myfastjson.serializer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.dongdongxia.myfastjson.util.FieldInfo;

/**
 * 
 * <P>Description: Bean 的类信息</P>
 * @ClassName: BeanContext
 * @author java_liudong@163.com  2017年5月25日 上午9:45:27
 */
public class BeanContext {

	private final Class<?> beanClass;
	private final FieldInfo fieldInfo;
	private final String format;
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: 初始化Bean 的信息</p>
	 * @param beanClass
	 * @param fieldInfo
	 */
	public BeanContext(Class<?> beanClass, FieldInfo fieldInfo) {
		this.beanClass = beanClass;
		this.fieldInfo = fieldInfo;
		this.format = fieldInfo.format;
	}
	
	/** 下面的方法, 大部分,都是获取fieldInfo中的信息,  方便 再次调用fieldInfo对象 */
	
	public Class<?> getBeanClass() {
		return beanClass;
	}
	
	public Method getMethod() {
		return fieldInfo.method;
	}
	
	public Field getField() {
		return fieldInfo.field;
	}
	
	public String getName() {
		return fieldInfo.name;
	}
	
	public String getLabel() {
		return fieldInfo.label;
	}
	
	public Class<?> getFieldClass() {
		return fieldInfo.fieldClass;
	}
	
	public Type getFieldType() {
		return fieldInfo.fieldType;
	}
	
	public int getFeatures() {
		return fieldInfo.serializeFeatures;
	}
	
	public boolean isJsonDircet() {
		return this.fieldInfo.jsonDirect;
	}
	
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return fieldInfo.getAnnotation(annotationClass);
	}
	
	public String getFormat() {
		return format;
	}
}
