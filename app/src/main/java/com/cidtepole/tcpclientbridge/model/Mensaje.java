package com.cidtepole.tcpclientbridge.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Mensaje implements Parcelable {

    private String destinatario;
    private String remitente;
    private byte[] mensaje;
    private String formatType;


    public static final String MENSAJE = "Mensaje";
    public static final String TO_USB_AND_SERVER = "To_usb";
    public static final String ONLY_SERVER = "Only_server";
    public static final String ASCII = "ascii";
    public static final String HEX = "hex";

    public Mensaje(String remitente, String destinatario,  byte[] mensaje) {
        this.remitente = remitente;
        this.destinatario = destinatario;
        this.mensaje = mensaje;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public String getRemitente() {
        return remitente;
    }



    public byte[] getMensaje() { return mensaje; }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public void setRemitente(String remitente){
        this.remitente = remitente;
    }

    public void setMensaje(byte[] mensaje){
        this.mensaje = mensaje;
    }





    protected Mensaje(Parcel in) {
        remitente=in.readString();
        destinatario=in.readString();

        mensaje = new byte[in.readInt()];
        in.readByteArray(mensaje);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(remitente);
        dest.writeString(destinatario);

        dest.writeInt(mensaje.length);
        dest.writeByteArray(mensaje);
        dest.writeString(formatType);
    }

    @SuppressWarnings("unused")
    public static final Creator<Mensaje> CREATOR = new Creator<Mensaje>() {
        @Override
        public Mensaje createFromParcel(Parcel in) {
            return new Mensaje(in);
        }

        @Override
        public Mensaje[] newArray(int size) {
            return new Mensaje[size];
        }
    };

}
