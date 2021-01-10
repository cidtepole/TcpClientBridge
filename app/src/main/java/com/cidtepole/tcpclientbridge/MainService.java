package com.cidtepole.tcpclientbridge;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.NotificationChannel;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


import com.cidtepole.tcpclientbridge.Manager.ClientManager;
import com.cidtepole.tcpclientbridge.data.Clock;
import com.cidtepole.tcpclientbridge.model.Item;
import com.cidtepole.tcpclientbridge.model.Mensaje;
import com.felhr.usbserial.CDCSerialDevice;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.felhr.utils.ProtocolBuffer;
//import com.felhr.utils.ProtocolBuffer;


public class MainService extends Service {

    public static final int MESSAGE_FROM_SERIAL_PORT = 0;
    public static final int MESSAGE_TO_USB= 1;
    public static final int MESSAGE_CONNECT = 2;
    public static final int MESSAGE_DISCONNECT = 3;
    public static final int MESSAGE_SERVER_STOP = 4;
    public static final int MESSAGE_CONNECTED= 5;
    public static final int MESSAGE_DISCONNECTED =6;
    public static final int MESSAGE_CONNECTION_FAILURE =7;
    public static final int MESSAGE_SET_NAME = 8;
    public static final int MESSAGE_SET_OPTIONS_CLIENT = 9;
    public static final int MESSAGE_CLIENT_TO = 10;
    public static final int MESSAGE_BUSY_PORT = 11;
    public static final int MESSAGE_TO_SERVER = 12;
    public static final int MESSAGE_SENT_TO_SERVER =14;
    public static final int MESSAGE_DISCONNECT_CLIENT = 15;
    public static final int MESSAGE_BUTTON_STOP=15;
    public static final int CTS_CHANGE = 16;
    public static final int DSR_CHANGE = 17;

    //public static final int MESSAGE_ALARM=18;


    public static final String ACTION_USB_READY = "com.felhr.connectivityservices.USB_READY";
    public static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    public static final String ACTION_USB_NOT_SUPPORTED = "com.felhr.usbservice.USB_NOT_SUPPORTED";
    public static final String ACTION_NO_USB = "com.felhr.usbservice.NO_USB";
    public static final String ACTION_USB_PERMISSION_GRANTED = "com.felhr.usbservice.USB_PERMISSION_GRANTED";
    public static final String ACTION_USB_PERMISSION_NOT_GRANTED = "com.felhr.usbservice.USB_PERMISSION_NOT_GRANTED";
    public static final String ACTION_USB_DISCONNECTED = "com.felhr.usbservice.USB_DISCONNECTED";
    public static final String ACTION_CDC_DRIVER_NOT_WORKING = "com.felhr.connectivityservices.ACTION_CDC_DRIVER_NOT_WORKING";
    public static final String ACTION_USB_DEVICE_NOT_WORKING = "com.felhr.connectivityservices.ACTION_USB_DEVICE_NOT_WORKING";
    public static final String ACTION_RELAUNCH_SERVER ="com.cidtepole.servidorandroid.RELAUNCH_SERVER";

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    public static final String NOTIFICATION_CHANNEL_ID_SERVICE = "de.appplant.cordova.plugin.background";
    public static final String NOTIFICATION_CHANNEL_ID_INFO = "com.package.download_info";

    public static final String IP = "IP";
    public static final String PORT = "puerto";

    // Parametros de configuracion UART
    private int BAUD_RATE;
    private int DATA_BITS;
    private int PARITY;
    private int STOP_BITS;

    private UsbManager usbManager;
    private UsbDevice device;
    private UsbDeviceConnection connection;
    private UsbSerialDevice serialPort;
    private Context context;

    public static final int CONNECT = 20;
    public static final int DISCONNECT = 21;
    public static boolean SERVICE_CONNECTED = false;
    public static boolean CLIENT_CONNECTED =false;
    public static  boolean SERIAL_PORT_CONNECTED = false;


