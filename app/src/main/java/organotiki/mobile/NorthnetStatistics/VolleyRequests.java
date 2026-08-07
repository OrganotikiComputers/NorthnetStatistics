package organotiki.mobile.NorthnetStatistics;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

import io.realm.Realm;

import io.realm.RealmList;
import io.realm.RealmResults;
import organotiki.mobile.NorthnetStatistics.objects.DynamicRealmObject;
import organotiki.mobile.NorthnetStatistics.objects.GlobalVar;
import organotiki.mobile.NorthnetStatistics.objects.Item;
import organotiki.mobile.NorthnetStatistics.objects.KeyValue;
import organotiki.mobile.NorthnetStatistics.objects.User;


import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;
import org.json.JSONException;
import org.w3c.dom.*;
import javax.xml.parsers.*;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.UUID;
public class VolleyRequests {
    private int jsonSize = 1000;
    Realm realm = MyApplication.getRealm();
    GlobalVar gVar = realm.where(GlobalVar.class).findFirst();
    private Context mContext;
    private Communicator comm;
    private RequestQueue requestQueue;
    ArrayList<Item> items;
    Integer parent;

    //region authenticate the Device and check the Version of the Application
    void sendAuthenticationRequest(Context context) {
        try {

            parent = 0;
            comm = (Communicator) context;
            mContext = context;
            HashMap<String, String> params = new HashMap<>();
            String deviceId = android.provider.Settings.Secure.getString(mContext.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

            String url = (isLocalIPReachable() ? gVar.getLocalIP() : gVar.getOnlineIP()) + "MobStoreService/GetMobDevice?DevID=" + deviceId + "&VerNum=" + gVar.getVerNum();

            Log.d("asdfg", url);
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                VolleyLog.v("asdfg", response.toString(4));
                                Log.d("asdfg", String.valueOf(response));
                                checkAuthentication(response);
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    JSONObject object = new JSONObject();
                    try {
                        object.put("Message", "Ο Διακομιστής δεν ανταποκρίθηκε.");
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                    comm.respondVolleyRequestFinished(1, object);
//                    Toast.makeText(mContext, "\tΟ Διακομιστής δεν ανταποκρίθηκε.", Toast.LENGTH_LONG).show();
                    VolleyLog.e("asdfg", error.getMessage());
                    Log.e("asdfg", error.getMessage(), error);
                }
            });
            req.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(mContext);
                Log.d("asdfg", "Requesting new queue");
            }
            requestQueue.add(req);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    private void checkAuthentication(JSONObject json) {
        try {
            String authentication;
            JSONObject object = new JSONObject();
            if (!json.isNull("VerifyDeviceResult")) {
                authentication = json.getString("VerifyDeviceResult");
                final String[] sParts = authentication.split("\\.");
                final int l = sParts.length;
                if (l == 5) {
                    if (sParts[0].equals("1")) {
                        String[] mParts = gVar.getVerNum().split("\\.");
                        if (sParts[1].equals(mParts[0]) && sParts[2].equals(mParts[1]) && sParts[3].equals(mParts[2])) {
                            if (sParts[4].equals(mParts[3])) {
                                object.put("Message", "H συσκευή είναι έτοιμη για χρήση.\nΠαρακαλώ περιμένετε...");
                                comm.respondVolleyRequestFinished(0, object);
                                FillBackOfficeCombobox(mContext,"Z_VW_org_tablet_prom2");
                                /*if (mContext instanceof Sync) {
                                    sendVolleyRequest("MobStoreService/GetMobDocParDev?DevID=" + android.provider.Settings.Secure.getString(mContext.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID), "");
                                }*/
                            } else {
                                object.put("Message", "Βρέθηκε νεότερη έκδοση της εφαρμογής.\nΠαρακαλώ εγκαταστήστε την όσο το δυνατόν συντομότερο.\nΠαρακαλώ περιμένετε...");
                                comm.respondVolleyRequestFinished(0, object);
                                if (mContext instanceof Sync) {
                                    //sendVolleyRequest("MobStoreService/GetMobItem", "0");
                                    //sendVolleyRequest("MobStoreService/GetMobUser", "0");
                                    FillBackOfficeCombobox(mContext,"Z_VW_org_tablet_prom2");
                                }
                            }
                        } else {
                            object.put("Message", "Βρέθηκε νεότερη έκδοση της εφαρμογής.\nΠαρακαλώ εγκαταστήστε την όσο το δυνατόν συντομότερο.\nΠαρακαλώ περιμένετε...");
                            //object.put("Message", "Βρέθηκε νεότερη έκδοση της εφαρμογής που πρέπει να εγκατασταθεί άμεσα.\nΠαρακαλώ εγκαταστήστε την αφού συγχρονίσετε τα παραστατικά και προσπαθείστε ξανά");
                            comm.respondVolleyRequestFinished(0, object);
                            if (mContext instanceof Sync) {
                                //sendVolleyRequest("MobStoreService/GetMobItem", "0");
                               // sendVolleyRequest("MobStoreService/GetMobUser", "0");
                                FillBackOfficeCombobox(mContext,"Z_VW_org_tablet_prom2");
                            }
                        }
                    } else {
                        object.put("Message", "H συσκευή δεν είναι δηλωμένη.");
                        comm.respondVolleyRequestFinished(1, object);
                    }
                } else {
                    object.put("Message", "Υπήρξε κάποιο πρόβλημα με το Διακομιστή.");
                    comm.respondVolleyRequestFinished(1, object);
                }
            } else {
                object.put("Message", "Υπήρξε κάποιο πρόβλημα με το Διακομιστή.");
                comm.respondVolleyRequestFinished(1, object);
            }
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
    //endregion

    //region Send Simple Requests
    void sendRequest(Context context, String requestText, String requeststring) {
        comm = (Communicator) context;
        mContext = context;
        sendVolleyRequest(requestText, requeststring);
    }

    private void sendVolleyRequest(final String request, final String n) {
        try {
            HashMap<String, String> params = new HashMap<>();
            String url = (isLocalIPReachable() ? gVar.getLocalIP() : gVar.getOnlineIP()) + request;
            url += n.equals("") ? "" : "?Row=" + n + "&Date=" + gVar.getLastUpdate();
            Log.d("asdfg", url);
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (request.contains("GetMobItem")) {
                                    final JSONArray jsonArray = response.getJSONArray("GetItemsResult");
                                    if (jsonArray.length() > 0) {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                try {
                                                    realm.createOrUpdateAllFromJson(Item.class, jsonArray);
                                                } catch (Exception e) {
                                                    Log.e("asdfg", e.getMessage(), e);
                                                }
                                                Log.d("asdfg", "number of items: " + realm.where(Item.class).count());
                                            }
                                        });
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("Message", "Περάστηκαν τα πρώτα " + (Integer.parseInt(n) + jsonSize) + " είδη.");
                                        comm.respondVolleyRequestFinished(0, jsonObject);
                                        sendVolleyRequest("MobStoreService/GetMobItem", (String.valueOf(Integer.parseInt(n) + jsonSize)));
                                    } else {
                                        Calendar cal = Calendar.getInstance();
                                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        final String today = df.format(cal.getTime());
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                gVar.setLastUpdate(today);
                                            }
                                        });
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("Message", "Ο συγχρονισμός των ειδών ολοκληρώθηκε.");
                                        comm.respondVolleyRequestFinished(0, jsonObject);
                                        //FillBackOfficeCombobox(mContext);
                                        sendVolleyRequest("MobStoreService/GetMobUser", "");
                                    }
                                }
                                else if (request.contains("GetMobUser")) {
                                    final JSONArray jsonArray = response.getJSONArray("GetUsersResult");
                                    if (jsonArray.length() > 0) {
                                        realm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                try {
                                                    Log.d("asdfg", "number of Users: " + realm.where(User.class).count());
                                                    realm.createOrUpdateAllFromJson(User.class, jsonArray);
                                                } catch (Exception e) {
                                                    Log.e("asdfg", e.getMessage(), e);
                                                }
                                                Log.d("asdfg", "number of Users: " + realm.where(User.class).count());
                                            }
                                        });
                                    }
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("Message", "Ο συγχρονισμός των χρηστών ολοκληρώθηκε.");
                                    //comm.respondVolleyRequestFinished(0, jsonObject);
                                    try {
                                        if(((Sync) mContext).SyncImages()){
                                            comm.respondVolleyRequestFinished(0, jsonObject);
                                            AllImageDownload();
                                        }else{
                                            comm.respondVolleyRequestFinished(0, jsonObject);
                                            FillBackOfficeCombobox(mContext,"Z_VW_org_tablet_prom2");
                                        }
                                    } catch (Exception ex) {
                                        Log.e("asdfg", ex.getMessage(), ex);
                                    }

                                }


                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);

                                try {
                                    JSONObject object = new JSONObject();
                                    object.put("Message", mContext.getString(R.string.serverNotResponding));
                                    comm.respondVolleyRequestFinished(2, object);
                                } catch (Exception ex) {
                                    Log.e("asdfg", ex.getMessage(), ex);
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        JSONObject object = new JSONObject();
                        object.put("Message", "Ο Διακομιστής δεν ανταποκρίθηκε.");
                        if (error instanceof TimeoutError){
                            Log.e("TEST", "timeout");
                            Log.e("TEST",String.valueOf(error));
                        }
                        comm.respondVolleyRequestFinished(1, object);
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
//                    Toast.makeText(mContext, "\tΟ Διακομιστής δεν ανταποκρίθηκε.", Toast.LENGTH_LONG).show();
                    VolleyLog.e("Error: ", error.getMessage());
                }
            });
            req.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 50000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 50000;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {

                }
            });
            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(mContext);
                Log.d("asdfg", "Requesting new queue");
            }
            requestQueue.add(req);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);

            try {
                JSONObject object = new JSONObject();
                object.put("Message", mContext.getString(R.string.serverNotResponding));
                comm.respondVolleyRequestFinished(1, object);
            } catch (Exception ex) {
                Log.e("asdfg", ex.getMessage(), ex);
            }
        }
    }
    //endregion

    private void getImages() {
        try {
            items = new ArrayList<>();
            items.addAll(realm.where(Item.class).findAll());
            imageDownload(0);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
    public void imageDownload(final int i) {
        try {
            final int c = items.size() - 1;
//            Log.d("asdfg", "c = "+String.valueOf(c));
            final String cusCode = items.get(i).getCode();
            Log.d("asdfg", cusCode);
            final File myImageFile = new File(Environment.getExternalStorageDirectory().getPath() + "/NorthnetStatistics/Images/" + cusCode + ".jpg");

            BasicImageDownloader downloader = new BasicImageDownloader(new BasicImageDownloader.OnImageLoaderListener() {
                @Override
                public void onError(BasicImageDownloader.ImageError error) {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("Message", "Δεν βρέθηκε καινούργια η εικόνα του είδους " + items.get(i).getCode() + " - " + items.get(i).getDescription() + ".");
                        comm.respondVolleyRequestFinished(0, jsonObject);
                        Log.e("asdfg", error.getMessage(), error);
                        if (i < c) {
                            Log.d("asdfg", String.valueOf(i));
                            imageDownload(i + 1);
                        } else {
                            //Toast.makeText(mContext, "Ο συγχρονισμός των εικόνων ολοκληρώθηκε.", Toast.LENGTH_SHORT).show();
                            jsonObject = new JSONObject();
                            jsonObject.put("Message", "Ο συγχρονισμός των εικόνων ολοκληρώθηκε.");
                            comm.respondVolleyRequestFinished(0, jsonObject);
                           getLogo();
                        }
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }

                @Override
                public void onProgressChange(int percent) {

                }

                @Override
                public void onComplete(Bitmap result) {
                    /* save the image - I'm gonna use JPEG */
                    final Bitmap.CompressFormat mFormat = Bitmap.CompressFormat.JPEG;
                    /* don't forget to include the extension into the file cusName */
                    File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/NorthnetStatistics/Images/");
                    boolean success = true;
                    if (!folder.exists()) {
                        success = folder.mkdirs();
                    }
                    if (success) {
                        // Do something on success
                    } else {
                        Toast.makeText(mContext, mContext.getString(R.string.noDirectoryCreated, Environment.getExternalStorageDirectory().getAbsolutePath() + "/NorthnetStatistics/Pictures/"), Toast.LENGTH_SHORT).show();
                    }
//                    if (!myImageFile.exists()){

                    BasicImageDownloader.writeToDisk(myImageFile, result, new BasicImageDownloader.OnBitmapSaveListener() {
                        @Override
                        public void onBitmapSaved() {
                            try {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("Message", "Αποθηκεύτηκε η εικόνα του είδους " + items.get(i).getCode() + " - " + items.get(i).getDescription() + ".");
                                comm.respondVolleyRequestFinished(0, jsonObject);
                                //Toast.makeText(Sync.this, "Image saved as: " + myImageFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                                Log.d("asdfg", "Image saved as: " + myImageFile.getAbsolutePath());
                                if (i < c) {
                                    Log.d("asdfg", String.valueOf(i));
                                    imageDownload(i + 1);
                                } else {
                                    //Toast.makeText(mContext, "Ο συγχρονισμός των εικόνων ολοκληρώθηκε.", Toast.LENGTH_SHORT).show();
                                    jsonObject = new JSONObject();
                                    jsonObject.put("Message", "Ο συγχρονισμός των εικόνων ολοκληρώθηκε.");
                                    comm.respondVolleyRequestFinished(0, jsonObject);
                                   getLogo();                          }
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }

                        @Override
                        public void onBitmapSaveError(BasicImageDownloader.ImageError error) {
                            try {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("Message", "Δεν αποθηκεύτηκε η εικόνα του είδους " + items.get(i).getCode() + " - " + items.get(i).getDescription() + ".");
                                comm.respondVolleyRequestFinished(0, jsonObject);
                                Log.e("asdfg", error.getMessage(), error);
                                if (i < c) {
                                    Log.d("asdfg", String.valueOf(i));
                                    imageDownload(i + 1);
                                } else {
                                    //Toast.makeText(mContext, "Ο συγχρονισμός των εικόνων ολοκληρώθηκε.", Toast.LENGTH_SHORT).show();
                                    jsonObject = new JSONObject();
                                    jsonObject.put("Message", "Ο συγχρονισμός των εικόνων ολοκληρώθηκε.");
                                    comm.respondVolleyRequestFinished(0, jsonObject);
                                   getLogo();
                                }
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    }, mFormat, true);
                }
            });
            if (myImageFile.exists()) {
                Date lastModified = new Date(myImageFile.lastModified());
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String date = df.format(lastModified);
                Log.d("asdfg", (isLocalIPReachable() ? gVar.getLocalIP() : gVar.getOnlineIP()) + "MobStoreService/ItemImage?Code=" + items.get(i).getCode() + "&Date=" + date);
                downloader.download((isLocalIPReachable() ? gVar.getLocalIP() : gVar.getOnlineIP()) + "MobStoreService/ItemImage?Code=" + items.get(i).getCode() + "&Date=" + date, true);
            } else {
                Calendar cal = Calendar.getInstance();
                cal.set(2010, 0, 1, 0, 0, 0);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String date0 = df.format(cal.getTime());
                Log.d("asdfg", (isLocalIPReachable() ? gVar.getLocalIP() : gVar.getOnlineIP()) + "MobStoreService/ItemImage?Code=" + items.get(i).getCode() + "&Date=" + date0);
                downloader.download((isLocalIPReachable() ? gVar.getLocalIP() : gVar.getOnlineIP()) + "MobStoreService/ItemImage?Code=" + items.get(i).getCode() + "&Date=" + date0, true);
            }
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
    public void AllImage(final ArrayList<String> items,final int i)
    {
        try {

            final int c = items.size() - 1;
            final File myImageFile = new File(Environment.getExternalStorageDirectory().getPath() + "/NorthnetStatistics/Images/" + URLEncoder.encode(items.get(i), "utf-8") + ".jpg");

            BasicImageDownloader downloader = new BasicImageDownloader(new BasicImageDownloader.OnImageLoaderListener() {
                @Override
                public void onError(BasicImageDownloader.ImageError error) {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("Message", "Δεν ήταν δυνατό να αποθηκευτεί η εικόνα " + items.get(i) +".");
                        comm.respondVolleyRequestFinished(0, jsonObject);
                        Log.e("asdfg", error.getMessage(), error);
                        if (i < c) {
                            Log.d("asdfg", String.valueOf(i));
                            AllImage(items,i + 1);
                        } else {
                            //Toast.makeText(mContext, "Ο συγχρονισμός των εικόνων ολοκληρώθηκε.", Toast.LENGTH_SHORT).show();
                            jsonObject = new JSONObject();
                            jsonObject.put("Message", "Ο συγχρονισμός των εικόνων ολοκληρώθηκε.");
                            comm.respondVolleyRequestFinished(0, jsonObject);
                            getLogo();
                        }
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }

                @Override
                public void onProgressChange(int percent) {

                }

                @Override
                public void onComplete(Bitmap result) {
                    /* save the image - I'm gonna use JPEG */
                    final Bitmap.CompressFormat mFormat = Bitmap.CompressFormat.JPEG;
                    /* don't forget to include the extension into the file cusName */
                    File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/NorthnetStatistics/Images/");
                    boolean success = true;
                    if (!folder.exists()) {
                        success = folder.mkdirs();
                    }
                    if (success) {
                        // Do something on success
                    } else {
                        Toast.makeText(mContext, mContext.getString(R.string.noDirectoryCreated, Environment.getExternalStorageDirectory().getAbsolutePath() + "/NorthnetStatistics/Pictures/"), Toast.LENGTH_SHORT).show();
                    }
//                    if (!myImageFile.exists()){
                    final File myImageFile2 = new File(Environment.getExternalStorageDirectory().getPath() + "/NorthnetStatistics/Images/" + items.get(i)+".jpg");

                    BasicImageDownloader.writeToDisk(myImageFile2, result, new BasicImageDownloader.OnBitmapSaveListener() {
                        @Override
                        public void onBitmapSaved() {
                            try {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("Message", "Αποθηκεύτηκε η εικόνα " + items.get(i) +  ".");
                                comm.respondVolleyRequestFinished(0, jsonObject);
                                if (i < c) {
                                    Log.d("asdfg", String.valueOf(i));
                                    AllImage(items,i + 1);
                                } else {
                                    //Toast.makeText(mContext, "Ο συγχρονισμός των εικόνων ολοκληρώθηκε.", Toast.LENGTH_SHORT).show();
                                    jsonObject = new JSONObject();
                                    jsonObject.put("Message", "Ο συγχρονισμός των εικόνων ολοκληρώθηκε.");
                                    comm.respondVolleyRequestFinished(0, jsonObject);
                                    getLogo();
                                }                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }

                        @Override
                        public void onBitmapSaveError(BasicImageDownloader.ImageError error) {
                            try {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("Message", "Δεν αποθηκεύτηκε η εικόνα  " + items.get(i)+ ".");
                                comm.respondVolleyRequestFinished(0, jsonObject);
                                Log.e("asdfg", error.getMessage(), error);
                                if (i < c) {
                                    Log.d("asdfg", String.valueOf(i));
                                    AllImage(items,i + 1);
                                } else {
                                    //Toast.makeText(mContext, "Ο συγχρονισμός των εικόνων ολοκληρώθηκε.", Toast.LENGTH_SHORT).show();
                                    jsonObject = new JSONObject();
                                    jsonObject.put("Message", "Ο συγχρονισμός των εικόνων ολοκληρώθηκε.");
                                    comm.respondVolleyRequestFinished(0, jsonObject);
                                    getLogo();
                                }
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    }, mFormat, true);
                }
            });
            if (myImageFile.exists()) {
                Date lastModified = new Date(myImageFile.lastModified());
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String date = df.format(lastModified);
                Log.d("asdfg", (isLocalIPReachable() ? gVar.getLocalIP() : gVar.getOnlineIP()) + "MobStoreService/ItemImage?Code=" + URLEncoder.encode(items.get(i), "utf-8")+ "&Date=" + date);
                downloader.download((isLocalIPReachable() ? gVar.getLocalIP() : gVar.getOnlineIP()) + "MobStoreService/ItemImage?Code=" + URLEncoder.encode(items.get(i), "utf-8") + "&Date=" + date, true);
            } else {
                Calendar cal = Calendar.getInstance();
                cal.set(2010, 0, 1, 0, 0, 0);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String date0 = df.format(cal.getTime());
                Log.d("asdfg", (isLocalIPReachable() ? gVar.getLocalIP() : gVar.getOnlineIP()) + "MobStoreService/ItemImage?Code=" + URLEncoder.encode(items.get(i), "utf-8") + "&Date=" + date0);
                downloader.download((isLocalIPReachable() ? gVar.getLocalIP() : gVar.getOnlineIP()) + "MobStoreService/ItemImage?Code=" + URLEncoder.encode(items.get(i), "utf-8") + "&Date=" + date0, true);
            }
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }
    public void AllImageDownload() {

           try {

                HashMap<String, String> params = new HashMap<>();
                final JSONObject response2;
                String url = (isLocalIPReachable() ? gVar.getLocalIP() : gVar.getOnlineIP()) + "MobStoreService/AllImages";

                Log.d("asdfg", url);
                JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    VolleyLog.v("asdfg", response.toString(4));
                                    Log.d("asdfg", String.valueOf(response));
                                    JSONArray item = response.getJSONArray("AllImagesResult");
                                    ArrayList<String> items=new ArrayList<String>();
                                    for(int i=0;i<item.length();i++)
                                    {
                                        items.add(item.getString(i));
                                    }
                                    AllImage(items,0);
                                } catch (Exception e) {
                                    Log.e("asdfg", e.getMessage(), e);
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        JSONObject object = new JSONObject();
                        try {
                            object.put("Message", "Ο Διακομιστής δεν ανταποκρίθηκε.");
                        } catch (Exception e) {
                            Log.e("asdfg", e.getMessage(), e);
                        }
//                    Toast.makeText(mContext, "\tΟ Διακομιστής δεν ανταποκρίθηκε.", Toast.LENGTH_LONG).show();
                        VolleyLog.e("asdfg", error.getMessage());
                        Log.e("asdfg", error.getMessage(), error);
                    }
                });
                req.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                if (requestQueue == null) {
                    requestQueue = Volley.newRequestQueue(mContext);
                    Log.d("asdfg", "Requesting new queue");
                }
                requestQueue.add(req);
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }

    }
    //region Check if local IP is responding
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

                // This method will block no more than timeoutMs.
                // If the timeout occurs, SocketTimeoutException is thrown.
                int timeoutMs = 500;   // 2 seconds
                sock.connect(sockaddr, timeoutMs);
                exists = true;
            }
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
        Log.d("asdfg", "LocalIP found:" + exists);
        return exists;
    }

    public static ArrayList<File> getAllFilesInDir(File dir) {
        if (dir == null)
            return null;

        ArrayList<File> files = new ArrayList<File>();

        Stack<File> dirlist = new Stack<File>();
        dirlist.clear();
        dirlist.push(dir);

        while (!dirlist.isEmpty()) {
            File dirCurrent = dirlist.pop();

            File[] fileList = dirCurrent.listFiles();
            for (File aFileList : fileList) {
                if (aFileList.isDirectory())
                    dirlist.push(aFileList);
                else
                    files.add(aFileList);
            }
        }

        return files;
    }

    public void GetLicence(Context context) {
        try {
            comm = (Communicator) context;
            mContext = context;
            HashMap<String, String> params = new HashMap<>();
            String url = isLocalIPReachable()? gVar.getLocalIP() + "MobStoreService/GetMobLicence" : gVar.getOnlineIP() + "MobStoreService/GetMobLicence";
            Log.d("asdfg", url);

            JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d("asdfg", String.valueOf(response));
                                try {
                                    String licence;
                                    if (!response.isNull("GetMobLicenceResult")) {
                                        licence = response.getString("GetMobLicenceResult");
                                        String[] licenceSplit = licence.split("#");

                                        if(licenceSplit[0].equals("0")) {
                                            JSONObject jsonObject = new JSONObject();
                                            jsonObject.put("Message",  licenceSplit[1]);
                                            comm.respondVolleyRequestFinished(5, jsonObject);
                                        }
                                        else{
                                            JSONObject jsonObject = new JSONObject();
                                            jsonObject.put("Message",  licenceSplit[1]);
                                            comm.respondVolleyRequestFinished(6, jsonObject);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e("asdfg", e.getMessage(), e);
                                }
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        JSONObject object = new JSONObject();
                        object.put("Message",  mContext.getString(R.string.serverNotResponding));
                        comm.respondVolleyRequestFinished(6, object);
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }
            });
            req.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(mContext);
                Log.d("asdfg", "Requesting new queue");
            }
            requestQueue.add(req);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    public void getLogo(){
        try {
            final File myImageFile = new File(Environment.getExternalStorageDirectory().getPath() + "/NorthnetStatistics/Images/logo.jpg");
            BasicImageDownloader downloader = new BasicImageDownloader(new BasicImageDownloader.OnImageLoaderListener() {
                public void onError(BasicImageDownloader.ImageError error) {
                    try {
                        JSONObject jsonObject2 = new JSONObject();
                        if (((Sync) mContext).SyncImages()) jsonObject2.put("Message",  "Παρακαλώ περιμένετε...");
                        else  jsonObject2.put("Message",  mContext.getString(R.string.syncCompleted));
                        comm.respondVolleyRequestFinished(2, jsonObject2);
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }

                public void onProgressChange(int percent) {
                }

                public void onComplete(Bitmap result) {
                    Bitmap.CompressFormat mFormat = Bitmap.CompressFormat.JPEG;
                    File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/NorthnetStatistics/Images/");
                    boolean success = true;
                    if (!folder.exists()) {
                        success = folder.mkdirs();
                    }
                    if (!success) {
                        Context access$100 =  mContext;
                        Context access$1002 =  mContext;
                        Toast.makeText(access$100, access$1002.getString(R.string.noDirectoryCreated, new Object[]{Environment.getExternalStorageDirectory().getAbsolutePath() + "/NorthnetStatistics/Images/"}), Toast.LENGTH_SHORT).show();
                    }
                    BasicImageDownloader.writeToDisk(myImageFile, result, new BasicImageDownloader.OnBitmapSaveListener() {
                        public void onBitmapSaved() {
                            try {
                                JSONObject jsonObject2 = new JSONObject();
                                jsonObject2.put("Message",  mContext.getString(R.string.syncCompleted));
                                comm.respondVolleyRequestFinished(2, jsonObject2);
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }

                        public void onBitmapSaveError(BasicImageDownloader.ImageError error) {
                            try {
                                JSONObject jsonObject2 = new JSONObject();
                                jsonObject2.put("Message",  mContext.getString(R.string.syncCompleted));
                                comm.respondVolleyRequestFinished(2, jsonObject2);
                            } catch (Exception e) {
                                Log.e("asdfg", e.getMessage(), e);
                            }
                        }
                    }, mFormat, true);
                }
            });
            if (myImageFile.exists()) {
                String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date(myImageFile.lastModified()));
                StringBuilder sb = new StringBuilder();
                sb.append(isLocalIPReachable() ? this.gVar.getLocalIP() : this.gVar.getOnlineIP());
                sb.append("MobStoreService/ItemImage?Code=");
                sb.append("logo");
                sb.append("&Date=");
                sb.append(date);
                Log.d("asdfg", sb.toString());
                StringBuilder sb2 = new StringBuilder();
                sb2.append(isLocalIPReachable() ? this.gVar.getLocalIP() : this.gVar.getOnlineIP());
                sb2.append("MobStoreService/ItemImage?Code=");
                sb2.append("logo");
                sb2.append("&Date=");
                sb2.append(date);
                downloader.download(sb2.toString(), true);
                return;
            }
            Calendar cal = Calendar.getInstance();
            cal.set(2016, 0, 1, 0, 0, 0);
            String date0 = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
            StringBuilder sb3 = new StringBuilder();
            sb3.append(isLocalIPReachable() ? this.gVar.getLocalIP() : this.gVar.getOnlineIP());
            sb3.append("MobStoreService/ItemImage?Code=");
            sb3.append("logo");
            sb3.append("&Date=");
            sb3.append(date0);
            Log.d("asdfg", sb3.toString());
            StringBuilder sb4 = new StringBuilder();
            sb4.append(isLocalIPReachable() ? this.gVar.getLocalIP() : this.gVar.getOnlineIP());
            sb4.append("MobStoreService/ItemImage?Code=");
            sb4.append("logo");
            sb4.append("&Date=");
            sb4.append(date0);
            downloader.download(sb4.toString(), true);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    void FillBackOfficeCombobox(Context context,String tablename) throws UnsupportedEncodingException {
        mContext = context;
        comm = (Communicator) mContext;
        HashMap<String, String> params = new HashMap<>();
        String columns = URLEncoder.encode("*", "UTF-8");  // results in "%2A"
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = (isLocalIPReachable() ? gVar.getLocalIP() : gVar.getOnlineIP()) + "MobStoreService/FillBackOfficeCombobox?Columns=*&Table=(SELECT * FROM "+tablename+" ) t"; ///den douleve xwris emfolevmeno select,min rwtas giati,rwta tin oracle tou karagianni
        Log.d("asdfg", url + params.toString());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String xml = response.getString("FillBackOfficeComboboxResult");
                        parseAndSaveXML(xml,tablename);
                    } catch (JSONException e) {
                        Log.e("Volley", "JSON parsing error", e);
                    }
                },
                error -> {
                    Log.e("Volley", "Volley error", error);
                }
        );

        request.setRetryPolicy(new DefaultRetryPolicy(
                0, // timeout in milliseconds (0 = no timeout) 60000 for 60 seconds
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, // or 0 for no retries
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        queue.add(request);
    }

  /*  private void parseAndSaveXML(String xmlString,String tablename) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream inputStream = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8));
            Document doc = builder.parse(inputStream);

            NodeList tableNodes = doc.getElementsByTagName("Table");

            Realm realm = Realm.getDefaultInstance();
            realm.executeTransaction(r -> {
                for (int i = 0; i < tableNodes.getLength(); i++) {
                    Element tableElement = (Element) tableNodes.item(i);
                    NodeList children = tableElement.getChildNodes();

                    // DynamicRealmObject obj = r.createObject(DynamicRealmObject.class, tablename);

                    DynamicRealmObject obj;

                    // Check if object exists
                    RealmResults<DynamicRealmObject> results = realm.where(DynamicRealmObject.class).equalTo("ID", tablename).findAll();

                    if (results.isEmpty()) {
                        obj = r.createObject(DynamicRealmObject.class, tablename);
                    } else {
                        obj = results.first();
                    }

                    RealmList<KeyValue> fields = new RealmList<>();

                    for (int j = 0; j < children.getLength(); j++) {
                        Node child = children.item(j);
                        if (child.getNodeType() == Node.ELEMENT_NODE) {
                            String key = child.getNodeName();
                            String value = child.getTextContent();

                            KeyValue kv = r.createObject(KeyValue.class);
                            kv.setKey(key);
                            kv.setValue(value);

                            fields.add(kv);
                        }
                    }

                    obj.setFields(fields);
                }
            });

            realm.close();
            getLogo();

        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }*/

    private void parseAndSaveXML(String xmlString, String tablename) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            InputStream inputStream = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8));

            saxParser.parse(inputStream, new DefaultHandler() {
                String currentKey = null;
                String idValue = null;
                Stack<String> elementStack = new Stack<>();
                Realm realm;
                DynamicRealmObject obj;
                RealmList<KeyValue> fields;
                boolean inTable = false;
                boolean inNewDataSet = false;
                boolean inDataSet = false;
                StringBuilder content = new StringBuilder();

                @Override
                public void startDocument() {
                    realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                }

                @Override
                public void endDocument() {
                    realm.commitTransaction();
                    realm.close();
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("Message", "Ο συγχρονισμός των στατιστικών ολοκληρώθηκε.");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    //comm.respondVolleyRequestFinished(0, jsonObject);
                    /*comm.respondVolleyRequestFinished(0, jsonObject);
                    getLogo();*/
                    if(tablename.equals("Z_VW_org_tablet_prom2")){
                        try {
                            FillBackOfficeCombobox(mContext,"z_vw_org_tablet_bi1");
                        } catch (UnsupportedEncodingException e) {

                        }
                    }else{
                        if(((Sync) mContext).SyncImages()){
                            comm.respondVolleyRequestFinished(0, jsonObject);
                            AllImageDownload();
                        }else{
                            comm.respondVolleyRequestFinished(0, jsonObject);
                            getLogo();
                        }
                    }


                }

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) {
                    content.setLength(0);

                    if (qName.equalsIgnoreCase("diffgr:diffgram")) {
                        inDataSet = true;
                    } else if (inDataSet && qName.equalsIgnoreCase("NewDataSet")) {
                        inNewDataSet = true;
                    } else if (inNewDataSet && qName.equalsIgnoreCase("Table")) {
                        inTable = true;
                        fields = new RealmList<>();
                    } else if (inTable) {
                        elementStack.push(qName);
                        //content.setLength(0);
                    }
                }

                @Override
                public void endElement(String uri, String localName, String qName) {
                    if (inTable) {
                        if (!elementStack.isEmpty() && qName.equals(elementStack.peek())) {
                            String currentTag = elementStack.pop();
                            String value = content.toString().trim();

                            if (currentTag.equalsIgnoreCase("ID")) {
                                idValue = value;
                                idValue = value;
                            }

                            // Δημιουργία KeyValue και αποθήκευση και του κενού string ""
                            KeyValue kv = realm.createObject(KeyValue.class);
                            kv.setKey(currentTag.replace("_x0020_", " "));
                            kv.setValue(value); // ακόμα και αν value είναι "", αποθηκεύουμε

                            fields.add(kv);

                            content.setLength(0);
                        } else if (qName.equalsIgnoreCase("Table")) {
                            if (idValue != null) {
                                DynamicRealmObject existing = realm.where(DynamicRealmObject.class)
                                        .equalTo("ID", idValue)
                                        .findFirst();

                                if (existing == null) {
                                    obj = realm.createObject(DynamicRealmObject.class, idValue);
                                    obj.setFields(fields);
                                } else {
                                    obj = existing;

                                    RealmList<KeyValue> existingFields = obj.getFields();

                                    for (KeyValue newField : fields) {
                                        boolean found = false;

                                        for (KeyValue oldField : existingFields) {
                                            if (oldField.getKey().equals(newField.getKey())) {
                                                oldField.setValue(newField.getValue()); // update υπάρχοντος πεδίου
                                                found = true;
                                                break;
                                            }
                                        }

                                        if (!found) {
                                            KeyValue kv = realm.createObject(KeyValue.class);
                                            kv.setKey(newField.getKey());
                                            kv.setValue(newField.getValue());
                                            existingFields.add(kv);
                                        }
                                    }
                                }
                            }

                            inTable = false;
                            idValue = null;
                        }
                    }
                }

                @Override
                public void characters(char[] ch, int start, int length) {
                    content.append(ch, start, length);
                }
            });

        } catch (Exception e) {
            Log.e("parseAndSaveXML", e.getMessage(), e);
        }
    }


}