package com.dongdongxia.myfastjson.parser;

import com.dongdongxia.myfastjson.util.IOUtils;

/**
 * 
 * <P>Description: 解析的默认配置</P>
 * @ClassName: ParserConfig
 * @author java_liudong@163.com  2017年4月26日 下午6:59:14
 */
public class ParserConfig {

	public final static String DENY_PROPERTY = "myfastjson.parser.deny";
	public final static String AUTOTYPE_ACCEPT = "myfastjson.parser.autoTypeAccept";
	public final static String AUTOTYPE_SUPPORT_PROPERTY = "myfastjson.parser.autoTypeSupport";
	
	public static final String[] DENYS;
	private static final String[] AUTO_TYPE_ACCEPT_LIST;
	public static final boolean AUTO_SUPPORT;
	
	
	static {
		{
			String property = IOUtils.getStringProperty(DENY_PROPERTY);
			DENYS = splitItemsFormProperty(property);
		}
		{
			String property = IOUtils.getStringProperty(AUTOTYPE_SUPPORT_PROPERTY);
			AUTO_SUPPORT = "true".equals(property);
		}
		{
			String property = IOUtils.getStringProperty(AUTOTYPE_ACCEPT);
			String items[] = splitItemsFormProperty(property);
			if (items == null){
				items = new String[0];
			}
			AUTO_TYPE_ACCEPT_LIST = items;
		}
	}

	/**
	 * 
	 * <p>Title: getGlobalInstance</p>
	 * <p>Description: 单例,获取对象的方法</p>
	 * @return
	 * @author java_liudong@163.com  2017年4月26日 下午7:12:18
	 */
	public static ParserConfig getGlobalInstance() {
		return global;
	}
	
	public static ParserConfig global = new ParserConfig();
	
	/**
	 * 
	 * <p>Title: splitItemsFormProperty</p>
	 * <p>Description: 将字符串按照, 进行分割</p>
	 * @param property myfastjson.properties中的value
	 * @return
	 * @author java_liudong@163.com  2017年4月26日 下午7:07:24
	 */
	private static String[] splitItemsFormProperty(final String property){
		if (property!= null && property.length() > 0){
			return property.split(",");
		}
		return null;
	}
	
}
