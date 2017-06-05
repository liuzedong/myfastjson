package com.dongdongxia.myfastjson;
/**
 * 
 * <P>Description: 命令规范枚举</P>
 * @ClassName: PropertyNamingStrategy
 * @author java_liudong@163.com  2017年6月5日 上午9:52:46
 */
public enum PropertyNamingStrategy {
	/**
	 * 驼峰命名法
	 * 案例: lastName
	 */
	CamelCase,
	/**
	 * 帕斯卡命名法,首字母大写
	 * 案例 : LastName
	 */
	PacalCase,
	/**
	 * 每个单词之间,使用下划线分割,单词可以是小写,可以是大写, 这里的转换,全部转换成为小写
	 * 案例 : last_name or Last_Name
	 */
	SnakeCase,
	/**
	 *  每个单词以- 进行分割
	 *  案例 : last-name or Last-Name
	 */
	KebabCase;
	
	/**
	 * 
	 * <p>Title: translate</p>
	 * <p>Description: 命名规范之间的转换, JDK8 中已经有这个方法啦</p>
	 * @return 
	 * @author java_liudong@163.com  2017年6月5日 上午10:05:27
	 */
	public String translate(String propertyName) {
		switch (this) {
			case SnakeCase: {
				StringBuilder buf = new StringBuilder();
				// 使用递归替换
				for (int i = 0; i < propertyName.length(); ++i) {
					char ch = propertyName.charAt(i);
					if (ch >= 'A' && ch <= 'Z') {
						char ch_ucase = (char)(ch + 32);
						if (i > 0) {
							buf.append('_');
						}
						buf.append(ch_ucase);
					} else {
						buf.append(ch);
					}
				}
				return buf.toString();
			}
			case KebabCase: {
				StringBuilder buf = new StringBuilder();
				for (int i = 0; i < propertyName.length(); ++i) {
					char ch = propertyName.charAt(i);
					if (ch >= 'A' && ch <= 'Z') {
						char ch_ucase = (char) (ch + 32);
						if (i > 0) {
							buf.append('-');
						}
						buf.append(ch_ucase);
					} else {
						buf.append(ch);
					}
				}
				return buf.toString();
			}
			case PacalCase: {
				char ch = propertyName.charAt(0);
				if (ch >= 'a' && ch <= 'z') {
					char[] chars = propertyName.toCharArray();
					chars[0] -= 32; // 小写变大写
					return new String(chars);
				}
				return propertyName;
			}
			case CamelCase: {
				char ch = propertyName.charAt(0);
				if (ch >= 'A' && ch <= 'Z') {
					char[] chars = propertyName.toCharArray();
					chars[0] += 32;  // 大写变小写
					return new String(chars);
				}
				return propertyName;
			}
			default: 
				return propertyName;
		}
	}
	
}
