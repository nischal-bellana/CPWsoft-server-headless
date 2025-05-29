package com.utils;

import java.util.Random;

public class OtherUtils {
	private static final long max_ID_no = 2176782336l;
	private static Random random = new Random();
	private static final char[] IDletters = {'0', '1', '2', '3', '4'
			, '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
			, 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q'
			, 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
	
	
	public static String generateID() {
		StringBuilder ID = new StringBuilder();
		long idNo = Math.abs(random.nextLong()) % max_ID_no;
		while(idNo!=0) {
			char c = IDletters[(int) (idNo%36)];
			ID.append(c);
			idNo/=36;
		}
		return ID.toString();
	}
}
