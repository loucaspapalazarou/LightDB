package ed.inf.adbs.lightdb.type;

import java.util.Objects;

import net.sf.jsqlparser.schema.Column;

public class TupleElement {

    private String columnName;
    private String tableName;
    private String alias;
    private int value;

    public TupleElement(String columnName, String tableName, String alias, int value) {
        this.columnName = columnName;
        this.tableName = tableName;
        this.value = value;
        this.alias = alias;
    }

    public boolean columnsMatch(Column c) {
        if (alias == null) {
            return this.tableName.equals(c.getTable().getName())
                    && this.columnName.equals(c.getColumnName());
        }
        return this.alias.equals(c.getTable().getName())
                && this.columnName.equals(c.getColumnName());
    }

    public String getFullName() {
        if (this.alias == null) {
            return this.tableName + "." + this.columnName;
        }
        return this.alias + "." + this.columnName;
    }

    public int getValue() {
        return this.value;
    }

    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.tableName, this.columnName, this.alias, this.value);
    }
}