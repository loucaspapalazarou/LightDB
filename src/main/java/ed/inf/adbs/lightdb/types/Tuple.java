package ed.inf.adbs.lightdb.types;

public class Tuple {
    private int[] attrs;

    public Tuple(int... attrs) {
        this.attrs = attrs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < attrs.length; i++) {
            sb.append(attrs[i]);
            if (i < attrs.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}