package organotiki.mobile.NorthnetStatistics;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

import io.realm.Realm;
import io.realm.RealmResults;
import organotiki.mobile.NorthnetStatistics.objects.GlobalVar;
import organotiki.mobile.NorthnetStatistics.objects.Item;

public class Sync extends AppCompatActivity implements Communicator {

    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    RelativeLayout relativeLayoutIPs;
    Button btnSave, btnSyncCusItem;
    TextView txvDeviceID;
    EditText localIP, onlineIP;
    Realm realm;
    GlobalVar gVar;
    //VolleyRequests request = new VolleyRequests();
    private long mLastClickTime;
    SyncMessagesFragment frag;
    AlertDialog mAlertDialog;
    Integer permissionParent;
    CheckBox cbxSyncImages;
    //int iOrientation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);

        try {
            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();
            Toolbar toolbar = findViewById(R.id.syncBar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            mLastClickTime = 0;
            txvDeviceID = findViewById(R.id.textView_deviceID);
            String textDeviceID = "Ο κωδικός της συσκευής είναι: " + Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            txvDeviceID.setText(textDeviceID);

            relativeLayoutIPs = findViewById(R.id.relativeLayout_IPs);
            if (!(gVar.getMyUser().getID().equals("user"))) {
                relativeLayoutIPs.setVisibility(View.GONE);
            }



            btnSave = findViewById(R.id.button_save);
            btnSave.setTransformationMethod(null);
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime > 1500) {
                        mLastClickTime = SystemClock.elapsedRealtime();
                        try {
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    gVar.setLocalIP(String.valueOf(localIP.getText()));
                                    gVar.setOnlineIP(String.valueOf(onlineIP.getText()));
                                }
                            });
                            if (Build.VERSION.SDK_INT < 23) {
                                // your code
                                writeToFile(localIP.getText() + "*" + onlineIP.getText());
                                {
                                    Toast.makeText(Sync.this, "Η διεύθυνση αποθηκεύτηκε", Toast.LENGTH_SHORT).show();
                                }
                            }else {
                                if (Sync.this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    permissionParent = 1;
                                    ActivityCompat.requestPermissions(Sync.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                                }else{
                                    writeToFile(localIP.getText() + "*" + onlineIP.getText());
                                    {
                                        Toast.makeText(Sync.this, "Η διεύθυνση αποθηκεύτηκε", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }
                }
            });

            btnSyncCusItem = findViewById(R.id.button_syncCustomerItem);
            btnSyncCusItem.setTransformationMethod(null);
            btnSyncCusItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1500) {

                    } else {
                        mLastClickTime = SystemClock.elapsedRealtime();
                        try {
                            if (Build.VERSION.SDK_INT < 23) {
                                // your code

                                /*iOrientation=getRequestedOrientation();
                                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);*/
                                FragmentManager manager = getFragmentManager();
                                frag = new SyncMessagesFragment();
                                frag.setParentButton(0);
                                frag.show(manager, "Sync Messages Fragment");
                            }else {
                                if (Sync.this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    permissionParent = 0;
                                    ActivityCompat.requestPermissions(Sync.this,
                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                                }else{
                                    /*iOrientation=getRequestedOrientation();
                                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);*/
                                    FragmentManager manager = getFragmentManager();
                                    frag = new SyncMessagesFragment();
                                    frag.setParentButton(0);
                                    frag.show(manager, "Sync Messages Fragment");
                                }
                            }

                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
                    }
                }
            });

            cbxSyncImages = findViewById(R.id.checkBox_syncImages);
            onlineIP = findViewById(R.id.editText_online_IP);
            onlineIP.setText(gVar.getOnlineIP());
            localIP = findViewById(R.id.editText_local_IP);
            localIP.setText(gVar.getLocalIP());
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(Sync.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    public boolean SyncImages(){
        return cbxSyncImages.isChecked();
    }


    private boolean writeToFile(String data) {
        try {
            File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/NorthnetStatistics/");
            boolean success = true;
            if (!folder.exists()) {
                success=folder.mkdir();
            }
            if (success) {
                // Do something on success
            } else {
                Toast.makeText(Sync.this, getString(R.string.noDirectoryCreated, Environment.getExternalStorageDirectory().getAbsolutePath() + "/NorthnetStatistics/"), Toast.LENGTH_SHORT).show();
                return false;
            }
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/NorthnetStatistics/", "Server.txt");
            FileOutputStream stream = new FileOutputStream(file);
            try {
                stream.write(data.getBytes());
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
                return false;
            } finally {
                stream.close();
            }
            return true;
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString(),e);
            return false;
        }
    }



    @Override
    public void respondTotalChanged() {

    }


    @Override
    public void respondVolleyRequestFinished(Integer position, JSONObject jsonArray) {

        try {
            String message = jsonArray.getString("Message");
            frag.addNewMessage(message);
            //frag.enableButton();
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
        switch (position) {
            case 0:
                try {

                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case 1:
                //setRequestedOrientation(iOrientation);
                try {
                    //if(!SyncImages())
                        frag.enableButton();
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case 2:
                try{
                    if(SyncImages()){
                        /*frag.addNewMessage("Ξεκινά η αντιστοίχιση εικόνων στα είδη!");
                        frag.content();*/
                        frag.enableButton();
                    }else{
                        frag.enableButton();
                    }
                }catch (Exception e){
                    Log.e("asdfg",e.getMessage(),e);
                }
                break;
            case 5:
                frag.BeginSyncAfterLicence();
                break;
            case 6:
                try {
                    if(!SyncImages())frag.enableButton();
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (permissionParent==0){
                        FragmentManager manager = getFragmentManager();
                        frag = new SyncMessagesFragment();
                        frag.setParentButton(0);
                        frag.show(manager, "Sync Messages Fragment");
                    }else {
                        writeToFile(localIP.getText() + "*" + onlineIP.getText());
                        {
                            Toast.makeText(Sync.this, "Η διεύθυνση αποθηκεύτηκε", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {


                }
            }

        }
    }

}
