package com.dongdongxia.myfastjson.parser;
/**
 * 说是这个类做勒, 为勒性能做勒特别处理, 所以性能很重要, 着重看
 * <P>Description: JSON扫描器, 用来解析使用</P>
 * @ClassName: JSONScanner
 * @author java_liudong@163.com  2017年6月29日 上午11:34:08
 */
public final class JSONScanner extends JSONLexerBase{

	/**
	 * JSON串
	 */
	private final String text;
	/**
	 * JSON串长度
	 */
	private final int len;
	
	
	public JSONScanner(String input, int features) {
		super(features);
		
		text = input;
		len = text.length();
		bp = -1;
		
		next();
		if (ch == 65279) { // utf-8 bom , 包含此字符的, 是UTF-8 bom的
			next();
		}
	}
	
	public final char next() {
		int indext = ++bp;
		return ch = (indext >= this.len ? EOI : text.charAt(indext));
	}
}
