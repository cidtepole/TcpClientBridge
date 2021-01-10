package com.cidtepole.tcpclientbridge;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cidtepole.tcpclientbridge.adapter.FragmentAdapter;
import com.cidtepole.tcpclientbridge.communication.Communicator;
import com.cidtepole.tcpclientbridge.data.Clock;
import com.cidtepole.tcpclientbridge.data.InternalStorage;
import com.cidtepole.tcpclientbridge.data.Tools;
import com.cidtepole.tcpclientbridge.dialogs.AlertDialogFragment;
import com.cidtepole.tcpclientbridge.dialogs.AlertDialogFragment2;
import com.cidtepole.tcpclientbridge.dialogs.DialogConnect;
import com.cidtepole.tcpclientbridge.dialogs.DialogEditMacro;
import com.cidtepole.tcpclientbridge.dialogs.DialogSinglePickerFragment;
import com.cidtepole.tcpclientbridge.fragment.SummaryFragment;
import com.cidtepole.tcpclientbridge.fragment.TerminalFragment;

import com.cidtepole.tcpclientbridge.model.Item;
import com.cidtepole.tcpclientbridge.model.Macro;
import com.cidtepole.tcpclientbridge.model.Mensaje;
import com.cidtepole.tcpclientbridge.model.Server;
import com.cidtepole.tcpclientbridge.model.ServerSave;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.cidtepole.tcpclientbridge.Manager.ClientManager.getDeviceIpAddress;


public class MainActivity extends AppCompatActivity implements Communicator, DialogConnect.ConnectDialogListener, DialogEditMacro.EditMacroDialogListener {

    public static final String BROADCAST = "Broadcast";


    private static File file = null;
    private static  String fileLogName = "logFile.txt";

    private static final String Dialogo_ID_SendMto = "D_SMT";
    private static final String Dialogo_ID_Warning_Stop = "D_WS";
    private static final String Dialogo_ID_DataFormat_Summ = "D_DFS";
    private static final String Dialogo_ID_DataFormat_Term = "D_DFT";
    private static final String Dialogo_ID_Clear_Summ = "D_CS";
    private static final String Dialogo_ID_Clear_Term = "D_CT";
    private static final String Dialogo_ID_Select_Row_Buttons_Summ = "D_SRBS";
    private static final String Dialogo_ID_Select_Row_Buttons_Term = "D_SRBT";

    public static final String ID_MACROS_SUMM = "M_SUMM";
    public static final String ID_MACROS_TERM = "M_TERM";

    public static final String Activity_Code = "code";


    // REQUEST_CODE can be any value we like, used to determine the result type later
    private final int REQUEST_CODE = 20;

    public static boolean SERVICE_CONNECTED = false;

    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar toolbar;
    private AppBarLayout appBarLayout;


    private ViewPager viewPager;


    private DrawerLayout drawerLayout;

    private View parent_view;
    //private LinearLayout macros_row1;

    private SummaryFragment f_summary;
    private TerminalFragment f_terminal;



    private String sendTo = MainActivity.BROADCAST;


    boolean mBound = false;
    Messenger outMessenger = null;
    Messenger inMessenger=null;

    private int countClients = 0;
    private android.support.v7.widget.PopupMenu popupMenu;
    private MenuItem menuItem_run=null;
    private MenuItem menuItem_servers=null;
    private MenuItem menuItem_sendLogSumm = null;
    private MenuItem menuItem_sendLogTerm = null;
    private MenuItem menuItem_clearScreenSumm = null;
    private MenuItem menuItem_clearScreenTerm = null;

    private String formatSummary = "ASCII";
    private String formatTerminal = "ASCII";

    // The list that should be saved to internal storage.
    private ArrayList<Macro> macros_summ;
    private ArrayList<Macro> macros_term;
    public static final String KEY_MACROS_SUMM = "keyMacrosSumm";//KEY file to save macros
    public static final String KEY_MACROS_TERM = "keyMacrosTerm";//KEY file to save macros
    public static final String ROWS_SUMM = "ROWS_SUMM";
    public static final String ROWS_TERM = "ROWS_TERM";
    private String row_buttons_sum = "NONE"; //variable auxiliar para guardar la opion del numero de filas de botones visibles
    private String row_buttons_term = "NONE";
    private final String[] optionsRows = {"NONE", "1 ROW", "2 ROW", "3 ROW"};
    public static final String PREF_IP = "IP";//KEY file to save macros
    public static final String PREF_PORT = "PORT";
    public static final String PREF_CONNECT = "CONNECT";




    private ArrayList<Server> servers;
    private ArrayList<ServerSave> servers_save;
    public static final String KEY_SERVERS = "keyServers";//KEY file to save servers

    private String prefServerIP;
    private int prefPort;
    private boolean connect_on_opening;
    private boolean candado_onActivity_Result;



    private ServiceConnection mCon = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            outMessenger = new Messenger(binder);
            mBound = true;
            Log.i("INFO", "Servicio Ligado");

