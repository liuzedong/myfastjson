package com.dongdongxia.myfastjson.parser;

import java.io.Closeable;
import java.io.IOException;

import com.dongdongxia.myfastjson.JSONException;
/**
 * 
 * <P>Description: JSON的词法分析器默认实现</P>
 * @ClassName: JSONLexerBase
 * @author java_liudong@163.com  2017年6月29日 上午11:31:33
 */
public abstract class JSONLexerBase implements JSONLexer, Closeable{

	protected int token; // 字符标识
	protected int pos;
	protected int features;
	
	/**
	 * A character buffer for literals : 文本字符缓冲区
	 */
	protected char[] sbuf;
	protected int sp;
	
	/**
	 * number start position : 起始位置的数字
	 */
	protected int np;
	
	protected boolean hasSpecial; // special : 特别的, 专属的
	
	protected char ch; // 存储当前的字符
	protected int bp; // 当前读取字符的下标
	
	protected final static int[] digits = new int[(int) 'f' + 1];
	
	static {
		for (int i = '0'; i < '9'; ++i) {
			digits[i] = i - '0';
		}
		
		for (int i = 'a'; i <= 'f'; ++i) {
			digits[i] = (i - 'a') + 10;
		}
		
		for (int i = 'A'; i <= 'F'; ++i) {
			digits[i] = (i - 'A') + 10;
		}
	}
	
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
	
	/**
	 * 
	 * <p>Title: getCurrent</p>
	 * <p>Description: 返回当前的第一个字符</p>
	 * @return
	 * @author java_liudong@163.com  2017年6月29日 下午5:17:14
	 * @see com.dongdongxia.myfastjson.parser.JSONLexer#getCurrent()
	 */
	public final char getCurrent() {
		return ch;
	}
	
	/**
	 * 
	 * <p>Title: nextToken</p>
	 * <p>Description: 获取下一个标记</p>
	 * @author java_liudong@163.com  2017年6月29日 下午6:39:23
	 * @see com.dongdongxia.myfastjson.parser.JSONLexer#nextToken()
	 */
	public final void nextToken() {
		sp = 0;
		for (;;) {
			pos = bp;
			
			if (ch == '/') {
				skipComment();
				continue ;
			}
			
			if (ch == '"') { // ", 如果是 " 就扫描这个字符串
				scanString();
				return ;
			}
			
			if (ch == ',') {
				next();
				token = JSONToken.COMMA;
				return ;
			}
			
			if (ch >= '0' && ch <='9') { // 检测数字
				scanNumber();
				return ;
			}
			
			if (ch == '-') {
				scanNumber();
				return ;
			}
			
			switch (ch) {
				case '\'' :
					if (!isEnabled(Feature.AllowSingleQuotes)) {
						throw new JSONException("Feature.AllowSingleQuites is false");
					}
					scanStringSingleQuote();
					return ;
			}
		}
	}
	
