package com.whs.whsapi.jdbc.meta;

import java.io.Serializable;

import lombok.Data;

@Data
public class PK implements Serializable {
	
	public static final long serialVersionUID = 1l;
	
	private int keySeq;
	private String columnName;
	private String pkName;
	private String tableSchem;
	private String tableName;
	private String tableCat;
	
}
