package com.surmize.snaporm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseDAO<T> {

    protected final DataSourceManager dsMan;
    protected final ResultSetMapper mapper;
    protected final PreparedStatementGenerator staementGenerator;

    public BaseDAO() {
        dsMan = DataSourceManager.getInstance();
        mapper = new ResultSetMapper();
        staementGenerator = new PreparedStatementGenerator();
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
            stmt = staementGenerator.getStatement(con, query, params);
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
            stmt = staementGenerator.getStatement(con, update, params);
            result = stmt.executeUpdate();
        } finally {
            dsMan.closeStatement(stmt);
            dsMan.closeConnection(con);
        }
        return result;
    }

    public T findEntityById(int id) throws SQLException {
        T result = null;
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = dsMan.getConnection();
            stmt = staementGenerator.getFindByIdStatement(con, instantiateEntity(), id);
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

    public int insertEntity(T entity) throws SQLException {
        Connection con = null;
        PreparedStatement stmt = null;
        int result;
        try {
            con = dsMan.getConnection();
            stmt = staementGenerator.getInsertStatement(con, entity);
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
            stmt = staementGenerator.getInsertStatement(con, entity);
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

    public int deleteByPrimaryKey(T entity) throws SQLException {
        Connection con = null;
        PreparedStatement stmt = null;
        int result;
        try {
            con = dsMan.getConnection();
            stmt = staementGenerator.getDeleteStatement(con, entity);
            result = stmt.executeUpdate();
        } finally {
            dsMan.closeStatement(stmt);
            dsMan.closeConnection(con);
        }
        return result;
    }

    public abstract T instantiateEntity();
}
