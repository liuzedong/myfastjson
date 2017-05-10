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

	/**
	 * 将一个字符写入缓冲区中,其实,就是写入到 buf中
	 */
	@Override
	public void write(int c) {
		int newcount = count + 1;
		// 下面的判断是, 如果,新加 的长度, 如果查过原有的容器大小, 且 没有writer那么就扩容, 不然,就写出,然后再扩容
		if (newcount > buf.length) {
			if (writer == null) {
				expandCapacity(newcount);
			} else {
				flush();
				newcount = 1;
			}
		}
		buf[count] = (char)c; // 在数组中添加一个字符
		count = newcount; // 将计算出来的, 数组长度, 复制给count
	}
	
	/**
	 * 写入c[]数组, 从指定的位置,到指定的长度
	 * c 写入内容
	 * off 起始位置
	 * len 总长度
	 */
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
			if (writer == null) { // 没有writer对象, 且保存的内容, 大于 数组的容量, 扩容吧
				expandCapacity(newcount);
			} else {
				// 有Writer的话, 那么就先写出去吧
				do {
					// buf.length 总容量, count 使用容量, rest 未使用容量
					int rest = buf.length - count;
					// 将方法中的请求的c[] 指定的off 位置开始, 复制到, buf的 count 开始, 然后到剩余的容量里面
					/** 比如: buf.leng=10, c.leng=5,count=5, c的off是2, 
					 * buf[0]=刘,buf[1]=话,buf[2]=西,buf[3]=钱,buf[4]=o,buf[5]=null,buf[6]=null,buf[7]=null,buf[8]=null,buf[9]=null
					 * c[0]=阿,c[1]=阿,c[2]=阿,c[3]=阿,c[4]=阿
					 * 则rest = 10 - 5 = 5
					 * buf[0]=刘,buf[1]=话,buf[2]=西,buf[3]=钱,buf[4]=o,buf[5]=阿,buf[6]=阿,buf[7]=阿,buf[8]=null,buf[9]=null
					 */
					System.arraycopy(c, off, buf, count, rest);
					// 将复制后的 缓冲区, buf的长度, 赋值给count
					count = buf.length;
					// buf 缓冲区的内容写出去
					flush();
					// len 要复制的总容量, 算出来还剩多少没有复制过去
					len -= rest;
					// off 是复制的起始位置, 将位置添加上已经复制的长度,等于现在的大小
					off += rest;
				} while (len > buf.length); // 每2048 个字节,就先写出去
				newcount = len;
			}
		}
		// 将剩余的 都拷贝到 buf中
		System.arraycopy(c, off, buf, count, len);
		count = newcount;
	}
	
	/**
	 * 
	 * <p>Title: write</p>
	 * <p>Description: TODO</p>
	 * @param str 写入的字符串
	 * @param off 写入的起始位置
	 * @param len 写入的长度
	 * @author java_liudong@163.com  2017年5月10日 上午11:13:06
	 * @see java.io.Writer#write(java.lang.String, int, int)
	 */
	@Override
	public void write(String str, int off, int len) {
		int newcount = count + len;
		// 长度不够, 那么就扩容, 有writer, 则先把2048的字节写出
		if (newcount > buf.length) {
			if (writer == null) {
				expandCapacity(newcount);
			} else {
				do {
					// 2048 - 48 = 2000
					int rest = buf.length - count;
					// 将str的 信息, 底层, 起始 就是  System.arraycopy(Object src,  int  srcPos, Object dest, int destPos, int length);
					// 0 , 2000, buf, 48    count, 开始写入的位置, 下面的方法, 其实,就是buf 的2048 给填满啦
					str.getChars(off, off + rest, buf, count);
					count = buf.length; // 因为填满啦, 所以此处的count= 2048, 如果指定勒大小,就是initCount的值
					flush();
					len -= rest; // 剩下的长度
					off += rest; // 剩下的起始位置
				} while (len > buf.length);
				newcount = len;
			}
		}
		// 第二个参数, 为 写入的 结束位置, 不是 写入的长度
		str.getChars(off, off + len, buf, count);
		count = newcount;
	}

	/**
	 * 
	 * <p>Title: write</p>
	 * <p>Description: 写入一个字符串</p>
	 * @param str
	 * @author java_liudong@163.com  2017年5月10日 上午11:41:21
	 * @see java.io.Writer#write(java.lang.String)
	 */
	@Override
	public void write(String text) {
		if (text == null) {
			writeNull();
			return;
		}
		
		write(text, 0, text.length());
	}
	
	/**
	 * 
	 * <p>Title: writeNull</p>
	 * <p>Description: 写入null 字符串, 用在, 如果字符串对象为NULL 的情况</p>
	 * @author java_liudong@163.com  2017年5月10日 上午11:42:22
	 */
	public void writeNull() {
		write("null");
	}
	
	/**
	 * 
	 * <p>Title: append</p>
	 * <p>Description: 缓存中追加字符串</p>
	 * @param csq 追加的字符串
	 * @return
	 * @author java_liudong@163.com  2017年5月10日 上午11:46:20
	 * @see java.io.Writer#append(java.lang.CharSequence)
	 */
	@Override
	public SerializeWriter append(CharSequence csq) {
		String s = (csq == null ? "null" : csq.toString());
		write(s, 0, s.length());
		return this;
	}
	
	
	@Override
	public void flush() {
		if (writer == null) {
			return ;
		}
		
		try {
			writer.write(buf, 0, count); // 将将数组中的内容,全部写出去
			writer.flush();
		} catch (IOException e) {
			throw new JSONException(e.getMessage(), e);
		}
		// 将count 清零
		count = 0;
	}

	@Override
	public void close() {
		// 不为空, 先将 数据进行 写出,然后在关闭
		if (writer != null && count > 0) {
			flush();
		}
		if (buf.length <= 1024 * 64) { // 检测 ,入过 buf 小于64M , 那么就将 数据保存到 本地的一个缓存中. bufLocal中
			bufLocal.set(buf);
		}
		this.buf = null; // 然后,将数组清空
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

	/**
	 * toString, 将缓存中的数据, 进行输出
	 */
	@Override
	public String toString() {
		return new String(buf, 0, count);
	}
	
}