    //Se usa para recibir los mensajes de la Activity
    final Messenger inMessenger = new Messenger(new  IncomingHandlerActivity());
    //Se usa para enviar mensajes a la Activity
    private Messenger outMessenger=null;
    private int SERVER_PORT=5000;
    private Thread hilo=null;
    ClientManager clientManager=null;

    private static final int ONGOING_NOTIFICATION_ID = 1;


    ProtocolBuffer buffer = new ProtocolBuffer(ProtocolBuffer.BINARY); //Also Binary











    //Se usa para recibir los mensajes del ClientManager
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MESSAGE_CONNECTED:
                    try {

                        showForegroundNotification("Conectado");
                        clientManager.manage();
                        CLIENT_CONNECTED = true;

                        Bundle bundleRx = msg.getData();
                        String info = bundleRx.getString("info");
                        Item item = new Item(0, "Info:", info,
                                Clock.getNow(), Item.infoSummAndTerm);
                        Bundle bundleTx = new Bundle();
                        bundleTx.putParcelable(Item.ITEM, item);
                        Message msgTx = Message.obtain(null, MainService.MESSAGE_CONNECTED, 0, 0);
                        msgTx.setData(bundleTx);
                        if(outMessenger!=null)
                            outMessenger.send(msgTx);

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    //mActivity.get().display.append(data);
                    break;

                case MESSAGE_DISCONNECTED:
                    try {
                        //No se recibe ningún mensaje, unicamente sirve como bandera e informamos al Activity que el servidor se detuvo.
                        Item item = new Item(0, "Info:", "Desconectado.",
                                Clock.getNow(), Item.infoSummAndTerm);
                        Bundle bundleTx = new Bundle();
                        bundleTx.putParcelable(Item.ITEM, item);
                        Message msgTx = Message.obtain(null, MainService.MESSAGE_DISCONNECTED, 0, 0);
                        msgTx.setData(bundleTx);
                        if(outMessenger!=null)
                            outMessenger.send(msgTx);
                        CLIENT_CONNECTED = false;
                        stopForeground(true);
                        //write(datos.getBytes());//Enviar datos al dispositivo usb conectado
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    //mActivity.get().display.append(data);
                    break;


                case MESSAGE_CONNECTION_FAILURE:
                    try {
                        Bundle bundleRx = msg.getData();
                        String info = bundleRx.getString("info");
                        Item item = new Item(0, "Info:", "Host not found.",
                                Clock.getNow(), Item.infoSummAndTerm);
                        Bundle bundleTx = new Bundle();
                        bundleTx.putParcelable(Item.ITEM, item);
                        Message msgTx = Message.obtain();
                        msgTx.setData(bundleTx);
                        if(outMessenger!=null)
                            outMessenger.send(msgTx);
                        CLIENT_CONNECTED = false;
                        stopForeground(true);
                        //write(datos.getBytes());//Enviar datos al dispositivo usb conectado
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    //mActivity.get().display.append(data);
                    break;


                case MESSAGE_TO_USB:

                    Bundle bundleRx = msg.getData();
                    Mensaje mensaje_to_USB =  bundleRx.getParcelable(Mensaje.MENSAJE);

                    try {
                        String remitente = mensaje_to_USB.getRemitente();
                        String destinatario = mensaje_to_USB.getDestinatario();
                        String contenido = new String(mensaje_to_USB.getMensaje());

                        Item item = null;

                        if(serialPortConnected && destinatario.equals(Mensaje.TO_USB_AND_SERVER)){ //Si esta conectado un dispositivo USB-Serial, enviar los datos
                            //item = new Item(0,remitente+":", contenido, Clock.getNow(), Item.toSummAndTerm);
                            item = new Item(0,remitente+":", contenido, Clock.getNow(), Item.toSummAndTerm);
                            write(mensaje_to_USB.getMensaje());//Enviar datos al dispositivo usb conectado
                        }
                        else {
                            item = new Item(0, remitente + ":", contenido, Clock.getNow(), Item.toSummary);
                        }

                        Message msgt = Message.obtain();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(Item.ITEM, item);
                        msgt.setData(bundle);
                        if(outMessenger!=null)
                            outMessenger.send(msgt);

                    }catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;

                case MESSAGE_SENT_TO_SERVER:

                    Log.d("I", "MESSAGE_SENT_TO_SERVER");
                    try {
                        Bundle bundleRx1 = msg.getData();
                        String encabezado = bundleRx1.getString(Item.ENCABEZADO);
                        String contenido = bundleRx1.getString(Item.CONTENIDO);

                        Log.d("I", "MESSAGE_SENT_TO_SERVER");

                        Item item = new Item(0, encabezado,  contenido, Clock.getNow(), Item.fromSummary);

                        Message msgt = Message.obtain();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(Item.ITEM, item);
                        msgt.setData(bundle);
                        if(outMessenger!=null)
                            outMessenger.send(msgt);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;


                case MainService.MESSAGE_SET_OPTIONS_CLIENT :
                   
                    break;


                //case MESSAGE_ALARM:
                //  soundAcc.start();
                //Toast.makeText(mActivity.get(), "CTS_CHANGE",Toast.LENGTH_LONG).show();
                //break;

                case CTS_CHANGE:
                    //Toast.makeText(mActivity.get(), "CTS_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case DSR_CHANGE:
                    //Toast.makeText(mActivity.get(), "DSR_CHANGE",Toast.LENGTH_LONG).show();
                    break;
            }


        }
    };



    //Se usa para recibir los mensajes de la Activity
    private class IncomingHandlerActivity extends Handler {
        @Override
        public void handleMessage(Message msg) {

            Bundle bundleRx = msg.getData();

            switch (msg.what) {
                case MESSAGE_CONNECT:
                    Log.d("I", "CONNECT");

                   String ip = bundleRx.getString(IP);
                   int puerto = bundleRx.getInt(PORT);
                    conectar(ip, puerto);
                    break;

                case MESSAGE_DISCONNECT:
                    Log.i("INFO","DETENER SERVIDOR");
                    desconectar();
                    break;

                case MESSAGE_SET_OPTIONS_CLIENT:
                    //Cliente c = msg.getData().getParcelable(Cliente.CLIENTE);
                    //Log.i("INFO","SET_OPTIONS_CLIENT " + c.getNombre());
                    //servidor.setOptionsClient(c);
                    break;

                case MESSAGE_TO_USB:
                    //Item item = bundle.getParcelable("Item");
                    if(serialPortConnected) {

                        Mensaje mensaje_to_USB = bundleRx.getParcelable(Mensaje.MENSAJE);

                        write(mensaje_to_USB.getMensaje());//Enviar datos al dispositivo usb conectado

                        try {
                            String encabezado = "";
                            String contenido = new String(mensaje_to_USB.getMensaje());
                            Item item = new Item(0, encabezado, contenido, Clock.getNow(), Item.fromTerminal);

                            Message msgt = Message.obtain();
                            Bundle bundleTx = new Bundle();
                            bundleTx.putParcelable(Item.ITEM, item);
                            msgt.setData(bundleTx);
                            if(outMessenger!=null)
                                outMessenger.send(msgt);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    break;

                case MESSAGE_TO_SERVER:
                    Mensaje mensaje_to_Server = bundleRx.getParcelable(Mensaje.MENSAJE);
                    clientManager.enviarMensaje(mensaje_to_Server);
                    break;

                case MESSAGE_DISCONNECT_CLIENT:
                    //Cliente C = bundleRx.getParcelable(Cliente.CLIENTE);
                    //servidor.desconectarCliente(C);
                    break;



                default:


                    //Toast.makeText(mActivity.get(), "DSR_CHANGE",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }


    public void conectar(String IP, int puerto){

        //showForegroundNotification("Conectado");
        clientManager = new ClientManager(handler, context);
        clientManager.conectar(IP, puerto);

        //else{
           // clientManager.desconectar();
        //}


    }

    public void desconectar(){

        if(CLIENT_CONNECTED) {
            clientManager.desconectar();
            Log.i("INFO","DETENER Client");
        }

    }



    @Override
    public void onCreate(){

        this.context = this;
        serialPortConnected = false;
        SERIAL_PORT_CONNECTED = false;
        MainService.SERVICE_CONNECTED = true;
        setFilter();
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        findSerialPortDevice();
        //serialSettings = new ConfiguracionSerial(9600, UsbSerialInterface.DATA_BITS_8, UsbSerialInterface.PARITY_NONE, UsbSerialInterface.STOP_BITS_1);

        BAUD_RATE = 9600;
        DATA_BITS = UsbSerialInterface.DATA_BITS_8;
        PARITY = UsbSerialInterface.PARITY_NONE;
        STOP_BITS =  UsbSerialInterface.STOP_BITS_1;

        buffer.setDelimiter("\r\n");


        Log.i("INFO", "DATA_BITS_8 " + UsbSerialInterface.DATA_BITS_8);
        Log.i("INFO", "PARITY_NONE " + UsbSerialInterface.PARITY_NONE);
        Log.i("INFO", "STOP_BITS_1 " + UsbSerialInterface.STOP_BITS_1);



        SharedPreferences pref = context.getSharedPreferences("MisPreferencias", context.MODE_PRIVATE);
        Log.d("I", Integer.toString(pref.getInt("lanzador",1)));
        if(pref.getInt("lanzador",1)==2) {

            Log.d("I", "START_SERVER");
            SERVER_PORT = Integer.parseInt(pref.getString("port","1234"));
            //SERVER_PORT=1234;
            //lanzador = bundle.getInt(MainActivity.EXTRA_PORT);
            //iniciar_servidor();

        }




        //Notification notification = new Notification();
        //startForeground(ONGOING_NOTIFICATION_ID, notification);
        //startForeground();
        //showForegroundNotification("");

    }





    private void showForegroundNotification(String contentText) {
        // Create intent that will bring our app to the front, as if it was tapped in the app
        // launcher
        Intent showTaskIntent = new Intent(getApplicationContext(), MainActivity.class);
        showTaskIntent.setAction(Intent.ACTION_MAIN);
        showTaskIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        showTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                showTaskIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(contentText)
                    .setSmallIcon(R.mipmap.ic_launcher)/////////////////////////////////////////////////77cambiar icono
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(contentIntent)
                    .build();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(ONGOING_NOTIFICATION_ID, notification);



    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){

        Intent showTaskIntent = new Intent(getApplicationContext(), MainActivity.class);
        showTaskIntent.setAction(Intent.ACTION_MAIN);
        showTaskIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        showTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                showTaskIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(contentIntent)
                .build();
        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }


    public void setInicia(Context ctx, int mode){
        try {
            SharedPreferences prefs= getSharedPreferences("MisPreferencias", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("lanzador", mode);
            editor.apply();
            //Long.i("MoveMore", "Saving readings to preferences");
        } catch (NullPointerException e) {
            //Log.e(TAG, "error saving: are you testing?" +e.getMessage());
            //ServerOperations.database.addError(e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setInicia(this, 1);
        unregisterReceiver(usbReceiver);
        SERVICE_CONNECTED = false;
        desconectar();
        //Intent toSend = new Intent(ACTION_RELAUNCH_SERVER);
        //sendBroadcast(toSend);

        stopForeground(true);
        //Log.i("INFO", "onDestroy()");


        Log.i("EXIT", "ondestroy!");
    }

    public static boolean isRunning(){

        return SERVICE_CONNECTED;
    }

    public MainService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("I", "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }



    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        if(outMessenger == null)
            outMessenger = intent.getParcelableExtra("Messenger");//Mensajero para enviar datos al Activity
        Log.d("I", "onBind");

        if (CLIENT_CONNECTED){
            Message msg = Message.obtain(null, MESSAGE_BUTTON_STOP);
            try {
                outMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return inMessenger.getBinder();//Mensajero para recibir datos del Activity

    }


    private boolean serialPortConnected;
    /*
     *  Data received from serial port will be received here. Just populate onReceivedData with your code
     *  In this particular example. byte stream is converted to String and send to UI thread to
     *  be treated there.
     */


    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] arg0) {

            Mensaje mensaje = new Mensaje("Serial device", MainActivity.BROADCAST,"".getBytes());

            buffer.appendData(arg0);
            while (buffer.hasMoreCommands()) {
                byte[] data = buffer.nextBinaryCommand();
                mensaje.setMensaje(data);
                // Do your thing with textCommand
            }



            if (clientManager != null && !mensaje.getMensaje().equals("")) {

                clientManager.enviarMensaje(mensaje);
            }

            if (!mensaje.getMensaje().equals("")) {
                Bundle bundleTx = new Bundle();

                Item itm = new Item(0, "", new String(mensaje.getMensaje()), Clock.getNow(), Item.toTerminal);
                bundleTx.putParcelable(Item.ITEM, itm);
                Message msgt = Message.obtain();
                msgt.setData(bundleTx);
                try {
                    outMessenger.send(msgt);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        }
    };

    /*
     * State changes in the CTS line will be received here
     */
    private UsbSerialInterface.UsbCTSCallback ctsCallback = new UsbSerialInterface.UsbCTSCallback() {
        @Override
        public void onCTSChanged(boolean state) {

        }
    };

    /*
     * State changes in the DSR line will be received here
     */
    private UsbSerialInterface.UsbDSRCallback dsrCallback = new UsbSerialInterface.UsbDSRCallback() {
        @Override
        public void onDSRChanged(boolean state) {
            //if(mHandler != null)
            //  mHandler.obtainMessage(DSR_CHANGE).sendToTarget();
        }
    };

    /*
     * Different notifications from OS will be received here (USB attached, detached, permission responses...)
     * About BroadcastReceiver: http://developer.android.com/reference/android/content/BroadcastReceiver.html
     */
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (arg1.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = arg1.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) // User accepted our USB connection. Try to open the device as a serial port
                {
                    Intent intent = new Intent(ACTION_USB_PERMISSION_GRANTED);
                    arg0.sendBroadcast(intent);
                    connection = usbManager.openDevice(device);
                    new ConnectionThread().start();
                } else // User not accepted our USB connection. Send an Intent to the Main Activity
                {
                    Intent intent = new Intent(ACTION_USB_PERMISSION_NOT_GRANTED);
                    arg0.sendBroadcast(intent);
                }
            } else if (arg1.getAction().equals(ACTION_USB_ATTACHED)) {
                if (!serialPortConnected)
                    findSerialPortDevice(); // A USB device has been attached. Try to open it as a Serial port
            } else if (arg1.getAction().equals(ACTION_USB_DETACHED)) {
                // Usb device was disconnected. send an intent to the Main Activity
                Intent intent = new Intent(ACTION_USB_DISCONNECTED);
                arg0.sendBroadcast(intent);
                if (serialPortConnected) {
                    serialPort.close();
                }
                serialPortConnected = false;
                SERIAL_PORT_CONNECTED = false;
            }
        }
    };



    /*
     * Esta funcion sera llamada para escribir datos a traves del puerto serie.
     */
    public void write(byte[] data) {
        if (serialPort != null)
            serialPort.write(data);
    }



    private void findSerialPortDevice() {
        // This snippet will try to open the first encountered usb device connected, excluding usb root hubs
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                int devicePID = device.getProductId();

                if (deviceVID != 0x1d6b && (devicePID != 0x0001 && devicePID != 0x0002 && devicePID != 0x0003)) {
                    // There is a device connected to our Android device. Try to open it as a Serial Port.
                    requestUserPermission();
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }

                if (!keep)
                    break;
            }
            if (!keep) {
                // There is no USB devices connected (but usb host were listed). Send an intent to MainActivity.
                Intent intent = new Intent(ACTION_NO_USB);
                context.sendBroadcast(intent);
            }
        } else {
            // There is no USB devices connected. Send an intent to MainActivity
            Intent intent = new Intent(ACTION_NO_USB);
            context.sendBroadcast(intent);
        }
    }



    private void setFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_DETACHED);
        filter.addAction(ACTION_USB_ATTACHED);
        context.registerReceiver(usbReceiver, filter);
    }

    /*
     * Requiere permiso del usuario. La respuesta debera ser recivida en el BroadcastReceiver
     */
    private void requestUserPermission() {
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        usbManager.requestPermission(device, mPendingIntent);
    }



    /*
     * Un simple hilo para abrir un puerto serie.
     *
     */
    private class ConnectionThread extends Thread {
        @Override
        public void run() {

            serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
            if (serialPort != null) {
                if (serialPort.open()) {


                    serialPortConnected = true;
                    SERIAL_PORT_CONNECTED = true;

                    SharedPreferences prefs =
                            getSharedPreferences("MisPreferencias",Context.MODE_PRIVATE);     // 16 de enero 2017
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("isEnable", false);  // deshabilitamos el menu Settings
                    editor.commit();

                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
                    BAUD_RATE = Integer.parseInt(pref.getString("baudRate","9600"));
                    DATA_BITS = Integer.parseInt(pref.getString("dataBits","8"));
                    PARITY = Integer.parseInt(pref.getString("parity","0"));
                    STOP_BITS = Integer.parseInt(pref.getString("stopBits","1"));

                    serialPort.setBaudRate(BAUD_RATE);
                    serialPort.setDataBits(DATA_BITS);
                    serialPort.setStopBits(STOP_BITS);
                    serialPort.setParity(PARITY);
                    /**
                     * Current flow control Options:
                     * UsbSerialInterface.FLOW_CONTROL_OFF
                     * UsbSerialInterface.FLOW_CONTROL_RTS_CTS only for CP2102 and FT232
                     * UsbSerialInterface.FLOW_CONTROL_DSR_DTR only for CP2102 and FT232
                     */
                    serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                    serialPort.read(mCallback);
                    serialPort.getCTS(ctsCallback);
                    serialPort.getDSR(dsrCallback);

                    //
                    // Some Arduinos would need some sleep because firmware wait some time to know whether a new sketch is going
                    // to be uploaded or not
                    //Thread.sleep(2000); // sleep some. YMMV with different chips.

                    // Everything went as expected. Send an intent to MainActivity
                    Intent intent = new Intent(ACTION_USB_READY);
                    context.sendBroadcast(intent);
                } else {
                    // Serial port could not be opened, maybe an I/O error or if CDC driver was chosen, it does not really fit
                    // Send an Intent to Main Activity
                    if (serialPort instanceof CDCSerialDevice) {
                        Intent intent = new Intent(ACTION_CDC_DRIVER_NOT_WORKING);
                        context.sendBroadcast(intent);
                    } else {
                        Intent intent = new Intent(ACTION_USB_DEVICE_NOT_WORKING);
                        context.sendBroadcast(intent);
                    }
                }
            } else {
                // No driver for given device, even generic CDC driver could not be loaded
                Intent intent = new Intent(ACTION_USB_NOT_SUPPORTED);
                context.sendBroadcast(intent);
            }
        }
    }

}

