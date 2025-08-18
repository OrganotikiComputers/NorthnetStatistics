package organotiki.mobile.NorthnetStatistics;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import organotiki.mobile.NorthnetStatistics.objects.GlobalVar;


public class MyApplication extends Application {

    static Realm realm;
    static Context context;
    GlobalVar gVar;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            // Create a RealmConfiguration that saves the Realm file in the app's "files" directory.
            Log.d("asdfg","IN MYAPPLICATION CREATE!!!");
            Realm.init(this);
            
            //RealmConfiguration realmConfig = new RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().allowWritesOnUiThread(true).build();
            RealmConfiguration realmConfig = new RealmConfiguration.Builder()
                    .schemaVersion(0) // Increment the schema version
                    .migration(new MyMigration()) // Add the migration
                    .allowWritesOnUiThread(true)
                    .build();
            Realm.setDefaultConfiguration(realmConfig);

            realm =Realm.getDefaultInstance();
            MyApplication.context = getApplicationContext();
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    public static Realm getRealm() {
        return realm;
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

}