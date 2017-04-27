package com.dongdongxia.myfastjson;

import java.util.Properties;

import com.dongdongxia.myfastjson.asm.Opcodes;
import com.dongdongxia.myfastjson.serializer.SerializerFeature;
import com.dongdongxia.myfastjson.util.IOUtils;

public class JSONTest {

	public static void main(String[] args) {
		f();
	}
	
	
	public static void f() {
		int x = 0 << 16 | 49;
		System.out.println(x);
		
		int y = 0x0001;
		System.out.println(Opcodes.ACC_SUPER);
		
	}
	
	public static void e() {
		System.out.println(SerializerFeature.QuoteFieldNames.ordinal());
		System.out.println(SerializerFeature.QuoteFieldNames.getMask());
		
		System.out.println(SerializerFeature.UseSingleQuotes.ordinal());
		System.out.println(SerializerFeature.UseSingleQuotes.getMask());
	}
	
	public static void d(){
		int bir = -6;//Integer.parseInt("0110", 2);
		
		System.out.println(Integer.toBinaryString(bir));
		System.out.println(Integer.toBinaryString(~bir));
		System.out.println(~bir);
	}
	
	
	public static void A(){
		int x = Integer.parseInt("01111111", 2);
		int y = Integer.parseInt("00010000", 2);
		System.out.println(String.format("x = %d, y = %d", x, y));
		
		int z = x | y;
		System.out.println(String.format("x | y = %d | %d = %d", x, y, z));
		
		int n = x & y;
		System.out.println(String.format("x & y = %d & %d = %d", x, y, n));
		
		System.out.println(Integer.toBinaryString(Integer.parseInt("00000001", 2) | Integer.parseInt("00000010", 2) | Integer.parseInt("00000100", 2) | Integer.parseInt("00001000", 2)));
		
		System.out.println(Integer.parseInt("00000001", 2) 
				| Integer.parseInt("00000010", 2)
				| Integer.parseInt("00000100", 2)
				| Integer.parseInt("00001000", 2)
				& Integer.parseInt("00001000", 2));
		
		
		System.out.println(Integer.parseInt("01111", 2) & Integer.parseInt("10000", 2));
		
		
		System.out.println(15 | 18);
		
		
		System.out.println();
	}
	
	public static void b(){
		Properties properties = System.getProperties();
		System.out.println(properties);
		
		Properties p = new Properties();
		System.out.println();
		System.out.println(p);
	}
	
	public static void c(){
		IOUtils.getStringProperty("myfastjson.serializerFeatures.MapSortField");
	}
}
