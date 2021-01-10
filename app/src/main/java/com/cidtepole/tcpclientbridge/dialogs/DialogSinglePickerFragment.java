package com.cidtepole.tcpclientbridge.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

import com.cidtepole.tcpclientbridge.R;
import com.cidtepole.tcpclientbridge.communication.Communicator;

public class DialogSinglePickerFragment extends AppCompatDialogFragment {

    private static final int UNSELECTED = -1;
    private static final String ARG_ID = "ARG_ID";
    private static final String ARG_SELECTED = "ARG_SELECTED";
    private static final String ARG_ITEMS = "ARG_ITEMS";
    private static final String ARG_TITLE = "ARG_TITLE";
    private static final String STATE_SELECTED = "STATE_SELECTED";

    private int selected;

    Communicator communicator;
    Context ctx;

    // new version of code
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity;

        ctx = context;

        if (context instanceof Activity){
            activity=(Activity) context;
            try {
                communicator = (Communicator) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement Communicator");
            }
        }

    }

    public static DialogSinglePickerFragment newInstance(String id, Integer selection, String[] items, String title) {
        DialogSinglePickerFragment fragment = new DialogSinglePickerFragment();
        Bundle args = new Bundle();
        if (selection != null) {
            args.putInt(ARG_SELECTED, selection);
        }
        args.putString(ARG_ID, id);
        args.putStringArray(ARG_ITEMS, items);
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) { //rotation
            selected = savedInstanceState.getInt(STATE_SELECTED, UNSELECTED);
        } else if (getArguments() != null) {
            selected = getArguments().getInt(ARG_SELECTED, UNSELECTED);
        }

        final String[] brands = getArguments().getStringArray(ARG_ITEMS);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle(getArguments().getString(ARG_TITLE))
                .setSingleChoiceItems(brands, selected, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selected = which;
                    }
                })
                .setNegativeButton(ctx.getString(R.string.cancel), null)
                .setPositiveButton(ctx.getString(R.string.help_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selected != UNSELECTED) {
                            communicator.selectOption(getArguments().getString(ARG_ID), getArguments().getStringArray(ARG_ITEMS)[selected]);
                            // /Toast.makeText(getContext(), brands[selected], Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED, selected);
    }

}