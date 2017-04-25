package com.dongdongxia.myfastjson;

import java.io.IOException;

/**
 * 
 * <P>Description: 模板, 由子类去实现这个功能</P>
 * @ClassName: JSONStreamAware
 * @author java_liudong@163.com  2017年4月25日 上午10:30:43
 */
public interface JSONStreamAware {

	/**
	 * 
	 * <p>Title: writeJSONString</p>
	 * <p>Description: 写出JSON字符串</p>
	 * @param out
	 * @throws IOException
	 * @author java_liudong@163.com  2017年4月25日 上午10:37:25
	 */
	void writeJSONString(Appendable out) throws IOException;
	
}
