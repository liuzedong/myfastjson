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

	protected int bp;
	
	
	protected int features;
	
	/**
	 * 本地缓存的数据
	 */
	protected char[] sbuf;
	
	/**
	 * 缓存本地数据
	 */
	private final static ThreadLocal<char[]> SBUF_LOCAL = new ThreadLocal<char[]>();
	
	protected String stringDefaultValue = null;
	
	public JSONLexerBase(int features) {
		this.features = features;
		
		if ((features & Feature.InitStringFieldAsEmpty.mask) != 0) {
			stringDefaultValue = "";
		}
		
		sbuf = SBUF_LOCAL.get();
		
		if (sbuf == null) {
			sbuf = new char[512];
		}
	}
	
	
	@Override
	public void close() throws IOException {
		
	}

}
