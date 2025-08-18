package organotiki.mobile.NorthnetStatistics;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.github.chrisbanes.photoview.PhotoView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.ortiz.touch.TouchImageView;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

public class ImageFragment extends DialogFragment {


    public void setImageCode(String imageCode)
    {
        //imageUrl = url;
        code = imageCode;
        Log.d("asdfg", "hi");
    }

    String code;
    public int i=0;
    //public ImageView imageslide;
    Bitmap images[];
    String[] imageFileNames;
    TextView name;
    PhotoView scaleImageView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image,null);
        //imageslide=view.findViewById(R.id.imageSlide);
        scaleImageView=view.findViewById(R.id.imageSlide);
        name=(TextView) view.findViewById(R.id.name);

        final Button previous=(Button) view.findViewById(R.id.previousbutton);
        ImageButton close=(ImageButton)view.findViewById(R.id.close_dialog);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousView(v);
            }
        });
        Button next=(Button) view.findViewById(R.id.nextbutton);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextView(v);
            }
        });



        try {
            images=getStockImages(code);
            if(images.length>1){
                previous.setVisibility(View.VISIBLE);
                next.setVisibility(View.VISIBLE);
            }
            if(images.length==0) {
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
                    if(imgs.length==0){
                        scaleImageView.setImageResource(R.drawable.logoo);
                        //zoomInImageView.setImageResource(R.drawable.logoo);
                    }
                    else{
                        scaleImageView.setImageBitmap(imgs[0]);
                        name.setText(imageFileNames[0]);
                    }
                }catch (Exception e) {
                    Log.e("asdfg", e.getMessage(), e);
                }
                //imageslide.setImageResource(R.drawable.logoo);
            }
            else {
                scaleImageView.setImageBitmap(images[0]);
                name.setText(imageFileNames[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return view;

    }
    public void previousView(View v) {
        if(i==0){
            i=images.length-1;
        }
        else{
            i--;
        }
        if(images.length!=0) {
            scaleImageView.setImageBitmap(images[i]);
            name.setText(imageFileNames[i]);
        }
    }
    public void nextView(View v) {
        if(i==images.length-1){
            i=0;
        }
        else{
            i++;
        }
        if(images.length!=0){
            scaleImageView.setImageBitmap(images[i]);
            name.setText(imageFileNames[i]);
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    public Bitmap[] getStockImages(final String Id) throws IOException {
        Bitmap[] RetVal = new Bitmap[0];
        File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/NorthnetStatistics/Images/");
        String[] list = dir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(Id + ".") || name.startsWith(Id + "_");
            }
        });
        Arrays.sort(list);
        Bitmap[] imgs = new Bitmap[list.length];
        imageFileNames = list;
        for (int i = 0; i < list.length; i++) {
            File imgFile = new  File(Environment.getExternalStorageDirectory().getPath() + "/NorthnetStatistics/Images/"+list[i]);
            imgs[i] = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        }
        if (imgs.length > 0) {
            return imgs;
        }
        return RetVal;
    }
}