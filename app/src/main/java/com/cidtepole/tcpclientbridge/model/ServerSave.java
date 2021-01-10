package com.cidtepole.tcpclientbridge.model;




import java.io.Serializable;


public class ServerSave implements Serializable  {

    private long id;
    private String name;
    private String ip;
    private int port;

    public static final String SERVER = "server";

    public ServerSave(long id, String name, String ip, int port) {
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

}