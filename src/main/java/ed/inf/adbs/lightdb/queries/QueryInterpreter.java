package ed.inf.adbs.lightdb.queries;

import java.io.FileReader;
import java.util.List;

import ed.inf.adbs.lightdb.catalog.DatabaseCatalog;
import ed.inf.adbs.lightdb.operators.JoinOperator;
import ed.inf.adbs.lightdb.operators.Operator;
import ed.inf.adbs.lightdb.operators.ProjectionOperator;
import ed.inf.adbs.lightdb.operators.ScanOperator;
import ed.inf.adbs.lightdb.operators.SelectOperator;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Join;
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
            Operator rootOperator = null;

            // parse aliases
            catalog.addAliases(select);

            // mandatory scan
            ScanOperator scanOperator = new ScanOperator(select.getFromItem(), this.catalog);
            rootOperator = scanOperator;

            // optional where
            Expression whereExpression = select.getWhere();
            if (whereExpression != null) {
                SelectOperator selectOperator = new SelectOperator(rootOperator, whereExpression, this.catalog);
                rootOperator = selectOperator;
            }

            // Possible joins
            List<Join> joins = select.getJoins();
            if (joins != null) {
                for (Join join : joins) {
                    Operator left = rootOperator;
                    ScanOperator tempScan = new ScanOperator(join.getFromItem(), this.catalog);
                    JoinOperator joinOperator = null;
                    // there might not be a WHERE, which means cartesian product
                    if (whereExpression != null) {
                        SelectOperator tempSelect = new SelectOperator(tempScan, whereExpression, this.catalog);
                        joinOperator = new JoinOperator(left, tempSelect, whereExpression, this.catalog);
                    } else {
                        joinOperator = new JoinOperator(left, tempScan, whereExpression, this.catalog);
                    }
                    rootOperator = joinOperator;
                }
            }

            // optional projection
            if (!select.getSelectItem(0).toString().equals("*")) {
                ProjectionOperator projectionOperator = new ProjectionOperator(rootOperator, select, this.catalog);
                rootOperator = projectionOperator;
            }

            return new QueryPlan(rootOperator);
        } catch (Exception e) {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
        return null;
    }
}
