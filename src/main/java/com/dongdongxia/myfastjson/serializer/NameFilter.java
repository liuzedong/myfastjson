package com.dongdongxia.myfastjson.serializer;
/**
 * 
 * <P>Description: 键的过滤器接口</P>
 * @ClassName: NameFilter
 * @author java_liudong@163.com  2017年5月22日 上午11:38:31
 */
public interface NameFilter extends SerializeFilter{

	/**
	 * 
	 * <p>Title: process</p>
	 * <p>Description: 键处理接口</p>
	 * @param object
	 * @param name
	 * @param value
	 * @return
	 * @author java_liudong@163.com  2017年5月22日 上午11:39:23
	 */
	String process(Object object, String name, Object value);
}
