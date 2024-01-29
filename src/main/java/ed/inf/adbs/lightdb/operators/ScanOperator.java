package ed.inf.adbs.lightdb.operators;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import ed.inf.adbs.lightdb.catalog.DatabaseCatalog;
import ed.inf.adbs.lightdb.types.Tuple;
import net.sf.jsqlparser.statement.select.FromItem;

public class ScanOperator extends Operator {
    private String tableName;
    private BufferedReader reader;
    private DatabaseCatalog catalog;

    public ScanOperator(FromItem fromItem, DatabaseCatalog catalog) {
        this.tableName = fromItem.toString();
        this.catalog = catalog;
        this.reader = openFileReader(tableName);
    }

    private BufferedReader openFileReader(String tableName) {
        try {
            String filePath = catalog.getTableDir(tableName);
            return new BufferedReader(new FileReader(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Tuple getNextTuple() {
        try {
            String line = reader.readLine();
            if (line != null) {
                String[] values = line.split(",");
                int[] intValues = new int[values.length];
                for (int i = 0; i < values.length; i++) {
                    intValues[i] = Integer.parseInt(values[i].trim());
                }
                return new Tuple(intValues);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void reset() {
        // Reopen the file reader to start from the beginning
        this.reader = openFileReader(tableName);
    }
}