package com.dongdongxia.myfastjson;


/**
 * 
 * <P>Description: JSON的转换和解析</P>
 * @ClassName: JSON
 * @author java_liudong@163.com  2017年4月25日 上午10:27:10
 */
public abstract class JSON implements JSONStreamAware, JSONAware{

	
	@Override
	public String toString() {
		return toJSONString();
	}
	
	@Override
	public String toJSONString() {
		return null;
	}
	
	@Override
	public void writeJSONString(Appendable out) {
		
	}
	
}
