package organotiki.mobile.NorthnetStatistics;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;
import java.util.Map;

import io.realm.RealmResults;
import organotiki.mobile.NorthnetStatistics.objects.DynamicRealmObject;
import organotiki.mobile.NorthnetStatistics.objects.KeyValue;

public class RecyclerViewKarfwta extends RecyclerView.Adapter<RecyclerViewKarfwta.RecyclerViewKarfwtaViewHolder> {

    private final Context context;
    public static Bitmap[] logo;
    private final List<DynamicRealmObject> dataList;
    private Activity activity;
    private final Map<String, File> imageCache;
    public RecyclerViewKarfwta(Context context, Activity activity, List<DynamicRealmObject> dataList, Map<String, File> imageCache) {
        this.context = context;
        this.dataList = dataList;
        this.activity = activity;
        this.imageCache = imageCache;
        logo = getLogo();
    }

    @NonNull
    @Override
    public RecyclerViewKarfwtaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_karfwto, parent, false);
        return new RecyclerViewKarfwtaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewKarfwtaViewHolder holder, int position) {
        DynamicRealmObject obj = dataList.get(position);

        holder.code.setText(getValueByKey(obj, "CODE"));
        holder.description.setText(getValueByKey(obj, "DESCRIPTION"));
        holder.promitheuthis.setText(getValueByKey(obj, "SUPPLIER"));
        holder.xwra.setText(getValueByKey(obj, "COUNTRY"));
        holder.katigoria.setText(getValueByKey(obj, "CATEGORY"));
        holder.timi_agoras.setText(getValueByKey(obj, "TIMI_AGORAS"));
        holder.nomisma.setText(getValueByKey(obj, "NOMISMA"));
        holder.timi_xondrikis.setText(getValueByKey(obj, "TIMI_XONDRIKIS"));
        holder.ag_23.setText(getValueByKey(obj, "POSOTITA_AGORAS_23"));
        holder.pwl_23.setText(getValueByKey(obj, "POSOTITA_PWLISEWN_23"));
        holder.ag_24.setText(getValueByKey(obj, "POSOTITA_AGORAS_24"));
        holder.pwl_24.setText(getValueByKey(obj, "POSOTITA_PWLISEWN_24"));
        holder.parag.setText(getValueByKey(obj, "PARAGGELIES"));
        holder.ypol.setText(getValueByKey(obj, "YPOLOIPO"));
        // Set image if needed using Glide or Picasso

        String ID=getValueByKey(obj, "ID");
        if (imageCache.containsKey(ID.toLowerCase())) {
            File imgFile = imageCache.get(ID.toLowerCase());
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                holder.image.setImageBitmap(bitmap);
            }
        } else {
            if((logo != null && logo.length > 0)){
                holder.image.setImageBitmap(logo[0]);
            }else{
                holder.image.setImageResource(R.drawable.logoo);
            }
        }

        holder.image.setOnClickListener(v -> {
            try {
                FragmentManager manager = activity.getFragmentManager();
                ImageFragment frag = new ImageFragment();
                frag.setImageCode(ID);
                frag.show(manager, "Image Fragment");
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }
        });
    }
    private String getValueByKey(DynamicRealmObject obj, String key) {
        for (KeyValue kv : obj.getFields()) {
            if (kv.getKey().equalsIgnoreCase(key)) {
                return kv.getValue();
            }
        }
        return "";
    }
    @Override
    public int getItemCount() {
        return dataList.size();
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
    static class RecyclerViewKarfwtaViewHolder extends RecyclerView.ViewHolder {
        TextView code, description, promitheuthis, xwra, katigoria,timi_agoras, nomisma, timi_xondrikis,ag_23, pwl_23, ag_24, pwl_24, parag, ypol;
        ImageView image;

        public RecyclerViewKarfwtaViewHolder(@NonNull View itemView) {
            super(itemView);
            code = itemView.findViewById(R.id.code);
            description = itemView.findViewById(R.id.description);
            promitheuthis = itemView.findViewById(R.id.promitheuthis);
            xwra = itemView.findViewById(R.id.xwra);
            katigoria = itemView.findViewById(R.id.katigoria);
            timi_agoras = itemView.findViewById(R.id.timi_agoras);
            nomisma = itemView.findViewById(R.id.nomisma);
            timi_xondrikis = itemView.findViewById(R.id.timi_xondrikis);
            ag_23 = itemView.findViewById(R.id.ag_23);
            pwl_23 = itemView.findViewById(R.id.pwl_23);
            ag_24 = itemView.findViewById(R.id.ag_24);
            pwl_24 = itemView.findViewById(R.id.pwl_24);
            parag = itemView.findViewById(R.id.parag);
            ypol = itemView.findViewById(R.id.ypol);
            image = itemView.findViewById(R.id.imageView_image);
        }
    }
}
