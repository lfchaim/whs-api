package com.whs.whsapi.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class ObjectUtil {

	public static boolean isNullOrEmpty( Object obj ) {
		boolean ret = false;
		if( obj == null ) {
			ret =  true;
		} else if( obj instanceof String ) {
			ret = StringUtil.isNull((String)obj);
		} else if( obj instanceof Map ) {
			Map map = (Map)obj;
			ret = map.isEmpty();
		} else if( obj instanceof List ) {
			List list = (List)obj;
			ret = list.size() < 1;
		} else {
			ret = obj.toString().length() < 1;
		}
		return ret;
	}
	
	public static void main(String[] args) {
		Long id = null;
		System.out.println(isNullOrEmpty(id));
		String strId = "";
		System.out.println(isNullOrEmpty(strId));
		List list = null;
		System.out.println(isNullOrEmpty(list));
		list = new ArrayList<>();
		System.out.println(isNullOrEmpty(list));
		list.add("1");
		System.out.println(isNullOrEmpty(list));
		Map map = new LinkedHashMap<>();
		System.out.println(isNullOrEmpty(map));
		map.put("id", 1000);
		System.out.println(isNullOrEmpty(map));
	}
}
