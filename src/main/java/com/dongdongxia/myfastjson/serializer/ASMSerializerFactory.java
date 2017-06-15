package com.dongdongxia.myfastjson.serializer;

import static com.dongdongxia.myfastjson.util.ASMUtils.desc;
import static com.dongdongxia.myfastjson.util.ASMUtils.type;

import java.util.concurrent.atomic.AtomicLong;

import com.dongdongxia.myfastjson.asm.Opcodes;
import com.dongdongxia.myfastjson.util.ASMClassLoader;

/**
 * 
 * <P>Description: 使用ASM字节码, 生成序列化实体类工厂</P>
 * @ClassName: ASMSerializerFactory
 * @author java_liudong@163.com  2017年6月15日 上午11:33:07
 */
public class ASMSerializerFactory implements Opcodes{

	/**
	 * 用来将生成对象加载到内存中
	 */
	protected final ASMClassLoader classLoader = new ASMClassLoader();
	/**
	 * 用来生成 每个 序列化对象名称中的序号, 以便不出现重名
	 */
	private final AtomicLong seed = new AtomicLong();
	
	/**
	 * 下面为定义虚拟机内存中定义的对象名称
	 */
	static final String JSONSerializer = type(JSONSerializer.class); // com/dongdongxia/myfastjson/serializer/JSONSerializer
	static final String ObjectSerializer = type(ObjectSerializer.class); // com/dongdongxia/myfastjson/serializer/ObjectSerializer
	static final String ObjectSerializer_desc = "L" + ObjectSerializer + ";"; // Lcom/dongdongxia/myfastjson/serializer/ObjectSerializer;
	static final String SerializeWriter = type(SerializeWriter.class); // com/dongdongxia/myfastjson/serializer/SerializeWriter
	static final String SerializeWriter_desc = "L" + SerializeWriter + ";"; // Lcom/dongdongxia/myfastjson/serializer/SerializeWriter;
	static final String JavaBeanSerializer = type(JavaBeanSerializer.class); // com/dongdongxia/myfastjson/serializer/JavaBeanSerializer
	static final String JavaBeanSerialzier_desc = "L" + JavaBeanSerializer + ";"; // Lcom/dongdongxia/myfastjson/serializer/JavaBeanSerializer;
	static final String SerialContext_desc = desc(SerialContext.class); // Lcom/dongdongxia/myfastjson/serializer/SerialContext;
	static final String SerializeFilterable_desc = desc(SerializerFilterable.class); // Lcom/dongdongxia/myfastjson/serializer/SerializerFilterable;
	
	static class Context {
		
		/**
		 * 
		 * <p>Title: createJavaBeanSerializer</p>
		 * <p>Description: 使用字节码, 创建JavaBean序列化器</p>
		 * @param beanInfo
		 * @return
		 * @author java_liudong@163.com  2017年6月15日 上午11:54:08
		 */
		public JavaBeanSerializer createJavaBeanSerializer(SerializeBeanInfo beanInfo) {
			return null;
		}
	}
}
