package com.whs.whsapi.jdbc.util;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.whs.whsapi.jdbc.meta.Column;
import com.whs.whsapi.jdbc.meta.DBMetaUtil;
import com.whs.whsapi.util.JSONUtil;
import com.whs.whsapi.util.MapUtil;
import com.whs.whsapi.util.ObjectUtil;
import com.whs.whsapi.util.StringUtil;

public class DBUtil {

	public int insert(Connection conn, String tableName, Map<String,Object> mapSet ){
		int ret = 0;
		if( StringUtil.isNull(tableName) || (mapSet == null || mapSet.isEmpty()) )
			return ret;
		PreparedStatement ps = null;
		StringBuilder sb = new StringBuilder();
		try {
			List<Column> listCol = DBMetaUtil.listColumns(conn, null, tableName);
			sb.append("insert into ").append(tableName).append(" (");
			List<String> listKey = MapUtil.listKeyString(mapSet);
			// Se no Map houver uma chave que nao pertence a tabela, remover
			for( int i = 0; i < listKey.size(); i++ ) {
				if( getColumn(listCol, listKey.get(i)) == null ) {
					listKey.remove(i--);
				}
			}
			for( int i = 0; i < listKey.size(); i++ ) {
				if( i > 0 )
					sb.append(", ");
				sb.append(listKey.get(i));
			}
			sb.append(") values (");
			for( int i = 0; i < listKey.size(); i++ ) {
				if( i > 0 )
					sb.append(", ");
				Column col = getColumn(listCol, listKey.get(i));
				if( "jsonb".equals(col.getTypeName()) )
					sb.append("?::jsonb");
				else if( "geometry".equals(col.getTypeName()) )
					sb.append("ST_GeomFromText(?, 4326)"); //25832)");
				else if( "_varchar".equals(col.getTypeName()) )
					sb.append("?::_varchar");
				else
					sb.append("?");
			}
			sb.append(")");
			ps = conn.prepareStatement(sb.toString());
			for( int i = 0; i < listKey.size(); i++ ) {
				setType(ps, (i+1), mapSet.get(listKey.get(i)), getColumn(listCol, listKey.get(i)));
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

	
}
