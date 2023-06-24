package taskone;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

class StringList {
    
    List<String> strings = new ArrayList<String>();

    public void add(String str) {
        int pos = strings.indexOf(str);
        if (pos < 0) {
            strings.add(str);
        }
    }

    public void clear() {
        strings.clear();
    }

    public int find(String str) {
        return strings.indexOf(str);
    }

    public void sort() {
        Collections.sort(strings);
    }

    public void prepend(String str, int indx) {
        if (indx >= 0 && size() > indx) {
            strings.set(indx, str + strings.get(indx));
        }
    }

    public boolean contains(String str) {
        return strings.indexOf(str) >= 0;
    }

    public int size() {
        return strings.size();
    }

    public String toString() {
        return strings.toString();
    }
}