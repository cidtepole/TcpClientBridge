package com.cidtepole.tcpclientbridge.model;

import java.io.Serializable;

public class Macro implements Serializable {
    private String macro;
    private String name;
    private String value;
    private int textMode;
    private int LF;
    private int CR;

    public Macro(String macro, String name, String value, int textMode, int CR, int LF) {
        this.macro = macro;
        this.name = name;
        this.value = value;
        this.textMode = textMode;
        this.LF = LF;
        this.CR = CR;
    }

    public String getMacro() {
        return macro;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public int getTextMode() {
        return textMode;
    }

    public int getLF() {
        return LF;
    }

    public int getCR() {
        return CR;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setTextMode(int textMode) {
        this.textMode = textMode;
    }

    public void setLF(int LF) {
        this.LF = LF;
    }

    public void setCR(int CR) {
        this.CR = CR;
    }





}