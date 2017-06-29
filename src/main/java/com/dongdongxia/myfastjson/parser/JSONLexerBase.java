package com.dongdongxia.myfastjson.parser;

import java.io.Closeable;
import java.io.IOException;
/**
 * 
 * <P>Description: JSON的词法分析器默认实现</P>
 * @ClassName: JSONLexerBase
 * @author java_liudong@163.com  2017年6月29日 上午11:31:33
 */
public abstract class JSONLexerBase implements JSONLexer, Closeable{

	@Override
	public void close() throws IOException {
		
	}

}
