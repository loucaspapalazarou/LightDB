package ed.inf.adbs.lightdb.query;

import java.io.FileReader;
import java.util.List;

import ed.inf.adbs.lightdb.catalog.DatabaseCatalog;
import ed.inf.adbs.lightdb.operator.DuplicateEliminationOperator;
import ed.inf.adbs.lightdb.operator.JoinOperator;
import ed.inf.adbs.lightdb.operator.Operator;
import ed.inf.adbs.lightdb.operator.ProjectionOperator;
import ed.inf.adbs.lightdb.operator.ScanOperator;
import ed.inf.adbs.lightdb.operator.SelectOperator;
import ed.inf.adbs.lightdb.operator.SortOperator;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
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

            // mandatory scan
            ScanOperator scanOperator = new ScanOperator(select.getFromItem(), this.catalog);
            rootOperator = scanOperator;

            // optional where
            Expression whereExpression = select.getWhere();
            if (whereExpression != null) {
                SelectOperator selectOperator = new SelectOperator(rootOperator,
                        whereExpression, this.catalog);
                rootOperator = selectOperator;
            }

            // Possible joins
            List<Join> joins = select.getJoins();
            if (joins != null) {
                for (Join join : joins) {
                    Operator right = new ScanOperator(join.getFromItem(), this.catalog);
                    if (whereExpression != null) {
                        right = new SelectOperator(right, whereExpression, this.catalog);
                    }
                    rootOperator = new JoinOperator(rootOperator, right, whereExpression, this.catalog);
                }
            }

            // optional projection
            if (!select.getSelectItem(0).toString().equals("*")) {
                ProjectionOperator projectionOperator = new ProjectionOperator(rootOperator, select);
                rootOperator = projectionOperator;
            }

            List<OrderByElement> orderByElements = select.getOrderByElements();
            if (orderByElements != null) {
                rootOperator = new SortOperator(rootOperator, orderByElements);
            }

            if (select.getDistinct() != null) {
                rootOperator = new DuplicateEliminationOperator(rootOperator);
            }

            return new QueryPlan(rootOperator);
        } catch (Exception e) {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
        return null;
    }
}
