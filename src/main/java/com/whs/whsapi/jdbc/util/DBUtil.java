package com.whs.whsapi.jdbc.util;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.LinkedMultiValueMap;

import com.google.common.base.Joiner;
import com.whs.whsapi.jdbc.meta.Column;
import com.whs.whsapi.jdbc.meta.DBMetaUtil;
import com.whs.whsapi.jdbc.meta.PK;
import com.whs.whsapi.jdbc.meta.Table;
import com.whs.whsapi.util.JSONUtil;
import com.whs.whsapi.util.ObjectUtil;
import com.whs.whsapi.util.StringUtil;

public class DBUtil {

	public List<Map<String,Object>> listByTable(Connection conn, String table, LinkedMultiValueMap<String, String> param, int limit ){
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		List<Object> listSet = new ArrayList<Object>();
		
		String sql = "select * from "+table+" where 1=1 ";
		
		Table t = null;
		try {
			t = DBMetaUtil.loadTable(conn, null, table, null);
		}catch(SQLException e) {
			e.printStackTrace();
		}
		
		StringBuilder sb = new StringBuilder(sql);
		if( param != null && param.size() > 0 ) {
			Iterator<String> it = param.keySet().iterator();
			while( it.hasNext() ) {
				String key = it.next();
				if( key.equalsIgnoreCase("filter") ) {
					for (String value : param.get(key)) {
						String[] parts = value.split(",", 3);
						String columnName = parts[0];
						
						Column c = DBMetaUtil.getColumn(t.getColumn(), columnName);
						String cast = "";
						if( "jsonb".equals(c.getTypeName()) ) {
							cast = "::text";
						}
						
						Object valueParam = StringUtil.convert(parts[2]);
						String command = parts[1];
						switch (command) {
						case "ics":
							sb.append(" and lower(").append(parts[0]).append(cast).append(") like lower(?) ");
							listSet.add("%"+parts[2]+"%");
							break;
						case "ieq":
							sb.append(" and lower(").append(parts[0]).append(") = lower(?) ");
							listSet.add(valueParam);
							break;
						case "rx":
							sb.append(" and ").append(parts[0]).append(" REGEXP ? ");
							listSet.add(parts[2]);
							break;
						case "cs":
							sb.append(" and ").append(parts[0]).append(cast).append(" like ? ");
							listSet.add("%"+parts[2]+"%");
							break;
						case "sw":
							sb.append(" and ").append(parts[0]).append(cast).append(" like ? ");
							listSet.add(parts[2]+"%");
							break;
						case "ew":
							sb.append(" and ").append(parts[0]).append(cast).append(" like ? ");
							listSet.add("%"+parts[2]);
							break;
						case "eq":
							sb.append(" and ").append(parts[0]).append(" = ? ");
							listSet.add(valueParam);
							break;
						case "lt":
							sb.append(" and ").append(parts[0]).append(" < ? ");
							listSet.add(valueParam);
							break;
						case "le":
							sb.append(" and ").append(parts[0]).append(" <= ? ");
							listSet.add(valueParam);
							break;
						case "ge":
							sb.append(" and ").append(parts[0]).append(" >= ? ");
							listSet.add(valueParam);
							break;
						case "gt":
							sb.append(" and ").append(parts[0]).append(" > ? ");
							listSet.add(valueParam);
							break;
						case "bt":
							sb.append(" and ").append(parts[0]).append(" between ? and ? ");
							String[] parts2 = parts[2].split(",", 2);
							listSet.add(StringUtil.convert(parts2[0]));
							listSet.add(StringUtil.convert(parts2[1]));
							break;
						case "in":
							String[] partsn = parts[2].split(",");
							sb.append(" and ").append(parts[0]).append(" in ( ");
							for( int j = 0; j < partsn.length; j++ ) {
								if( j > 0 )
									sb.append(",");
								sb.append("?");
								listSet.add(StringUtil.convert(partsn[j]));
							}
							sb.append(") ");
							break;
						case "inc":
							String[] partsi = parts[2].split(",");
							sb.append(" and ( ");
							for( int j = 0; j < partsi.length; j++ ) {
								if( j > 0 ) {
									sb.append(" or ");
								}
								sb.append(" ").append(parts[0]).append(cast).append(" ilike ? ");
								listSet.add("%"+partsi[j]+"%");
							}
							sb.append(") ");
							break;

						case "is":
							sb.append(" and ").append(parts[0]).append(" is null ");
							break;
						}
					}
				} else if( key.equalsIgnoreCase("json") ) {
					for (String value : param.get(key)) {
						String[] parts = value.split(",", 4);
						Object valueParam = StringUtil.convert(parts[3]);
						String command = parts[2];
						String colJSON = parts[0];
						String colName = parts[1];
						switch (command) {
						case "ics":
							sb.append(" and ").append(colJSON).append(" ->> '").append(colName).append("' ilike ? ");
							listSet.add("%"+parts[3]+"%");
							break;
						case "ieq":
							sb.append(" and ").append(colJSON).append(" ->> '").append(colName).append("' ilike ? ");
							listSet.add(valueParam);
							break;
						case "rx":
							sb.append(" and ").append(colJSON).append(" ->> ").append(colName).append(" REGEXP ? ");
							listSet.add(parts[3]);
							break;
						case "cs":
							sb.append(" and ").append(colJSON).append(" ->> ").append(colName).append(" ilike ? ");
							listSet.add("%"+parts[3]+"%");
							break;
						case "sw":
							sb.append(" and ").append(colJSON).append(" ->> ").append(colName).append(" ilike ? ");
							listSet.add(parts[3]+"%");
							break;
						case "ew":
							sb.append(" and ").append(colJSON).append(" ->> ").append(colName).append(" ilike ? ");
							listSet.add("%"+parts[3]);
							break;
						case "eq":
							sb.append(" and ").append(colJSON).append(" ->> ").append(colName).append(" = ? ");
							listSet.add(valueParam);
							break;
						case "lt":
							sb.append(" and ").append(colJSON).append(" ->> ").append(colName).append(" < ? ");
							listSet.add(valueParam);
							break;
						case "le":
							sb.append(" and ").append(colJSON).append(" ->> ").append(colName).append(" <= ? ");
							listSet.add(valueParam);
							break;
						case "ge":
							sb.append(" and ").append(colJSON).append(" ->> ").append(colName).append(" >= ? ");
							listSet.add(valueParam);
							break;
						case "gt":
							sb.append(" and ").append(colJSON).append(" ->> ").append(colName).append(" > ? ");
							listSet.add(valueParam);
							break;
						case "bt":
							sb.append(" and ").append(colJSON).append(" ->> ").append(colName).append(" between ? and ? ");
							String[] parts2 = parts[3].split(",", 2);
							listSet.add(StringUtil.convert(parts2[0]));
							listSet.add(StringUtil.convert(parts2[1]));
							break;
						case "in":
							String[] partsn = parts[3].split(",");
							sb.append(" and ").append(colJSON).append(" ->> ").append(colName).append(" in ( ");
							for( int j = 0; j < partsn.length; j++ ) {
								if( j > 0 )
									sb.append(",");
								sb.append("?");
								listSet.add(StringUtil.convert(partsn[j]));
							}
							sb.append(") ");
							break;
						case "inc":
							String[] partsi = parts[3].split(",");
							sb.append(" and ( ");
							for( int j = 0; j < partsi.length; j++ ) {
								if( j > 0 ) {
									sb.append(" or ");
								}
								sb.append(" ").append(colJSON).append(" ->> ").append(colName).append("' ilike ? ");
								//listSet.add(StringUtil.convert(partsi[j]));
								listSet.add("%"+partsi[j]+"%");
							}
							sb.append(") ");
							break;
						case "is":
							sb.append(" and ").append(colJSON).append(" ->> ").append(colName).append(" is null ");
							break;
						}
					}
				} else if( key.equals("ordering") ) {
					sb.append(" order by ");
					for( int i = 0; i < param.get(key).size(); i++ ) {
						if( i > 0 )
							sb.append(", ");
						sb.append(param.get(key).get(i));
					}
				}
			}
		}
		sql = sb.toString();
		
		if( limit > 0 ) {
			try {
				DatabaseMetaData meta = conn.getMetaData();
				if( meta.getDriverName().toLowerCase().contains("postgre") ) {
					sql = sql + " limit "+limit;
				}
			}catch( Exception e ) {
				e.printStackTrace();
			}
		}

		list = executePreparedQuery(conn, sql, listSet);
		
		return list;
	}
	
