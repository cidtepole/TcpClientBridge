package com.cidtepole.tcpclientbridge.Manager;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cidtepole.tcpclientbridge.MainService;
import com.cidtepole.tcpclientbridge.MainService;
import com.cidtepole.tcpclientbridge.data.Tools;
import com.cidtepole.tcpclientbridge.fragment.SummaryFragment;
import com.cidtepole.tcpclientbridge.model.Item;
import com.cidtepole.tcpclientbridge.model.Mensaje;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientManager  implements Runnable{


    private String nombreCliente;
    private Socket cliente;

    private Thread hilo;
    private boolean conectado = false;
    protected boolean disableBridge = false;

    private int puerto;
    private InputStream entrada;
    //private PrintWriter salida;
    private OutputStream salida;
    private String identificador, nombre, ID;
    private Handler handler;
    protected Thread runningThread= null;
    protected Context context;

    private  byte[] lenBytes = new byte[256];
    private String received=null;
    int length=0;

    private boolean disponible = true; //se usa para sincronizar los mensajes salientes
    private boolean disponibleE = true; //se usa para sincronizar los mensajes entrantes

    public ClientManager(Handler handler, Context context) {

        this.handler = handler;
        this.context= context;

    }


    public void conectar(String direccion, int puerto){

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    cliente = new Socket(direccion, puerto);
                    entrada = cliente.getInputStream();
                    //salida = new PrintWriter(new OutputStreamWriter(cliente.getOutputStream()), true);
                    salida = cliente.getOutputStream();
                    conectado = true;

                    Message msg = handler.obtainMessage(MainService.MESSAGE_CONNECTED);
                    Bundle bundle= new Bundle();
                    bundle.putString("info", "Conectado al servidor.");
                    msg.setData(bundle);
                    handler.sendMessage(msg);


                } catch (SocketTimeoutException ex) {
                    //Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                    Message msg = handler.obtainMessage(MainService.MESSAGE_CONNECTION_FAILURE);
                    Bundle bundle= new Bundle();
                    bundle.putString("info", ex.toString());
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    conectado = false;
                } catch (IOException ex) {
                    //Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                    Message msg = handler.obtainMessage(MainService.MESSAGE_CONNECTION_FAILURE);
                    Bundle bundle= new Bundle();
                    bundle.putString("info", ex.toString());
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    conectado = false;
                }

            }
        });



    }


    public void desconectar(){
        /*
        Message msg = handler.obtainMessage(MainService.MESSAGE_DISCONNECTED);
        Bundle bundle= new Bundle();
        bundle.putString("info", "Desonectado al servidor.");
        msg.setData(bundle);
        handler.sendMessage(msg);
        finalize();
        */

        this.conectado = false;
        try {
            this.cliente.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing cliente", e);
        }


    }

    private synchronized boolean getConectado(){
        return conectado;
    }

    public void setConectado(){
        this.conectado = false;
    }

    public void manage(){
        hilo = new Thread(this);
        hilo.start();
    }

    public  boolean isDisableBridge()
    {
        return disableBridge;
    }

    public  void setDisableBridge(boolean disableBridge)
    {
        this.disableBridge = disableBridge;
    }


    public synchronized void  enviarMensaje(Mensaje mensaje) {

        //final String msn = mensaje.getMensaje();
        //final Mensaje message = mensaje;




            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {

                        if(SummaryFragment.is_ascii_format){  // Envio mensaje;
                            enviarDatos(mensaje.getMensaje());
                        }else{
                            //Log.i("info", "data: " + msn.toUpperCase().replaceAll(" ", ""));
                           //enviarDatos(Tools.decodeHexString(msn.toUpperCase().replaceAll(" ", "")));

                        }


                    } catch (Exception e) {
                        Log.e("Error", "Exception: " + e.getMessage());
                    }

                    if (handler != null){
                        Bundle bundle = new Bundle();
                        String contenido = new String(mensaje.getMensaje());
                        //bundle.putString("encabezado", mensaje.getRemitente() + " send to " + mensaje.getDestinatario());   //Componer el encabezado ??????????????????????????????????????????????'
                        bundle.putString(Item.ENCABEZADO,  "Send to " + mensaje.getDestinatario());
                        bundle.putString(Item.CONTENIDO, contenido);

                        Message msgTx = handler.obtainMessage(MainService.MESSAGE_SENT_TO_SERVER);
                        msgTx.setData(bundle);
                        handler.sendMessage(msgTx);
                    }
                }

            });




            return;
        }



    public synchronized void enviarDatos(byte[] data){
        //aqui sincronizo el acceso a este metodo
        while(!disponible){
            try {
                wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //defino el metodo como ocupado y notifico a todos los sub procesos
        disponible = false;
        notify();

        //Log.i("INFO", "EnivarDatos " + datos);
        //try {
        //aqui envio el mensaje al cliente
        try {
            salida.write(data);
            salida.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //} catch (IOException ex) {
        //Logger.getLogger(ManejadorCliente.class.getName()).log(Level.SEVERE, null, ex);
        //Log.i("INFO", "Ha ocurrido un error al enviar los datos.");
        //Log.i("INFO", "Los datos se descartaran.");
        //}

        //defino el metodo como desocupado y notifico a todos los subprocesos
        disponible = true;
        notify();
    }








    public void run() {

        synchronized(this){
            this.runningThread = Thread.currentThread();
        }

        while(conectado){
            try {

                length = entrada.read(lenBytes,0,256);

                //received = new String(lenBytes,0, length,"UTF-8");
                Log.i("INFO", "Recividos:"+ Tools.ByteArrayToHexString(lenBytes));
                //aqui gestiono el mensaje
                if(length > 0)
                    gestionarMensaje(length,lenBytes);


            }catch(StringIndexOutOfBoundsException  e) {
                Log.i("INFO", "IOException ex");

                    Log.i("INFO", "IOException ex");
                    Message msg = handler.obtainMessage(MainService.MESSAGE_DISCONNECTED);
                    handler.sendMessage(msg);
                    e.printStackTrace();
                    desconectar();
                    finalize();
                    break;



            } catch (IOException ex) {
                //Logger.getLogger(ManejadorCliente.class.getName()).log(Level.SEVERE, null, ex);

                Log.i("INFO", "IOException ex");
                Message msg = handler.obtainMessage(MainService.MESSAGE_DISCONNECTED);
                handler.sendMessage(msg);
                ex.printStackTrace();
                desconectar();
                finalize();
                break;

            } catch (NullPointerException ex) {
                //Logger.getLogger(ManejadorCliente.class.getName()).log(Level.SEVERE, null, ex);
                Log.i("INFO", "NullPointerException ex");
                desconectar();
                finalize();
                break;
            }

        }

    }



    private  synchronized void gestionarMensaje(int length, byte[] data) {

        //aqui sincronizo el acceso a este metodo
        while(!disponibleE){
            try {
                wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //defino el metodo como ocupado y notifico a todos los sub procesos
        disponibleE = false;
        notify();


            Mensaje mensaje_to_USB = null;

            String destinatario = Mensaje.TO_USB_AND_SERVER;

            if(this.isDisableBridge())
                mensaje_to_USB = new Mensaje(this.nombre,Mensaje.ONLY_SERVER,  data);
            else
                mensaje_to_USB = new Mensaje(this.nombre,destinatario,  data);

            Bundle bundle = new Bundle();
            bundle.putParcelable(Mensaje.MENSAJE, mensaje_to_USB);
            Message msg = Message.obtain(null, MainService.MESSAGE_TO_USB, 0, 0);
            msg.setData(bundle);
            handler.sendMessage(msg);



        //defino el metodo como desocupado y notifico a todos los subprocesos
        disponibleE = true;
        notify();


    }


    @Override
    public void finalize(){

        try {
            entrada.close();
            salida.close();
        } catch (IOException ex) {
            //Logger.getLogger(Orquestador.class.getName()).log(Level.SEVERE, null, ex);
            entrada = null;
            salida = null;
            cliente = null;
        }
        conectado = false;
        hilo = null;
    }


    /**
     * Get ip address of the device
     */
    public static String getDeviceIpAddress() {
        try {
            //Loop through all the network interface devices
            for (Enumeration<NetworkInterface> enumeration = NetworkInterface
                    .getNetworkInterfaces(); enumeration.hasMoreElements();) {
                NetworkInterface networkInterface = enumeration.nextElement();
                //Loop through all the ip addresses of the network interface devices
                for (Enumeration<InetAddress> enumerationIpAddr = networkInterface.getInetAddresses(); enumerationIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumerationIpAddr.nextElement();
                    //Filter out loopback address and other irrelevant ip addresses
                    if (!inetAddress.isLoopbackAddress() && inetAddress.getAddress().length == 4) {
                        //Print the device ip address in to the text view
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            Log.e("ERROR:", e.toString());
        }
        return "";

    }


}
