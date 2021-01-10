package com.cidtepole.tcpclientbridge.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;



import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.cidtepole.tcpclientbridge.R;



public class FragmentSettings extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {



    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        //add xml
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);

        Context context = getActivity();
        SharedPreferences sharedPref = context.getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        boolean isEnable = sharedPref.getBoolean("isEnable", true);

        ListPreference baudRate_List = (ListPreference) findPreference("baudRate");
        baudRate_List.setSummary(baudRate_List.getEntry());
        ListPreference dataBits_List = (ListPreference) findPreference("dataBits");
        dataBits_List.setSummary(dataBits_List.getEntry());
        ListPreference parity_List = (ListPreference) findPreference("parity");
        parity_List.setSummary(parity_List.getEntry());
        ListPreference stopBits_List = (ListPreference) findPreference("stopBits");
        stopBits_List.setSummary(stopBits_List.getValue());

        ListPreference fileFormat_List = (ListPreference) findPreference("fileFormat");
        fileFormat_List.setSummary(fileFormat_List.getEntry());

        EditTextPreference port_Edit = (EditTextPreference)findPreference("port");
        port_Edit.setSummary(port_Edit.getText());

        ListPreference textSize_List = (ListPreference) findPreference("textSize");
        textSize_List.setSummary(textSize_List.getEntry());

        EditTextPreference numOfBubbles_Edit = (EditTextPreference)findPreference("numOfBubbles");
        numOfBubbles_Edit.setSummary(numOfBubbles_Edit.getText());

        EditTextPreference numOfLines_Edit = (EditTextPreference)findPreference("numOfLines");
        numOfLines_Edit.setSummary(numOfLines_Edit.getText());


        if(!isEnable) {

            baudRate_List.setEnabled(false);

            dataBits_List.setEnabled(false);

            parity_List.setEnabled(false);

            stopBits_List.setEnabled(false);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        //unregister the preferenceChange listener
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference connectionPref = findPreference(key);
        switch (key){
            case "baudRate":
                // Set summary to be the user-description for the selected value
                connectionPref.setSummary(sharedPreferences.getString(key, ""));
                break;

            case "dataBits":
                // Set summary to be the user-description for the selected value
                connectionPref.setSummary(sharedPreferences.getString(key, ""));
                break;
            case "parity":
                ListPreference parity_List = (ListPreference) findPreference("parity");
                parity_List.setSummary(parity_List.getEntry());
                break;
            case "stopBits":
                ListPreference stopBits_List = (ListPreference) findPreference("stopBits");
                stopBits_List.setSummary(stopBits_List.getEntry());
                break;

            case "port":
                // Set summary to be the user-description for the selected value

                String value = sharedPreferences.getString(key, "");
                if(value.equals("")){
                    EditTextPreference p = (EditTextPreference) findPreference(key);
                    p.setText("1234");
                }

                connectionPref.setSummary(sharedPreferences.getString(key, ""));
                break;


            case "textSize":
                // Set summary to be the user-description for the selected value
                connectionPref.setSummary(sharedPreferences.getString(key, ""));
                break;

            case "numOfBubbles":

                String valueB = sharedPreferences.getString(key, "");
                if(valueB.equals("")||Integer.parseInt(valueB)<=1){
                    EditTextPreference p = (EditTextPreference) findPreference(key);
                    p.setText("1000");
                }

                connectionPref.setSummary(sharedPreferences.getString(key, ""));


                break;


            case "numOfLines":

                String valueC = sharedPreferences.getString(key, "");
                if(valueC.equals("")||Integer.parseInt(valueC)<=1){
                    EditTextPreference p = (EditTextPreference) findPreference(key);
                    p.setText("1000");
                }

                connectionPref.setSummary(sharedPreferences.getString(key, ""));


                break;




            case "fileFormat":
                // Set summary to be the user-description for the selected value
                ListPreference dataFormat_List = (ListPreference) findPreference("fileFormat");
                dataFormat_List.setSummary(dataFormat_List.getEntry());
                break;

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //unregister the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}



