package com.dongdongxia.myfastjson.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import com.dongdongxia.myfastjson.annotation.JSONField;

/**
 * 
 * <P>Description: 类的信息, 用来状态一个类的反射中的所有信息, 且具有比较性</P>
 * @ClassName: FieldInfo
 * @author java_liudong@163.com  2017年5月23日 下午4:58:03
 */
public class FieldInfo implements Comparable<FieldInfo>{

	/** 下面定义的所有字段, 都在构造方法中,进行初始化*/
	
	public final String name; // 类名
	public final Method method; // 类的方法
	public final Field field; // 类的字段
	
	private int ordinal = 0; // 排序
	public final Class<?> fieldClass; // 字段的类
	public final Type fieldType; // 字段的类型
	public final Class<?> declaringClass; // 生命的类
	public final boolean getOnly;
	public final int serializeFeatures; // 序列化的所有特性
	public final int parserFeatures; // 反序列化的所有特性, 就是将对象解析成为JSON字符串, 所有的特性
	public final String label; // 标签
	
	private final JSONField fieldAnnotation; // 字段上的注解
	private final JSONField methodAnnotation; // 字段上的注解, 是否可 被序列化
	
	public final boolean fieldAccess; // 字段访问权限, 是否是public的
	public final boolean fieldTransient; // 字段是否可 被序列化, 就是看 , 是否有transient 关键字
	
	public final char[] name_chars; // 所有的字段信息
	
	public final boolean isEnum; // 是否是枚举
	public final boolean jsonDirect; // 
	public final boolean unwrapped; // unwrapped : 打开
	
	public final String format; // 格式化
	
	private final String[] alternateNames; // alternate : 备用, 替换
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: 构造方法, 初始化上面的参数</p>
	 * @param name 类名
	 * @param declaringCLass 全类名
	 * @param fieldClass 字段类
	 * @param fieldType 字段类型
	 * @param field 字段
	 * @param ordinal 排序
	 * @param serializeFeatures 序列化功能集
	 * @param parserFeatures 反序列化功能集
	 */
	public FieldInfo(String name, Class<?> declaringClass, Class<?> fieldClass, Type fieldType, Field field, int ordinal, int serializeFeatures, int parserFeatures) {
		this.name = name;
		this.declaringClass = declaringClass;
		this.fieldClass = fieldClass;
		this.fieldType = fieldType;
		this.method = null;
		this.field = field;
		this.ordinal = ordinal;
		this.serializeFeatures = serializeFeatures;
		this.parserFeatures = parserFeatures;
		
		isEnum = fieldClass.isEnum(); // 字段类型是否是枚举
		
		if (field != null) {
			int modifiers = field.getModifiers(); // 这个字段的 修饰符
			fieldAccess = (modifiers & Modifier.PUBLIC) != 0 || method == null; // 这里应该就是 true啦, 上面都定义method = true啦
			fieldTransient = Modifier.isTransient(modifiers); 
		} else {
			fieldTransient = false;
			fieldAccess = false;
		}
		
		name_chars = getFieldNameChars(); // 获取字段的 "":  的这种包装形式
		
		if (field != null) {
			TypeUtils.setAccessible(field); // 将这个字段,变成可以访问的 public 
		}
		
		this.label = "";
		fieldAnnotation = null;
		methodAnnotation = null;
		this.getOnly = false;
		this.jsonDirect = false;
		this.unwrapped = false;
		this.format = null;
		this.alternateNames = new String[0];
	}
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: Method for constructor</p>
	 * @param name
	 * @param method
	 * @param field
	 * @param clazz
	 * @param type
	 * @param ordinal
	 * @param serializeFeatures
	 * @param parserFeatures
	 * @param fieldAnnotation
	 * @param methodAnnotation
	 * @param label
	 */
	public FieldInfo(String name, Method method, Field field, Class<?> clazz, Type type, int ordinal,
			int serializeFeatures, int parserFeatures, JSONField fieldAnnotation, JSONField methodAnnotation, String label) {
		if (field != null) {
			String fieldName = field.getName();
			if (fieldName.equals(name)) {
				name = fieldName;
			}
		}
		
		this.name = name;
		this.method = method;
		this.field = field;
		this.ordinal = ordinal;
		this.serializeFeatures = serializeFeatures;
		this.parserFeatures = parserFeatures;
		this.fieldAnnotation = fieldAnnotation;
		this.methodAnnotation = methodAnnotation;
		
		if (field != null) {
			int modifiers = field.getModifiers(); // 获取字段的访问权限, public private , protected 这种的
			fieldAccess = ((modifiers & Modifier.PUBLIC) != 0 || method == null);
			fieldTransient = Modifier.isTransient(modifiers) || TypeUtils.isTransient(method);
		} else {
			fieldAccess = false;
			fieldTransient = false;
		}
		
		if (label != null && label.length() > 0) {
			this.label = label;
		} else {
			this.label = "";
		}
		
		String format = null;
		JSONField annotation = getAnnotation(); // 获取, 不为空的 注解
		
		boolean jsonDirect = false;
		if (annotation != null) {
			format = annotation.format();
			
			if (format.trim().length() == 0) {
				format = null;
			}
			
			jsonDirect = annotation.jsonDirect();
			unwrapped = annotation.unwrappend();
			alternateNames = annotation.alternateNames();
		} else {
			jsonDirect = false;
			unwrapped = false;
			alternateNames = new String[0];
		}
		this.format = format;
		
		name_chars = getFieldNameChars();
		
		if (field != null) {
			TypeUtils.setAccessible(field);
		}
		
		boolean getOnly = false;
		Type fieldType;
		Class<?> fieldClass;
		if (method != null) { // 方法不为空的情况
			Class<?>[] types;
			if ((types = method.getParameterTypes()).length == 1) { // 获取方法的入参, 如果是一个, 就是正确的
				fieldClass = types[0]; // 获取方法的类型
				fieldType = method.getGenericParameterTypes()[0];
			} else {
				fieldClass = method.getReturnType();
				fieldType = method.getGenericReturnType();
				getOnly = true;
			}
			this.declaringClass = method.getDeclaringClass();
		} else { // 方法为空的情况
			fieldClass = field.getType();
			fieldType = field.getGenericType();
			this.declaringClass = field.getDeclaringClass();
			getOnly = Modifier.isFinal(field.getModifiers()); // 检测, 是否是final 的
		}
		this.getOnly = getOnly;
		this.jsonDirect = jsonDirect && fieldClass == String.class;
		
		if (clazz != null && fieldClass == Object.class && fieldType instanceof TypeVariable) {
			TypeVariable<?> tv = (TypeVariable<?>) fieldType;
			Type genericFieldType = getInheritGenericType(clazz, tv);
			if (genericFieldType != null) {
				this.fieldClass = TypeUtils.getClass(genericFieldType);
				this.fieldType = genericFieldType;
				
				isEnum = fieldClass.isEnum();
				return ;
			}
		}
		
		Type genericFieldType = fieldType;
		
		if (!(fieldType instanceof Class)) {
			genericFieldType = getFieldType(clazz, type != null ? type : clazz, fieldType); // 获取指定类的字段类型
			
			if (genericFieldType != fieldType) {
				if (genericFieldType instanceof ParameterizedType) {
					fieldClass = TypeUtils.getClass(genericFieldType);
				}
			}
		}
		
		this.fieldType = genericFieldType;
		this.fieldClass = fieldClass;
		
		isEnum = fieldClass.isEnum();
	}
	
