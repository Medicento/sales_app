package com.safdar.medicento.salesappmedicento.networking.util;

import android.content.Context;
import android.util.Log;

import com.safdar.medicento.salesappmedicento.R;
import com.safdar.medicento.salesappmedicento.networking.data.SalesArea;
import com.safdar.medicento.salesappmedicento.networking.data.SalesPharmacy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class SalesDataExtractor {


    public static Object initiateConnection(String stringUrl, String action, Context ctxt) {
        String jsonResponse = "";
        URL url = getUrl(stringUrl);
        ArrayList<SalesArea> salesAreasList = null;
        ArrayList<SalesPharmacy> salesPharmaciesList = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.v("Saf", e.toString());
        }
        if (action.equals(ctxt.getString(R.string.fetch_area_action))) {
            salesAreasList = extractSalesAreasFromJson(jsonResponse);
            return salesAreasList;
        } else if (action.equals(ctxt.getString(R.string.fetch_pharmacy_action))) {
            salesPharmaciesList = extractSalesPharmaciesFromJson(jsonResponse);
            return salesPharmaciesList;
        }
        return null;
    }

    private static ArrayList<SalesPharmacy> extractSalesPharmaciesFromJson(String jsonResponse) {
        Log.v("Saf", "here");
        ArrayList<SalesPharmacy> salesPharmacies = new ArrayList<>();
        try {
            JSONObject baseObject = new JSONObject(jsonResponse);
            JSONArray areasArray = baseObject.getJSONArray("pharmas");
            for (int i = 0; i < areasArray.length(); i++) {
                JSONObject areaObject = areasArray.optJSONObject(i);
                Log.v("Saf", areaObject.getString("pharma_name"));
                salesPharmacies.add(new SalesPharmacy(
                        areaObject.getString("pharma_name"),
                        areaObject.getString("pharma_address"),
                        areaObject.getString("_id"),
                        areaObject.getString("area_id")
                ));
            }
            Log.v("Saf", salesPharmacies.size() + "");
        } catch (JSONException e) {
            Log.e("Saf", e.toString());
        }
        return salesPharmacies;
    }

    private static ArrayList<SalesArea> extractSalesAreasFromJson(String jsonData) {
        ArrayList<SalesArea> salesAreas = new ArrayList<>();
        try {
            JSONObject baseObject = new JSONObject(jsonData);
            JSONArray areasArray = baseObject.getJSONArray("areas");
            for (int i = 0; i < areasArray.length(); i++) {
                JSONObject areaObject = areasArray.optJSONObject(i);
                salesAreas.add(new SalesArea(
                        areaObject.getString("area_name"),
                        areaObject.getString("area_city"),
                        areaObject.getString("area_state"),
                        areaObject.getInt("area_pincode"),
                        areaObject.getString("_id")
                ));

            }
        } catch (JSONException e) {
            Log.e("Saf", e.toString());
        }
        return salesAreas;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        if (url == null) {
            return jsonResponse;
        }
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(15000);
            urlConnection.connect();
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e("EarthQuakeExtractData", "Error response code : " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e("EarthQuakeExtractData", "Error IOExeception");
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private static URL getUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e("Saf", "URL Exception => Not able to convert to url object.");
        }
        return url;
    }

}
