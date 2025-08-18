package organotiki.mobile.NorthnetStatistics;
import static com.google.android.material.internal.ViewUtils.dpToPx;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import organotiki.mobile.NorthnetStatistics.objects.DynamicRealmObject;
import organotiki.mobile.NorthnetStatistics.objects.Item;
import organotiki.mobile.NorthnetStatistics.objects.KeyValue;
public class DynamicRealmAdapterGrid extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ROW = 1;

    private List<DynamicRealmObject> items;
    private List<String> keys;
    private Context context;
    private Activity activity;
    public static Bitmap[] logo;
    private final Map<String, File> imageCache;

    public DynamicRealmAdapterGrid(Context context, Activity activity, List<DynamicRealmObject> items, Map<String, File> imageCache) {
        this.context = context;
        this.items = items;
        this.activity = activity;
        this.imageCache = imageCache;
        logo = getLogo();
        this.keys = extractKeys(items); // keys for header and alignment
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_HEADER : VIEW_TYPE_ROW;
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() + 1 : 1; // +1 for header
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dynamic_realm_grid, parent, false);
        return new TableRowViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TableRowViewHolder viewHolder = (TableRowViewHolder) holder;

        if (getItemViewType(position) == VIEW_TYPE_HEADER) {
            viewHolder.bindHeader(keys);
        } else {
            DynamicRealmObject obj = items.get(position - 1); // adjust for header
            viewHolder.bindRow(obj, keys, imageCache, activity);
        }
    }

    // Extract all unique keys from the data to build the header
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

    static class TableRowViewHolder extends RecyclerView.ViewHolder {
        LinearLayout rowContainer;
        ImageView imageView;
        int viewType;

        public TableRowViewHolder(View itemView, int viewType) {
            super(itemView);
            rowContainer = itemView.findViewById(R.id.rowContainer);
            imageView = itemView.findViewById(R.id.imageView);
            this.viewType = viewType;

        }

        public void bindHeader(List<String> keys) {
            rowContainer.removeAllViews();
            for (String key : keys) {
                TextView tv = new TextView(rowContainer.getContext());
                tv.setText(key);
                tv.setTypeface(null, Typeface.BOLD);
               // tv.setPadding(8, 8, 8, 8);
                tv.setGravity(Gravity.CENTER_VERTICAL);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(  dpToPx(250), dpToPx(50)); // σταθερό πλάτος/ύψος
                tv.setLayoutParams(params);
                tv.setBackgroundColor(Color.LTGRAY);
                rowContainer.addView(tv);
            }
            imageView.setImageResource(0);
            imageView.setBackgroundColor(Color.LTGRAY);
        }
        public void bindRow(DynamicRealmObject obj, List<String> keys, Map<String, File> imageCache, Activity activity) {
            rowContainer.removeAllViews();
            Map<String, String> values = new HashMap<>();
            String idForImage = null;

            for (KeyValue kv : obj.getFields()) {
                if (kv.getKey().equalsIgnoreCase("ID")) {
                    idForImage = kv.getValue();
                } else {
                    values.put(kv.getKey(), kv.getValue());
                }
            }

            for (String key : keys) {
                TextView tv = new TextView(rowContainer.getContext());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tv.setText(values.getOrDefault(key, ""));
                }
                //tv.setPadding(4, 4, 4, 4);
                //tv.setGravity(Gravity.CENTER_VERTICAL);

                tv.setBackgroundResource(R.drawable.textview_border);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(250), dpToPx(50));
                tv.setLayoutParams(params);
                rowContainer.addView(tv);
            }

            if (idForImage != null && imageCache.containsKey(idForImage.toLowerCase())) {
                File imgFile = imageCache.get(idForImage.toLowerCase());
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    imageView.setImageBitmap(bitmap);
                }
            } else {
                if((logo != null && logo.length > 0)){
                    imageView.setImageBitmap(logo[0]);
                }else{
                    imageView.setImageResource(R.drawable.logoo);
                }

            }

            String finalIdForImage = idForImage;
            imageView.setOnClickListener(v -> {
                try {
                    FragmentManager manager = activity.getFragmentManager();
                    ImageFragment frag = new ImageFragment();
                    frag.setImageCode(finalIdForImage);
                    frag.show(manager, "Image Fragment");
                } catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
            });
        }
        private int dpToPx(int dp) {
            float density = itemView.getResources().getDisplayMetrics().density;
            return Math.round((float) dp * density);
        }
        }

        public Bitmap[] getLogo() {
        try {
            File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/NorthnetStatistics/Images/");
            String[] list = dir.list((d, name) -> name.startsWith("logo"));
            if (list == null) return new Bitmap[0];
            Bitmap[] imgs = new Bitmap[list.length];
            for (int i = 0; i < list.length; i++) {
                File imgFile = new File(dir, list[i]);
                imgs[i] = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            }
            return imgs;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return new Bitmap[0];
        }
    }
}
