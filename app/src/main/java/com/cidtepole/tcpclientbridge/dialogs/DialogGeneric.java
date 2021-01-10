package com.cidtepole.tcpclientbridge.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cidtepole.tcpclientbridge.R;
import com.cidtepole.tcpclientbridge.ServersActivity;



public class DialogGeneric extends DialogFragment {

    private static final String ARG_ID = "ID";
    private static final String ARG_TITLE = "ARG_TITLE";
    private static final String ARG_MESSAGE = "ARG_MESSAGE";
    private static final String ARG_OK_BUTTON = "ARG_OK_BUTTON";

    GenericDialogListener mListener;
    Context ctx;


    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface GenericDialogListener {
        public void onDialogGenericOkClick(int id);
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
                mListener = (GenericDialogListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement Communicator");
            }
        }

    }


    public static DialogGeneric newInstance(int id, String title, String message, String ok_button) {
        DialogGeneric fragment = new DialogGeneric();
        Bundle args = new Bundle();
        args.putInt(ARG_ID, id);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putString(ARG_OK_BUTTON, ok_button);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_generic, null);
        builder.setView(view);
        final TextView tvMessage = (TextView) view.findViewById(R.id.tv_message);
        final Button btnOK = (Button) view.findViewById(R.id.btn_ok);
        final Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);

        tvMessage.setText(getArguments().getString(ARG_MESSAGE));
        btnOK.setText(getArguments().getString(ARG_OK_BUTTON));

        btnOK.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
                mListener.onDialogGenericOkClick(getArguments().getInt(ARG_ID));
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });

        return builder.create();
    }





}