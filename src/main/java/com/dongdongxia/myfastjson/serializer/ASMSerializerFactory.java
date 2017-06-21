package com.dongdongxia.myfastjson.serializer;

import static com.dongdongxia.myfastjson.util.ASMUtils.desc;
import static com.dongdongxia.myfastjson.util.ASMUtils.type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.dongdongxia.myfastjson.JSONException;
import com.dongdongxia.myfastjson.annotation.JSONType;
import com.dongdongxia.myfastjson.asm.ClassWriter;
import com.dongdongxia.myfastjson.asm.FieldWriter;
import com.dongdongxia.myfastjson.asm.Label;
import com.dongdongxia.myfastjson.asm.MethodVisitor;
import com.dongdongxia.myfastjson.asm.MethodWriter;
import com.dongdongxia.myfastjson.asm.Opcodes;
import com.dongdongxia.myfastjson.util.ASMClassLoader;
import com.dongdongxia.myfastjson.util.ASMUtils;
import com.dongdongxia.myfastjson.util.FieldInfo;

/**
 * 
 * <P>Description: 使用ASM字节码, 生成序列化实体类工厂</P>
 * @ClassName: ASMSerializerFactory
 * @author java_liudong@163.com  2017年6月15日 上午11:33:07
 */
public class ASMSerializerFactory implements Opcodes{

	/**
	 * 用来将生成对象加载到内存中
	 */
	protected final ASMClassLoader classLoader = new ASMClassLoader();
	/**
	 * 用来生成 每个 序列化对象名称中的序号, 以便不出现重名
	 */
	private static final AtomicLong seed = new AtomicLong();
	
	/**
	 * 下面为定义虚拟机内存中定义的对象名称
	 */
	static final String JSONSerializer = type(JSONSerializer.class); // com/dongdongxia/myfastjson/serializer/JSONSerializer
	static final String ObjectSerializer = type(ObjectSerializer.class); // com/dongdongxia/myfastjson/serializer/ObjectSerializer
	static final String ObjectSerializer_desc = "L" + ObjectSerializer + ";"; // Lcom/dongdongxia/myfastjson/serializer/ObjectSerializer;
	static final String SerializeWriter = type(SerializeWriter.class); // com/dongdongxia/myfastjson/serializer/SerializeWriter
	static final String SerializeWriter_desc = "L" + SerializeWriter + ";"; // Lcom/dongdongxia/myfastjson/serializer/SerializeWriter;
	static final String JavaBeanSerializer = type(JavaBeanSerializer.class); // com/dongdongxia/myfastjson/serializer/JavaBeanSerializer
	static final String JavaBeanSerialzier_desc = "L" + JavaBeanSerializer + ";"; // Lcom/dongdongxia/myfastjson/serializer/JavaBeanSerializer;
	static final String SerialContext_desc = desc(SerialContext.class); // Lcom/dongdongxia/myfastjson/serializer/SerialContext;
	static final String SerializeFilterable_desc = desc(SerializerFilterable.class); // Lcom/dongdongxia/myfastjson/serializer/SerializerFilterable;
	
	static class Context {
		
		/** 定义字节码内容 begin */
		static final int serializer = 1;
		static final int obj = 2;
		static final int paramFieldName = 3;
		static final int paramFieldType = 4;
		static final int features = 5;
		static int fieldName = 6;
		static int original = 7;
		static int processValue = 8;
		/** 定义字节码内容 end */
		
		
		private final FieldInfo[] getters;
		private final String className;
		private final SerializeBeanInfo beanInfo;
		private final boolean writeDirect;
		
		private Map<String, Integer> variants = new HashMap<String, Integer>();
		private int variantIndex = 9; // 变异指数
		private boolean nonContext;
		
