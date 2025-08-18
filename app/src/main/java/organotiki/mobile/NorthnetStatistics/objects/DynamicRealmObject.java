package organotiki.mobile.NorthnetStatistics.objects;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class DynamicRealmObject extends RealmObject {
    @PrimaryKey
    private String ID; // Or any unique identifier, or use UUID
    private RealmList<KeyValue> fields;

    public DynamicRealmObject() {
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public RealmList<KeyValue> getFields() {
        return fields;
    }

    public void setFields(RealmList<KeyValue> fields) {
        this.fields = fields;
    }
}
