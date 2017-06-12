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
	
	/**
	 * 
	 * <p>Title: getGlobalConfig</p>
	 * <p>Description: 获取单例全局实例</p>
	 * @return
	 * @author java_liudong@163.com  2017年5月27日 上午10:13:31
	 */
	public static SerializeConfig getGlobalConfig() {
		return globalInstance;
	}
	
	/**
	 * 
	 * <p>Title: getObjectWriter</p>
	 * <p>Description: 获取指定对象的序列化实现对象</p>
	 * @param clazz
	 * @return
	 * @author java_liudong@163.com  2017年6月12日 上午10:28:46
	 */
	public ObjectSerializer getObjectWriter(Class<?> clazz) {
		return getObjectWriter(clazz, true);
	}
	
	/**
	 * 
	 * <p>Title: getObjectWriter</p>
	 * <p>Description: 获取指定对象的序列化实现对象, 没有就使用字节码创建</p>
	 * @param clazz 序列化对象
	 * @param create 是否创建
	 * @return
	 * @author java_liudong@163.com  2017年6月12日 上午10:30:08
	 */
	private ObjectSerializer getObjectWriter(Class<?> clazz, boolean create) {
		return null;
	}
}
