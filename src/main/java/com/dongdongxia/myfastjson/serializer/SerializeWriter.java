package com.dongdongxia.myfastjson.serializer;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;

import com.dongdongxia.myfastjson.JSONException;
/**
 * 
 * <P>Description: 序列化输出</P>
 * @ClassName: SerializeWriter
 * @author java_liudong@163.com  2017年4月25日 上午11:12:12
 */
public final class SerializeWriter extends Writer{

	private final static Charset UTF8 = Charset.forName("UTF-8");
	/** 在构造方法中初始化 **/
	private final static ThreadLocal<char[]> bufLocal = new ThreadLocal<char[]>();
	
	private final static ThreadLocal<byte[]> bytesBufLocal = new ThreadLocal<byte[]>();
	/** 在构造方法中初始化, 初始化缓冲区 **/
	protected char buf[];
	
	/** 记录的是buf中的总长度, 在每个write方法中都有增加**/
	protected int count;
	/** 在构造方法中初始化 **/
	protected int features;
	
	private final Writer writer;
	
	/**以下定义字段, 检测序列化的时候, 是否包含这些功能, 在构造方法中初始化*/
	protected boolean useSingleQuotes; // 使用单引号而不是双引号, 默认为false
	protected boolean quoteFieldNames; // 输出Key时是否使用双引号, 默认true
	protected boolean sortField; // 按字段名称排序后输出。默认为false
	protected boolean disableCircularReferenceDetect; // 32768, 消除对同一对象循环引用的问题，默认为false
	protected boolean beanToArray; // bean转换为Array数组
	protected boolean writeNonStringValueAsString;
	protected boolean notWriteDefaultValue; // 不写入默认值
	protected boolean writeEnumUsingName;
	protected boolean writeEnumUsingToString; // Enum输出name() 或者 original, 默认为false
	protected boolean writeDirect; // 几个属性的综合, 见下面的初始化方法
	
	protected char keySeperator; // 检测key 是使用 ' 还是" 来进行包起来
	
	protected int maxBufSize = -1; // 最大的缓存空间, 默认 为 -1, 就是没有啦, 构造方法没有初始化, 在指定方法中赋值
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: 默认构造方法</p>
	 */
	public SerializeWriter(){
		this((Writer) null);
	}
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: 构造方法, 添加默认的 流的特性, 1:SerializerFeature.QuoteFieldNames</p>
	 * <p>SerializerFeature.EMPTY, 这个是默认特性,就是啥都没有</p>
	 * @param writer 输出流
	 */
	public SerializeWriter(Writer writer){
		this(writer, 1, SerializerFeature.EMPTY);
	}
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: 构造方法, 初始化指定流的特性</p>
	 * @param features 输出流的特性, 多个使用 | 进行拼接, 组成
	 */
	public SerializeWriter(SerializerFeature... features){
		this(null, features);
	}
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: 构造方法,添加 指定的流, 添加 指定的特性</p>
	 * @param writer 指定输出流
	 * @param features 指定输出流的特性
	 */
	public SerializeWriter(Writer writer, SerializerFeature... features){
		this(writer, 0, features);
	}
	
	/**
	 * 
	 * <p>Title: Constructor</p>
	 * <p>Description: 给全局变量,进行赋值, 在下面各个方法中,使用到</p>
	 * @param writer 指定的输出流, Writer的子类,都行如 FileWriter等
	 * @param defaultFeatures 添加默认的属性, Features 的属性
	 * @param features 添加默认的属性,以数组形式表示 SerializerFeature 的属性
	 */
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

	/**
	 * 
	 * <p>Title: getMaxBufSize</p>
	 * <p>Description: 获取最大缓冲区大小</p>
	 * @return
	 * @author java_liudong@163.com  2017年5月9日 上午10:36:25
	 */
	public int getMaxBufSize() {
		return maxBufSize;
	}
	
