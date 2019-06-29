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
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import pt.ipleiria.smartchef.R;
import pt.ipleiria.smartchef.adapter.CardImageAdapter;
import pt.ipleiria.smartchef.api.CloudVision;
import pt.ipleiria.smartchef.model.UploadImage;
import pt.ipleiria.smartchef.util.PermissionUtils;

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
    private ArrayList<UploadImage> images;
    private ArrayList<Bitmap> bitmapArrayList;
    private int wordsNumberFound=0;
    private int wordsNumberProcessed=0;
    private String foodWords="";
    private List<String> foodDetected;
    private List<String> objectsDetected=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_images_view);

        mRecyclerView = (RecyclerView) findViewById(R.id.images_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        images = getDataSet();
        mAdapter = new CardImageAdapter(images);
        mRecyclerView.setAdapter(mAdapter);
        findViewById(R.id.loadingPanel).setVisibility(View.INVISIBLE);

//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

    }


    @Override
    protected void onResume() {
        super.onResume();
        ((CardImageAdapter) mAdapter).setOnItemClickListener(new CardImageAdapter
                .MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Log.i(LOG_TAG, " Clicked on Item " + position);
            }

            @Override
            public void onButtonClick(View v, int position) {
                imageSelected = images.get(position);
                log.warning("position:---------"+position);
                selectSource();
            }

            @Override
            public void onDeleteButtonClick(View v, int position) {
                removeImage(position);
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

    public void removeImage(int position){
        UploadImage uploadImage=images.get(position);
        images.remove(uploadImage);
        mAdapter.notifyDataSetChanged();
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

    private void modifyImages(UploadImage image){
        for(UploadImage i: images) {
            if (image.getId()==i.getId()) {
                i.setUrl(image.getUrl());
                i.setBitmap(image.getBitmap());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            try{
                Bitmap bitmap = scaleBitmapDown(
                        MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData()),
                        1200);
                imageSelected.setUrl(data.getData().toString());
                imageSelected.setBitmap(bitmap);
                modifyImages(imageSelected);
                mAdapter.notifyDataSetChanged();
            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            try{
                Bitmap bitmap = scaleBitmapDown(
                        MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri),
                        1200);
                imageSelected.setUrl(photoUri.toString());
                imageSelected.setBitmap(bitmap);
                modifyImages(imageSelected);
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

    private ArrayList<UploadImage> getDataSet() {
        ArrayList results = new ArrayList<UploadImage>();
        for (int index = 0; index < 1; index++) {
            UploadImage obj = new UploadImage();
            obj.setId(index);
            results.add(index, obj);
        }
        return results;
    }

    public void addImage(View view){
        CharSequence text = "Item added";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this, text, duration);
        toast.show();
        UploadImage uploadImage=new UploadImage();
        uploadImage.setId(mAdapter.getItemCount()+1);
        images.add(uploadImage);
        mAdapter.notifyDataSetChanged();
    }

    private boolean validateImages(){
        boolean empty=false;
        for(UploadImage uploadImage: images){
            if(uploadImage.getBitmap()==null){
                empty=true;
            }
        }
        return empty;
    }

    public void callImageReconition(View view){
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        boolean empty=validateImages();
        if(empty){
            CharSequence text = "Please upload images to every item, or delete the image you are not using";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(this, text, duration);
            toast.show();
        }else {
            fillBitMapArrayList();
            foodWords = "";
            try {
                objectsDetected = new ArrayList<>();
                foodDetected = new ArrayList<>();
                List<String> responseArray = CloudVision.callCloudVision(bitmapArrayList, CLOUD_VISION_API_KEY, getPackageName(), ANDROID_PACKAGE_HEADER, getPackageManager(), ANDROID_CERT_HEADER);
                wordsNumberFound = responseArray.size();
                for (String s : responseArray) {
                    log.warning("-------------------" + s);
                }
                for (String s : responseArray) {
                    log.warning("word to Taxonomy:" + s);

                    consumeTaxonomyApi(URLEncoder.encode(s, "UTF-8"), this);
//                    boolean food=Taxonomy.verifyFood(URLEncoder.encode(s, "UTF-8"), this);
//                    if(food){
//                        foodDetected.add(s);
//                    }
                }
//                Set<String> foods=filterTypesOfFood(foodDetected);
//                for(String s: foods){
//                    log.warning("word to api recipes:--------:" + s);
//                    foodWords=foodWords+","+s;
//                }
//                wordsNumberProcessed=0;
//                log.warning("foooooooooooood:"+foodWords);
//                Intent intent = new Intent(this, CardViewRecipeList.class);
//                intent.putExtra("foodWords", foodWords);
//                startActivity(intent);
//                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            } catch (IOException e) {
                log.warning(e.getMessage());
            }
        }
    }

    public void fillBitMapArrayList(){
        bitmapArrayList=new ArrayList<>();
        for(UploadImage u: images){
            bitmapArrayList.add(u.getBitmap());
        }
    }

    public void consumeTaxonomyApi(final String word, final Context context) {
        String url = "https://api.uclassify.com/v1/uclassify/iab-taxonomy/classify?readkey=BaCk5w4RQ4y2&text="+word;
        RequestQueue queue = Volley.newRequestQueue(this);
        log.warning("url to Taxonomy:"+ url);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Iterator<String> array= response.keys();
                            String taxonmy="food and drink_";
                            while (array.hasNext()){
                                String key = array.next();
                                Object value = response.get(key);
                                Double d=(Double) value;
                                if(d.compareTo(0.4)>0 && key.startsWith(taxonmy)) {
//                                    log.warning("*****"+word+"******************key:" + key+ "------------"+ String.valueOf(d));
                                    foodDetected.add(word);
                                }
                            }
                        } catch (Exception e) {
                            log.warning(e.getMessage());
                        }
                        wordsNumberProcessed++;
                        validateEveryWordIsProcesed();
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
    }

    private void validateEveryWordIsProcesed() {
        log.warning("nuuuuuuuuuuuuuumeeeeerooooo:"+ wordsNumberFound);
        log.warning("proceseddddddddddddd:"+ wordsNumberProcessed);
        if(wordsNumberProcessed==wordsNumberFound){
            log.warning("YEEEEEEEEEEEEEEEEEEEEEEEEEEES");
            for(String s: foodDetected){
                log.warning("word with out filtered::--------:" + s);
            }
            Set<String> foods=filterTypesOfFood(foodDetected);

            for(String s: foods){
                log.warning("word to api recipes:--------:" + s);
                foodWords=foodWords+","+s;
            }
            wordsNumberProcessed=0;
            log.warning("foooooooooooood:"+foodWords);
//            Intent intent = new Intent(this, RecipeList.class);
            Intent intent = new Intent(this, CardViewRecipeList.class);
            intent.putExtra("foodWords", foodWords);
            startActivity(intent);
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
    }

    private Set<String> filterTypesOfFood(List<String> foods){
        List<String> foodsAux=new ArrayList<>(foods);
        for(String s: foodsAux){
            for(String f: WORDS_TO_FILTER){
                if(s.toUpperCase().trim().equals(f.toUpperCase())){
                    foods.remove(s);
                }
            }
        }
        ArrayList<String> repeatedWords=new ArrayList<>();
        Set<String> foodsFinal=new HashSet<>();
        for(String s: foods){
            foodsFinal.add(s);
        }
        return foodsFinal;
    }
}
