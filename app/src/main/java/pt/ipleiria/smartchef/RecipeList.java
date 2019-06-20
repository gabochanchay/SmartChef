package pt.ipleiria.smartchef;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import pt.ipleiria.smartchef.adapter.CustomAdapter;
import pt.ipleiria.smartchef.model.Recipe;

public class RecipeList extends AppCompatActivity {

    private static Logger log= Logger.getLogger("log");

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);
        Intent intent = getIntent();
        String foodWords = intent.getStringExtra("foodWords");
        log.warning("words receied///////////////////////////////////:"+foodWords);
        consumeRecipeAPI(foodWords);
    }

    public void consumeRecipeAPI(String Foodwords){
        log.warning("BOOOOOOOTONNNN");
        EditText et=findViewById(R.id.editText4);
//        String food=et.getText().toString();
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api.edamam.com/search?q="+Foodwords+"&app_id=00fef183&app_key=54f40f77cbdd0f866bee7e8d4c7170a3&from=0&to=3&calories=591-722&health=alcohol-free";
        log.warning(url);
//    url = url.concat(foodName);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                        Log.d("Response", response.toString());
//                log.warning(response.toString());
                        try {
                            Object recipes=response.get("hits");
                            JSONArray arrayRecipes=response.getJSONArray("hits");
                            ArrayList<Recipe> recipesList=new ArrayList<>();
                            for (int i = 0; i < arrayRecipes.length(); i++) {
                                JSONObject recipeJson=arrayRecipes.getJSONObject(i);
                                Gson gson = new Gson();
                                Object r=recipeJson.get("recipe");
                                Recipe contact = gson.fromJson(r.toString(), Recipe.class);
//                                log.warning(arrayRecipes.getJSONObject(i).toString());
//                    contact= new Con
                                recipesList.add(contact);
                            }
                            for (Recipe r : recipesList) {
                                log.warning(r.getUri());
//                                log.warning(r.getLabel());
//                                log.warning(r.getImage());
//                                for(String s : r.getIngredientLines()){
//                                    log.warning(s);
//                                }
                            }
                            CustomAdapter myCustomAdapter = new CustomAdapter(RecipeList.this ,recipesList);
                            listView = findViewById(R.id.listView_contacts);
                            listView.setAdapter(myCustomAdapter);
                        }catch (JSONException e){
                            log.warning(e.getMessage());
                        }
//                Gson gson = new Gson();
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
        queue.add(request);
    }
}
