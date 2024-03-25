package ed.inf.adbs.lightdb.query;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ed.inf.adbs.lightdb.catalog.DatabaseCatalog;
import ed.inf.adbs.lightdb.operator.*;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.FromItem;
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
    public QueryPlan createQueryPlan(String fileName) {
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
            rootOperator = new ScanOperator(select.getFromItem(), this.catalog);

            // Optional where clause
            Expression whereExpression = select.getWhere();
            if (whereExpression != null) {
                rootOperator = new SelectOperator(rootOperator, whereExpression);
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
                        right = new SelectOperator(right, whereExpression);
                    }
                    // The root operator is updated with a join operator and the left child is set
                    // at the previous root and the right child is the new created child. Using this
                    // strategy, for each join table, we update the left child and thus end up with
                    // a left-deep tree. As select operators are used on top of scans for each right
                    // child, it is ensured that no unessecary tuples will be being joined. This is
                    // done to avoid computing a cross product of all tables and filtering tuples
                    // afterward, resulting in unessecary computation.
                    rootOperator = new JoinOperator(rootOperator, right, whereExpression);
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
                rootOperator = new SumOperator(rootOperator, groupByElement, sumFunction);
            }

            // Optional projection. In the case of '*' the operator handles the tuple
            // appropriately
            rootOperator = new ProjectionOperator(rootOperator, select.getSelectItems());

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

    /**
     * This function behaves the same as createQueryPlanOptimized but with some
     * optimizations.
     * 
     * 1. Join Order
     * At a high level the Join order is handled like so: The WHERE clause is
     * analyzed and a value is assigned to each table based of how selective the
     * expressions in WHERE are. Each inequalitie (>, <, >=, <=, !=) gets a value of
     * 1 and the equality (=) a value of 2. The value of each table is added up and
     * we end up with an estimate on the selectivity of the query based on tables.
     * We then perform the joins in descending order of table selectiviity. This
     * approach is a heuristic way to minimize the intermideate tuples because of
     * the fact that Selection is performed before joins. However, if the query
     * requested all the columns of the resulting tuple, we have an ordering
     * problem because of the join order. To address this, we save a copy of the
     * requested join order and if the selection is of type '*', we simply expand
     * the '*' to all columns of all tables. Then, the projection operator handles
     * the reordering of the tuple elements.
     * 
     * 2. Early Projection
     * The query is checked to see whether is possible to perform projection before
     * joins. Although this will not reduce the number of tuples proccessed, it will
     * reduce their "width" and essentially save some procssing memory. An early
     * projection is possible if the columns referenced in the WHERE expression are
     * a subset of the ones in the SELECT. If that is the case, we simply perform
     * the Projection at an earlier stage.
     * 
     * @param fileName the path to the file containing the SQL query
     * @return the query execution plan
     */
    public QueryPlan createQueryPlanOptimized(String fileName) {
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

            // Other initializations
            Expression whereExpression = select.getWhere();
            boolean earlyProjection = QueryUtils.isEarlyProjectPossible(select);
            List<SelectItem<?>> expandedItems = null;
            List<Join> joins = select.getJoins();
            List<SelectItem<?>> selectItems = select.getSelectItems();

            // the joins can only be reordered if they exist
            if (joins != null) {
                // keep track of all the tables
                ArrayList<FromItem> tables = new ArrayList<>();
                // keep track of each table's heuristic selectivity value
                ArrayList<Integer> values = new ArrayList<>();
                // populate the lists
                FromItem fromItem = select.getFromItem();
                tables.add(fromItem);
                values.add(0);
                for (Join join : select.getJoins()) {
                    tables.add(join.getFromItem());
                    values.add(0);
                }

                QueryUtils.calculateJoinSelectivityOfTables(tables, values, select.getWhere());

                // make a copy of the join order that the user requested
                ArrayList<FromItem> initialJoinOrder = new ArrayList<>();
                for (FromItem t : tables) {
                    initialJoinOrder.add(t);
                }

                // we now need a root operator to start adding right operators to
                // the first operator is simply the one with the highest value
                int maxIdx;
                maxIdx = values.indexOf(Collections.max(values));
                rootOperator = new ScanOperator(tables.get(maxIdx), this.catalog);
                if (earlyProjection) {
                    rootOperator = new ProjectionOperator(rootOperator, selectItems);
                }
                if (whereExpression != null) {
                    rootOperator = new SelectOperator(rootOperator, whereExpression);
                }
                // we remove the tables we already used
                tables.remove(maxIdx);
                values.remove(maxIdx);

                // go thourgh the remaining tables, adding an operator corresponding to it
                Operator rightOperator;
                while (tables.size() > 0) {
                    maxIdx = values.indexOf(Collections.max(values));
                    // create the table's scan
                    rightOperator = new ScanOperator(tables.get(maxIdx), this.catalog);
                    // add possible early join
                    if (earlyProjection) {
                        rightOperator = new ProjectionOperator(rightOperator, selectItems);
                    }
                    // add possible selection
                    if (whereExpression != null) {
                        rightOperator = new SelectOperator(rightOperator, whereExpression);
                    }
                    // remove the used table
                    tables.remove(maxIdx);
                    values.remove(maxIdx);
                    // update the root with the new right
                    rootOperator = new JoinOperator(rootOperator, rightOperator, whereExpression);
                }

                // in the case that the projection is '*', we need to reorder
                // therefore, we use all the tables' columns to expand the projection to include
                // them all
                if (select.getSelectItem(0).getExpression() instanceof AllColumns) {
                    expandedItems = new ArrayList<>();
                    // iterate all tables with initial order
                    for (FromItem table : initialJoinOrder) {
                        // add all their columns to a new list of select items that will be used later
                        List<Column> columns = catalog.getAllColumns((Table) table);
                        for (Column column : columns) {
                            SelectItem<?> selectItem = new SelectItem<>(column);
                            expandedItems.add(selectItem);
                        }
                    }
                }
                // if no joins are present, we simply create the root operator from the FromItem
            } else {
                rootOperator = new ScanOperator(select.getFromItem(), this.catalog);
                if (earlyProjection) {
                    rootOperator = new ProjectionOperator(rootOperator, selectItems);
                }
                if (whereExpression != null) {
                    rootOperator = new SelectOperator(rootOperator, whereExpression);
                }
            }

            // Handle group by / sum
            // It is possible that SUM exists without GROUP BY and vise versa, thus both
            // cases need to be handled
            Function sumFunction = null;
            // Look of a SUM function in the SELECT items
            for (SelectItem<?> s : selectItems) {
                if (s.getExpression() instanceof Function) {
                    sumFunction = (Function) s.getExpression();
                    break;
                }
            }
            // Try get a GROUP BY element
            GroupByElement groupByElement = select.getGroupBy();
            // If either element is present, a sum operator is needed.
            if (groupByElement != null || sumFunction != null) {
                rootOperator = new SumOperator(rootOperator, groupByElement, sumFunction);
            }

            // Optional projection. In the case of '*' the operator handles the tuple
            // appropriately
            if (!earlyProjection) {
                // if expandedItems is not null, this means that there was a need to reorder
                // join columns and thus the new expandedItems list is used as the new
                // Projection blueprint
                if (expandedItems != null) {
                    rootOperator = new ProjectionOperator(rootOperator, expandedItems);
                    // otherwise use the normal selectItems
                } else {
                    rootOperator = new ProjectionOperator(rootOperator, selectItems);
                }
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
