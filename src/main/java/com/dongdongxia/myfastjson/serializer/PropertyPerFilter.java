package com.dongdongxia.myfastjson.serializer;
/**
 * 
 * <P>Description: 配置文件前置处理器</P>
 * @ClassName: PropertyPerFilter
 * @author java_liudong@163.com  2017年5月22日 上午11:42:05
 */
public interface PropertyPerFilter extends SerializeFilter{

	/**
	 * 
	 * <p>Title: apply</p>
	 * <p>Description: 配置文件前置处理器</p>
	 * @param serializer
	 * @param object
	 * @param name
	 * @return
	 * @author java_liudong@163.com  2017年5月22日 上午11:43:15
	 */
	boolean apply(JSONSerializer serializer, Object object, String name);
}
