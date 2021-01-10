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
import com.cidtepole.tcpclientbridge.data.InternalStorage;
import com.cidtepole.tcpclientbridge.data.Tools;
import com.cidtepole.tcpclientbridge.model.Item;
import com.cidtepole.tcpclientbridge.R;
import com.cidtepole.tcpclientbridge.adapter.SummaryDetailsListAdapter;
import com.cidtepole.tcpclientbridge.communication.Communicator;
import com.cidtepole.tcpclientbridge.model.*;
import com.xw.repo.XEditText;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class SummaryFragment extends Fragment {


    //CustomKeyboard mCustomKeyboard;
    //private Button btn_send, btn_select_client;
    //private EditText et_content;
    public static SummaryDetailsListAdapter adapter;


    private ListView listview;
    private List<Item> items = new ArrayList<>();

    private Button btn_send, btn_select_ascii, btn_select_client;
    private EditText et_content;
    private XEditText et_content_hex;
    private LinearLayout macros_row1, macros_row2, macros_row3;
    private ArrayList<Button> btn_macros;
    public static boolean is_ascii_format = true;



    private Communicator com; // communication interface object

    View view;

    // The list that should be saved to internal storage.
    private ArrayList<Macro> macros;


    int mLastFirstVisibleItem;
    int mLastVisibleItemCount;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_sumario, container, false);

        // activate fragment menu
        setHasOptionsMenu(true);



        initComponen();


        adapter = new SummaryDetailsListAdapter(getActivity(), items);
        listview.setAdapter(adapter);
        listview.setSelectionFromTop(adapter.getCount(), 0);
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

    public void initComponen() {
        listview = (ListView) view.findViewById(R.id.listview_summ);

        macros_row1 =(LinearLayout) view.findViewById(R.id.macros_row1);
        //macros_row1.setVisibility(View.GONE);

        macros_row2 =(LinearLayout) view.findViewById(R.id.macros_row2);
        //macros_row2.setVisibility(View.GONE);

        macros_row3 =(LinearLayout) view.findViewById(R.id.macros_row3);
        //macros_row3.setVisibility(View.GONE);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String row_buttons = pref.getString(MainActivity.ROWS_SUMM, "NONE");

        setVisibleRowButtons(row_buttons);

        initButtonMacros();

        btn_send = (Button)  view.findViewById(R.id.btn_send_Sumary);
        btn_select_client = (Button)  view.findViewById(R.id.btn_select_client);
        //fab = (FloatingActionButton) view.findViewById(R.id.fab);

        btn_select_client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*
                //com.showMyDialog(Communicator.dialogSelectClient);
                if(macros_row1.getVisibility()==View.VISIBLE){
                    macros_row1.setVisibility(View.GONE);
                    macros_row2.setVisibility(View.GONE);
                    macros_row3.setVisibility(View.GONE);

                }

                else{
                    macros_row1.setVisibility(View.VISIBLE);
                    macros_row2.setVisibility(View.VISIBLE);
                    macros_row3.setVisibility(View.VISIBLE);
                }
                */

                com.showMyDialog(MainActivity.dialogSelectRowButtonsSumm);

            }
        });

        btn_select_ascii = (Button)  view.findViewById(R.id.btn_sumary_Ascii);
        btn_select_ascii.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.showMyDialog(Communicator.dialogSelectFormatSummary);
            }
        });


        et_content = (EditText)  view.findViewById(R.id.text_content_Sumary);
        et_content_hex = (XEditText)  view.findViewById(R.id.text_content_Sumary_Hex);
        et_content_hex.setVisibility(View.GONE);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String data = "";
                if(is_ascii_format){
                  data = et_content.getText().toString();
                }else{
                   data = et_content_hex.getText().toString();
                }

                com.writeToServer(data);
                //items.add(items.size(), new Item(items.size(), "Jorge", data, Constant.formatTime(System.currentTimeMillis()),0));
                et_content.setText("");
                bindView();
                hideKeyboard();
            }
        });

        et_content.addTextChangedListener(contentWatcher);
        if (et_content.length() == 0) {
            btn_send.setEnabled(false);
        }

        et_content_hex.addTextChangedListener(contentWatcher);
        if (et_content_hex.length() == 0) {
            btn_send.setEnabled(false);
        }

        //et_content.setNoSeparator();
        Typeface typeface = getResources().getFont(R.font.ibm_regular);
        et_content.setTypeface(typeface);
        //et_content.setNoSeparator();

        Typeface typeface_hex = getResources().getFont(R.font.ibm_hex);
        et_content_hex.setTypeface(typeface_hex);


        hideKeyboard();

    }


    private void initButtonMacros(){

        btn_macros  = new ArrayList<Button>();
        btn_macros.add((Button) view.findViewById(R.id.btn_m1));
        btn_macros.add((Button) view.findViewById(R.id.btn_m2));
        btn_macros.add((Button) view.findViewById(R.id.btn_m3));
        btn_macros.add((Button) view.findViewById(R.id.btn_m4));
        btn_macros.add((Button) view.findViewById(R.id.btn_m5));
        btn_macros.add((Button) view.findViewById(R.id.btn_m6));
        btn_macros.add((Button) view.findViewById(R.id.btn_m7));
        btn_macros.add((Button) view.findViewById(R.id.btn_m8));
        btn_macros.add((Button) view.findViewById(R.id.btn_m9));
        btn_macros.add((Button) view.findViewById(R.id.btn_m10));
        btn_macros.add((Button) view.findViewById(R.id.btn_m11));
        btn_macros.add((Button) view.findViewById(R.id.btn_m12));
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
                            com.showEditDialogMacro(MainActivity.ID_MACROS_SUMM, macro.getName(), macro.getValue(), macros.indexOf(macro));
                        Log.i("INFO", macro.getName());

                        }else{com.writeToServer(macro.getValue());}


                    }
                });

                btn_macro.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick (View view) {

                        com.showEditDialogMacro(MainActivity.ID_MACROS_SUMM, macro.getName(), macro.getValue(), macros.indexOf(macro));
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
            macros = (ArrayList<Macro>) InternalStorage.readObject(getActivity(), MainActivity.KEY_MACROS_SUMM);

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
            InternalStorage.writeObject(getContext(), MainActivity.KEY_MACROS_SUMM, macros);

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



    public void setIconButtonFormat(String format){


        if(btn_select_ascii != null){
            btn_select_ascii.setText(format);
        }

        int pattern [] = {2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,
                2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,
                2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,
                2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,
                2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,
                2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,
                2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,
                2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,
                2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,
                2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,
                2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,
                2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,
                2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,
                2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,
                2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,
                2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,
                2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,
                2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2};

        switch (format) {
            case "ASCII":
                //Typeface typeface_ascii = getResources().getFont(R.font.ibm_regular);
                //et_content.setTypeface(typeface_ascii);
                //et_content.setNoSeparator();
                //et_content.setDisableEmoji(true);
                et_content_hex.setVisibility(View.GONE);
                et_content.setVisibility(View.VISIBLE);
                is_ascii_format=true;
                break;


            case "HEX":

                et_content.setVisibility(View.GONE);
                et_content_hex.setVisibility(View.VISIBLE);
                et_content_hex.setText("");
                et_content_hex.setPattern(pattern, " ");
                is_ascii_format=false;


                break;

                default:
                    break;
        }    }


    public int getItemsSize(){
        return items.size();
    }

    public void displayItem(Item item){
        item.setId(items.size());
        items.add(items.size(), item);
        bindView();
    }

    public void deleteItems(){
        items.clear();
        bindView();
    }


    public void logData(){
        com.createTemporaryFileSumm(items);
    }

    private void hideKeyboard() {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }


    private TextWatcher contentWatcher = new TextWatcher() {

        int prevL = 0;
        int cursor = 0;

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
            adapter.notifyDataSetChanged();
            listview.setSelectionFromTop(adapter.getCount(), 0);
        } catch (Exception e) {

        }
    }

}
