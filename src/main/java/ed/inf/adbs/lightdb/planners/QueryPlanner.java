package ed.inf.adbs.lightdb.planners;

import java.io.FileReader;

import ed.inf.adbs.lightdb.catalog.DatabaseCatalog;
import ed.inf.adbs.lightdb.operators.ScanOperator;
import ed.inf.adbs.lightdb.types.QueryResult;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.Statement;

public class QueryPlanner {

    private DatabaseCatalog catalog;
    private ScanOperator scanOperator;

    public QueryPlanner(DatabaseCatalog catalog) {
        this.catalog = catalog;
    }

    public void parseQuery(String fileName) {
        try {
            Statement statement = CCJSqlParserUtil.parse(new FileReader(fileName));
            if (statement == null) {
                throw new JSQLParserException();
            }
            PlainSelect select = (PlainSelect) statement;
            this.scanOperator = new ScanOperator(select.getFromItem(), catalog);

        } catch (Exception e) {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
        return;
    }

    public QueryResult evaluate() {
        return null;
    }
}
