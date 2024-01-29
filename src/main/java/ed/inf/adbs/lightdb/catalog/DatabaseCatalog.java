package ed.inf.adbs.lightdb.catalog;

public class DatabaseCatalog {

    private String dataDir;

    public DatabaseCatalog(String databaseDir) {
        this.dataDir = databaseDir + "/data/";
    }

    public String getTableDir(String table) {
        return this.dataDir + table + ".csv";
    }

}