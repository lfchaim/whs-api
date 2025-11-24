package com.whs.whsapi.util;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

	public static String getShort( ) {
		SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
		return sdf.format(new Date());
	}
	
	public static String getFull( ) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		return sdf.format(new Date());
	}
	
	public static Date parseDate( String value, String format ) {
		DateFormatSymbols dfs = new DateFormatSymbols();
		dfs.setShortMonths(new String[]{
				   "jan", "fev", "mar", "abr", "mai", "jun",
				   "jul", "ago", "set", "out", "nov", "dez"});
		SimpleDateFormat sdf = new SimpleDateFormat(format,dfs);
		Date ret = null;
		try {
			ret = sdf.parse(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public static String changeDateFormat( String date, String originFormat, String newFormat ) {
		DateFormatSymbols dfs = new DateFormatSymbols();
		dfs.setShortMonths(new String[]{
				   "jan", "fev", "mar", "abr", "mai", "jun",
				   "jul", "ago", "set", "out", "nov", "dez"});
		SimpleDateFormat sdf = new SimpleDateFormat(originFormat,dfs);
		SimpleDateFormat sdf2 = new SimpleDateFormat(newFormat,dfs);
		String ret = date;
		try {
			ret = sdf2.format(sdf.parse(date));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public static void main( String[] args ) {
		Date d = parseDate("22/05/1972", "dd/MM/yyyy");
		System.out.println(d.toString());
		d = parseDate("05/72", "MM/yy");
		System.out.println(d.toString());
		System.out.println(changeDateFormat("05/72", "MM/yy", "MMM/yyyy"));
	}

}
