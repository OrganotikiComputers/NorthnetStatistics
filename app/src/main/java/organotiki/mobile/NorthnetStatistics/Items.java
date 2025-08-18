package organotiki.mobile.NorthnetStatistics;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import organotiki.mobile.NorthnetStatistics.objects.GlobalVar;
import organotiki.mobile.NorthnetStatistics.objects.Item;
import organotiki.mobile.NorthnetStatistics.objects.User;


public class Items extends AppCompatActivity implements View.OnClickListener, Communicator {
    Realm realm;
    GlobalVar gVar;
    VolleyRequests request;
    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    Double total;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.activity_items);

            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();

            Toolbar toolbar = findViewById(R.id.itemsBar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        try {
            getMenuInflater().inflate(R.menu.items_menu, menu);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
        return true;
    }

    @Override
    public void respondTotalChanged() {
        try {

        }catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }



    @Override
    public void respondVolleyRequestFinished(Integer position, JSONObject jsonObject) {
        switch (position) {
            case 0:

                break;
            case 1:

                break;
            case 2:

                break;
            case 4:

                break;


        }
    }

    @Override
    public void onClick(View v) {

    }
}