	/**
	 * 
	 * <p>Title: getFieldType</p>
	 * <p>Description: TODO</p>
	 * @param clazz
	 * @param type
	 * @param fieldType
	 * @return
	 * @author java_liudong@163.com  2017年5月24日 上午10:32:05
	 */
	public static Type getFieldType(final Class<?> clazz, final Type type, Type fieldType) {
		if (clazz == null || type == null) {
			return fieldType;
		}
		
		if (fieldType instanceof GenericArrayType) {
			GenericArrayType genericArrayType = (GenericArrayType) fieldType;
			Type componentType = genericArrayType.getGenericComponentType();
			Type componentTypeX = getFieldType(clazz, type, componentType);
			if (componentType != componentTypeX) {
				Type fieldTypeX = Array.newInstance(TypeUtils.getClass(componentTypeX), 0).getClass();
				return fieldTypeX;
			}
		}
		
		// 如果没有父类, 就返回这个对象
		if (!TypeUtils.isGenericParamType(type)) {
			return fieldType;
		}
		
		if (fieldType instanceof TypeVariable) {
			ParameterizedType paramType = (ParameterizedType) TypeUtils.getGenericParamType(type);
			Class<?> parameterizedClass = TypeUtils.getClass(paramType);
			final TypeVariable<?> typeVar = (TypeVariable<?>) fieldType;
			
			TypeVariable<?>[] typeVariables = parameterizedClass.getTypeParameters();
			for (int i = 0; i < typeVariables.length; ++i) {
				if (typeVariables[i].getName().equals(typeVar.getName())) {
					fieldType = paramType.getActualTypeArguments()[i];
					return fieldType;
				}
			}
		}
		
		if (fieldType instanceof ParameterizedType) {
			ParameterizedType parameterizedFieldType = (ParameterizedType) fieldType;
			
			Type[] arguments = parameterizedFieldType.getActualTypeArguments();
			boolean changed = false;
			TypeVariable<?>[] typeVariables = null;
			Type[] actualTypes = null;
			
			ParameterizedType paramType = null;
			if (type instanceof ParameterizedType) {
				paramType = (ParameterizedType) type;
				typeVariables = clazz.getTypeParameters();
			} else if (clazz.getGenericSuperclass() instanceof ParameterizedType) {
				paramType = (ParameterizedType) clazz.getGenericSuperclass();
				typeVariables = clazz.getSuperclass().getTypeParameters();
			}
			
			for (int i = 0; i < arguments.length && paramType != null; ++i) {
				Type fieldTypeArguement = arguments[i];
				if (fieldTypeArguement instanceof TypeVariable) {
					TypeVariable<?> typeVar = (TypeVariable<?>) fieldTypeArguement;
					
					for (int j = 0; j < typeVariables.length; ++j) {
						if (typeVariables[j].getName().equals(typeVar.getName())) {
							if (actualTypes == null) {
								actualTypes = paramType.getActualTypeArguments();
							}
							if (arguments[i] != actualTypes[j]) {
								arguments[i] = actualTypes[j];
								changed = true;
							}
						}
					}
				}
			}
			if (changed) {
				fieldType = new ParameterizedTypeImpl(arguments, parameterizedFieldType.getOwnerType(), parameterizedFieldType.getRawType());
				return fieldType;
			}
		}
		return fieldType;
	}
	