	/**
	 * 
	 * <p>Title: scanStringSingleQuote</p>
	 * <p>Description: 扫描单引号中的字符串</p>
	 * @author java_liudong@163.com  2017年7月3日 上午11:42:27
	 */
	private void scanStringSingleQuote() {
		np = bp;
		hasSpecial = false;
		char chLocal;
		for (;;) {
			chLocal = next();
			
			if (chLocal == '\'') {
				break ;
			}
			
			if (chLocal == EOI) {
				if (!isEOF()) {
					putChar((char) EOI);
					continue ;
				}
				throw new JSONException("unclosed single-quote string");
			}
			
			if (chLocal == '\\') {
				if (!hasSpecial) {
					hasSpecial = true;
					
					if (sp > sbuf.length) {
						char[] newsbuf = new char[sp * 2];
						System.arraycopy(sbuf, 0, newsbuf, 0, sbuf.length);
						sbuf = newsbuf;
					}
					
					this.copyTo(np + 1, sp, sbuf);
				}
				
				chLocal = next();
				
				switch (chLocal) {
					 case '0':
	                     putChar('\0');
	                     break;
	                 case '1':
	                     putChar('\1');
	                     break;
	                 case '2':
	                     putChar('\2');
	                     break;
	                 case '3':
	                     putChar('\3');
	                     break;
	                 case '4':
	                     putChar('\4');
	                     break;
	                 case '5':
	                     putChar('\5');
	                     break;
	                 case '6':
	                     putChar('\6');
	                     break;
	                 case '7':
	                     putChar('\7');
	                     break;
	                 case 'b': // 8
	                     putChar('\b');
	                     break;
	                 case 't': // 9
	                     putChar('\t');
	                     break;
	                 case 'n': // 10
	                     putChar('\n');
	                     break;
	                 case 'v': // 11
	                     putChar('\u000B');
	                     break;
	                 case 'f': // 12
	                 case 'F':
	                     putChar('\f');
	                     break;
	                 case 'r': // 13
	                     putChar('\r');
	                     break;
	                 case '"': // 34
	                     putChar('"');
	                     break;
	                 case '\'': // 39
	                     putChar('\'');
	                     break;
	                 case '/': // 47
	                     putChar('/');
	                     break;
	                 case '\\': // 92
	                     putChar('\\');
	                     break;
	                 case 'x':
	                     putChar((char) (digits[next()] * 16 + digits[next()]));
	                     break;
	                 case 'u':
	                     putChar((char) Integer.parseInt(new String(new char[] { next(), next(), next(), next() }), 16));
	                     break;
	                 default:
	                     this.ch = chLocal;
	                     throw new JSONException("unclosed single-quote string");
				}
				continue ;
			}
			
			if (!hasSpecial) {
				sp++;
				continue;
			}
			
			if (sp == sbuf.length) {
				putChar(chLocal);
			} else {
				sbuf[sp++] = chLocal;
			}
		}
		
		token = JSONToken.LITERAL_STRING;
		this.next();
	}
	
	public final boolean isEnabled(Feature feature) {
		return isEnabled(feature.mask);
	}
	
	public final boolean isEnabled(int feature) {
		return (this.features & feature) != 0;
	}
	
	public final boolean isEnbaled(int features, int feature) {
		return (this.features & feature) != 0 || (features & feature) != 0;
	}
	
	/**
	 * 
	 * <p>Title: scanNumber</p>
	 * <p>Description: 扫描数字</p>
	 * @author java_liudong@163.com  2017年7月3日 上午11:36:39
	 * @see com.dongdongxia.myfastjson.parser.JSONLexer#scanNumber()
	 */
	public final void scanNumber() {
		np = bp;
		
		if (ch == '-') {
			sp ++;
			next();
		}
		
		for (;;) {
			if (ch >= '0' && ch <= '9') {
				sp ++;
			} else {
				break ;
			}
			next();
		}
		
		boolean isDouble = false;
		
		if (ch == '.') {
			sp++;
			next();
			isDouble = true;
			
			for (;;) {
				if (ch >= '0' && ch <= '9') {
					sp ++;
				} else {
					break ;
				}
				next();
			}
		}
		
		if (ch == 'L') {
			sp++;
			next();
		} else if (ch == 'S') {
			sp++;
			next();
		} else if (ch == 'B') {
			sp++;
			next();
		} else if (ch == 'F') {
			sp++;
			next();
			isDouble = true;
		} else if (ch == 'D') {
			sp++;
			next();
			isDouble= true;
		} else if (ch == 'e' || ch == 'E') {
			sp++;
			next();
			
			if (ch == '+' || ch == '-') {
				sp++;
				next();
			}
			
			for (;;) {
				if (ch >= '0' && ch <= '9') {
					sp++;
				} else {
					break ;
				}
				next();
			}
			
			if (ch == 'D' ||ch == 'F') {
				sp++;
				next();
			}
			
			isDouble = true;
		}
		
		if (isDouble) {
			token = JSONToken.LITERAL_FLOAT;
		} else {
			token = JSONToken.LITERAL_INT;
		}
		
	}
	
