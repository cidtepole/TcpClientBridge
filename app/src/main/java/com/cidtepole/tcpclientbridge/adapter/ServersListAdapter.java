package com.cidtepole.tcpclientbridge.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cidtepole.tcpclientbridge.MainActivity;
import com.cidtepole.tcpclientbridge.R;
import com.cidtepole.tcpclientbridge.model.Item;
import com.cidtepole.tcpclientbridge.model.Server;

import java.util.ArrayList;
import java.util.List;

public class ServersListAdapter extends RecyclerView.Adapter<ServersListAdapter.ViewHolder> {

    private List<Server> server_items;

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;
    private boolean clicked = false;


    public interface OnItemClickListener {
           void onItemClick(View view, Server obj, int position);
           void onDeleteClick(int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ServersListAdapter(Context context, List<Server> server_items) {
        this.server_items = server_items ;
        ctx = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        // each data item is just a string in this case
        public TextView tv_ip;
        public TextView tv_port;
        public ImageButton ib_button;
        public LinearLayout lyt_parent;

        public ViewHolder(View v) {
            super(v);
            tv_ip = (TextView) v.findViewById(R.id.tv_ip_row);
            tv_port = (TextView) v.findViewById(R.id.tv_port_row);
            ib_button = (ImageButton) v.findViewById(R.id.ib_close);
            lyt_parent = (LinearLayout) v.findViewById(R.id.lyt_parent_server);
        }
    }


    @Override
    public ServersListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_server, parent, false);

        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Server  server = server_items.get(position);
        holder.tv_ip.setText(server.getIp());
        holder.tv_port.setText(":" + String.valueOf(server.getPort()));


        holder.ib_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {

                    mOnItemClickListener.onDeleteClick(position);

                }
            }
        });



        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnItemClickListener != null) {
                    //clicked = true;
                    mOnItemClickListener.onItemClick(view, server, position);
                    //Log.i("INFO", server.getIp());
                }
                //Log.i("INFO", server.getIp());
            }
        });
        //clicked = false;
    }

    public Server getItem(int position){
        return server_items.get(position);
    }



    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return server_items.size();
    }

}
