package com.apl.model;

import java.util.ArrayList;
import java.util.List;

public class Form {
    private List<Field> fieldList = new ArrayList<>();
    private int id;
    private int resolvedID;
    private String name = "";

    public Form(String name, int id, int resolvedID) {
        this.name = name;
        this.id = id;
        this.setResolvedID(resolvedID);
    }

    public void addField(String fieldName, Integer id) {
        Field field = new Field(fieldName, id);
        this.fieldList.add(field);
    }

    public Field getField(int fieldID) {
        for (Field field : this.fieldList) {
            if (field.getId() == fieldID) {
                return field;
            }
        }

        return null;
    }

    public Field getField(String fieldName) {
        for (Field field : this.fieldList) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }

        return null;
    }

    public List<Field> getFieldList() {
        return this.fieldList;
    }

    public int getID() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setFieldList(List<Field> fieldList) {
        this.fieldList = fieldList;
    }

    public void setID(int formID) {
        this.id = formID;
    }

    public void setName(String formName) {
        this.name = formName;
    }

    public int getResolvedID() {
        return this.resolvedID;
    }

    public void setResolvedID(int resolvedID) {
        this.resolvedID = resolvedID;
    }
}
