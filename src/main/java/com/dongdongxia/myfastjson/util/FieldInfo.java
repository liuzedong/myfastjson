package com.dongdongxia.myfastjson.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

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
	
	
	@Override
	public int compareTo(FieldInfo o) {
		return 0;
	}

}
