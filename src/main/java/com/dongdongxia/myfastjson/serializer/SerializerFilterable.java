package com.dongdongxia.myfastjson.serializer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * <P>Description: 序列化可选过滤器, 包含多个过滤器, 在ObjectSerializer 中使用到的该类的子类</P>
 * @ClassName: SerializerFilterable
 * @author java_liudong@163.com  2017年4月28日 下午2:05:34
 */
public abstract class SerializerFilterable {

	/** 定义所有的过滤器 start */
	protected List<BeforeFilter> beforeFilters = null;
	protected List<AfterFilter> afterFilters = null;
	protected List<PropertyFilter> propertyFilters = null;
	protected List<NameFilter> nameFilters = null;
	protected List<ValueFilter> valueFilters = null;
	protected List<PropertyPreFilter> propertyPreFilters = null;
	protected List<LabelFilter> labelFilters = null;
	protected List<ContextValueFilter> contextValueFilters = null;
	/** 定义所有的过滤器 end */
	
	protected boolean writeDirect;
	
	
	/** 获取过滤器, 没有就初始化 start */
	public List<BeforeFilter> getBeforeFilters() {
		if (beforeFilters == null) {
			beforeFilters = new ArrayList<BeforeFilter>();
			writeDirect = false;
		}
		return beforeFilters;
	}
	
	public List<AfterFilter> getAfterFilters() {
		if (afterFilters == null) {
			afterFilters = new ArrayList<AfterFilter>();
			writeDirect = false;
		}
		return afterFilters;
	}
	
	public List<PropertyFilter> getPropertyFilters() {
		if (propertyFilters == null) {
			propertyFilters = new ArrayList<PropertyFilter>();
			writeDirect = false;
		}
		return propertyFilters;
	}
	
	public List<NameFilter> getNameFilters() {
		if (nameFilters == null) {
			nameFilters = new ArrayList<NameFilter>();
			writeDirect = false;
		}
		return nameFilters;
	}
	
	public List<ValueFilter> getValueFilters() {
		if (valueFilters == null) {
			valueFilters = new ArrayList<ValueFilter>();
			writeDirect = false;
		}
		return valueFilters;
	}
	
	public List<PropertyPreFilter> getPropertyPreFilters() {
		if (propertyPreFilters == null) {
			propertyPreFilters = new ArrayList<PropertyPreFilter>();
			writeDirect = false;
		}
		return propertyPreFilters;
	}
	
	public List<LabelFilter> getLabelFilters() {
		if (labelFilters == null) {
			labelFilters = new ArrayList<LabelFilter>();
			writeDirect = false;
		}
		return labelFilters;
	}
	
	public List<ContextValueFilter> getContextValueFilters() {
		if (contextValueFilters == null) {
			contextValueFilters = new ArrayList<ContextValueFilter>();
			writeDirect = false;
		}
		return contextValueFilters;
	}
	/** 获取过滤器, 没有就初始化 end */
	
	
	/**
	 * 
	 * <p>Title: addFilter</p>
	 * <p>Description: 向过滤器集合中添加指定的过滤器</p>
	 * @param filter
	 * @author java_liudong@163.com  2017年5月25日 上午11:14:39
	 */
	public void addFilter(SerializeFilter filter) {
		if (filter == null) {
			return ;
		}
		
		if (filter instanceof BeforeFilter) {
			this.getBeforeFilters().add((BeforeFilter) filter);
		}
		
		if (filter instanceof AfterFilter) {
			this.getAfterFilters().add((AfterFilter) filter);
		}
		
		if (filter instanceof PropertyFilter) {
			this.getPropertyFilters().add((PropertyFilter) filter);
		}
		
		if (filter instanceof NameFilter) {
			this.getNameFilters().add((NameFilter) filter);
		}
		
		if (filter instanceof ValueFilter) {
			this.getValueFilters().add((ValueFilter) filter);
		}
		
		if (filter instanceof PropertyPreFilter) {
			this.getPropertyPreFilters().add((PropertyPreFilter) filter);
		}
		
		if (filter instanceof LabelFilter) {
			this.getLabelFilters().add((LabelFilter) filter);
		}
		
		if (filter instanceof ContextValueFilter) {
			this.getContextValueFilters().add((ContextValueFilter) filter);
		}
	}
	

