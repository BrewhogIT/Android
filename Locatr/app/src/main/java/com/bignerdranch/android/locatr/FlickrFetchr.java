package com.bignerdranch.android.locatr;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FlickrFetchr {
    private static final String TAG = "FlickrFetchr";
    private static final String API_KEY = "b97565cb39811c157e51eea4c2b14be5";
    private static int pageNumber = 1;
    private static int maxPage = 0;

    private static final String FETCH_RECENTS_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";
    private static final Uri ENDPOINT = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("api_key",API_KEY)
                    .appendQueryParameter("format","json")
                    .appendQueryParameter("nojsoncallback","1")
                    .appendQueryParameter("extras","url_s")
                    .build();



    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage() +
                        ": with " + urlSpec);
            }

            int byteReads = 0;
            byte[] buffer = new byte[1024];
            while((byteReads = in.read(buffer)) > 0){
                out.write(buffer,0,byteReads);
            }
            out.close();
            return out.toByteArray();
        }finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec)throws IOException{
        return new String(getUrlBytes(urlSpec));
    }

    private List<GalleryItem> downloadGalleryItems(String url){
        List<GalleryItem> items = new ArrayList<>();

        try {
            String jsonString = getUrlString(url);
            Log.i(TAG,"Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
//            parseItems(items,jsonBody);
            gsonParseItem(items,jsonBody);
        } catch (IOException ioe) {
            Log.e(TAG,"Failed to fetch mGalleryItems",ioe);
        } catch (JSONException je){
            Log.e(TAG, "Failed to parse JSON", je);
        }
        Log.i(TAG,"items length after return is: " + items.size());

        return items;
    }

    private String buildUrl(String method, String query){
        String strPageNumber = String.valueOf(pageNumber);

        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method",method)
                .appendQueryParameter("page",strPageNumber);

        if (method.equals(SEARCH_METHOD)){
            uriBuilder.appendQueryParameter("text",query);
        }
        return uriBuilder.build().toString();
    }

    public List<GalleryItem> fetchRecentPhotos(){
        String url = buildUrl(FETCH_RECENTS_METHOD,null);
        return downloadGalleryItems(url);
    }

    public List<GalleryItem> searchPhotos(String query){
        String url = buildUrl(SEARCH_METHOD,query);
        return downloadGalleryItems(url);
    }

    public void parseItems(List<GalleryItem> items, JSONObject jsonBody)
            throws JSONException {
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        for (int i = 0; i < photoJsonArray.length(); i++){
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

            GalleryItem item = new GalleryItem();
            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));

            if (!photoJsonObject.has("url_s")){
                continue;
            }

            item.setUrl(photoJsonObject.getString("url_s"));
            item.setOwner(photoJsonObject.getString("owner"));
            items.add(item);
        }
    }

    public void gsonParseItem(List<GalleryItem> items, JSONObject jsonBody)
            throws JSONException {
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        String stringPhotoJsonArray = photosJsonObject.optJSONArray("photo").toString();
        maxPage = photosJsonObject.getInt("pages");

        Type listItemType = new TypeToken<List<GalleryItem>>(){}.getType();
        ArrayList<GalleryItem> list = new Gson().fromJson(stringPhotoJsonArray,listItemType);

        Iterator<GalleryItem> itemIterator = list.iterator();
        while (itemIterator.hasNext()){
            GalleryItem item = itemIterator.next();

            if (item.getUrl() != null){
                items.add(item);
            }
        }
    }

    public static void updatePage(){
        pageNumber++;
    }

    public static int getMaxPage(){
        return maxPage;
    }

    public static int getPageNumber(){
        return pageNumber;
    }

}
