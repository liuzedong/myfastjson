package com.dongdongxia.myfastjson.parser;
/**
 * 
 * <P>Description: JSON词法分析器接口</P>
 * @ClassName: JSONLexer
 * @author java_liudong@163.com  2017年4月26日 下午4:50:54
 */
public interface JSONLexer {

	char EOI = 0x1A;
	
	char next();
	
}
