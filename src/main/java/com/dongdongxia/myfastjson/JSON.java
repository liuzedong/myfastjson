package com.dongdongxia.myfastjson;

import java.util.Locale;
import java.util.TimeZone;

import com.dongdongxia.myfastjson.parser.Feature;
import com.dongdongxia.myfastjson.serializer.SerializerFeature;
import com.dongdongxia.myfastjson.util.IOUtils;


/**
 * 
 * <P>Description: JSON的转换和解析</P>
 * @ClassName: JSON
 * @author java_liudong@163.com  2017年4月25日 上午10:27:10
 */
public abstract class JSON implements JSONStreamAware, JSONAware{
	
	/**
	 * 默认的 时区
	 */
	public static TimeZone defaultTimeZone = TimeZone.getDefault();
	/**
	 * 默认系统语言环境
	 */
	public static Locale defaultLocale = Locale.getDefault();
	/**
	 * 默认编译后对象内存中标识
	 */
	public static String DEFAULT_TYPE_KEY = "@type";

	// 初始化, 默认解析特性
	public static int DEFAULT_PARSER_FEATURE;
	// 这里使用 | 来添加解析的默认特性, 因为mask 是每一个字段的一个位标, 比如 1000 | 0100 | 0010 = 1110
	static {
		int features = 0;
		features |= Feature.AutoCloseSource.getMask();
		features |= Feature.InternFieldNames.getMask();
		features |= Feature.UseBigDecimal.getMask();
		features |= Feature.AllowUnQuotedFieldNames.getMask();
		features |= Feature.AllowSingleQuotes.getMask();
		features |= Feature.AllowArbitrayCommas.getMask();
		features |= Feature.SortFeidFastMatch.getMask();
		features |= Feature.IgnoreNotMatch.getMask();
		DEFAULT_PARSER_FEATURE = features;
	}
	
	public static int DEFAULT_GENERATE_FEATURE;
	static {
		int features = 0;
		features |= SerializerFeature.QuoteFieldNames.getMask();
		features |= SerializerFeature.SkipTransientField.getMask();
		features |= SerializerFeature.WriteEnumUsingName.getMask();
		features |= SerializerFeature.SortField.getMask();
		
		{
			// 检测本地是否有myfastjsonerializerFeatures.MapSortField 这个字段
			String featuresProperty = IOUtils.getStringProperty("myfastjsonerializerFeatures.MapSortField");
			int mask = SerializerFeature.MapSortField.getMask();
			if ("true".equals(featuresProperty)){
				features |= mask;
			} else if ("false".equals(featuresProperty)){
				features &= ~mask;
			}
		}
		DEFAULT_GENERATE_FEATURE = features;
	}
	
	public static String getName(){
		return "刘东";
	}
	
	@Override
	public String toString() {
		return toJSONString();
	}
	
	@Override
	public String toJSONString() {
		return null;
	}
	
	@Override
	public void writeJSONString(Appendable out) {
		
	}
	
}
