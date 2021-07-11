package com.rgdgr8.rggallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ImageFetcher {
    private static final String TAG = "ImageFetcher";
    public static final String API_KEY = "c643625a18d170ffaaf02fd5a8824cee";
    public static final String ENDPOINT_URL = "https://www.flickr.com/services/rest/";
    public static int PAGE_NO = 1;
    private static List<GalleryItem> mItems = new ArrayList<>();

    public static byte[] getUrlBytes(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            InputStream inputStream = connection.getInputStream();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            return outputStream.toByteArray();
        }finally {
            connection.disconnect();
        }
    }

    public static String getUrlData(String urlString) throws IOException{
        return new String(getUrlBytes(urlString));
    }

    private static void parseItem(JSONObject jsonObject) throws Exception{
        JSONObject photosObject = jsonObject.getJSONObject("photos");
        JSONArray photoArray = photosObject.getJSONArray("photo");

        for (int i = 0; i < photoArray.length(); i++) {
            JSONObject photoData = photoArray.getJSONObject(i);
            if (!photoData.has("url_s")) { continue; }

            photoData.getString("title");
            if(photoData.getString("title").equals("")){
                Log.d(TAG, "parseItem: id = "+photoData.getString("id"));
            }
            GalleryItem galleryItem = new GalleryItem(photoData.getString("title")
            ,photoData.getString("id"),photoData.getString("url_s"));
            mItems.add(galleryItem);
        }

        if(mItems.size()>500){
            mItems.subList(0,400).clear();
        }
    }

    public static void fetchItems() throws Exception{
        Log.d(TAG, "fetchItems: before "+mItems.size());
        String urlString = Uri.parse(ENDPOINT_URL)
                .buildUpon()
                .appendQueryParameter("method","flickr.photos.getRecent")
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("format","json")
                .appendQueryParameter("nojsoncallback", "1")
                .appendQueryParameter("extras", "url_s")
                .appendQueryParameter("page",String.valueOf(PAGE_NO++))
                .build().toString();

        String data = getUrlData(urlString);
        Log.d(TAG, "fetchItems: "+urlString);
        Log.i(TAG, "fetchItems: "+data);

        JSONObject jsonObject = new JSONObject(data);
        parseItem(jsonObject);
        Log.d(TAG, "parseItem: after "+mItems.size());
    }

    public static List<GalleryItem> getItemList(){return mItems;}
}
