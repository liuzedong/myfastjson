package com.dongdongxia.myfastjson.serializer;
/**
 * 
 * <P>Description: 串行上下文, 用来装父类,父类的, 一级一级的向上累加, 属于一个链表结构</P>
 * @ClassName: SerialContext
 * @author java_liudong@163.com  2017年5月26日 下午4:29:31
 */
public class SerialContext {

	public final SerialContext parent; // 父对象
	public final Object object; // 当前对象
	public final Object fieldName;
	public final int features;
	
	public SerialContext(SerialContext parent, Object object, Object fieldName, int features, int fieldFeatures) {
		this.parent = parent;
		this.object = object;
		this.fieldName = fieldName;
		this.features = features;
	}
	
	
	@Override
	public String toString() {
		if (parent == null) {
			return "$";
		} else {
			if (fieldName instanceof Integer) {
				return parent.toString() + "[" + fieldName + "]";
			} else {
				return parent.toString() + "." + fieldName;
			}
		}
	}
	
	
}