	public List<Map<String,Object>> executePreparedQuery(Connection conn, String sql, List<Object> listSet){
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		PreparedStatement ps = null;
		StringBuilder sbLog = new StringBuilder();
		sbLog.append("DBUtil.executeQuery - ").append("listSet: ").append(JSONUtil.toJSONString(listSet)).append(" SQL: ").append(sql);
		System.out.println(sbLog.toString());
		try {
			ps = conn.prepareStatement(sql);
			for( int i = 0; listSet != null && i < listSet.size(); i++ ) {
				if( listSet.get(i) instanceof Integer ) {
					ps.setInt(i+1, (Integer)listSet.get(i));
				} else if( listSet.get(i) instanceof Long ) {
					ps.setLong(i+1, (Long)listSet.get(i));
				} else if( listSet.get(i) instanceof Double ) {
					ps.setDouble(i+1, (Double)listSet.get(i));
				} else if( listSet.get(i) instanceof Float ) { 
					ps.setFloat(i+1, (Float)listSet.get(i));
				} else if( listSet.get(i) instanceof String ) {
					ps.setString(i+1, (String)listSet.get(i));
				} else {
					ps.setObject(i+1, listSet.get(i));
				}
			}
			ResultSet rs = ps.executeQuery();
			ResultSetMetaData meta = rs.getMetaData();
			while( rs.next() ) {
				Map<String,Object> mapRes = new LinkedHashMap<String, Object>();
				for( int i = 0; i < meta.getColumnCount(); i++ ) {
					String col = meta.getColumnName(i+1);
					Object obj = rs.getObject(col);
					mapRes.put(col, obj);
				}
				list.add(mapRes);
			}
			rs.close();
		} catch (Exception e) {
			Map<String,Object> mapErr = new LinkedHashMap<>();
			mapErr.put("sql", sql);
			mapErr.put("listSet", listSet);
			//mapErr.put("stacktrace", ExceptionUtils.getStackTrace(e));
			System.err.println("SQL: "+sql);
			System.err.println("Params: "+JSONUtil.toJSONString(listSet));
			e.printStackTrace();
		} finally {
			if( ps != null ) {
				try{ps.close();}catch(SQLException e) {}
			}
		}
		return list;
	}