		public Context(FieldInfo[] getters, SerializeBeanInfo beanInfo, String className, boolean writeDirect, boolean nonContext) {
			this.getters = getters;
			this.className = className;
			this.beanInfo = beanInfo;
			this.writeDirect = writeDirect;
			this.nonContext = nonContext;
		}
		
		
		/**
		 * 
		 * <p>Title: createJavaBeanSerializer</p>
		 * <p>Description: 使用字节码, 创建JavaBean序列化器</p>
		 * @param beanInfo
		 * @return
		 * @author java_liudong@163.com  2017年6月15日 上午11:54:08
		 */
		public JavaBeanSerializer createJavaBeanSerializer(SerializeBeanInfo beanInfo) throws Exception{
			/* 先检测 该对象是否为私有对象 */
			Class<?> clazz = beanInfo.beanType;
			if (clazz.isPrimitive()) {
				throw new JSONException("unsupported class " + clazz.getName() + ", 不支持对象是私有的, 私有的对象, 你还想序列化啊");
			}
			
			JSONType jsonType = clazz.getAnnotation(JSONType.class);
			
			FieldInfo[] unsortedGetters = beanInfo.fields; // 未被排序的所有字段FieldInfo对象
			
			for (FieldInfo fieldInfo : unsortedGetters) {
				// 字段 || 字段方法 || 字段是否为接口 ; 最后一个一般为false, 因为,字段的对象一般不是接口
				if (fieldInfo.field == null && fieldInfo.method != null && fieldInfo.method.getDeclaringClass().isInterface()) { 
					return new JavaBeanSerializer(clazz);
				}
			}
			
			FieldInfo[] getters = beanInfo.sortedFields; // 排序的字段对象集合
			
			boolean nativeSorted = beanInfo.sortedFields == beanInfo.fields; // 检测, 非排序 和 排序集合是否一致, 一般为false
			
			if (getters.length > 256) { // 如果, 字段个数, 大于 256, 直接使用 JavaBeanSerializer(); 对象
				return new JavaBeanSerializer(clazz);
			}
			
			/** 下面是使用ASM 进行生成Java字节码, 动态生成解析 JSON解析对象 begin */
			String className = "ASMSerializer_" + seed.incrementAndGet() + "_" + clazz.getSimpleName(); // ASMSerializer_1_UserName
			String packageName = ASMSerializerFactory.class.getPackage().getName(); // com.dongdongxia.myfastjson.serializer
			String classNameType = packageName.replace('.', '/') + "/" + className; // com/dongdongxia/myfastjson/serializer/ASMSerializer_1_UserName
			String classNameFull = packageName + "." + className; // com.dongdongxia.myfastjson.serializer.ASMSerializer_1_UserName
			
			ClassWriter cw = new ClassWriter();
			// 定义类的形式
			/**
			 * public class com.dongdongxia.myfastjson.serializer.ASMSerializer_1_UserName extends com.dongdongxia.myfastjson.serializer.JavaBeanSerializer implements com.dongdongxia.myfastjson.serializer.ObjectSerializer {
				}
			 */
			cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, classNameType, JavaBeanSerializer, new String[] {ObjectSerializer});
			
