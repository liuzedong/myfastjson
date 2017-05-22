package com.dongdongxia.myfastjson.serializer;
/**
 * 
 * <P>Description: 值的过滤接口</P>
 * @ClassName: ValueFilter
 * @author java_liudong@163.com  2017年5月22日 上午11:34:50
 */
public interface ValueFilter extends SerializeFilter{
	
	/**
	 * 
	 * <p>Title: process</p>
	 * <p>Description: 处理接口</p>
	 * @param object
	 * @param name
	 * @param value
	 * @return
	 * @author java_liudong@163.com  2017年5月22日 上午11:35:45
	 */
	Object process(Object object, String name, Object value);

}
