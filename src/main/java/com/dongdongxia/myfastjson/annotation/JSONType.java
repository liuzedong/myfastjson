package com.dongdongxia.myfastjson.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.dongdongxia.myfastjson.parser.Feature;
import com.dongdongxia.myfastjson.serializer.SerializerFeature;

/**
 * 
 * <P>Description: JSON类型注解, 用于对象上面</P>
 * @ClassName: JSONType
 * @author java_liudong@163.com  2017年6月5日 上午10:21:21
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface JSONType {
	
	boolean asm() default true;
	
	String[] orders() default {};
	
	String[] includes() default {};
	
	String[] ignores() default {};
	
	SerializerFeature[] serializeFeatures() default {};
	
	Feature[] parseFeatures() default {};
	
	boolean alphabetic() default true; // alphabetic : 字母, 按照字母进行排序
	
	Class<?> mappingTo() default Void.class;
	
	Class<?> builder() default Void.class;
	
	String typeName() default "";
	
	Class<?>[] seeAlso() default {}; // 参考其他的
	
	Class<?> serializer() default Void.class;
	
	Class<?> deserializer() default Void.class;
	
	boolean serializeEnumAsJavaBean() default false;
}
