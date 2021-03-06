package com.dongdongxia.myfastjson.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.List;

import com.dongdongxia.myfastjson.JSONException;
import com.dongdongxia.myfastjson.util.IOUtils;
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
	
	/**
	 * 
	 * <p>Title: config</p>
	 * <p>Description: 向该类中添加 序列化的特性 , 是添加还是移除</p>
	 * @param feature  某个特性
	 * @param state true 为添加, false 为移除
	 * @author java_liudong@163.com  2017年5月12日 下午3:42:37
	 */
	public void config(SerializerFeature feature, boolean state) {
		if (state) {
			features |= feature.getMask();
			// 下面两个枚举序列化不能共存, 所以, 必须移除其中一个 WriteEnumUsingName 和 WriteEnumUsingToString
			if (feature == SerializerFeature.WriteEnumUsingToString) {
				features &= ~SerializerFeature.WriteEnumUsingName.getMask();
			} else if (feature == SerializerFeature.WriteEnumUsingName) {
				features &= ~SerializerFeature.WriteEnumUsingToString.getMask();
			}
			
		} else {
			features &= ~feature.getMask();
		}
		
		computeFeatures();
	}
	
	
	// 非直接的特性
	final static int nonDirectFeatures = 0
			| SerializerFeature.UseSingleQuotes.mask
			| SerializerFeature.BrowserSecure.mask
			| SerializerFeature.BrowserCompatible.mask
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
	 * <p>Description: </p>
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
	 * <p>Title: writeTo</p>
	 * <p>Description: 输出到指定的输出流中</p>
	 * @param out 自定的输出流
	 * @throws IOException
	 * @author java_liudong@163.com  2017年5月10日 下午12:22:33
	 */
	public void writeTo(Writer out) throws IOException {
		// 是指,当前没有指定writer对象, 才能使用
		if (this.writer != null) {
			throw new UnsupportedOperationException("writer not null, 就是当前对象,有指定的 writer 对象");
		}
		// 将 缓存中的数据, 写出去
		out.write(buf, 0, count);
	}
	
	/**
	 * 
	 * <p>Title: writeToEx</p>
	 * <p>Description: 输出到指定的字节流中,并指定编码格式</p>
	 * @param out 字节流
	 * @param charset 编码格式
	 * @return 写出流的最后字节位置
	 * @author java_liudong@163.com  2017年5月10日 下午12:26:45
	 * @throws IOException 
	 */
	public int writeToEx(OutputStream out, Charset charset) throws IOException {
		if (this.writer != null) {
			throw new UnsupportedOperationException("write not null");
		}
		// 如果编码是UTF8,那么就直接 将数据输出到out字节流中
		if (charset == UTF8) {
			return encodeToUTF8(out);
		} else {
			byte[] bytes = new String(buf, 0, count).getBytes(charset);
			out.write(bytes);
			return bytes.length;
		}
	}
	
	/**
	 * 
	 * <p>Title: writeTo</p>
	 * <p>Description: 将缓冲中的数据, 输出到指定的字节流中</p>
	 * @param out 指定的字节流
	 * @param charsetName 编码格式
	 * @author java_liudong@163.com  2017年5月10日 下午12:24:48
	 */
	public void writeTo(OutputStream out, String charsetName) throws IOException {
		writeTo(out, Charset.forName(charsetName));
	}
	
	/**
	 * 
	 * <p>Title: writeTo</p>
	 * <p>Description: 通过字节流,写出缓存中的内容</p>
	 * @param out 字节流
	 * @param charset 指定编码集
	 * @throws IOException
	 * @author java_liudong@163.com  2017年5月11日 下午7:51:32
	 */
	public void writeTo(OutputStream out, Charset charset) throws IOException {
		writeToEx(out, charset);
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
	 * <p>Title: writeNull</p>
	 * <p>Description: 设置输出null 的属性, 比如,0 是否输出为null, false是否输出为null</p>
	 * @param feature 
	 * @author java_liudong@163.com  2017年5月12日 下午3:20:17
	 */
	public void writeNull(SerializerFeature feature) {
		writeNull(0, feature.mask);
	}
	
	/**
	 * 
	 * <p>Title: writeNull</p>
	 * <p>Description: 根据指定特性,向缓冲区中存入指定类型的空</p>
	 * @param beanFeatures
	 * @param feature
	 * @author java_liudong@163.com  2017年5月12日 下午3:10:07
	 */
	public void writeNull(int beanFeatures, int feature) {
		// 两个没有交集, 那么就输出null 字符串
		if ((beanFeatures & feature) == 0 && (this.features & feature) == 0) {
			writeNull();
			return ;
		}
		
		if (feature == SerializerFeature.WriteNullListAsEmpty.mask) { // 如果数组为空
			write("[]");
		} else if (feature == SerializerFeature.WriteNullStringAsEmpty.mask) { // 如果字符串为空
			write("");
		} else if (feature == SerializerFeature.WriteNullBooleanAsFalse.mask) { // Boolean值 为false
			write("false");
		} else if (feature == SerializerFeature.WriteNullNumberAsZero.mask) { // 如果数字为0
			write("0");
		} else { // 其他为null的情况, 都输出 null 字符串
			writeNull();
		}
	}
	
	/**
	 * 
	 * <p>Title: writeInt</p>
	 * <p>Description: 向缓冲中,添加一个int, 以每位 的字符存储</p>
	 * @param i int数据
	 * @author java_liudong@163.com  2017年5月12日 下午2:37:47
	 */
	public void writeInt(int i) {
		// 如果最小, 就直接把这个数字写出去, 这个数字,Int的最小值
		if (i == Integer.MIN_VALUE) {
			write("-2147483648");
			return ;
		}
		
		// +1 , 是因为负数, 前面有个负号(-)
		int size = (i < 0) ? IOUtils.stringSize(-i) + 1 : IOUtils.stringSize(i);
		
		int newcount = count + size; 
		if (newcount > buf.length) {
			if (writer == null) {
				expandCapacity(newcount);
			} else {
				char [] chars = new char[size];
				IOUtils.getChars(i, 0, chars);
				write(chars, 0, chars.length);
				return ;
			}
		}
		
		IOUtils.getChars(i, newcount, buf);
		count = newcount;
	}
	
	/**
	 * 
	 * <p>Title: writeByteArray</p>
	 * <p>Description: 此处是把byte[] 数组中的数据, 签名为Base64,进行装入到缓存中</p>
	 * @param bytes
	 * @author java_liudong@163.com  2017年5月16日 上午9:37:55
	 */
	public void writeByteArray(byte[] bytes) {
		int bytesLen =bytes.length;
		final char quote = useSingleQuotes ? '\'' : '"';
		
		if (bytesLen == 0) {
			// 为空,及直接 写入 '' 或者 ""
			String emptyString = useSingleQuotes ? "''" : "\"\"";
			write(emptyString);
			return ;
		}
		
		// 获取Base64 
		final char[] CA = IOUtils.CA;
		
		int eLen = (bytesLen / 3) * 3; // 算出高三为的值
		int charsLen = ((bytesLen - 1) / 3 + 1) << 2;   // 根据原始的数组长度, 算出base64 的长度
		
		int offset = count;
		int newcount = count + charsLen + 2; // 这个+2, 是加的 '' 或者 ""  这个两个 引号字符
		if (newcount > buf.length) {
			if (writer != null) {
				write(quote);
				
				for (int s = 0; s < eLen;) {
					int i = (bytes[s++] & 0xff) << 16 | (bytes[s++] & 0xff) << 8 | (bytes[s++] & 0xff);
					
					// 把三个字节变成四个字节
					write(CA[(i >>> 18) & 0x3f]);
					write(CA[(i >>> 12) & 0x3f]);
					write(CA[(i >>> 6) & 0x3f]);
					write(CA[i & 0x3f]);
				}
				
				// 编码 底二位的, 具体的编码规则, 百度就有
				int left = bytesLen = eLen;
				if (left > 0) {
					int i = ((bytes[eLen] & 0xff) << 10) | (left == 2 ? ((bytes[bytesLen - 1] & 0xff) << 2) : 0);
					
					// 设置后四尾, 最后一个是等于号
					write(CA[i >> 12]);
					write(CA[(i >>> 6) & 0x3f]);
					write(left == 2 ? CA[i & 0x3f] : '=');
					write('=');
				}
				write(quote);
				return ;
			}
			expandCapacity(newcount);
		}
		
		// 下面的是直接把, byte数组中的内容转换为Base64,写入到缓存中
		count = newcount;
		buf[offset++] = quote;
		
		// 编码24 bit
		for (int s = 0, d = offset; s < eLen; ) { 
			int i = (bytes[s++] & 0xff) << 16 | (bytes[s++] & 0xff) << 8 | (bytes[s++] & 0xff);
			
			// 把三个字节变成四个字节
			buf[d++] = CA[(i >>> 18) & 0x3f];
			buf[d++] = CA[(i >>> 12) & 0x3f];
			buf[d++] = CA[(i >>> 6) & 0x3f];
			buf[d++] = CA[i & 0x3f];
		}
		
		int left = bytesLen - eLen;
		if (left > 0) {
			int i = ((bytes[eLen] & 0xff) << 10) | (left == 2 ? ((bytes[bytesLen - 1] & 0xff) << 2) : 0);
			
			// 设置后四尾, 最后一个是等于号
			buf[newcount - 5] = CA[i >> 12];
			buf[newcount - 4] = CA[(i >>> 6) & 0x3f];
			buf[newcount - 3] = left == 2 ? CA[i & 0x3f] : '=';
			buf[newcount - 2] = '=';
		}
		buf[newcount - 1] = quote;
	}
	
	/**
	 * 
	 * <p>Title: writeFloat</p>
	 * <p>Description: 向缓存中存入float值</p>
	 * @param value
	 * @param checkWriteClassName
	 * @author java_liudong@163.com  2017年5月16日 上午10:36:35
	 */
	public void writeFloat(float value, boolean checkWriteClassName) {
		// 如果为空, 或者是无限大, 则显示null
		if (Float.isNaN(value) || Float.isInfinite(value)) {
			writeNull();
		} else {
			String floatText = Float.toString(value);
			if (isEnable(SerializerFeature.WriteNullNumberAsZero) && floatText.endsWith(".0")) {
				floatText = floatText.substring(0, floatText.length() - 2);
			}
			write(floatText);
			
			if (checkWriteClassName && isEnable(SerializerFeature.WriteClassName)) {
				write('F');
			}
		}
	}
	
	/**
	 * 
	 * <p>Title: writeDouble</p>
	 * <p>Description: 向缓存中写入Double的数值</p>
	 * @param doubleValue
	 * @param checkWriteCalssName
	 * @author java_liudong@163.com  2017年5月16日 上午10:43:24
	 */
	public void writeDouble(double doubleValue, boolean checkWriteClassName) {
		if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue)) {
			writeNull();
		} else {
			String doubleText = Double.toString(doubleValue);
			if (isEnable(SerializerFeature.WriteNullNumberAsZero) && doubleText.endsWith(".0")) {
				doubleText = doubleText.substring(0, doubleText.length() - 2);
			}
			
			write(doubleText);
			
			if (checkWriteClassName && isEnable(SerializerFeature.WriteClassName)) {
				write('D');
			}
		}
	}
	
	/**
	 * 
	 * <p>Title: writeEnum</p>
	 * <p>Description: 向缓存中存入 枚举</p>
	 * @param value
	 * @author java_liudong@163.com  2017年5月16日 上午10:49:08
	 */
	public void writeEnum(Enum<?> value) {
		if (value == null) {
			writeNull();
			return;
		}
		
		String strVal = null;
		if (writeEnumUsingName && !writeEnumUsingToString) {
			strVal = value.name();
		} else if (writeEnumUsingToString) {
			strVal = value.toString();
		}
		
		if (strVal != null) {
			char quote = isEnable(SerializerFeature.UseSingleQuotes) ? '\'' : '"';
			write(quote);
			write(strVal);
			write(quote);
		} else {
			writeInt(value.ordinal());
		}
	}
	
	/**
	 * 
	 * <p>Title: writeLong</p>
	 * <p>Description: 向缓存中写入long 数值</p>
	 * @param i
	 * @author java_liudong@163.com  2017年5月16日 上午10:54:02
	 */
	public void writeLong(long i) {
		// 检测,是否需要 双引号
		boolean needQuotationMark = isEnable(SerializerFeature.BrowserCompatible) 
															&& (!isEnable(SerializerFeature.WriteClassName))
															&& (i > 9007199254740991L || i < -9007199254740991L);
		
		if (i == Long.MIN_VALUE) {
			if (needQuotationMark) {
				write("\"-9223372036854775808\"");
			} else {
				write("-9223372036854775808");
			}
			return ;
		}
		
		int size = (i < 0) ? IOUtils.stringSize(-i) + 1 : IOUtils.stringSize(i);
		
		int newcount = count + size;
		if (needQuotationMark) newcount += 2; 
		if (newcount > buf.length) {
			if (writer == null) {
				expandCapacity(newcount);
			} else {
				char[] chars = new char[size];
				IOUtils.getChars(i, size, chars);
				if (needQuotationMark) {
					write('"');
					write(chars, 0, chars.length);
					write('"');
				} else {
					write(chars, 0, chars.length);
				}
				return ;
			}
		}
		
		if (needQuotationMark) {
			buf[count] = '"';
			IOUtils.getChars(i, newcount - 1, buf);
			buf[newcount - 1] = '"';
		} else {
			IOUtils.getChars(i, newcount, buf);
		}
		count = newcount;
	}
	
	/**
	 * 
	 * <p>Title: writeStringWithDoubleQuote</p>
	 * <p>Description: 向缓存中添加字符串,并添加双引号, 最后还添加一个分隔符
	 *  里面,使用到勒, 大量的重复代码, 主要, 是将字符, 转换为Unicode编码, 为主要功能
	 *  大量,的位移, 就是为了, 计算得到字符, 对应的Unicode编码</p>
	 * @param text
	 * @param seperator
	 * @author java_liudong@163.com  2017年5月18日 上午11:00:08 - 16:18:50
	 */
	public void writeStringWithDoubleQuote(String text, final char seperator) {
		/** 1, 入参为null 且 seperator = (char)0 的情况 */
		if (text == null) {
			writeNull();
			if (seperator != 0) {
				write(seperator);
			}
			return ;
		}
		
		/** 2, 计算出新的字符长度 */
		int len = text.length();
		int newcount = count + len + 2;
		if (seperator != 0) {
			newcount++;
		}
		
		/** 3, 如果长度不够, 就扩容,然后写入 */
		if (newcount > buf.length) {
			if (writer != null) {
				write('"');
				
				for (int i = 0; i < text.length(); ++i) {
					char ch = text.charAt(i);
					
					if (isEnable(SerializerFeature.BrowserSecure)) {
						if (!(ch >= '0' && ch <= '9') && !(ch >= 'a' && ch <= 'z') && !(ch >= 'A' && ch <= 'Z') && !(ch == ',') && !(ch == '.') && !(ch == '_')) {
							write('\\');
							write('u');
							write(IOUtils.DIGITS[(ch >>> 12) & 15]);
							write(IOUtils.DIGITS[(ch >>> 8) & 15]);
							write(IOUtils.DIGITS[(ch >>> 5) & 15]);
							write(IOUtils.DIGITS[ch & 15]);
							continue;
						}
					} else if (isEnable(SerializerFeature.BrowserCompatible)) {
						if (ch == '\b' || ch == '\f' || ch == '\n' || ch == '\r' || ch == '\t' || ch == '"' || ch == '/' || ch == '\\') {
							write('\\');
							write(IOUtils.replaceChars[(int) ch]);
							continue;
						}
						
						if (ch < 32) {
							write('\\');
							write('u');
							write('0');
							write('0');
							write(IOUtils.ASCII_CHARS[ch * 2]);
							write(IOUtils.ASCII_CHARS[ch * 2 + 1]);
							continue;
						}
						
						if (ch >= 127) {
							write('\\');
							write('u');
							write(IOUtils.DIGITS[(ch >>> 12) & 15]);
							write(IOUtils.DIGITS[(ch >>> 8) & 15]);
							write(IOUtils.DIGITS[(ch >>> 4) & 15]);
							write(IOUtils.DIGITS[ch & 15]);
							continue;
						}
					} else {
						if (ch < IOUtils.specicalFlags_doubleQuotes.length
								&& IOUtils.specicalFlags_doubleQuotes[ch] != 0
								|| (ch == '/' && isEnable(SerializerFeature.WriteSlashAsSpecial))) { // 是否写入特殊字符
							write('\\');
							if (IOUtils.specicalFlags_doubleQuotes[ch] == 4) {
								write('u');
								write(IOUtils.DIGITS[(ch >>> 12) & 15]);
								write(IOUtils.DIGITS[(ch >>> 8) & 15]);
								write(IOUtils.DIGITS[(ch >>> 4) & 15]);
								write(IOUtils.DIGITS[ch & 15]);
							} else {
								write(IOUtils.replaceChars[ch]);
							}
							continue;
						}
					}
					
					write(ch);
				}
				write('"');
				if (seperator != 0) {
					write(seperator);
				}
				return ;
			}
			expandCapacity(newcount);
		}
		
		// 如果长度足够, 就直接写入
		int start = count + 1;
		int end = start + len;
		
		buf[count] = '\"';
		text.getChars(0, len, buf, start);
		
		count = newcount;
		
		/** 4, 检测是否包含浏览器安全的特性 */
		if (isEnable(SerializerFeature.BrowserSecure)) { // 浏览器的安全性, 将入参中的特殊字符, 进行转译成为Unicode编码,除了数字,字母,,._其他都是
			int lastSpecialIndex = -1;  // 记录下最后一个字符, 到时候,从后向前转译
			
			for (int i = start; i < end; ++i) {
				char ch = buf[i];
				// 筛选的是除了 0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ,._  等都认为是特殊字符,
				// 都将转译成为Unicode编码
				if (!(ch >= '0' && ch <= '9') && !(ch >= 'a' && ch <= 'z') && !(ch >= 'A' && ch <= 'Z') && !(ch == ',') && !(ch == '.') && !(ch == '_')) {
					lastSpecialIndex = i;
					newcount += 5;
					continue;
				}
			}
			
			if (newcount > buf.length) {
				expandCapacity(newcount);
			}
			count = newcount;
			
			// 从后向前 进行 字符, 转Unicode
			for (int i = lastSpecialIndex; i >= start; --i) {
				char ch = buf[i];
				
				// 重复将, 所有的非Unicode字符,转换成为Unicode字符
				if (!(ch >= '0' && ch <= '9') && !(ch >= 'a' && ch <= 'z') && !(ch >= 'A' && ch <= 'Z') && !(ch == ',') && !(ch == '.') && !(ch == '_')) {
					System.arraycopy(buf, i + 1, buf, i + 6, end -i - 1); // 将转译的6个字符, 复制到后面, 比如"\u5218\u534E""\u003D", 将复制的向后移动
					buf[i] = '\\';
					buf[i + 1] = 'u';
					buf[i + 2] = IOUtils.DIGITS[(ch >>> 12) & 15];
					buf[i + 3] = IOUtils.DIGITS[(ch >>> 8) & 15];
					buf[i + 4] = IOUtils.DIGITS[(ch >>> 4) & 15];
					buf[i + 5] = IOUtils.DIGITS[(ch & 15)];
					end += 5;
				}
			}
			
			if (seperator != 0) {
				buf[count - 2] = '\"';
				buf[count - 1] = seperator;
			} else {
				buf[count - 1] = '\"';
			}
			return ;
		}
		
		/** 5, 检测是否包含浏览器,的兼容性的特性 */
		if (isEnable(SerializerFeature.BrowserCompatible)) { // 找出浏览器中的特殊字符, 然后进行转换成Unicode字符
			int lastSpecialIndex = -1;
			
			// 先算出, 转后后使用的 长度, 是否需要扩容
			for (int i = start; i < end; ++i) {
				char ch = buf[i];
				
				if (ch == '"' || ch == '/' || ch == '\\') {
					lastSpecialIndex = i;
					newcount += 1;
					continue;
				}
				
				if (ch == '\b' || ch == '\f' || ch == '\n' || ch == '\r' || ch == '\t') {
					lastSpecialIndex = i;
					newcount += 1;
					continue;
				}
				
				if (ch < 32) {
					lastSpecialIndex = i;
					newcount += 5;
					continue;
				}
				
				if (ch >= 127) {
					lastSpecialIndex = i;
					newcount += 5;
					continue;
				}
			}
			
			if (newcount > buf.length) {
				expandCapacity(newcount);
			}
			
			count = newcount;
			
			for (int i = lastSpecialIndex; i >= start; --i) {
				char ch = buf[i];
				
				if (ch == '\b' || ch == '\f' || ch == '\n' || ch == '\r' || ch == '\t') {
					System.arraycopy(buf, i + 1, buf, i + 2, end - i - 1);
					buf[i] = '\\';
					buf[i + 1] = IOUtils.replaceChars[(int) ch]; // 替换成为指定字符
					end += 1;
					continue;
				}
				
				if (ch == '"' || ch == '/' || ch == '\\') {
					System.arraycopy(buf, i + 1, buf, i + 2, end -i - 1);
					buf[i] = '\\';
					buf[i + 1] = ch;
					end += 1;
					continue;
				}
				
				if (ch < 32) {
					System.arraycopy(buf, i + 1, buf, i + 6, end - i -1);
					buf[i] = '\\';
					buf[i + 1] = 'u';
					buf[i + 2] = '0';
					buf[i + 3] = '0';
					buf[i + 4] = IOUtils.ASCII_CHARS[ch * 2];
					buf[i + 5] = IOUtils.ASCII_CHARS[ch * 2 + 1];
					end += 5;
					continue;
				}
				
				if (ch > 127) {
					System.arraycopy(buf, i + 1, buf, i + 6, end - i - 1);
					buf[i] = '\\';
					buf[i + 1] = 'u';
					buf[i + 2] = IOUtils.DIGITS[(ch >>> 12) & 15];
					buf[i + 3] = IOUtils.DIGITS[(ch >>> 8) & 15];
					buf[i + 4] = IOUtils.DIGITS[(ch >>> 4) & 15];
					buf[i + 5] = IOUtils.DIGITS[ch & 15];
					end += 5;
				}
			}
			
			if (seperator != 0) {
				buf[count - 2] = '\"';
				buf[count - 1] = seperator;
			} else {
				buf[count - 1] = '\"';
			}
			return ;
		}
		
		/** 6, 一下是啥都没有的特性, 直接进行直接特殊字符的特换 */
		int specialCount = 0; // 特殊字符个数
		int lastSpecialIndex = -1; // 最后一个特殊字符的位置
		int firstSpecialIndex = -1; // 第一个特殊字符的位置
		char lastSpecial = '\0'; //
		
		// 从前向后匹配
		for (int i = start; i < end; ++i) {
			char ch = buf[i];
			
			// \u2028 行分隔符 \u2029 段落分隔符, 是两个特殊字符
			if (ch == '\u2028' || ch == '\u2029') {
				specialCount++;
				lastSpecialIndex = i;
				lastSpecial = ch;
				newcount += 4;
				
				if (firstSpecialIndex == -1) {
					firstSpecialIndex = i;
				}
				continue;
			}
			
			if (ch >= ']') {
				// GB2312和ASCII 编码集的范围 0xA1 - 0xF7 0x00  -  0x7F
				if (ch >= 0x7F && ch < 0xA0) {
					if (firstSpecialIndex == -1) {
						firstSpecialIndex = i;
					}
					
					specialCount++;
					lastSpecialIndex = i;
					lastSpecial = ch;
					newcount += 4;
				}
				continue;
			}
			
			if (isSpecial(ch, this.features)) {
				specialCount++;
				lastSpecialIndex = i;
				lastSpecial = ch;
				
				if (ch < IOUtils.specicalFlags_doubleQuotes.length && IOUtils.specicalFlags_doubleQuotes[ch] == 4) {
					newcount += 4;
				}
				
				if (firstSpecialIndex == -1) {
					firstSpecialIndex = i;
				}
			}
		}
		
		// 检测特殊字符,是否超过容量的体积, 进行是否扩容
		if (specialCount > 0) {
			newcount += specialCount;
			if (newcount > buf.length) {
				expandCapacity(newcount);
			}
			count = newcount;
			
			// 检测, 特殊字符
			if (specialCount == 1) {
				if (lastSpecial == '\u2028') {
					int srcPos = lastSpecialIndex + 1;
					int destPos = lastSpecialIndex + 6;
					int LengthOfCopy = end - lastSpecialIndex - 1;
					System.arraycopy(buf, srcPos, buf, destPos, LengthOfCopy);
					buf[lastSpecialIndex] = '\\';
					buf[++lastSpecialIndex] = 'u';
					buf[++lastSpecialIndex] = '2';
					buf[++lastSpecialIndex] = '0';
					buf[++lastSpecialIndex] = '2';
					buf[++lastSpecialIndex] = '8';
				} else if (lastSpecial == '\u2029') {
					int srcPos = lastSpecialIndex + 1;
					int destPos = lastSpecialIndex + 6;
					int LengthOfCopy = end - lastSpecialIndex - 1;
					System.arraycopy(buf, srcPos, buf, destPos, LengthOfCopy);
					buf[lastSpecialIndex] = '\\';
					buf[++lastSpecialIndex] = 'u';
					buf[++lastSpecialIndex] = '2';
					buf[++lastSpecialIndex] = '0';
					buf[++lastSpecialIndex] = '2';
					buf[++lastSpecialIndex] = '9';
				} else {
					final char ch = lastSpecial;
					if (ch < IOUtils.specicalFlags_doubleQuotes.length && IOUtils.specicalFlags_doubleQuotes[ch] == 4) {
						int srcPos = lastSpecialIndex + 1;
						int destPos = lastSpecialIndex + 6;
						int LengthOfCopy = end - lastSpecialIndex - 1;
						System.arraycopy(buf, srcPos, buf, destPos, LengthOfCopy);
						
						int bufIndex = lastSpecialIndex;
						buf[bufIndex++] = '\\';
						buf[bufIndex++] = 'u';
						buf[bufIndex++] = IOUtils.DIGITS[(ch >>> 12) & 15];
						buf[bufIndex++] = IOUtils.DIGITS[(ch >>> 8) & 15];
						buf[bufIndex++] = IOUtils.DIGITS[(ch >>> 4) & 15];
						buf[bufIndex++] = IOUtils.DIGITS[ch & 15];
					} else {
						int srcPos = lastSpecialIndex + 1;
						int destPos = lastSpecialIndex + 2;
						int LengthOfCopy = end - lastSpecialIndex - 1;
						System.arraycopy(buf, srcPos, buf, destPos, LengthOfCopy);
						buf[lastSpecialIndex] = '\\';
						buf[++lastSpecialIndex] = IOUtils.replaceChars[(int) ch];
					}
				}
			} else if (specialCount > 1) { // 特殊字符 多于 1个以上
				int textIndex = firstSpecialIndex - start;
				int bufIndex = firstSpecialIndex;
				for (int i = textIndex; i < text.length(); ++i) {
					char ch = text.charAt(i);
					
					if (ch < IOUtils.specicalFlags_doubleQuotes.length 
							&& IOUtils.specicalFlags_doubleQuotes[ch] != 0
							&& (ch == '/' && isEnable(SerializerFeature.WriteSlashAsSpecial))) {
						buf[bufIndex++] = '\\';
						if (IOUtils.specicalFlags_doubleQuotes[ch] == 4) {
							buf[bufIndex++] = 'u';
							buf[bufIndex++] = IOUtils.DIGITS[(ch >>> 12) & 15];
							buf[bufIndex++] = IOUtils.DIGITS[(ch >>> 8) & 15];
							buf[bufIndex++] = IOUtils.DIGITS[(ch >>> 4) & 15];
							buf[bufIndex++] = IOUtils.DIGITS[ch & 15];
							end += 5;
						} else {
							buf[bufIndex++] = IOUtils.replaceChars[(int) ch];
							end++;
						}
					} else {
						if (ch == '\u2028' || ch == '\u2029') {
							buf[bufIndex++] = '\\';
							buf[bufIndex++] = 'u';
							buf[bufIndex++] = IOUtils.DIGITS[(ch >>> 12) & 15];
							buf[bufIndex++] = IOUtils.DIGITS[(ch >>> 8) & 15];
							buf[bufIndex++] = IOUtils.DIGITS[(ch >>> 4) & 15];
							buf[bufIndex++] = IOUtils.DIGITS[ch & 15];
						} else {
							buf[bufIndex++] = ch;
						}
					}
				}
			}
		}
		
		/** 7, 检测是否有分隔符, seperator */
		if (seperator != 0) {
			buf[count - 2] = '\"';
			buf[count - 1] = seperator;
		} else {
			buf[count - 1] = '\"';
		}
	}
	
	/**
	 * 
	 * <p>Title: isSpecial</p>
	 * <p>Description: 匹配特殊字符</p>
	 * @param ch
	 * @param features
	 * @return
	 * @author java_liudong@163.com  2017年5月18日 下午3:22:06
	 */
	static boolean isSpecial(char ch, int features) {
		if (ch == ' ') { // 32
			return false;
		}
		
		if (ch == '/') { //47
			return (features & SerializerFeature.WriteSlashAsSpecial.mask) != 0;
		}
		
		if (ch > '#' && ch != '\\') { // 35 && 92
			return false;
		}
		
		if (ch <= 0x1f || ch == '\\' || ch == '"') { // 31 || 92 || 34
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * 
	 * <p>Title: writeFieldNameDirect</p>
	 * <p>Description: 直接写字段名称, 入参为 passwd, 则写入的是 "passwd":</p>
	 * @param text 字段名
	 * @author java_liudong@163.com  2017年5月18日 下午4:56:38
	 */
	public void writeFieldNameDirect(String text) {
		int len = text.length();
		int newcount = count + len + 3;
		
		if (newcount >  buf.length) {
			expandCapacity(newcount);
		}
		
		int start = count + 1;
		
		buf[count] = '\"';
		text.getChars(0, len, buf, start);
		
		count = newcount;
		buf[count - 2] = '\"';
		buf[count - 1] = ':';
	}
	
	/**
	 * 
	 * <p>Title: write</p>
	 * <p>Description: 向缓存中存入List<String> 数组, 如果有中文或者一些特殊字符, 就会转换成为 Unicode编码</p>
	 * @param list
	 * @author java_liudong@163.com  2017年5月18日 下午5:02:35
	 */
	public void write(List<String> list) {
		if (list.isEmpty()) {
			write("[]");
			return ;
		}
		
		int offset = count;
		final int initOffset = offset;
		for (int i = 0, list_size = list.size(); i < list_size; ++i) {
			String text = list.get(i);
			
			boolean hasSpecial = false; // 特殊的has值
			if (text == null) {
				hasSpecial = true;
			} else {
				for (int j = 0, len = text.length(); j < len; ++j) { // 获取 每个List中的 元素, 就是字符串, 字符串中的每个字符
					char ch = text.charAt(j);
					if (hasSpecial = (ch < ' ' || ch > '~' || ch == '"' || ch == '\\')) { // 检测, 这些范围內的字符, 一般中文字符的话, 就会为true
						break ;
					}
				}
			}
			
			if (hasSpecial) {
				count = initOffset;
				write('[');
				for (int j = 0; j < list.size(); ++j) {
					text = list.get(j);
					if (j != 0) {
						write(',');
					}
					
					if (text == null) {
						write("null");
					} else {
						writeStringWithDoubleQuote(text, (char) 0);
					}
				}
				write(']');
				return ;
			}
			
			int newcount = offset + text.length() + 3;
			if (i == list.size() - 1) {
				newcount++;
			}
			if (newcount > buf.length) {
				count = offset;
				expandCapacity(newcount);
			}
			
			if (i == 0) {
				buf[offset++] = '[';
			} else {
				buf[offset++] = ',';
			}
			buf[offset++] = '"';
			text.getChars(0, text.length(), buf, offset);
			offset += text.length();
			buf[offset++] = '"';
		}
		buf[offset++] = ']';
		count = offset;
	}
	
	/**
	 * 
	 * <p>Title: write</p>
	 * <p>Description: 向缓存中,添加 boolean值的字符串</p>
	 * @param value
	 * @author java_liudong@163.com  2017年5月19日 上午10:57:26
	 */
	public void write(boolean value) {
		if (value) {
			write("true");
		} else {
			write("false");
		}
	}
	
	
	/**
	 * 
	 * <p>Title: writeKeyWithSingleQuoteIfHasSpecial</p>
	 * <p>Description: 给入参添加单引号, 比如 passwd , 缓存中就是 'passwd'</p>
	 * @param text
	 * @author java_liudong@163.com  2017年5月19日 上午9:14:14
	 */
	private void writeKeyWithSingleQuoteIfHasSpecial(String text) {
		final byte[] specicalFlags_singleQuotes = IOUtils.specicalFlags_singleQuotes;
		
		/**1, 计算出长度*/
		int len = text.length();
		int newcount = count + len + 1;
		
		/**2, 长度超过缓存就扩容*/
		if (newcount > buf.length) {
			if (writer != null) {
				if (len == 0) {
					write('\'');
					write('\'');
					write(':');
					return ;
				}
				
				// 入参有值,不为空或"" 的情况
				boolean hasSpecial = false;
				for (int i = 0; i < len; ++i) {
					char ch = text.charAt(i);
					if (ch < specicalFlags_singleQuotes.length && specicalFlags_singleQuotes[ch] != 0) { // 中文之类的
						hasSpecial =true;
						break ;
					}
				}
				
				if (hasSpecial) {
					write('\'');
				}
				
				for (int i = 0; i < len; ++i) {
					char ch = text.charAt(i);
					if (ch < specicalFlags_singleQuotes.length && specicalFlags_singleQuotes[ch] != 0) { // 
						write('\\');
						write(IOUtils.replaceChars[(int) ch]);
					} else {
						write(ch);
					}
				}
				if (hasSpecial) {
					write('\'');
				}
				write(':');
				return ;
			}
			expandCapacity(newcount);
		}
		
		/**3, 未超过长度的, 进行写入*/
		if (len == 0) { // 没有字符串
			int newCount = count + 3;
			if (newCount > buf.length) {
				expandCapacity(count + 3);
			}
			buf[count++] = '\'';
			buf[count++] = '\'';
			buf[count++] = ':';
			return ;
		}
		
		int start = count;
		int end = start + len;
		
		text.getChars(0, len, buf, start);
		count = newcount;
		
		// 下面的是检测是否需要字符转译Unicode
		boolean hasSpecial = false;
		for (int i = start; i < end; ++i) {
			char ch = buf[i];
			if (ch < specicalFlags_singleQuotes.length && specicalFlags_singleQuotes[ch] != 0) {
				if (!hasSpecial) {
					newcount += 3;
					if (newcount > buf.length) {
						expandCapacity(newcount);
					}
					count = newcount;
					
					System.arraycopy(buf, i + 1, buf, i + 3, end - i - 1);
					System.arraycopy(buf, 0, buf, 1, i);
					buf[start] = '\'';
					buf[++i] = '\\';
					buf[++i] = IOUtils.replaceChars[(int) ch];
					end += 2;
					buf[count - 2] = '\'';
					
					hasSpecial = true;
				} else {
					newcount++;
					if (newcount > buf.length) {
						expandCapacity(newcount);
					}
					count = newcount;
					
					System.arraycopy(buf, i + 1, buf, i + 2, end - i);
					buf[i] = '\\';
					buf[++i] = IOUtils.replaceChars[(int) ch];
					end++;
				}
			}
		}
		
		buf[newcount - 1] = ':';
	}
	
	/**
	 * 
	 * <p>Title: writeFieldName</p>
	 * <p>Description: 向缓存中添加字段名, 默认不检测, 不转译</p>
	 * @param key
	 * @author java_liudong@163.com  2017年5月19日 上午10:49:48
	 */
	public void writeFieldName(String key) {
		writeFieldName(key, false);
	}
	
	/**
	 * 
	 * <p>Title: writeFieldName</p>
	 * <p>Description: 写入key值, 就是 类的字段名称</p>
	 * @param key 字段名
	 * @param checkSpecial 是否转译
	 * @author java_liudong@163.com  2017年5月19日 上午10:34:44
	 */
	public void writeFieldName(String key, boolean checkSpecial) {
		if (key == null) {
			write("null:");
			return ;
		}
		
		if (useSingleQuotes) { // 是否使用单引号
			if (quoteFieldNames) { // 是否在后面, 添加冒号 :
				writeStringWithSingleQuote(key);
				write(':');
			} else {
				writeKeyWithSingleQuoteIfHasSpecial(key);
			} 
		} else {
			if (quoteFieldNames) {
				writeStringWithDoubleQuote(key, ':');
			} else {
				boolean hashSpecial = key.length() == 0;
				for (int i = 0; i < key.length(); ++i) {
					char ch = key.charAt(i);
					if (SerializeWriter.isSpecial(ch, 0)) {
						hashSpecial = true;
						break ;
					}
				}
				if (hashSpecial) {
					writeStringWithDoubleQuote(key, ':');
				} else {
					write(key);
					write(':');
				}
			}
		}
	}
	
	
	
	/**
	 * 
	 * <p>Title: writeStringWithSingleQuote</p>
	 * <p>Description: 向缓存中,添加引号 包装的 字符串</p>
	 * @param text 字符串
	 * @author java_liudong@163.com  2017年5月19日 上午9:49:30
	 */
	protected void writeStringWithSingleQuote(String text) {
		// 如果为null的情况
		if (text == null) {
			int newcount = count + 4;
			if (newcount > buf.length) {
				expandCapacity(newcount);
			}
			"null".getChars(0, 4, buf, count);
			count = newcount;
			return ;
		}
		
		// 如果有值的情况
		int len = text.length();
		int newcount = count + len + 2;
		if (newcount > buf.length) {
			if (writer != null) {
				write('\'');
				for (int i = 0; i < text.length(); ++i) {
					char ch = text.charAt(i);
					if (ch <= 13 || ch == '\\' || ch == '\'' || ch == '/' && isEnable(SerializerFeature.WriteSlashAsSpecial)) {
						write('\\');
						write(IOUtils.replaceChars[(int) ch]);
					} else {
						write(ch);
					}
				}
				write('\'');
				return ;
			}
			expandCapacity(newcount);
		}
		
		int start = count + 1;
		int end = start + len;
		
		buf[count] = '\'';
		text.getChars(0, len, buf, start);
		count = newcount;
		
		int specialCount = 0;
		int lastSpecialIndex = -1;
		char lastSpecial = '\0';
		for (int i = start; i < end; ++i) {
			char ch = buf[i];
			if (ch <= 13 || ch == '\\' || ch == '\'' || ch == '/' && isEnable(SerializerFeature.WriteSlashAsSpecial)) {
				specialCount++;
				lastSpecialIndex = i;
				lastSpecial = ch;
			}
		}
		
		newcount += specialCount;
		if (newcount > buf.length) {
			expandCapacity(newcount);
		}
		count = newcount;
		
		if (specialCount == 1) {
			System.arraycopy(buf, lastSpecialIndex + 1, buf, lastSpecialIndex + 2, end - lastSpecialIndex - 1);
			buf[lastSpecialIndex] = '\\';
			buf[++lastSpecialIndex] = IOUtils.replaceChars[(int) lastSpecial];
		} else if (specialCount > 1) {
			System.arraycopy(buf, lastSpecialIndex + 1, buf, lastSpecialIndex + 2, end - lastSpecialIndex - 1);
			buf[lastSpecialIndex] = '\\';
			buf[++lastSpecialIndex] = IOUtils.replaceChars[(int) lastSpecial];
			end++;
			for (int i = lastSpecialIndex - 2; i >= start; --i) {
				char ch = buf[i];
				
				if (ch <= 13 || ch == '\\' || ch == '\'' || ch == '/' && isEnable(SerializerFeature.WriteSlashAsSpecial)) {
					System.arraycopy(buf, i + 1, buf, i + 2, end - i - 1);
					buf[i] = '\\';
					buf[i + 1] = IOUtils.replaceChars[(int) ch];
					end++;
				}
			}
		}
		buf[count - 1] = '\'';
	}
	
	/**
	 * 
	 * <p>Title: writeString</p>
	 * <p>Description: 向缓存中添加,字符串, 并且添加后缀, 就是分隔符</p>
	 * @param text 字符串
	 * @param seperator 分隔符
	 * @author java_liudong@163.com  2017年5月19日 上午10:52:23
	 */
	public void writeString(String text, char seperator) {
		if (useSingleQuotes) {
			writeStringWithSingleQuote(text);
			write(seperator);
		} else {
			writeStringWithDoubleQuote(text, seperator);
		}
	}
	
	/**
	 * 
	 * <p>Title: writeString</p>
	 * <p>Description: 向缓存中添加,字符串, 会检测是使用单引号还是双引号</p>
	 * @param text 字符串
	 * @author java_liudong@163.com  2017年5月19日 上午10:54:23
	 */
	public void writeString(String text) {
		if (useSingleQuotes) {
			writeStringWithSingleQuote(text);
		} else {
			writeStringWithDoubleQuote(text, (char) 0);
		}
	}
	
	/**
	 * 
	 * <p>Title: writeFieldValue</p>
	 * <p>Description: 写入, 键值对, 就是 javaBean的 字段名称 : 字段值</p>
	 * @param seperator 前缀, 一般为逗号
	 * @param name 字段名
	 * @param value 字段值 int
	 * @author java_liudong@163.com  2017年5月19日 上午11:10:59
	 */
	public void writeFieldValue(char seperator, String name, int value) {
		if (value == Integer.MIN_VALUE || !quoteFieldNames) { // 不添加: 号
			write(seperator);
			writeFieldName(name);
			writeInt(value);
			return ;
		}
		
		int intSize = (value < 0) ? IOUtils.stringSize(-value) + 1 : IOUtils.stringSize(value);
		
		int nameLen = name.length();
		int newcount = count + nameLen + 4 + intSize; // + 4 name的两个引号, 和前缀, 和 : 号    比如 a"b": 中的, a"":
		if (newcount > buf.length) {
			if (writer != null) {
				write(seperator);
				writeFieldName(name);
				writeInt(value);
				return ;
			}
			expandCapacity(newcount);
		}
		
		int start = count;
		count = newcount;
		
		buf[start] = seperator;
		int nameEnd = start + nameLen + 1;
		buf[start + 1] = keySeperator;
		
		name.getChars(0, nameLen, buf, start + 2); // 将name 写入到buf 中
		
		buf[nameEnd + 1] = keySeperator;
		buf[nameEnd + 2] = ':';
		
		IOUtils.getChars(value, count, buf);
	}
	
	
	/**
	 * 
	 * <p>Title: writeFieldValue</p>
	 * <p>Description: 添加JavaBean字段为long类型</p>
	 * @param seperator 前缀
	 * @param name 字段名
	 * @param value 字段值
	 * @author java_liudong@163.com  2017年5月19日 上午11:19:14
	 */
	public void writeFieldValue(char seperator, String name, long value) {
		if (value == Long.MIN_VALUE || !quoteFieldNames) {
			write(seperator);
			writeFieldName(name);
			writeLong(value);
			return ;
		}
		
		int intSize = (value < 0) ? IOUtils.stringSize(-value) + 1 : IOUtils.stringSize(value);
		
		int nameLen = name.length();
		int newcount = count + nameLen + 4 + intSize;
		if (newcount > buf.length) {
			if (writer != null) {
				write(seperator);
				writeFieldName(name);
				writeLong(value);
				return ;
			}
			expandCapacity(newcount);
		}
		
		int start = count;
		count = newcount;
		
		buf[start] = seperator;
		int nameEnd = start + nameLen + 1;
		buf[start + 1] = keySeperator;
		name.getChars(0, nameLen, buf, start + 2);
		
		buf[nameEnd + 1] = keySeperator;
		buf[nameEnd + 2] = ':';
		
		IOUtils.getChars(value, count, buf);
	}
	
	/**
	 * 
	 * <p>Title: writeFieldValue</p>
	 * <p>Description: 添加JavaBean字段为float类型</p>
	 * @param seperator 前缀
	 * @param name 字段名
	 * @param value 字段值
	 * @author java_liudong@163.com  2017年5月19日 上午11:31:53
	 */
	public void writeFieldValue(char seperator, String name, float value) {
		write(seperator);
		writeFieldName(name);
		writeFloat(value, false);
	}
	
	/**
	 * 
	 * <p>Title: writeFieldValue</p>
	 * <p>Description: 添加JavaBean字段为double类型</p>
	 * @param seperator 前缀
	 * @param name 字段名
	 * @param value 字段值
	 * @author java_liudong@163.com  2017年5月19日 上午11:35:12
	 */
	public void writeFieldValue(char seperator, String name, double value) {
		write(seperator);
		writeFieldName(name);
		writeDouble(value, false);
	}
	
	/**
	 * 
	 * <p>Title: writeFieldValue</p>
	 * <p>Description: 添加JavaBean字段为String类型</p>
	 * @param seperator 前缀
	 * @param name 字段名
	 * @param value 字段值
	 * @author java_liudong@163.com  2017年5月19日 上午11:37:11
	 */
	public void writeFieldValue(char seperator, String name, String value) {
		if (quoteFieldNames) { // 使用引号的情况
			if (useSingleQuotes) {
				write(seperator);
				writeFieldName(name);
				if (value == null) {
					writeNull();
				} else {
					writeString(value);
				}
			} else {
				if (isEnable(SerializerFeature.BrowserSecure)) {
					write(seperator);
					writeStringWithDoubleQuote(name, ':');
					writeStringWithDoubleQuote(value, (char) 0);
				} else if (isEnable(SerializerFeature.BrowserCompatible)){
					write(seperator);
					writeStringWithDoubleQuote(name, ':');
					writeStringWithDoubleQuote(value, (char) 0);
				} else {
					writeFieldValueStringWithDoubleQuoteCheck(seperator, name, value);
				}
			}
		} else { // 非引号的情况
			write(seperator);
			writeFieldName(name);
			if (value == null) {
				writeNull();
			} else {
				writeString(value);
			}
		}
	}
	
	/**
	 * 
	 * <p>Title: writeFieldValueStringWithDoubleQuoteCheck</p>
	 * <p>Description: 检测是否使用双引号包括</p>
	 * @param seperator
	 * @param name
	 * @param value
	 * @author java_liudong@163.com  2017年5月19日 上午11:42:03
	 */
	public void writeFieldValueStringWithDoubleQuoteCheck(char seperator, String name, String value) {
		int nameLen = name.length();
		int valueLen;
		
		int newcount = count;
		
		if (value == null) {
			valueLen = 4;
			newcount = nameLen + 8;
		} else {
			valueLen = value.length();
			newcount += nameLen + valueLen + 6;
		}
		
		if (newcount > buf.length) {
			if (writer != null) {
				write(seperator);
				writeStringWithDoubleQuote(name, ':');
				writeStringWithDoubleQuote(value, (char) 0);
				return ;
			}
			expandCapacity(newcount);
		}
		
		buf[count] = seperator;
		
		int nameStart = count + 2; // 前缀加冒号
		int nameEnd = nameStart + nameLen;
		
		buf[count + 1] = '\"';
		name.getChars(0, nameLen, buf, nameStart);
		
		count = newcount;
		
		buf[nameEnd] = '\"';
		
		int index = nameEnd + 1;
		buf[index++] = ':';
		
		// 为空的情况
		if (value == null) {
			buf[index++] = 'n';
			buf[index++] = 'u';
			buf[index++] = 'l';
			buf[index++] = 'l';
			return ;
		}
		
		buf[index++] = '"';
		
		int valueStart = index;
		int valueEnd = valueStart + valueLen;
		
		value.getChars(0, valueLen, buf, valueStart);
		
		int specialCount = 0;
		int lastSpecialIndex = -1;
		int firstSpecialIndex = -1;
		char lastSpecial = '\0';
		
		for (int i = valueStart; i < valueEnd; ++i) {
			char ch = buf[i];
			
			// 
			if (ch >= ']') {
				if (ch >= 0x7F && (ch == '\u2028' || ch == '\u2029' || ch < 0xA0)) {
					if (firstSpecialIndex == -1) {
						firstSpecialIndex = i;
					}
					
					specialCount++;
					lastSpecialIndex = i;
					lastSpecial = ch;
					newcount += 4;
				}
				continue;
			}
			// 
			if (isSpecial(ch, this.features)) {
				specialCount++;
				lastSpecialIndex = i;
				lastSpecial = ch;
				
				if (ch < IOUtils.specicalFlags_doubleQuotes.length && IOUtils.specicalFlags_doubleQuotes[ch] == 4) {
					newcount += 4;
				}
				
				if (firstSpecialIndex == -1) {
					firstSpecialIndex = i;
				}
			}
		}
		
		if (specialCount > 0) {
			newcount += specialCount;
			if (newcount > buf.length) {
				expandCapacity(newcount);
			}
			count = newcount;
			
			if (specialCount == 1) {
				if (lastSpecial == '\u2028') {
					int srcPos = lastSpecialIndex + 1;
					int destPos = lastSpecialIndex + 6;
					int LengthOfCopy = valueEnd - lastSpecialIndex - 1;
					System.arraycopy(buf, srcPos, buf, destPos, LengthOfCopy);
					buf[lastSpecialIndex] = '\\';
					buf[++lastSpecialIndex] = 'u';
					buf[++lastSpecialIndex] = '2';
					buf[++lastSpecialIndex] = '0';
					buf[++lastSpecialIndex] = '2';
					buf[++lastSpecialIndex] = '8';
				} else if  (lastSpecial == '\u2029') {
					int srcPos = lastSpecialIndex + 1;
					int destPos = lastSpecialIndex + 6;
					int LengthOfCopy = valueEnd - lastSpecialIndex - 1;
					System.arraycopy(buf, srcPos, buf, destPos, LengthOfCopy);
					buf[lastSpecialIndex] = '\\';
					buf[++lastSpecialIndex] = 'u';
					buf[++lastSpecialIndex] = '2';
					buf[++lastSpecialIndex] = '0';
					buf[++lastSpecialIndex] = '2';
					buf[++lastSpecialIndex] = '9';
				} else {
					final char ch = lastSpecial;
					if (ch < IOUtils.specicalFlags_doubleQuotes.length && IOUtils.specicalFlags_doubleQuotes[ch] == 4) {
						int srcPos = lastSpecialIndex + 1;
						int destPos = lastSpecialIndex + 6;
						int LengthOfCopy = valueEnd - lastSpecialIndex - 1;
						System.arraycopy(buf, srcPos, buf, destPos, LengthOfCopy);
						
						int bufIndex = lastSpecialIndex;
						buf[bufIndex++] = '\\';
						buf[bufIndex++] = 'u';
						buf[bufIndex++] = IOUtils.DIGITS[(ch >>> 12) & 15];
						buf[bufIndex++] = IOUtils.DIGITS[(ch >>> 8) & 15];
						buf[bufIndex++] = IOUtils.DIGITS[(ch >>> 4) & 15];
						buf[bufIndex++] = IOUtils.DIGITS[ch & 15];
					} else {
						int srcPos = lastSpecialIndex + 1;
						int destPos = lastSpecialIndex + 2;
						int LengthOfCopy = valueEnd - lastSpecialIndex - 1;
						System.arraycopy(buf, srcPos, buf, destPos, LengthOfCopy);
						buf[lastSpecialIndex] = '\\';
						buf[++lastSpecialIndex] = IOUtils.replaceChars[(int) ch];
					}
				}
			} else if (specialCount > 1) {
				int textIndex = firstSpecialIndex - valueStart;
				int bufIndex = firstSpecialIndex;
				for (int i = textIndex; i < value.length(); ++i) {
					char ch = value.charAt(i);
					
					if (ch < IOUtils.specicalFlags_doubleQuotes.length 
							&& IOUtils.specicalFlags_doubleQuotes[ch] != 0
							|| (ch == '/' && isEnable(SerializerFeature.WriteSlashAsSpecial))) {
						buf[bufIndex++] = '\\';
						if (IOUtils.specicalFlags_doubleQuotes[ch] == 4) {
							buf[bufIndex++] = 'u';
							buf[bufIndex++] = IOUtils.DIGITS[(ch >>> 12) & 15];
							buf[bufIndex++] = IOUtils.DIGITS[(ch >>> 8) & 15];
							buf[bufIndex++] = IOUtils.DIGITS[(ch >>> 4) & 15];
							buf[bufIndex++] = IOUtils.DIGITS[ch & 15];
							valueEnd += 5;
						} else {
							buf[bufIndex++] = IOUtils.replaceChars[(int) ch];
							valueEnd++;
						}
					} else {
						if (ch == '\u2028' || ch == '\u2029') {
							buf[bufIndex++] = '\\';
							buf[bufIndex++] = 'u';
							buf[bufIndex++] = IOUtils.DIGITS[(ch >>> 12) & 15];
							buf[bufIndex++] = IOUtils.DIGITS[(ch >>> 8) & 15];
							buf[bufIndex++] = IOUtils.DIGITS[(ch >>> 4) & 15];
							buf[bufIndex++] = IOUtils.DIGITS[ch & 15];
							valueEnd += 5;
						} else {
							buf[bufIndex++] = ch;
						}
					}
				}
			}
		}
		buf[count - 1] = '\"';
	}
	
	/**
	 * 
	 * <p>Title: writeFieldValueStringWithDoubleQuote</p>
	 * <p>Description: 向缓存中添加,JavaBean的字段名和值</p>
	 * @param seperator
	 * @param name
	 * @param value
	 * @author java_liudong@163.com  2017年5月19日 下午2:51:51
	 */
	public void writeFieldValueStringWithDoubleQuote(char seperator, String name, String value) {
		int nameLen = name.length();
		int valueLen;
		
		int newcount = count;
		
		valueLen = value.length();
		newcount += nameLen + valueLen + 6;
		
		if (newcount > buf.length) {
			if (writer != null) {
				write(seperator);
				writeStringWithDoubleQuote(name, ':');
				writeStringWithDoubleQuote(value, (char) 0);
				return ;
			}
			expandCapacity(newcount);
		}
		
		buf[count] = seperator;
		
		int nameStart = count + 2;
		int nameEnd = nameStart + nameLen;
		
		buf[count + 1] = '\"';
		name.getChars(0, nameLen, buf, nameStart);
		
		count = newcount;
		
		buf[nameEnd] = '\"';
		
		int index = nameEnd + 1;
		buf[index++] = ':';
		buf[index++] = '"';
		
		int valueStart = index;
		value.getChars(0, valueLen, buf, valueStart);
		buf[count - 1] = '\"';
	}
	
	/**
	 * 
	 * <p>Title: writeFieldValue</p>
	 * <p>Description: 向缓存中写入枚举</p>
	 * @param seperator
	 * @param name
	 * @param value
	 * @author java_liudong@163.com  2017年5月19日 下午2:56:13
	 */
	public void writeFieldValue(char seperator, String name, Enum<?> value) {
		if (value == null) {
			write(seperator);
			writeFieldName(name);
			writeNull();
			return ;
		}
		
		if (writeEnumUsingName && !writeEnumUsingToString) {
			writeEnumFieldValue(seperator, name, value.name());
		} else if (writeEnumUsingToString) {
			writeEnumFieldValue(seperator, name, value.toString());
		} else {
			writeFieldValue(seperator, name, value.ordinal());
		}
	}
	
	/**
	 * 
	 * <p>Title: writeEnumFieldValue</p>
	 * <p>Description: </p>
	 * @param seperator
	 * @param name
	 * @param value
	 * @author java_liudong@163.com  2017年5月19日 下午2:54:23
	 */
	private void writeEnumFieldValue(char seperator, String name, String value) {
		if (useSingleQuotes) {
			writeFieldValue(seperator, name, value);
		} else {
			writeFieldValueStringWithDoubleQuote(seperator, name, value);
		}
	}
	
	/**
	 * 
	 * <p>Title: writeFieldValue</p>
	 * <p>Description: 向缓存中写入BigDecimal</p>
	 * @param seperator
	 * @param name
	 * @param value
	 * @author java_liudong@163.com  2017年5月19日 下午3:01:18
	 */
	public void writeFieldValue(char seperator, String name, BigDecimal value) {
		write(seperator);
		writeFieldName(name);
		if (value == null) {
			writeNull();
		} else {
			write(value.toString());
		}
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

	/**
	 * 
	 * <p>Title: append</p>
	 * <p>Description: 追加部分字符串</p>
	 * @param csq 追加字符串
	 * @param start 追加字符串的起始位置
	 * @param end 追加字符串的结束位置
	 * @return
	 * @author java_liudong@163.com  2017年5月10日 上午11:49:05
	 * @see java.io.Writer#append(java.lang.CharSequence, int, int)
	 */
	@Override
	public SerializeWriter append(CharSequence csq, int start, int end) {
		String s = (csq == null ? "null" : csq).subSequence(start, end).toString();
		write(s, 0, s.length());
		return this;
	}
	
	/**
	 * 
	 * <p>Title: append</p>
	 * <p>Description: 追加单个字符</p>
	 * @param c 追加字符
	 * @return
	 * @author java_liudong@163.com  2017年5月10日 下午12:02:31
	 * @see java.io.Writer#append(char)
	 */
	@Override
	public SerializeWriter append(char c) {
		write(c);
		return this;
	}
	
	/**
	 * 
	 * <p>Title: toCharArray</p>
	 * <p>Description: 将缓存中的数据,复制到新的char[] 缓存中, 这个新的缓存,将会不会有多余的null空间</p>
	 * @return 返回只有数据的缓存
	 * @author java_liudong@163.com  2017年5月11日 下午7:58:09
	 */
	public char[] toCharArray() {
		if (this.writer != null) {
			throw new UnsupportedOperationException("writer not null");
		}
		
		char[] newValue = new char[count];
		System.arraycopy(buf, 0, newValue, 0, count);
		return newValue;
	}
	
	/**
	 * 
	 * <p>Title: toCharArrayForSpringSocket</p>
	 * <p>Description: 用于 spring socket中的, 少勒点东西</p>
	 * @return
	 * @author java_liudong@163.com  2017年5月11日 下午8:01:25
	 */
	public char[] toCharArrayForSpringSocket() {
		if (this.writer != null) {
			throw new UnsupportedOperationException("writer not null");
		}
		
		char[] newValue = new char[count - 2];
		System.arraycopy(buf, 1, newValue, 0, count - 2);
		return newValue;
	}
	
	/**
	 * 
	 * <p>Title: toBytes</p>
	 * <p>Description: 缓存数据,指定格式转换为byte[]</p>
	 * @param charsetName 字符串形式的编码格式
	 * @return
	 * @author java_liudong@163.com  2017年5月11日 下午8:16:34
	 */
	public byte[] toBytes(String charsetName) {
		return toBytes(charsetName == null || "UTF-8".equals(charsetName) ? UTF8 : Charset.forName(charsetName));
	}
	
	/**
	 * 
	 * <p>Title: toBytes</p>
	 * <p>Description: 缓存数据,指定格式转换为byte[]</p>
	 * @param charset 编码格式
	 * @return 字节数据
	 * @author java_liudong@163.com  2017年5月11日 下午8:15:15
	 */
	public byte[] toBytes(Charset charset) {
		if (this.writer != null) {
			throw new UnsupportedOperationException("writer not null");
		}
		
		if (charset == UTF8) {
			return encodeToUTF8Bytes();
		} else {
			return new String(buf, 0, count).getBytes(charset);
		}
	}
	
	/**
	 * 
	 * <p>Title: size</p>
	 * <p>Description: 返回缓冲区内容大小</p>
	 * @return
	 * @author java_liudong@163.com  2017年5月11日 下午8:20:11
	 */
	public int size() {
		return count;
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
	
	/**
	 * 
	 * <p>Title: encodeToUTF8</p>
	 * <p>Description: 将缓冲区中的数据, 以UTF8的编码格式,输出到指定的字节流中</p>
	 * @param out 字节流
	 * @return
	 * @author java_liudong@163.com  2017年5月10日 下午12:30:44
	 * @throws IOException 
	 */
	private int encodeToUTF8(OutputStream out) throws IOException {
		// 给缓存中的长度, 做一个扩容, 因为 char中可能 是字符
		int bytesLength = (int) (count * (double) 3);
		// 先检测本地是否有 字节信息保存着
		byte[] bytes = bytesBufLocal.get();
		
		if (bytes == null) {
			bytes = new byte[1024 * 8]; // 初始化个8K 空间吧
			bytesBufLocal.set(bytes);
		}
		
		if (bytes.length < bytesLength) {
			bytes = new byte[bytesLength];
		}
		
		// 将buf缓冲区中的数据,以UTF8编码转换
		int position = IOUtils.encodeUTF8(buf, 0, count, bytes);
		// 将数据以流的形式写出去
		out.write(bytes, 0, position);
		return position;
	}
	
	/**
	 * 
	 * <p>Title: encodeToUTF8Bytes</p>
	 * <p>Description: 将数据以UTF8编码,转换为 字节数组</p>
	 * @return 缓存的字节数据
	 * @author java_liudong@163.com  2017年5月11日 下午8:10:17
	 */
	private byte[] encodeToUTF8Bytes() {
		int bytesLength = (int) (count * (double) 3);
		byte[] bytes = bytesBufLocal.get();
		
		if (bytes == null) {
			bytes = new byte[1024 * 8];
			bytesBufLocal.set(bytes);
		}
		
		if (bytes.length < bytesLength) {
			bytes =  new byte[bytesLength];
		}
		
		int position = IOUtils.encodeUTF8(buf, 0, count, bytes);
		// 新建一个 字节数组, 用来装 缓存中的数据
		byte[] copy = new byte[position];
		System.arraycopy(bytes, 0, copy, 0, position);
		return copy;
	}
	
	
}
