package com.dongdongxia.myfastjson.parser;

import com.dongdongxia.myfastjson.util.ASMUtils;

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
		if (ch == 65279) { // utf-8 bom , 包含此字符的, 是UTF-8 bom的, 第一个字符, 是空的 65279, 所以循环寻找下一个
			next();
		}
	}
	
	public final char next() {
		int indext = ++bp;
		return ch = (indext >= this.len ? EOI : text.charAt(indext));
	}

	/**
	 * 
	 * <p>Title: isEOF</p>
	 * <p>Description: 判断文本是否已经到达结尾啦</p>
	 * @return
	 * @author java_liudong@163.com  2017年7月3日 上午10:34:59
	 * @see com.dongdongxia.myfastjson.parser.JSONLexerBase#isEOF()
	 */
	@Override
	public boolean isEOF() {
		return bp == len || ch == EOI && bp + 1 == len;
	}

	/*
	 * 将文本开始位置到个数, 复制到 字符数组中
	 */
	@Override
	protected void copyTo(int offset, int count, char[] dest) {
		text.getChars(offset, offset + count, dest, 0);
	}

	@Override
	public String stringVal() {
		if (!hasSpecial) {
			return this.subString(np + 1, sp);
		} else {
			return new String(sbuf, 0, sp);
		}
	}
	
	/**
	 * 
	 * <p>Title: subString</p>
	 * <p>Description: 截取JSON字符串文本</p>
	 * @param offset
	 * @param count
	 * @return
	 * @author java_liudong@163.com  2017年7月3日 下午3:20:00
	 * @see com.dongdongxia.myfastjson.parser.JSONLexerBase#subString(int, int)
	 */
	public final String subString(int offset, int count) {
		if (ASMUtils.IS_ANDROID) {
			if (count < sbuf.length) {
				text.getChars(offset, offset + count, sbuf, 0);
				return new String(sbuf, 0, count);
			} else {
				char[] chars = new char[count];
				text.getChars(offset, offset + count, chars, 0);
				return new String(chars);
			}
		} else {
			return text.substring(offset, offset + count);
		}
	}
}
