package organotiki.mobile.NorthnetStatistics;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

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

public class StatsBI extends AppCompatActivity implements  Communicator{

    private RecyclerView recyclerView;
    private DynamicRealmAdapter adapter;
    ProgressBar progressBar;
    private RecyclerViewBI recyclerViewBI;
    // EditText search;
    // Button searchItem;
    private Realm realm;
    private List<String> keys;
    ImageView MenuShowFilters;
    LinearLayout fFilters;
    private Spinner spinnerCategories1, spinnerCategories2, spinnerCategories3, spinnerOrderBy , spinnerPages1,spinnerPages2;
    private EditText editTextCode, editTextDescription;
    private TextView totalAp, totalAg,totalPar,totalPwl,totalKent;
    private CheckBox descending;
    private Button buttonSearch,buttonclearfilters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout
        setContentView(R.layout.activity_stats_bi);
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
        spinnerCategories1 = findViewById(R.id.spinner_categories1);
        spinnerCategories2 = findViewById(R.id.spinner_categories2);
        spinnerCategories3 = findViewById(R.id.spinner_categories3);
        spinnerPages1 = findViewById(R.id.spinner_pages1);
        spinnerPages2 = findViewById(R.id.spinner_pages2);
        spinnerOrderBy = findViewById(R.id.spinner_orderby);


