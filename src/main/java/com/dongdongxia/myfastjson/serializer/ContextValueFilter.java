package com.dongdongxia.myfastjson.serializer;
/**
 * 
 * <P>Description: 全文值过滤器</P>
 * @ClassName: ContextValueFilter
 * @author java_liudong@163.com  2017年5月22日 上午11:48:31
 */
public interface ContextValueFilter extends SerializeFilter{
	
	Object process(BeanContext context, Object object, String name, Object value);

}
