package pt.ipleiria.smartchef.api;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

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
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.WebDetection;
import com.google.api.services.vision.v1.model.WebEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import pt.ipleiria.smartchef.activities.PackageManagerUtils;

public class CloudVision {

    private static Logger log= Logger.getLogger("log");

    private static final String TAG = CloudVision.class.getSimpleName();

    public static ArrayList<String> callCloudVision(final ArrayList<Bitmap> bitmapList, final String key, final String packageName,
                                        final String packageHeader, final PackageManager packageManager,
                                        final String androidCertHeader) throws IOException {
        String responseResult = "";
        try {

            responseResult = new AsyncTask<Object, Void, String>() {
                @Override
                protected String doInBackground(Object... params) {
                    try {
                        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                        VisionRequestInitializer requestInitializer =
                                new VisionRequestInitializer(key) {
                                    /**
                                     * We override this so we can inject important identifying fields into the HTTP
                                     * headers. This enables use of a restricted cloud platform API key.
                                     */
                                    @Override
                                    protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                                            throws IOException {
                                        super.initializeVisionRequest(visionRequest);

//                                    String packageName = getPackageName();
                                        visionRequest.getRequestHeaders().set(packageHeader, packageName);

                                        String sig = PackageManagerUtils.getSignature(packageManager, packageName);

                                        visionRequest.getRequestHeaders().set(androidCertHeader, sig);
                                    }
                                };

                        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                        builder.setVisionRequestInitializer(requestInitializer);

                        Vision vision = builder.build();

                        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                                new BatchAnnotateImagesRequest();
                        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {
                            {
                                for (Bitmap bm : bitmapList) {
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
                        String r = convertResponseToString(response);

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
                    log.warning("post execute");
                }
            }.execute().get();
        }
        catch(ExecutionException | InterruptedException ie ){
            log.warning("error:"+ie.getMessage());
        }
        String[] response=responseResult.split(",");
        ArrayList<String> responseArray=new ArrayList<>();
        for(String s: response){
            responseArray.add(s);
        }

        return responseArray;
    }


    private static String convertResponseToString(BatchAnnotateImagesResponse response) {
        String message = "";
        List<EntityAnnotation> annotations;
        for(int i=0; i<response.getResponses().size(); i++){
            AnnotateImageResponse annotateImageResponse = response.getResponses().get(i);
            WebDetection webDetection = annotateImageResponse.getWebDetection();
            if (webDetection != null) {
                List<WebEntity> webEntities = webDetection.getWebEntities();
                if (webEntities != null) {
                    for (WebEntity webEntity :
                            webEntities) {
                        log.warning(String.valueOf(webEntity.getScore()));
                        Double doubleScore = Double.valueOf(webEntity.getScore());
                        if (doubleScore.compareTo(0.5) > 0) {
                            message += webEntity.getDescription() + ",";
                        }
                    }
                }
            }
        }


        return message;
    }


}
