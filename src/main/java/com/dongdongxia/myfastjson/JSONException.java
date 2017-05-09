package com.dongdongxia.myfastjson;
/**
 * 
 * <P>Description: 定义JSON内部异常类, 紧紧是包装类 RuntimeException</P>
 * @ClassName: JSONException
 * @author java_liudong@163.com  2017年5月9日 上午10:39:34
 */
@SuppressWarnings("serial") // 此处说明, 这个异常类, 不进行序列化
public class JSONException extends RuntimeException{
	
	public JSONException() {
		super();
	}
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: 错误描述</p>
	 * @param message 错误描述
	 */
	public JSONException(String message) {
		super(message);
	}
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: Method for constructor</p>
	 * @param message 错误描述
	 * @param cause 指定异常
	 */
	public JSONException(String message, Throwable cause) {
		super(message, cause);
	}
}
