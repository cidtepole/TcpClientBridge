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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.cidtepole.tcpclientbridge.MainActivity;
import com.cidtepole.tcpclientbridge.R;
import com.cidtepole.tcpclientbridge.ServersActivity;
import com.cidtepole.tcpclientbridge.communication.Communicator;

import java.io.IOException;



public class DialogConnect extends DialogFragment {

    private static final String ARG_ID = "ID";
    private static final String ARG_TITLE = "ARG_TITLE";
    private static final String ARG_IP = "ARG_IP";
    private static final String ARG_PORT = "ARG_PORT";
    private static final String ARG_PREF_CONNECT = "ARG_PREF";

    ConnectDialogListener mListener;
    Context ctx;


    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface ConnectDialogListener {
        public void onDialogConnectClick(String id, String ip, int port, boolean connect_on_opening);
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
                mListener = (ConnectDialogListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement Communicator");
            }
        }

    }


    public static DialogConnect newInstance(String id, String title, String ip, int port, boolean pref_connect_on_opening) {
        DialogConnect fragment = new DialogConnect();
        Bundle args = new Bundle();
        args.putString(ARG_ID, id);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_IP, ip);
        args.putInt(ARG_PORT, port);
        args.putBoolean(ARG_PREF_CONNECT, pref_connect_on_opening);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_connect, null);
        builder.setView(view);
        final Button mBtConnect = (Button) view.findViewById(R.id.btn_connect);
        final Button mBtCancel = (Button) view.findViewById(R.id.btn_cancel);
        final EditText  mEtIP = (EditText) view.findViewById(R.id.et_ip);
        final EditText  mEtPort = (EditText) view.findViewById(R.id.et_port);
        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox_connect);


        if(getArguments().getString(ARG_ID).equals(ServersActivity.ID_FAB_EVENT))
            mBtConnect.setText("ADD");
        else
           mBtConnect.setText("CONNECT");

        mEtIP.setText(getArguments().getString(ARG_IP));
        int port = getArguments().getInt(ARG_PORT);
        if(port==0)
            mEtPort.setText("");
        else
            mEtPort.setText(String.valueOf(port));

        checkBox.setChecked(getArguments().getBoolean(ARG_PREF_CONNECT));
        Log.i("connect_on_opening", String.valueOf(getArguments().getBoolean(ARG_PREF_CONNECT)));

        mBtConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String ip = mEtIP.getText().toString();
                int port = 0;
                boolean connect_on_opening = checkBox.isChecked();

                if(!mEtPort.getText().toString().equals(""))
                   port = Integer.parseInt( mEtPort.getText().toString());

                dismiss();
                mListener.onDialogConnectClick(getArguments().getString(ARG_ID),ip, port,connect_on_opening);
            }
        });

        mBtCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });

        return builder.create();
    }





}