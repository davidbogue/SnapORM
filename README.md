SnapORM
=======

SnapORM is a very simple JDBC based ORM that provides resultset mapping and basic crud operations.

To use add SnapORM as a dependency to your project:
<dependency>
    <groupId>com.surmize</groupId>
    <artifactId>SnapORM</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>

When you create a new data access object it should extend BaseDAO<entity> where entity is the object you want to bind to map in the resultset.  
You must also override an instantiateEntity method which creates a new entity.

For example:
public class StockSymbolDAO extends BaseDAO<StockSymbol>{

 @Override
    public StockSymbol instantiateEntity() {
        return new StockSymbol();
    }

}


In you entity you can annotate the class with the @TableName annotation.  This will allow BaseDAO to generate the SQL for basic CRUD operations.

For the resultset mapping to work you must annotation the fields with the @ColumnName annotation.

For example:
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
