package com.dongdongxia.myfastjson.serializer;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
	
	
	protected final void writeKeyValue(char seperator, String key, Object value){
		
	}
	
}