	/**
	 * 
	 * <p>Title: setMaxBufSize</p>
	 * <p>Description: 设置最大缓冲区大小</p>
	 * @param maxBufSize 最大缓冲区大小
	 * @author java_liudong@163.com  2017年5月9日 上午10:37:31
	 */
	public void setMaxBufSize(int maxBufSize) {
		if (maxBufSize < this.buf.length) {
			throw new JSONException("must > " + buf.length);
		}
		this.maxBufSize = maxBufSize;
	}
	
	/**
	 * 
	 * <p>Title: getBufferLength</p>
	 * <p>Description: 获取缓冲区的长度, 没有设置缓冲去的长度, 因为,在构造方法中已经初始化啦</p>
	 * @return 缓冲区长度
	 * @author java_liudong@163.com  2017年5月9日 上午10:50:04
	 */
	public int getBufferLength() {
		return this.buf.length;
	}
	
	/**
	 * 
	 * <p>Title: isSortField</p>
	 * <p>Description: 检测是否 按字段名称排序后输出, 就是获取全局变量的值, 在构造方法中初始化的</p>
	 * @return
	 * @author java_liudong@163.com  2017年5月9日 上午10:54:36
	 */
	public boolean isSortField() {
		return this.sortField;
	}
	
	/**
	 * 
	 * <p>Title: isNotWriteDefaultValue</p>
	 * <p>Description: 检测是否 不写出默认值, 就是获取全局变量的值, 在构造方法中初始化的</p>
	 * @return
	 * @author java_liudong@163.com  2017年5月9日 上午10:54:52
	 */
	public boolean isNotWriteDefaultValue() {
		return this.notWriteDefaultValue;
	}
	
	/**
	 * 
	 * <p>Title: isEnable</p>
	 * <p>Description: 检测, 初始化的是否, 是否包含 该特性</p>
	 * @param feature 序列化特性
	 * @return 
	 * @author java_liudong@163.com  2017年5月9日 上午10:58:17
	 */
	public boolean isEnable(SerializerFeature feature) {
		/**
		 * 为0 则不包含
		 * 不为 0 则包含
		 * 情况一 :
		 * 1 1 1 1
		 * 0 1 0 0 &
		 * 0 1 0 0 不为0 , 所以包含
		 * 情况二 : 
		 * 0 1 1 1
		 * 1 0 0 0 &
		 * 0 0 0 0 为0 , 所以不包含
		 */
		return (this.features & feature.mask) != 0;
	}
	
	/**
	 * 
	 * <p>Title: isEnable</p>
	 * <p>Description: 检测是否, 包含多个字段</p>
	 * @param feature 多个特性
	 * @return
	 * @author java_liudong@163.com  2017年5月9日 上午11:06:00
	 */
	public boolean isEnable(int feature) {
		return (this.features & feature) != 0;
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

	/**
	 * 
	 * <p>Title: expandCapacity</p>
	 * <p>Description: 为buf 缓冲区进行扩充容量</p>
	 * @param minimumCapacity 扩充的最小容量
	 * @author java_liudong@163.com  2017年5月9日 上午11:30:38
	 */
	public void expandCapacity(int minimumCapacity) {
		// 扩展的容量, 必须不能大于 最大容量, maxBufSize 为指定 的一个最大值, 不指定则 为-1, 就可以无限扩大
		if (maxBufSize != -1 && minimumCapacity >= maxBufSize) {
			throw new JSONException("serialize exceeded MAX_OUTPUT_LENGTH = " + maxBufSize + ", minimumCapacity = " + minimumCapacity);
		}
		// 如果 buf 最开始 为 4 , 则 4 * 3 / 2 + 1 = 7; (x * 3) / 2 + 1; 是一个增函数, 增加速度比较一般, 所以扩容时候,比较好, 不会有太大的波动
		int newCapacity = (buf.length * 3) / 2 + 1;
		
		// 如果, 算出来还没有  , 最小的大, 那么就直接使用最小的值
		if (newCapacity < minimumCapacity) {
			newCapacity = minimumCapacity;
		}
		// 初始化, 扩容的 数组, 然后, 将数组, copy 过来, 使用新的 数组容器
		char newValue[] = new char[newCapacity];
		System.arraycopy(buf, 0, newValue, 0, count);
		buf = newValue;
	}
}
