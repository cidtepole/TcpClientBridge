package com.cidtepole.tcpclientbridge;

import android.content.Intent;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.cidtepole.tcpclientbridge.adapter.ServersListAdapter;
import com.cidtepole.tcpclientbridge.data.Tools;
import com.cidtepole.tcpclientbridge.dialogs.DialogConnect;
import com.cidtepole.tcpclientbridge.dialogs.DialogGeneric;
import com.cidtepole.tcpclientbridge.model.Item;
import com.cidtepole.tcpclientbridge.model.Server;

import java.util.ArrayList;
import java.util.List;

import static com.cidtepole.tcpclientbridge.MainActivity.Activity_Code;


public class ServersActivity extends AppCompatActivity implements  DialogConnect.ConnectDialogListener, DialogGeneric.GenericDialogListener {


    public static final String ID_FAB_EVENT = "ID_fab";
    public static final String ID_ITEM_EVENT = "ID_item";

    private ActionBar actionBar;
    private RecyclerView recyclerView;
    private ServersListAdapter adapter;
    private FloatingActionButton fab;
    private ListView listview;
    private ArrayList<Server> servers = new ArrayList<>();
    private int code = 1;
    private Intent intent;

    public static final int CONNECT = 1;
    public static final int CLEAR = 2;

    private Server server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servers);
        initToolbar();

        intent = getIntent();
        code = 0;
        servers = intent.getParcelableArrayListExtra("Servers");

        initComponent();



        Log.i("Servers size", String.valueOf(servers.size()));

        // specify an adapter (see also next example)
        adapter = new ServersListAdapter(this, servers);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new ServersListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Server server, int position) {
                Log.i("INFO", server.getIp());
                onDialogConnectClick(ID_ITEM_EVENT, server.getIp(), server.getPort(), false);//auxiliar
            }

            @Override
            public void onDeleteClick(int position) {

                FragmentManager fmDG = getSupportFragmentManager();
                DialogGeneric DialogfmDG = DialogGeneric.newInstance(position, "", "Do you want to delete the server?", "OK");
                DialogfmDG.show(fmDG, "fragment_generic");

                //servers.remove(position);
                //adapter.notifyDataSetChanged();
                //Log.i("INFO", "Delete " + position);
            }

        });


        // for system bar in lollipop
        Tools.systemBarLolipop(this);


    }

    private void initComponent() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        //recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        fab = findViewById(R.id.fab_add_server);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fmDC = getSupportFragmentManager();
                DialogConnect DialogfmDC = DialogConnect.newInstance(ID_FAB_EVENT, "Add", "",1234, false);
                DialogfmDC.show(fmDC, "fragment_connect");

            }
        });

    }


    public void bindView() {
        try {

            adapter.notifyItemInserted(servers.size());
            //adapter.notifyDataSetChanged();
            //recyclerView.setSelectionFromTop(adapter.getCount(), 0);
            recyclerView.scrollToPosition(adapter.getItemCount());
        } catch (Exception e) {

        }
    }

    public void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(R.string.options_client);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_settings, menu);
        //getFragmentManager().beginTransaction()
        //      .add(android.R.id.content, new FragmentSettings())
        //    .commit();

        return true;
    }

    @Override
    public void onBackPressed() {

        /*
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.putExtra(Server.SERVER, server); // pass arbitrary data to launched activity
        i.putExtra(MainActivity.Activity_Code, code);
        Log.i("INFO", String.valueOf(MainActivity.RESULT_OK));
        */

        intent.putParcelableArrayListExtra("Servers", servers);
        intent.putExtra(Activity_Code, code);
        setResult(RESULT_OK, intent);

        Log.i("INFO", String.valueOf(MainActivity.RESULT_OK));
        //setResult(MainActivity.RESULT_OK, i); // set result code and bundle data for response
        super.onBackPressed();
        finish(); // closes the activity, pass data to parent
    }

    /**
     * Handle click on action bar
     **/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            // case R.id.action_options:

            //return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onDialogConnectClick(String id, String ip, int port, boolean connect) {

        Server server =  new Server(0, "", ip, port);

        switch (id){
            case ID_FAB_EVENT:
                 //Agregamos un servidor a la lista y actulizamos la vista
                 servers.add(server);
                 bindView();
               break;

            case ID_ITEM_EVENT:
                intent.putExtra(Server.SERVER, server);
                code=ServersActivity.CONNECT;
                onBackPressed();
               break;

             default:
                 break;
        }


    }

    @Override
    public void onDialogGenericOkClick(int position) {
        servers.remove(position);
        adapter.notifyDataSetChanged();
        Log.i("INFO", "Delete " + position);
    }
}
