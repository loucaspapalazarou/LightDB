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
        Alias fromAlias = fromItem.getAlias();
        if (fromAlias != null) {
            this.aliases.put(fromItem.getAlias(), (Table) fromItem);
        }
        List<Join> joins = select.getJoins();
        if (joins == null) {
            return;
        }
        Alias joinAlias;
        for (Join join : joins) {
            joinAlias = join.getFromItem().getAlias();
            if (joinAlias != null) {
                this.aliases.put(join.getFromItem().getAlias(), (Table) join.getFromItem());
            }
        }
    }

    public String getTableDir(String table) {
        return this.dataDir + table + ".csv";
    }

    public List<Column> getAllColumns(Table table) {
        for (Entry<Table, List<Column>> t : this.tables.entrySet()) {
            if (t.getKey().getName().equals(table.getName())) {
                return t.getValue();
            }
        }
        return null;
    }

}
