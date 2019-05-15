package pt.ipleiria.smartchef.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

import pt.ipleiria.smartchef.model.Recipe;

public class CustomAdapter extends BaseAdapter {

    Context mContexxt;

    ArrayList<Recipe> recipes = new ArrayList<>();

    public CustomAdapter(Context context, ArrayList<Recipe> recipes){
        mContexxt = context;
        this.recipes = recipes;

    }

    @Override
    public int getCount() {
        return recipes.size();
    }

    @Override
    public Object getItem(int position) {
        return recipes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
