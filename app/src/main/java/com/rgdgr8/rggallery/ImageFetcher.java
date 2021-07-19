package com.rgdgr8.rggallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
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
    private static final String API_KEY = "730b966dcdd0bcb9a58f3b4b9b0c6efe";
    private static final String ENDPOINT_URL = "https://www.flickr.com/services/rest/";
    private static final String GET_RECENT_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";
    public static int PAGE_NO = 1;
    private static final List<GalleryItem> mItems = new ArrayList<>();
    private static final List<GalleryItem> mCheckItems = new ArrayList<>();

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
        } finally {
            connection.disconnect();
        }
    }

    public static String getUrlData(String urlString) throws IOException {
        return new String(getUrlBytes(urlString));
    }

    private static void parseItem(JSONObject jsonObject, boolean forIdCheck) throws Exception {
        if (forIdCheck) {
            mCheckItems.clear();
        }

        JSONObject photosObject = jsonObject.getJSONObject("photos");
        JSONArray photoArray = photosObject.getJSONArray("photo");

        for (int i = 0; i < photoArray.length(); i++) {
            JSONObject photoData = photoArray.getJSONObject(i);
            if (!photoData.has("url_s")) {
                continue;
            }

            String id = photoData.getString("id");
            String title = photoData.getString("title");
            /*if (title.equals("")) {
                Log.d(TAG, "parseItem: id = " + id);
            }*/
            GalleryItem galleryItem = new GalleryItem(title
                    , id, photoData.getString("url_s"));

            if (!forIdCheck)
                mItems.add(galleryItem);
            else {
                mCheckItems.add(galleryItem);
            }
        }
    }

    private static void addItems(JSONObject jsonObject, boolean forIdCheck) throws Exception {
        parseItem(jsonObject,forIdCheck);
    }

    private static void fetchItems(String urlString, boolean forIdCheck) throws Exception {
        Log.d(TAG, "fetchItems: list size before " + mItems.size());
        String data = getUrlData(urlString);
        Log.i(TAG, "fetchItems: " + data);

        JSONObject jsonObject = new JSONObject(data);
        addItems(jsonObject, forIdCheck);
        Log.d(TAG, "parseItem: list size after " + mItems.size());
    }

    private static Uri.Builder getUriBuilder(boolean forIdCheck) {
        Uri.Builder builder = Uri.parse(ENDPOINT_URL)
                .buildUpon()
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("format", "json")
                .appendQueryParameter("nojsoncallback", "1")
                .appendQueryParameter("extras", "url_s");
        if (!forIdCheck)
            builder.appendQueryParameter("page", String.valueOf(PAGE_NO++));
        else {
            builder.appendQueryParameter("page", "1");
        }

        return builder;
    }

    public static void getRecentImages(boolean forIdCheck) throws Exception {
        Log.d(TAG, "getRecentImages: forIdCheck = " + forIdCheck);

        Uri.Builder builder = getUriBuilder(forIdCheck);
        builder.appendQueryParameter("method", GET_RECENT_METHOD);
        String urlString = builder.build().toString();
        fetchItems(urlString, forIdCheck);
    }

    public static void getSearchedImages(String query, boolean forIdCheck) throws Exception {
        Log.d(TAG, "getSearchedImages: " + query + ", forIdCheck = " + forIdCheck);

        Uri.Builder builder = getUriBuilder(forIdCheck);
        builder.appendQueryParameter("method", SEARCH_METHOD)
                .appendQueryParameter("text", query);
        String urlString = builder.build().toString();
        fetchItems(urlString, forIdCheck);
    }

    public static List<GalleryItem> getItemList() {
        return mItems;
    }

    public static List<GalleryItem> getCheckItemsList() {
        return mCheckItems;
    }
}
