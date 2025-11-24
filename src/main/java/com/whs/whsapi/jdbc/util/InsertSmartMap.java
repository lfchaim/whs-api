package com.whs.whsapi.jdbc.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Repository layer class to insert a single record into a database table
 * using a LinkedHashMap for column-value pairs.
 * It attempts to handle database-specific primary key generation logic
 * (Sequences for Oracle/Postgres, Identity for MySQL/SQL Server).
 */
public class InsertSmartMap {

    // Common sequence/identity column types (simplified)
    private static final Set<String> AUTO_GENERATED_TYPES = Set.of(
        "SERIAL", "BIGSERIAL", "IDENTITY", "NUMBER", "INT" // Simplified list
    );

    /**
     * Inserts a record into the specified table using the provided connection and data.
     *
     * @param connection The active database connection.
     * @param tableName The name of the table to insert into.
     * @param content A LinkedHashMap where keys are column names and values are the data to insert.
     * @return The number of rows affected (usually 1) or the generated key if applicable.
     * @throws SQLException If a database access error occurs.
     */
    public long insertRecord(Connection connection, String tableName, LinkedHashMap<String, Object> content) 
            throws SQLException {
        
        // 1. Identify Database and Primary Key Strategy
        String dbName = connection.getMetaData().getDatabaseProductName().toUpperCase();
        
        // In a production system, you'd query metadata to find the *actual* PK column name.
        // For simplicity and common practice, we'll assume the PK is the first column 
        // in the map or we try to retrieve the generated key.
        
        // Get the list of columns to be inserted
        String columns = content.keySet().stream()
            .collect(Collectors.joining(", "));
        
        // Create the placeholders for the PreparedStatement
        String valuesPlaceholders = content.keySet().stream()
            .map(k -> "?")
            .collect(Collectors.joining(", "));

        // Base INSERT statement
        String insertSql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, valuesPlaceholders);

        long generatedKey = -1;
        boolean isSequenceBased = false;

        // 2. Adopt Best Strategy Based on Database Type
        
        // --- ORACLE/POSTGRES (Sequence/Serial) Strategy ---
        if (dbName.contains("ORACLE") || dbName.contains("POSTGRESQL")) {
            // Find PK (simulated) - we assume the first column in the map is the PK if it's typically sequence-managed.
            String pkColumn = content.keySet().iterator().next(); 
            isSequenceBased = isSequenceColumn(connection, tableName, pkColumn);
            
            if (isSequenceBased) {
                // If the PK is sequence/serial managed, we might need to query the sequence
                // and pass the value OR rely on RETURNING clause (Postgres) or an identity column (modern Oracle).
                
                // For a robust implementation, Oracle often requires fetching the sequence value:
                // SELECT MY_SEQ.NEXTVAL FROM DUAL;
                
                // For this smart map, we'll assume the primary key column (first column in map) 
                // is NOT in the 'content' map, and we'll use the DB's ability to generate it 
                // while expecting the generated key back.
                
                // If it's a sequence-managed PK, we need to try and get the generated key.
                // In a real scenario, if the PK is managed by an external sequence (Oracle/Postgres), 
                // you would need to modify the SQL to fetch the next value and include it in the INSERT, 
                // OR rely on the DB to fill it and use `getGeneratedKeys`.
                
                // We'll rely on `getGeneratedKeys` for simplicity and compatibility with RETURNING/IDENTITY.
                // Modern Oracle and Postgres often support this well.
                
                System.out.println("DEBUG: Sequence/Serial strategy applied for " + dbName);
            }
        } 
        // --- MYSQL/SQL SERVER (Auto-Increment/Identity) Strategy ---
        else if (dbName.contains("MYSQL") || dbName.contains("SQL SERVER")) {
            // These databases typically use AUTO_INCREMENT or IDENTITY, which are handled 
            // by setting the flag to retrieve generated keys.
            System.out.println("DEBUG: Auto-Increment/Identity strategy applied for " + dbName);
        }

        // 3. Execute the INSERT Statement
        try (PreparedStatement ps = connection.prepareStatement(
                insertSql, 
                Statement.RETURN_GENERATED_KEYS // Request generated keys back
             )) {

            // Set the parameters in the PreparedStatement
            int i = 1;
            for (Map.Entry<String, Object> entry : content.entrySet()) {
                // Handle different object types (simplified - production code needs full type handling)
                ps.setObject(i++, entry.getValue());
            }

            // Execute the update
            int rowsAffected = ps.executeUpdate();

            // 4. Retrieve Generated Keys (if applicable)
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    // Assuming the primary key is a long/integer type
                    generatedKey = rs.getLong(1); 
                }
            }

            // Return the generated key if found, otherwise the rows affected count.
            return generatedKey != -1 ? generatedKey : rowsAffected;
        }
    }

    /**
     * Helper method to determine if a column is likely managed by a sequence or serial.
     * This is a simplified check based on column metadata.
     *
     * @param connection The database connection.
     * @param tableName The table name.
     * @param columnName The column name (assumed PK).
     * @return true if the column is likely sequence/serial managed and should be skipped in INSERT data.
     */
    private boolean isSequenceColumn(Connection connection, String tableName, String columnName) {
        try {
            DatabaseMetaData dbmd = connection.getMetaData();
            
            // Try to find if the column is auto-generated based on the type name
            // Note: This is a *highly* simplified check. Production code would use 
            // database-specific catalog queries (e.g., pg_class, all_tab_columns, information_schema).
            
            try (ResultSet rs = dbmd.getColumns(null, null, tableName.toUpperCase(), columnName.toUpperCase())) {
                if (rs.next()) {
                    String typeName = rs.getString("TYPE_NAME").toUpperCase();
                    // Check if it's an auto-generated type like SERIAL, BIGSERIAL, IDENTITY, etc.
                    if (AUTO_GENERATED_TYPES.contains(typeName)) {
                         return true;
                    }
                }
            }
            
            // Oracle-specific check (simplified: check for sequence existence)
            if (dbmd.getDatabaseProductName().toUpperCase().contains("ORACLE")) {
                String sequenceName = tableName.toUpperCase() + "_SEQ";
                // A true check would involve querying ALL_SEQUENCES. For this layer, we'll assume the convention.
                System.out.println("DEBUG: Assuming Oracle PK is managed by sequence: " + sequenceName);
                return true; 
            }
            
        } catch (SQLException e) {
            System.err.println("Error accessing metadata: " + e.getMessage());
        }
        return false;
    }
}