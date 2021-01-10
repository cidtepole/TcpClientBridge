package com.cidtepole.tcpclientbridge.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import android.widget.LinearLayout;
import android.widget.ListView;

import com.cidtepole.tcpclientbridge.MainActivity;
import com.cidtepole.tcpclientbridge.R;
import com.cidtepole.tcpclientbridge.adapter.TerminalDetailsListAdapter;
import com.cidtepole.tcpclientbridge.communication.Communicator;
import com.cidtepole.tcpclientbridge.data.InternalStorage;
import com.cidtepole.tcpclientbridge.model.Item;
import com.cidtepole.tcpclientbridge.model.Macro;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TerminalFragment extends Fragment {

    //private Button btn_send, btn_select_client;
    //private EditText et_content;
    public static TerminalDetailsListAdapter adapter_term;

    private ListView listview;
    private List<Item> items = new ArrayList<>();

    private LinearLayout macros_row1, macros_row2, macros_row3;
    private ArrayList<Button> btn_macros;
    // The list that should be saved to internal storage.
    private ArrayList<Macro> macros;

    private Button btn_send, btn_select_ascii;
    private EditText et_content;

    private Communicator com; // communication interface object

    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_terminal, container, false);

        // activate fragment menu
        setHasOptionsMenu(true);

        iniComponen();


        adapter_term = new TerminalDetailsListAdapter(getActivity(), items);
        listview.setAdapter(adapter_term);
        listview.setSelectionFromTop(adapter_term.getCount(), 0);
        listview.requestFocus();
        registerForContextMenu(listview);

        hideKeyboard();

        return view;
    }


    // new version of code
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity;

        if (context instanceof Activity){
            activity=(Activity) context;
            try {
                com = (Communicator) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement Communicator");
            }
        }

    }

    public void iniComponen() {
        listview = (ListView) view.findViewById(R.id.listview_term);

        macros_row1 =(LinearLayout) view.findViewById(R.id.macros_row1_term);
        //macros_row1.setVisibility(View.GONE);

        macros_row2 =(LinearLayout) view.findViewById(R.id.macros_row2_term);
        //macros_row2.setVisibility(View.GONE);

        macros_row3 =(LinearLayout) view.findViewById(R.id.macros_row3_term);
        //macros_row3.setVisibility(View.GONE);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String row_buttons = pref.getString(MainActivity.ROWS_TERM, "NONE");

        setVisibleRowButtons(row_buttons);

        initButtonMacros();


        btn_send = (Button)  view.findViewById(R.id.btn_send_term);
        et_content = (EditText)  view.findViewById(R.id.text_content_term);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String data = et_content.getText().toString();
                com.writeToSerial(data);
                //items.add(items.size(), new Item(items.size(), "Jorge", data, Constant.formatTime(System.currentTimeMillis()),0));
                et_content.setText("");
                bindView();
                hideKeyboard();
            }
        });

        btn_select_ascii = (Button)  view.findViewById(R.id.btn_terminal_Ascii);
        btn_select_ascii.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.showMyDialog(Communicator.dialogSelectFormatTerminal);
            }
        });


        et_content.addTextChangedListener(contentWatcher);
        if (et_content.length() == 0) {
            btn_send.setEnabled(false);
        }

        Typeface typeface = getResources().getFont(R.font.ibm_regular);
        et_content.setTypeface(typeface);

        hideKeyboard();

    }


    public void setVisibleRowButtons(String opcion){

        switch (opcion){

            case "NONE":
                macros_row1.setVisibility(View.GONE);
                macros_row2.setVisibility(View.GONE);
                macros_row3.setVisibility(View.GONE);
                break;

            case "1 ROW":
                macros_row1.setVisibility(View.VISIBLE);
                macros_row2.setVisibility(View.GONE);
                macros_row3.setVisibility(View.GONE);
                break;

            case "2 ROW":
                macros_row1.setVisibility(View.VISIBLE);
                macros_row2.setVisibility(View.VISIBLE);
                macros_row3.setVisibility(View.GONE);
                break;

            case "3 ROW":
                macros_row1.setVisibility(View.VISIBLE);
                macros_row2.setVisibility(View.VISIBLE);
                macros_row3.setVisibility(View.VISIBLE);
                break;

            default:
                macros_row1.setVisibility(View.GONE);
                macros_row2.setVisibility(View.GONE);
                macros_row3.setVisibility(View.GONE);
                break;
        }

    }



    private void initButtonMacros(){

        btn_macros  = new ArrayList<Button>();
        btn_macros.add((Button) view.findViewById(R.id.btn_m1_term));
        btn_macros.add((Button) view.findViewById(R.id.btn_m2_term));
        btn_macros.add((Button) view.findViewById(R.id.btn_m3_term));
        btn_macros.add((Button) view.findViewById(R.id.btn_m4_term));
        btn_macros.add((Button) view.findViewById(R.id.btn_m5_term));
        btn_macros.add((Button) view.findViewById(R.id.btn_m6_term));
        btn_macros.add((Button) view.findViewById(R.id.btn_m7_term));
        btn_macros.add((Button) view.findViewById(R.id.btn_m8_term));
        btn_macros.add((Button) view.findViewById(R.id.btn_m9_term));
        btn_macros.add((Button) view.findViewById(R.id.btn_m10_term));
        btn_macros.add((Button) view.findViewById(R.id.btn_m11_term));
        btn_macros.add((Button) view.findViewById(R.id.btn_m12_term));
        readMacros();

        try{
            for (Macro macro : macros) {
                Button btn_macro = btn_macros.get(macros.indexOf(macro));
                btn_macro.setText(macro.getName());

                btn_macro.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if(macro.getValue().equals(""))
                        {
                            com.showEditDialogMacro(MainActivity.ID_MACROS_TERM, macro.getName(), macro.getValue(), macros.indexOf(macro));
                            Log.i("INFO", macro.getName());

                        }else{com.writeToServer(macro.getValue());}


                    }
                });

                btn_macro.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick (View view) {

                        com.showEditDialogMacro(MainActivity.ID_MACROS_TERM, macro.getName(), macro.getValue(), macros.indexOf(macro));
                        Log.i("INFO", macro.getName());

                        return true;
                    }

                });

            }}catch (IndexOutOfBoundsException e){
            return;
        }

        return;
    }


    private void readMacros() {

        macros = new ArrayList<>(); //Lista de Macros a guardar

        try {

            // Retrieve the list from internal storage
            macros = (ArrayList<Macro>) InternalStorage.readObject(getActivity(), MainActivity.KEY_MACROS_TERM);

            // Display the items from the list retrieved.
            //for (Macro macro : macros) {

            //macro.setName("M");
            //  Log.d("INFO", macro.getName());
            //}

            // Save the list of entries to internal storage
            //InternalStorage.writeObject(this, KEY, cachedMacros);



        } catch (IOException e) {
            Log.e("INFO", e.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e("INFO", e.getMessage());
        }
    }


    private void updateButton(int index){


        Button btn_macro = btn_macros.get(index);
        btn_macro.setText(macros.get(index).getName());

        try{
            // Save the list of entries to internal storage
            InternalStorage.writeObject(getContext(), MainActivity.KEY_MACROS_TERM, macros);

        }catch (IndexOutOfBoundsException e){
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return;
    }


    public void updateMacro(String name, String value, int index){

        Macro macro = macros.get(index);
        macro.setName(name);
        macro.setValue(value);
        macros.set(index, macro);

        updateButton(index);

        return;

    }



    public int getItemsSize(){
        return items.size();
    }

    public void setTextSize(int size){

        if(adapter_term!=null)
        adapter_term.setTextSize(size);
    }


    public void displayItem(Item item){
        item.setId(items.size());
        items.add(items.size(), item);
        bindView();
    }




    public void logData(){
        com.createTemporaryFileTerm(items);
    }

    public void deleteItems(){
        items.clear();
        bindView();
    }





    public void setIconButtonFormat(String format){
        if(btn_select_ascii != null){
            btn_select_ascii.setText(format);
        }
    }


    private void hideKeyboard() {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }


    private TextWatcher contentWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable etd) {
            if (etd.toString().trim().length() == 0) {
                btn_send.setEnabled(false);
            } else {
                btn_send.setEnabled(true);
            }
            //draft.setContent(etd.toString());
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }
    };


    public void bindView() {
        try {
            adapter_term.notifyDataSetChanged();
            listview.setSelectionFromTop(adapter_term.getCount(), 0);
        } catch (Exception e) {

        }
    }



}