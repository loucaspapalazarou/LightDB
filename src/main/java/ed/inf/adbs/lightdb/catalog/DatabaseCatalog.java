package ed.inf.adbs.lightdb.catalog;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.jsqlparser.expression.Alias;
// import ed.inf.adbs.lightdb.types.Table;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class DatabaseCatalog {

    private String dataDir;
    private Map<Table, List<Column>> tables;
    private Map<Alias, Table> aliases;

    public DatabaseCatalog(String databaseDir) {
        this.dataDir = databaseDir + "/data/";
        this.tables = new HashMap<>();
        this.aliases = new HashMap<>();
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

    public void addAliases(PlainSelect select) {
        FromItem fromItem = select.getFromItem();
        this.aliases.put(fromItem.getAlias(), (Table) fromItem);
        List<Join> joins = select.getJoins();
        if (joins != null) {
            for (Join join : joins) {
                this.aliases.put(join.getFromItem().getAlias(), (Table) join.getFromItem());
            }
        }
    }

    public String getTableDir(String table) {
        return this.dataDir + table + ".csv";
    }

    public int getColumnIndex(Column column) {
        // Look for table in aliased tables
        String tableName = getTableFromAliasedTable(column);

        // if the table was found in the alias map,
        if (tableName != null) {
            for (Entry<Table, List<Column>> tableEntry : this.tables.entrySet()) {
                if (tableEntry.getKey().getName().equals(tableName)) {
                    for (Column c : tableEntry.getValue()) {
                        if (c.getColumnName().equals(column.getColumnName())) {
                            return tableEntry.getValue().indexOf(c);
                        }
                    }
                }
            }
        }

        // Look for that table in normal tables
        for (Entry<Table, List<Column>> tableEntry : this.tables.entrySet()) {
            if (tableEntry.getKey().getName().equals(column.getTable().getName())) {
                for (Column c : tableEntry.getValue()) {
                    if (c.getColumnName().equals(column.getColumnName())) {
                        return tableEntry.getValue().indexOf(c);
                    }
                }
            }
        }
        return -1;
    }

    // TODO: Fix
    public int getColumnIndex(Column column, FromItem fromItem, List<Join> joins) {
        // System.out.println(getTableFromAliasedTable((Table) fromItem));
        // create a temporary column list based on the order of the join
        // go though all columns of fromItem and joins and stick them together
        List<Column> tempColumns = new ArrayList<>();
        Table tempTable = new Table(((Table) fromItem).getName());
        tempColumns.addAll(getAllColumns(tempTable));

        for (Join join : joins) {
            tempTable = new Table(((Table) join.getFromItem()).getName());
            tempColumns.addAll(getAllColumns(tempTable));
        }

        for (Column c : tempColumns) {
            if (c.getColumnName().equals(column.getColumnName())) {
                return tempColumns.indexOf(c);
            }
        }

        return -1;
    }

    private String getTableFromAliasedTable(Column column) {
        for (Entry<Alias, Table> aliasEntry : this.aliases.entrySet()) {
            if (column.getTable().getName().equals(aliasEntry.getKey().getName())) {
                return aliasEntry.getValue().getName();
            }
        }
        return null;
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
