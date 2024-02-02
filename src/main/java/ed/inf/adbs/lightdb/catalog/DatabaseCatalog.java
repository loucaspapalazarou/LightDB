package ed.inf.adbs.lightdb.catalog;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

// import ed.inf.adbs.lightdb.types.Table;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;

public class DatabaseCatalog {

    private String dataDir;
    private Map<Table, List<Column>> tables;

    public DatabaseCatalog(String databaseDir) {
        this.dataDir = databaseDir + "/data/";
        this.tables = new HashMap<>();
        loadSchemaFromFile(databaseDir + "/schema.txt");
    }

    private void loadSchemaFromFile(String schemaFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(schemaFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\s+");
                Table table = new Table(parts[0]);
                List<Column> columns = new ArrayList<>();
                for (int i = 1; i < parts.length; i++) {
                    columns.add(new Column(table, parts[i]));
                }
                tables.put(table, columns);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getTableDir(String table) {
        return this.dataDir + table + ".csv";
    }

    public int getColumnIndex(Column column) {
        for (Entry<Table, List<Column>> table : this.tables.entrySet()) {
            if (table.getKey().getName().equals(column.getTable().getName())) {
                for (Column c : table.getValue()) {
                    if (c.getColumnName().equals(column.getColumnName())) {
                        return table.getValue().indexOf(c);
                    }
                }
            }
        }
        return -1;
    }

    public int getColumnIndex(Column column, FromItem fromItem, List<Join> joins) {
        // create a temporary column list based on the order of the join
        // go though all columns of fromItem and joins and stick them together
        List<Column> tempColumns = new ArrayList<>();
        Table tempTable = new Table(fromItem.toString());
        tempColumns.addAll(getAllColumns(tempTable));

        for (Join join : joins) {
            tempTable = new Table(join.getFromItem().toString());
            tempColumns.addAll(getAllColumns(tempTable));
        }

        for (Column c : tempColumns) {
            if (c.getColumnName().equals(column.getColumnName())) {
                return tempColumns.indexOf(c);
            }
        }

        return -1;
    }

    private List<Column> getAllColumns(Table table) {
        for (Entry<Table, List<Column>> t : this.tables.entrySet()) {
            if (t.getKey().getName().equals(table.getName())) {
                return t.getValue();
            }
        }
        return null;
    }

}
