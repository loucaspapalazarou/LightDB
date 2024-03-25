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

public class QueryUtils {

    private static void extractColumnsFromExpression(Expression expression, List<Column> columns) {
        if (expression instanceof LongValue) {
            return;
        }
        if (expression instanceof Column) {
            columns.add((Column) expression);
            return;
        }
        extractColumnsFromExpression(((BinaryExpression) expression).getLeftExpression(), columns);
        extractColumnsFromExpression(((BinaryExpression) expression).getRightExpression(), columns);
    }

    private static List<Column> extractColumnsFromExpression(Expression expression) {
        List<Column> columns = new ArrayList<Column>();
        extractColumnsFromExpression(expression, columns);
        return columns;
    }

    public static boolean isEarlyProjectPossible(PlainSelect select) {
        // If SUM is present, early projection gets very complicated so we just dont do
        // it
        for (SelectItem<?> s : select.getSelectItems()) {
            if (s.getExpression() instanceof Function) {
                return false;
            }
        }

        List<SelectItem<?>> projectItems = select.getSelectItems();
        if (projectItems.get(0).getExpression() instanceof AllColumns) {
            return true;
        }
        Expression whereExpression = select.getWhere();
        List<Column> selectColumns;
        if (whereExpression != null) {
            selectColumns = extractColumnsFromExpression(whereExpression);
        } else {
            selectColumns = new ArrayList<>();
        }

        // NOW CHECK FOR SUBSET
        // select <<< project
        Set<Column> projectColumns = new HashSet<>();
        for (SelectItem<?> selectItem : projectItems) {
            projectColumns.add((Column) selectItem.getExpression());
        }

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

    private static void updateTableValues(ComparisonOperator operator, int value, ArrayList<FromItem> tables,
            ArrayList<Integer> values) {
        Expression left = operator.getLeftExpression();
        Expression right = operator.getRightExpression();
        String t1, t2;
        FromItem fromItem;
        Alias alias;
        if (!(left instanceof LongValue)) {
            // manip table
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
            // manip table
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

    // find which tables are most affected by the where condition
    public static void calculateJoinSelectivityOfTables(ArrayList<FromItem> tables, ArrayList<Integer> values,
            Expression whereExpression) {
        if (whereExpression == null) {
            return;
        }
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
        if (whereExpression instanceof BinaryExpression) {
            calculateJoinSelectivityOfTables(tables, values, ((BinaryExpression) whereExpression).getLeftExpression());
            calculateJoinSelectivityOfTables(tables, values, ((BinaryExpression) whereExpression).getRightExpression());
        }
    }

}
