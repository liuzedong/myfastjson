package com.dongdongxia.myfastjson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;

import com.dongdongxia.myfastjson.serializer.SerializeWriter;

/**
 * 
 * <P>Description: 序列化写入测试类</P>
 * @ClassName: SerilizeWriterTest
 * @author java_liudong@163.com  2017年5月10日 上午8:41:54
 */
public class SerializeWriterTest {

	
	public static void main(String[] args) throws Exception {
//		testWriteChar();
		testWriterToFile();
	}
	
	/**
	 * 
	 * <p>Title: testWriteChar</p>
	 * <p>Description: writer(int ) 方法的使用, 这个里面是没有 构造函数,writer的</p>
	 * @author java_liudong@163.com  2017年5月10日 上午9:43:22
	 * @throws FileNotFoundException 
	 */
	public static void testWriteChar() throws Exception{
		SerializeWriter writer = new SerializeWriter();
		writer.write('刘');
		writer.write('东');
//		char[] c = {'天', '天', '向', '上', ',', '就', '是', '我',};
		
		String txt = readFileContext("/home/liuzedong/下载/bb.txt");
		char[] c = txt.toCharArray();
		
		writer.write(c, 0, c.length);
		writer.flush();
		String s = writer.toString();
		System.out.println(s);
		writer.close();
	}
	
	/**
	 * 
	 * <p>Title: testWriterToFile</p>
	 * <p>Description: 使用构造函数, 参数中,包含 Writer, 就是指定输出到什么地方</p>
	 * @author java_liudong@163.com  2017年5月10日 上午10:16:50
	 */
	public static void testWriterToFile() throws Exception{
		FileWriter fileWriter = new FileWriter(new File("/home/liuzedong/下载/aa.txt"));
		SerializeWriter writer = new SerializeWriter(fileWriter);
		writer.write('刘');
		writer.write('东');
		
		// 将读取的文件内容, 这个内容已经超过啦, 初始化的容量大小2048, 2M
		String txtStr = readFileContext("/home/liuzedong/下载/bb.txt");
		char[] c = txtStr.toCharArray();
		writer.write(c, 0, c.length);
		writer.flush();
		writer.close();
	}
	
	
	
	public static String readFileContext(String filePath) throws Exception{
		FileReader fileReader = new FileReader(new File(filePath));
		StringBuilder sb = new StringBuilder();
		while(fileReader.ready()){
			sb.append((char)fileReader.read());
		}
		fileReader.close();
		return sb.toString();
	}
}
