package pt.ipleiria.smartchef;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.ImageProperties;
import com.google.api.services.vision.v1.model.WebDetection;
import com.google.api.services.vision.v1.model.WebEntity;
import com.google.api.services.vision.v1.model.WebImage;
import com.google.api.services.vision.v1.model.WebPage;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import pt.ipleiria.smartchef.model.Recipe;

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

    private static final String TAG = UploadImagesactivity.class.getSimpleName();

    private int imageNumber=0;

    private static Logger log= Logger.getLogger("log");

    private TextView reesultTextView;

    private ArrayList<Bitmap> bitmapArrayList;

    private List<String> objectsDetected=new ArrayList<>();
    private List<String> foodDetected;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_imagesactivity);
        reesultTextView = findViewById(R.id.resultText);
        bitmapArrayList = new ArrayList<>(4);

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
        if(bitmapArrayList.size()<4) {
            bitmapArrayList.add(bitmap);
        }else{
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
        }
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

    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth

                Bitmap bitmap = scaleBitmapDown(
                        MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                        1200);

//            callCloudVision(bitmapArrayList);

            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }


    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        String message = "";
        List<EntityAnnotation> annotations;
        log.warning(String.valueOf(response.getResponses().size()));
//        objectsDetected= new ArrayList<>();
        for(int i=0; i<response.getResponses().size(); i++){
            AnnotateImageResponse annotateImageResponse = response.getResponses().get(i);
//            message += "\n___\n# WEB DETECTION \n";
            WebDetection webDetection = annotateImageResponse.getWebDetection();
            if (webDetection != null) {
                List<WebEntity> webEntities = webDetection.getWebEntities();
                if (webEntities != null) {
//                    message += "\n§ Web Entities:\n";
                    for (WebEntity webEntity :
                            webEntities) {
                        message += String.format(Locale.US, "> %.3f: %s ", webEntity.getScore(), webEntity.getDescription());
                        log.warning(String.valueOf(webEntity.getScore()));
                        Double doubleScore=Double.valueOf(webEntity.getScore());
                        if(doubleScore.compareTo(0.5)>0) {
                            objectsDetected.add(webEntity.getDescription());
                        }
                    }
                }

                foodDetected = new ArrayList<>();
                for(String s: objectsDetected){
                    consumeTaxonomyApi(s);
                }
                String words="";
                for(String s: foodDetected){
                    words+=s;
                }
                log.warning("*******************"+words);
                reesultTextView.setText(reesultTextView.getText()+words);

//                List<WebImage> fullMatchingImages = webDetection.getFullMatchingImages();
//                if (fullMatchingImages != null) {
//                    message += "\n§ Full Matching Images:\n";
//                    for (WebImage fullMatchingImage :
//                            fullMatchingImages) {
////          message += String.format(Locale.US, "> %s \n", fullMatchingImage.getUrl());
//                    }
//                }
//
//                List<WebImage> partialMatchingImages = webDetection.getPartialMatchingImages();
//                if (partialMatchingImages != null) {
//                    message += "\n§ Partial Matching Images\n";
//                    for (WebImage partialMatchingImage :
//                            partialMatchingImages) {
////          message += String.format(Locale.US, "> %s \n", partialMatchingImage.getUrl());
//                    }
//                }
//
//                List<WebImage> visuallySimilarImages = webDetection.getVisuallySimilarImages();
//                if (visuallySimilarImages != null) {
//                    message += "\n§ Visually Similar Images\n";
//                    for (WebImage visuallySimilarImage :
//                            visuallySimilarImages) {
////          message += String.format(Locale.US, "> %s \n", visuallySimilarImage.getUrl());
//                    }
//                }
//
//                List<WebPage> pagesWithMatchingImages = webDetection.getPagesWithMatchingImages();
//                if (pagesWithMatchingImages != null) {
//                    message += "\n§ Pages With Matching Images\n";
//                    for (WebPage pageWithMatchingImage :
//                            pagesWithMatchingImages) {
////          message += String.format(Locale.US, "> %s \n", pageWithMatchingImage.getUrl());
//                    }
//                }
            }

//            message += "\n___\n# TEXT\n";
//            annotations = annotateImageResponse.getTextAnnotations();
//            if (annotations != null) {
//                message += String.format(Locale.US, "> Locale: %s.\n%s\n", annotations.get(0).getLocale(), annotations.get(0).getDescription());
//            } else {
//                message += "nothing\n";
//            }
//
//            message += "\n___\n# LANDMARKS\n";
//            annotations = annotateImageResponse.getLandmarkAnnotations();
//            if (annotations != null) {
//                for (EntityAnnotation annotation : annotations) {
//                    message += String.format(Locale.US, "> %.3f: %s (%s) \n", annotation.getScore(), annotation.getDescription(), annotation.getLocations());
//                }
//            } else {
//                message += "nothing\n";
//            }
//
//            message += "\n___\n# LOGOS\n";
//            annotations = annotateImageResponse.getLogoAnnotations();
//            if (annotations != null) {
//                for (EntityAnnotation annotation : annotations) {
//                    message += String.format(Locale.US, "%.3f: %s \n", annotation.getScore(), annotation.getDescription());
//                }
//            } else {
//                message += "nothing\n";
//            }
//
//            message += "\n___\n# FACES\n";
//            List<FaceAnnotation> faceAnnotations = annotateImageResponse.getFaceAnnotations();
//            if (faceAnnotations != null) {
//                for (FaceAnnotation annotation : faceAnnotations) {
//                    message += String.format(Locale.US, "> position:%s anger:%s joy:%s surprise:%s headwear:%s \n",
//                            annotation.getBoundingPoly(),
//                            annotation.getAngerLikelihood(),
//                            annotation.getJoyLikelihood(),
//                            annotation.getSurpriseLikelihood(),
//                            annotation.getHeadwearLikelihood());
//                }
//            } else {
//                message += "nothing\n";
//            }
//
//            message += "\n___\n# IMAGE PROPERTIES:\n\n";
//            ImageProperties imagePropertiesAnnotation = annotateImageResponse.getImagePropertiesAnnotation();
//            if (imagePropertiesAnnotation != null) {
//                message += String.format(Locale.US, "> %s \n", imagePropertiesAnnotation.getDominantColors());
//            } else {
//                message += "nothing";
//            }
        }


        return message;
    }

    private void    callCloudVision(final ArrayList<Bitmap> bitmapList) throws IOException {
//        private void callCloudVision(final Bitmap bitmapImage) throws IOException {
        // Switch text to loading

        reesultTextView.setText(R.string.loading_message);

        // Do the real work in an async task, because we need to use the network anyway
        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                try {
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    VisionRequestInitializer requestInitializer =
                            new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                                /**
                                 * We override this so we can inject important identifying fields into the HTTP
                                 * headers. This enables use of a restricted cloud platform API key.
                                 */
                                @Override
                                protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                                        throws IOException {
                                    super.initializeVisionRequest(visionRequest);

                                    String packageName = getPackageName();
                                    visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                                    String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                                    visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                                }
                            };

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(requestInitializer);

                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        for (Bitmap bm : bitmapList){
                            AnnotateImageRequest annotateImageRequest = createImagerequestPerImage(bm);

                            // Add the list of one thing to the request
                            add(annotateImageRequest);
                        }
//                        AnnotateImageRequest annotateImageRequest = createImagerequestPerImage(bitmapImage);
                    }

                        private AnnotateImageRequest createImagerequestPerImage(Bitmap bitmap) {
                            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                            // Add the image
                            Image base64EncodedImage = new Image();
                            // Convert the bitmap to a JPEG
                            // Just in case it's a format that Android understands but Cloud Vision
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                            byte[] imageBytes = byteArrayOutputStream.toByteArray();

                            // Base64 encode the JPEG
                            base64EncodedImage.encodeContent(imageBytes);
                            annotateImageRequest.setImage(base64EncodedImage);

                            // add the features we want
                            // TODO: to add or remove features just (un)comment the blocks below
                            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{


                                //              Feature textDetection = new Feature();
                                //              textDetection.setType("TEXT_DETECTION");
                                //              textDetection.setMaxResults(10);
                                //              add(textDetection);

                                //              Feature landmarkDetection = new Feature();
                                //              landmarkDetection.setType("LANDMARK_DETECTION");
                                //              landmarkDetection.setMaxResults(10);
                                //              add(landmarkDetection);

                                //              Feature logoDetection = new Feature();
                                //              logoDetection.setType("LOGO_DETECTION");
                                //              logoDetection.setMaxResults(10);
                                //              add(logoDetection);

                                //              Feature faceDetection = new Feature();
                                //              faceDetection.setType("FACE_DETECTION");
                                //              faceDetection.setMaxResults(10);
                                //              add(faceDetection);

                                //              Feature imageProperties = new Feature();
                                //              imageProperties.setType("IMAGE_PROPERTIES");
                                //              imageProperties.setMaxResults(10);
                                //              add(imageProperties);

                                Feature webDetection = new Feature();
                                webDetection.setType("WEB_DETECTION");
                                webDetection.setMaxResults(10);
                                add(webDetection);
                            }});
                            return annotateImageRequest;
                        }
                    });

                    Vision.Images.Annotate annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.setDisableGZipContent(true);
                    Log.d(TAG, "created Cloud Vision request object, sending request");

                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    String r=convertResponseToString(response);

                    return r;

                } catch (GoogleJsonResponseException e) {
                    Log.d(TAG, "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d(TAG, "failed to make API request because of other IOException " +
                            e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }

            protected void onPostExecute(String result) {

                reesultTextView.setText(result);

            }
        }.execute();
    }

    public void callCloudVisionAPI(View view){
        try {
//            for(Bitmap bm: bitmapArrayList) {
//                callCloudVision(bm);
//            }
            objectsDetected = new ArrayList<>();
            callCloudVision(bitmapArrayList);

        }catch (IOException e){
            log.warning(e.getMessage());
        }
    }




    public void consumeTaxonomyApi(final String word) {
    log.warning("-----------------------------"+word);
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api.uclassify.com/v1/uclassify/iab-taxonomy/classify?readkey=BaCk5w4RQ4y2&text="+word;
//    log.warning(url);


        final boolean food=false;
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

                        while (array.hasNext()){
                            String key = array.next();
                            Object value = response.get(key);
                            Double d=(Double) value;

                            if(d.compareTo(0.5)>0) {
                                log.warning("key:" + key+ "-"+ String.valueOf(d));
                                foodDetected.add(word);

                            }
                        }
                        } catch (Exception e) {
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



}
