package com.dongdongxia.myfastjson.serializer;
/**
 * 
 * <P>Description: JSON序列化控制器</P>
 * @ClassName: JSONSerializer
 * @author java_liudong@163.com  2017年4月28日 下午2:14:19
 */
public class JSONSerializer extends SerializerFilterable{

	/** 两个主要对象, 用来控制配置和输出, 在构造方法中初始化*/
	protected final SerializeConfig config;
	public final SerializeWriter out;
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: 初始化默认的 写入 和 全局配置的对象</p>
	 */
	public JSONSerializer() {
		this(new SerializeWriter(), SerializeConfig.getGlobalConfig());
	}
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: 指定输出构造方法</p>
	 * @param out 序列输出
	 */
	public JSONSerializer(SerializeWriter out) {
		this(out, SerializeConfig.getGlobalConfig());
	}
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: 指定全局配置的构造方法</p>
	 * @param config 全局配置
	 */
	public JSONSerializer(SerializeConfig config) {
		this(new SerializeWriter(), config);
	}
	
	
	
	public JSONSerializer(SerializeWriter out, SerializeConfig config) {
		this.out = out;
		this.config = config;
	}
	
	protected final void writeKeyValue(char seperator, String key, Object value){
		
	}
	
}
