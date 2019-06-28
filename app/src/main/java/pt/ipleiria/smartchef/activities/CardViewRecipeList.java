/*
 * Copyright (c) 2017. Truiton (http://www.truiton.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * Mohit Gupt (https://github.com/mohitgupt)
 *
 */

package pt.ipleiria.smartchef.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import pt.ipleiria.smartchef.R;
import pt.ipleiria.smartchef.adapter.CardViewRecipeAdapter;
import pt.ipleiria.smartchef.model.Recipe;


public class CardViewRecipeList extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private static String LOG_TAG = "CardViewRecipeList";

    ArrayList<Recipe> recipesList=new ArrayList<>();

    private static Logger log= Logger.getLogger("log");
    private ImageView imageView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_view_recipe);
//        consumeRecipeAPI(null, this);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new CardViewRecipeAdapter(recipesList);
        mRecyclerView.setAdapter(mAdapter);
        Intent intent = getIntent();
        String foodWords = intent.getStringExtra("foodWords");
        log.warning("words receied///////////////////////////////////:"+foodWords);
        textView = findViewById(R.id.processing_text);
        textView.setText(".........Processsing...........");
        imageView = findViewById(R.id.sad_face);
        Picasso.get().load("https://image.flaticon.com/icons/png/512/15/15135.png").into(imageView);
        imageView.setVisibility(View.GONE);
//        if(!foodWords.isEmpty()) {
            consumeRecipeAPI("asdasdasas", this);
//        }
        // Code to Add an item with default animation
        //((CardViewRecipeAdapter) mAdapter).addItem(obj, index);

        // Code to remove an item with default animation
        //((CardViewRecipeAdapter) mAdapter).deleteItem(index);
    }


    @Override
    protected void onResume() {
        super.onResume();
        ((CardViewRecipeAdapter) mAdapter).setOnItemClickListener(new CardViewRecipeAdapter
                .MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Log.i(LOG_TAG, " Clicked on Item " + position);
//                List<Recipe> recipes=getDataSet();
                goToDetails(recipesList.get(position));
            }
        });
    }

    private void goToDetails(Recipe recipe){
        Intent intent = new Intent(this, RecipeDetails.class);
        intent.putExtra("recipe", recipe);
        startActivity(intent);
    }

    public void consumeRecipeAPI(String Foodwords, final Context context){
        log.warning("BOOOOOOOTONNNN");
        EditText et=findViewById(R.id.editText4);
//        String food=et.getText().toString();
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api.edamam.com/search?q="+Foodwords+"&app_id=00fef183&app_key=54f40f77cbdd0f866bee7e8d4c7170a3&from=0&to=10&calories=591-722&health=alcohol-free";
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
//                            ArrayList<Recipe> bitmaps=new ArrayList<>();
                            for (int i = 0; i < arrayRecipes.length(); i++) {
                                JSONObject recipeJson=arrayRecipes.getJSONObject(i);
                                Gson gson = new Gson();
                                Object r=recipeJson.get("recipe");
                                Recipe contact = gson.fromJson(r.toString(), Recipe.class);
                                recipesList.add(contact);
                            }
                            for (Recipe r : recipesList) {
                                log.warning(r.getUri());
                            }
                            mAdapter.notifyDataSetChanged();
                            if(recipesList.isEmpty()){
                                imageView.setVisibility(View.VISIBLE);
                                textView.setVisibility(View.GONE);
                            }
                            validateNumberOfRecipes();
                        }catch (JSONException e){
//                            log.warning(e.getMessage());
                        }

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

    private void validateNumberOfRecipes() {
        if(recipesList.isEmpty()){
            showErrorMessage();
        }
    }

    private void showErrorMessage(){
        CharSequence text = "We could not find recipes with the pictures that you upload please try again changing them!";
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(this, text, duration);
        toast.show();
    }

    private ArrayList<Recipe> getDataSet() {
        ArrayList results = new ArrayList<Recipe>();
        for (int index = 0; index < 20; index++) {
            Recipe obj = new Recipe();
            obj.setLabel("Prueba"+index);
            obj.setImage("https://www.edamam.com/web-img/6fb/6fb01301f56533a5a880f9ee072b7cb2");
            ArrayList<String> ingredients=new ArrayList<>();
            ingredients.add("Ingredient 1");
            ingredients.add("Ingredient 2");
            ingredients.add("Ingredient 3");
            ingredients.add("Ingredient 4");
            ingredients.add("Ingredient 5");
            ingredients.add("Ingredient 6");
            ingredients.add("Ingredient 1");
            ingredients.add("Ingredient 1");
            ingredients.add("Ingredient 1");
            ingredients.add("Ingredient 1");
            ingredients.add("Ingredient 1");
            ingredients.add("Ingredient 1");
            ingredients.add("Ingredient 1");
            ingredients.add("Ingredient 1");
            obj.setIngredientLines(ingredients);

            results.add(index, obj);
        }
        return results;
    }
}
