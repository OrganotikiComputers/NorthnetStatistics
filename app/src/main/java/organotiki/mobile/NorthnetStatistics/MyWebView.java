package organotiki.mobile.NorthnetStatistics;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;

import organotiki.mobile.NorthnetStatistics.objects.GlobalVar;


public class MyWebView extends AppCompatActivity implements  Communicator {


    Realm realm;
    GlobalVar gVar;
    String code;
    private ProgressBar progressBar;
    private boolean isPageLoadedComplete = false;
    WebView mWebView;
    Timer myTimer;
    String link;
    VolleyRequests request;
    String[] emaillist;
    AlertDialog mAlertDialog;
    int fromYear=0, fromMonth=0, fromDay=0, toYear=0, toMonth=0, toDay=0;
    private long mLastClickTime = 0;
    Button btn_email;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        try {
            super.onCreate(savedInstanceState);
            this.getWindow().requestFeature(Window.FEATURE_PROGRESS);
            setContentView(R.layout.activity_mywebview);

            realm = MyApplication.getRealm();
            gVar = realm.where(GlobalVar.class).findFirst();

            Intent intent = getIntent();

            Realm realm = MyApplication.getRealm();
            GlobalVar gVar = realm.where(GlobalVar.class).findFirst();

            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
                progressBar.setVisibility(View.VISIBLE);
            }

            btn_email=findViewById(R.id.btn_email);
            btn_email.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doEmail();
                }
            });

            mWebView = (WebView) findViewById(R.id.mWebView);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setBuiltInZoomControls(true);
            mWebView.getSettings().setDisplayZoomControls(false);
            mWebView.getSettings().setLoadWithOverviewMode(true);
            mWebView.canGoBack();

            progressBar.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);

