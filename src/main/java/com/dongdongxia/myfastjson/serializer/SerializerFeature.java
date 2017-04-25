package com.dongdongxia.myfastjson.serializer;
/**
 * 
 * <P>Description: 序列化使用枚举</P>
 * @ClassName: SerialerFeature
 * @author java_liudong@163.com  2017年4月25日 上午11:43:25
 */
public enum SerializerFeature {

	QuoteFieldNames,
	/**
	 * 
	 */
	UseSingleQuotes,
	/**
	 * 
	 */
	WriteMapNullValue,
	/**
	 * 用枚举toString()值输出
	 */
	WriteEnumUsingToString,
	/**
	 * 用枚举name()输出
	 */
	WriteEnumUsingName,
	/**
	 * 
	 */
	UseISO8501DateFormat,
	/**
	 * 
	 */
	WriteNullListAsEmpty,
	/**
	 * 
	 */
	WriteNullStringAsEmpty,
	/**
	 * 
	 */
	WriteNullNumberAsZero,
	/**
	 * 
	 */
	WriteNullBooleanAsFalse,
	/**
	 * 
	 */
	SkipTransientField,
	/**
	 * 
	 */
	SortField,
	/**
	 * 
	 */
	@Deprecated
	WriteTabAsSpecial,
	/**
	 * 
	 */
	PrettyFormat,
	/**
	 * 
	 */
	WriteClassName,
	/**
	 * 
	 */
	DisableCircularReferenceDetect, // 32768
	/**
	 * @since 1.1.9
	 */
	WriteSlashAsSpecial,
	/**
	 * 
	 */
	BrowerCompatible,
	/**
	 * 
	 */
	WriteDateUseDateFormat,
	/**
	 * 
	 */
	NotWriteRootClassName,
	/**
	 * 
	 */
	@Deprecated
	DisableCheckSpecialChar,
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
	BrowerSecure,
	/**
	 * 
	 */
	IgnoreNonFieldGetter,
	/**
	 * 
	 */
	WriteNonStringValueAsString,
	/**
	 * 
	 */
	IgnoreErrorGetter,
	/**
	 * 
	 */
	WriteBigDecimalAsPlain,
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
