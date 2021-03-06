package com.dongdongxia.myfastjson.serializer;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.TimeZone;

import com.dongdongxia.myfastjson.JSON;

/**
 * 
 * <P>Description: JSON序列化控制器</P>
 * @ClassName: JSONSerializer
 * @author java_liudong@163.com  2017年4月28日 下午2:14:19
 */
public class JSONSerializer extends SerializerFilterable{

	/** 两个主要对象, 用来控制配置和输出, 在构造方法中初始化*/
	protected final SerializeConfig config;
	public final SerializeWriter out;

	private String dateFormatPattern; // 日式格式化模板
	private DateFormat dateFormat; // 日期格式化对象
	
	private int indentCount = 0; // 缩进的格式
	private String indent = "\t"; // 缩进 是一个Tab 键
	
	/**
	 * 多个引用
	 */
	protected IdentityHashMap<Object, SerialContext> references = null;
	protected SerialContext context;
	
	/**
	 * 获取默认时区
	 */
	protected TimeZone timeZone = JSON.defaultTimeZone;
	/**
	 * 获取默认的环境
	 */
	protected Locale locale = JSON.defaultLocale;
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: 初始化默认的 写入 和 全局配置的对象</p>
	 */
	public JSONSerializer() {
		this(new SerializeWriter(), SerializeConfig.getGlobalConfig());
	}
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: 指定输出构造方法</p>
	 * @param out 序列输出
	 */
	public JSONSerializer(SerializeWriter out) {
		this(out, SerializeConfig.getGlobalConfig());
	}
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: 指定全局配置的构造方法</p>
	 * @param config 全局配置
	 */
	public JSONSerializer(SerializeConfig config) {
		this(new SerializeWriter(), config);
	}
	
	
	
	public JSONSerializer(SerializeWriter out, SerializeConfig config) {
		this.out = out;
		this.config = config;
	}
	
	/**
	 * 
	 * <p>Title: getDateFormatPattern</p>
	 * <p>Description: 获取日期格式化的模式</p>
	 * @return
	 * @author java_liudong@163.com  2017年5月27日 上午10:19:26
	 */
	public String getDateFormatPattern() {
		if (dateFormat instanceof SimpleDateFormat) {
			return ((SimpleDateFormat) dateFormat).toPattern(); // 返回类似: yyyy-MM-dd HH:mm:ss  如果指定勒的话
		}
		return dateFormatPattern; // 没有的话, 就返回指定的 格式化模式
	}
	
	/**
	 * 
	 * <p>Title: getDateFormat</p>
	 * <p>Description: 获取日期格式化对象, 没有的话, 就初始化</p>
	 * @return
	 * @author java_liudong@163.com  2017年5月27日 上午10:27:45
	 */
	public DateFormat getDateFormat() {
		if (dateFormat == null) {
			if (dateFormatPattern != null) {
				dateFormat = new SimpleDateFormat(dateFormatPattern, locale);
				dateFormat.setTimeZone(timeZone);
			}
		}
		return dateFormat;
	}
	
	/**
	 * 
	 * <p>Title: setDateFormat</p>
	 * <p>Description: 设置日期格式化对象</p>
	 * @param dateFormat
	 * @author java_liudong@163.com  2017年5月27日 上午10:36:48
	 */
	public void setDateFormat(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
		if (dateFormatPattern != null) {
			dateFormatPattern = null;
		}
	}
	
	/**
	 * 
	 * <p>Title: setDateFormat</p>
	 * <p>Description: 设置日期格式模式</p>
	 * @param dateFormat
	 * @author java_liudong@163.com  2017年5月27日 上午10:42:03
	 */
	public void setDateFormat(String dateFormat) {
		this.dateFormatPattern = dateFormat;
		if (this.dateFormat != null) {
			this.dateFormat = null;
		}
	}
	
	public SerialContext getContext() {
		return context;
	}

	public void setContext(SerialContext context) {
		this.context = context;
	}
	
	/**
	 * 
	 * <p>Title: setContext</p>
	 * <p>Description: 设置SerialContext 初始化的方法</p>
	 * @param parent
	 * @param object
	 * @param fieldName
	 * @param features
	 * @author java_liudong@163.com  2017年5月27日 上午10:47:52
	 */
	public void setContext(SerialContext parent, Object object, Object fieldName, int features) {
		this.setContext(parent, object, fieldName, features, 0);
	}
	