//            fileLink ="http://192.168.0.34:8008/PDF/?Code="+cusCode+"&From="+fromDay+"-"+fromMonth+"-"+fromYear+"&To="+toDay+"-"+toMonth+"-"+toYear;
            link =  (isLocalIPReachable() ? gVar.getLocalIP() : gVar.getOnlineIP()) +"MobStoreService/GetMobOnlineReport";
            request = new VolleyRequests();

            myTimer = new Timer();
            //Start this timer when you create you task
            myTimer.schedule(new loaderTask(), 90000); // 60000 is delay in millies

            mWebView.setWebViewClient(new WebViewClient() {

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    try {

                        if (Uri.parse(url).getHost().equals(link)) {
                            // This is my web site, so do not override; let my WebView load the page
                            return false;
                        }
                        // Otherwise, the fileLink is not for a page on my site, so launch another Activity that handles URLs
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                    return true;
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    try {
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
                            progressBar.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    try {
                        // TODO Auto-generated method stub
                        super.onPageFinished(view, url);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(MyWebView.this, "Η εργασία ολοκληρώθηκε.", Toast.LENGTH_SHORT).show();

                        isPageLoadedComplete = true;
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }

                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    handler.proceed();
                }

                @Override
                public void onReceivedError(WebView view, int errorCod, String description, String failingUrl) {
                    try {
                        Toast.makeText(MyWebView.this, "Your Internet Connection May not be active Or " + description, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }
            });

            Log.d("asdfg", link);
            mWebView.loadUrl(link);


        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.mywebview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        try {
//            switch (item.getItemId()) {
//                case R.id.menuItem_send_email:
//                    try {
//                        request.getCustomerEmails(MyWebView.this, customer);
//                    } catch (Exception e) {
//                        Log.e("asdfg", e.getMessage(), e);
//                    }
//                    break;
//                case android.R.id.home:
//                    finish();
//            }
//        } catch (Exception e) {
//            Log.e("asdfg", e.getMessage(), e);
//        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
        myTimer.cancel();
    }

    private void doEmail() {
        try {
//            String message = gVar.getMyFInvoice().getMyAddress().getEmail()== null || gVar.getMyFInvoice().getMyAddress().getEmail().equals("")?"Σε ποιο e-mail θέλετε να σταλεί η είσπραξη;":"Η είσπραξη θα σταλεί στο : "+gVar.getMyFInvoice().getMyAddress().getEmail()+"\nΑν θέλετε να σταλεί και κάπου αλλού προσθέστε το λογαριασμό e-mail.";
            String message = "Σε ποιo e-mail θέλετε να σταλθούν τα στοιχεία του πελάτη;";
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MyWebView.this);

            LinearLayout layout = new LinearLayout(MyWebView.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setMinimumWidth(LinearLayout.LayoutParams.MATCH_PARENT);
            layout.setPadding(10, 0, 10, 0);

            final EditText input1 = new EditText(MyWebView.this);
            final EditText input2 = new EditText(MyWebView.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);

            input1.setLayoutParams(lp);
            input1.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            input1.setHint(R.string.email);


            Log.d("asdfg", Arrays.toString(emaillist));
            input2.setLayoutParams(lp);
            input2.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            input2.setHint(R.string.email);

            layout.addView(input1);
            layout.addView(input2);
            alertDialog.setView(layout);

            alertDialog.setPositiveButton("Αποστολή", null);
            alertDialog.setNegativeButton("Άκυρο", null);

            alertDialog.setMessage(message);
            alertDialog.setTitle(R.string.app_name);


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
            mAlertDialog.show();
            mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try { if (SystemClock.elapsedRealtime() - mLastClickTime > 1500) {
                        mLastClickTime = SystemClock.elapsedRealtime();
                        if (!(isEmailValid(input1.getText()) || isEmailValid(input2.getText()))) {
                            Toast.makeText(MyWebView.this, "Πρέπει να συμπληρώσετε μια σωστή διεύθυνση.", Toast.LENGTH_LONG).show();
                        } else {
                            link = "MobStoreService/GetOnlineReportEmail";

                        }
                    }
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }


    @Override
    public void respondTotalChanged() {

    }


    @Override
    public void respondVolleyRequestFinished(Integer position, JSONObject jsonArray) {
        boolean isSaved;
//        Log.d("asdfg", String.valueOf(jsonArray));
        switch (position) {
            case 0:
                try {
                    JSONArray array = jsonArray.getJSONArray("GetCustomerEmailsResult") ;
                    int l = array.length();
                    emaillist = new String[l];
                    if (l>0) {
                        for (int i=0;i<l; i++){
                            emaillist[i] = String.valueOf(array.get(i));
                        }
                    }
                    doEmail();
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
            case 1:
                try {
//                    Log.d("asdfg", jsonArray.toString());
                    isSaved = jsonArray.getBoolean("OnlineEmailResult");
                    if (isSaved) {
                        Toast.makeText(MyWebView.this, "Το e-mail στάλθηκε επιτυχώς.", Toast.LENGTH_LONG).show();
                        mAlertDialog.dismiss();
//                        finish();
                    } else {
                        Toast.makeText(MyWebView.this, "Το e-mail δεν στάλθηκε. Προσπαθήστε ξανά.", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                break;
        }
    }

    private boolean isLocalIPReachable() {
        boolean exists = false;

        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);
            final String[] sParts = gVar.getLocalIP().split(":");
            final int l = sParts.length;
            if (l > 1) {
                int port = Integer.parseInt(sParts[l - 1].replace("/", ""));
                String ip = sParts[l - 2].replace("/", "");
                Log.d("asdfg", "IP: " + ip);
                Log.d("asdfg", "Port: " + port);
                SocketAddress sockaddr = new InetSocketAddress(ip, port);
                // Create an unbound socket
                Socket sock = new Socket();

                int timeoutMs = 500;   // 2 seconds
                sock.connect(sockaddr, timeoutMs);
                exists = true;
            }
        } catch (Exception e) {
            //Log.e("asdfg", e.getMessage(), e);
        }
        Log.d("asdfg", "LocalIP found:" + exists);
        return exists;
    }
    /**
     * This class is invoke when you times up.
     */
    class loaderTask extends TimerTask {
        public void run() {
            try {
                System.out.println("Times Up");
                if (!isPageLoadedComplete) {
                    Log.d("asdfg", "hi");
                    /*if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                        progressDialog = null;
                        mWebView.setEnabled(true);
                    }*/
                    //show error message as per you need.
                    MyWebView.this.runOnUiThread(new Runnable() {
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(MyWebView.this, "Η ιστοσελίδα δεν είναι διαθέσιμη", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }
        }
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
