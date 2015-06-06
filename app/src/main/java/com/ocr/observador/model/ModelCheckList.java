package com.ocr.observador.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by Vazh on 15/5/2015.
 */
@Table(name = "ModelCheckLists")
public class ModelCheckList extends Model {

    @Column
    public String checkListJSON;

    @Column
    public String name;

    @Column
    public boolean repeatable;

    @Column
    public String nationalId;

    public ModelCheckList(String checkListJSON, String name, boolean repeatable, String nationalId) {
        this.checkListJSON = checkListJSON;
        this.name = name;
        this.repeatable = repeatable;
        this.nationalId = nationalId;
    }

    public ModelCheckList() {
        super();
    }
}
