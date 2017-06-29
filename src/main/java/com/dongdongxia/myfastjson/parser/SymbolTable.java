package com.dongdongxia.myfastjson.parser;

import com.dongdongxia.myfastjson.JSON;

/**
 * 
 * <P>Description: 符号表</P>
 * @ClassName: SymbolTable
 * @author java_liudong@163.com  2017年6月29日 下午3:44:22
 */
public class SymbolTable {

	private final String[] symbols;
	private final int indexMask;
	
	public SymbolTable(int tableSize) {
		this.indexMask = tableSize - 1;
		this.symbols = new String[tableSize];
		
		this.addSymbol("$ref", 0, 4, "$ref".hashCode());
		this.addSymbol(JSON.DEFAULT_TYPE_KEY, 0, JSON.DEFAULT_TYPE_KEY.length(), JSON.DEFAULT_TYPE_KEY.hashCode()); // @type
	}
	
	/**
	 * 
	 * <p>Title: addSymbol</p>
	 * <p>Description: 添加符号</p>
	 * @param buffer 写入的字符
	 * @param offset 写入的起点
	 * @param len 写入的长度
	 * @param hash 字符串的HashCode
	 * @return
	 * @author java_liudong@163.com  2017年6月29日 下午4:41:37
	 */
	public String addSymbol(String buffer, int offset, int len, int hash) {
		final int bucket = hash & indexMask;
		
		String symbol = symbols[bucket];
		if (symbol != null) {
			if (hash == symbol.hashCode() && len == symbol.length() && buffer.startsWith(symbol, offset)) {
				return symbol;
			}
			return subString(buffer, offset, len);
		}
		
		symbol = len == buffer.length() ? buffer : subString(buffer, offset, len);
		symbol = symbol.intern();
		symbols[bucket] = symbol;
		return symbol;
	}
	
	/**
	 * 
	 * <p>Title: subString</p>
	 * <p>Description: 截取字符串</p>
	 * @param src 字符串
	 * @param offset 起始位置
	 * @param len 截止的长度
	 * @return
	 * @author java_liudong@163.com  2017年6月29日 下午4:46:33
	 */
	private static String subString(String src, int offset, int len) {
		char[] chars = new char[len];
		src.getChars(offset, offset + len, chars, 0);
		return new String(chars);
	}
}