	public int insert(Connection conn, String tableName, Map<String,Object> mapSet ){
		int ret = 0;
		if( StringUtil.isNull(tableName) || (mapSet == null || mapSet.isEmpty()) )
			return ret;
		PreparedStatement ps = null;
		StringBuilder sb = new StringBuilder();
		try {
			List<Column> listCol = DBMetaUtil.listColumns(conn, null, tableName);
			
			List<String> listColName = new ArrayList<>();
			List<String> listColChar = new ArrayList<>();
			for( String key: mapSet.keySet() ) {
				Column colFound = getColumn(listCol, key);
				if( colFound != null ) {
					listColName.add(colFound.getColumnName());
					if( "jsonb".equals(colFound.getTypeName()) )
						listColChar.add("?::jsonb");
					else if( "geometry".equals(colFound.getTypeName()) )
						listColChar.add("ST_GeomFromText(?, 4326)"); //25832)");
					else if( "_varchar".equals(colFound.getTypeName()) )
						listColChar.add("?::_varchar");
					else
						listColChar.add("?");
				}
			}
			
			Joiner commaJoin = Joiner.on(", ");
			String sql = String.format("insert into %s (%s) values (%s) ", tableName, commaJoin.join(listColName), commaJoin.join(listColChar));
			sb.append(sql);
			
			ps = conn.prepareStatement(sb.toString());
			for( int i = 0; i < listColName.size(); i++ ) {
				setType(ps, (i+1), mapSet.get(listColName.get(i)), getColumn(listCol, listColName.get(i)));
			}
			ret = ps.executeUpdate();
		} catch (Exception e) {
			System.err.println("DBUtil.insert - SQL: "+sb.toString()+" map: "+JSONUtil.toJSONString(mapSet));
			System.err.println(e.getMessage());
			e.printStackTrace();
		} finally {
			try {ps.close();}catch(Exception e) {}
		}
		return ret;
	}

