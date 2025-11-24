package com.whs.whsapi.jdbc.meta;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.whs.whsapi.util.JSONUtil;

public class DBMetaUtil {
	
	public static final String INDEX = "INDEX";
	public static final String SEQUENCE = "SEQUENCE";
	public static final String TABLE = "TABLE";
	public static final String VIEW = "VIEW";
	public static final String MATERIALIZED_VIEW = "MATERIALIZED VIEW";
	public static final String SYSTEM_INDEX = "SYSTEM INDEX";
	public static final String SYSTEM_TABLE = "SYSTEM TABLE";
	public static final String SYSTEM_VIEW = "SYSTEM VIEW";
	public static final String SYSTEM_TOAST_INDEX = "SYSTEM TOAST INDEX";
	
	public static String getProductName( Connection conn ) {
		String ret = "";
		try {
			DatabaseMetaData meta = conn.getMetaData();
			ret = meta.getDatabaseProductName();
		}catch( Exception e ) {
			e.printStackTrace();
		}
		return ret;
	}

	public static int execute(Connection conn, Map<String, String[]> map, String table, String operation,
			List<String> pk, Map mapData) {
		StringBuffer sql = new StringBuffer();
		List<String> listPk = new ArrayList<String>();
		List<String> listNotPk = new ArrayList<String>();
		Iterator it = mapData.keySet().iterator();
		int ret = 0;
		while (it.hasNext()) {
			String key = (String) it.next();
			if (pk.contains(key))
				listPk.add(key);
			else
				listNotPk.add(key);
		}
		if (operation.equalsIgnoreCase("update") && (listNotPk == null || listNotPk.size() < 1))
			return -1;
		if (operation.equalsIgnoreCase("insert")) {
			sql.append("insert into ").append(table);
			sql.append("(");
			int count = 0;
			for (int i = 0; i < listNotPk.size(); i++) {
				if (count++ > 0)
					sql.append(",");
				sql.append(listNotPk.get(i));
			}
			for (int i = 0; i < listPk.size(); i++) {
				if (count++ > 0)
					sql.append(",");
				sql.append(listPk.get(i));
			}
			sql.append(") values (");
			count = 0;
			for (int i = 0; i < listNotPk.size(); i++) {
				if (count++ > 0)
					sql.append(",");
				sql.append("?");
			}
			for (int i = 0; i < listPk.size(); i++) {
				if (count++ > 0)
					sql.append(",");
				sql.append("?");
			}
			sql.append(")");
		} else {
			sql.append("update ").append(table).append(" set ");
			int count = 0;
			for (int i = 0; i < listNotPk.size(); i++) {
				if (count++ > 0)
					sql.append(",");
				sql.append(listNotPk.get(i)).append(" = ? ");
			}
			sql.append(" where 1=1 ");
			for (int i = 0; i < listPk.size(); i++) {
				sql.append(" and ").append(listPk.get(i)).append(" = ? ");
			}
		}
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql.toString());
			int idx = 1;
			for (int i = 0; i < listNotPk.size(); i++) {
				if ("".equals(mapData.get(listNotPk.get(i))))
					ps.setObject(idx++, null);
				else
					ps.setObject(idx++, mapData.get(listNotPk.get(i)));
			}
			for (int i = 0; i < listPk.size(); i++) {
				ps.setObject(idx++, mapData.get(listPk.get(i)));
			}
			ret = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (Exception e) {

			}
		}
		System.out.println("ret: " + ret + " OP: " + operation + " Value: " + JSONUtil.toJSONString(mapData));
		return ret;
	}

	public static List<String> getPK(Connection conn, Map<String, String[]> map, String table) {
		List<String> ret = null;
		try {
			DatabaseMetaData meta = conn.getMetaData();
			ResultSet rs = meta.getPrimaryKeys(null, null, table);
			while (rs.next()) {
				if (ret == null)
					ret = new ArrayList<String>();
				ret.add(rs.getString("COLUMN_NAME"));
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {

		}
		return ret;
	}

	public static Map exists(Connection conn, Map<String, String[]> map, String table, List<String> pk, Map mapData) {
		StringBuffer sql = new StringBuffer();
		sql.append("select * from " + table + " where 1=1 ");
		for (int i = 0; i < pk.size(); i++) {
			sql.append("and ").append(pk.get(i)).append(" = ? ");
		}
		PreparedStatement ps = null;
		Map mapRet = null;
		try {
			ps = conn.prepareStatement(sql.toString());
			int idx = 1;
			for (int i = 0; i < pk.size(); i++) {
				ps.setObject(idx++, mapData.get(pk.get(i)));
			}
			ResultSet rs = ps.executeQuery();
			ResultSetMetaData meta = rs.getMetaData();
			String[] cols = new String[meta.getColumnCount()];
			for (int i = 0; i < meta.getColumnCount(); i++) {
				cols[i] = meta.getColumnName(i + 1);
			}
			if (rs.next()) {
				mapRet = new HashMap();
				for (int i = 0; i < cols.length; i++) {
					if (rs.getString(cols[i]) == null)
						mapRet.put(cols[i], "");
					else
						mapRet.put(cols[i], rs.getObject(cols[i]));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ps.close();
			} catch (Exception e) {
			}
		}
		return mapRet;
	}

	public static Table loadTable(Connection conn, String schema, String tableName, String[] types) throws SQLException {
		String[] table = Arrays.asList(tableName).toArray(new String[0]);
		List<Table> list = loadTables(conn, schema, table, types);
		if( list != null )
			return list.get(0);
		else
			return null;
	}
	
	public static List<Table> loadTables(Connection conn, String schema, String[] tableName, String[] types)
			throws SQLException {
		List<Table> list = new ArrayList<Table>();
		DatabaseMetaData dbmd = conn.getMetaData();
		String table = "%";
		if (tableName != null && tableName.length > 0) {
			for (int i = 0; i < tableName.length; i++) {
				ResultSet rs = dbmd.getTables(conn.getCatalog(), schema, tableName[i], types);
				while (rs.next()) {
					if (rs.getString("TABLE_NAME").indexOf("$") > -1)
						continue;
					Table tableDto = readResultSet(conn, rs, schema);
					list.add(tableDto);
				}
				rs.close();
			}
		} else {
			ResultSet rs = dbmd.getTables(conn.getCatalog(), schema, table, types);
			while (rs.next()) {
				if (rs.getString("TABLE_NAME").indexOf("$") > -1)
					continue;
				Table tableDto = readResultSet(conn, rs, schema);
				list.add(tableDto);
			}
			rs.close();
		}
		for (int i = 0; i < list.size(); i++) {
			Table tableDto = list.get(i);
			tableDto.setColumn(listColumns(conn, schema, tableDto.getTableName()));
			tableDto.setExportedKey(listExportedKeys(conn, schema, tableDto.getTableName()));
			tableDto.setImportedKey(listImportedKeys(conn, schema, tableDto.getTableName()));
			tableDto.setPk(listPK(conn, schema, tableDto.getTableName()));
			
			tableDto.setManyToMany(isManyToMany(tableDto));
		}
		return list;
	}

	public String[] listSchemas(Connection conn) throws SQLException {
		DatabaseMetaData dbmd = conn.getMetaData();
		ResultSet rs = dbmd.getSchemas();
		List<String> list = new ArrayList<String>();
		while (rs.next()) {
			list.add(rs.getString("TABLE_SCHEM"));
		}
		rs.close();
		return (String[]) list.toArray(new String[list.size()]);
	}

	private static Table readResultSet(Connection conn, ResultSet rs, String schema) throws SQLException {
		Table tableDto = new Table();
		tableDto.setTableSchem(rs.getString("TABLE_SCHEM"));
		tableDto.setRemarks(rs.getString("REMARKS"));
		tableDto.setTableName(rs.getString("TABLE_NAME"));
		tableDto.setTableCat(rs.getString("TABLE_CAT"));
		tableDto.setTableType(rs.getString("TABLE_TYPE"));

		tableDto.setColumn(listColumns(conn, schema, tableDto.getTableName()));
		tableDto.setExportedKey(listExportedKeys(conn, schema, tableDto.getTableName()));
		tableDto.setImportedKey(listImportedKeys(conn, schema, tableDto.getTableName()));
		tableDto.setPk(listPK(conn, schema, tableDto.getTableName()));
		return tableDto;
	}

	public static List<PK> listPK(Connection conn, String schema, String table) throws SQLException {
		List<PK> list = new ArrayList<PK>();
		DatabaseMetaData dbmd = conn.getMetaData();
		ResultSet rs = dbmd.getPrimaryKeys(conn.getCatalog(), schema, table);
		while (rs.next()) {
			PK pkDto = new PK();
			pkDto.setKeySeq(Integer.parseInt(rs.getString("KEY_SEQ")));
			pkDto.setColumnName(rs.getString("COLUMN_NAME"));
			pkDto.setPkName(rs.getString("PK_NAME"));
			pkDto.setTableSchem(rs.getString("TABLE_SCHEM"));
			pkDto.setTableName(rs.getString("TABLE_NAME"));
			pkDto.setTableCat(rs.getString("TABLE_CAT"));
			list.add(pkDto);
		}
		rs.close();
		return list;
	}

	public static List<ForeignKey> listExportedKeys(Connection conn, String schema, String table) throws SQLException {
		List<ForeignKey> list = new ArrayList<ForeignKey>();
		DatabaseMetaData dbmd = conn.getMetaData();
		ResultSet rs = dbmd.getExportedKeys(conn.getCatalog(), schema, table);
		while (rs.next()) {
			ForeignKey foreignKeyDto = new ForeignKey();
			foreignKeyDto.setKeySeq(Integer.parseInt(rs.getString("KEY_SEQ")));
			foreignKeyDto.setDeleteRule(Integer.parseInt(rs.getString("DELETE_RULE")));
			foreignKeyDto.setFkTableSchem(rs.getString("FKTABLE_SCHEM"));
			foreignKeyDto.setPkTableName(rs.getString("PKTABLE_NAME"));
			foreignKeyDto.setUpdateRule(Integer.parseInt(rs.getString("UPDATE_RULE")));
			foreignKeyDto.setPkTableSchem(rs.getString("PKTABLE_SCHEM"));
			foreignKeyDto.setFkColumnName(rs.getString("FKCOLUMN_NAME"));
			foreignKeyDto.setPkColumnName(rs.getString("PKCOLUMN_NAME"));
			foreignKeyDto.setPkName(rs.getString("PK_NAME"));
			foreignKeyDto.setDeferrability(Integer.parseInt(rs.getString("DEFERRABILITY")));
			foreignKeyDto.setFkName(rs.getString("FK_NAME"));
			foreignKeyDto.setPkTableCat(rs.getString("PKTABLE_CAT"));
			foreignKeyDto.setFkTableCat(rs.getString("FKTABLE_CAT"));
			foreignKeyDto.setFkTableName(rs.getString("FKTABLE_NAME"));
			list.add(foreignKeyDto);
		}
		rs.close();
		return list;
	}

	public static List<ForeignKey> listImportedKeys(Connection conn, String schema, String table) throws SQLException {
		List<ForeignKey> list = new ArrayList<ForeignKey>();
		DatabaseMetaData dbmd = conn.getMetaData();
		ResultSet rs = dbmd.getImportedKeys(conn.getCatalog(), schema, table);
		while (rs.next()) {
			ForeignKey foreignKeyDto = new ForeignKey();
			foreignKeyDto.setKeySeq(Integer.parseInt(rs.getString("KEY_SEQ")));
			foreignKeyDto.setDeleteRule(Integer.parseInt(rs.getString("DELETE_RULE")));
			foreignKeyDto.setFkTableSchem(rs.getString("FKTABLE_SCHEM"));
			foreignKeyDto.setPkTableName(rs.getString("PKTABLE_NAME"));
			foreignKeyDto.setUpdateRule(Integer.parseInt(rs.getString("UPDATE_RULE")));
			foreignKeyDto.setPkTableSchem(rs.getString("PKTABLE_SCHEM"));
			foreignKeyDto.setFkColumnName(rs.getString("FKCOLUMN_NAME"));
			foreignKeyDto.setPkColumnName(rs.getString("PKCOLUMN_NAME"));
			foreignKeyDto.setPkName(rs.getString("PK_NAME"));
			foreignKeyDto.setDeferrability(Integer.parseInt(rs.getString("DEFERRABILITY")));
			foreignKeyDto.setFkName(rs.getString("FK_NAME"));
			foreignKeyDto.setPkTableCat(rs.getString("PKTABLE_CAT"));
			foreignKeyDto.setFkTableCat(rs.getString("FKTABLE_CAT"));
			foreignKeyDto.setFkTableName(rs.getString("FKTABLE_NAME"));
			list.add(foreignKeyDto);
		}
		rs.close();
		return list;
	}
	
	public static Column getColumn( List<Column> list, String columnName ) {
		Column ret = null;
		if( list != null ) {
			for( Column c : list ) {
				if( c.getColumnName().equals(columnName) ) {
					ret = c;
					break;
				}
			}
		}
		return ret;
	}

	public static List<Column> listColumns(Connection conn, String schema, String table) throws SQLException {
		List<Column> list = new ArrayList<Column>();
		DatabaseMetaData dbmd = conn.getMetaData();
		ResultSet rs = dbmd.getColumns(conn.getCatalog(), schema, table, null);
		ResultSetMetaData meta = rs.getMetaData();
		int cols = meta.getColumnCount();
		List<String> permCols = new ArrayList<String>();
		for (int i = 0; i < cols; i++) {
			String name = meta.getColumnName(i + 1);
			permCols.add(name);
		}
		while (rs.next()) {
			Column columnDto = new Column();
			if( rs.getString("BUFFER_LENGTH") != null )
				columnDto.setBufferLength(Integer.parseInt(rs.getString("BUFFER_LENGTH")));
			if (rs.getString("CHAR_OCTET_LENGTH") != null)
				columnDto.setCharOctetLength(Integer.parseInt(rs.getString("CHAR_OCTET_LENGTH")));
			// columnDto.setColumnDef(rs.getString("COLUMN_DEF"));
			columnDto.setColumnName(rs.getString("COLUMN_NAME"));
			columnDto.setColumnSize(Integer.parseInt((rs.getString("COLUMN_SIZE"))));
			columnDto.setDataType(Integer.parseInt(rs.getString("DATA_TYPE")));
			if (rs.getString("DECIMAL_DIGITS") != null)
				columnDto.setDecimalDigits(Integer.parseInt(rs.getString("DECIMAL_DIGITS")));
			columnDto.setIsNullable(rs.getString("IS_NULLABLE"));
			columnDto.setNullabble(Integer.parseInt(rs.getString("NULLABLE")));
			if (rs.getString("NUM_PREC_RADIX") != null)
				columnDto.setNumPrecRadix(Integer.parseInt(rs.getString("NUM_PREC_RADIX")));
			columnDto.setOrdinalPosition(Integer.parseInt(rs.getString("ORDINAL_POSITION")));
			columnDto.setRemarks(rs.getString("REMARKS"));
			if( rs.getString("SQL_DATA_TYPE") != null )
				columnDto.setSqlDataType(Integer.parseInt(rs.getString("SQL_DATA_TYPE")));
			if (rs.getString("SQL_DATETIME_SUB") != null)
				columnDto.setSqlDatetimeSub(Integer.parseInt(rs.getString("SQL_DATETIME_SUB")));
			columnDto.setTableCat(rs.getString("TABLE_CAT"));
			columnDto.setTableName(rs.getString("TABLE_NAME"));
			columnDto.setTableSchem(rs.getString("TABLE_SCHEM"));
			columnDto.setTypeName(rs.getString("TYPE_NAME"));
			if (permCols.contains("IS_AUTOINCREMENT"))
				columnDto.setIsAutoIncrement(rs.getString("IS_AUTOINCREMENT"));
			list.add(columnDto);
		}
		rs.close();
		return list;
	}
	
	public static boolean isManyToMany( Table table ) {
		List<Boolean> listRet = new ArrayList<>();
		if( table == null )
			return false;
		for( int i = 0; table.getImportedKey() != null && i < table.getImportedKey().size(); i++ ) {
			ForeignKey fk = table.getImportedKey().get(i);
			boolean found = false;
			for( int j = 0; table.getPk() != null && j < table.getPk().size(); j++ ) {
				PK pk = table.getPk().get(j);
				if( table.getImportedKey() != null ) {
					if( fk.getFkColumnName().equals(pk.getColumnName()) ) {
						found = true;
						break;
					}
				}
			}
			listRet.add(found);
		}
		return !listRet.contains(false);
	}

	public static Connection getConnection(String url, String usr, String pwd) {
		Connection ret = null;
		try {
			//Class.forName(driver);
			ret = DriverManager.getConnection(url, usr, pwd);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
}
