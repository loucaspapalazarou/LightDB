package ed.inf.adbs.lightdb.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;

/**
 * This class implements some helper functions for the optimized query plan.
 */
public class QueryUtils {

    /**
     * Extracts columns from the given expression.
     *
     * @param expression the expression to extract columns from
     * @param columns    the list to store the extracted columns
     */
    private static void extractColumnsFromExpression(Expression expression, List<Column> columns) {
        if (expression instanceof LongValue) {
            return;
        }
        if (expression instanceof Column) {
            columns.add((Column) expression);
            return;
        }
        // if expression is a binary expression (i.e. AND), recursively extract the left
        // and right
        extractColumnsFromExpression(((BinaryExpression) expression).getLeftExpression(), columns);
        extractColumnsFromExpression(((BinaryExpression) expression).getRightExpression(), columns);
    }

    /**
     * Recursive helper for extractColumnsFromExpression
     *
     * @param expression the expression to extract columns from
     * @return list of extracted columns
     */
    private static List<Column> extractColumnsFromExpression(Expression expression) {
        List<Column> columns = new ArrayList<Column>();
        extractColumnsFromExpression(expression, columns);
        return columns;
    }

    /**
     * Determines if early projection is possible based on the provided PlainSelect.
     * The condition for a projection to be possible early, is that the selection
     * columns are a subset of the projection columns.
     *
     * @param select the PlainSelect to check for early projection possibility
     * @return true if early projection is possible, false otherwise
     */
    public static boolean isEarlyProjectPossible(PlainSelect select) {
        // If SUM is present, early projection gets very complicated so we simply don't
        // do it
        for (SelectItem<?> s : select.getSelectItems()) {
            if (s.getExpression() instanceof Function) {
                return false;
            }
        }

        // if '*:, trivially, early projection is possible
        List<SelectItem<?>> projectItems = select.getSelectItems();
        if (projectItems.get(0).getExpression() instanceof AllColumns) {
            return true;
        }

        // get select columns
        Expression whereExpression = select.getWhere();
        List<Column> selectColumns;
        if (whereExpression != null) {
            selectColumns = extractColumnsFromExpression(whereExpression);
        } else {
            selectColumns = new ArrayList<>();
        }

        // get project columns
        Set<Column> projectColumns = new HashSet<>();
        for (SelectItem<?> selectItem : projectItems) {
            projectColumns.add((Column) selectItem.getExpression());
        }

        // Now check for subset: select columns <subset of> project columns
        boolean found = false;
        for (Column c1 : selectColumns) {
            found = false;
            for (Column c2 : projectColumns) {
                if (c1.getFullyQualifiedName().equals(c2.getFullyQualifiedName())) {
                    found = true;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    /**
     * Updates table values based on the comparison operator that calculate how
     * selective selection is on each table
     *
     * @param operator the comparison operator
     * @param value    the value to update
     * @param tables   list of tables
     * @param values   list of values
     */
    private static void updateTableValues(ComparisonOperator operator, int value, ArrayList<FromItem> tables,
            ArrayList<Integer> values) {
        Expression left = operator.getLeftExpression();
        Expression right = operator.getRightExpression();
        String t1, t2;
        FromItem fromItem;
        Alias alias;
        if (!(left instanceof LongValue)) {
            // go thourgh all elements and increment the one we want, also handle possible
            // alias
            for (int i = 0; i < tables.size(); i++) {
                fromItem = tables.get(i);
                t1 = ((Column) left).getTable().getFullyQualifiedName();
                alias = ((Table) fromItem).getAlias();
                if (alias == null) {
                    t2 = ((Table) fromItem).getFullyQualifiedName();
                } else {
                    t2 = ((Table) fromItem).getAlias().getName();
                }
                if (t1.equals(t2)) {
                    values.set(i, values.get(i) + value);
                }
            }
        }
        if (!(right instanceof LongValue)) {
            // go thourgh all elements and increment the one we want, also handle possible
            // alias
            for (int i = 0; i < tables.size(); i++) {
                fromItem = tables.get(i);
                t1 = ((Column) left).getTable().getFullyQualifiedName();
                alias = ((Table) fromItem).getAlias();
                if (alias == null) {
                    t2 = ((Table) fromItem).getFullyQualifiedName();
                } else {
                    t2 = ((Table) fromItem).getAlias().getName();
                }
                if (t1.equals(t2)) {
                    values.set(i, values.get(i) + value);
                }
            }
        }
    }

    /**
     * Calculates the join selectivity of tables based on the provided where
     * expression.
     *
     * @param tables          list of tables
     * @param values          list of values
     * @param whereExpression the where expression
     */
    public static void calculateJoinSelectivityOfTables(ArrayList<FromItem> tables, ArrayList<Integer> values,
            Expression whereExpression) {
        if (whereExpression == null) {
            return;
        }
        // inequalities count of 1 and equality for 2 because its more selective
        if (whereExpression instanceof ComparisonOperator) {
            boolean gt = whereExpression instanceof GreaterThan || whereExpression instanceof GreaterThanEquals;
            boolean mt = whereExpression instanceof MinorThan || whereExpression instanceof MinorThanEquals;
            boolean ne = whereExpression instanceof NotEqualsTo;
            int value;
            if (gt || mt || ne) {
                value = 1;
            } else {
                value = 2;
            }
            updateTableValues((ComparisonOperator) whereExpression, value, tables, values);
            return;
        }
        // in the case of binary expression, recursively evaluate
        if (whereExpression instanceof BinaryExpression) {
            calculateJoinSelectivityOfTables(tables, values, ((BinaryExpression) whereExpression).getLeftExpression());
            calculateJoinSelectivityOfTables(tables, values, ((BinaryExpression) whereExpression).getRightExpression());
        }
    }
}
