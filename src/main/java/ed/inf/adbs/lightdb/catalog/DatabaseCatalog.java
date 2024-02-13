package ed.inf.adbs.lightdb.catalog;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

/**
 * The DatabaseCatalog class represents a catalog that stores information about
 * tables and their columns
 * in a database.
 */
public class DatabaseCatalog {

    private String dataDir;
    private Map<Table, List<Column>> tables;

    /**
     * Constructs a DatabaseCatalog object with the specified database directory.
     * 
     * @param databaseDir the directory where the database files are stored
     */
    public DatabaseCatalog(String databaseDir) {
        this.dataDir = databaseDir + "/data/";
        this.tables = new HashMap<>();
        loadSchemaFromFile(databaseDir + "/schema.txt");
    }

    /**
     * Loads table schema information from the specified schema file.
     * 
     * @param schemaFilePath the path to the schema file
     */
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

    /**
     * Gets the directory path for the specified table.
     * 
     * @param table the name of the table
     * @return the directory path for the table's data file
     */
    public String getTableDir(String table) {
        return this.dataDir + table + ".csv";
    }

    /**
     * Gets all columns for the specified table.
     * 
     * @param table the table object
     * @return a list of columns for the specified table
     */
    public List<Column> getAllColumns(Table table) {
        for (Entry<Table, List<Column>> t : this.tables.entrySet()) {
            if (t.getKey().getName().equals(table.getName())) {
                return t.getValue();
            }
        }
        return null;
    }
}
