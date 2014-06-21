package com.surmize.snaporm;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PreparedStatementGenerator {

    public PreparedStatement getStatement(Connection con, String query, List params) throws SQLException {
        PreparedStatement stmt = con.prepareStatement(query);
        if (params != null) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
        }
        return stmt;
    }

    public PreparedStatement getInsertStatement(Connection con, Object entity) throws SQLException {
        List params = new ArrayList();
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO ").append(getTableName(entity)).append(" (");
        StringBuilder paramList = new StringBuilder(" VALUES (");
        final Field[] fields = entity.getClass().getDeclaredFields();
        for (final Field field : fields) {
            final ColumnName columnName = field.getAnnotation(ColumnName.class);
            Object fieldValue = getFieldValue(field, entity);
            if (columnName != null && fieldValue != null) {
                String column = columnName.value();
                sqlBuilder.append(column).append(",");
                paramList.append("?,");
                params.add(fieldValue);
            }
        }
        sqlBuilder.deleteCharAt(sqlBuilder.lastIndexOf(",")).append(") ");
        paramList.deleteCharAt(paramList.lastIndexOf(",")).append(") ");
        sqlBuilder.append(paramList);
        return getStatement(con, sqlBuilder.toString(), params);
    }

    public PreparedStatement getUpdateStatement(Connection con, Object entity) throws SQLException {
        AbstractMap.SimpleEntry primaryKeyAndValueMap = getPrimaryKeyNameAndValue(entity);
        if (primaryKeyAndValueMap == null) {
            throw new SQLException("Primary Key cannot be empty");
        }
        List params = new ArrayList();
        StringBuilder sqlBuilder = new StringBuilder("UPDATE ").append(getTableName(entity)).append(" SET ");
        final Field[] fields = entity.getClass().getDeclaredFields();
        for (final Field field : fields) {
            final ColumnName columnName = field.getAnnotation(ColumnName.class);
            Object fieldValue = getFieldValue(field, entity);
            if (columnName != null && fieldValue != null) {
                String column = columnName.value();
                if (columnName != primaryKeyAndValueMap.getKey()) {
                    sqlBuilder.append(column).append("=?,");
                    params.add(fieldValue);
                }
            }
        }
        sqlBuilder.deleteCharAt(sqlBuilder.lastIndexOf(",")).append(" ");
        sqlBuilder.append("WHERE ").append(primaryKeyAndValueMap.getKey()).append("=?");
        params.add(primaryKeyAndValueMap.getValue());
        return getStatement(con, sqlBuilder.toString(), params);
    }

    public PreparedStatement getDeleteStatement(Connection con, Object entity) throws SQLException {
        AbstractMap.SimpleEntry keyValue = getPrimaryKeyNameAndValue(entity);
        if (keyValue == null) {
            throw new SQLException("Primary Key cannot be empty");
        }
        String deleteSQL = "DELETE FROM %s WHERE %s=?";
        String sql = String.format(deleteSQL, getTableName(entity), keyValue.getKey());
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setObject(1, keyValue.getValue());
        return stmt;
    }

    public PreparedStatement getFindByIdStatement(Connection con, Object entity, Object pk) throws SQLException {
        String pkColumn = getPrimaryKeyColumnName(entity);
        if (pkColumn == null) {
            throw new SQLException("Primary Key not defined on entity");
        }
        String findSQL = String.format("SELECT * FROM %s WHERE %s = ?", getTableName(entity), pkColumn);
        PreparedStatement stmt = con.prepareStatement(findSQL);
        stmt.setObject(1, pk);
        return stmt;
    }

    public PreparedStatement getExistsStatement(Connection con, Object entity) throws SQLException {
        AbstractMap.SimpleEntry keyValue = getPrimaryKeyNameAndValue(entity);
        if (keyValue == null) {
            throw new SQLException("Primary Key cannot be empty");
        }
        String existsSQL = "SELECT COUNT(1) AS TOTAL FROM %s WHERE %s=?";
        String sql = String.format(existsSQL, getTableName(entity), keyValue.getKey());
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setObject(1, keyValue.getValue());
        return stmt;
    }
    
    private String getTableName(Object entity) {
        String tableName = "";
        TableName tableNameAnnotation = entity.getClass().getAnnotation(TableName.class);
        if (tableNameAnnotation != null) {
            tableName = tableNameAnnotation.value();
        } else {
            tableName = entity.getClass().getName();
        }
        return tableName;
    }

    private String getPrimaryKeyColumnName(Object entity) {
        String pkField = getPrimaryKeyFieldName(entity);
        if (pkField != null) {
            try {
                Field idField = entity.getClass().getField(pkField);
                ColumnName columnName = idField.getAnnotation(ColumnName.class);
                if (columnName != null) {
                    return columnName.value();
                }
            } catch (NoSuchFieldException | SecurityException ex) {
                //ignore;
            }
        }
        return null;
    }

    public AbstractMap.SimpleEntry getPrimaryKeyNameAndValue(Object entity) {
        String pkField = getPrimaryKeyFieldName(entity);
        if (pkField != null) {
            try {
                Field idField = entity.getClass().getField(pkField);
                Object fieldValue = getFieldValue(idField, entity);
                ColumnName columnName = idField.getAnnotation(ColumnName.class);
                if (columnName != null && fieldValue != null) {
                    return new AbstractMap.SimpleEntry(columnName.value(), fieldValue);
                }
            } catch (NoSuchFieldException | SecurityException ex) {
                //ignore;
            }
        }
        return null;
    }

    private String getPrimaryKeyFieldName(Object entity) {
        final Field[] fields = entity.getClass().getDeclaredFields();
        // first look for a @PK annotation
        for (final Field field : fields) {
            PK pk = field.getAnnotation(PK.class);
            if (pk != null) {
                return field.getName();
            }
        }
        try {
            if (entity.getClass().getField("id") != null) {
                return "id";
            }
        } catch (NoSuchFieldException | SecurityException ex) {
            Logger.getLogger(PreparedStatementGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private Object getFieldValue(final Field field, Object entity) {
        try {
            Class<?> clazz = field.getType();

            if (clazz.equals(Integer.TYPE)) {
                return field.getInt(entity);
            }
            if (clazz.equals(Long.TYPE)) {
                return field.getLong(entity);
            }
            if (clazz.equals(String.class)) {
                return field.get(entity);
            }
            if (clazz.equals(Boolean.TYPE)) {
                return field.getBoolean(entity);
            }
            if (clazz.equals(Float.TYPE)) {
                return field.getFloat(entity);
            }
            if (clazz.equals(Double.TYPE)) {
                return field.getDouble(entity);
            }
            if (clazz.equals(Date.class)) {
                return field.get(entity);
            }
            return field.get(entity);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(ResultSetMapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
