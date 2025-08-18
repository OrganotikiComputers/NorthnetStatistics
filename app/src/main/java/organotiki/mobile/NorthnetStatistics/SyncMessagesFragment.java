package organotiki.mobile.NorthnetStatistics;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

import io.realm.Realm;
import io.realm.RealmResults;
import organotiki.mobile.NorthnetStatistics.objects.GlobalVar;
import organotiki.mobile.NorthnetStatistics.objects.Item;

public class SyncMessagesFragment extends DialogFragment {

    int ParentButton;
    Button close;
    ListView listView;
    TextView title;
    Realm realm;
    GlobalVar gVar;
    ArrayList<String> messages;
    MyListAdapter myListAdapter;
    View view;
    VolleyRequests request;
    int iOrientation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            view = inflater.inflate(R.layout.fragment_sync_dialog, null);
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();
            messages = new ArrayList<>();
            String message0 = "Ο συγχρονισμός έχει ξεκινήσει, παρακαλώ περιμένετε.";

            messages.add(message0);
            title = (TextView) view.findViewById(R.id.textView_title);
            title.setText(R.string.app_name);
            listView = (ListView) view.findViewById(R.id.listView_sync_messages);
            myListAdapter = new MyListAdapter(getActivity(), messages);
            listView.setAdapter(myListAdapter);
//            listView.setStackFromBottom(true);
            listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            close = (Button) view.findViewById(R.id.button_close);
            close.setTransformationMethod(null);
            close.setText(getString(R.string.close));
            close.setEnabled(false);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            request = new VolleyRequests();
            if (ParentButton == 0) {
                request = new VolleyRequests();
                request.GetLicence(getActivity());
            } else if (ParentButton == 1) {
                request = new VolleyRequests();
                request.GetLicence(getActivity());

            } else if (ParentButton == 2) {
                request = new VolleyRequests();
                request.GetLicence(getActivity());
            }

            setCancelable(false);
            return view;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return null;
        }

    }

    private class RealmTransactionTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Show progress dialog
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Execute Realm transaction in the background
            addNewMessage("Παρακαλώ περιμένετε.Γίνεται εκκαθάριση...");

            Realm realm = Realm.getDefaultInstance();

            realm.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    public void setParentButton(int parentButton) {
        ParentButton = parentButton;
    }

    int position=0;
    public void content(){
        try{
            RealmResults<Item> items=realm.where(Item.class).findAll().sort("Code");
            if(position<items.size()){
                String a="";
                a=items.get(position).getCode();
                if(getItemImage(items.get(position),a)){
                    addNewMessage("Περάστηκε εικόνα στο είδός:"+items.get(position).getCode());
                }else{
                    addNewMessage("Δεν περάστηκε εικόνα στο είδός:"+items.get(position).getCode());
                }
                position++;
                refresh(1000);
            }
            else{
                addNewMessage("O συγχρονισμός ολοκληρώθηκε, μπορείτε να κλείσετε το παράθυρο.");
                enableButton();
            }
        }catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }

    }

    public boolean getItemImage(final Item item, final String searchField){
        try {
            File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/NorthnetStatistics/Images/");
            final String[] list = dir.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    if (name.startsWith(String.valueOf(searchField) + ".")) {
                        return true;
                    }
                    return false;
                }
            });
            if(list!=null && list.length>0){
                Arrays.sort(list);
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        item.setImagePath(Environment.getExternalStorageDirectory().getPath() + "/NorthnetStatistics/Images/"+list[0]);
                    }
                });
               return true;
            }else{
                return false;
            }
        }catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return false;
        }
    }
    private void  refresh(int miliseconds){

        final Handler handler =new Handler();

        final Runnable runnable=new Runnable() {
            @Override
            public void run() {
                content();
            }
        };
        handler.postDelayed(runnable,miliseconds);
    }

    public void addNewMessage(String message) {
        try {
            messages.add(message);
            myListAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    public void enableButton() {
        try {
            setCancelable(true);
            close.setEnabled(true);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    public void BeginSyncAfterLicence(){
        if(ParentButton==0){
            request.sendAuthenticationRequest(getActivity());
        }
    }

    public class MyListAdapter extends ArrayAdapter<String> {

        Realm realm;
        GlobalVar gVar;

        private ArrayList<String> messages;

        private class ViewHolder {
            TextView Message;
            int ref;
        }

        public MyListAdapter(Context context, ArrayList<String> messages) {
            super(context, 0, messages);
            this.messages = messages;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            try {
                // Get the data item for this position
                final String message = messages.get(position);
                realm = Realm.getDefaultInstance();
                gVar = realm.where(GlobalVar.class).findFirst();

                final ViewHolder viewHolder;
                if (convertView == null) {
                    viewHolder = new ViewHolder();
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    convertView = inflater.inflate(R.layout.listview_sync_messages, parent, false);
                    viewHolder.Message = (TextView) convertView.findViewById(R.id.textView_message);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }

                viewHolder.ref = position;
                viewHolder.Message.setText(message);

            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }
            return convertView;
        }
    }
}