package com.bignerdranch.android.photogallery;

import android.net.Uri;
import android.os.AsyncTask;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class FlickrFetchr {
    private final String TAG = "FlickrFetchr";
    private final String API_KEY = "b97565cb39811c157e51eea4c2b14be5";


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

    public List<GalleryItem> fetchItems(){

        List<GalleryItem> items = new ArrayList<>();

        try {
            String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method","flickr.photos.getRecent")
                    .appendQueryParameter("api_key",API_KEY)
                    .appendQueryParameter("format","json")
                    .appendQueryParameter("nojsoncallback","1")
                    .appendQueryParameter("extras","url_s")
                    .build().toString();
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
            items.add(item);
        }
    }

    public void gsonParseItem(List<GalleryItem> items, JSONObject jsonBody)
            throws JSONException {
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        String stringPhotoJsonArray = photosJsonObject.optJSONArray("photo").toString();

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

}
