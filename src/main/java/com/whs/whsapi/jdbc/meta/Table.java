package com.whs.whsapi.jdbc.meta;

import java.io.Serializable;
import java.util.List;

public class Table implements Serializable {
	
	public static final long serialVersionUID = 1l;

	private String tableSchem;
	private String remarks;
	private String tableName;
	private String tableCat;
	private String tableType;

	public String getTableSchem() {
		return tableSchem;
	}
	public void setTableSchem(String tableSchem) {
		this.tableSchem = tableSchem;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
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
	public String getTableType() {
		return tableType;
	}
	public void setTableType(String tableType) {
		this.tableType = tableType;
	}
	public boolean isManyToMany() {
		return isManyToMany;
	}
	public void setManyToMany(boolean isManyToMany) {
		this.isManyToMany = isManyToMany;
	}
	public List<Column> getColumn() {
		return column;
	}
	public void setColumn(List<Column> column) {
		this.column = column;
	}
	public List<PK> getPk() {
		return pk;
	}
	public void setPk(List<PK> pk) {
		this.pk = pk;
	}
	public List<ForeignKey> getImportedKey() {
		return importedKey;
	}
	public void setImportedKey(List<ForeignKey> importedKey) {
		this.importedKey = importedKey;
	}
	public List<ForeignKey> getExportedKey() {
		return exportedKey;
	}
	public void setExportedKey(List<ForeignKey> exportedKey) {
		this.exportedKey = exportedKey;
	}
	private boolean isManyToMany;
	
	private List<Column> column;
	private List<PK> pk;
	private List<ForeignKey> importedKey;
	private List<ForeignKey> exportedKey;
	
}
