package ed.inf.adbs.lightdb.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.FromItem;

public class Tuple {
    private List<TupleElement> elements;

    public Tuple(List<TupleElement> elements) {
        this.elements = elements;
    }

    public Tuple() {
        this.elements = new ArrayList<>();
    }

    public Tuple(List<Column> columns, int[] values, FromItem fromItem) throws Exception {
        if (columns.size() != values.length) {
            throw new Exception("Number of columns in schema does not match data.");
        }
        this.elements = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            TupleElement newTupleElement = null;
            Column c = columns.get(i);
            String aliasName = null;
            Alias alias = fromItem.getAlias();
            if (alias != null) {
                aliasName = alias.getName();
            }
            newTupleElement = new TupleElement(c.getColumnName(), c.getTable().getName(), aliasName, values[i]);
            this.elements.add(newTupleElement);
        }
    }

    public List<TupleElement> getElements() {
        return this.elements;
    }

    public static Tuple concatTuples(Tuple a, Tuple b) {
        Tuple t = new Tuple();
        t.elements.addAll(a.getElements());
        t.elements.addAll(b.getElements());
        return t;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < this.elements.size(); i++) {
            sb.append(this.elements.get(i));
            if (i < this.elements.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public Integer getValueAt(Column column) {
        for (TupleElement te : this.elements) {
            if (te.columnsMatch(column)) {
                return te.getValue();
            }
        }
        return null;
    }

    public String toStringDebug() {
        String s = "[";
        for (TupleElement t : this.elements) {
            s += t.getFullName() + "=" + t.getValue() + ", ";
        }
        s += "]";
        return s.replace(", ]", "]");
    }

    public void append(Column c, int val) {
        String tableName = c.getTable().getName();
        String columnName = c.getColumnName();
        Alias alias = c.getTable().getAlias();
        String aliasName = null;
        if (alias != null) {
            aliasName = alias.getName();
        }
        TupleElement t = new TupleElement(columnName, tableName, aliasName, val);
        this.elements.add(t);
    }

    public Integer getIndexOf(Column c) {
        for (TupleElement te : this.elements) {
            if (te.columnsMatch(c)) {
                return this.elements.indexOf(te);
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        // return Objects.hash(this.elements);
        int hash = 1;
        for (TupleElement element : this.elements) {
            hash = 31 * hash + Objects.hashCode(element);
        }
        return hash;
    }

}
