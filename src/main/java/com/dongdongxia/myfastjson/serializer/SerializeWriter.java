package com.dongdongxia.myfastjson.serializer;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
/**
 * 
 * <P>Description: 序列化输出</P>
 * @ClassName: SerializeWriter
 * @author java_liudong@163.com  2017年4月25日 上午11:12:12
 */
public final class SerializeWriter extends Writer{

	private final static Charset UTF8 = Charset.forName("UTF-8");
	
	private final static ThreadLocal<char[]> bufLocal = new ThreadLocal<char[]>();
	private final static ThreadLocal<byte[]> bytesBufLocal = new ThreadLocal<byte[]>();
	
	protected char buf[];
	
	protected int count;
	
	protected int features;
	
	private final Writer writer;
	
	/**以下定义字段, 检测序列化的时候, 是否包含这些功能*/
	protected boolean useSingleQuotes;
	protected boolean quoteFieldNames;
	protected boolean sortField;
	protected boolean disableCircularReferenceDetect;
	protected boolean beanToArray;
	protected boolean writeNonStringValueAsString;
	protected boolean notWriteDefaultValue;
	protected boolean writeEnumUsingName;
	protected boolean writeEnumUsingToString;
	protected boolean writeDirect;
	
	protected char keySeperator;
	
	public SerializeWriter(){
		this((Writer) null);
	}
	
	public SerializeWriter(Writer writer){
		this(writer, 1, SerializerFeature.EMPTY);
	}
	
	public SerializeWriter(SerializerFeature... features){
		this(null, features);
	}
	
	public SerializeWriter(Writer writer, SerializerFeature... features){
		this(writer, 0, features);
	}
	
	public SerializeWriter(Writer writer, int defaultFeatures, SerializerFeature... features){
		this.writer = writer;
		
		buf = bufLocal.get();
		
		if (buf != null){
			bufLocal.set(null);
		} else {
			buf = new char[2048];
		}
		
		int featuresValue = defaultFeatures;
		
		for (SerializerFeature feature : features){
			featuresValue |= feature.getMask();
		}
		this.features = featuresValue;
		
		computeFeatures();
	}
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: 初始化输出字长</p>
	 * @param initalSize 指定缓冲大小
	 */
	public SerializeWriter(int initalSize) {
		this(null, initalSize);
	}
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: 初始化writer 和 buf  </p>
	 * @param writer
	 * @param initialSize
	 */
	public SerializeWriter(Writer writer, int initialSize) {
		this.writer = writer;
		// 说明入参 错误
		if (initialSize <= 0) {
			throw new IllegalArgumentException("Negative initial size: " + initialSize);
		}
		buf = new char[initialSize];
		
		// 此处不做, 序列化功能的 赋值
//		computeFeatures();
	}
	
	
	
	
	// 非直接的特性
	final static int nonDirectFeatures = 0
			| SerializerFeature.UseSingleQuotes.mask
			| SerializerFeature.BrowerSecure.mask
			| SerializerFeature.BrowerCompatible.mask
			| SerializerFeature.PrettyFormat.mask
			| SerializerFeature.WriteEnumUsingToString.mask
			| SerializerFeature.WriteNonStringValueAsString.mask
			| SerializerFeature.WriteSlashAsSpecial.mask
			| SerializerFeature.IgnoreErrorGetter.mask
			| SerializerFeature.WriteClassName.mask
			| SerializerFeature.NotWriteDefaultValue.mask
			;
	
	/**
	 * 
	 * <p>Title: computeFeatures</p>
	 * <p>Description: 检测,是否包含这写序列化的功能, 并赋值到改对象的 全局变量中
	 * 这里使用的是&, 清零的 作用, 如果为0, 则不包含这个功能</p>
	 * @author java_liudong@163.com  2017年4月28日 下午2:47:22
	 */
	protected void computeFeatures(){
		quoteFieldNames = (this.features & SerializerFeature.QuoteFieldNames.mask) != 0;
		useSingleQuotes = (this.features & SerializerFeature.UseSingleQuotes.mask) != 0;
		sortField = (this.features & SerializerFeature.SortField.mask) != 0;
		disableCircularReferenceDetect = (this.features & SerializerFeature.DisableCircularReferenceDetect.mask) != 0;
		beanToArray = (this.features & SerializerFeature.BeanToArray.mask) != 0;
		writeNonStringValueAsString = (this.features & SerializerFeature.WriteNonStringValueAsString.mask) != 0;
		notWriteDefaultValue = (this.features & SerializerFeature.NotWriteDefaultValue.mask) != 0;
		writeEnumUsingName = (this.features & SerializerFeature.WriteEnumUsingName.mask) != 0; 
		writeEnumUsingToString = (this.features & SerializerFeature.WriteEnumUsingToString.mask) != 0;
		
		// 计算出 直接特性
		writeDirect = quoteFieldNames 
				&& (this.features & nonDirectFeatures) == 0
				&& (beanToArray || writeEnumUsingName)
				;
		// 检测 使用是单引号  还是 双引号
		keySeperator = useSingleQuotes ? '\'' : '"';
	}
	
	@Override
	public void write(char[] c, int off, int len) {
		// 不符合写出规则的就抛出异常, 这些都是检测 字符是否越界
		if (off < 0 || off > c.length || len < 0 || off + len > c.length || off + len < 0){
			throw new IndexOutOfBoundsException();
		} else if (len == 0){
			return ;
		}
		
		int newcount = count + len;
		if (newcount > buf.length){
			
		}
	}

	@Override
	public void flush() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

}
