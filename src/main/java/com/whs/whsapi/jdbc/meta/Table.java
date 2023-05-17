package com.whs.whsapi.jdbc.meta;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class Table implements Serializable {
	
	public static final long serialVersionUID = 1l;

	private String tableSchem;
	private String remarks;
	private String tableName;
	private String tableCat;
	private String tableType;

	private boolean isManyToMany;
	
	private List<Column> column;
	private List<PK> pk;
	private List<ForeignKey> importedKey;
	private List<ForeignKey> exportedKey;
	
}
