package com.cidtepole.tcpclientbridge.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

import com.cidtepole.tcpclientbridge.communication.Communicator;
import com.cidtepole.tcpclientbridge.R;

public class DialogMultiplePickerFragment extends AppCompatDialogFragment {

    public static final String BROADCAST_MULTIPICK = "BROADCAST_MULTIPICK";
    public static final String SELECTION = "SELECTION";

    private static final String ARG_ID = "ARG_ID";
    private static final String ARG_SELECTED = "ARG_SELECTED";
    private static final String STATE_SELECTED = "STATE_SELECTED";
    private static final String ARG_ITEMS = "ARG_ITEMS";
    private static final String ARG_TITLE = "ARG_TITLE";

    private boolean[] selected;

    Communicator communicator;
    Context ctx;

    // new version of code
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        ctx = context;
        Activity activity;

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

    public static DialogMultiplePickerFragment newInstance(String[] items, String title, int... selection) {
        DialogMultiplePickerFragment fragment = new DialogMultiplePickerFragment();
        Bundle args = new Bundle();
        if (selection != null) {
            args.putIntArray(ARG_SELECTED, selection);
        }
        args.putString(ARG_ID, title);
        args.putStringArray(ARG_ITEMS, items);
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final CharSequence[] brands = getArguments().getStringArray(ARG_ITEMS);

        if (savedInstanceState != null) { //rotation
            selected = savedInstanceState.getBooleanArray(STATE_SELECTED);
        } else if (getArguments() != null) {
            selected = new boolean[brands.length];
            int[] selection = getArguments().getIntArray(ARG_SELECTED);
            for (int aSelection : selection) {
                selected[aSelection] = true;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle(getArguments().getString(ARG_TITLE)).setMultiChoiceItems(brands, selected,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        selected[which] = isChecked;
                    }
                })
                .setNegativeButton(ctx.getString(R.string.cancel), null)
                .setPositiveButton(ctx.getString(R.string.help_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        communicator.selectOptions(selected);


                    }

                });

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBooleanArray(STATE_SELECTED, selected);
    }

}