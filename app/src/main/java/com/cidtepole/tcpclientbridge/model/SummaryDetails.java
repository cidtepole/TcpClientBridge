package com.cidtepole.serverbridgex.model;

import java.io.Serializable;

public class SummaryDetails implements Serializable{
	private long id;
	private String header;
	private String date;
	private String content;
	private int display;

	public SummaryDetails(long id, String header, String content, String date, int display) {
		this.id = id;
		this.header = header;
		this.date = date;
		this.content = content;
		this.display = display;
	}

	public long getId() {
		return id;
	}

	public String getDate() {
		return date;
	}

	public String getHeader() {
		return header;
	}

	public String getContent() {
		return content;
	}

	public int getDisplay() {return display;}

}