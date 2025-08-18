package organotiki.mobile.NorthnetStatistics;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import io.realm.Realm;

import organotiki.mobile.NorthnetStatistics.objects.GlobalVar;


public class RecyclerViewItemsAdapter extends RecyclerView.Adapter<RecyclerViewItemsAdapter.ViewHolder> {

    Realm realm;
    private static GlobalVar gVar;
    Context mContext;
    public Bitmap[] logo;
    private int finalHeight,finalWidth;
    private DecimalFormat decim = new DecimalFormat("0.00");
    private NumberFormat priceFormat = NumberFormat.getInstance();
    private Activity activity;
    private Rect r = new Rect();
    private float historicX = Float.NaN, historicY = Float.NaN;
    private static int DELTA = 50;
    private Communicator comm;
    VolleyRequests request;

    ArrayList<String> lines;
    public RecyclerViewItemsAdapter(Context context,Activity activity, ArrayList<String> invoiceLines) {
        mContext=context;
        this.lines = invoiceLines;
        this.activity = activity;
        logo=getLogo();
        comm = (Communicator)activity;
        Log.d("asdfg", "in-" + lines.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textView;
        public String ID;
        public ImageView Image;
        public TextView Code;
        public TextView Description;
        public EditText Price;
        public LinearLayout LLWarehouse,LLWarehouse2;
        public EditText Warehouse,Warehouse2;
        public LinearLayout LLQuantityB;
        public EditText QuantityB;
        public LinearLayout LLQuantitySpecial;
        public LinearLayout LLDiscountA,LLSurCharges;
        LinearLayout LLQuantityBHeader,LLQuantitySpecialHeader,LLDiscountAHeader,LLWarehouseHeader,LLWarehouse2Header,LLSurChargesHeader;
        public EditText QuantitySpecial;
        public EditText Quantity;
        public EditText Discount;
        public EditText DiscountA;
        public TextView SurChargesValue;
        public TextView Value;
        public TextView Page;
        public TextView ItemSalesRemark;
        public TextView Payable;
        public AppCompatImageButton Details;
        public AppCompatImageButton LotDetails;
        public int ref;
        Realm realm;
        LinearLayout itemLayout;


        public ViewHolder(View v) {
            super(v);
            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();
            String ID;
            Code=(TextView)v.findViewById(R.id.code);
            Description=(TextView)v.findViewById(R.id.description);
            Price=(EditText)v.findViewById(R.id.price);;

            itemLayout = itemView.findViewById(R.id.linLayout);

            Image = (ImageView) v.findViewById(R.id.imageView_image);
            LLWarehouse=(LinearLayout)v.findViewById(R.id.linearLayout_warehouse_balance);
            LLWarehouse2=(LinearLayout)v.findViewById(R.id.linearLayout_warehouse2_balance);
            LLDiscountA=(LinearLayout)v.findViewById(R.id.linearLayout_discountA);
            Warehouse=(EditText) v.findViewById(R.id.edtx_warehouse);
            Warehouse2=(EditText) v.findViewById(R.id.edtx_warehouse2);
            LLQuantityB=(LinearLayout)v.findViewById(R.id.linearLayout_quantityB);
            QuantityB=(EditText)v.findViewById(R.id.editText_quantityB);
            LLQuantitySpecial=(LinearLayout)v.findViewById(R.id.linearLayout_quantitySpecial);
            QuantitySpecial=(EditText)v.findViewById(R.id.editText_quantitySpecial);;
            LLSurCharges=(LinearLayout)v.findViewById(R.id.linearLayout_efk);

            if (!v.getContext().getResources().getBoolean(R.bool.isTablet) || (v.getContext().getResources().getBoolean(R.bool.isTablet) && v.getContext().getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT)) {
                LLQuantityBHeader=v.findViewById(R.id.linearLayout_header_quantityB);
                LLQuantitySpecialHeader=v.findViewById(R.id.linearLayout_header_quantitySpecial);
                LLWarehouseHeader=v.findViewById(R.id.linearLayout_header_warehouse_balance);
                LLWarehouse2Header=v.findViewById(R.id.linearLayout_header_warehouse2_balance);
                LLDiscountAHeader=v.findViewById(R.id.linearLayout_header_discountA);
                LLSurChargesHeader=v.findViewById(R.id.linearLayout_header_efk);
            }


            Quantity=(EditText)v.findViewById(R.id.quantity);
            Discount=(EditText)v.findViewById(R.id.discount);
            DiscountA=(EditText)v.findViewById(R.id.editText_discountA);
            Details = (AppCompatImageButton)v.findViewById(R.id.imageButton_details);
            LotDetails = (AppCompatImageButton)v.findViewById(R.id.imageButton_lotdetails);
            Page=(TextView)v.findViewById(R.id.page);
            ItemSalesRemark=(TextView)v.findViewById(R.id.itemsalesremark);

            Value=(TextView)v.findViewById(R.id.value);
            Payable=(TextView)v.findViewById(R.id.payable);
            SurChargesValue=(TextView)v.findViewById(R.id.efk);


            int ref;


        }

        public void setBackgroundColor(int color) {
            itemLayout.setBackgroundColor(color);
        }
    }
    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerViewItemsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.listview_items, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        try{
            final String invoiceLine = lines.get(position);
            realm = Realm.getDefaultInstance();



/*

            //Setting qSpecialSetting = realm.where(Setting.class).equalTo("Name", "QuantitySpecial").findFirst();
            holder.Code.setText(invoiceLine.getMyItem().getCode());
            holder.Description.setText(invoiceLine.getMyItem().getDescription());
            holder.Price.setText(gVar.getMyCInvoice().getMyType() == null ? invoiceLine.getPriceText(priceDigits) : gVar.getMyCInvoice().getMyType().getSalesPolicy() ? invoiceLine.getPriceWithVATText(priceDigits) : invoiceLine.getPriceText(priceDigits));
            holder.Discount.setText(invoiceLine.getDiscountText());
            //holder.DiscountA.setText(invoiceLine.getDiscountA()>0? invoiceLine.getDiscountAText() :(invoiceLine.getMyItem().getDiscountAText()));
            holder.DiscountA.setText(invoiceLine.getMyItem().getDiscountAText());
            holder.Quantity.setText(invoiceLine.getQuantityText());
            holder.Value.setText(invoiceLine.getValueText());
            holder.Payable.setText(invoiceLine.getPayableText());
            holder.SurChargesValue.setText(String.valueOf(invoiceLine.getSurchargesValue()));
*/





    /*        holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (activity instanceof NewOrder){
                        ((NewOrder)activity).openItemDetailFragment(lines.get(position));
                    }else if (activity instanceof Items) {
                        ((Items) activity).openItemDetailFragment(lines.get(position));
                    }

                }
            });
*/
            holder.itemView.setTag(holder);

            File img=null;

          /*  if(!TextUtils.isEmpty(lines.get(position).getMyItem().getImagePath())){
                img = new File(lines.get(position).getMyItem().getImagePath());
                if(img!=null && img.exists()){
                    Picasso.get().load(img).into(holder.Image);
                }else{
                    if(logo==null || logo.length==0){
                        holder.Image.setImageResource(R.drawable.logoo);
                    }
                    else{
                        holder.Image.setImageBitmap(logo[0]);
                    }
                }
            }else{
                if(logo==null || logo.length==0){
                    holder.Image.setImageResource(R.drawable.logoo);
                }
                else{
                    holder.Image.setImageBitmap(logo[0]);
                }
            }*/

            holder.Image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        FragmentManager manager = activity.getFragmentManager();
                        ImageFragment frag = new ImageFragment();
                       //frag.setImageCode(lines.get(position).getMyItem().getCode());
                        frag.show(manager, "Image Fragment");
                    } catch (Exception e) {
                        Log.e("asdfg", e.getMessage(), e);
                    }
                }
            });

            // Return the completed view to render on screen
        }catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            Toast.makeText(mContext,"Εκτός Σύνδεσης",Toast.LENGTH_SHORT).show();
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

    public Bitmap[] getStockImages(final String Id) throws IOException {
        try{
            Bitmap[] RetVal = new Bitmap[0];
            File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/NorthnetStatistics/Images/");
            String[] list = dir.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    if (name.startsWith(String.valueOf(Id) + ".")) {
                        return true;
                    }
                    return false;
                }
            });
            if(list!=null){
                Bitmap[] imgs = new Bitmap[list.length];
                for (int i = 0; i < 1/*list.length*/; i++) {
                    File imgFile = new  File(Environment.getExternalStorageDirectory().getPath() + "/NorthnetStatistics/Images/"+list[i]);
                    imgs[i] = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                }
                if (imgs.length > 0) {
                    return imgs;
                }
            }

            return RetVal;
        }catch (Exception ex){
            return new Bitmap[0];
        }

    }

    public File getImage(final String Id) throws IOException {
        try{
            Bitmap[] RetVal = new Bitmap[0];
            File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/NorthnetStatistics/Images/");
            String[] list = dir.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    if (name.startsWith(String.valueOf(Id) + ".")) {
                        return true;
                    }
                    return false;
                }
            });
            if(list!=null){
                File[] imgs = new File[list.length];
                for (int i = 0; i < 1/*list.length*/; i++) {
                    File imgFile = new  File(Environment.getExternalStorageDirectory().getPath() + "/NorthnetStatistics/Images/"+list[i]);
                    imgs[i] = imgFile;
                }
                if (imgs.length > 0) {
                    return imgs[0];
                }
            }

            return null;
        }catch (Exception ex){
            return null;
        }

    }
    @Override
    public int getItemCount() {
        return lines.size();
    }


}
