package com.surmize.snaporm;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResultSetMapper {

    public void mapResults(ResultSet rs, Object entity) throws SQLException {
        final Field[] fields = entity.getClass().getDeclaredFields();
        for (final Field field : fields) {
            final ColumnName columnName = field.getAnnotation(ColumnName.class);
            if (columnName != null) {
                String column = columnName.value(); 
                if (hasColumn(rs, column)) {
                    mapField(field, rs, column, entity);
                }
            }
        }
    }

    private void mapField(final Field field, ResultSet rs, String column, Object entity) throws SQLException {
        try {
            Class<?> clazz = field.getType();
            if (clazz.equals(Integer.TYPE) || clazz.equals(Integer.class)) {
                int value = rs.getInt(column);
                field.set(entity, value);
            } else if (clazz.equals(Long.TYPE) || clazz.equals(Long.class)) {
                long value = rs.getLong(column);
                field.set(entity, value);
            } else if (clazz.equals(String.class)) {
                String value = rs.getString(column);
                field.set(entity, value);
            } else if (clazz.equals(Boolean.TYPE) || clazz.equals(Boolean.class)) {
                boolean value = rs.getBoolean(column);
                field.set(entity, value);
            } else if (clazz.equals(Float.TYPE) || clazz.equals(Float.class)) {
                float value = rs.getFloat(column);
                field.set(entity, value);
            } else if (clazz.equals(Double.TYPE) || clazz.equals(Double.class)) {
                double value = rs.getDouble(column);
                field.set(entity, value);
            } else if (clazz.equals(Date.class)) {
                Date value = rs.getDate(column);
                field.set(entity, value);
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(ResultSetMapper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        for (int x = 1; x <= columns; x++) {
            if (columnName.equalsIgnoreCase(rsmd.getColumnName(x))) {
                return true;
            }
        }
        return false;
    }

}