	/**
	 * 
	 * <p>Title: applyName</p>
	 * <p>Description: 执行过滤器中的propertyPreFilters 中的子类</p>
	 * @param jsonBeanDeser
	 * @param object
	 * @param key
	 * @return
	 * @author java_liudong@163.com  2017年5月25日 上午11:23:37
	 */
	public boolean applyName(JSONSerializer jsonBeanDeser, Object object, String key) {
		if (jsonBeanDeser.propertyPreFilters != null) {
			for (PropertyPreFilter filter : jsonBeanDeser.propertyPreFilters) {
				if (!filter.apply(jsonBeanDeser, object, key)) {
					return false;
				}
			}
		}
		
		if (this.propertyPreFilters != null) {
			for (PropertyPreFilter filter : this.propertyPreFilters) {
				if (!filter.apply(jsonBeanDeser, object, key)) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * 
	 * <p>Title: apply</p>
	 * <p>Description: 执行所有的propertyFilter 集合过滤器的子类</p>
	 * @param jsonBeanDeser
	 * @param object
	 * @param key
	 * @param propertyValue
	 * @return
	 * @author java_liudong@163.com  2017年5月25日 上午11:36:56
	 */
	public boolean apply(JSONSerializer jsonBeanDeser, Object object, String key, Object propertyValue) {
		if (jsonBeanDeser.propertyFilters != null) {
			for (PropertyFilter filter : jsonBeanDeser.propertyFilters) {
				if (!filter.apply(object, key, propertyValue)) {
					return false;
				}
			}
		}
		
		// 接口中的
		if (this.propertyFilters != null) {
			for (PropertyFilter filter : this.propertyFilters) {
				if (!filter.apply(object, key, propertyValue)) {
					return false;
				}
			}
		}
		return true;
	}
	
	
	/**
	 * 
	 * <p>Title: processKey</p>
	 * <p>Description: 执行nameFilters 中的过滤器</p>
	 * @param jsonBeanDeser
	 * @param object
	 * @param key
	 * @param propertyValue
	 * @return
	 * @author java_liudong@163.com  2017年5月25日 上午11:41:23
	 */
	protected String processKey(JSONSerializer jsonBeanDeser, Object object, String key, Object propertyValue) {
		if (jsonBeanDeser.nameFilters != null) {
			for (NameFilter filter : jsonBeanDeser.nameFilters) {
				key = filter.process(object, key, propertyValue);
			}
		}
		
		if (this.nameFilters != null) {
			for (NameFilter filter : this.nameFilters) {
				key = filter.process(object, key, propertyValue);
			}
		}
		return key;
	}
	
	/**
	 * 
	 * <p>Title: processValue</p>
	 * <p>Description: valueFilters 和 contextValueFilters 的过滤器执行</p>
	 * @param jsonBeanDeser
	 * @param beanContext
	 * @param object
	 * @param key
	 * @param propertyValue
	 * @return
	 * @author java_liudong@163.com  2017年5月25日 下午4:12:50
	 */
	protected Object processValue(JSONSerializer jsonBeanDeser, BeanContext beanContext,Object object, String key, Object propertyValue) {
		if (propertyValue != null) {
			// 非字符串最为字符串 || (对象不为空 && 是否包含 )
			if (jsonBeanDeser.out.writeNonStringValueAsString
					|| (beanContext != null && (beanContext.getFeatures() & SerializerFeature.WriteNonStringValueAsString.mask) != 0)
					&& (propertyValue instanceof Number || propertyValue instanceof Boolean)) {
				String format = null;
				if(propertyValue instanceof Number && beanContext != null) {
					format = beanContext.getFormat();
				}
				
				if (format != null) {
					propertyValue = new DecimalFormat(format).format(propertyValue); // 按照一定的模式, 格式化 数字
				} else {
					propertyValue = propertyValue.toString();
				}
			} else if (beanContext != null && beanContext.isJsonDircet()) {
				String jsonStr = (String) propertyValue;
//				propertyValue = JSON.parser(jsonStr); // 暂时没有
			}
		}
		
		if (jsonBeanDeser.valueFilters != null) {
			for (ValueFilter filter : jsonBeanDeser.valueFilters) {
				propertyValue = filter.process(object, key, propertyValue);
			}
		}
		
		List<ValueFilter> valueFilters = this.valueFilters;
		if (valueFilters != null) {
			for (ValueFilter filter : valueFilters) {
				propertyValue = filter.process(object, key, propertyValue);
			}
		}
		
		if (jsonBeanDeser.contextValueFilters != null) {
			for (ContextValueFilter filter : jsonBeanDeser.contextValueFilters) {
				propertyValue = filter.process(beanContext, object, key,  propertyValue);
			}
		}
		
		if (this.contextValueFilters != null) {
			for (ContextValueFilter filter : this.contextValueFilters) {
				propertyValue = filter.process(beanContext, object, key,  propertyValue);
			}
		}
		
		return propertyValue;
	}
	
	/**
	 * 
	 * <p>Title: writeDirect</p>
	 * <p>Description: 检测是否都是直接写入的</p>
	 * @param jsonBeanDeser
	 * @return
	 * @author java_liudong@163.com  2017年5月25日 下午4:54:18
	 */
	protected boolean writeDirect(JSONSerializer jsonBeanDeser) {
		return jsonBeanDeser.out.writeDirect && this.writeDirect && jsonBeanDeser.writeDirect;
	}
}
