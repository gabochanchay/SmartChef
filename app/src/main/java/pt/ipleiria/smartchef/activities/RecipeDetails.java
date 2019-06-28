package pt.ipleiria.smartchef.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import pt.ipleiria.smartchef.R;
import pt.ipleiria.smartchef.adapter.IngredientsAdapter;
import pt.ipleiria.smartchef.model.Recipe;

public class RecipeDetails extends AppCompatActivity {

    TextView recipeName;
    RecyclerView recyclerView;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);
        recipeName=findViewById(R.id.recipeNameDetail);
        imageView=findViewById(R.id.imageRecipeDetail);

        Intent intent = getIntent();
        Recipe recipe=(Recipe)intent.getSerializableExtra("recipe");
        Picasso.get().load(recipe.getImage()).into(imageView);
        recipeName.setText(recipe.getLabel());
//        ArrayList<Item> itemList = new ArrayList<Item>();

        IngredientsAdapter itemArrayAdapter = new IngredientsAdapter(R.layout.list_item_ingredient, recipe.getIngredientLines());

//        ArrayList <String> itemList = new ArrayList<String>();

//        IngredientsAdapter itemArrayAdapter = new IngredientsAdapter(R.layout.list_item_ingredient, itemList);
        recyclerView = (RecyclerView) findViewById(R.id.item_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(itemArrayAdapter);
    }
}
