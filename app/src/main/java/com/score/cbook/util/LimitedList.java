package com.score.cbook.util;

import java.util.ArrayList;

public class LimitedList<K> extends ArrayList<K> {

    private static final int DEFAULT_MAX_SIZE = 7;

    private int maxSize;

    public LimitedList(int size) {
        if (size <= DEFAULT_MAX_SIZE) {
            this.maxSize = DEFAULT_MAX_SIZE;
        } else {
            this.maxSize = size;
        }
    }

    public boolean add(K k) {
        boolean r = super.add(k);
        if (size() > maxSize) {
            //remove(0);
        }
        return r;
    }

    public K getYongest() {
        if (size() > 0)
            return get(size() - 1);
        else return null;
    }

    public K getOldest() {
        return get(0);
    }
}
