package com.cidtepole.tcpclientbridge.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;


public class Server implements  Parcelable {

	private long id;
	private String name;
	private String ip;
	private int port;

	public static final String SERVER = "server";

	public Server(long id, String name, String ip, int port) {
		this.id = id;
		this.name = name;
		this.ip = ip;
		this.port = port;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getIp() {
		return ip;
	}


	public void setPort(int port) {
		this.port = port;
	}

	public int getPort() {
		return port;
	}



	protected Server(Parcel in) {
		id=in.readLong();
		name= in.readString();
		ip = in.readString();
		port = in.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(name);
		dest.writeString(ip);
		dest.writeInt(port);
	}

	@SuppressWarnings("unused")
	public static final Creator<Server> CREATOR = new Creator<Server>() {
		@Override
		public Server createFromParcel(Parcel in) {
			return new Server(in);
		}

		@Override
		public Server[] newArray(int size) {
			return new Server[size];
		}
	};

}