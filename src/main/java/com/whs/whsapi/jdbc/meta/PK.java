package com.whs.whsapi.jdbc.meta;

import java.io.Serializable;

public class PK implements Serializable {
	
	public static final long serialVersionUID = 1l;
	
	private int keySeq;
	private String columnName;
	public int getKeySeq() {
		return keySeq;
	}
	public void setKeySeq(int keySeq) {
		this.keySeq = keySeq;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getPkName() {
		return pkName;
	}
	public void setPkName(String pkName) {
		this.pkName = pkName;
	}
	public String getTableSchem() {
		return tableSchem;
	}
	public void setTableSchem(String tableSchem) {
		this.tableSchem = tableSchem;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getTableCat() {
		return tableCat;
	}
	public void setTableCat(String tableCat) {
		this.tableCat = tableCat;
	}
	private String pkName;
	private String tableSchem;
	private String tableName;
	private String tableCat;
	
}
