package ed.inf.adbs.lightdb.query;

import java.io.FileReader;
import java.util.List;

import ed.inf.adbs.lightdb.catalog.DatabaseCatalog;
import ed.inf.adbs.lightdb.operator.*;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.SelectItem;

/**
 * The QueryInterpreter class is responsible for parsing SQL queries and
 * generating query execution plans.
 */
public class QueryInterpreter {

    private DatabaseCatalog catalog;

    /**
     * Constructs a QueryInterpreter object with the specified database catalog.
     * 
     * @param catalog the database catalog
     */
    public QueryInterpreter(DatabaseCatalog catalog) {
        this.catalog = catalog;
    }

    /**
     * Parses the SQL query from the specified file and generates a query execution
     * plan. This method goes through the statements of the query, creating
     * operators appropriately one on top of the other. Some oeprators are mandatory
     * and
     * others are not. Whenever a new operator is created, it is set as the root
     * operator from which getNextTuple/dump will be called. Tuples will go
     * through all the operators in the query plan, filtering them or altering them
     * as each operator specifies.
     * 
     * @param fileName the path to the file containing the SQL query
     * @return the query execution plan
     */
    public QueryPlan parseQuery(String fileName) {
        try {
            // Parse using JSQLParser
            Statement statement = CCJSqlParserUtil.parse(new FileReader(fileName));
            if (statement == null) {
                throw new JSQLParserException();
            }

            // Extract the plain select object, which contains all the info needed
            PlainSelect select = (PlainSelect) statement;

            // Initialize the root operator
            Operator rootOperator = null;

            // Mandatory scan
            ScanOperator scanOperator = new ScanOperator(select.getFromItem(), this.catalog);
            rootOperator = scanOperator;

            // Optional where clause
            Expression whereExpression = select.getWhere();
            if (whereExpression != null) {
                SelectOperator selectOperator = new SelectOperator(rootOperator,
                        whereExpression, this.catalog);
                rootOperator = selectOperator;
            }

            // Handle joins
            List<Join> joins = select.getJoins();
            if (joins != null) {
                // Each join in the join list is essentially a table
                for (Join join : joins) {
                    // Create a scan for that table, conceptually set it to the right branch
                    Operator right = new ScanOperator(join.getFromItem(), this.catalog);
                    // If a WHERE is present, we set the operator of 'right' as a select
                    // operator on top of its scan. The WHERE may not reference any column of the
                    // join, but we cannot know that at this point, thus we create a select operator
                    // regardless
                    if (whereExpression != null) {
                        right = new SelectOperator(right, whereExpression, this.catalog);
                    }
                    // The root operator is updated with a join operator and the left child is set
                    // at the previous root and the right child is the new created child. Using this
                    // strategy, for each join table, we update the left child and thus end up with
                    // a left-deep tree. As select operators are used on top of scans for each right
                    // child, it is ensured that no unessecary tuples will be being joined. This is
                    // done to avoid computing a cross product of all tables and filtering tuples
                    // afterward, resulting in unessecary computation.
                    rootOperator = new JoinOperator(rootOperator, right, whereExpression, this.catalog);
                }
            }

            // Handle group by / sum
            // It is possible that SUM exists without GROUP BY and vise versa, thus both
            // cases need to be handled
            Function sumFunction = null;
            // Look of a SUM function in the SELECT items
            for (SelectItem<?> s : select.getSelectItems()) {
                if (s.getExpression() instanceof Function) {
                    sumFunction = (Function) s.getExpression();
                    break;
                }
            }
            // Try get a GROUP BY element
            GroupByElement groupByElement = select.getGroupBy();
            // If either element is present, a sum operator is needed.
            if (groupByElement != null || sumFunction != null) {
                SumOperator sumOperator = new SumOperator(rootOperator, groupByElement, sumFunction);
                rootOperator = sumOperator;
            }

            // Optional projection
            boolean includeProjection = true;
            // Look for the AllColumns instance of a Select item
            for (SelectItem<?> selectItem : select.getSelectItems()) {
                // If '*' in the selection, we don't need the ProjectionOperator, therefore set
                // the flag to false
                if (selectItem.getExpression() instanceof AllColumns) {
                    includeProjection = false;
                    break;
                }
            }
            if (includeProjection) {
                ProjectionOperator projectionOperator = new ProjectionOperator(rootOperator,
                        select);
                rootOperator = projectionOperator;
            }

            // Optional order by
            List<OrderByElement> orderByElements = select.getOrderByElements();
            if (orderByElements != null) {
                rootOperator = new SortOperator(rootOperator, orderByElements);
            }

            // Optional distinct
            if (select.getDistinct() != null) {
                rootOperator = new DuplicateEliminationOperator(rootOperator);
            }

            // create the query plan object with the root operator and return it
            return new QueryPlan(rootOperator);
        } catch (Exception e) {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
        return null;
    }
}
