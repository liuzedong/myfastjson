package com.dongdongxia.myfastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

public class JavaBeanSerializer extends SerializerFilterable implements ObjectSerializer{

	
	
	@Override
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
		
	}
	
	protected void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features, boolean unwrapped) {
		
	}

}
