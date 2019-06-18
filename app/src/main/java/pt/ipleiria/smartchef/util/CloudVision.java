package pt.ipleiria.smartchef.util;

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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import pt.ipleiria.smartchef.PackageManagerUtils;
import pt.ipleiria.smartchef.R;
import pt.ipleiria.smartchef.UploadImagesactivity;

public class CloudVision {

    private static Logger log= Logger.getLogger("log");

    private static final String TAG = CloudVision.class.getSimpleName();

    public static ArrayList<String> callCloudVision(final ArrayList<Bitmap> bitmapList, final String key, final String packageName,
                                        final String packageHeader, final PackageManager packageManager,
                                        final String androidCertHeader) throws IOException {
//        private void callCloudVision(final Bitmap bitmapImage) throws IOException {
        // Switch text to loading

//        reesultTextView.setText(R.string.loading_message);

        // Do the real work in an async task, because we need to use the network anyway
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
//                reesultTextView.setText(result);

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
//                        message += String.format(Locale.US, "> %.3f: %s ", webEntity.getScore(), webEntity.getDescription());
                        log.warning(String.valueOf(webEntity.getScore()));
                        Double doubleScore=Double.valueOf(webEntity.getScore());
                        if(doubleScore.compareTo(0.5)>0) {
//                            objectsDetected.add(webEntity.getDescription());
                            message+= webEntity.getDescription()+",";
                        }
                    }
                }

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


}
