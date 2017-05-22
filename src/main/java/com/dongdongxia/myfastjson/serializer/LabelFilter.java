package com.dongdongxia.myfastjson.serializer;
/**
 * 
 * <P>Description: 标签过滤器接口</P>
 * @ClassName: LabelFilter
 * @author java_liudong@163.com  2017年5月22日 上午11:45:24
 */
public interface LabelFilter extends SerializeFilter{

	/**
	 * 
	 * <p>Title: apply</p>
	 * <p>Description: 标签过滤器</p>
	 * @param label 标签
	 * @return
	 * @author java_liudong@163.com  2017年5月22日 上午11:46:02
	 */
	boolean apply(String label);
}
