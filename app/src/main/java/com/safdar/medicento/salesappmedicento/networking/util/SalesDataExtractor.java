package com.safdar.medicento.salesappmedicento.networking.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.safdar.medicento.salesappmedicento.R;
import com.safdar.medicento.salesappmedicento.helperData.Constants;
import com.safdar.medicento.salesappmedicento.helperData.OrderedMedicine;
import com.safdar.medicento.salesappmedicento.networking.data.Medicine;
import com.safdar.medicento.salesappmedicento.networking.data.SalesArea;
import com.safdar.medicento.salesappmedicento.networking.data.SalesPerson;
import com.safdar.medicento.salesappmedicento.networking.data.SalesPharmacy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class SalesDataExtractor {

    static Context mContext;
    public static Object initiateConnection(String stringUrl, String action, Context ctxt, ArrayList<OrderedMedicine> data) {
        String jsonResponse = "";
        URL url = getUrl(stringUrl);
        mContext = ctxt;
        ArrayList<SalesArea> salesAreasList;
        ArrayList<SalesPharmacy> salesPharmaciesList;
        ArrayList<Medicine> medicinesDataList;
        SalesPerson salesPerson;
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
        } else if (action.equals(ctxt.getString(R.string.login_action)))  {
            salesPerson = extractSalesPersonFromJson(jsonResponse);
            return salesPerson;
        } else if (action.equals(ctxt.getString(R.string.fetch_medicine_action))) {
            medicinesDataList = extractMedicineDataFromJson(jsonResponse);
            return medicinesDataList;
        } else if (action.equals(ctxt.getString(R.string.place_order_action))) {
            String jsonData = extractJsonFromOrderItemsList(data, url);
            try {
                return sendJsonDataToServer(url, jsonData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static String extractJsonFromOrderItemsList(ArrayList<OrderedMedicine> data, URL url) {
        JSONArray orderItems = new JSONArray();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        try {
            for (OrderedMedicine orderedMedicine : data) {
                JSONObject object = new JSONObject();
                object.put("medicento_name", orderedMedicine.getMedicineName());
                object.put("company_name", orderedMedicine.getMedicineCompany());
                object.put("pharma_id", orderedMedicine.getPharmaId());
                object.put("qty", String.valueOf(orderedMedicine.getQty()));
                object.put("rate", String.valueOf(orderedMedicine.getRate()));
                object.put("cost", String.valueOf(orderedMedicine.getCost()));
                object.put("salesperson_id", sp.getString(Constants.SALE_PERSON_ID,""));
                orderItems.put(object);
            }
        }catch (JSONException e) {
            Log.v("Saf", e.toString());
        }
        return orderItems.toString();
    }

    private static ArrayList<Medicine> extractMedicineDataFromJson(String jsonResponse) {
        ArrayList<Medicine> medicines = new ArrayList<>();
        try {
            JSONObject baseObject = new JSONObject(jsonResponse);
            JSONArray areasArray = baseObject.getJSONArray("products");
            for (int i = 0; i < areasArray.length(); i++) {
                JSONObject medicineObject = areasArray.optJSONObject(i);
                medicines.add(new Medicine(
                        medicineObject.getString("medicento_name"),
                        medicineObject.getString("company_name"),
                        medicineObject.getInt("price"),
                        medicineObject.getString("_id")
                ));
            }
        } catch (JSONException e) {
            Log.e("Saf", e.toString());
        }
        return medicines;
    }

    private static SalesPerson extractSalesPersonFromJson(String jsonResponse) {
        SalesPerson salesPerson = null;
        try {
            JSONObject baseObject = new JSONObject(jsonResponse);
            JSONArray userArray = baseObject.getJSONArray("Sales_Person");
            JSONObject user = userArray.optJSONObject(0);
            salesPerson = new SalesPerson(user.getString("Name"),
                    user.getLong("Total_sales"),
                    user.getInt("Returns"),
                    user.getLong("Earnings"),
                    user.getString("_id"),
                    user.getString("Allocated_Area"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return salesPerson;
    }

    private static ArrayList<SalesPharmacy> extractSalesPharmaciesFromJson(String jsonResponse) {
        ArrayList<SalesPharmacy> salesPharmacies = new ArrayList<>();
        try {
            JSONObject baseObject = new JSONObject(jsonResponse);
            JSONArray areasArray = baseObject.getJSONArray("pharmas");
            for (int i = 0; i < areasArray.length(); i++) {
                JSONObject areaObject = areasArray.optJSONObject(i);
                salesPharmacies.add(new SalesPharmacy(
                        areaObject.getString("pharma_name"),
                        areaObject.getString("pharma_address"),
                        areaObject.getString("_id"),
                        areaObject.getString("area_id")
                ));
            }
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
                Log.e("Saf", "Error response code : " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e("Saf", "Error IOException");
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

    private static String[] sendJsonDataToServer(URL url, String jsonData) throws IOException {
        String jsonResponse = "";
        if (url == null) {
            return null;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(15000);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.connect();
            OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
            writer.write(jsonData);
            writer.close();
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e("Saf", "Error response code : " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e("Saf", "Error IOException");
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return extractIdAndDateFromJson(jsonResponse);
    }

    private static String[] extractIdAndDateFromJson(String jsonResponse) {
        String details[] = new String[2];
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            details[0] = jsonObject.getString("order_id");
            details[1] = jsonObject.getString("delivery_date");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return details;
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
