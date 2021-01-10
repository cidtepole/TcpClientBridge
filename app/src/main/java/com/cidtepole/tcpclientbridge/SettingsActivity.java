package com.cidtepole.tcpclientbridge;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


import com.cidtepole.tcpclientbridge.data.Tools;
import com.cidtepole.tcpclientbridge.fragment.FragmentSettings;


public class SettingsActivity extends AppCompatActivity {


    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting);

        initToolbar();
        Tools.systemBarLolipop(this);

        // Display the fragment as the main content.
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new FragmentSettings())
                .commit();


    }


    public void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        //actionBar.hide();

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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            //case R.id.action_sample:
                //Snackbar.make(parent_view, item.getTitle() + " Clicked ", Snackbar.LENGTH_SHORT).show();
              //  return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
