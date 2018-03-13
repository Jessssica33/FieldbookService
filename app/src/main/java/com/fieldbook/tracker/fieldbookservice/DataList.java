package com.fieldbook.tracker.fieldbookservice;

import java.util.LinkedList;

/**
 * Created by jessica on 3/9/18.
 */

public class DataList {

    private LinkedList<String> list;
    private static DataList instance = null;

    protected DataList() {
        list = new LinkedList<>();
    }

    public static DataList getInstance() {
        if (instance == null) {
            instance = new DataList();
        }
        return instance;
    }

    public void addData(String s) {
        list.add(s);
    }

    public LinkedList<String> getList() {
        return list;
    }

}