	public void setContext(SerialContext parent, Object object, Object fieldName, int features, int fieldFeatures) {
		if (out.disableCircularReferenceDetect) {
			return ;
		}
		
		this.context = new SerialContext(parent, object, fieldName, features, fieldFeatures); // 初始化 SerialContext对象
		
		if (references == null) { // 为空,就初始化, 不然, 就开始装对象吧
			references = new IdentityHashMap<Object, SerialContext>();
		}
		this.references.put(object, context); // object 就是当前序列化的对象
	}
	
	
	/**
	 * 
	 * <p>Title: setContext</p>
	 * <p>Description: 设置SerialContext 并设置的是父类</p>
	 * @param object
	 * @param fieldName
	 * @author java_liudong@163.com  2017年5月27日 上午10:56:11
	 */
	public void setContext(Object object, Object fieldName) {
		this.setContext(context, object, fieldName, 0);
	}
	
	/**
	 * 
	 * <p>Title: popContext</p>
	 * <p>Description: 将父类的SerialContext 赋值 到当前的SerialContext 的变量上</p>
	 * @author java_liudong@163.com  2017年5月27日 上午10:58:58
	 */
	public void popContext() {
		if (context != null) {
			this.context = this.context.parent;
		}
	}
	
	/**
	 * 
	 * <p>Title: isWriteClassName</p>
	 * <p>Description: 检测,是否写入对象的类名</p>
	 * @param fieldType
	 * @param obj
	 * @return
	 * @author java_liudong@163.com  2017年5月27日 上午11:00:53
	 */
	public final boolean isWriteClassName(Type fieldType, Object obj) {
		return out.isEnable(SerializerFeature.WriteClassName)   
				&& (fieldType != null || (out.isEnable(SerializerFeature.NotWriteRootClassName)) || context.parent != null);
	}
	
	
	/**
	 * 
	 * <p>Title: containsReference</p>
	 * <p>Description: 容器中是有指定key的值</p>
	 * @param value key
	 * @return
	 * @author java_liudong@163.com  2017年5月27日 上午11:06:09
	 */
	public boolean containsReference(Object value) {
		if (references == null) {
			return false;
		}
		
		SerialContext refContext = references.get(value);
		if (refContext == null) {
			return false;
		}
		
		Object fieldName = refContext.fieldName;
		
		return fieldName == null || fieldName instanceof Integer || fieldName instanceof String; // 
	}
	
	/**
	 * 
	 * <p>Title: writeReference</p>
	 * <p>Description: 写入引用</p>
	 * @param object
	 * @author java_liudong@163.com  2017年5月27日 下午2:10:49
	 */
	public void writeReference(Object object) {
		SerialContext context = this.context;
		Object current = context.object; // 当前对象
		
		if (object == current) {
			out.write("{\"$ref\":\"@\"}"); // {"$ref":"@"}
			return ;
		}
		
		SerialContext parentContext = context.parent; // 获取父类
		
		if (parentContext != null) {
			if (object == parentContext.object) {
				out.write("\"$ref\":\"..\""); // "$ref":".."
				return ;
			}
		}
		
		
		SerialContext rootContext = context;
		for (;;) {
			if (rootContext.parent == null) {
				break ;
			}
			rootContext = rootContext.parent;
		}
		
		if (object == rootContext.object) {
			out.write("{\"$ref\":\"$\"}"); // {"$ref":"$"}
		} else {
			out.write("{\"$ref\":\""); // {"$ref":"}
			out.write(references.get(object).toString());  // 值
			out.write("\"}"); // "}
		}
	}
	
	/**
	 * 
	 * <p>Title: checkValue</p>
	 * <p>Description: TODO</p>
	 * @param filterable
	 * @return
	 * @author java_liudong@163.com  2017年6月1日 下午3:23:32
	 */
	public boolean checkValue(SerializerFilterable filterable) {
		return (valueFilters != null && valueFilters.size() > 0) // 检测,值过滤器是否存在
				|| (contextValueFilters != null && contextValueFilters.size() > 0)
				|| (filterable.valueFilters != null && filterable.valueFilters.size() > 0)
				|| (filterable.contextValueFilters != null && filterable.contextValueFilters.size() > 0)
				|| out.writeNonStringValueAsString; // 是否写入字符串
	}
	
	
	/**
	 * 
	 * <p>Title: hasNameFilters</p>
	 * <p>Description: TODO</p>
	 * @param filterable
	 * @return
	 * @author java_liudong@163.com  2017年6月1日 下午3:29:20
	 */
	public boolean hasNameFilters(SerializerFilterable filterable) {
		return (nameFilters != null && nameFilters.size() > 0) || (filterable.nameFilters != null && filterable.nameFilters.size() > 0);
	}
	
