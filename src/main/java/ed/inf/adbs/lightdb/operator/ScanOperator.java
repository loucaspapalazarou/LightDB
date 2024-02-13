package ed.inf.adbs.lightdb.operator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import ed.inf.adbs.lightdb.catalog.DatabaseCatalog;
import ed.inf.adbs.lightdb.type.Tuple;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;

/**
 * The ScanOperator class represents an operator that scans tuples from a table.
 * It reads tuples from the corresponding data file and provides methods to
 * retrieve the next tuple and reset the operator.
 */
public class ScanOperator extends Operator {

    private String tableName; // Name of the table
    private FromItem fromItem; // FromItem representing the table
    private BufferedReader reader; // BufferedReader to read data from file
    private DatabaseCatalog catalog; // Database catalog for table information

    /**
     * Constructs a ScanOperator with the specified FromItem and database catalog.
     * 
     * @param fromItem the FromItem representing the table to scan
     * @param catalog  the database catalog
     */
    public ScanOperator(FromItem fromItem, DatabaseCatalog catalog) {
        this.fromItem = fromItem;
        this.tableName = ((Table) fromItem).getName();
        this.catalog = catalog;
        this.reader = openFileReader(((Table) fromItem).getName());
    }

    /**
     * Opens a file reader for the given table name.
     * 
     * @param tableName the name of the table
     * @return a BufferedReader to read data from the table file
     */
    private BufferedReader openFileReader(String tableName) {
        try {
            String filePath = catalog.getTableDir(tableName);
            return new BufferedReader(new FileReader(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves and parses the raw text in order to create next tuple from the
     * table.
     * 
     * @return the next tuple, or null if there are no more tuples
     */
    @Override
    public Tuple getNextTuple() {
        try {
            String line = reader.readLine();
            if (line != null) {
                // read and split the strings
                String[] values = line.split(",");
                // convert to integers
                int[] intValues = new int[values.length];
                for (int i = 0; i < values.length; i++) {
                    intValues[i] = Integer.parseInt(values[i].trim());
                }
                // as we know the source table, we create a tuple using that table's columns
                // the getAllColumns function takes is a Table argument but as we don't have
                // that reference we create a new one with the same name.
                List<Column> columns = catalog.getAllColumns(new Table(tableName));
                try {
                    // the tuple is created and returned
                    return new Tuple(columns, intValues, this.fromItem);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Resets the state of the operator by reopening the file reader.
     */
    @Override
    public void reset() {
        // Reopen the file reader to start from the beginning
        this.reader = openFileReader(tableName);
    }
}
