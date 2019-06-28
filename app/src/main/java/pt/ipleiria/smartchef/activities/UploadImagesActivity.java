package pt.ipleiria.smartchef.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import java.util.Map;
import java.util.logging.Logger;

import pt.ipleiria.smartchef.R;
import pt.ipleiria.smartchef.adapter.CardImageAdapter;
import pt.ipleiria.smartchef.adapter.CardViewRecipeAdapter;
import pt.ipleiria.smartchef.model.Recipe;
import pt.ipleiria.smartchef.model.UploadImage;

public class UploadImagesActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private static String LOG_TAG = "CardViewRecipeList";

    ArrayList<UploadImage> bitmaps =new ArrayList<>();

    private static Logger log= Logger.getLogger("log");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_images_view);
//        consumeRecipeAPI(null, this);

        mRecyclerView = (RecyclerView) findViewById(R.id.images_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new CardImageAdapter(getDataSet());
        mRecyclerView.setAdapter(mAdapter);
        Intent intent = getIntent();
        String foodWords = intent.getStringExtra("foodWords");

//        imageView.setVisibility(View.GONE);
//        if(!foodWords.isEmpty()) {

//        }
        // Code to Add an item with default animation
        //((CardViewRecipeAdapter) mAdapter).addItem(obj, index);

        // Code to remove an item with default animation
        //((CardViewRecipeAdapter) mAdapter).deleteItem(index);
    }


    @Override
    protected void onResume() {
        super.onResume();
        ((CardImageAdapter) mAdapter).setOnItemClickListener(new CardImageAdapter
                .MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Log.i(LOG_TAG, " Clicked on Item " + position);
//                List<Recipe> recipes=getDataSet();
//                goToDetails(bitmaps.get(position));
            }
        });
    }

    private void goToDetails(Recipe recipe){
        Intent intent = new Intent(this, RecipeDetails.class);
        intent.putExtra("recipe", recipe);
        startActivity(intent);
    }

    private void validateNumberOfRecipes() {
        if(bitmaps.isEmpty()){
            showErrorMessage();
        }
    }

    private void showErrorMessage(){
        CharSequence text = "We could not find recipes with the pictures that you upload please try again changing them!";
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(this, text, duration);
        toast.show();
    }

    private ArrayList<UploadImage> getDataSet() {
        ArrayList results = new ArrayList<UploadImage>();
        for (int index = 0; index < 20; index++) {
            UploadImage obj = new UploadImage();
            obj.setUrl("https://www.edamam.com/web-img/6fb/6fb01301f56533a5a880f9ee072b7cb2");
            results.add(index, obj);
        }
        return results;
    }
}
