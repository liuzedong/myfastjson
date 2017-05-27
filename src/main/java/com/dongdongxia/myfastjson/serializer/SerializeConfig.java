package com.dongdongxia.myfastjson.serializer;
/**
 * 
 * <P>Description: 序列化配置对象</P>
 * @ClassName: SerializeConfig
 * @author java_liudong@163.com  2017年5月25日 上午11:45:06
 */
public class SerializeConfig {

	// 使用单例获取全局的配置
	public static final SerializeConfig globalInstance = new SerializeConfig();
	
	
	public SerializeConfig() {
		this(1024);
	}
	
	/**
	 * 
	 * <p>Title: 初始化容器,并确定是否使用ASM</p>
	 * <p>Description: Method for constructor</p>
	 * @param fieldBase
	 */
	public SerializeConfig(boolean fieldBase) {
		this(1024, fieldBase);
	}
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: 初始化容器大小</p>
	 * @param tableSize
	 */
	public SerializeConfig(int tableSize) {
		this(tableSize, false);
	}
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: 将序列化和反序列化 实现进行初始化, 放入到容器</p>
	 * @param tableSize 容器大小
	 * @param fieldBase 基础字段
	 */
	public SerializeConfig(int tableSize, boolean fieldBase) {
		
	}
}
