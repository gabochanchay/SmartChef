package pt.ipleiria.smartchef.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import java.io.File;
import java.io.IOException;
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
    private static String LOG_TAG = "UploadImagesActivity";

    ArrayList<UploadImage> bitmaps =new ArrayList<>();

    private static Logger log= Logger.getLogger("log");

    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;
    public static final String FILE_NAME = "temp.jpg";

    private static final String CLOUD_VISION_API_KEY = "AIzaSyCkRTDlHPRu1jQBdk4uKEJHpXzBNygT3EE";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final String FOOD_TAXONOMY = "food and drink_";
    private static final String FOOD = "food";
    private static final String VEGETABLE = "vegetable";
    private static final String[] WORDS_TO_FILTER= {"food","vegetable","gizzards"};
    private static final String TAG = UploadImagesactivityOldVersion.class.getSimpleName();

    private UploadImage imageSelected=null;

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

            @Override
            public void onButtonClick(View v, int position) {
                imageSelected = getDataSet().get(position);
                log.warning("position:---------"+position);
                selectSource();
            }
        });

    }

    public void selectSource(){
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadImagesActivity.this);
        builder.setMessage(R.string.dialog_select_prompt)
                .setPositiveButton(R.string.dialog_select_gallery, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startGalleryChooser();
                    }
                })
                .setNegativeButton(R.string.dialog_select_camera, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startCamera();
                    }
                });
        builder.create().show();
    }

    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                    GALLERY_IMAGE_REQUEST);
        }
    }

    public void startCamera() {
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    public File getCameraFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    private UploadImage getCorespondingImage(){

        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        ImageView imageView = findViewById(R.id.image_to_upload);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            try{
                Bitmap bitmap = scaleBitmapDown(
                        MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData()),
                        1200);
//                imageView.setImageBitmap(bitmap);
                log.warning(imageSelected.toString());
                imageSelected.setUrl("https://www.edamam.com/web-img/6fb/6fb01301f56533a5a880f9ee072b7cb2");
//                imageSelected.setBitmap(bitmap);
//                imageSelected.setImageView();
//                imageSelected.getImageView().setImageBitmap(bitmap);
                mAdapter.notifyDataSetChanged();
            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
//            uploadImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
//            uploadImage(photoUri);
            try{
                Bitmap bitmap = scaleBitmapDown(
                        MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri),
                        1200);
                imageSelected.setBitmap(bitmap);
                mAdapter.notifyDataSetChanged();

            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        }
    }

    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
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
        for (int index = 0; index < 4; index++) {
            UploadImage obj = new UploadImage();
            int w = 100, h = 100;

            Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
            Bitmap bmp = Bitmap.createBitmap(w, h, conf);
            obj.setBitmap(bmp);
            obj.setUrl("https://www.edamam.com/web-img/6fb/6fb01301f56533a5a880f9ee072b7cb2");
            results.add(index, obj);
        }
        return results;
    }
}
