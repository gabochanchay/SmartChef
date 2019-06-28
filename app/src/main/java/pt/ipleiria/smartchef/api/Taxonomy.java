package pt.ipleiria.smartchef.api;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Taxonomy {

    private static Logger log = Logger.getLogger("log");

    private static boolean food=false;

//    public booleans

    public static boolean verifyFood(String word, Context context) {

        String url = "https://api.uclassify.com/v1/uclassify/iab-taxonomy/classify?readkey=BaCk5w4RQ4y2&text=" + word;
        RequestQueue queue = Volley.newRequestQueue(context);
        log.warning("url to Taxonomy:" + url);
        final JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Iterator<String> array = response.keys();
                            String taxonmy = "food and drink_";
                            while (array.hasNext()) {
                                String key = array.next();
                                Object value = response.get(key);
                                Double d = (Double) value;
                                if (d.compareTo(0.4) > 0 && key.startsWith(taxonmy)) {
//                                    log.warning("*****"+word+"******************key:" + key+ "------------"+ String.valueOf(d));
                                    food = true;
                                }

                            }
                        } catch (Exception e) {
                            log.warning(e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {


                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        queue.add(request);
        return food;
    }

}