            if(connect_on_opening && !MainService.CLIENT_CONNECTED && !candado_onActivity_Result){

                onDialogConnectClick("",prefServerIP, prefPort, connect_on_opening);

            }

        }

        public void onServiceDisconnected(ComponentName className) {
            mBound = false;
            Log.i("INFO", "mBound = false");
            //outMessenger = null;
        }
    };

    public MainActivity() {
    }


    @Override
    public void onDialogConnectClick(String id, String ip, int puerto, boolean connect_on_opening) {

        Bundle bundleTx = new Bundle();
        bundleTx.putString(MainService.IP, ip);
        bundleTx.putInt(MainService.PORT, puerto);
        Message msg = Message.obtain(null, MainService.MESSAGE_CONNECT, 0, 0);
        msg.setData(bundleTx);

        this.connect_on_opening=connect_on_opening;

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor=pref.edit();
        editor.putBoolean(PREF_CONNECT, connect_on_opening);
        editor.commit();
        prefServerIP = ip;
        prefPort = puerto;


        try {
            outMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        //item.setIcon(getResources().getDrawable(R.drawable.ic_stop));

        //boton.setText(getApplicationContext().getString(R.string.stop_server));


        //SERVER_ALIVE=true;
        Log.i("INFO","UNO");
        return;
    }

    @Override
    public void onDialogEditMacroClick(String id, String name, String value, int index) {

        switch (id){
            case ID_MACROS_SUMM:
                f_summary.updateMacro(name, value, index);
                break;

            case ID_MACROS_TERM:
                f_terminal.updateMacro(name, value, index);
                break;

             default:
                 break;

        }
    }

    //Responsable de los mensajes de entrada desde MainService
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            Item item = msg.getData().getParcelable(Item.ITEM); //Obtenemos el Item de información

            if(item!=null){

                //new Thread(new verificarScroll()).start();//Hilo para checar el Scroll
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                int NUM_OF_BUBBLES = Integer.parseInt(pref.getString("numOfBubbles","1000"));
                int NUM_OF_LINES = Integer.parseInt(pref.getString("numOfLines","1000"));

                if(f_summary.getItemsSize()>= NUM_OF_BUBBLES ){
                    f_summary.deleteItems();
                }

                if(f_terminal.getItemsSize()>= NUM_OF_LINES ){
                    f_terminal.deleteItems();
                }

                switch (item.getDisplay()){
                    case Item.infoSummary:
                        f_summary.displayItem(item);
                        //f_terminal.displayItem(item);//////////////////////////////???????????????????????????
                        break;
                    case Item.infoSummAndTerm:
                        f_summary.displayItem(item);
                        f_terminal.displayItem(item);
                        break;

                    case Item.fromTerminal:
                        if(formatTerminal.equals("ASCII")) {
                            item.setEncabezado(Clock.getTime());
                            item.setContenido(item.getContenido().replace("\n", ""));
                            item.setContenido(item.getContenido().replace("\r", ""));
                            f_terminal.displayItem(item);
                        }else{
                            item.setEncabezado(Clock.getTime());
                            item.setContenido(Tools.ByteArrayToHexString(item.getContenido().getBytes()));
                            f_terminal.displayItem(item);
                        }
                        break;

                    case Item.fromSummary:
                        if(MainService.CLIENT_CONNECTED){
                            if(formatSummary.equals("ASCII")) {
                                f_summary.displayItem(item);
                            }else{
                                item.setContenido(Tools.ByteArrayToHexString(item.getContenido().getBytes()));
                                f_summary.displayItem(item);
                            }
                        }
                        break;

                    case Item.toSummary:
                        if(formatSummary.equals("ASCII")) {
                            f_summary.displayItem(item);
                        }else{
                            item.setContenido(Tools.ByteArrayToHexString(item.getContenido().getBytes()));
                            f_summary.displayItem(item);
                        }
                        break;

                    case Item.toTerminal:
                        if(formatTerminal.equals("ASCII")) {
                            item.setEncabezado(Clock.getTime());
                            item.setContenido(item.getContenido().replace("\n", ""));
                            item.setContenido(item.getContenido().replace("\r", ""));
                            f_terminal.displayItem(item);
                        }else{
                            item.setEncabezado(Clock.getTime());
                            item.setContenido(Tools.ByteArrayToHexString(item.getContenido().getBytes()));
                            f_terminal.displayItem(item);
                        }
                        break;


                    case Item.toSummAndTerm:
                        if(formatTerminal.equals("ASCII")) {
                            f_summary.displayItem(item);
                            //item.setEncabezado(Clock.getTime());
                            //item.setDisplay(Item.toTerminal);
                            item.setContenido(item.getContenido().replace("\n", ""));
                            item.setContenido(item.getContenido().replace("\r", ""));
                            f_terminal.displayItem(item);
                        }else{
                            item.setContenido(Tools.ByteArrayToHexString(item.getContenido().getBytes()));
                            f_summary.displayItem(item);
                            //item.setEncabezado(Clock.getTime());
                            //item.setDisplay(Item.toTerminal);
                            f_terminal.displayItem(item);
                        }
                        break;



                    default:

                        break;


                }
            }


            switch (msg.what){
                case MainService.MESSAGE_CONNECTED:
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor=pref.edit();
                    editor.putString(PREF_IP, prefServerIP);
                    editor.putInt(PREF_PORT, prefPort);
                    editor.commit();

                    Server server=new Server(0, "", prefServerIP, prefPort);
                    boolean add_server= true;
                    String ip = server.getIp();
                    int port = server.getPort();


                    for(Server server_l:servers){ //Verificamos que la direccion del servidor no este repetida
                        if ( ip.equals(server_l.getIp())&& port == server_l.getPort()) {
                            add_server=false;
                            //Log.i("INFO_add_servers", "false");
                        }
                    }

                    if(add_server){
                           servers.add(server);
                           Log.i("Servers", "Servidor agregado");
                           saveServers();
                    }

                    menuItem_run.setIcon(getResources().getDrawable(R.drawable.ic_conectado));
                    break;

                case MainService.MESSAGE_DISCONNECTED:
                    menuItem_run.setIcon(getResources().getDrawable(R.drawable.ic_desconectado));
                    break;

                /*
                case MainService.MESSAGE_CLIENT_CONNECTED:
                    Cliente itmc0 = msg.getData().getParcelable("Cliente");
                    clientes.add(itmc0);
                    doIncrease(); //incrementamos popup;

                    break;

                case MainService.MESSAGE_CLIENT_DISCONNECTED:

                    Cliente itmc1 = msg.getData().getParcelable("Cliente");
                    for (Cliente c : clientes) {
                        if(c.getNombre().equals(itmc1.getNombre())){
                            Log.i("INFO", c.getNombre()+"  "+itmc1.getNombre());
                            clientes.remove(c);

                            break;
                        }
                    }

                    doDecrease(); //incrementamos popup;

                    break;

                */
                case MainService.MESSAGE_SET_OPTIONS_CLIENT:


                    break;



                case MainService.MESSAGE_BUSY_PORT:

                    //showAlertDialog(getApplicationContext().getString(R.string.warning),
                    //getApplicationContext().getString(R.string.busy_port);

                    FragmentManager fm = getSupportFragmentManager();
                    AlertDialogFragment alertDialog = AlertDialogFragment.newInstance(getApplicationContext().getString(R.string.warning),
                            getApplicationContext().getString(R.string.busy_port) );
                    alertDialog.show(fm, "fragment_alert");


                    break;


                case MainService.MESSAGE_BUTTON_STOP:
                    //boton.setText(getApplicationContext().getString(R.string.stop_server));
                    break;


            }


        }
    }

    /*
     * Las notificaciones de UsbService serán recibidas aquí.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case MainService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, getApplicationContext().getString(R.string.usb_ready), Toast.LENGTH_SHORT).show();
                    break;
                case MainService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, getApplicationContext().getString(R.string.usb_permission_not_granted), Toast.LENGTH_SHORT).show();
                    break;
                case MainService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, getApplicationContext().getString(R.string.no_usb_connected), Toast.LENGTH_SHORT).show();
                    break;
                case MainService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    SharedPreferences prefs =
                            getSharedPreferences("MisPreferencias",Context.MODE_PRIVATE);     // 16 de enero 2017
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("isEnable", true);  // Habilitamos el menu Settings
                    editor.commit();
                    Toast.makeText(context, getApplicationContext().getString(R.string.usb_disconnected), Toast.LENGTH_SHORT).show();
                    break;
                case MainService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, getApplicationContext().getString(R.string.usb_device_not_supported), Toast.LENGTH_SHORT).show();
                    break;
                //case MainService.ACTION_RELAUNCH_SERVER: // RELANZAR SERVICIO


                //  break;
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        parent_view = findViewById(R.id.main_content);
        setupDrawerLayout();
        initComponent();
        prepareActionBar(toolbar);
        initAction();
        candado_onActivity_Result = false;

        if (viewPager != null) {
            setupViewPager(viewPager);
        }

        initAction();

        //addMacros();

        retrieveMacros();
        retrieveServers();

        setFilters();  // Start listening notifications from UsbService


        SharedPreferences prefs =
                getSharedPreferences("MisPreferencias",Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit(); //Habilitamos el menu Settings, ligado a las funciones iniciar y detener Servidor
        editor.putBoolean("isEnable", true);
        editor.commit();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        fileLogName = pref.getString("fileFormat","logFile.txt");

        prefServerIP=pref.getString(PREF_IP,"");
        prefPort=pref.getInt(PREF_PORT,0);
        connect_on_opening=pref.getBoolean(PREF_CONNECT,false);

        //SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
         row_buttons_sum = pref.getString(ROWS_SUMM, "NONE");
         row_buttons_term = pref.getString(ROWS_TERM, "NONE");
        //f_summary.setVisibleRowButtons("NONE");


    }

    private void retrieveMacros() {

        macros_summ = new ArrayList<Macro>(); //Lista de Macros a guardar
        macros_term = new ArrayList<Macro>(); //Lista de Macros a guardar


        //if(macros_summ.size()>1) {
            Log.i("Macro size", String.valueOf(macros_summ.size()));
            try {

                // Retrieve the list from internal storage
                macros_summ = (ArrayList<Macro>) InternalStorage.readObject(this, KEY_MACROS_SUMM);
                macros_term = (ArrayList<Macro>) InternalStorage.readObject(this, KEY_MACROS_TERM);

                // Display the items from the list retrieved.
                for (Macro macro : macros_summ) {

                    //macro.setName("M");
                    Log.d("INFO", macro.getName());
                }

                // Save the list of entries to internal storage
                //InternalStorage.writeObject(this, KEY, cachedMacros);


            } catch (IOException e) {
                Log.e("INFO", e.getMessage());
            } catch (ClassNotFoundException e) {
                Log.e("INFO", e.getMessage());
            }

        //}else{
        if(macros_summ.size()<1 && macros_term.size()<1){
            addMacros();
            Log.i("Macro size", String.valueOf(macros_summ.size()));
        }
    }




    private void retrieveServers() {

        servers = new ArrayList<Server>(); //Lista de Servers
        servers_save = new ArrayList<ServerSave>(); //Lista de Servers a guardar

        //if(servers.size()>1) {

            try {

                //Log.i("INFO", "lista de servidores");
                // Retrieve the list from internal storage
                servers_save = (ArrayList<ServerSave>) InternalStorage.readObject(this, KEY_SERVERS);

                for(ServerSave server:servers_save){
                    Server s = new Server(server.getId(),server.getName(),server.getIp(),server.getPort());
                    servers.add(s);
                }


            } catch (IOException e) {
                Log.e("INFO", e.getMessage());
            } catch (ClassNotFoundException e) {
                Log.e("INFO", e.getMessage());
            }
        //}else{
            //addServer("192.168.1.66", 1234);
        //}
    }



    private void addMacros(){

        // The list that should be saved to internal storage.
        ArrayList<Macro> macros_summ = new ArrayList<Macro>();
        macros_summ.add(new Macro("M1","M1", "", 0, 0, 0));
        macros_summ.add(new Macro("M2","M2", "", 0, 0, 0));
        macros_summ.add(new Macro("M3","M3", "", 0, 0, 0));
        macros_summ.add(new Macro("M4","M4", "", 0, 0, 0));
        macros_summ.add(new Macro("M5","M5", "", 0, 0, 0));
        macros_summ.add(new Macro("M6","M6", "", 0, 0, 0));
        macros_summ.add(new Macro("M7","M7", "", 0, 0, 0));
        macros_summ.add(new Macro("M8","M8", "", 0, 0, 0));
        macros_summ.add(new Macro("M9","M9", "", 0, 0, 0));
        macros_summ.add(new Macro("M10","M10", "", 0, 0, 0));
        macros_summ.add(new Macro("M11","M11", "", 0, 0, 0));
        macros_summ.add(new Macro("M12","M12", "", 0, 0, 0));

        ArrayList<Macro> macros_term = new ArrayList<Macro>();
        macros_term.add(new Macro("M1","M1", "", 0, 0, 0));
        macros_term.add(new Macro("M2","M2", "", 0, 0, 0));
        macros_term.add(new Macro("M3","M3", "", 0, 0, 0));
        macros_term.add(new Macro("M4","M4", "", 0, 0, 0));
        macros_term.add(new Macro("M5","M5", "", 0, 0, 0));
        macros_term.add(new Macro("M6","M6", "", 0, 0, 0));
        macros_term.add(new Macro("M7","M7", "", 0, 0, 0));
        macros_term.add(new Macro("M8","M8", "", 0, 0, 0));
        macros_term.add(new Macro("M9","M9", "", 0, 0, 0));
        macros_term.add(new Macro("M10","M10", "", 0, 0, 0));
        macros_term.add(new Macro("M11","M11", "", 0, 0, 0));
        macros_term.add(new Macro("M12","M12", "", 0, 0, 0));


        try {
            // Save the list of entries to internal storage
            InternalStorage.writeObject(this, KEY_MACROS_SUMM, macros_summ);
            InternalStorage.writeObject(this, KEY_MACROS_TERM, macros_term);


        } catch (IOException e) {
            Log.e("Macros", e.getMessage());
        }


    }






    private void saveServers(){

        try {

            servers_save.clear();

            for(Server server:servers){
                ServerSave s= new ServerSave(server.getId(),server.getName(),server.getIp(),server.getPort());
                servers_save.add(s);
            }

            // Save the list of entries to internal storage
            InternalStorage.writeObject(this, KEY_SERVERS, servers_save);
            Log.i("Servers", "Servidor salvado");

        } catch (IOException e) {
           Log.e("Servers", e.getStackTrace().toString());
        }


    }




    private void setupViewPager(ViewPager viewPager) {
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());

        if (f_summary == null) {
            f_summary = new SummaryFragment();
        }
        if (f_terminal == null) {
            f_terminal = new TerminalFragment();
        }

        adapter.addFragment(f_summary, getApplicationContext().getString(R.string.summary));
        adapter.addFragment(f_terminal, getApplicationContext().getString(R.string.terminal));

        viewPager.setAdapter(adapter);
    }



    private void setupDrawerLayout() {
        //drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    }

    private void prepareActionBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setHomeButtonEnabled(true);
        //actionBar.hide();
    }





    public void setVisibilityAppBar(boolean visible){
        CoordinatorLayout.LayoutParams layout_visible = new CoordinatorLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        CoordinatorLayout.LayoutParams layout_invisible = new CoordinatorLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
        if(visible){
            appBarLayout.setLayoutParams(layout_visible);

        }else{
            appBarLayout.setLayoutParams(layout_invisible);

        }
    }



    private void initAction() {

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                viewPager.setCurrentItem(tab.getPosition());
                switch (tab.getPosition()) {

                    case 0:

                        if(menuItem_clearScreenSumm!=null) {


                        }
                        break;
                    case 1:

                        if(menuItem_clearScreenSumm!=null) {

                        }
                        break;
                    case 2:
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        viewPager.setCurrentItem(0);
    }

    private void initComponent() {
        toolbar = (Toolbar) findViewById(R.id.toolbar_viewpager);
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        //macros_row1 = (LinearLayout) findViewById(R.id.macros_row1);
        //macros_row1.setVisibility(View.GONE);
        //settingDrawer();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mDrawerToggle.onConfigurationChanged(newConfig);

    }


    private  void doDecrease(){
        countClients--;
        invalidateOptionsMenu();

    }

    private void doIncrease() {
        countClients++;
        invalidateOptionsMenu();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.menu_main, menu);

        //MenuItem menuItem_clients = menu.findItem(R.id.action_servers);
        //menuItem_clients.setIcon(buildCounterDrawable(countClients));



        menuItem_run = menu.findItem(R.id.action_run);
        menuItem_servers = menu.findItem(R.id.action_servers);
        menuItem_sendLogSumm = menu.findItem(R.id.action_send_logFile_summ);
        menuItem_sendLogTerm = menu.findItem(R.id.action_send_logFile_term);
        menuItem_clearScreenSumm = menu.findItem(R.id.action_delete_summ);
        menuItem_clearScreenTerm = menu.findItem(R.id.action_delete_term);


        if(MainService.CLIENT_CONNECTED)
            menuItem_run.setIcon(getResources().getDrawable(R.drawable.ic_conectado));
        else
            menuItem_run.setIcon(getResources().getDrawable(R.drawable.ic_desconectado));






        if(viewPager!=null){
            if(viewPager.getCurrentItem()==0){
                menuItem_clearScreenSumm.setVisible(true);
                menuItem_sendLogSumm.setVisible(true);
                menuItem_clearScreenTerm.setVisible(false);
                menuItem_sendLogTerm.setVisible(false);
            }else{
                menuItem_clearScreenSumm.setVisible(false);
                menuItem_sendLogSumm.setVisible(false);
                menuItem_clearScreenTerm.setVisible(true);
                menuItem_sendLogTerm.setVisible(true);
            }

        }




        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // REQUEST_CODE is defined above
        Log.i("INFO", String.valueOf(resultCode));

        candado_onActivity_Result = true;//para evitar que se conecte cada vez que regrese a la Activity
        if (resultCode == MainActivity.RESULT_OK && requestCode == REQUEST_CODE) {
            // Extract name value from result extras

            int code = data.getExtras().getInt(Activity_Code, 0);

            //ArrayList<Server> servers_aux = data.getParcelableArrayListExtra("Servers");;
            //servers.clear();

            //for(Server s:servers_aux) {
              //  servers.add(s);
            //}

            servers = data.getParcelableArrayListExtra("Servers");
            saveServers();

            //Log.i("INFO", "onActivityResulttepole "  + cliente.getIp());

            Bundle bundleTx = new Bundle();

            switch (code){
                case ServersActivity.CONNECT:
                    if(!MainService.CLIENT_CONNECTED){
                        Server server =  data.getParcelableExtra(Server.SERVER);
                        onDialogConnectClick("", server.getIp(), server.getPort(), connect_on_opening);
                    }

/*
                    Server server =  data.getParcelableExtra(Server.SERVER);
                    //addServer(server.getIp(), server.getPort());
                    bundleTx = new Bundle();
                    bundleTx.putString(MainService.IP, server.getIp());
                    bundleTx.putInt(MainService.PORT, server.getPort());
                    Message msg = Message.obtain(null, MainService.MESSAGE_CONNECT, 0, 0);
                    msg.setData(bundleTx);


                    try {
                        outMessenger.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
*/
                    break;


                default:

                    break;
            }


        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id){
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;


            case R.id.action_delete_summ:;

                FragmentManager fmCsS = getSupportFragmentManager();
                AlertDialogFragment2 alertDialogFmCsS = AlertDialogFragment2.newInstance(Dialogo_ID_Clear_Summ, getApplicationContext().getString(R.string.warning),
                        getApplicationContext().getString(R.string.Do_you_want_to_clear_the_summary_screen) );
                alertDialogFmCsS.show(fmCsS, "fragment_alert");

                return true;


            case R.id.action_delete_term:;

                FragmentManager fmCsT = getSupportFragmentManager();
                AlertDialogFragment2 alertDialogFmCsT = AlertDialogFragment2.newInstance(Dialogo_ID_Clear_Term, getApplicationContext().getString(R.string.warning),
                        getApplicationContext().getString(R.string.Do_you_want_to_clear_the_terminal_screen) );
                alertDialogFmCsT.show(fmCsT, "fragment_alert");

                return true;

            case R.id.action_help:
              /*
                Intent intent = new Intent(this, HelpActivity.class);
                startActivity(intent);

                return true;
               */

            case R.id.action_contact:

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "cidtepole@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "ServerBridgeX");
                getApplicationContext().startActivity(Intent.createChooser(emailIntent, "Write a comment.").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));



                return true;


            case R.id.action_send_logFile_summ:

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                fileLogName = prefs.getString("fileFormat","logFile.txt");

                file = new File(this.getFilesDir(), fileLogName);
                f_summary.logData();

                return true;


            case R.id.action_send_logFile_term:

                SharedPreferences preft = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                fileLogName = preft.getString("fileFormat","logFile.txt");

                file = new File(this.getFilesDir(), fileLogName);
                f_terminal.logData();

                return true;

            case R.id.action_servers:

                Log.i("ACTION servers size", String.valueOf(servers.size()));
                Intent intent = new Intent(getApplicationContext(), ServersActivity.class);
                intent.putParcelableArrayListExtra("Servers",  servers);
                startActivityForResult(intent, REQUEST_CODE);

                return true;



            case R.id.action_show_ip:

                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
                String port= pref.getString("port","1234");
                FragmentManager fm = getSupportFragmentManager();
                AlertDialogFragment alertDialog = AlertDialogFragment.newInstance(getApplicationContext().getString(R.string.ip_address),getDeviceIpAddress()+":"+port );
                alertDialog.show(fm, "fragment_alert");

                return true;

            case R.id.action_run:

                connect();

                return true;

        }

        //noinspection SimplifiableIfStatement
        return super.onOptionsItemSelected(item);
    }





    protected Boolean estaConectado(){
        if(conectadoWifi()){
            //showAlertDialog("Conexion a Internet",
            //"Tu Dispositivo tiene Conexion a Wifi.");
            return true;
        }else{
            if(conectadoRedMovil()){
                //showAlertDialog("Conexion a Internet",
                //"Tu Dispositivo tiene Conexion Movil.");
                return true;
            }else{
                showAlertDialog(getApplicationContext().getString(R.string.internet_connection),
                        getApplicationContext().getString(R.string.without_connection));
                return false;
            }
        }
    }

    protected Boolean conectadoWifi(){
        ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (info != null) {
                if (info.isConnected()) {
                    return true;
                }
            }
        }
        return false;
    }

    protected Boolean conectadoRedMovil(){
        ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (info != null) {
                if (info.isConnected()) {
                    return true;
                }
            }
        }
        return false;
    }


    protected void showAlertDialog(String title, String message) {

        // get prompts.xml view
        //LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        //setTheme(R.style.DialogTheme);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme));

        // setup a dialog window
        alertDialogBuilder.setCancelable(true)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();

                    }
                });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }







    private Drawable buildCounterDrawable(int count) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.notification_bubble, null);
        //view.setBackgroundResource(backgroundImageId);

        if (count == 0) {
            TextView textView = (TextView) view.findViewById(R.id.count);
            textView.setVisibility(View.GONE);
        } else {
            TextView textView = (TextView) view.findViewById(R.id.count);
            textView.setText("" + count);
        }

        view.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        return new BitmapDrawable(getResources(), bitmap);
    }


    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        if(pref.getBoolean("prefScreen",false))
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if(f_terminal!=null) {
            f_terminal.setTextSize(Integer.parseInt(pref.getString("textSize", "18")));
            f_terminal.bindView();//para refrescar
        }
        //Messenger inMessenger;
        //inMessenger = new Messenger(new IncomingHandler());
        //Intent lIntent = new Intent(this, MainService.class);
        //lIntent.putExtra("Messenger", inMessenger);
        //bindService(lIntent, mCon, 0); // mCon is an object of ServiceConnection Class
        startService(MainService.class, mCon); // Iniciar sercvicio y si no esta iniciado, ligarlo



        Log.i("INFO", "onResume");
        //if(getInicia(this)==2) {
        //  new Thread(new ServerThreadRun()).start();//Hilo para iniciar servidor
        // }

        //addServer("192.168.1.66", 1234);
        //addServer("192.168.1.67",1234);

    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mCon);
            mBound = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUsbReceiver);
        stopService(MainService.class, mCon);
        if (mBound) {
            unbindService(mCon);
            mBound = false;
        }

        Log.i("INFO", "onDestroy Activity");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBound) {
            unbindService(mCon);
            mBound = false;
        }
    }


    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(MainService.ACTION_NO_USB);
        filter.addAction(MainService.ACTION_USB_DISCONNECTED);
        filter.addAction(MainService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(MainService.ACTION_USB_PERMISSION_NOT_GRANTED);
        //filter.addAction(MainService.ACTION_RELAUNCH_SERVER);
        registerReceiver(mUsbReceiver, filter);

    }


    private void startService(Class<?> service, ServiceConnection serviceConnection) {
        if (!MainService.isRunning()) {
            Intent lIntent = new Intent(this, service);
            startService(lIntent);
            SERVICE_CONNECTED=true;
            Log.i("INFO", "Servicio Iniciado");
        }

        //Messenger inMessenger;
        inMessenger = new Messenger(new IncomingHandler());
        Intent lIntent = new Intent(this, MainService.class);
        lIntent.putExtra("Messenger", inMessenger);
        bindService(lIntent, mCon, Context.BIND_AUTO_CREATE); // mCon is an object of ServiceConnection Class

        //Log.i("INFO", "startService");
        //Intent bindingIntent = new Intent(this, service);
        //bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }


    private void stopService(Class<?> service, ServiceConnection serviceConnection) {

        if (mBound) {
            unbindService(mCon);
            mBound = false;
        }

        if (MainService.SERVICE_CONNECTED) {
            stopService(new Intent(this, service));
        }

    }


    private long exitTime = 0;

    public void doExitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(this, "Press twice to exit", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        doExitApp();
    }


    @Override
    public void writeToServer(String data) {

        /*
        Item item = new Item(0, "info",data, Clock.getNow(),Item.fromSummary);
        if(formatSummary.equals("ASCII")) {
            f_summary.displayItem(item);
        }else{
            item.setContenido(Tools.ByteArrayToHexString(item.getContenido().getBytes()));
            f_summary.displayItem(item);
        }*/

        if(mBound){


            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if(pref.getBoolean("prefCR",false)){
                data=data+"\r";
            }
            if(pref.getBoolean("prefLF",false)){
                data=data+"\n";
            }

            Bundle bundle = new Bundle();

            if(MainService.CLIENT_CONNECTED){       //Enviar mensaje al Servidor


                Mensaje mensaje_to_Server = new Mensaje("fromMe", sendTo,  data.getBytes());
                bundle.putParcelable(Mensaje.MENSAJE, mensaje_to_Server);
                Message msg = Message.obtain(null, MainService.MESSAGE_TO_SERVER, 0, 0);
                msg.setData(bundle);
                try {
                    outMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
        }

        return;
    }


    public void writeToSerial(String data) {

        /*
        Item item = new Item(0, "info",data, Clock.getNow(),Item.fromTerminal);
        if(formatTerminal.equals("ASCII")) {
            f_terminal.displayItem(item);
        }else{
            item.setContenido(Tools.ByteArrayToHexString(item.getContenido().getBytes()));
            f_terminal.displayItem(item);
        }*/

        if(mBound && !data.equals("")){

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if(pref.getBoolean("prefCR",false)){
                data=data+"\r";
            }
            if(pref.getBoolean("prefLF",false)){
                data=data+"\n";
            }

            Bundle bundle = new Bundle();

            if(MainService.SERIAL_PORT_CONNECTED){    //Enviar mensaje a usb

                Mensaje mensaje_to_USB = new Mensaje("Dispositivo","USB",  data.getBytes());
                bundle.putParcelable(Mensaje.MENSAJE, mensaje_to_USB);
                Message msg = Message.obtain(null, MainService.MESSAGE_TO_USB, 0, 0);
                msg.setData(bundle);
                try {
                    outMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        return;

    }

    @Override
    public void showMyDialog(int dialog) {

        switch (dialog){
            case Communicator.dialogSelectClient:

                break;

            case Communicator.dialogSelectFormatSummary:
                String[] optionsS = {"ASCII", "HEX"};

                int j=0;

                for (String s : optionsS) {
                    if(optionsS[j].equals(formatSummary)){
                        break;
                    }
                    j++;
                }

                FragmentManager fm_sFS = getSupportFragmentManager();
                DialogSinglePickerFragment alertDialog_sFS = DialogSinglePickerFragment.newInstance(Dialogo_ID_DataFormat_Summ,j, optionsS, "Data Format");/////############???????
                alertDialog_sFS.show(fm_sFS, "fragment_alert");
                break;


            case Communicator.dialogSelectFormatTerminal:
                String[] optionsT = {"ASCII", "HEX"};

                int k=0;

                for (String onDialogConnectClicks : optionsT) {
                    if(optionsT[k].equals(formatSummary)){
                        break;
                    }
                    k++;
                }

                FragmentManager fm_sFT = getSupportFragmentManager();
                DialogSinglePickerFragment alertDialog_sFT = DialogSinglePickerFragment.newInstance(Dialogo_ID_DataFormat_Term,0, optionsT, "Data Format ");/////############???????
                alertDialog_sFT.show(fm_sFT, "fragment_alert");
                break;


            case Communicator.dialogSelectRowButtonsSumm:

                //String[] optionsDsrb = {"NONE", "1 ROW", "2 ROW", "3 ROW"};

                int op_summ=0;
                for (String s : optionsRows) {
                    if(optionsRows[op_summ].equals(row_buttons_sum)){
                        break;
                    }
                    op_summ++;
                }

                FragmentManager fm_srbFS = getSupportFragmentManager();
                DialogSinglePickerFragment Dialog_srbFS = DialogSinglePickerFragment.newInstance(Dialogo_ID_Select_Row_Buttons_Summ, op_summ, optionsRows, "Select Row Buttons");/////############???????
                Dialog_srbFS.show(fm_srbFS, "fragment_alert");

                break;


            case Communicator.dialogSelectRowButtonsTerm:

                int op_term=0;
                for (String s : optionsRows) {
                    if(optionsRows[op_term].equals(row_buttons_sum)){
                        break;
                    }
                    op_term++;
                }

                FragmentManager fm_srbFT = getSupportFragmentManager();
                DialogSinglePickerFragment Dialog_srbFT = DialogSinglePickerFragment.newInstance(Dialogo_ID_Select_Row_Buttons_Summ, op_term, optionsRows, "Select Row Buttons");/////############???????
                Dialog_srbFT.show(fm_srbFT, "fragment_alert");

                break;


               default:

                break;
        }

        return;

    }

    @Override
    public void showEditDialogMacro(String id, String name,  String value, int index) {

        FragmentManager fm_eM = getSupportFragmentManager();
        DialogEditMacro Dialog_fm_eM = DialogEditMacro.newInstance(id,"Edit Macro", name, value, index);
        Dialog_fm_eM.show(fm_eM, "fragment_editMacro");
    }


    public void connect(){

        if(estaConectado()){

            if(!MainService.CLIENT_CONNECTED){
                FragmentManager fmDC = getSupportFragmentManager();
                DialogConnect DialogfmDC = DialogConnect.newInstance("","Conect", prefServerIP, prefPort, connect_on_opening);
                DialogfmDC.show(fmDC, "fragment_connect");
            }else{
                FragmentManager fmStop = getSupportFragmentManager();
                AlertDialogFragment2 alertDialogStop = AlertDialogFragment2.newInstance(Dialogo_ID_Warning_Stop, getApplicationContext().getString(R.string.warning),
                        getApplicationContext().getString(R.string.Do_you_want_to_stop_the_server) );
                alertDialogStop.show(fmStop, "fragment_alert");
            }

        }


        return;
    }


    @Override
    public void selectOption(String id, String option) {

        Log.i("INFO", "Select Option");
   /*
        if(id.equals(getApplicationContext().getString(R.string.sent_messages_to))){
            sendTo = option;
        }else if(id.equals("Data Format")){
            formatSummary = option;
            f_summary.setIconButtonFormat(option);
        }else if(id.equals("Data Format ")) {
            formatTerminal = option;
            f_terminal.setIconButtonFormat(option);
        }else if(id.equals(getApplicationContext().getString(R.string.warning))) {
            Log.i("INFO", "Stop Server");
            menuItem_run.setIcon(getResources().getDrawable(R.drawable.ic_play_arrow));
            Message msg = Message.obtain(null, MainService.STOP_SERVER, 0, 0);
            try {
                outMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            clientes.clear();
        }

        return;

      */


        switch (id){
            case Dialogo_ID_SendMto:
                sendTo = option;
                Log.i("INFO", sendTo);

                break;

            case Dialogo_ID_DataFormat_Summ:
                formatSummary = option;
                f_summary.setIconButtonFormat(option);
                break;


            case Dialogo_ID_DataFormat_Term:
                formatTerminal = option;
                f_terminal.setIconButtonFormat(option);
                break;

            case Dialogo_ID_Clear_Summ:
                f_summary.deleteItems();
                break;

            case Dialogo_ID_Clear_Term:
                f_terminal.deleteItems();
                break;


            case Dialogo_ID_Warning_Stop:
                Log.i("INFO", "Stop Connection");
                menuItem_run.setIcon(getResources().getDrawable(R.drawable.ic_desconectado));
                Message msg = Message.obtain(null, MainService.MESSAGE_DISCONNECT, 0, 0);
                try {
                    outMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                //clientes.clear();
                break;

            case Dialogo_ID_Select_Row_Buttons_Summ:

                row_buttons_sum = option;
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(ROWS_SUMM, option);
                editor.commit();

                f_summary.setVisibleRowButtons(option);
               break;

            case Dialogo_ID_Select_Row_Buttons_Term:

                row_buttons_sum = option;
                SharedPreferences pref_row_term = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor_row_term = pref_row_term.edit();
                editor_row_term.putString(ROWS_TERM, option);
                editor_row_term.commit();

                f_terminal.setVisibleRowButtons(option);
                break;

            default:

                break;
        }

        return;




    }



    @Override
    public void selectOptions(boolean[] options) {

    }


    public void createTemporaryFileSumm(List<Item> messages) {
        try {

            //File file = File.createTempFile(fileLogName, ".cvs", getCacheDir());
            String separador = " ";

            if(fileLogName.equals("logFile.txt"))
                separador = "  ";
            else
                separador = ",";


            if(file.exists()){
                //file = new File(file.getCanonicalPath(), fileLogName);
                file.delete();
                Log.i("INFO", "File delete");
            }

            file = new File(this.getFilesDir(), fileLogName);

            FileOutputStream outputStream = new FileOutputStream(file);

            String log = "SENT\n";
            outputStream.write(log.getBytes());

            for(Item item: messages){
                if(item.getDisplay()== Item.fromSummary){
                    if(item.getContenido().endsWith("\n"))
                        log = item.getFecha()+ separador + item.getContenido();
                    else
                        log = item.getFecha()+ separador + item.getContenido()+"\n";
                    outputStream.write(log.getBytes());
                }
            }

            log = "RECEIVE\n";
            outputStream.write(log.getBytes());

            for(Item item: messages){
                if(item.getDisplay()== Item.toSummary){
                    if(item.getContenido().endsWith("\n"))
                        log = item.getFecha()+ separador + item.getEncabezado().replace(":","")+  separador + item.getContenido();
                    else
                        log = item.getFecha()+ separador + item.getEncabezado().replace(":","")+ separador +item.getContenido()+"\n";
                    outputStream.write(log.getBytes());
                }
            }

            outputStream.flush();
            outputStream.close();



            // Get the shared file's Uri
            final Uri uri = FileProvider.getUriForFile(this, "com.cidtepole.serverbridge", file);

            // Create a intent
            final ShareCompat.IntentBuilder intentBuilder = ShareCompat.IntentBuilder.from(this)
                    .setType("text/plain")
                    .setSubject("LogFile Server BridgeX!")
                    .setChooserTitle("Choose application to share file")
                    .addStream(uri);

            // Start the intent
            final Intent chooserIntent = intentBuilder.createChooserIntent();
            startActivity(chooserIntent);

        } catch (IOException e) {
        }
    }


    public void createTemporaryFileTerm(List<Item> messages) {

        try {

            //File file = File.createTempFile(fileLogName, ".cvs", getCacheDir());
            String separador = " ";

            if(fileLogName.equals("logFile.txt"))
                separador = "  ";
            else
                separador = ",";


            if(file.exists()){
                //file = new File(file.getCanonicalPath(), fileLogName);
                file.delete();
                Log.i("INFO", "File delete");
            }

            file = new File(this.getFilesDir(), fileLogName);

            FileOutputStream outputStream = new FileOutputStream(file);

            String log = "SENT\n";
            outputStream.write(log.getBytes());

            for(Item item: messages){
                if(item.getDisplay()== Item.fromTerminal || item.getDisplay()== Item.toSummAndTerm){
                    if(item.getContenido().endsWith("\n"))
                        log = item.getFecha()+ separador + item.getContenido();
                    else
                        log = item.getFecha()+ separador + item.getContenido()+"\n";
                    outputStream.write(log.getBytes());
                }
            }

            log = "RECEIVE\n";
            outputStream.write(log.getBytes());

            for(Item item: messages){
                if(item.getDisplay()== Item.toTerminal){
                    if(item.getContenido().endsWith("\n"))
                        log = item.getFecha()+ separador  + item.getContenido();
                    else
                        log = item.getFecha() + separador +item.getContenido()+"\n";
                    outputStream.write(log.getBytes());
                }
            }

            outputStream.flush();
            outputStream.close();



            // Get the shared file's Uri
            final Uri uri = FileProvider.getUriForFile(this, "com.cidtepole.serverbridge", file);

            // Create a intent
            final ShareCompat.IntentBuilder intentBuilder = ShareCompat.IntentBuilder.from(this)
                    .setType("text/plain")
                    .setSubject("LogFile Server BridgeX!")
                    .setChooserTitle("Choose application to share file")
                    .addStream(uri);

            // Start the intent
            final Intent chooserIntent = intentBuilder.createChooserIntent();
            startActivity(chooserIntent);

        } catch (IOException e) {
        }

    }




    public void createTemporaryFileTerm2(List<Item> messages) {

        try {

            //File file = File.createTempFile(fileLogName, ".cvs", getCacheDir());
            String separador = " ";

            if(fileLogName.equals("logFile.txt"))
                separador = " ";
            else
                separador = ",";


            if(file.exists()){
                //file = new File(file.getCanonicalPath(), fileLogName);
                file.delete();
                Log.i("INFO", "File delete");
            }

            file = new File(this.getFilesDir(), fileLogName);

            FileOutputStream outputStream = new FileOutputStream(file);

            String log = "SENT\n";
            outputStream.write(log.getBytes());

            for(Item item: messages){
                if(item.getDisplay()== Item.fromSummary){
                    if(item.getContenido().endsWith("\n"))
                        log = item.getFecha()+ separador + item.getContenido();
                    else
                        log = item.getFecha()+ separador + item.getContenido()+"\n";
                    outputStream.write(log.getBytes());
                }
            }

            log = "RECEIVE\n";
            outputStream.write(log.getBytes());

            for(Item item: messages){
                if(item.getDisplay()== Item.toSummary){
                    if(item.getContenido().endsWith("\n"))
                        log = item.getFecha()+ separador + item.getEncabezado().replace(":","")+  separador + item.getContenido();
                    else
                        log = item.getFecha()+ separador + item.getEncabezado().replace(":","")+ separador +item.getContenido()+"\n";
                    outputStream.write(log.getBytes());
                }
            }

            outputStream.flush();
            outputStream.close();



            // Get the shared file's Uri
            final Uri uri = FileProvider.getUriForFile(this, "com.cidtepole.serverbridge", file);

            // Create a intent
            final ShareCompat.IntentBuilder intentBuilder = ShareCompat.IntentBuilder.from(this)
                    .setType("text/plain")
                    .setSubject("LogFile Server BridgeX!")
                    .setChooserTitle("Choose application to share file")
                    .addStream(uri);

            // Start the intent
            final Intent chooserIntent = intentBuilder.createChooserIntent();
            startActivity(chooserIntent);

        } catch (IOException e) {
        }

    }




}