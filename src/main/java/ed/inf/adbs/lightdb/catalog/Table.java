package ed.inf.adbs.lightdb.catalog;

public class Table {
    private String tableName;
    private String[] columns;

    public Table(String tableName, String... columns) {
        this.tableName = tableName;
        this.columns = columns;
    }

    public String getTableName() {
        return tableName;
    }

    public String[] getColumns() {
        return columns;
    }
}