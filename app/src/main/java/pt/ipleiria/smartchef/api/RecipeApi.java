package pt.ipleiria.smartchef.api;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class RecipeApi {

    private static Logger log= Logger.getLogger("logApi");

    public static void searchRecipes(String foodName){

        String url = "https://api.edamam.com/search?q=chicken&app_id=00fef183&app_key=54f40f77cbdd0f866bee7e8d4c7170a3&from=0&to=3&calories=591-722&health=alcohol-free";
        url = url.concat(foodName);
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("Response", response.toString());
                        log.warning(response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Response", error.getMessage());

                    }
                }
        )
        {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

    }
}
