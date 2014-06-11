SnapORM
=======

SnapORM is a very simple JDBC based ORM that provides resultset mapping and basic crud operations.

To add SnapORM as a dependency to your project:
```
<dependency>
    <groupId>com.surmize</groupId>
    <artifactId>SnapORM</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
When you create a new data access object it should extend BaseDAO<entity> where entity is the object you want to bind to map in the resultset.  
You must also override an instantiateEntity method which creates a new entity.

For example:
```java
public class StockSymbolDAO extends BaseDAO<StockSymbol>{

 @Override
    public StockSymbol instantiateEntity() {
        return new StockSymbol();
    }

}
```

In you entity you can annotate the class with the @TableName annotation.  This will allow BaseDAO to generate the SQL for basic CRUD operations.

For the resultset mapping to work you must annotation the fields with the @ColumnName annotation.

For example:
```
import com.surmize.snaporm.ColumnName;
import com.surmize.snaporm.PK;
import com.surmize.snaporm.TableName;

@TableName("stock_symbols")
public class StockSymbol {

    @PK
    @ColumnName("id")
    public Long id;
    
    @ColumnName("symbol")
    public String symbol;
    
    @ColumnName("company_name")
    public String companyName;
    
    @ColumnName("sector")
    public String sector;
            
}
```

The BaseDAO provides the following methods:
```
//executes query and maps restuls to a list of the entity defined in the parent DAO
public List<T> executeSelect(String query)

//executes query and maps restuls to a list of the entity defined in the parent DAO
//replaces ? in query with params
public List<T> executeSelect(String query, List params)

// similar to exeuteQuery, but performs an update and returns 0 or 1 for success or failure
public int executeUpdate(String update, List params)

// returns a single entity based on id or primary key
public T findEntityById(int id)

// inserts entity and returns primary key (currently only works on MySql or MariaDB)
public int insertEntityReturnId(T entity)

// inserts entity and returns 0 or 1 for success or failure.
public int insertEntity(T entity)

//deletes entity
public int deleteByPrimaryKey(T entity)
