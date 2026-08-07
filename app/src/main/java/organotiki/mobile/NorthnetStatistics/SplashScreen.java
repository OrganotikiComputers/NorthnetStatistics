package organotiki.mobile.NorthnetStatistics;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmList;
import organotiki.mobile.NorthnetStatistics.objects.GlobalVar;
import organotiki.mobile.NorthnetStatistics.objects.User;


public class SplashScreen extends AppCompatActivity {
    Intent intent = null;
    Realm realm;
    GlobalVar gVar;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    Integer permissionParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_splash_screen);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (SplashScreen.this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    permissionParent = 1;
                    ActivityCompat.requestPermissions(SplashScreen.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }else {
                    proceedToReadFile();
                }
            }else {
                proceedToReadFile();
            }
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    public void proceedToReadFile(){
        try {
            final String str = readFromFile("Server.txt");
            final String parts[] = str.split("\\*");
            final int l = parts.length;
            Log.d("asdfg", str + " parts: " + l);
            realm = Realm.getDefaultInstance();

            final String versionNumber = "1.1.1.4";

            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    if (realm.where(GlobalVar.class).count() == 0) {
                        Calendar cal = Calendar.getInstance();
                        cal.set(2020, 0, 1, 0, 0, 0);
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        String date0 = df.format(cal.getTime());
                        Log.d("asdfg", "number of parts: " + l);
                        GlobalVar g = new GlobalVar(UUID.randomUUID().toString(), l > 0 ? parts[0] : "", l > 1 ? parts[1] : "");
                        g.setVerNum(versionNumber);
                        g.setLastUpdate(date0);
                        gVar = realm.copyToRealmOrUpdate(g);
                    } else {
                        gVar = realm.where(GlobalVar.class).findFirst();
                        gVar.setLocalIP(l > 0 ? parts[0] : "");
                        gVar.setOnlineIP(l > 1 ? parts[1] : "");
                        gVar.setVerNum(versionNumber);
                    }
                    User user = new User("user", "user", "user", "user","user",true);
                    realm.copyToRealmOrUpdate(user);

                    gVar.setMyUser(realm.where(User.class).equalTo("ID","user").findFirst());
                }
            });


            intent = new Intent(SplashScreen.this, MainScreen.class);

            final ImageView iv = findViewById(R.id.logo);
            final Animation an = AnimationUtils.loadAnimation(getBaseContext(), R.anim.breathing);

            iv.startAnimation(an);
            an.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //finish();
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }catch (Exception ex){
            Log.e("asdfg", ex.toString());
        }
    }

    private String readFromFile(String filename) {

        String ret = "";
        FileInputStream inputStream;
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/NorthnetStatistics/" + filename);
            if (file.exists()) {
                inputStream = new FileInputStream(file);
            } else {
                inputStream = getApplicationContext().getAssets().openFd(filename).createInputStream();
            }
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
            if (!file.exists()) {
                writeToFile(ret);
            }
        } catch (FileNotFoundException e) {
            Log.e("asdfg", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("asdfg", "Can not read file: " + e.toString());
        }

        return ret;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                proceedToReadFile();
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied to read storage", Toast.LENGTH_SHORT).show();
                proceedToReadFile();
            }
        }
    }
    private void writeToFile(String data) {
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/NorthnetStatistics/", "Server.txt");
            FileOutputStream stream = new FileOutputStream(file);
//            FileOutputStream stream = getApplicationContext().getAssets().openFd("Server.txt").createOutputStream();
            try {
                stream.write(data.getBytes());
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
