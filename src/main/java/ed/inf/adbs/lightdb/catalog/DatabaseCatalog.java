package ed.inf.adbs.lightdb.catalog;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DatabaseCatalog {

    private String dataDir;
    private Map<String, Table> tables;

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
                String tableName = parts[0];
                String[] columns = new String[parts.length - 1];
                System.arraycopy(parts, 1, columns, 0, columns.length);
                addTable(tableName, columns);
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your requirements
        }
    }

    public void addTable(String tableName, String... columns) {
        Table table = new Table(tableName, columns);
        tables.put(tableName, table);
    }

    public String getTableDir(String table) {
        return this.dataDir + table + ".csv";
    }

    public Table getTable(String tableName) {
        return tables.get(tableName);
    }
}
