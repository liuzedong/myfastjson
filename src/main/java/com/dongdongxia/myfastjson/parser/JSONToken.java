package com.dongdongxia.myfastjson.parser;
/**
 * 
 * <P>Description: JSON的标识</P>
 * @ClassName: JSONToken
 * @author java_liudong@163.com  2017年6月29日 下午5:23:18
 */
public class JSONToken {

	public final static int ERROR = 1;
	
	public final static int LITERAL_INT  = 2;
	
	public final static int LITERAL_FLOAT = 3;
	
	public final static int LITERAL_STRING = 4;
	
	public final static int LITERAL_ISO8601_DATE = 5;
	
	public final static int TRUE = 6;
	
	public final static int FALSE = 7;
	
	public final static int NULL = 8;
	
	public final static int NEW = 9;
	
	public final static int LPAREN = 10; // ("("), 左括弧
	
	public final static int RPAREN = 11; // (")"), 右括弧
	
	public final static int LBRACE = 12; // ("{"), 左大括弧
	
	public final static int RBRACE = 13; // ("}"), 右大括弧
	
	public final static int LBRACKET = 14; // ("["), 左方括号
	
	public final static int RBRACKET = 15; // ("]"), 右方括号
	
	public final static int COMMA = 16; // (","),
	
	public final static int COLON = 17; // (":"),
	
	public final static int IDENTIFIER = 18; // 标识符
	
	public final static int FIELD_NAME = 19;
	
	public final static int EOF = 20;
	
	public final static int SET = 21;
	
	public final static int TREE_SET = 22;
	
	public final static int UNDEFINED = 23; // undefined
	
	public final static int SEMI = 24;
	
	public final static int DOT = 25;
	
	public static String name(int value) {
		switch (value) {
			case ERROR : 
				return "error";
			case LITERAL_INT :
				return "int";
			case LITERAL_FLOAT :
				return "float";
			case LITERAL_STRING :
				return "string";
			case LITERAL_ISO8601_DATE : 
				return "iso8601";
			case TRUE :
				return "true";
			case FALSE :
				return "false";
			case NULL :
				return "null";
			case NEW :
				return "new";
			case LPAREN :
				return "(";
			case RPAREN :
				return ")";
			case LBRACE :
				return "{";
			case RBRACE :
				return "}";
			case LBRACKET :
				return "[";
			case RBRACKET :
				return "]";
			case COMMA :
				return ",";
			case COLON :
				return ":";
			case SEMI :
				return ";";
			case DOT :
				return ".";
			case IDENTIFIER :
				return "ident";
			case FIELD_NAME :
				return "fieldName";
			case EOF :
				return "EOF";
			case TREE_SET :
				return "TreeSet";
			case UNDEFINED :
				return "undefined";
			default :
				return "Unknown";
		}
	}
	
}
