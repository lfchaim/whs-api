package com.whs.whsapi.jdbc.meta;

import java.io.Serializable;

import lombok.Data;

@Data
public class ForeignKey implements Serializable {

	public static final long serialVersionUID = 1l;
	
	private int    keySeq;
	private int    deleteRule;
	private String fkTableSchem;
	private String pkTableName;
	private int    updateRule;
	private String pkTableSchem;
	private String fkColumnName;
	private String pkColumnName;
	private String pkName;
	private int    deferrability;
	private String fkName;
	private String pkTableCat;
	private String fkTableCat;
	private String fkTableName;
		
}
