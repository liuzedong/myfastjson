package com.dongdongxia.myfastjson.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.dongdongxia.myfastjson.parser.Feature;
import com.dongdongxia.myfastjson.serializer.SerializerFeature;

/**
 * 
 * <P>Description: JSON的字段枚举</P>
 * @ClassName: JSONField
 * @author java_liudong@163.com  2017年5月22日 下午1:48:30
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER}) // 在 方法, 字段, 入参  等上面可以使用
public @interface JSONField {
	
	int ordinal() default 0;
	
	String name() default "";
	
	String format() default "";
	
	boolean serialize() default true;
	
	boolean deserialize() default true;
	
	SerializerFeature[] serialzeFeatures() default {};
	
	Feature[] parseFeatures() default {};
	
	String label() default "";
	
	boolean jsonDirect() default false;
	
	Class<?> serializeUsing() default Void.class;
	
	Class<?> deserializeUsing() default Void.class;
	
	String[] alternateNames() default {};
	
	boolean unwrappend() default false;
}
