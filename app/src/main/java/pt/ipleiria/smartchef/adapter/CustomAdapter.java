package pt.ipleiria.smartchef.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.telephony.euicc.DownloadableSubscription;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import pt.ipleiria.smartchef.R;
import pt.ipleiria.smartchef.model.Recipe;

public class CustomAdapter extends BaseAdapter {

    Context mContexxt;

    ArrayList<Recipe> recipes = new ArrayList<>();

    public CustomAdapter(Context context, ArrayList<Recipe> recipes) {
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
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(mContexxt).inflate(R.layout.list_view_item, parent, false);
        }

        Recipe tempRecipe = (Recipe) getItem(position);
        TextView name = (TextView) convertView.findViewById(R.id.recipeName);
        ImageView image = (ImageView) convertView.findViewById(R.id.recipeImage);
        name.setText(tempRecipe.getLabel());
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);
//        image.setImageBitmap(getImageBitmap(tempRecipe.getUri()));
//        Picasso.with(mContexxt).load(tempRecipe.getUri()).into(image);

        Picasso.get().load(tempRecipe.getImage()).into(image);
        return convertView;
    }


    private Bitmap getImageBitmap(String url) {
        Bitmap bm = null;
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (IOException e) {
            Log.e("Error getting bitmap", e.getMessage());
        }
        return bm;
    }


}
