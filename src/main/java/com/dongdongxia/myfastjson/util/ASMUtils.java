package com.dongdongxia.myfastjson.util;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * 
 * <P>Description: 字节码工具类</P>
 * @ClassName: ASMUtils
 * @author java_liudong@163.com  2017年4月28日 上午9:54:31
 */
public class ASMUtils {

	// 获取本机的JDK虚拟机的版本
	public static final String JAVA_VM_HOME = System.getProperty("java.vm.name");
	
	// 检测是否是Android 的虚拟机
	public static final boolean IS_ANDROID = isAndroid(JAVA_VM_HOME);
	
	/**
	 * 
	 * <p>Title: isAndroid</p>
	 * <p>Description: 检测是否是Android 的JDK虚拟机</p>
	 * @param vmName
	 * @return
	 * @author java_liudong@163.com  2017年4月28日 上午9:59:53
	 */
	public static boolean isAndroid(String vmName) {
		if (vmName == null){
			return false;
		}
		String lowerVMName = vmName.toLowerCase();
		return lowerVMName.contains("dalvik") // google Android VM 版本
				|| lowerVMName.contains("lemur"); // aliyun-vm name
	}
	
	/**
	 * 
	 * <p>Title: desc</p>
	 * <p>Description: 获取方法的,入参字节类型,和出参的字节类型, 入参类型使用() 进行括起来, ; 进行分割</p>
	 * @param method
	 * @return
	 * @author java_liudong@163.com  2017年4月28日 上午11:43:45
	 */
	public static String desc(Method method){
		// 获取方法的入参, 所有类型
		Class<?>[] types = method.getParameterTypes();
		// 进行扩容
		StringBuilder buf = new StringBuilder((types.length + 1) << 4);
		buf.append('(');
		for (int i = 0; i < types.length; ++i){
			buf.append(desc(types[i]));
		}
		buf.append(')');
		buf.append(desc(method.getReturnType()));
		return buf.toString();
	}
	
	/**
	 * 
	 * <p>Title: desc</p>
	 * <p>Description: 获取类的字节码字符</p>
	 * @param returnType
	 * @return
	 * @author java_liudong@163.com  2017年4月28日 上午10:38:58
	 */
	public static String desc(Class<?> returnType){
		if (returnType.isPrimitive()){ // 检测是否是, 基础类型的原始类型Integer, Boolean 之类的
			return getPrimitiveLetter(returnType);
		} else if (returnType.isArray()) { // 检测是否是数组
			return "[" + desc(returnType.getComponentType()); // 递归调用该方法, 获取数组的类型
		} else { // 这个是,对象
			return "L" + type(returnType) + ";"; // 
		}
	}
	
	/**
	 * 
	 * <p>Title: type</p>
	 * <p>Description: 入参的对象的字节码</p>
	 * @param parameterType
	 * @return
	 * @author java_liudong@163.com  2017年4月28日 上午11:32:39
	 */
	public static String type(Class<?> parameterType){
		if (parameterType.isArray()) { // 检测是否是数组, 如果是从desc(Class<?> returnType) 方法进来的, 说明是多维数组
			return "[" + desc(parameterType.getComponentType());
		} else {
			if (!parameterType.isPrimitive()) { // 不是基本数据类型的情况
				String clsName = parameterType.getName(); // 获取数据的类型
				return clsName.replace('.', '/'); // 将. 改成目录的/ 形式, 比如   java.lang.String == > java/lang/String
			} else {
				return getPrimitiveLetter(parameterType);
			}
		}
	}
	
	/**
	 * 
	 * <p>Title: getPrimitiveLetter</p>
	 * <p>Description: 返回基础类型的字节码字符</p>
	 * @param type
	 * @return
	 * @author java_liudong@163.com  2017年4月28日 上午10:42:32
	 */
	public static String getPrimitiveLetter(Class<?> type){
		if (Integer.TYPE == type) {
			return "I";
		} else if (Void.TYPE == type) {
			return "V";
		} else if (Boolean.TYPE == type) {
			return "Z";
		} else if (Character.TYPE == type) {
			return "C";
		} else if (Byte.TYPE == type) {
			return "B";
		} else if (Short.TYPE == type) {
			return "S";
		} else if (Float.TYPE == type) {
			return "F";
		} else if (Long.TYPE == type) {
			return "J";
		} else if (Double.TYPE == type) {
			return "D";
		}
		throw new IllegalStateException("Type: " + type.getCanonicalName() + "is not a primitive type (就是告诉你, 不是基本数据类型)");
	}
	
	/**
	 * 
	 * <p>Title: getMethodType</p>
	 * <p>Description: 获取指定对象的指定方法的返回参数</p>
	 * @param clazz
	 * @param methodName
	 * @return
	 * @author java_liudong@163.com  2017年4月28日 上午11:47:04
	 */
	public static Type getMethodType(Class<?> clazz, String methodName) {
		try{
			Method method = clazz.getMethod(methodName);
			return method.getGenericReturnType();
		} catch (Exception e) {
			return null;
		}
	}
	
	public static boolean checkName(String name) {
		for (int i = 0; i < name.length(); ++i) {
			char c = name.charAt(i); // 获取入参的每个一个字符,是否包含 . 和空格一下, 或者不符合的字符串
			if (c < '\001' || c > '\177' || c == '.') { 
				return false;
			}
		}
		return true;
	}
	
	/*public static void main(String[] args) {
		Method[] methods = String.class.getMethods();
		desc(methods[54]);
		System.out.println(methods);
		
		System.out.println(Character.toString('\000'));
		checkName(" 1Y3W8F7UQOEI1	aWAHDWOUFEGWJAKFKSGCUHI HIUweu	q103798yOUJGgfjhgfjhfasf");
		
	}*/
	
}
