package ed.inf.adbs.lightdb.queries;

import java.io.FileReader;

import ed.inf.adbs.lightdb.catalog.DatabaseCatalog;
import ed.inf.adbs.lightdb.operators.ScanOperator;
import ed.inf.adbs.lightdb.operators.SelectOperator;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class QueryInterpreter {

    private DatabaseCatalog catalog;

    public QueryInterpreter(DatabaseCatalog catalog) {
        this.catalog = catalog;
    }

    public QueryPlan parseQuery(String fileName) {
        try {
            Statement statement = CCJSqlParserUtil.parse(new FileReader(fileName));
            if (statement == null) {
                throw new JSQLParserException();
            }
            PlainSelect select = (PlainSelect) statement;
            ScanOperator scanOperator = new ScanOperator(select.getFromItem(), this.catalog);
            SelectOperator selectOperator = new SelectOperator(scanOperator, select.getWhere(), this.catalog);
            return new QueryPlan(selectOperator);
        } catch (Exception e) {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
        return null;
    }
}