	public int getIndentCount() {
		return indentCount;
	}
	
	public void incrementIndent() {
		indentCount++;
	}
	
	public void decrementIndent() {
		indentCount--;
	}
	
	/**
	 * 
	 * <p>Title: println</p>
	 * <p>Description: 输出\n 用来输出格式化</p>
	 * @author java_liudong@163.com  2017年6月1日 下午3:35:57
	 */
	public void println() {
		out.write('\n');
		for (int i = 0; i < indentCount; ++i) {
			out.write(indent);
		}
	}
	
	/**
	 * 
	 * <p>Title: getWriter</p>
	 * <p>Description: 获取输出流</p>
	 * @return
	 * @author java_liudong@163.com  2017年6月1日 下午3:39:33
	 */
	public SerializeWriter getWriter() {
		return out;
	}
	
	/**
	 * 
	 * <p>Title: toString</p>
	 * <p>Description: 输出为out格式化后的数据</p>
	 * @return
	 * @author java_liudong@163.com  2017年6月1日 下午3:40:37
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return out.toString();
	}
	
	/**
	 * 
	 * <p>Title: config</p>
	 * <p>Description: 配置输出流中的功能</p>
	 * @param feature 功能
	 * @param state true为添加,false为移除
	 * @author java_liudong@163.com  2017年6月1日 下午3:42:51
	 */
	public void config(SerializerFeature feature, boolean state) {
		out.config(feature, state);
	}
	
	/**
	 * 
	 * <p>Title: isEnabled</p>
	 * <p>Description: 检测输出流中,是否有指定的功能</p>
	 * @param feature 检测的功能
	 * @return
	 * @author java_liudong@163.com  2017年6月1日 下午3:44:40
	 */
	public boolean isEnabled(SerializerFeature feature) {
		return out.isEnable(feature);
	}
	
	/**
	 * 
	 * <p>Title: writeNull</p>
	 * <p>Description: 输出Null</p>
	 * @author java_liudong@163.com  2017年6月1日 下午3:46:17
	 */
	public void writeNull() {
		this.out.writeNull();
	}
	
	/**
	 * 
	 * <p>Title: getMapping</p>
	 * <p>Description: 获取输出配置对象</p>
	 * @return
	 * @author java_liudong@163.com  2017年6月1日 下午3:47:25
	 */
	public SerializeConfig getMapping() {
		return config;
	}
	
	/**
	 * 
	 * <p>Title: write</p>
	 * <p>Description: 向输出流中输出对象</p>
	 * @param object
	 * @author java_liudong@163.com  2017年6月1日 下午4:05:35
	 */
	public final void write(Object object) {
		
	}
	
	
	protected final void writeKeyValue(char seperator, String key, Object value){
		
	}
	
	/**
	 * 
	 * <p>Title: writeWithFormat</p>
	 * <p>Description: 将序列化对象,进行日期格式化</p>
	 * @param object
	 * @param format
	 * @author java_liudong@163.com  2017年6月12日 上午11:47:36
	 */
	public final void writeWithFormat(Object object, String format) {
		if (object instanceof Date) {
			DateFormat dateFormat = this.getDateFormat();
			if (dateFormat == null) {
				dateFormat = new SimpleDateFormat(format, locale);
				dateFormat.setTimeZone(timeZone);
			}
			String text = dateFormat.format((Date)object);
			out.writeString(text);
			return ;
		}
		write(object);
	}
	
	/**
	 * 
	 * <p>Title: write</p>
	 * <p>Description: 向流对象中输入文本</p>
	 * @param text
	 * @author java_liudong@163.com  2017年6月8日 上午10:45:25
	 */
	public final void write(String text) {
		StringCodec.instance.write(this, text);
	}
	
	/**
	 * 
	 * <p>Title: getObjectWriter</p>
	 * <p>Description: 获取指定对象的序列化实现对象</p>
	 * @param clazz 指定序列化对象
	 * @return
	 * @author java_liudong@163.com  2017年6月12日 上午11:01:18
	 */
	public ObjectSerializer getObjectWriter(Class<?> clazz) {
		return config.getObjectWriter(clazz);
	}
}
