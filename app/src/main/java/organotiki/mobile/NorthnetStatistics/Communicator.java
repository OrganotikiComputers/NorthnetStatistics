package organotiki.mobile.NorthnetStatistics;

import org.json.JSONObject;



public interface Communicator {
    void respondTotalChanged();
    void respondVolleyRequestFinished(Integer position,JSONObject jsonObject);
}
