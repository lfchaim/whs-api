package com.whs.whsapi.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.validator.GenericValidator;
import org.springframework.util.LinkedMultiValueMap;

public class StringUtil {

	public static boolean isNull(String val) {
		boolean ret = false;
		if( val == null )
			return true;
		if( "null".equals(val) )
			return true;
		if( val.trim().length() < 1 )
			return true;
		return ret;
	}

	public static String toRequestParam(LinkedMultiValueMap<String, String> params) {
		StringBuilder strParam = new StringBuilder();
		if (params != null && params.size() > 0) {
			List<String> keys = new ArrayList<>(params.keySet());
			for (int i = 0; i < keys.size(); i++) {
				List<String> paramValues = params.get(keys.get(i));
				for (int j = 0; j < paramValues.size(); j++) {
					if (strParam.toString().length() > 0)
						strParam.append("&");
					else
						strParam.append("?");
					strParam.append(keys.get(i)).append("=").append(paramValues.get(j));
				}
			}
		}
		return strParam.toString();
	}

	public static String toString(Object obj) {
		String ret = null;
		try {
			ret = String.valueOf(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public static String onlyNumber(String val) {
		if (val != null)
			return val.replaceAll("\\D+", "");
		else
			return null;
	}

	public static boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?"); // match a number with optional '-' and decimal.
	}

	public static boolean isDate(String dateStr, String format, Locale locale) {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(format, locale);
        try {
            LocalDate.parse(dateStr, dateFormatter);
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }
	
	public static BigDecimal bigDecimalDirt( String val ) {
		String strOk = numericDirt(val);
		if( !isNull(strOk) )
			return new BigDecimal(strOk);
		else
			return null;
	}

	public static Double doubleDirt( String val ) {
		String strOk = numericDirt(val);
		if( !isNull(strOk) )
			return Double.parseDouble(strOk);
		else
			return null;
	}
	
	public static String numericDirt( String val ) {
		if( isNull(val) )
			return null;
		// Tentando converter inicialmente
		try {
			BigDecimal bg = new BigDecimal(val);
			return bg.toString();
		} catch (Exception e) {
			// TODO: handle exception
		}
		// Tratamento de duas virgulas - Jesus
		if( val.indexOf(",") != val.lastIndexOf(",") ) {
			val = val.substring(0,val.lastIndexOf(","));
		}
		// Formato Brasileiro
		if( val.indexOf(",") > -1 && val.indexOf(".") > -1 ) {
			// Esta com virgula na casa decimal
			if( val.lastIndexOf(",") > val.lastIndexOf(".") ) {
				val = StringUtils.replace(val, ".", "");
				val = StringUtils.replace(val, ",", ".");
			} else if( val.indexOf(".") > -1 ) {
				val = StringUtils.replace(val, ".", "");
			}
		} else if( val.indexOf(",") > -1 ) {
			val = StringUtils.replace(val, ",", ".");
		}
		if( val.indexOf(",") > -1 ) {
			val = StringUtils.replace(val, ",", ".");
		}
		// Para evitar multiple points
		if( val.indexOf(".") != val.lastIndexOf(".") ) {
			val = removeMultiplePoints(val);
		}
		StringBuilder sb = new StringBuilder();
		for( int i = val.length()-1; i >= 0; i-- ) {
			if( ((int)val.charAt(i) >= (int)'0' && (int)val.charAt(i) <= '9') || val.charAt(i) == ',' || val.charAt(i) == '.' || val.charAt(i) == '-' )
				sb.append(val.charAt(i));
		}
		sb = sb.reverse();
		return sb.toString();
	}

	public static String removeMultiplePoints( String val ) {
		StringBuilder sb = new StringBuilder();
		if( isNull(val) )
			val = "";
		boolean notFound = false;
		for( int i = val.length()-1; i >= 0; i-- ) {
			if( val.charAt(i) == '.' ) {
				if( !notFound ) {
					sb.append(val.charAt(i));
					notFound = true;
				}
			} else {
				if( ((int)val.charAt(i) >= (int)'0' && (int)val.charAt(i) <= '9') || val.charAt(i) == '-' )
					sb.append(val.charAt(i));
			}
		}
		sb = sb.reverse();
		return sb.toString();
	}
	
	public static String objToString( Object obj ) {
		if( obj == null )
			return null;
		return String.valueOf(obj);
	}
	
	public static Integer toInteger( Object obj ) {
		if( obj == null )
			return 0;
		return Integer.parseInt(objToString(obj));
	}
	
	public static Short toShort( Object obj ) {
		if( obj == null )
			return 0;
		return Short.valueOf(objToString(obj));
	}

	public static Long toLong( Object obj ) {
		if( obj == null )
			return 0L;
		return Long.parseLong(objToString(obj));
	}
	
	public static Float toFloat( Object obj ) {
		if( obj == null )
			return 0F;
		return Float.parseFloat(objToString(obj));
	}

	public static Double toDouble( Object obj ) {
		if( obj == null )
			return 0D;
		return Double.parseDouble(objToString(obj));
	}

	public static Timestamp toTimestamp( String value ) {
		Timestamp ret = null;
		if( value == null )
			return ret;
		List<String> formats = Arrays.asList("yyyy-MM-dd HH:mm:ss.SSS","yyyy-MM-dd HH:mm:ss","yyyy-MM-dd HH:mm","yyyy-MM-dd HH","yyyy-MM-dd","dd/MM/yyyy HH:mm:ss.SSS","dd/MM/yyyy HH:mm:ss","dd/MM/yyyy");
		Date date = null;
		for( String format: formats ) {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			try {
				date = sdf.parse(String.valueOf(value));
				if( date != null ) {
					ret = new Timestamp(date.getTime());
					return ret;
				}
			}catch( Exception e ) {
				
			}
		}
		return ret;
	}
	
	public static Timestamp toTimestamp( Object obj ) {
		Timestamp ret = null;
		if( obj == null )
			return ret;
		if( obj instanceof String ) {
			return toTimestamp(String.valueOf(obj));
		} else if( obj instanceof Date ) {
			try {
				Date date = (Date)obj;
				ret = new Timestamp(date.getTime());
				if( ret != null )
					return ret;
			}catch(Exception e) {
				
			}
		}
		return ret;
	}
	
	public static Boolean toBoolean( Object obj ) {
		String val = toString(obj);
		if( val == null )
			return false;
		if( "1".equals(val) )
			return true;
		return Boolean.valueOf(val);
	}
	
	public static String normalizeInfra( String column ) {
		if( column == null )
			return column;
		if( column.indexOf("Infra") > -1 )
			column = column.replaceAll("Infra", "");
		StringBuilder sb = new StringBuilder();
		for( int i = 0; i < column.length(); i++ ) {
			int charCode = (int)column.charAt(i);
			// Caracter maiusculo
			if( charCode >= 65 && charCode <= 90 ) {
				if( i > 0 ) {
					sb.append(" ");
				}
			}
			sb.append(column.charAt(i));
		}
		return sb.toString();
	}
	
	public static String fillChar( String value, char ch, int quantity, boolean left ) {
		StringBuilder sb = new StringBuilder();
		for( int i = 0; i < quantity; i++ ) {
			sb.append(ch);
		}
		if( left )
			return sb.toString()+value;
		else
			return value+sb.toString();
	}
	
	public static String removeSpace( String value ) {
		StringBuilder sb = new StringBuilder();
		for( int i = 0; value != null && i < value.length(); i++ ) {
			if( value.charAt(i) != ' ' )
				sb.append(value.charAt(i));
		}
		return sb.toString();
	}
	
	public static String normalize( String value ) {
		String ret = null;
		if( value == null )
			return ret;
		ret = value.toLowerCase();
		ret = ret.trim();
		ret = StringUtils.stripAccents(ret);
		ret = ret.replaceAll(" +", " ");
		ret = StringUtils.replace(ret, " ", "-");
		return ret;
	}
		
	public static Object convert(String value) {
		Object ret = value;
		try {
			if (NumberUtils.isCreatable(value)) {
				if (StringUtils.containsAny(value, '.')) {
					int idxPoint = value.lastIndexOf(".");
					if ((value.length() - idxPoint - 1) > 4)
						ret = Double.parseDouble(value);
					else
						ret = Float.parseFloat(value);
				} else {
					if (value.length() > 9)
						ret = Long.parseLong(value);
					else
						ret = Integer.parseInt(value);
				}
			} else {
				if( isISO8601(value) ) {
					value = value.substring(0,23);
					DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
					Date date = df1.parse(value);
					Timestamp ts = new Timestamp(date.getTime()); 
					ret = ts;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if( ret instanceof String ) {
			String valRet = (String)ret;
			if( valRet.contains("*") ) {
				valRet = StringUtils.replace(valRet, "*", "%");
				ret = valRet;
			}
		}
		return ret;
	}
	
	public static boolean isISO8601( String value ) {
		boolean ret = false;
		if( value.length() == 29 || value.length() == 23 ) {
			if( (value.charAt(4) == '-') && (value.charAt(7) == '-') ) {
				return true;
			}
		}
		return ret;
	}
	
	/**
	 * 
	 * @param sql - SQL a ser executado. Ex: select from customer where id = ${:customer_id}
	 * @param paramName - Lista de parametros presentes. Ex: customer_id, customer_name
	 * @return Map com resultado: SQL ajustado para PreparedStament, indices iniciais e finais dos parametros e parametros faltantes
	 */
	private static Map<String,Object> extractSQLParam( String sql, List<String> paramName ) {
		Map<String,Object> map = new LinkedHashMap<String,Object>();
		// Verifica se no SQL tem alguma configuracao de parametro, mas nao esta na lista paramName
		String[] str = StringUtils.substringsBetween(sql, "${:", "}");
		List<String> listMissing = new ArrayList<String>();
		for( int i = 0; str != null && i < str.length; i++ ) {
			if( !paramName.contains(str[i]) )
				listMissing.add(str[i]);
		}
		if( !listMissing.isEmpty() )
			map.put("missing", listMissing);
		String newSQL = sql;
		List<Map<String,List<int[]>>> listStarEndMap = new ArrayList<Map<String,List<int[]>>>();
		for( int i = 0; i < paramName.size(); i++ ) {
			String par = paramName.get(i);
			String parIni = "${:"+par;
			String parEnd = par+"}";
			if( sql.contains(parIni) ) {
				List<int[]> listStarEnd = new ArrayList<int[]>();
				int idx = 0;
				int idxParam = sql.indexOf(parIni);
				do {
					int endIdxParam = sql.indexOf(parEnd,idxParam)+parEnd.length();
					//System.out.println("ini: "+idxParam+" end: "+endIdxParam);
					listStarEnd.add(new int[] {idxParam,endIdxParam});
					idxParam = sql.indexOf(parIni,endIdxParam);
				}while( idx < idxParam );
				Map<String,List<int[]>> mapItem = new LinkedHashMap<String,List<int[]>>();
				mapItem.put(paramName.get(i),listStarEnd);
				listStarEndMap.add(mapItem);
				newSQL = StringUtils.replace(newSQL, parIni+"}", "?");
			}
		}
		map.put("newSql", newSQL);
		map.put("listStarEnd", listStarEndMap);
		return map;
	}
	
	public static List<Integer> getIndexes( String value, String toFind ) {
		List<Integer> ret = new ArrayList<>();
		int curIdx = -1;
		int idx = -1;
		do {
			idx = value.indexOf(toFind,curIdx);
			if( idx > -1 ) {
				ret.add(idx);
				curIdx = idx+1;
			}
		} while ( idx > -1 );
		return ret;
	}
	
	public static String listToString( List<String> listData, String delimiter ) {
		StringBuilder sb = new StringBuilder();
		for( int i = 0; listData != null && i < listData.size(); i++ ) {
			if( i > 0 ) {
				if( delimiter != null )
					sb.append(delimiter);
			}
			sb.append(listData.get(i));
		}
		return sb.toString();
	}
	
	public static String info( String value, Object... objects ) {
		if( objects == null )
			return null;
		String retVal = new String(value);
		int len = objects.length;
		for( int i = 0; i < len; i++ ) {
			if( retVal.indexOf("{}") > -1 ) {
				retVal = StringUtils.replaceOnce(retVal, "{}", StringUtil.toString(objects[i]));
			}
		}
		return retVal;
	}

	public static String dateMatcher( String date ) {
		String ret = null;
		List<String[]> matcher = new ArrayList<>();
		matcher.add(new String[]{"^[0-9]{4}\\-[0-9]{2}\\-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}$", "yyyy-MM-dd HH:mm:ss.SSS"});
		matcher.add(new String[]{"^[0-9]{4}\\/[0-9]{2}\\/[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}$", "yyyy/MM/dd HH:mm:ss.SSS"});
		matcher.add(new String[]{"^[0-9]{4}\\-[0-9]{2}\\-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}$",          "yyyy-MM-dd HH:mm:ss"});
		matcher.add(new String[]{"^[0-9]{4}\\/[0-9]{2}\\/[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}$",          "yyyy/MM/dd HH:mm:ss"});
		matcher.add(new String[]{"^[0-9]{4}\\-[0-9]{2}\\-[0-9]{2} [0-9]{2}:[0-9]{2}$",                   "yyyy-MM-dd HH:mm"});
		matcher.add(new String[]{"^[0-9]{4}\\/[0-9]{2}\\/[0-9]{2} [0-9]{2}:[0-9]{2}$",                   "yyyy/MM/dd HH:mm"});
		matcher.add(new String[]{"^\\d{4}-\\d{2}-\\d{2}$",                                               "yyyy-MM-dd"});
		matcher.add(new String[]{"^[0-9]{4}\\/[0-9]{2}\\/[0-9]{2}$",                                     "yyyy/MM/dd"});
		matcher.add(new String[]{"^[0-9]{2}\\/[0-9]{2}\\/[0-9]{4}$",                                     "dd/MM/yyyy"});
		matcher.add(new String[]{"^[0-9]{2}\\-[0-9]{2}\\-[0-9]{4}$",                                     "dd-MM-yyyy"});
		matcher.add(new String[]{"^[0-9]{2}\\/[0-9]{4}$",                                                "MM/yyyy"});
		matcher.add(new String[]{"^[0-9]{1}\\/[0-9]{4}$",                                                "M/yyyy"});
		matcher.add(new String[]{"^[0-9]{2}\\/[0-9]{2}$",                                                "MM/yy"});
		for( int i = 0; i < matcher.size(); i++ ) {
			Pattern pat = Pattern.compile(matcher.get(i)[0]);
			if( pat.matcher(date).matches() ) {
				return matcher.get(i)[1];
			}
		}
		return ret;
	}
	
	public static String fromList( List<String> list ) {
		if( list == null )
			return null;
		StringBuilder sb = new StringBuilder();
		for( String item: list ) {
			sb.append(item);
		}
		return sb.toString();
	}
	
	public static void main( String[] args ) {
		Object obj = "2011-05-01 13";
		Timestamp ts = toTimestamp(obj);
		System.out.println(ts.toString());
		
		String dec = "23.344.76";
		dec = "14.026.942,75,37";
		dec = "-4.6. 652256219";
		//dec = "0,00";
		//dec = "1.742. 81";
		dec = "-46.6493872035766";
		System.out.println(removeMultiplePoints(dec));
		Double n = doubleDirt(dec);
		System.out.println("double: "+n);
		int dig = 8;
		BigDecimal bd = BigDecimal.valueOf(n);
	    bd = bd.setScale(dig, RoundingMode.HALF_UP);
	    n = bd.doubleValue();
	    System.out.println("double: "+n);
	    
	    String val = "-9.470537618104449E-4";
	    BigDecimal bg = new BigDecimal(val);
	    bg = bigDecimalDirt(val);
	    System.out.println(bg.toString());
	    
	    val = " Rua São Sebastião   das  Astúrias ";
	    val = normalize(val);
	    System.out.println("Normalize: "+val);
	    
	    String sql = "select * from tblbairro where ST_Contains (ST_GeomFromText(${:polygon_text}, 4326), geobairro) and ( city = ${:city_name} or city = ${:city_name} )  and uf = ${:uf}";
		List<String> par = Arrays.asList("polygon_text","city_name");
		Map<String,Object> map = extractSQLParam(sql, par);
		System.out.println(JSONUtil.toJSONString(map));
		
		val = "ST_DWithin(ST_geometryfromtext(?, 0),location::geometry,(?::decimal / 100))";
		List<Integer> listIdx = getIndexes(val, "?");
		System.out.println(JSONUtil.toJSONString(listIdx));
		
		String date = "07/23";
		System.out.println("date: "+date+" isDate: "+GenericValidator.isDate(date,"MM/yy",true)+" isDate2: "+GenericValidator.isDate(date,Locale.getDefault()));
		date = DateUtil.changeDateFormat(date, "MM/yy", "MMM/yy");
		System.out.println("CHANGED date: "+date);
		
		String dateRegex = "2023-01-03";
		System.out.println("dateMatcher: "+dateMatcher(dateRegex));
		dateRegex = "2023/04/05 22:36:12.123";
		System.out.println("dateMatcher: "+dateMatcher(dateRegex));
		dateRegex = "22/05/1972";
		System.out.println("dateMatcher: "+dateMatcher(dateRegex));
		dateRegex = "05/72";
		System.out.println("dateMatcher: "+dateMatcher(dateRegex));
		
	}

}
