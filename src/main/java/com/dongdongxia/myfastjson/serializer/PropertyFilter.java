package com.dongdongxia.myfastjson.serializer;
/**
 * 
 * <P>Description: properties配置文件过滤器</P>
 * @ClassName: PropertyFilter
 * @author java_liudong@163.com  2017年5月22日 上午11:14:23
 */
public interface PropertyFilter extends SerializeFilter{

	/**
	 * 
	 * <p>Title: apply</p>
	 * <p>Description: 应用</p>
	 * @param object proprety 中的对象
	 * @param name proprety 中的name
	 * @param value proprety 中的value
	 * @return
	 * @author java_liudong@163.com  2017年5月22日 上午11:15:30
	 */
	boolean apply(Object object, String name, Object value);
}
