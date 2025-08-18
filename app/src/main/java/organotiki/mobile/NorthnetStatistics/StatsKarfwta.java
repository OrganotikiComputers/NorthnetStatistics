package organotiki.mobile.NorthnetStatistics;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

public class StatsKarfwta extends AppCompatActivity implements  Communicator{

    private RecyclerView recyclerView;
    private DynamicRealmAdapter adapter;
    ProgressBar progressBar;
    private RecyclerViewKarfwta recyclerViewKarfwta;
   // EditText search;
   // Button searchItem;
    private Realm realm;
    private List<String> keys;
    ImageView MenuShowFilters;
    LinearLayout fFilters;
    private Spinner spinnerSuppliers, spinnerCountries, spinnerCategories, spinnerOrderBy;
    private EditText editTextCode, editTextDescription;
    private CheckBox descending;
    private Button buttonSearch,buttonclearfilters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout
        setContentView(R.layout.activity_stats_karfwta);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // Init Realm
        Realm.init(this);
        realm = Realm.getDefaultInstance();

        progressBar=findViewById(R.id.progressBar);
        MenuShowFilters=findViewById(R.id.MenuShowFilters);
        fFilters=findViewById(R.id.fFilters);
        fFilters.setVisibility(View.GONE);
        MenuShowFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fFilters.getVisibility()==View.VISIBLE){
                    fFilters.setVisibility(View.GONE);
                }else{
                    fFilters.setVisibility(View.VISIBLE);
                }
            }
        });


        buildImageCache();

        //RealmResults<DynamicRealmObject> results = realm.where(DynamicRealmObject.class).findAll();
        //List<DynamicRealmObject> resultList = realm.copyFromRealm(results);
        recyclerView = findViewById(R.id.dynamicRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //recyclerViewKarfwta= new RecyclerViewKarfwta(this,StatsKarfwta.this, resultList,imageCache);
        //recyclerView.setAdapter(recyclerViewKarfwta);

        initViews();
        populateSpinners();
        setupSearchButton();
    }

    private void initViews() {
        spinnerSuppliers = findViewById(R.id.spinner_suppliers);
        spinnerCountries = findViewById(R.id.spinner_countries);
        spinnerCategories = findViewById(R.id.spinner_categories);
        spinnerOrderBy = findViewById(R.id.spinner_orderby);
        editTextCode = findViewById(R.id.edittext_code);

        editTextCode.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                search();

                return true; // consume event
            }
            return false;
        });

        editTextDescription = findViewById(R.id.edittext_description);

        editTextDescription.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                search();

                return true; // consume event
            }
            return false;
        });

        descending = findViewById(R.id.descending);
        buttonSearch = findViewById(R.id.button_search);
        buttonclearfilters=findViewById(R.id.clear_filter);

        buttonclearfilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerCategories.setSelection(0);
                spinnerCountries.setSelection(0);
                spinnerSuppliers.setSelection(0);
                editTextCode.setText("");
                editTextDescription.setText("");
            }
        });
    }

    private void populateSpinners() {
        // You can load these from Realm too
        List<String> suppliers = new ArrayList<>();
        List<String> countries = new ArrayList<>();
        List<String> categories = new ArrayList<>();

        suppliers.add("");
        countries.add("");
        categories.add("");

        RealmResults<DynamicRealmObject> allObjects = realm.where(DynamicRealmObject.class).findAll();
        HashSet<String> supplierSet = new HashSet<>();
        HashSet<String> countrySet = new HashSet<>();
        HashSet<String> categoriesSet = new HashSet<>();
        for (DynamicRealmObject obj : allObjects) {
            for (KeyValue kv : obj.getFields()) {
                if ("SUPPLIER".equalsIgnoreCase(kv.getKey()) && kv.getValue() != null) {
                    supplierSet.add(kv.getValue());
                }
                else if ("COUNTRY".equalsIgnoreCase(kv.getKey()) && kv.getValue() != null) {
                    countrySet.add(kv.getValue());
                }
                else if ("CATEGORY".equalsIgnoreCase(kv.getKey()) && kv.getValue() != null) {
                    categoriesSet.add(kv.getValue());
                }
            }
        }
        List<String> sortedSuppliers = new ArrayList<>(supplierSet);
        List<String> sortedCountries = new ArrayList<>(countrySet);
        List<String> sortedCategories = new ArrayList<>(categoriesSet);

        Collections.sort(sortedSuppliers, String.CASE_INSENSITIVE_ORDER);
        Collections.sort(sortedCountries, String.CASE_INSENSITIVE_ORDER);
        Collections.sort(sortedCategories, String.CASE_INSENSITIVE_ORDER);

        suppliers.addAll(sortedSuppliers);
        countries.addAll(sortedCountries);
        categories.addAll(sortedCategories);



        List<String> orderBy = new ArrayList<>();
        orderBy.add("Κωδικό");
        orderBy.add("Περιγραφή");
        orderBy.add("Προμηθευτή");
        orderBy.add("Χώρα");
        orderBy.add("Κατηγορία");
        orderBy.add("Τιμή αγοράς");
        orderBy.add("Νόμισμα");
        orderBy.add("Τιμή Χονδρικής");
        orderBy.add("Αγορές 23");
        orderBy.add("Πωλήσεις 23");
        orderBy.add("Αγορές 24");
        orderBy.add("Πωλήσεις 24");
        orderBy.add("Παραγγελίες");
        orderBy.add("Υπόλοιπο");

        spinnerSuppliers.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, suppliers));
        spinnerCountries.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, countries));
        spinnerCategories.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories));
        spinnerOrderBy.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, orderBy));
    }


    private void search(){


  /*      if(codeInput.length()==1 || codeInput.length()==2){
            Toast.makeText(StatsKarfwta.this,"Συμπληρώστε τουλάχιστον 3 χαρακτήρες στο πεδίο Κωδικό αν θέλετε να φιλτράρετε με αυτό!",Toast.LENGTH_LONG).show();
            return;
        }
        if(descriptionInput.length()==1 || descriptionInput.length()==2){
            Toast.makeText(StatsKarfwta.this,"Συμπληρώστε τουλάχιστον 3 χαρακτήρες στο πεδίο Περιγραφή αν θέλετε να φιλτράρετε με αυτό!",Toast.LENGTH_LONG).show();
            return;
        }*/
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            Realm backgroundRealm = Realm.getDefaultInstance(); // <-- Νέο instance στο νέο thread

            try{


                String selectedSupplier = spinnerSuppliers.getSelectedItem().toString();
                String selectedCountry = spinnerCountries.getSelectedItem().toString();
                String selectedCategory = spinnerCategories.getSelectedItem().toString();
                String selectedOrder = spinnerOrderBy.getSelectedItem().toString();
                String codeInput = editTextCode.getText().toString().trim();
                String descriptionInput = editTextDescription.getText().toString().trim();

                RealmQuery<DynamicRealmObject> query = backgroundRealm.where(DynamicRealmObject.class);

                query = addEqualsFilter(query, "SUPPLIER", selectedSupplier);
                query = addEqualsFilter(query, "COUNTRY", selectedCountry);
                query = addEqualsFilter(query, "CATEGORY", selectedCategory);

                RealmResults<DynamicRealmObject> results = query.findAll();
                List<DynamicRealmObject> resultList = backgroundRealm.copyFromRealm(results);
                if (!TextUtils.isEmpty(codeInput)) {
                    resultList = filterByFieldContains(resultList, "CODE", codeInput.toUpperCase());
                }
                if (!TextUtils.isEmpty(descriptionInput)) {
                    resultList = filterByFieldContains(resultList, "DESCRIPTION", descriptionInput.toUpperCase());
                }


                String test=query.getDescription();



                String selectedOrderID;
                if(!TextUtils.isEmpty(selectedOrder)){


                    if(selectedOrder.equals("Κωδικό")) selectedOrderID="CODE";
                    else if(selectedOrder.equals("Περιγραφή")) selectedOrderID="DESCRIPTION";
                    else if(selectedOrder.equals("Προμηθευτή")) selectedOrderID="SUPPLIER";
                    else if(selectedOrder.equals("Χώρα")) selectedOrderID="COUNTRY";
                    else if(selectedOrder.equals("Κατηγορία")) selectedOrderID="CATEGORY";
                    else if(selectedOrder.equals("Τιμή αγοράς")) selectedOrderID="TIMI_AGORAS";
                    else if(selectedOrder.equals("Νόμισμα")) selectedOrderID="NOMISMA";
                    else if(selectedOrder.equals("Τιμή Χονδρικής")) selectedOrderID="TIMI_XONDRIKIS";
                    else if(selectedOrder.equals("Αγορές 23")) selectedOrderID="POSOTITA_AGORAS_23";
                    else if(selectedOrder.equals("Πωλήσεις 23")) selectedOrderID="POSOTITA_PWLISEWN_23";
                    else if(selectedOrder.equals("Αγορές 24")) selectedOrderID="POSOTITA_AGORAS_24";
                    else if(selectedOrder.equals("Πωλήσεις 24")) selectedOrderID="POSOTITA_PWLISEWN_24";
                    else if(selectedOrder.equals("Παραγγελίες")) selectedOrderID="PARAGGELIES";
                    else if(selectedOrder.equals("Υπόλοιπο"))  selectedOrderID="YPOLOIPO";
                    else {
                        selectedOrderID = "CODE";
                    }

                    Set<String> numericFields = new HashSet<>(Arrays.asList(
                            "TIMI_AGORAS","TIMI_XONDRIKIS", "POSOTITA_AGORAS_23", "POSOTITA_PWLISEWN_23",
                            "POSOTITA_AGORAS_24", "POSOTITA_PWLISEWN_24", "PARAGGELIES", "YPOLOIPO"
                    ));

                    boolean isDescending = descending.isChecked();

                    if (numericFields.contains(selectedOrderID)) {
                        // Numeric comparison
                        Collections.sort(resultList, (o1, o2) -> {
                            BigDecimal v1 = parseDecimal(getField(o1, selectedOrderID));
                            BigDecimal v2 = parseDecimal(getField(o2, selectedOrderID));
                            return isDescending ? v2.compareTo(v1) : v1.compareTo(v2);
                        });
                    } else {
                        // Textual comparison
                        Collections.sort(resultList, (o1, o2) -> {
                            String v1 = getField(o1, selectedOrderID);
                            String v2 = getField(o2, selectedOrderID);
                            return isDescending ? v2.compareToIgnoreCase(v1) : v1.compareToIgnoreCase(v2);
                        });
                    }
                }
                List<DynamicRealmObject> finalResultList = resultList;
                runOnUiThread(() -> {
                    recyclerView.setLayoutManager(new LinearLayoutManager(StatsKarfwta.this));
                    recyclerViewKarfwta = new RecyclerViewKarfwta(StatsKarfwta.this, StatsKarfwta.this, finalResultList, imageCache);
                    recyclerView.setAdapter(recyclerViewKarfwta);
                    progressBar.setVisibility(View.GONE);
                });

            }
            catch (Exception e){
                Log.e("asdfg", e.getMessage(), e);
                runOnUiThread(() -> progressBar.setVisibility(View.GONE));
            }}).start();
    }
    private void setupSearchButton() {
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               search();
            }
        });
    }

    private RealmQuery<DynamicRealmObject> addEqualsFilter(RealmQuery<DynamicRealmObject> query, String key, String value) {
        if (TextUtils.isEmpty(value)) return query;
        return query.beginGroup()
                .equalTo("fields.key", key)
                .and()
                .equalTo("fields.value", value, Case.INSENSITIVE)
                .endGroup();
    }
    private List<DynamicRealmObject> filterByFieldContains(List<DynamicRealmObject> list, String key, String value) {
        List<DynamicRealmObject> filtered = new ArrayList<>();
        for (DynamicRealmObject obj : list) {
            RealmList<KeyValue> fields = obj.getFields();
            for (KeyValue kv : fields) {
                String k = kv.getKey();
                String v = kv.getValue();
                if (key.equalsIgnoreCase(k) && v != null && v.toUpperCase().contains(value)) {
                    filtered.add(obj);
                    break;
                }
            }
        }
        return filtered;
    }
    private BigDecimal parseDecimal(String value) {
        try {
            return new BigDecimal(value.replace(",", ".")); // in case commas are used
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
    private String getField(DynamicRealmObject obj, String key) {
        for (KeyValue kv : obj.getFields()) {
            if (kv.getKey().equalsIgnoreCase(key)) {
                return kv.getValue();
            }
        }
        return "";
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

  /*  private void searchItems() {

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

               *//* RealmResults<DynamicRealmObject> results = realm.where(DynamicRealmObject.class)
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
                        .findAll();*//*

                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerViewKarfwta= new RecyclerViewKarfwta(this,StatsKarfwta.this, results,imageCache);
                recyclerView.setAdapter(recyclerViewKarfwta);
               *//* adapter = new DynamicRealmAdapter(this,OfflineStats.this, results,imageCache);
                recyclerView.setAdapter(adapter);*//*
                //  search.setText("");
            } else {
                Toast.makeText(StatsKarfwta.this, "Δεν έχετε συμπληρώσει το κείμενο αναζήτησης.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }*/

    private Map<String, File> imageCache = new HashMap<>();
    private void buildImageCache() {
        File imagesDir =  new File(Environment.getExternalStorageDirectory().getPath() + "/NorthnetStatistics/Images/");
        if (imagesDir.exists() && imagesDir.isDirectory()) {
            File[] allFiles = imagesDir.listFiles();
            if (allFiles != null) {
                for (File file : allFiles) {
                    String name = file.getName().toLowerCase(Locale.ROOT);
                    if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")) {
                        String baseName = name.replaceAll("\\.(jpg|jpeg|png)$", "");
                        String idPrefix = baseName.split("[_.]", 2)[0];// assumes format: <ID>_anything.jpg
                        if (!imageCache.containsKey(idPrefix))
                        {
                            imageCache.put(idPrefix, file);
                        }
                    }
                }
            }
        }
    }
}

