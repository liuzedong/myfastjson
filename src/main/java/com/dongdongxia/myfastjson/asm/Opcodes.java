package com.dongdongxia.myfastjson.asm;
/**
 * 
 * <P>Description: 操作码</P>
 * @ClassName: Opcodes
 * @author java_liudong@163.com  2017年4月27日 下午2:36:17
 */
public interface Opcodes {
	int T_INT = 10;
	
//	int v1_1 = 3 << 16 | 45;
//	int v1_2 = 0 << 16 | 46;
//	int v1_3 = 0 << 16 | 47;
//	int v1_4 = 0 << 16 | 48;
	// 就是49 , 因为前面的 << 左移, 成为0 | 49 = 49
	int V1_5 = 0 << 16 | 49;
//	int V1_6 = 0 << 16 | 50;
//	int V1_7 = 0 << 16 | 51;
	
	// 1
	int ACC_PUBLIC = 0x0001;
	// 32 , 16进制, 2 * 16 = 32
	int ACC_SUPER = 0x0020;
	
	// 常量码
	int ACONST_NULL = 1;
	int ICONST_0 = 3;
	int ICONST_1 = 4;
	int LCONST_0 = 9;
	int LCONST_1 = 10;
	int FCONST_0 = 11;
	int DCONST_0 = 14;
	int BIPUSH = 16;
//	int SIPUSH = 17;
//	int LDC = 18;
//	int LDC_W = 19;
//	int LDC2_W = 20;
	int ILOAD = 21;
	int LLOAD = 22;
	int FLOAD = 23;
	int DLOAD = 24;
	int ALOAD = 25;
	
	int ISTORE = 54;
	int LSTORE = 55;
	int FSTORE = 56;
	int DSTORE = 57;
	int ASTORE = 58;
	int IASTORE = 79;
	
	int POP = 87;
//	int POP2 = 88
	int DUP = 89;
	
	int IADD = 96;
	
//	int ISUB = 100;
	
	int IAND = 126;
	
//	int LAND = 127;
	
	int IOR = 128;
	
//	int LOR = 129;
//	int IXOR = 130;
//	int LXOR = 131;
//	int IINC = 132;
	
	int LCMP = 148;
	int FCMPL = 149;
	int DCMPL = 151;
	int IFEQ = 153;
	int IFNE = 154;
	int IFLE = 158;
	int IF_ICMPEQ = 159;
	int IF_ICMPNE = 160;
	int IF_ICMPLT = 161;
	int IF_ICMPGE = 162;
	int IF_ICMPGT = 163;
	int IF_ACMPEQ = 165;
	int IF_ACMPNE = 166;
	int GOTO = 167;
	int RET = 169;
	int ARETURN = 176;
	int RETURN = 177;
	int GETSTATIC = 178;
	int GETFIELD = 180;
	int PUTFIELD = 181;
	int INVOKEVIRTUAL = 182;
	int INVOKESPECIAL = 183;
	int INVOKESTATIC = 184;
	int INVOKEINTERFACE = 185;
//	int INVOKEDYNAMIC = 186;
	int NEW = 187;
	int NEWARRAY = 188;
//	int ANEWARRAY = 189;
//	int ARRAYLENGTH = 190;
//	int ATHROW = 191;
	int CHECKCAST = 192;
	int INSTANCEOF = 193;
	
	int ifnull = 198;
	int IFNONNULL = 199;
//	int GOTO_W = 200;
//	int JSR_W = 201;
	
}