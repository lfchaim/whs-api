package com.whs.whsapi.jdbc.meta;

import java.io.Serializable;

public class ForeignKey implements Serializable {

	public static final long serialVersionUID = 1l;
	
	private int    keySeq;
	private int    deleteRule;
	private String fkTableSchem;
	private String pkTableName;
	private int    updateRule;
	private String pkTableSchem;
	private String fkColumnName;
	public int getKeySeq() {
		return keySeq;
	}
	public void setKeySeq(int keySeq) {
		this.keySeq = keySeq;
	}
	public int getDeleteRule() {
		return deleteRule;
	}
	public void setDeleteRule(int deleteRule) {
		this.deleteRule = deleteRule;
	}
	public String getFkTableSchem() {
		return fkTableSchem;
	}
	public void setFkTableSchem(String fkTableSchem) {
		this.fkTableSchem = fkTableSchem;
	}
	public String getPkTableName() {
		return pkTableName;
	}
	public void setPkTableName(String pkTableName) {
		this.pkTableName = pkTableName;
	}
	public int getUpdateRule() {
		return updateRule;
	}
	public void setUpdateRule(int updateRule) {
		this.updateRule = updateRule;
	}
	public String getPkTableSchem() {
		return pkTableSchem;
	}
	public void setPkTableSchem(String pkTableSchem) {
		this.pkTableSchem = pkTableSchem;
	}
	public String getFkColumnName() {
		return fkColumnName;
	}
	public void setFkColumnName(String fkColumnName) {
		this.fkColumnName = fkColumnName;
	}
	public String getPkColumnName() {
		return pkColumnName;
	}
	public void setPkColumnName(String pkColumnName) {
		this.pkColumnName = pkColumnName;
	}
	public String getPkName() {
		return pkName;
	}
	public void setPkName(String pkName) {
		this.pkName = pkName;
	}
	public int getDeferrability() {
		return deferrability;
	}
	public void setDeferrability(int deferrability) {
		this.deferrability = deferrability;
	}
	public String getFkName() {
		return fkName;
	}
	public void setFkName(String fkName) {
		this.fkName = fkName;
	}
	public String getPkTableCat() {
		return pkTableCat;
	}
	public void setPkTableCat(String pkTableCat) {
		this.pkTableCat = pkTableCat;
	}
	public String getFkTableCat() {
		return fkTableCat;
	}
	public void setFkTableCat(String fkTableCat) {
		this.fkTableCat = fkTableCat;
	}
	public String getFkTableName() {
		return fkTableName;
	}
	public void setFkTableName(String fkTableName) {
		this.fkTableName = fkTableName;
	}
	private String pkColumnName;
	private String pkName;
	private int    deferrability;
	private String fkName;
	private String pkTableCat;
	private String fkTableCat;
	private String fkTableName;
		
}