			// 遍历所有的get方法, 获取字段的详情, 定义对象中的字段
			for (FieldInfo fieldInfo : getters) {
				if (fieldInfo.fieldClass.isPrimitive() || fieldInfo.fieldClass == String.class) {  // 基础类型和String类型, 不做处理
					continue ;
				}
				
				/**
				 * public java.lang.reflect.Type addresses_asm_fieldType;
 				 *	public java.lang.reflect.Type money_asm_fieldType;
  				 *	public java.lang.reflect.Type user_fieldType;
  				 *	public java.lang.reflect.Type sex_fieldType;
				 */
				new FieldWriter(cw, ACC_PUBLIC, fieldInfo.name + "_asm_fieldType", "Ljava/lang/reflect/Type;").visitEnd();
				
				/**
				 * public com.dongdongxia.myfastjson.serializer.ObjectSerializer sex_asm_list_item_ser_;
				 */
				if (List.class.isAssignableFrom(fieldInfo.fieldClass)) { // 检测, 对象是否是List的子类, 数组不是List的子类
					new FieldWriter(cw, ACC_PUBLIC, fieldInfo.name + "_asm_list_item_ser_", ObjectSerializer_desc).visitEnd();;
				}
				
				/**
				 * public com.dongdongxia.myfastjson.serializer.ObjectSerializer addresses_asm_ser_;
				 * public com.dongdongxia.myfastjson.serializer.ObjectSerializer money_asm_ser_;
				 * public com.dongdongxia.myfastjson.serializer.ObjectSerializer addresses_asm_ser_;
				 * public com.dongdongxia.myfastjson.serializer.ObjectSerializer user_asm_ser_;
				 * public com.dongdongxia.myfastjson.serializer.ObjectSerializer sex_asm_ser_;
				 */
				new FieldWriter(cw, ACC_PUBLIC, fieldInfo.name + "_asm_ser_", ObjectSerializer_desc).visitEnd();
			}
			
			// 此处定义的是构造方法mw , <init> 方法名称定义, 就为构造方法 (Lcom/dongdongxia/myfastjson/serializer/SerializeBeanInfo;)V , 这个是入参
			MethodVisitor mw = new MethodWriter(cw, ACC_PUBLIC, "<init>","(" + desc(SerializeBeanInfo.class) + ")V", null, null);
			mw.visitVarInsn(ALOAD, 0);
			mw.visitVarInsn(ALOAD, 1);
			mw.visitMethodInsn(INVOKESPECIAL, JavaBeanSerializer, "<init>", "(" + desc(SerializeBeanInfo.class) + ")V");
			
