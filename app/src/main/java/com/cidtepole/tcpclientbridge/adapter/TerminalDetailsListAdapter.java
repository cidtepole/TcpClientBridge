package com.cidtepole.tcpclientbridge.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import java.util.List;

import com.cidtepole.tcpclientbridge.data.Clock;
import com.cidtepole.tcpclientbridge.model.Item;
import com.cidtepole.tcpclientbridge.R;

public class TerminalDetailsListAdapter extends BaseAdapter {

	private List<Item> mMessages;
	private Context ctx;
	private int textSize = 18;

	public TerminalDetailsListAdapter(Context context, List<Item> messages) {
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
		TerminalDetailsListAdapter.ViewHolder holder;
		if(convertView == null){
			holder 				= new TerminalDetailsListAdapter.ViewHolder();
			convertView			= LayoutInflater.from(ctx).inflate(R.layout.row_terminal_details, parent, false);
			holder.header 		= (TextView) convertView.findViewById(R.id.header_term);
			holder.message 		= (TextView) convertView.findViewById(R.id.text_content_term);
			convertView.setTag(holder);
		}else{
			holder = (TerminalDetailsListAdapter.ViewHolder) convertView.getTag();
		}

		holder.header.setTextSize(textSize);
		holder.message.setTextSize(textSize);
		holder.header.setText(msg.getEncabezado());
		//holder.message.setText(msg.getContenido());




		switch (msg.getDisplay()){
			case Item.fromSummary:
				holder.message.setTextColor(ContextCompat.getColor(ctx, R.color.blue_500));
				holder.message.setText(msg.getContenido());
				break;
			case Item.toTerminal:
				holder.message.setTextColor(ContextCompat.getColor(ctx, R.color.blue_500));
				holder.header.setText(Clock.getTime());
				holder.message.setText(msg.getContenido());
				break;
            case Item.fromTerminal:
                holder.message.setTextColor(ContextCompat.getColor(ctx, R.color.green_500));
                holder.message.setText(msg.getContenido());
                break;
			case Item.toSummary:
				holder.message.setTextColor(ContextCompat.getColor(ctx, R.color.green_500));
				holder.message.setText(msg.getContenido());
				break;
			case Item.infoSummary:
				holder.message.setTextColor(ContextCompat.getColor(ctx, R.color.orange_500));
				holder.message.setText(msg.getContenido());
				break;
			case Item.toSummAndTerm:
				holder.message.setTextColor(ContextCompat.getColor(ctx, R.color.green_500));
				holder.header.setText(Clock.getTime());
				holder.message.setText(msg.getContenido());
				break;
			case Item.infoSummAndTerm:
				holder.message.setTextColor(ContextCompat.getColor(ctx, R.color.orange_500));
				holder.message.setText(msg.getContenido());
				break;
			default:
				holder.message.setTextColor(ContextCompat.getColor(ctx, R.color.orange_500));
				holder.message.setText(msg.getContenido());
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

	public void setTextSize(int size){	textSize = size; }

	private static class ViewHolder{
		TextView header;
		TextView message;
	}
}
