package com.dongdongxia.myfastjson.serializer;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicReference;
/**
 * 
 * <P>Description: java 四种引用的(强软软虚) 的序列化和反序列化</P>
 * @ClassName: ReferenceCodec
 * @author java_liudong@163.com  2017年6月9日 下午2:55:20
 */
public class ReferenceCodec implements ObjectSerializer{

	public static final ReferenceCodec instance = new ReferenceCodec();
	
	@SuppressWarnings("rawtypes")
	@Override
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
		Object item;
		if (object instanceof AtomicReference) { // 
			AtomicReference val = (AtomicReference) object;
			item = val.get();
		} else {
			item = ((Reference) object).get();
		}
		serializer.write(item);
	}

}
