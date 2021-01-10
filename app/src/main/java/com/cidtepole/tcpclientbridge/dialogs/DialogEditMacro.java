package com.cidtepole.tcpclientbridge.dialogs;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.cidtepole.tcpclientbridge.MainActivity;
import com.cidtepole.tcpclientbridge.R;
import com.cidtepole.tcpclientbridge.communication.Communicator;

import java.io.IOException;



public class DialogEditMacro extends DialogFragment {

    private static final String ARG_ID = "ARG_ID";
    private static final String ARG_TITLE = "ARG_TITLE";
    private static final String ARG_SELECTED = "ARG_SELECTED";
    private static final String ARG_NAME = "ARG_NAME";
    private static final String ARG_VALUE = "ARG_VALUE";
    private static final String ARG_INDEX = "ARG_INDEX";


    EditMacroDialogListener mListener;
    Context ctx;


    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface EditMacroDialogListener {
        public void onDialogEditMacroClick(String id, String name, String  value, int index);
        //public void onDialogNegativeClick(DialogFragment dialog);
    }

    // new version of code
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        ctx = context;
        Activity activity;

        if (context instanceof Activity){
            activity=(Activity) context;
            try {
                mListener = (EditMacroDialogListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement Communicator");
            }
        }

    }


    public static  DialogEditMacro newInstance(String id, String title, String name, String value, int index) {
        DialogEditMacro fragment = new DialogEditMacro();
        Bundle args = new Bundle();
        args.putString(ARG_ID, id);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_NAME, name);
        args.putString(ARG_VALUE, value);
        args.putInt(ARG_INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_macro, null);
        builder.setView(view);
        final Button btn_Save = (Button) view.findViewById(R.id.btn_save);
        final Button btn_Cancel = (Button) view.findViewById(R.id.btn_cancel_1);
        final EditText  et_Name = (EditText) view.findViewById(R.id.et_name);
        final EditText  et_Value = (EditText) view.findViewById(R.id.et_value);

        et_Name.setText(getArguments().getString(ARG_NAME));
        et_Value.setText(getArguments().getString(ARG_VALUE));

        btn_Save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                dismiss();
                mListener.onDialogEditMacroClick(getArguments().getString(ARG_ID), et_Name.getText().toString(), et_Value.getText().toString(), getArguments().getInt(ARG_INDEX));
            }
        });

        btn_Cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });

        return builder.create();
    }





}
