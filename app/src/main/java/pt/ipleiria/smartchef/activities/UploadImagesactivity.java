package pt.ipleiria.smartchef.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
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
import pt.ipleiria.smartchef.api.CloudVision;

public class UploadImagesactivity extends AppCompatActivity {

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
    private static final String TAG = UploadImagesactivity.class.getSimpleName();

    private int imageNumber=0;

    private static Logger log= Logger.getLogger("log");

    private TextView reesultTextView;

    private ArrayList<Bitmap> bitmapArrayList;

    private List<String> objectsDetected=new ArrayList<>();
    private List<String> foodDetected;
    private ListView listView;
    private boolean finished=false;
    private int wordsNumberFound=0;
    private int wordsNumberProcessed=0;
    private String foodWords="";
    private Bitmap bmp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_imagesactivity);
        reesultTextView = findViewById(R.id.resultText);
        bitmapArrayList = new ArrayList<>(4);
        int w = 100, h = 100;

        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        bmp = Bitmap.createBitmap(w, h, conf);
        bitmapArrayList.add(bmp);
        bitmapArrayList.add(bmp);
        bitmapArrayList.add(bmp);
        bitmapArrayList.add(bmp);

        findViewById(R.id.loadingPanel).setVisibility(View.INVISIBLE);
    }

    public void selectSource(View view){

        String idString = view.getResources().getResourceEntryName(view.getId());
//        String idString= String.valueOf(view.getId());
        if(idString.compareTo("btn1")==0){
            imageNumber = 1;
        }
        if(idString.compareTo("btn2")==0){
            imageNumber = 2;
        }
        if(idString.compareTo("btn3")==0){
            imageNumber = 3;
        }
        if(idString.compareTo("btn4")==0){
            imageNumber = 4;
        }
        log.warning(idString);
        log.warning(String.valueOf(imageNumber));
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadImagesactivity.this);
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

    private ImageView setCorespondingImage(){
        if(imageNumber == 1){
            return (ImageView) findViewById(R.id.image1);
        }
        if(imageNumber == 2){
            return (ImageView) findViewById(R.id.image2);
        }
        if(imageNumber == 3){
            return (ImageView) findViewById(R.id.image3);
        }
        if(imageNumber == 4){
            return (ImageView) findViewById(R.id.image4);
        }
        return null;
    }

    private void setontheArray(Bitmap bitmap){

//        if(){
//
//        }
//        if(bitmapArrayList.size() < 4) {
//            bitmapArrayList.add(bitmap);
//        }else{
            if(imageNumber == 1){
            bitmapArrayList.set(0, bitmap);
        }
        if(imageNumber == 2){
            bitmapArrayList.set(1, bitmap);
        }
        if(imageNumber == 3){
            bitmapArrayList.set(2, bitmap);
        }
        if(imageNumber == 4){
            bitmapArrayList.set(3, bitmap);
        }
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView imageView = setCorespondingImage();
        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            try{
            Bitmap bitmap = scaleBitmapDown(
                    MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData()),
                    1200);
            imageView.setImageBitmap(bitmap);
            setontheArray(bitmap);
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
                imageView.setImageBitmap(bitmap);
                setontheArray(bitmap);
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

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
                break;
        }
    }

    public void callCloudVisionAPI(View view){
        boolean empty=validateImages();
        if(empty){
            CharSequence text = "Please upload at least one image!";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(this, text, duration);
            toast.show();
        }else {
            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
            foodWords = "";
            try {
                objectsDetected = new ArrayList<>();
//            callCloudVision(bitmapArrayList);
                foodDetected = new ArrayList<>();
                List<String> responseArray = CloudVision.callCloudVision(bitmapArrayList, CLOUD_VISION_API_KEY, getPackageName(), ANDROID_PACKAGE_HEADER, getPackageManager(), ANDROID_CERT_HEADER);
                wordsNumberFound = responseArray.size();
                for (String s : responseArray) {
                    log.warning("-------------------" + s);
                }
                log.warning(String.valueOf(bitmapArrayList.size()));
//            Toast.makeText(UploadImagesactivity.this, wordsNumberFound,
//                    Toast.LENGTH_LONG).show();
//            log.warning("------------------------------------"+wordsNumberFound);
                for (String s : responseArray) {
                    log.warning("word to Taxonomy:" + s);
                    consumeTaxonomyApi(URLEncoder.encode(s, "UTF-8"), this);
                }
            } catch (IOException e) {
                log.warning(e.getMessage());
            }
            Intent intent = new Intent(this, CardViewRecipeList.class);
            intent.putExtra("foodWords", foodWords);
            startActivity(intent);
        }
    }

    private boolean validateImages(){
        boolean empty=true;
        for(Bitmap b: bitmapArrayList){
            if(!b.equals(bmp)){
                empty=false;
            }
        }
        return empty;
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
//                        log.warning(response.toString());
                        try {
                        Iterator<String> array= response.keys();
                        String taxonmy="food and drink_";
//                        taxonmy=URLEncoder.encode(taxonmy,"UTF-8");
                        while (array.hasNext()){
                            String key = array.next();
//                            key=URLEncoder.encode(key,"UTF-8");
                            Object value = response.get(key);
                            Double d=(Double) value;
//                            log.warning("key:" + key+ "-"+ String.valueOf(d));
                            if(d.compareTo(0.4)>0 && key.startsWith(taxonmy)) {
                                log.warning("*****"+word+"******************key:" + key+ "------------"+ String.valueOf(d));
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
//                        Log.e("Response", error.getMessage());

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
//            int cont=0;
//            for(String f: foods){
//                log.warning("1"+s+"----2:"+f);
//
//                if(s.equals(f)){
//                    cont++;
//                }
//            }
//            log.warning("contador-----------:"+cont);
//            if(cont==1 && !foodsFinal.contains(s)){
//                log.warning("addding-----------"+s);
                foodsFinal.add(s);
//            }
        }
        return foodsFinal;
    }
}
