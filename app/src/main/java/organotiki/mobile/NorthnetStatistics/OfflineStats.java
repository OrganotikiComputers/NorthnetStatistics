package organotiki.mobile.NorthnetStatistics;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import organotiki.mobile.NorthnetStatistics.objects.DynamicRealmObject;
import organotiki.mobile.NorthnetStatistics.objects.KeyValue;

public class OfflineStats extends AppCompatActivity implements  Communicator{

    private RecyclerView recyclerView;
    private DynamicRealmAdapter adapter;
    private DynamicRealmAdapterGrid adapterGrid;
    EditText search;
    Button searchItem;
    private Realm realm;
    private List<String> keys;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout
        setContentView(R.layout.activity_offlinestats);

        // Init Realm
        Realm.init(this);
        realm = Realm.getDefaultInstance();
        buildImageCache();
        // Load data from Realm

        search = findViewById(R.id.search);
        search.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        search.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    searchItems();
                    try {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                    return true;
                }
                return false;
            }
        });
        searchItem = findViewById(R.id.button_search);
        searchItem.setTransformationMethod(null);
        searchItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchItems();
                try {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
            }
        });

        RealmResults<DynamicRealmObject> results = realm.where(DynamicRealmObject.class).findAll();
        keys = extractKeys(results);
        // Setup RecyclerView
        recyclerView = findViewById(R.id.dynamicRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapterGrid= new DynamicRealmAdapterGrid(this,OfflineStats.this, results,imageCache);
        recyclerView.setAdapter(adapterGrid);
       /* adapter = new DynamicRealmAdapter(this,OfflineStats.this, results,imageCache);
        recyclerView.setAdapter(adapter);*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
    @Override
    public void respondTotalChanged() {

    }

    @Override
    public void respondVolleyRequestFinished(Integer position, JSONObject jsonObject) {

    }

    private List<String> extractKeys(List<DynamicRealmObject> items) {
        Set<String> keySet = new LinkedHashSet<>();
        for (DynamicRealmObject obj : items) {
            for (KeyValue kv : obj.getFields()) {
                String key = kv.getKey();
                if (!key.equalsIgnoreCase("ID")) {   // **παράλειψη του "ID"**
                    keySet.add(key);
                }
            }
        }
        return new ArrayList<>(keySet);
    }

    private void searchItems() {

        try {
            if (!String.valueOf(search.getText()).equals("")) {
                String str = String.valueOf(search.getText());
                RealmQuery<DynamicRealmObject> query = realm.where(DynamicRealmObject.class);
                RealmQuery<DynamicRealmObject> orGroup = null;
                for (String key : keys) {
                    RealmQuery<DynamicRealmObject> subQuery = query.beginGroup()
                            .equalTo("fields.key", key)
                            .and()
                            .contains("fields.value", str, Case.INSENSITIVE)
                            .endGroup();

                    if (orGroup == null) {
                        orGroup = subQuery;
                    } else {
                        orGroup = orGroup.or().beginGroup()
                                .equalTo("fields.key", key)
                                .and()
                                .contains("fields.value", str, Case.INSENSITIVE)
                                .endGroup();
                    }
                }

// Αν keysToSearch είναι κενό, κάνε απλά:
                if (orGroup == null) {
                    orGroup = query; // ή query που επιστρέφει όλα
                }

                RealmResults<DynamicRealmObject> results = orGroup.findAll();

               /* RealmResults<DynamicRealmObject> results = realm.where(DynamicRealmObject.class)
                        .beginGroup()
                        .equalTo("fields.key", "Περιγραφή")
                        .and()
                        .contains("fields.value", str, Case.INSENSITIVE)
                        .endGroup()
                        .or()
                        .beginGroup()
                        .equalTo("fields.key", "Κωδικός")
                        .and()
                        .contains("fields.value", str, Case.INSENSITIVE)
                        .endGroup()
                        // εδώ προσθέτεις αν θες επιπλέον φίλτρα στο DynamicRealmObject αν έχεις π.χ. boolean πεδία Active κτλ.
                        .findAll();*/

                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                adapterGrid= new DynamicRealmAdapterGrid(this,OfflineStats.this, results,imageCache);
                recyclerView.setAdapter(adapterGrid);
               /* adapter = new DynamicRealmAdapter(this,OfflineStats.this, results,imageCache);
                recyclerView.setAdapter(adapter);*/
              //  search.setText("");
            } else {
                Toast.makeText(OfflineStats.this, "Δεν έχετε συμπληρώσει το κείμενο αναζήτησης.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    private Map<String, File> imageCache = new HashMap<>();
    private void buildImageCache() {
        File imagesDir =  new File(Environment.getExternalStorageDirectory().getPath() + "/NorthnetStatistics/Images/");
        if (imagesDir.exists() && imagesDir.isDirectory()) {
            File[] allFiles = imagesDir.listFiles();
            if (allFiles != null) {
                for (File file : allFiles) {
                    String name = file.getName().toLowerCase(Locale.ROOT);
                    if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")) {
                        String idPrefix = name.split("\\.")[0];// assumes format: <ID>_anything.jpg
                        if (!imageCache.containsKey(idPrefix)) {
                            imageCache.put(idPrefix, file);
                        }
                    }
                }
            }
        }
    }
}
