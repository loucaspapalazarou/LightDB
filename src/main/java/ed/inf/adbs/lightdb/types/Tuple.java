package ed.inf.adbs.lightdb.types;

import java.util.ArrayList;
import java.util.List;

public class Tuple {
    private List<Integer> attrs;

    public Tuple(int... attrs) {
        this.attrs = new ArrayList<>();
        for (int attr : attrs) {
            this.attrs.add(attr);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < attrs.size(); i++) {
            sb.append(attrs.get(i));
            if (i < attrs.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public boolean contains(int value) {
        return attrs.contains(value);
    }
}
