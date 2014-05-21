package com.surmize.snaporm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

public class DataSourceManager {

    private DataSource datasource;

    private DataSourceManager() {
    }

    public static DataSourceManager getInstance() {
        return DatasourceManagerHolder.INSTANCE;
    }

    private static class DatasourceManagerHolder {
        private static final DataSourceManager INSTANCE = new DataSourceManager();
    }
    
    public DataSource getDataSource(){
        if( datasource == null){
            initializeDatasource();
        }
        return datasource;
    }
    
    public Connection getConnection() throws SQLException{
        return getDataSource().getConnection(); 
    }
    
    public void closeConnection(Connection c){
        if(c != null){
            try{
                c.close();
            }catch(Exception ignore){}
        }
    }
    
    public void closeResultSet(ResultSet rs){
        if(rs != null){
            try{
                rs.close();
            }catch(Exception ignore){}
        }
    }
    
    public void closeStatement(Statement s){
        if(s != null){
            try{
                s.close();
            }catch(Exception ignore){}
        }
    }
    
    public void closeAll(ResultSet rs, Statement s, Connection c ){
        closeResultSet(rs);
        closeStatement(s);
        closeConnection(c);
    }
    
    private void initializeDatasource() {
        PoolProperties p = new PoolProperties();
        p.setUrl( PropertyManager.getSetting("cp.Url") );
        p.setDriverClassName( PropertyManager.getSetting("cp.DriverClassName") );
        p.setUsername( PropertyManager.getSetting("cp.Username") );
        p.setPassword( PropertyManager.getSetting("cp.Password") );
        p.setJmxEnabled(false);
        p.setTestWhileIdle(false);
        p.setTestOnBorrow(true);
        p.setValidationQuery( PropertyManager.getSetting("cp.ValidationQuery") );
        p.setTestOnReturn(false);
        p.setValidationInterval(30000);
        p.setTimeBetweenEvictionRunsMillis(30000);
        p.setMaxActive(  PropertyManager.getIntegerSetting("cp.MaxActive")  );
        p.setInitialSize( PropertyManager.getIntegerSetting("cp.InitialSize") );
        p.setMaxWait( PropertyManager.getIntegerSetting("cp.MaxWait") );
        p.setRemoveAbandonedTimeout(60);
        p.setMinEvictableIdleTimeMillis(30000);
        p.setMinIdle(10);
        p.setLogAbandoned(true);
        p.setRemoveAbandoned(true);
        datasource = new DataSource();
        datasource.setPoolProperties(p);
    }
}
