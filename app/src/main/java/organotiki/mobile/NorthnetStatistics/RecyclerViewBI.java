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

import organotiki.mobile.NorthnetStatistics.objects.DynamicRealmObject;
import organotiki.mobile.NorthnetStatistics.objects.KeyValue;

public class RecyclerViewBI extends RecyclerView.Adapter<RecyclerViewBI.RecyclerViewBIViewHolder> {

    private final Context context;
    public static Bitmap[] logo;
    private final List<DynamicRealmObject> dataList;
    private Activity activity;
    private final Map<String, File> imageCache;
    public RecyclerViewBI(Context context, Activity activity, List<DynamicRealmObject> dataList, Map<String, File> imageCache) {
        this.context = context;
        this.dataList = dataList;
        this.activity = activity;
        this.imageCache = imageCache;
        logo = getLogo();
    }

    @NonNull
    @Override
    public RecyclerViewBIViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bi, parent, false);
        return new RecyclerViewBIViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewBIViewHolder holder, int position) {
        DynamicRealmObject obj = dataList.get(position);

        holder.page.setText(getValueByKey(obj, "ΣΕΛ"));
        holder.katigoria.setText(getValueByKey(obj, "ΚΑΤΗΓ"));
        holder.code.setText(getValueByKey(obj, "ΚΩΔ"));
        holder.timi_xondrikis.setText(getValueByKey(obj, "TX"));
        holder.description.setText(getValueByKey(obj, "ΠΕΡΙΓΡΑΦΗ"));
        holder.apografi.setText(getValueByKey(obj, "ΑΠΟΓ"));
        holder.agores.setText(getValueByKey(obj, "ΑΓΟΡ"));
        holder.paraggelies.setText(getValueByKey(obj, "ΠΑΡ"));
        holder.pwliseis.setText(getValueByKey(obj, "ΠΩΛ"));
        holder.kentriko.setText(getValueByKey(obj, "ΚΕΝ"));
        holder.timi_agoras.setText(getValueByKey(obj, "ΤΑ"));
        holder.nomisma.setText(getValueByKey(obj, "ΞΝ"));
        holder.sxolia.setText(getValueByKey(obj, "ΣΧ"));




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
    static class RecyclerViewBIViewHolder extends RecyclerView.ViewHolder {
        TextView page,katigoria,code,timi_xondrikis, description,apografi, agores, paraggelies, pwliseis , kentriko, timi_agoras, nomisma, sxolia;
        //TextView xwra, katigoria,ag_23, pwl_23;
        ImageView image;

        public RecyclerViewBIViewHolder(@NonNull View itemView) {
            super(itemView);
            code = itemView.findViewById(R.id.code);
            description = itemView.findViewById(R.id.description);
            katigoria = itemView.findViewById(R.id.katigoria1);
            page = itemView.findViewById(R.id.page);
            timi_agoras = itemView.findViewById(R.id.timi_agoras);
            nomisma = itemView.findViewById(R.id.nomisma);
            timi_xondrikis = itemView.findViewById(R.id.timi_xondrikis);
            apografi = itemView.findViewById(R.id.apografi);
            agores = itemView.findViewById(R.id.agores);
            paraggelies = itemView.findViewById(R.id.paraggelies);
            pwliseis = itemView.findViewById(R.id.pwliseis);
            kentriko = itemView.findViewById(R.id.kentriko);
            sxolia = itemView.findViewById(R.id.sxolia);
            image = itemView.findViewById(R.id.imageView_image);
        }
    }
}
