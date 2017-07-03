package com.dongdongxia.myfastjson.parser;

import java.io.Closeable;
import java.io.IOException;

/**
 * 
 * <P>Description: 默认JSON解析类</P>
 * @ClassName: DefaultJSONParser
 * @author java_liudong@163.com  2017年4月26日 下午4:49:02
 */
public class DefaultJSONParser implements Closeable{

	public final JSONLexer lexer;
	public final Object input;
	protected ParserConfig config;
	public final SymbolTable symbolTable;
	
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: 构造方法</p>
	 * @param input json串
	 * @param config 默认配置器
	 */
	public DefaultJSONParser(final String input, final ParserConfig config, int features){
		this(input, new JSONScanner(input, features), config);
	}
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: 默认JSON解析器的构造方法</p>
	 * @param input JSON串
	 * @param lexer 字符语法解析器
	 * @param config 解析器的配置信息
	 */
	public DefaultJSONParser(final Object input, final JSONLexer lexer, final ParserConfig config) {
		this.lexer = lexer;
		this.input = input;
		this.config = config;
		this.symbolTable = config.symbolTable;

		int ch = lexer.getCurrent();
		if (ch == '{') {
			lexer.next();
			((JSONLexerBase)lexer).token = JSONToken.LBRACE;
		} else if (ch == '[') {
			lexer.next();
			((JSONLexerBase)lexer).token = JSONToken.LBRACKET;
		} else {
			lexer.nextToken();
		}
	}
	
	@Override
	public void close() throws IOException {
		
	}

}
