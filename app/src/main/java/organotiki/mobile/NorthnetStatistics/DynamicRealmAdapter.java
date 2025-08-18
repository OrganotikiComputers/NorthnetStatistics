package organotiki.mobile.NorthnetStatistics;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import organotiki.mobile.NorthnetStatistics.objects.DynamicRealmObject;
import organotiki.mobile.NorthnetStatistics.objects.Item;
import organotiki.mobile.NorthnetStatistics.objects.KeyValue;

public class DynamicRealmAdapter extends RecyclerView.Adapter<DynamicRealmAdapter.DynamicViewHolder> {

    private List<DynamicRealmObject> items;
    private Context context;
    private Activity activity;
    public static Bitmap[] logo;
    private Communicator comm;

    private final Map<String, File> imageCache;
    public DynamicRealmAdapter(Context context, Activity activity, List<DynamicRealmObject> items, Map<String, File> imageCache) {
        this.context = context;
        this.items = items;
        this.activity = activity;
        logo=getLogo();
        comm = (Communicator)activity;
        this.imageCache = imageCache;
    }

    @Override
    public DynamicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dynamic_realm, parent, false);
        return new DynamicViewHolder(view,imageCache,activity);
    }

    @Override
    public void onBindViewHolder(DynamicViewHolder holder, int position) {
        DynamicRealmObject obj = items.get(position);
        holder.bind(obj);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class DynamicViewHolder extends RecyclerView.ViewHolder {
        LinearLayout fieldContainer;
        public ImageView Image;
        Map<String, File> imageCache;
        Realm realm;
        Activity activity;
        public DynamicViewHolder(View itemView,Map<String, File> imageCache,Activity activity) {
            super(itemView);
            realm = Realm.getDefaultInstance();
            fieldContainer = itemView.findViewById(R.id.fieldContainer);
            Image = itemView.findViewById(R.id.imageView_image);
            this.imageCache = imageCache;
            this.activity=activity;
        }

        public void bind(DynamicRealmObject obj) {
            realm = Realm.getDefaultInstance();
            fieldContainer.removeAllViews();
            String idForImage = null;
            for (KeyValue kv : obj.getFields()) {
                TextView textView = new TextView(fieldContainer.getContext());
                textView.setText(kv.getKey() + ": " + kv.getValue());
                textView.setPadding(4, 4, 4, 4);
                fieldContainer.addView(textView);

                if (kv.getKey().equalsIgnoreCase("ID")) {
                    idForImage = kv.getValue();
                }
            }

            if (idForImage != null) {
                File imageFile = imageCache.get(idForImage.toLowerCase());
                if (imageFile != null && imageFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    Image.setImageBitmap(bitmap);
                } else {
                    if(logo==null || logo.length==0){
                        Image.setImageResource(R.drawable.logoo);
                    }
                    else{
                        Image.setImageBitmap(logo[0]);
                    }
                }
            }

            String finalIdForImage = idForImage;
            Image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        FragmentManager manager = activity.getFragmentManager();
                        ImageFragment frag = new ImageFragment();
                        frag.setImageCode(finalIdForImage);
                        //frag.setImageCode(lines.get(position).getMyItem().getCode());
                        frag.show(manager, "Image Fragment");
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }
            });



        }
    }




    public Bitmap[] getLogo(){
        try{
            Bitmap[] RetVal = new Bitmap[0];
            File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/NorthnetStatistics/Images/");
            String[] list = dir.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    if (name.startsWith("logo")) {
                        return true;
                    }
                    return false;
                }
            });
            Bitmap[] imgs = new Bitmap[list.length];
            if(list!=null){

                for (int i = 0; i < list.length; i++) {
                    File imgFile = new  File(Environment.getExternalStorageDirectory().getPath() + "/NorthnetStatistics/Images/"+list[i]);
                    imgs[i] = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                }
            }
            return imgs;
        }catch(Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return null;
        }
    }
}
