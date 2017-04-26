package com.dongdongxia.myfastjson.parser;
/**
 * 
 * <P>Description: 解析JSON的功能枚举</P>
 * @ClassName: Feature
 * @author java_liudong@163.com  2017年4月26日 上午10:45:14
 */
public enum Feature {

	/**
	 * 
	 */
	AutoCloseSource,
	/**
	 * 
	 */
	AllowComment,
	/**
	 * 
	 */
	AllowUnQuotedFieldNames,
	/**
	 * 
	 */
	AllowSingleQuotes,
	/**
	 * 
	 */
	InternFieldName,
	/**
	 * 
	 */
	AllowISO8601DateFormat,
	/**
	 * 允许有多个逗号
	 * {"a":1,,,"b":2}
	 */
	AllowArbitrayCommas,
	/**
	 * 
	 */
	UseBigDecimal,
	/**
	 * 
	 */
	IgnoreNotMatch,
	/**
	 * 
	 */
	SortFeidFastMatch,
	/**
	 * 
	 */
	DisableASM,
	/**
	 * 
	 */
	DisableCircularReferenceDetect,
	/**
	 * 
	 */
	InitStringFieldAsEmpty,
	/**
	 * 
	 */
	SupportArrayToBean,
	/**
	 * 
	 */
	OrderedField,
	/**
	 * 
	 */
	DisableSpecialKeyDetect,
	/**
	 * 
	 */
	UseObjectArray,
	/**
	 * 
	 */
	SupportNonPublicField,
	/**
	 * 
	 */
	IgnoreAutoType,
	/**
	 * 
	 */
	DisableFieldSmartMatch;
	
	Feature(){
		mask = (1 << ordinal());
	}
	
	public final int mask;
	
	public final int getMask(){
		return mask;
	}
	
	public static boolean isEnabled(int features, Feature feature){
		return (features & feature.mask) != 0;
	}
	
	public static int config(int features, Feature feature, boolean state){
		if (state){
			features |= feature.mask;
		} else {
			features &= ~feature.mask;
		}
		return features;
	}
	
	public static int of(Feature[] features){
		if (features == null){
			return 0;
		}
		
		int value = 0;
		
		for (Feature feature : features){
			value |= feature.mask;
		}
		
		return value;
	}
}
