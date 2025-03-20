package com.utils;

public class ParsingUtils {
	
	public static void appendData(String data, StringBuilder str) {
		str.append(data.length());
		str.append('&');
		str.append(data);
	}
	
	public static int getBeginIndex(int start, String str, char stopchar) {
		for(int i = start; ; i++) {
			if(str.charAt(i) == stopchar) {
				return i + 1;
			}
		}
	}
	
	public static int getBeginIndex(int start, int end, String str, char stopchar) {
		for(int i = start; ; i++) {
			if(i >= end || str.charAt(i) == stopchar) {
				return i + 1;
			}
		}
	}
		
	public static float parseFloat(int start, int end, String str) {
		
		int left = 0;
		boolean sign = true;
		
		int i = start;
		
		if(str.charAt(i) == '-' || str.charAt(i) == '+') {
			sign = str.charAt(i) == '+';
			i++;
		}
		
		for(; i < end; i++) {
			char c = str.charAt(i);
			
			if(c == '.') break;
			
			int digit = c - '0';
			
			left *= 10;
			left += digit;
			
		}
		
		int right = 0;
		int divisor = 1;
		i++;
		
		for(; i < end; i++) {
			char c = str.charAt(i);
			
			if(c == 'e' || c == 'E') {
				break;
			}
			
			int digit = c - '0';
			
			right *= 10;
			right += digit;
			divisor *= 10;
		}
		
		float res = left + (right / ((float)divisor));
		
		if(i < end) {
			i++;
			
			int exp = parseInt(i, end, str);
			boolean multiply = exp > 0;
			if(!multiply) exp *= -1;
			
			 int multiplicant = 1;
			
			for(int j = 0; j < exp; j++) {
				multiplicant *= 10;
			}
			
			if(multiply) {
				res *= multiplicant;
			}
			else {
				res /= multiplicant;
			}
			
		}
		
		return (sign ? res : -res);
		
	}
	
	public static int parseInt(int start, int end, String str) {
		int x = 0;
		boolean sign = true;
		int i = start;
		
		if(str.charAt(i) == '+' || str.charAt(i) == '-') {
			sign = str.charAt(i) == '+';
			i++;
		}
		
		for(; i < end; i++) {
			int digit = str.charAt(i) - '0';
			
			x *= 10;
			x += digit;
			
		}
		
		return sign ? x : -x;
	}
	
	public static boolean requestCheck(int start, String str, String requesthead) {
		if(str.charAt(start) != requesthead.charAt(0)) return false;
		
		if(str.charAt(start + 1) != requesthead.charAt(1)) return false;
		
		return true;
	}
	
}