			// 下面是在构造方法中初始化上面的参数
			for (int i = 0; i < getters.length; ++i) {
				FieldInfo fieldInfo = getters[i];
				if (fieldInfo.fieldClass.isPrimitive() || fieldInfo.fieldClass == String.class) { // 所有基础类型不进行初始化
					continue ;
				}
				
				mw.visitVarInsn(ALOAD, 0);
				
				/**
				 * 下面构造的方法
				 * this.user_asm_fieldType = ASMUtils.getMethodType(UserTest.class, "getUser");
				 * this.addresses_asm_fieldType = ASMUtils.getMethodType(UserTest.class, "getAddresses");
				 * this.money_asm_fieldType = ASMUtils.getMethodType(UserTest.class, "getMoney");
				 * this.sex_asm_fieldType = ASMUtils.getMethodType(UserTest.class, "getSex");
				 */
				if (fieldInfo.method != null) {
					mw.visitLdcInsn(com.dongdongxia.myfastjson.asm.Type.getType(desc(fieldInfo.declaringClass)));
					mw.visitLdcInsn(fieldInfo.method.getName());
					mw.visitMethodInsn(INVOKESTATIC, type(ASMUtils.class), "getMethodType", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Type;");
				} else {
					mw.visitVarInsn(ALOAD, 0);
					mw.visitLdcInsn(i);
					mw.visitMethodInsn(INVOKESPECIAL, JavaBeanSerializer, "getFieldType", "(I)Ljava/lang/reflect/Type;");
				}
				
				mw.visitFieldInsn(PUTFIELD, classNameType, fieldInfo.name + "_asm_fieldType", "Ljava/lang/reflect/Type");
			}
			mw.visitInsn(RETURN);
		    mw.visitMaxs(4, 4);
		    
		    // 禁用循环引用检测
		    boolean DisableCircularReferenceDetect = false; 
		    if (jsonType != null) {
		    	for (SerializerFeature features : jsonType.serializeFeatures()) {
		    		if (features == SerializerFeature.DisableCircularReferenceDetect) {
		    			DisableCircularReferenceDetect = true;
		    			break ;
		    		}
		    	}
		    }
		    
		    
		    /**
		     * 0 write
		     * 1 writeNormal
		     * 2 writeNonContext
		     */
		    for (int i = 0; i < 3; ++i) {
		    	String methodName;
		    	boolean nonContext = DisableCircularReferenceDetect;
		    	boolean writeDirect = false;
		    	if (i == 0) {
		    		methodName = "write";
		    		writeDirect = true;
		    	} else if (i == 1) {
		    		methodName = "writeNormal";
		    	} else {
		    		writeDirect = true;
		    		nonContext = true;
		    		methodName = "writeDirectNonContext";
		    	}
		    	
		    	Context context = new Context(getters, beanInfo, classNameType, writeDirect, nonContext);
		    	mw = new MethodWriter(cw, ACC_PUBLIC, methodName, "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V", null, new String[]{"java/io/IOException"});
		    	
		    	// 此处定义方法内部的实现
		    	{
		    		Label endIf_ = new Label();
		    		mw.visitVarInsn(ALOAD, Context.obj);
		    		// serializer.writeNull();
		    		mw.visitJumpInsn(IFNONNULL, endIf_);
		    		mw.visitVarInsn(ALOAD, Context.serializer);
		    		mw.visitMethodInsn(INVOKEVIRTUAL, JSONSerializer, "writeNull", "()V");
		    		mw.visitInsn(RETURN);
		    		mw.visitLabel(endIf_);
		    	}
		    	
		    	mw.visitVarInsn(ALOAD, Context.serializer);
		    	mw.visitFieldInsn(GETFIELD, JSONSerializer, "out", SerializeWriter_desc);
		    	mw.visitVarInsn(ASTORE, context.var("out"));
		    	
		    	if (!nativeSorted && !context.writeDirect) {
		    		if (jsonType == null || jsonType.alphabetic()) {
		    			Label _else = new Label();
		    			mw.visitVarInsn(ALOAD, context.var("out"));
		    			mw.visitMethodInsn(INVOKEVIRTUAL, SerializeWriter, "isSortField", "()Z");
		    			
		    			mw.visitJumpInsn(IFNE, _else);
		    			mw.visitVarInsn(ALOAD, 0);
		    			mw.visitVarInsn(ALOAD, 1);
		    			mw.visitVarInsn(ALOAD, 2);
		    			mw.visitVarInsn(ALOAD, 3);
		    			mw.visitVarInsn(ALOAD, 4);
		    			mw.visitVarInsn(ILOAD, 5);
		    			mw.visitMethodInsn(INVOKEVIRTUAL, classNameType, "writeUnsorted", "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
		    			mw.visitInsn(RETURN);
		    			mw.visitLabel(_else);
		    		}
		    	}
		    	
		    	// isWriteDoubleQuoteDirect
		    	if (context.writeDirect && !nonContext) {
		    		Label _direct = new Label();
		    		Label _directElse = new Label();
		    		
		    		mw.visitVarInsn(ALOAD, 0);
		    		mw.visitVarInsn(ALOAD, Context.serializer);
		    		mw.visitMethodInsn(INVOKEVIRTUAL, JavaBeanSerializer, "writeDirect", "(L" + JSONSerializer + ";)Z");
		    		mw.visitJumpInsn(IFNE, _directElse);
		    		
		    		mw.visitVarInsn(ALOAD, 0);
		    		mw.visitVarInsn(ALOAD, 1);
		    		mw.visitVarInsn(ALOAD, 2);
		    		mw.visitVarInsn(ALOAD, 3);
		    		mw.visitVarInsn(ALOAD, 4);
		    		mw.visitVarInsn(ILOAD, 5);
		    		mw.visitMethodInsn(INVOKEVIRTUAL, classNameType, "writeNormal", "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
		    		mw.visitInsn(RETURN);
		    		mw.visitLabel(_directElse);
		    		mw.visitVarInsn(ALOAD, context.var("out"));
		    		mw.visitLdcInsn(SerializerFeature.DisableCircularReferenceDetect);
		    		mw.visitMethodInsn(INVOKEVIRTUAL, SerializeWriter, "isEnabled", "(I)Z");
		    		mw.visitJumpInsn(IFEQ, _direct);
		    		
		    		mw.visitVarInsn(ALOAD, 0);
		    		mw.visitVarInsn(ALOAD, 1);
		    		mw.visitVarInsn(ALOAD, 2);
		    		mw.visitVarInsn(ALOAD, 3);
		    		mw.visitVarInsn(ALOAD, 4);
		    		mw.visitVarInsn(ILOAD, 5);
		    		mw.visitMethodInsn(INVOKEVIRTUAL, classNameType, "writeDirectNonContext", "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
		    		mw.visitInsn(RETURN);
		    		mw.visitLabel(_direct);
		    	}
		    	
		    	mw.visitVarInsn(ALOAD, Context.obj);
		    	mw.visitTypeInsn(CHECKCAST, type(clazz));
		    	mw.visitVarInsn(ASTORE, context.var("entity"));
		    	generateWriteMethod(clazz, mw, getters, context);
		    	mw.visitInsn(RETURN);
		    	mw.visitMaxs(7, context.variantIndex + 2);
		    	mw.visitEnd();
		    }
		    
			/** 下面是使用ASM 进行生成Java字节码, 动态生成解析 JSON解析对象 end */
			
			
			return null;
		}
		
		/**
		 * 
		 * <p>Title: generateWriteMethod</p>
		 * <p>Description: 生成writeMehtod方法</p>
		 * @param clazz
		 * @param mw
		 * @param getters
		 * @param context
		 * @throws Exception
		 * @author java_liudong@163.com  2017年6月20日 下午6:56:38
		 */
		private void generateWriteMethod(Class<?> clazz, MethodVisitor mw, FieldInfo[] getters, Context context) throws Exception {
			Label end = new Label();
			
			int size = getters.length;
			
			if (!context.writeDirect) {
				Label endSupper_ = new Label();
				Label supper_ = new Label();
				mw.visitVarInsn(ALOAD, context.var("out"));
				mw.visitLdcInsn(SerializerFeature.PrettyFormat);
				mw.visitMethodInsn(INVOKEVIRTUAL, SerializeWriter, "isEnabled", "(I)Z");
				mw.visitJumpInsn(IFNE, supper_);
				
				boolean hasMethod = false;
				for (FieldInfo getter : getters) {
					if (getter.method != null) {
						hasMethod = true;
					}
				}
				
				if (hasMethod) {
					mw.visitVarInsn(ALOAD, context.var("out"));
					mw.visitLdcInsn(SerializerFeature.IgnoreNonFieldGetter.mask);
					mw.visitMethodInsn(INVOKEVIRTUAL, SerializeWriter, "isEnable", "(I)Z");
					mw.visitJumpInsn(IFEQ, endSupper_);
				} else {
					mw.visitJumpInsn(GOTO, endSupper_);
				}
				
				mw.visitLabel(supper_);
				mw.visitVarInsn(ALOAD, 0);
				mw.visitVarInsn(ALOAD, 1);
				mw.visitVarInsn(ALOAD, 2);
				mw.visitVarInsn(ALOAD, 4);
				mw.visitVarInsn(ILOAD, 5);
				mw.visitMethodInsn(INVOKESPECIAL, JavaBeanSerializer, "write", "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/reflect/Type;I)V");
				mw.visitInsn(RETURN);
				mw.visitLabel(endSupper_);
			}
			
			
			if (!context.nonContext) {
				Label endRef_ = new Label();
				
				mw.visitVarInsn(ALOAD, 0);
				mw.visitVarInsn(ALOAD, Context.serializer);
				mw.visitVarInsn(ALOAD, Context.obj);
				mw.visitMethodInsn(INVOKEVIRTUAL, JavaBeanSerializer, "writeReference", "(L" + JSONSerializer + ";Ljava/lang/Object;I)Z");
				mw.visitJumpInsn(IFEQ, endRef_);
				mw.visitInsn(RETURN);
				mw.visitLabel(endRef_);
			}
			
			final String writeAsArrayMethodName;
			
			if (context.writeDirect) {
				if (context.nonContext) {
					writeAsArrayMethodName = "writeAsArrayNonContext";
				} else {
					writeAsArrayMethodName = "writeAsArray";
				}
			} else {
				writeAsArrayMethodName = "writeAsArrayNormal";
			}
			
			if ((context.beanInfo.features & SerializerFeature.BeanToArray.mask) == 0) {
				Label endWriteAsArray_ = new Label();
				
				mw.visitVarInsn(ALOAD, 0); // this
				mw.visitVarInsn(ALOAD, Context.serializer); // 
				mw.visitVarInsn(ALOAD, 2); // obj
				mw.visitVarInsn(ALOAD, 3); // fieldObj
				mw.visitVarInsn(ALOAD, 4); // fieldType
				mw.visitVarInsn(ILOAD, 5); // features
				mw.visitMethodInsn(INVOKEVIRTUAL, context.className, writeAsArrayMethodName, "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
				mw.visitInsn(RETURN);
				
				mw.visitLabel(endWriteAsArray_);
			} else {
				mw.visitVarInsn(ALOAD, 0); // this
				mw.visitVarInsn(ALOAD, Context.serializer); // 
				mw.visitVarInsn(ALOAD, 2); // obj
				mw.visitVarInsn(ALOAD, 3); // fieldObj
				mw.visitVarInsn(ALOAD, 4); // fieldType
				mw.visitVarInsn(ILOAD, 5); // features
				mw.visitMethodInsn(INVOKEVIRTUAL, context.className, writeAsArrayMethodName, "(L" + JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
				mw.visitInsn(RETURN);
			}
			
			if (!context.nonContext) {
				mw.visitVarInsn(ALOAD, Context.serializer);
				mw.visitMethodInsn(INVOKEVIRTUAL, JSONSerializer, "getContext", "()" + SerialContext_desc);
				mw.visitVarInsn(ASTORE, context.var("parent"));
				
				mw.visitVarInsn(ALOAD, Context.serializer);
				mw.visitVarInsn(ALOAD, context.var("parent"));
				mw.visitVarInsn(ALOAD, Context.obj);
				mw.visitVarInsn(ALOAD, Context.paramFieldName);
				mw.visitLdcInsn(context.beanInfo.features);
				mw.visitMethodInsn(INVOKEVIRTUAL, JSONSerializer, "setContext", "(" + SerialContext_desc + "Ljava/lang/Object;Ljava/lang/Object;I)V");
			}
			
			// seperator : 下面是分词器的代码
			if (!context.writeDirect) {
				Label end_ = new Label();
				Label else_ = new Label();
				Label writeClass = new Label();
				
				mw.visitVarInsn(ALOAD, Context.serializer);
				mw.visitVarInsn(ALOAD, Context.paramFieldType);
				mw.visitVarInsn(ALOAD, Context.obj);
				mw.visitMethodInsn(INVOKEVIRTUAL, JSONSerializer, "isWriteClassName", "(Ljava/lang/reflect/Type;Ljava/lang/Object;)Z");
				mw.visitJumpInsn(IFEQ, else_);
				
				// ifnull , 如果为null
				mw.visitVarInsn(ALOAD, Context.paramFieldType);
				mw.visitVarInsn(ALOAD, Context.obj);
				mw.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class");
				mw.visitJumpInsn(IF_ACMPEQ, else_);
				
				mw.visitVarInsn(ALOAD, 0);
				mw.visitVarInsn(ALOAD, Context.serializer);
				mw.visitIincInsn(ALOAD, Context.obj);
				
				mw.visitMethodInsn(INVOKEVIRTUAL, JavaBeanSerializer, "writeClassName", "(L" + JSONSerializer + ";Ljava/lang/Object;)V");
				mw.visitVarInsn(BIPUSH, ',');
				mw.visitJumpInsn(GOTO, end_);
				
				mw.visitLabel(else_);
				mw.visitVarInsn(BIPUSH, '{');
				
				mw.visitLabel(end_);
			} else {
				mw.visitVarInsn(BIPUSH, '}');
			}
			
			mw.visitVarInsn(ISTORE, context.var("seperator"));
			
			if (!context.writeDirect) {
				mw.visitVarInsn(ALOAD, context.var("out"));
				mw.visitMethodInsn(INVOKEVIRTUAL, SerializeWriter, "isNotWriteDefaultValue", "()Z");
				mw.visitVarInsn(ISTORE, context.var("notWriteDefaultValue"));
				
				mw.visitVarInsn(ALOAD, Context.serializer);
				mw.visitVarInsn(ALOAD, 0);
				mw.visitMethodInsn(INVOKEVIRTUAL, JSONSerializer, "checkValue", "(" + SerializeFilterable_desc + ")Z"); // invoke vir tual : 调用
				mw.visitVarInsn(ISTORE, context.var("checkValue"));
				
				mw.visitVarInsn(ALOAD, Context.serializer);
				mw.visitVarInsn(ALOAD, 0);
				mw.visitMethodInsn(INVOKEVIRTUAL, JSONSerializer, "hasNameFilters", "(" + SerializeFilterable_desc + ")Z");
				mw.visitVarInsn(ISTORE, context.var("hasNameFilters"));
			}
			
			// 下面是根据字段的参数生成的 方法内部逻辑个数
			for (int i = 0; i < size; ++i) {
				FieldInfo property = getters[i];
				Class<?> propertyClass = property.fieldClass;
				
				mw.visitLdcInsn(property.name);
				mw.visitVarInsn(ASTORE, Context.fieldName);
				
				if (propertyClass == byte.class || propertyClass == short.class || propertyClass == int.class) {
					_int(clazz, mw, property, context, context.var(propertyClass.getName()), 'I');
				} else if (propertyClass == long.class) {
					_long(clazz, mw, property, context);
				} else if (propertyClass == float.class) {
					_float(clazz, mw, property, context);
				} else if (propertyClass == double.class) {
					_double(clazz, mw, property, context);
				} else if (propertyClass == boolean.class) {
					_int(clazz, mw, property, context, context.var("boolean"), 'Z');
				} else if (propertyClass == char.class) {
					_int(clazz, mw, property, context, context.var("char"), 'C');
				} else if (propertyClass == String.class) {
					_string(clazz, mw, property, context);
				} else if (propertyClass.isEnum()) {
					_enum(clazz, mw, property, context);
				} else {
					_object(clazz, mw, property, context);
				}
			}
			
			if (!context.writeDirect) {
				_after(mw, context);
			}
			
			Label _else = new Label();
			Label _end_if = new Label();
			
			mw.visitVarInsn(ILOAD, context.var("seperator"));
			mw.visitIntInsn(BIPUSH, '{');
			mw.visitJumpInsn(IF_ICMPNE, _else);
			
			mw.visitVarInsn(ALOAD, context.var("out"));
			mw.visitVarInsn(BIPUSH, '{');
			mw.visitMethodInsn(INVOKEVIRTUAL, SerializeWriter, "write", "(I)V");
			
			mw.visitLabel(_else);
			
			mw.visitVarInsn(ALOAD, context.var("out"));
			mw.visitVarInsn(BIPUSH, '}');
			mw.visitMethodInsn(INVOKEVIRTUAL, SerializeWriter, "write", "(I)V");
			
			mw.visitLabel(_end_if);
			mw.visitLabel(end);
			
			if (!context.nonContext) {
				mw.visitVarInsn(ALOAD, Context.serializer);
				mw.visitVarInsn(ALOAD, context.var("parent"));
				mw.visitMethodInsn(INVOKEVIRTUAL, JSONSerializer, "setContext", "(" + SerialContext_desc + ")V");
			}
		}
		
		/**
		 * 
		 * <p>Title: _object</p>
		 * <p>Description: 创建对象解析过程</p>
		 * @param clazz
		 * @param mw
		 * @param prperty
		 * @param context
		 * @author java_liudong@163.com  2017年6月21日 上午10:59:01
		 */
		private void _object(Class<?> clazz, MethodVisitor mw, FieldInfo prperty, Context context) {
			
		}
		
		/**
		 * 
		 * <p>Title: _enum</p>
		 * <p>Description: 创建枚举的解析过程</p>
		 * @param clazz
		 * @param mw
		 * @param fieldInfo
		 * @param context
		 * @author java_liudong@163.com  2017年6月21日 上午11:02:19
		 */
		private void _enum(Class<?> clazz, MethodVisitor mw, FieldInfo fieldInfo, Context context) {
			
		}
		
		/**
		 * 
		 * <p>Title: _int</p>
		 * <p>Description: 创建int类型的解析过程</p>
		 * @param clazz
		 * @param mw
		 * @param property
		 * @param context
		 * @param var
		 * @param type
		 * @author java_liudong@163.com  2017年6月21日 上午11:03:21
		 */
		private void _int(Class<?> clazz, MethodVisitor mw, FieldInfo property, Context context, int var, char type) {
			
		}
		
		/**
		 * 
		 * <p>Title: _long</p>
		 * <p>Description: 创建Long类型的解析过程</p>
		 * @param clazz
		 * @param mw
		 * @param property
		 * @param context
		 * @author java_liudong@163.com  2017年6月21日 上午11:04:33
		 */
		private void _long(Class<?> clazz, MethodVisitor mw, FieldInfo property, Context context) {
			
		}
		
		/**
		 * 
		 * <p>Title: _float</p>
		 * <p>Description: 创建Float类型的解析过程</p>
		 * @param clazz
		 * @param mw
		 * @param property
		 * @param context
		 * @author java_liudong@163.com  2017年6月21日 上午11:05:21
		 */
		private void _float(Class<?> clazz, MethodVisitor mw, FieldInfo property, Context context) {
			
		}
		
		/**
		 * 
		 * <p>Title: _double</p>
		 * <p>Description: 创建Double的解析过程</p>
		 * @param clazz
		 * @param mw
		 * @param property
		 * @param context
		 * @author java_liudong@163.com  2017年6月21日 上午11:06:25
		 */
		private void _double(Class<?> clazz, MethodVisitor mw, FieldInfo property, Context context) {
			
		}
		
		/**
		 * 
		 * <p>Title: _string</p>
		 * <p>Description: 创建String的解析过程</p>
		 * @param clazz
		 * @param mw
		 * @param property
		 * @param context
		 * @author java_liudong@163.com  2017年6月21日 上午11:08:09
		 */
		private void _string(Class<?> clazz, MethodVisitor mw, FieldInfo property, Context context) {
			
		}
		
		/**
		 * 
		 * <p>Title: _after</p>
		 * <p>Description: 创建解析后的过滤器</p>
		 * @param mw
		 * @param context
		 * @author java_liudong@163.com  2017年6月21日 上午11:09:34
		 */
		private void _after(MethodVisitor mw, Context context) {
			
		}
		
		/**
		 * 
		 * <p>Title: var</p>
		 * <p>Description: 获取方法的字节码信息</p>
		 * @param name
		 * @return
		 * @author java_liudong@163.com  2017年6月20日 下午4:47:00
		 */
		public int var(String name) {
			Integer i = variants.get(name);
			if (i == null) {
				variants.put(name, variantIndex++);
			}
			i = variants.get(name);
			return i.intValue();
		}
	}
}
