package com.dongdongxia.myfastjson;
/**
 * 
 * <P>Description: 输出自定义的JSON格式字符串,实现此接口</P>
 * @ClassName: JSONAware
 * @author java_liudong@163.com  2017年4月25日 上午10:40:16
 */
public interface JSONAware {

	/**
	 * 
	 * <p>Title: toJSONString</p>
	 * <p>Description: 自定义JSON字符串</p>
	 * @return JSON文本
	 * @author liudong@liuzedong.com  2017年4月25日 上午10:41:48
	 */
	String toJSONString();
	
}
