package com.surmize.snaporm;

import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseDAO<T> {

    protected final DataSourceManager dsMan;
    protected final ResultSetMapper mapper;
    protected final PreparedStatementGenerator statementGenerator;

    public BaseDAO() {
        dsMan = DataSourceManager.getInstance();
        mapper = new ResultSetMapper();
        statementGenerator = new PreparedStatementGenerator();
    }

    public List<T> executeSelect(String query) throws SQLException {
        return executeSelect(query, null);
    }

    public List<T> executeSelect(String query, List params) throws SQLException {
        List<T> results = new ArrayList<>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = dsMan.getConnection();
            stmt = statementGenerator.getStatement(con, query, params);
            rs = stmt.executeQuery();
            while (rs.next()) {
                T entity = instantiateEntity();
                mapper.mapResults(rs, entity);
                results.add(entity);
            }
        } finally {
            dsMan.closeAll(rs, stmt, con);
        }
        return results;
    }

    public int executeUpdate(String update, List params) throws SQLException {
        Connection con = null;
        PreparedStatement stmt = null;
        int result;
        try {
            con = dsMan.getConnection();
            stmt = statementGenerator.getStatement(con, update, params);
            result = stmt.executeUpdate();
        } finally {
            dsMan.closeStatement(stmt);
            dsMan.closeConnection(con);
        }
        return result;
    }

    public T findEntityById(Object id) throws SQLException {
        T result = null;
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = dsMan.getConnection();
            stmt = statementGenerator.getFindByIdStatement(con, instantiateEntity(), id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                result = instantiateEntity();
                mapper.mapResults(rs, result);
            }
        } finally {
            dsMan.closeAll(rs, stmt, con);
        }
        return result;
    }

    public int insertOrUpdateEntity(T entity) throws SQLException {
        if( exists(entity) ){
            return updateEntity(entity);
        } else {
            try{
                return insertEntity(entity);
            }
            catch(MySQLIntegrityConstraintViolationException | SQLIntegrityConstraintViolationException ex){
                Logger.getLogger(BaseDAO.class.getName()).log(Level.INFO, null, ex);
                return updateEntity(entity);
            }
            
        }
    }

    public int insertEntity(T entity) throws SQLException {
        Connection con = null;
        PreparedStatement stmt = null;
        int result;
        try {
            con = dsMan.getConnection();
            stmt = statementGenerator.getInsertStatement(con, entity);
            result = stmt.executeUpdate();
        } finally {
            dsMan.closeStatement(stmt);
            dsMan.closeConnection(con);
        }
        return result;
    }

    public int insertEntityReturnId(T entity) throws SQLException {
        Connection con = null;
        PreparedStatement stmt = null;
        PreparedStatement getIdStmt = null;
        ResultSet rs = null;
        int pkId = 0;
        try {
            con = dsMan.getConnection();
            stmt = statementGenerator.getInsertStatement(con, entity);
            stmt.executeUpdate();
            getIdStmt = con.prepareStatement("select LAST_INSERT_ID()");
            rs = getIdStmt.executeQuery();
            if (rs.next()) {
                pkId = rs.getInt(1);
            }
        } finally {
            dsMan.closeResultSet(rs);
            dsMan.closeStatement(stmt);
            dsMan.closeStatement(getIdStmt);
            dsMan.closeConnection(con);
        }
        return pkId;
    }

    public int updateEntity(T entity) throws SQLException {
        Connection con = null;
        PreparedStatement stmt = null;
        int result;
        try {
            con = dsMan.getConnection();
            stmt = statementGenerator.getUpdateStatement(con, entity);
            result = stmt.executeUpdate();
        } finally {
            dsMan.closeStatement(stmt);
            dsMan.closeConnection(con);
        }
        return result;
    }

    public int deleteByPrimaryKey(T entity) throws SQLException {
        Connection con = null;
        PreparedStatement stmt = null;
        int result;
        try {
            con = dsMan.getConnection();
            stmt = statementGenerator.getDeleteStatement(con, entity);
            result = stmt.executeUpdate();
        } finally {
            dsMan.closeStatement(stmt);
            dsMan.closeConnection(con);
        }
        return result;
    }

    public boolean exists(T entity) throws SQLException {
        AbstractMap.SimpleEntry keyValueMap = statementGenerator.getPrimaryKeyNameAndValue(entity);
        if (keyValueMap == null) {
            return false;
        } else {
            T result = null;
            Connection con = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                con = dsMan.getConnection();
                stmt = statementGenerator.getExistsStatement(con, entity);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    if(rs.getInt("TOTAL") > 0){
                        return true;
                    }
                }
            } finally {
                dsMan.closeAll(rs, stmt, con);
            }
            return false;
        }
    }

    public abstract T instantiateEntity();
}
