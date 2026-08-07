package organotiki.mobile.NorthnetStatistics;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;
import organotiki.mobile.NorthnetStatistics.objects.GlobalVar;
import organotiki.mobile.NorthnetStatistics.objects.User;

public class MainScreen extends AppCompatActivity implements View.OnClickListener, Communicator {
    Button sync,offlinestats,statsbi;
    Realm realm;
    GlobalVar gVar;
    TextView txvUser, txvVersion;
    AlertDialog mAlertDialog;
    AppCompatImageView imageView_customer_logo;
    DrawerLayout drawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        try {
            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();
            Toolbar toolbar = findViewById(R.id.mainScreenBar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            txvUser = findViewById(R.id.textView_user);
            String str = getString(R.string.user_)+ gVar.getMyUser().getFullName();
            txvUser.setText(str);
            if (!getResources().getBoolean(R.bool.isTablet)) {
                ActionBar actionbar = getSupportActionBar();
                actionbar.setHomeAsUpIndicator((int) R.drawable.round_menu_white_48);
                actionbar.setDisplayHomeAsUpEnabled(true);
                this.drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

                drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
                    @Override
                    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                        try {

                            Menu menu=((NavigationView) findViewById(R.id.navigation)).getMenu();


                        }catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }

                    @Override
                    public void onDrawerOpened(@NonNull  View drawerView) {

                    }

                    @Override
                    public void onDrawerClosed(@NonNull  View drawerView) {

                    }

                    @Override
                    public void onDrawerStateChanged(int newState) {

                    }
                });

                ((NavigationView) findViewById(R.id.navigation)).setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        int itemId = menuItem.getItemId();
                        switch (itemId) {
                            case R.id.menu_item_sync:
                                try {
                                    Intent intent = new Intent(MainScreen.this, Sync.class);
                                    startActivity(intent);
                                } catch (Exception e) {
                                    Log.e("asdfg", e.getMessage(), e);
                                }
                                break;
                        }
                        if (MainScreen.this.drawerLayout.isDrawerOpen((int) GravityCompat.START)) {
                            MainScreen.this.drawerLayout.closeDrawer((int) GravityCompat.START);
                        }
                        MainScreen.this.drawerLayout.closeDrawers();
                        return false;
                    }
                });
            }

            imageView_customer_logo=(AppCompatImageView) findViewById(R.id.imageView_customer_logo);

            txvVersion= findViewById(R.id.textView_version);
            str = getString(R.string.version_)+ gVar.getVerNum();
            txvVersion.setText(str);


            sync = findViewById(R.id.button_sync);
            sync.setOnClickListener(this);
            sync.setTransformationMethod(null);

            offlinestats = findViewById(R.id.button_offlinestats);
            offlinestats.setOnClickListener(this);
            offlinestats.setTransformationMethod(null);

            statsbi = findViewById(R.id.button_statsBI);
            statsbi.setOnClickListener(this);
            statsbi.setTransformationMethod(null);

            try{
                Bitmap[] RetVal = new Bitmap[0];
                File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/NorthnetStatistics/Images/");
                String[] list = dir.list(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        if (name.startsWith("logo")) {
                            return true;
                        }
                        return false;
                    }
                });
                Bitmap[] imgs = new Bitmap[list.length];
                if(list!=null){

                    for (int i = 0; i < list.length; i++) {
                        File imgFile = new  File(Environment.getExternalStorageDirectory().getPath() + "/NorthnetStatistics/Images/"+list[i]);
                        imgs[i] = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    }
                }
                if(imgs.length==0){
                    imageView_customer_logo.setImageResource(R.drawable.logoo);
                }
                else{
                    imageView_customer_logo.setImageBitmap(imgs[0]);
                }
            }catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }

            GlobalVar gVar = realm.where(GlobalVar.class).findFirst();
            User user = realm.where(User.class).equalTo("ID", gVar.getMyUser().getID()).findFirst();
            Toast.makeText(this, "Καλωσήλθες  " + user.getFullName(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public void onClick(View v) {
        Intent intent;

        switch (v.getId()) {
            case R.id.button_sync:
                try {
                    intent = new Intent(MainScreen.this, Sync.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case R.id.button_offlinestats:
                try {
                    intent = new Intent(MainScreen.this, StatsKarfwta.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case R.id.button_statsBI:
                try {
                    intent = new Intent(MainScreen.this, StatsBI.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        doExit();
    }

    private void doExit() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                MainScreen.this);

        alertDialog.setPositiveButton("Ναι", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainScreen.this.finishAffinity();
            }
        });

        alertDialog.setNegativeButton("Όχι", null);

        alertDialog.setMessage("Θέλετε να βγείτε από την εφαρμογή;");
        alertDialog.setTitle("Northnet Statistics");
        mAlertDialog = alertDialog.create();
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setTransformationMethod(null);

                Button negativeButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                negativeButton.setTransformationMethod(null);
            }
        });

        mAlertDialog.show();

    }


    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return true;
        }
        this.drawerLayout.openDrawer((int) GravityCompat.START);

        try {

            Menu menu=((NavigationView) findViewById(R.id.navigation)).getMenu();
            MenuItem Order=menu.findItem(R.id.menu_new_order);

        }catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
        return true;
    }


    @Override
    public void respondTotalChanged() {

    }


    @Override
    public void respondVolleyRequestFinished(Integer position, JSONObject jsonObject) {
        try {
            switch (position) {
                case -1:
                    try {
                        String message = jsonObject.getString("Message");
                        Toast.makeText(MainScreen.this, message, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                    break;
            }
        }catch (Exception e){
            Log.e("asdfg", e.getMessage(), e);
        }
    }
}
