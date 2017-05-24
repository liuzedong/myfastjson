package com.dongdongxia.myfastjson.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * 
 * <P>Description: 泛型接口实现</P>
 * @ClassName: ParameterizedTypeImpl
 * @author java_liudong@163.com  2017年5月24日 上午11:06:57
 */
public class ParameterizedTypeImpl implements ParameterizedType{

	private final Type[] actualTypeArguments;
	private final Type ownerType;
	private final Type rawType;
	
	public ParameterizedTypeImpl(Type[] actualTypeArguments, Type ownerType, Type rawType) {
		this.actualTypeArguments = actualTypeArguments;
		this.ownerType = ownerType;
		this.rawType = rawType;
	}
	
	@Override
	public Type[] getActualTypeArguments() {
		return actualTypeArguments;
	}

	@Override
	public Type getRawType() {
		return rawType;
	}

	@Override
	public Type getOwnerType() {
		return ownerType;
	}

	/**
	 * 
	 * <p>Title: equals</p>
	 * <p>Description: 重写equals方法</p>
	 * @param obj
	 * @return
	 * @author java_liudong@163.com  2017年5月24日 上午11:14:42
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		
		ParameterizedTypeImpl that = (ParameterizedTypeImpl) obj;
		
		if (!Arrays.equals(actualTypeArguments, that.actualTypeArguments)) {
			return false;
		}
		
		if (ownerType != null ? ownerType.equals(that.ownerType) : that.ownerType != null) {
			return false;
		}
		
		return rawType != null ? rawType.equals(that.rawType) : that.rawType == null;
	}
	
	@Override
	public int hashCode() {
		int result = actualTypeArguments != null ? Arrays.hashCode(actualTypeArguments) : 0;
		result = 31 * result + (ownerType != null ? ownerType.hashCode() : 0);
		result = 31 * result + (rawType != null ? rawType.hashCode() : 0);
		return result;
	}
}
