package com.cpm.GetterSetter;

import java.util.ArrayList;

/**
 * Created by deepakp on 3/22/2017.
 */

public class DisplayMasterGetterSetter {

    String table_DisplayMaster;
    ArrayList<String> DISPLAY_CD = new ArrayList<>();
    ArrayList<String> DISPLAY = new ArrayList<>();

    public String getTable_DisplayMaster() {
        return table_DisplayMaster;
    }

    public void setTable_DisplayMaster(String table_DisplayMaster) {
        this.table_DisplayMaster = table_DisplayMaster;
    }

    public ArrayList<String> getDISPLAY_CD() {
        return DISPLAY_CD;
    }

    public void setDISPLAY_CD(String DISPLAY_CD) {
        this.DISPLAY_CD.add(DISPLAY_CD);
    }

    public ArrayList<String> getDISPLAY() {
        return DISPLAY;
    }

    public void setDISPLAY(String DISPLAY) {
        this.DISPLAY.add(DISPLAY);
    }
}