	/**
	 * 
	 * <p>Title: scanString</p>
	 * <p>Description: 读取 " 后面的字符</p>
	 * @author java_liudong@163.com  2017年7月3日 上午10:25:48
	 */
	public final void scanString() {
		np = bp;
		hasSpecial = false;
		
		char ch;
		for (;;) {
			ch = next();
			
			if (ch == '\"') { // ""
				break ;
			}
			
			if (ch == EOI) {
				if (!isEOF()) {
					putChar((char) EOI);
					continue ;
				}
				throw new JSONException("unclosed string" + ch);
			}
			
			if (ch == '\\') { // \ , 说明是有一个转译字符
				if (!hasSpecial) {
					hasSpecial = true;
					
					if (sp >= sbuf.length) {
						int newCapcity = sbuf.length * 2;
						if (sp > newCapcity) {
							newCapcity = sp;
						}
						char[] newsbuf = new char[newCapcity];
						System.arraycopy(sbuf, 0, newsbuf, 0, sbuf.length);
						sbuf = newsbuf;
					}
					copyTo(np + 1, sp, sbuf);
				}
				
				ch = next();
				switch (ch) { // 将转译字符进行转译, \0, \1, \t, \n 等
					case '0' :
						putChar('\0');
						break ;
					case '1' :
						putChar('\1');
						break ;
					case '2' :
						putChar('\2');
						break ;
					case '3' :
						putChar('\3');
						break ;
					case '4' :
						putChar('\4');
						break ;
					case '5' :
						putChar('\5');
						break ;
					case '6' :
						putChar('\6');
						break ;
					case '7' :
						putChar('\7');
						break ;
					case 'b' :
						putChar('\b');
						break ;
					case 't' :
						putChar('\t');
						break ;
					case 'n' :
						putChar('\n');
						break ;
					case 'v' :
						putChar('\u000B');
						break ;
					case 'f' :
					case 'F' :
						putChar('\f');
						break ;
					case 'r' :
						putChar('\r');
						break ;
					case '"' :
						putChar('"');
						break ;
					case '\'' :
						putChar('\'');
						break ;
					case '/' :
						putChar('/');
						break ;
					case '\\' :
						putChar('\\');
						break ;
					case 'x' :
						char x1 = ch = next();
						char x2 = ch = next();
						
						int x_val = digits[x1] * 16 + digits[x2];
						char x_char = (char)x_val;
						putChar(x_char);
						break ;
					case 'u' :
						char u1 = ch = next();
						char u2 = ch = next();
						char u3 = ch = next();
						char u4 = ch = next();
						int val = Integer.parseInt(new String(new char[] {u1, u2, u3, u4}),  16); // 
						putChar((char) val);
						break ;
					default :
						this.ch = ch;
						throw new JSONException("unclosed string" + ch);
				}
				continue ;
			}
			
			if (!hasSpecial) {
				sp ++;
				continue ;
			}
			
			if (sp == sbuf.length) {
				putChar(ch);
			} else {
				sbuf[sp++] = ch;
			}
			
			token = JSONToken.LITERAL_STRING;
			this.ch = next();
		}
		
	}
	
	protected abstract void copyTo(int offset, int count, char[] dest);
	
	/**
	 * 
	 * <p>Title: putChar</p>
	 * <p>Description: 将字符放置到缓存的最后面</p>
	 * @param ch
	 * @author java_liudong@163.com  2017年7月3日 上午10:38:19
	 */
	protected final void putChar(char ch) {
		if (sp == sbuf.length) {
			 char[] newsbuf = new char[sbuf.length * 2];
			 System.arraycopy(sbuf, 0, newsbuf, 0, sbuf.length);
			 sbuf = newsbuf;
		}
		sbuf[sp++] = ch;
	}
	
	public abstract boolean isEOF();
	
	/**
	 * 
	 * <p>Title: skipComment</p>
	 * <p>Description: 跳过该数据</p>
	 * <p> 此处主要是解析 // /* 等注解, 就进行跳过 </p>
	 * @author java_liudong@163.com  2017年7月3日 上午9:46:31
	 */
	protected void skipComment() {
		next();
		if (ch == '/') { // //
			for (;;) {
				next();
				if (ch == '\n') {
					next();
					return ;
				} else if (ch == EOI) {
					return ;
				}
			}
		} else if (ch == '*') { // /*
			next();
			
			for (; ch != EOI;) {
				if (ch == '*') {
					next();
					if (ch == '/') { // */
						next();
						return ;
					} else {
						continue ;
					}
				}
				next();
			}
		} else { // 没有解析 到// 或者 /* */ 注解的时候, 就抛出异常
			throw new JSONException("invalid comment");
		}
	}
	
	public abstract char next();
	
	@Override
	public void close() throws IOException {
		
	}

}