	/**
	 * 
	 * <p>Title: getInheritGenericType</p>
	 * <p>Description: 获取继承的类</p>
	 * @param clazz
	 * @param tv
	 * @return
	 * @author java_liudong@163.com  2017年5月23日 下午6:41:10
	 */
	public static Type getInheritGenericType(Class<?> clazz, TypeVariable<?> tv) {
		Type type = null;
		GenericDeclaration gd = tv.getGenericDeclaration();
		
		do {
			type = clazz.getGenericSuperclass(); // 获取父类型
			if (type == null) {
				return null;
			}
			if (type instanceof ParameterizedType) { // 入参类型
				ParameterizedType ptype = (ParameterizedType) type;
				
				Type rawType = ptype.getRawType(); // 返回 Type 对象，表示声明此类型的类或接口。
				boolean eq = gd.equals(rawType) || (gd instanceof Class && rawType instanceof Class && ((Class)gd).isAssignableFrom((Class) rawType));
				if (eq) {
					TypeVariable<?>[] tvs = gd.getTypeParameters();
					Type[] types = ptype.getActualTypeArguments();
					for (int i = 0; i < tvs.length; i++) {
						if (tv.equals(tvs[i])) {
							return types[i];
						}
					}
					return null;
				}
			}
			clazz = TypeUtils.getClass(type);
		} while (type != null);
		return null;
	}
	
	/**
	 * 
	 * <p>Title: getFieldNameChars</p>
	 * <p>Description: 获取name的  JSON的String 类型格式</p>
	 * @return
	 * @author java_liudong@163.com  2017年5月23日 下午5:52:12
	 */
	protected char[] getFieldNameChars() {
		int nameLen = this.name.length();
		char[] name_chars = new char[nameLen + 3];
		this.name.getChars(0, nameLen, name_chars, 1); // 从第一个位置开始,  因为第一个位置, 用来装 "
		name_chars[0] = '"';
		name_chars[nameLen + 1] = '"';
		name_chars[nameLen + 2] = ':';
		return name_chars;
	}
	
	/**
	 * 
	 * <p>Title: getAnnotation</p>
	 * <p>Description: 检测, 两个注解, 只要有其中一个, 就返回一个</p>
	 * @return
	 * @author java_liudong@163.com  2017年5月23日 下午6:26:31
	 */
	public JSONField getAnnotation() {
		if (this.fieldAnnotation != null) {
			return this.fieldAnnotation;
		}
		
		return this.methodAnnotation;
	}
	
	/**
	 * 
	 * <p>Title: getAnnotation</p>
	 * <p>Description: 获取类上的注解</p>
	 * @param annotationClass
	 * @return
	 * @author java_liudong@163.com  2017年5月25日 上午9:54:48
	 */
	@SuppressWarnings("unchecked")
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		if (annotationClass == JSONField.class) {
			return (T) getAnnotation();
		}
		
		T annotation = null;
		if (method != null) {
			annotation = method.getAnnotation(annotationClass); // 如果方法不为空的话,那么就返回 方法上面的注解对象
		}
		
		if (annotation == null && field != null) {
			annotation = field.getAnnotation(annotationClass); // 如果方法上面没有的话, 这个对象也不是 注解, 那么就确认下, 这个是字段上面的注解对象
		}
		
		return annotation;
	}
	
	@Override
	public int compareTo(FieldInfo o) {
		return 0;
	}

	/**
	 * 
	 * <p>Title: setAccessible</p>
	 * <p>Description: 将方法中的字段, 设置为可访问的状态</p>
	 * @throws SecurityException
	 * @author java_liudong@163.com  2017年6月9日 下午4:01:15
	 */
	public void setAccessible() throws SecurityException{
		if (method != null) {
			TypeUtils.setAccessible(method);
			return ;
		}
		
		TypeUtils.setAccessible(field);
	}
}
