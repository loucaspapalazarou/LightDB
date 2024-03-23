package ed.inf.adbs.lightdb.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
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

}
