package com.cidtepole.tcpclientbridge.communication;

import com.cidtepole.tcpclientbridge.model.Item;

import java.util.List;

public interface Communicator {

    public static final int dialogSelectClient = 0;
    public static final int dialogSelectFormatSummary = 1;
    public static final int dialogSelectFormatTerminal = 2;
    public static final int dialogSelectRowButtonsSumm = 3;
    public static final int dialogSelectRowButtonsTerm = 4;

    public static final int SelectFormatSummary = 0;
    public static final int SelectFormatTerminal = 1;


    public void writeToServer(String data);

    public void writeToSerial(String data);

    public void showMyDialog(int dialog);

    public void showEditDialogMacro(String id, String name, String value, int index);

    public void selectOption(String id, String option);

    public void selectOptions(boolean[] options);

    public void createTemporaryFileSumm(List<Item> messages);

    public void createTemporaryFileTerm(List<Item> messages);



}
