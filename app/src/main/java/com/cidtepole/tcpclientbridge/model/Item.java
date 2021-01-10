package com.cidtepole.tcpclientbridge.model;

import android.os.Parcel;
import android.os.Parcelable;


public class Item implements Parcelable {
	private long id;
	private String encabezado;
	private String contenido;
	private String fecha;
	private int display;   // Selecciona el fragment en donde se despliega el mensaje y la colocacion

	public static final String ITEM = "Item";
	public static final String ENCABEZADO= "encabezado";
	public static final String CONTENIDO = "conetenido";


	public static final int infoSummAndTerm = 0;
	public static final int infoSummary = 1;
	public static final int infoTerminal = 2;
	public static final int fromSummary= 3;
	public static final int fromTerminal = 4;
	public static final int toSummAndTerm = 5;
	public static final int toTerminal= 6;
	public static final int toSummary= 7;



	public Item(long id, String encabezado, String contenido, String fecha, int display) {
		this.id = id;
		this.encabezado = encabezado;
		this.fecha = fecha;
		this.contenido = contenido;
		this.display = display;

	}

	public long getId() {
		return id;
	}

	public String getEncabezado() { return encabezado; }

	public String getFecha() {
		return fecha;
	}

	public String getContenido() { return contenido; }

	public void setId(long id) {
		this.id = id;
	}

	public void setEncabezado(String encabezado) { this.encabezado = encabezado; }


	public void setFecha(String fecha) { this.fecha = fecha; }

	public void setContenido(String contenido) { this.contenido = contenido; }

	public int getDisplay() {	return display; }

	public void setDisplay(int tipo) { this.display = display; }


	protected Item(Parcel in) {
		id=in.readLong();
		fecha=in.readString();
		contenido = in.readString();
		display = in.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(fecha);
		dest.writeString(contenido);

	}

	@SuppressWarnings("unused")
	public static final Creator<Item> CREATOR = new Creator<Item>() {
		@Override
		public Item createFromParcel(Parcel in) {
			return new Item(in);
		}

		@Override
		public Item[] newArray(int size) {
			return new Item[size];
		}
	};
}