package com.whs.whsapi.jdbc.meta;

import java.io.Serializable;

import lombok.Data;

@Data
public class Column implements Serializable {
	
	public static final long serialVersionUID = 1l;
	
	private String tableSchem;
	private int nullabble;
	private String isNullable;
	private String columnDef;
	private int dataType;
	private String remarks;
	private String tableName;
	private int sqlDataType;
	private int ordinalPosition;
	private String columnName;
	private int columnSize;
	private int charOctetLength;
	private String tableCat;
	private String typeName;
	private int numPrecRadix;
	private int sqlDatetimeSub;
	private int bufferLength;
	private int decimalDigits;
	private String isAutoIncrement;
	
}