	public int update(Connection conn, String tableName, Map<String,Object> mapSet ){
		int ret = 0;
		if( StringUtil.isNull(tableName) || (mapSet == null || mapSet.isEmpty()) )
			return ret;
		PreparedStatement ps = null;
		StringBuilder sb = new StringBuilder();
		try {
			List<Column> listCol = DBMetaUtil.listColumns(conn, null, tableName);
			List<PK> listPk  = DBMetaUtil.listPK(conn, null, tableName);
			
			List<String> listColSet = new ArrayList<>();
			List<String> listPkSet = new ArrayList<>();
			List<String> listSetName = new ArrayList<>();
			
			for( String key: mapSet.keySet() ) {
				Column colFound = getColumn(listCol, key);
				PK pk = getPk(listPk, key);
				if( colFound != null && pk == null ) {
					listSetName.add(key);
					if( "jsonb".equals(colFound.getTypeName()) )
						listColSet.add(colFound.getColumnName()+" = ?::jsonb");
					else if( "geometry".equals(colFound.getTypeName()) )
						listColSet.add(colFound.getColumnName()+" = ST_GeomFromText(?, 4326)"); //25832)");
					else if( "_varchar".equals(colFound.getTypeName()) )
						listColSet.add(colFound.getColumnName()+" = ?::_varchar");
					else
						listColSet.add(colFound.getColumnName()+" = ?");
				}
			}
			for( String key: mapSet.keySet() ) {
				Column colFound = getColumn(listCol, key);
				PK pk = getPk(listPk, key);
				if ( pk != null && !listSetName.contains(key) ){
					listSetName.add(key);
					if( "jsonb".equals(colFound.getTypeName()) )
						listPkSet.add(colFound.getColumnName()+" = ?::jsonb");
					else if( "geometry".equals(colFound.getTypeName()) )
						listPkSet.add(colFound.getColumnName()+" = ST_GeomFromText(?, 4326)"); //25832)");
					else if( "_varchar".equals(colFound.getTypeName()) )
						listPkSet.add(colFound.getColumnName()+" = ?::_varchar");
					else
						listPkSet.add(colFound.getColumnName()+" = ?");
				}
			}
			
			Joiner commaJoin = Joiner.on(", ");
			Joiner andJoin = Joiner.on(" and ");
			String sql = String.format("update %s set %s where %s ", tableName, commaJoin.join(listColSet), andJoin.join(listPkSet));
			sb.append(sql);
			
			ps = conn.prepareStatement(sb.toString());
			for( int i = 0; i < listSetName.size(); i++ ) {
				setType(ps, (i+1), mapSet.get(listSetName.get(i)), getColumn(listCol, listSetName.get(i)));
			}
			ret = ps.executeUpdate();
		} catch (Exception e) {
			System.err.println("DBUtil.insert - SQL: "+sb.toString()+" map: "+JSONUtil.toJSONString(mapSet));
			System.err.println(e.getMessage());
			e.printStackTrace();
		} finally {
			try {ps.close();}catch(Exception e) {}
		}
		return ret;
	}

	private static Object setType( PreparedStatement ps, int idxPs, Object object, Column column ) throws SQLException {
		Object retVal = null;
		if( column.getTypeName() != null ) {
			if( column.getTypeName().toLowerCase().indexOf("bigserial") > -1 || column.getTypeName().toLowerCase().indexOf("bigint") > -1 ) {
				if( !ObjectUtil.isNullOrEmpty(object) ) {
					ps.setLong(idxPs, StringUtil.toLong(object));
					retVal = StringUtil.toLong(object);
				} else {
					ps.setNull(idxPs,Types.BIGINT);
				}
			} else if( column.getTypeName().toLowerCase().indexOf("int2") > -1 ) {
				if( !ObjectUtil.isNullOrEmpty(object) ) {
					ps.setShort(idxPs, StringUtil.toShort(object));
					retVal = StringUtil.toShort(object);
				} else {
					ps.setNull(idxPs,Types.INTEGER);
				}
			} else if( column.getTypeName().toLowerCase().indexOf("serial") > -1 || column.getTypeName().toLowerCase().indexOf("int") > -1 ) {
				if( !ObjectUtil.isNullOrEmpty(object) ) {
					ps.setInt(idxPs, StringUtil.toInteger(object));
					retVal = StringUtil.toInteger(object);
				} else {
					ps.setNull(idxPs,Types.INTEGER);
				}
			} else if( column.getTypeName().toLowerCase().indexOf("numeric") > -1  || column.getTypeName().toLowerCase().indexOf("real") > -1) {
				if( !ObjectUtil.isNullOrEmpty(object) ) {
					int dec = getDecimalDigits(column);
					BigDecimal bdValue = null;
					try {
						bdValue = StringUtil.bigDecimalDirt(StringUtil.toString(object));
						int scale = bdValue.scale();
						//if( scale > 0 && dec != scale ) {
						if( scale > 0 && dec < scale ) {
							bdValue = bdValue.setScale(dec, RoundingMode.HALF_UP);
						}
					}catch( Exception e ) {
						System.err.println("Error validating numeric - object: "+StringUtil.toString(object));
					}
					//ps.setDouble(idxPs, dValue);
					ps.setBigDecimal(idxPs, bdValue);
					retVal = bdValue;
				} else {
					ps.setNull(idxPs,Types.DOUBLE);
				}
			} else if( column.getTypeName().toLowerCase().indexOf("timestamp") > -1 ) {
				if( !ObjectUtil.isNullOrEmpty(object) ) {
					ps.setTimestamp(idxPs, StringUtil.toTimestamp(StringUtil.toString(object)));
					retVal = StringUtil.toTimestamp(StringUtil.toString(object));
				} else {
					ps.setNull(idxPs,Types.TIMESTAMP);
				}
			} else if( column.getTypeName().toLowerCase().indexOf("bool") > -1 ) {
				if( !ObjectUtil.isNullOrEmpty(object) ) {
					ps.setBoolean(idxPs, StringUtil.toBoolean(object));
					retVal = StringUtil.toBoolean(object);
				} else {
					ps.setNull(idxPs,Types.BOOLEAN);
				}
			} else if( column.getTypeName().toLowerCase().indexOf("bytea") > -1 ) {
				if( !ObjectUtil.isNullOrEmpty(object) && object instanceof File) {
					try {
						File file = (File)object;
						FileInputStream fis = new FileInputStream(file);
						ps.setBinaryStream(idxPs, fis, (int) file.length());
						retVal = object;
					}catch( Exception e ) {
						ps.setNull(idxPs,Types.BLOB);
					}
				} else {
					ps.setNull(idxPs,Types.BLOB);
				}
			} else {
				if( !ObjectUtil.isNullOrEmpty(object) ) {
					ps.setString(idxPs, StringUtil.toString(object));
					retVal = StringUtil.toString(object);
				} else {
					ps.setNull(idxPs,Types.VARCHAR);
				}
			}
		} else {
			ps.setString(idxPs, StringUtil.toString(object));
			retVal = StringUtil.toString(object);
		}
		return retVal;
	}

