package com.dongdongxia.myfastjson.parser.deserializer;

import com.dongdongxia.myfastjson.asm.Opcodes;
import com.dongdongxia.myfastjson.util.ASMClassLoader;

/**
 * 
 * <P>Description: 根据字节码,进行序列化</P>
 * @ClassName: ASMDeserializerFactory
 * @author java_liudong@163.com  2017年4月27日 下午4:10:29
 */
public class ASMDeserializerFactory implements Opcodes{
	
	public final ASMClassLoader classLoader;
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: Method for constructor</p>
	 * @param parentClassLoader
	 */
	public ASMDeserializerFactory(ClassLoader parentClassLoader){
		classLoader = parentClassLoader instanceof ASMClassLoader
				? (ASMClassLoader) parentClassLoader
				: new ASMClassLoader(parentClassLoader);
	}
	
}
