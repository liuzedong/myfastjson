package com.dongdongxia.myfastjson.serializer;
/**
 * 
 * <P>Description: 生成JSON字符串的功能枚举</P>
 * @ClassName: SerialerFeature
 * @author java_liudong@163.com  2017年4月25日 上午11:43:25
 */
public enum SerializerFeature {

	QuoteFieldNames, // 输出Key时是否使用双引号, 默认true
	/**
	 * 
	 */
	UseSingleQuotes, // 使用单引号而不是双引号, 默认为false
	/**
	 * 
	 */
	WriteMapNullValue, // 是否输出值为null的字段, 默认为false
	/**
	 * 用枚举toString()值输出
	 */
	WriteEnumUsingToString, // Enum输出name() 或者 original, 默认为false
	/**
	 * 用枚举name()输出
	 */
	WriteEnumUsingName, // 
	/**
	 * 
	 */
	UseISO8501DateFormat, // Date使用ISO8601格式输出，默认为false
	/**
	 * 
	 */
	WriteNullListAsEmpty, // List字段如果为null,输出为[],而非null 
	/**
	 * 
	 */
	WriteNullStringAsEmpty, // 字符类型字段如果为null,输出为"",而非null 
	/**
	 * 
	 */
	WriteNullNumberAsZero, // 数值字段如果为null,输出为0,而非null 
	/**
	 * 
	 */
	WriteNullBooleanAsFalse, // Boolean字段如果为null,输出为false,而非null
	/**
	 * 
	 */
	SkipTransientField, // 如果是true，类中的Get方法对应的Field是transient，序列化时将会被忽略。默认为true
	/**
	 * 
	 */
	SortField, // 按字段名称排序后输出。默认为false
	/**
	 * 
	 */
	@Deprecated
	WriteTabAsSpecial, // 把\t做转义输出，默认为false
	/**
	 * 
	 */
	PrettyFormat, // 结果是否格式化,默认为false
	/**
	 * 
	 */
	WriteClassName, // 序列化时写入类型信息，默认为false。反序列化是需用到
	/**
	 * 
	 */
	DisableCircularReferenceDetect, // 32768, 消除对同一对象循环引用的问题，默认为false
	/**
	 * @since 1.1.9
	 */
	WriteSlashAsSpecial, // 对斜杠'/'进行转义
	/**
	 * 
	 */
	BrowserCompatible, // 将中文都会序列化为\\uXXXX格式，字节数会多一些，但是能兼容IE 6，默认为false
	/**
	 * 
	 */
	WriteDateUseDateFormat, // 全局修改日期格式,默认为false。JSON.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd";JSON.toJSONString(obj, SerializerFeature.WriteDateUseDateFormat);
	/**
	 * 
	 */
	NotWriteRootClassName, // 
	/**
	 * 
	 */
	@Deprecated
	DisableCheckSpecialChar, // 一个对象的字符串属性中如果有特殊字符如双引号，将会在转成json时带有反斜杠转移符。如果不需要转义，可以使用这个属性。默认为false 
	/**
	 * 
	 */
	BeanToArray, 
	/**
	 * 
	 */
	WriteNonStringKeyAsString,
	/**
	 * 
	 */
	NotWriteDefaultValue,
	/**
	 * 
	 */
	BrowserSecure, // 将非Unicode的字符,转译成为Unicode的字符,16进制的
	/**
	 * 
	 */
	IgnoreNonFieldGetter, // 没有Field对象的时候, 就不进行字段的序列化, 默认为false
	/**
	 * 
	 */
	WriteNonStringValueAsString,
	/**
	 * 
	 */
	IgnoreErrorGetter, // 如果获取对象上字段的值,比如name的值刘东, 出现异常时, 值就设置为null, 而不是抛出异常, 默认为false, 抛出异常
	/**
	 * 
	 */
	WriteBigDecimalAsPlain, // BigDecimal 是否带指数字段 的字符串, BigDecimal.toPlainString()
	/**
	 * 
	 */
	MapSortField;
	
	SerializerFeature() {
		mask = (1 << ordinal());
	}
	
	public final int mask;
	
	public final int getMask(){
		return mask;
	}
	
	public static boolean isEnabled(int features, SerializerFeature feature){
		return (features & feature.mask) != 0;
	}
	
	public static boolean isEnabled(int features, int fieaturesB, SerializerFeature feature){
		int mask = feature.mask;
		return (features & mask) != 0 || (fieaturesB & mask) != 0;
	}
	
	public static int config(int features, SerializerFeature feature, boolean state){
		if (state){
			features |= feature.mask;
		} else {
			features &= ~feature.mask;
		}
		return features;
	}
	
	public static int of(SerializerFeature[] features){
		if (features == null){
			return 0;
		}
		
		int value = 0;
		
		for (SerializerFeature feature : features){
			value |= feature.mask;
		}
		return value;
	}
	
	public final static SerializerFeature[] EMPTY = new SerializerFeature[0];
	
	public static final int WRITE_MAP_NULL_FEATURES
			= WriteMapNullValue.getMask()
			| WriteNullBooleanAsFalse.getMask()
			| WriteNullListAsEmpty.getMask()
			| WriteNullNumberAsZero.getMask()
			| WriteNullStringAsEmpty.getMask();
}