	private Object setType( PreparedStatement ps, int idxPs, Object object ) throws SQLException {
		Object retVal = null;
		if( object == null ) {
			ps.setNull(idxPs, Types.OTHER);
			return retVal;
		}
		if( object instanceof String ) {
			String val = StringUtil.toString(object);
			ps.setString(idxPs, val);
			retVal = val;
		} else if( object instanceof Integer ) {
			ps.setInt(idxPs, StringUtil.toInteger(object));
			retVal = StringUtil.toInteger(object);
		} else if( object instanceof Long ) {
			ps.setLong(idxPs, StringUtil.toLong(object));
			retVal = StringUtil.toLong(object);
		} else if( object instanceof Float ) {
			ps.setFloat(idxPs, StringUtil.toFloat(object));
			retVal = StringUtil.toFloat(object);
		} else if( object instanceof Double ) {
			ps.setDouble(idxPs, StringUtil.toDouble(object));
			retVal = StringUtil.toDouble(object);
		} else if( object instanceof BigDecimal ) {
			ps.setBigDecimal(idxPs, new BigDecimal(StringUtil.objToString(object)));
			retVal = new BigDecimal(StringUtil.objToString(object));
		} else if( object instanceof Date ) {
			ps.setTimestamp(idxPs, StringUtil.toTimestamp(object));
			retVal = StringUtil.toTimestamp(object);
		} else if( object instanceof Boolean ) {
			ps.setBoolean(idxPs, StringUtil.toBoolean(object));
			retVal = StringUtil.toBoolean(object);
		} else if( object instanceof File ) {
			try {
				File file = (File)object;
				FileInputStream fis = new FileInputStream(file);
				ps.setBinaryStream(idxPs, fis, file.length());
				retVal = (File)object;
			}catch( Exception e ) {
				ps.setNull(idxPs, Types.BINARY);
			}
		} else {
			ps.setObject(idxPs, object);
			retVal = object;
		}
		return retVal;
	}

	private static int getDecimalDigits( Column col ) {
		int ret = 0;
		if( col != null ) {
			ret = col.getDecimalDigits();
		}
		return ret;
	}

	private static Column getColumn( List<Column> list, String key ) {
		Column ret = null;
		for( int i = 0; list != null && i < list.size(); i++ ) {
			if( list.get(i).getColumnName().equals(key) ) {
				ret = list.get(i);
				break;
			}
		}
		return ret;
	}

	private static PK getPk( List<PK> list, String key ) {
		PK ret = null;
		for( int i = 0; list != null && i < list.size(); i++ ) {
			if( list.get(i).getColumnName().equals(key) ) {
				ret = list.get(i);
				break;
			}
		}
		return ret;
	}

	public static Connection getConnection(String url, String usr, String pwd) {
		Connection ret = null;
		try {
			ret = DriverManager.getConnection(url, usr, pwd);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
}
