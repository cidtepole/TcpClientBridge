package com.cidtepole.tcpclientbridge.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.cidtepole.tcpclientbridge.model.Item;
import com.cidtepole.tcpclientbridge.R;

import java.util.List;

public class SummaryDetailsListAdapter extends BaseAdapter {
	
	private List<Item> mMessages;
	private Context ctx;

	
	public SummaryDetailsListAdapter(Context context, List<Item> messages) {
        super();
        this.ctx = context;
        this.mMessages = messages;
	}
	
	@Override
	public int getCount() {
		return mMessages.size();
	}

	@Override
	public Object getItem(int position) {
		return mMessages.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mMessages.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Item msg = (Item) getItem(position);
        ViewHolder holder;
        if(convertView == null){
        	holder 				= new ViewHolder();
        	convertView			= LayoutInflater.from(ctx).inflate(R.layout.row_summary_details, parent, false);
			holder.header 		= (TextView) convertView.findViewById(R.id.header);
        	holder.time 		= (TextView) convertView.findViewById(R.id.text_time);
        	holder.message 		= (TextView) convertView.findViewById(R.id.text_content);
			holder.lyt_thread 	= (CardView) convertView.findViewById(R.id.lyt_thread);
			holder.lyt_parent 	= (LinearLayout) convertView.findViewById(R.id.lyt_parent);
			holder.image_status	= (ImageView) convertView.findViewById(R.id.image_status);
        	convertView.setTag(holder);	
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

		holder.header.setText(msg.getEncabezado());
		holder.message.setText(msg.getContenido());
		holder.time.setText(msg.getFecha());

		switch (msg.getDisplay()){
			case Item.fromSummary:
				holder.lyt_parent.setPadding(100, 0, 15, 0);
				holder.lyt_parent.setGravity(Gravity.RIGHT);
				holder.lyt_thread.setCardBackgroundColor(ctx.getResources().getColor(R.color.me_chat_bg));
                break;
			case Item.toSummary:
				holder.lyt_parent.setPadding(15, 0, 100, 0);
				holder.lyt_parent.setGravity(Gravity.LEFT);
				holder.lyt_thread.setCardBackgroundColor(ctx.getResources().getColor(R.color.to_chat_bg));
				break;
			case Item.toSummAndTerm:
				holder.lyt_parent.setPadding(15, 0, 100, 0);
				holder.lyt_parent.setGravity(Gravity.LEFT);
				holder.lyt_thread.setCardBackgroundColor(ctx.getResources().getColor(R.color.to_chat_bg));
				break;
			case Item.infoSummary:
				//holder.lyt_parent.setPadding(15, 10, 100, 10);
				//holder.lyt_parent.setGravity(Gravity.LEFT);
				//holder.lyt_thread.setCardBackgroundColor(ctx.getResources().getColor(R.color.info_chat_bl));
				holder.lyt_parent.setPadding(100, 10, 100, 10);
				holder.lyt_parent.setGravity(Gravity.CENTER_HORIZONTAL);
				holder.lyt_thread.setCardBackgroundColor(ctx.getResources().getColor(R.color.info_chat_bl));
				break;
			case Item.infoSummAndTerm:
                holder.lyt_parent.setPadding(100, 10, 100, 10);
                holder.lyt_parent.setGravity(Gravity.CENTER_HORIZONTAL);
                holder.lyt_thread.setCardBackgroundColor(ctx.getResources().getColor(R.color.info_chat_bl));
				break;
			default:
				holder.lyt_parent.setPadding(100, 0, 15, 0);
				holder.lyt_parent.setGravity(Gravity.RIGHT);
				holder.lyt_thread.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
				//holder.image_status.setImageResource(android.R.color.transparent);
				break;
		}


        return convertView;
	}

	/**
	 * remove data item from messageAdapter
	 * 
	 **/
	public void remove(int position){
		mMessages.remove(position);
	}



	
	/**
	 * add data item to messageAdapter
	 * 
	 **/
	public void add(Item msg){
		mMessages.add(msg);
	}
	
	private static class ViewHolder{
		TextView header;
		TextView time;
		TextView message;
		LinearLayout lyt_parent;
		CardView lyt_thread;
		ImageView image_status;
	}	
}