        totalAp = findViewById(R.id.tAp);
        totalAg = findViewById(R.id.tAg);
        totalPar = findViewById(R.id.tPar);
        totalPwl = findViewById(R.id.tPwl);
        totalKent = findViewById(R.id.tKent);


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
                spinnerCategories1.setSelection(0);
                spinnerCategories2.setSelection(0);
                spinnerCategories3.setSelection(0);
                spinnerPages1.setSelection(0);
                spinnerPages2.setSelection(0);
                editTextCode.setText("");
                editTextDescription.setText("");
            }
        });
    }

    private void populateSpinners() {
        // You can load these from Realm too
        List<String> categories1 = new ArrayList<>();
        List<String> categories2 = new ArrayList<>();
        List<String> categories3 = new ArrayList<>();
        List<String> pages1 = new ArrayList<>();
        List<String> pages2 = new ArrayList<>();

        categories1.add("");
        categories2.add("");
        categories3.add("");
        pages1.add("");
        pages2.add("");

        RealmResults<DynamicRealmObject> allObjects = realm.where(DynamicRealmObject.class).findAll();
        HashSet<String> categories1Set = new HashSet<>();
        HashSet<String> categories2Set = new HashSet<>();
        HashSet<String> categories3Set = new HashSet<>();
        HashSet<String> pages1Set = new HashSet<>();
        HashSet<String> pages2Set = new HashSet<>();
        for (DynamicRealmObject obj : allObjects) {
            for (KeyValue kv : obj.getFields()) {
                if ("ΚΑΤΗΓ".equalsIgnoreCase(kv.getKey()) && kv.getValue() != null) {
                    categories1Set.add(kv.getValue());
                }
                else if ("ΚΑΤΗΓ_2".equalsIgnoreCase(kv.getKey()) && kv.getValue() != null) {
                    categories2Set.add(kv.getValue());
                }
                else if ("ΚΑΤΗΓ_3".equalsIgnoreCase(kv.getKey()) && kv.getValue() != null) {
                    categories3Set.add(kv.getValue());
                }
                else if ("ΣΕΛ".equalsIgnoreCase(kv.getKey()) && kv.getValue() != null) {
                    pages1Set.add(kv.getValue());
                    pages2Set.add(kv.getValue());
                }
            }
        }
        List<String> sortedCategories1 = new ArrayList<>(categories1Set);
        List<String> sortedCategories2 = new ArrayList<>(categories2Set);
        List<String> sortedCategories3 = new ArrayList<>(categories3Set);
        List<String> sortedPages1 = new ArrayList<>(pages1Set);
        List<String> sortedPages2 = new ArrayList<>(pages2Set);


        Collections.sort(sortedCategories1, String.CASE_INSENSITIVE_ORDER);
        Collections.sort(sortedCategories2, String.CASE_INSENSITIVE_ORDER);
        Collections.sort(sortedCategories3, String.CASE_INSENSITIVE_ORDER);
        Collections.sort(sortedPages1, String.CASE_INSENSITIVE_ORDER);
        Collections.sort(sortedPages2, String.CASE_INSENSITIVE_ORDER);

        categories1.addAll(sortedCategories1);
        categories2.addAll(sortedCategories2);
        categories3.addAll(sortedCategories3);
        pages1.addAll(sortedPages1);
        pages2.addAll(sortedPages2);


        List<String> orderBy = new ArrayList<>();
        orderBy.add("Σελίδα");
        orderBy.add("Κατηγορία");
        orderBy.add("Κωδικό");
        orderBy.add("Τιμή Χονδρικής");
        orderBy.add("Περιγραφή");
        orderBy.add("Απογραφή");
        orderBy.add("Αγορές");
        orderBy.add("Παραγγελίες");
        orderBy.add("Πωλήσεις");
        orderBy.add("Κεντρικό");
        orderBy.add("Τιμή αγοράς");
        orderBy.add("Νόμισμα");
        orderBy.add("Σχόλια");


        spinnerCategories1.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories1));
        spinnerCategories2.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories2));
        spinnerCategories3.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories3));
        spinnerPages1.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, pages1));
        spinnerPages2.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, pages2));
        spinnerOrderBy.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, orderBy));
    }


    private void search(){


        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            Realm backgroundRealm = Realm.getDefaultInstance(); // <-- Νέο instance στο νέο thread

            try{


                String selectedCategory1 = spinnerCategories1.getSelectedItem().toString();
                String selectedCategory2 = spinnerCategories2.getSelectedItem().toString();
                String selectedCategory3 = spinnerCategories3.getSelectedItem().toString();
                String selectedPage1 = spinnerPages1.getSelectedItem().toString();
                String selectedPage2 = spinnerPages2.getSelectedItem().toString();
                String selectedOrder = spinnerOrderBy.getSelectedItem().toString();
                String codeInput = editTextCode.getText().toString().trim();
                String descriptionInput = editTextDescription.getText().toString().trim();

                RealmQuery<DynamicRealmObject> query = backgroundRealm.where(DynamicRealmObject.class);
                if (TextUtils.isEmpty(codeInput)){
                    query = query.equalTo("fields.key", "ΚΩΔ");
                }


                if (!TextUtils.isEmpty(selectedCategory1)) {

                    query = query.beginGroup()
                            .beginGroup()
                            .equalTo("fields.key", "ΚΑΤΗΓ")
                            .and()
                            .equalTo("fields.value", selectedCategory1, Case.INSENSITIVE)
                            .endGroup();
                    if(TextUtils.isEmpty(selectedCategory2))
                    {
                        query = query.or()
                                .beginGroup()
                                .equalTo("fields.key", "ΚΑΤΗΓ_2")
                                .and()
                                .equalTo("fields.value", selectedCategory1, Case.INSENSITIVE)
                                .endGroup();
                    }
                    if(TextUtils.isEmpty(selectedCategory3))
                    {
                        query = query.or()
                                .beginGroup()
                                .equalTo("fields.key", "ΚΑΤΗΓ_3")
                                .and()
                                .equalTo("fields.value", selectedCategory1, Case.INSENSITIVE)
                                .endGroup();
                    }
                    query = query.endGroup();
                }
                if (!TextUtils.isEmpty(selectedCategory2)) {

                    query = query.beginGroup()
                            .beginGroup()
                            .equalTo("fields.key", "ΚΑΤΗΓ_2")
                            .and()
                            .equalTo("fields.value", selectedCategory2, Case.INSENSITIVE)
                            .endGroup();
                    if(TextUtils.isEmpty(selectedCategory1))
                    {
                        query = query.or()
                                .beginGroup()
                                .equalTo("fields.key", "ΚΑΤΗΓ")
                                .and()
                                .equalTo("fields.value", selectedCategory2, Case.INSENSITIVE)
                                .endGroup();
                    }
                    if(TextUtils.isEmpty(selectedCategory3))
                    {
                        query = query.or()
                                .beginGroup()
                                .equalTo("fields.key", "ΚΑΤΗΓ_3")
                                .and()
                                .equalTo("fields.value", selectedCategory2, Case.INSENSITIVE)
                                .endGroup();
                    }
                    query = query.endGroup();
                }
                if (!TextUtils.isEmpty(selectedCategory3)) {

                    query = query.beginGroup()
                            .beginGroup()
                            .equalTo("fields.key", "ΚΑΤΗΓ_3")
                            .and()
                            .equalTo("fields.value", selectedCategory3, Case.INSENSITIVE)
                            .endGroup();
                    if(TextUtils.isEmpty(selectedCategory1))
                    {
                        query = query.or()
                                .beginGroup()
                                .equalTo("fields.key", "ΚΑΤΗΓ")
                                .and()
                                .equalTo("fields.value", selectedCategory3, Case.INSENSITIVE)
                                .endGroup();
                    }
                    if(TextUtils.isEmpty(selectedCategory2))
                    {
                        query = query.or()
                                .beginGroup()
                                .equalTo("fields.key", "ΚΑΤΗΓ_2")
                                .and()
                                .equalTo("fields.value", selectedCategory3, Case.INSENSITIVE)
                                .endGroup();
                    }
                    query = query.endGroup();
                }


                RealmResults<DynamicRealmObject> results = query.findAll();
                List<DynamicRealmObject> resultList = backgroundRealm.copyFromRealm(results);
                if (!TextUtils.isEmpty(codeInput)) {
                    resultList = filterByFieldContains(resultList, "ΚΩΔ", codeInput.toUpperCase());
                }
                if (!TextUtils.isEmpty(descriptionInput)) {
                    resultList = filterByFieldContains(resultList, "ΠΕΡΙΓΡΑΦΗ", descriptionInput.toUpperCase());
                }

                if(!TextUtils.isEmpty(selectedPage1)){
                    resultList = filterByFieldGreater(resultList, "ΣΕΛ", selectedPage1);
                }

                if(!TextUtils.isEmpty(selectedPage2)){
                    resultList = filterByFieldLower(resultList, "ΣΕΛ", selectedPage2);
                }

                String test=query.getDescription();



                String selectedOrderID;
                if(!TextUtils.isEmpty(selectedOrder)){
                    if(selectedOrder.equals("Κωδικό")) selectedOrderID="ΚΩΔ";
                    else if(selectedOrder.equals("Περιγραφή")) selectedOrderID="ΠΕΡΙΓΡΑΦΗ";
                    else if(selectedOrder.equals("Σελίδα")) selectedOrderID="ΣΕΛ";
                    else if(selectedOrder.equals("Κατηγορία")) selectedOrderID="ΚΑΤΗΓ";
                    else if(selectedOrder.equals("Τιμή Χονδρικής")) selectedOrderID="TX";
                    else if(selectedOrder.equals("Απογραφή")) selectedOrderID="ΑΠΟΓ";
                    else if(selectedOrder.equals("Αγορές")) selectedOrderID="ΑΓΟΡ";
                    else if(selectedOrder.equals("Παραγγελίες")) selectedOrderID="ΠΑΡ";
                    else if(selectedOrder.equals("Πωλήσεις")) selectedOrderID="ΠΩΛ";
                    else if(selectedOrder.equals("Κεντρικό")) selectedOrderID="ΚΕΝ";
                    else if(selectedOrder.equals("Τιμή αγοράς"))  selectedOrderID="ΤΑ";
                    else if(selectedOrder.equals("Νόμισμα"))  selectedOrderID="ΞΝ";
                    else if(selectedOrder.equals("Σχόλια"))  selectedOrderID="ΣΧ";
                    else {
                        selectedOrderID = "ΣΕΛ";
                    }

                    Set<String> numericFields = new HashSet<>(Arrays.asList(
                            "TX","ΑΠΟΓ", "ΑΓΟΡ", "ΠΑΡ",
                            "ΠΩΛ", "ΚΕΝ", "ΤΑ"
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
                BigDecimal Apog = BigDecimal.ZERO;
                BigDecimal Agor = BigDecimal.ZERO;
                BigDecimal Par = BigDecimal.ZERO;
                BigDecimal Pol = BigDecimal.ZERO;
                BigDecimal Ken = BigDecimal.ZERO;

                for (DynamicRealmObject obj : resultList) {

                    Apog = Apog.add(parseDecimal(getField(obj, "ΑΠΟΓ")));
                    Agor = Agor.add(parseDecimal(getField(obj, "ΑΓΟΡ")));
                    Par = Par.add(parseDecimal(getField(obj, "ΠΑΡ")));
                    Pol = Pol.add(parseDecimal(getField(obj, "ΠΩΛ")));
                    Ken = Ken.add(parseDecimal(getField(obj, "ΚΕΝ")));
                }
                final BigDecimal fTotalApog = Apog;
                final BigDecimal fTotalAgor = Agor;
                final BigDecimal fTotalPar =  Par;
                final BigDecimal fTotalPol =  Pol;
                final BigDecimal fTotalKen =  Ken;

                List<DynamicRealmObject> finalResultList = resultList;
                runOnUiThread(() -> {
                    recyclerView.setLayoutManager(new LinearLayoutManager(StatsBI.this));
                    recyclerViewBI = new RecyclerViewBI(StatsBI.this, StatsBI.this, finalResultList, imageCache);
                    recyclerView.setAdapter(recyclerViewBI);

                    totalAp.setText(fTotalApog.toString());
                    totalAg.setText(fTotalAgor.toString());
                    totalPwl.setText(fTotalPol.toString());
                    totalPar.setText(fTotalPar.toString());
                    totalKent.setText(fTotalKen.toString());

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

    private List<DynamicRealmObject> filterByFieldGreater(
            List<DynamicRealmObject> list,
            String key,
            String value
    ) {
        List<DynamicRealmObject> filtered = new ArrayList<>();
        double compareValue;

        try {
            compareValue = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            // if the given value is not numeric, just return the original list (or empty)
            return filtered;
        }

        for (DynamicRealmObject obj : list) {
            RealmList<KeyValue> fields = obj.getFields();
            for (KeyValue kv : fields) {
                String k = kv.getKey();
                String v = kv.getValue();

                if (key.equalsIgnoreCase(k) && v != null) {
                    try {
                        double fieldValue = Double.parseDouble(v);
                        if (fieldValue >= compareValue) {
                            filtered.add(obj);
                            break;
                        }
                    } catch (NumberFormatException ignored) {
                        // skip if not numeric
                    }
                }
            }
        }
        return filtered;
    }

    private List<DynamicRealmObject> filterByFieldLower(
            List<DynamicRealmObject> list,
            String key,
            String value
    ) {
        List<DynamicRealmObject> filtered = new ArrayList<>();
        double compareValue;

        try {
            compareValue = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            // if the given value is not numeric, just return the original list (or empty)
            return filtered;
        }

        for (DynamicRealmObject obj : list) {
            RealmList<KeyValue> fields = obj.getFields();
            for (KeyValue kv : fields) {
                String k = kv.getKey();
                String v = kv.getValue();

                if (key.equalsIgnoreCase(k) && v != null) {
                    try {
                        double fieldValue = Double.parseDouble(v);
                        if (fieldValue <= compareValue) {
                            filtered.add(obj);
                            break;
                        }
                    } catch (NumberFormatException ignored) {
                        // skip if not numeric
                    }
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

