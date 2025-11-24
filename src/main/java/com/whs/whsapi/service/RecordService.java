package com.whs.whsapi.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.util.LinkedMultiValueMap;

import com.whs.whsapi.jdbc.util.DBUtil;
import com.whs.whsapi.util.EnvUtil;
import com.whs.whsapi.util.StringUtil;

public class RecordService {

	public boolean exists( String tableName ) {
		boolean ret = true;
		
		return ret;
	}
	
	public List<Map<String,Object>> list( String tableName, LinkedMultiValueMap<String, String> params ){
		List<Map<String,Object>> ret = null;
		Connection conn = null;
		DBUtil du = new DBUtil();
		
		int limit = 0;
		if( params.containsKey("limit") ) {
			limit = StringUtil.toInteger(params.getFirst("limit"));
			params.remove("limit");
		}
		
		try {
			conn = DBUtil.getConnection(EnvUtil.getEnvValue("DB_URL"), EnvUtil.getEnvValue("DB_USR"), EnvUtil.getEnvValue("DB_PWD"));
			
			ret = du.listByTable(conn, tableName, params, limit);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try{ conn.close(); } catch( SQLException e ) { }
		}
		return ret;
	}
	
	public Object create(String tableName, Map<String,Object> map, LinkedMultiValueMap<String, String> params) {
		Object ret = null;
		Connection conn = null;
		DBUtil du = new DBUtil();
		try {
			conn = DBUtil.getConnection(EnvUtil.getEnvValue("DB_URL"), EnvUtil.getEnvValue("DB_USR"), EnvUtil.getEnvValue("DB_PWD"));
			int retIns = du.insert(conn, tableName, map);
			if( retIns > 0 )
				ret = Integer.parseInt(""+retIns);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try{ conn.close(); } catch( SQLException e ) { }
		}
		return ret;
	}

	public Object update(String tableName, Map<String,Object> map, LinkedMultiValueMap<String, String> params) {
		Object ret = null;
		Connection conn = null;
		DBUtil du = new DBUtil();
		try {
			conn = DBUtil.getConnection(EnvUtil.getEnvValue("DB_URL"), EnvUtil.getEnvValue("DB_USR"), EnvUtil.getEnvValue("DB_PWD"));
			int retIns = du.update(conn, tableName, map);
			if( retIns > 0 )
				ret = Integer.parseInt(""+retIns);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try{ conn.close(); } catch( SQLException e ) { }
		}
		return ret;
	}

}
