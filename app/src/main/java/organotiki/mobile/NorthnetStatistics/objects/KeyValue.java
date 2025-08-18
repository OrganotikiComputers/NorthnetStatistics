package organotiki.mobile.NorthnetStatistics.objects;

import io.realm.RealmObject;

public class KeyValue extends RealmObject {
    private String key;
    private String value;

    public KeyValue() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    // Getters and setters
}