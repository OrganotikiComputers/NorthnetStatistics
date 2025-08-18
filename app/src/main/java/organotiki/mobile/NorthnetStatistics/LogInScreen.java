package organotiki.mobile.NorthnetStatistics;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import io.realm.Realm;
import io.realm.RealmQuery;
import organotiki.mobile.NorthnetStatistics.objects.GlobalVar;
import organotiki.mobile.NorthnetStatistics.objects.User;


public class LogInScreen extends AppCompatActivity implements View.OnClickListener {

    EditText username, password;
    Button login, exit,anydesk;
    Intent intent = null;
    Realm realm;
    GlobalVar gVar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_screen);
        // Get a Realm instance for this thread
        realm = Realm.getDefaultInstance();
        gVar = realm.where(GlobalVar.class).findFirst();

        username = (EditText) findViewById(R.id.userName);
        password = (EditText) findViewById(R.id.password);
       /* username.setText("ΓΙΑΝΝΗΣ");
        password.setText("12345");*/
        login = (Button) findViewById(R.id.logIn);
        login.setTransformationMethod(null);
        login.setOnClickListener(this);
        exit = (Button) findViewById(R.id.exit);
        exit.setTransformationMethod(null);
        exit.setOnClickListener(this);
        anydesk = (Button) findViewById(R.id.anydesk);
        anydesk.setTransformationMethod(null);
        anydesk.setOnClickListener(this);
        /*StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        if (isLocalIPReachable()){
            Toast.makeText(LogInScreen.this, "Η τοπική διεύθηνση βρέθηκε.",Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(LogInScreen.this, "Η τοπική διεύθηνση δεν βρέθηκε.",Toast.LENGTH_SHORT).show();
        }*/
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.logIn:
                LogIn();
                break;
            case R.id.exit:
                this.finish();
                /*Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);*/
                break;
            case R.id.anydesk:
                try {
                    Intent intent = getPackageManager().getLaunchIntentForPackage("com.anydesk.anydeskandroid");
                    if (intent != null) {
                        // AnyDesk is installed, so launch it
                        startActivity(intent);
                    } else {
                        // AnyDesk is not installed, you can prompt the user to install it from the Play Store
                        Toast.makeText(this, "Δεν έχετε εγκατεστημένο το AnyDesk", Toast.LENGTH_SHORT).show();
                        Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.anydesk.anydeskandroid"));
                        playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(playStoreIntent);
                    }
                }catch (Exception ex) {
                    Log.e("asdfg", ex.getMessage(), ex);
                }
                break;
        }
    }

    private void LogIn() {
        try {
            if (checkIfExists(String.valueOf(username.getText()))) {
                final User user = realm.where(User.class).equalTo("Username", String.valueOf(username.getText())).findFirst();
                if (user.getPassword().equals(password.getText().toString())) {//.equals(sha1(password.getText() + LogIn.getString("Salt")))) {//
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            gVar.setMyUser(user);
                        }
                    });
                    intent = new Intent(LogInScreen.this, MainScreen.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Λανθασμένος κωδικός\nΠαρακαλώ προσπαθήστε ξανά", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Λανθασμένο όνομα χρήστη\nΠαρακαλώ προσπαθήστε ξανά", Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Log.e("asdfg", ex.getMessage(), ex);
        }
    }

    public boolean checkIfExists(String username) {
        Log.d("asdfg", username);
        RealmQuery<User> query = realm.where(User.class)
                .equalTo("Username", username);
        Log.d("asdfg", String.valueOf(query.count()));
        return query.count() != 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    private boolean isLocalIPReachable(){
        boolean exists = false;

        try {
            final String sParts[] = gVar.getLocalIP().split(":");
            final int l = sParts.length;
            if (l>1) {
                Log.d("asdfg", "IP: "+sParts[0]);
                Log.d("asdfg", "Port: "+sParts[l-1]);
                int port = Integer.parseInt(sParts[l-1].replace("/",""));
                //String ip = gVar.getLocalIP().replace(":"+ String.valueOf(port)+"/", "");
                String ip = sParts[l-2].replace("/","");
                Log.d("asdfg", "IP: "+ip);
                Log.d("asdfg", "Port: "+port);
                SocketAddress sockaddr = new InetSocketAddress(ip, port);
                // Create an unbound socket
                Socket sock = new Socket();

                // This method will block no more than timeoutMs.
                // If the timeout occurs, SocketTimeoutException is thrown.
                int timeoutMs = 2000;   // 2 seconds
                sock.connect(sockaddr, timeoutMs);
                exists = true;
            }
        }catch(Exception e){
            Log.e("asdfg", e.getMessage(), e);
        }
        return exists;
    }
}