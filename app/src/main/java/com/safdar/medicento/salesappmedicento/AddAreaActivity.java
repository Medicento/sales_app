package com.safdar.medicento.salesappmedicento;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AddAreaActivity extends AppCompatActivity implements View.OnClickListener{

    Button totalSales, oredrs, returns, earnings,profile,pharmab,areab,newPharma,newArea,fetch;
    AutoCompleteTextView pharma_spinner,area_spinner;
    Spinner slots;
    CoordinatorLayout coordinatorLayout;
    View sales_person;
    BottomSheetBehavior bottomSheetBehavior;
    public static final String FoodNameArray = "area_name";
    public static final String FoodName = "name";
    public static final String JSON_ARRAY = "result";
    private JSONArray result;
    String  FName;
    private ArrayList<String> arrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_area_details);
        fetch = (Button) findViewById(R.id.fetch);
        totalSales = (Button) findViewById(R.id.totalSales);
        oredrs = (Button) findViewById(R.id.orders);
        returns = (Button) findViewById(R.id.returns);
        earnings = (Button) findViewById(R.id.earnings);
        profile = (Button) findViewById(R.id.profile);
        newPharma = (Button) findViewById(R.id.add_new_pharma);
        newArea = (Button) findViewById(R.id.add_new_area);
        slots = (Spinner) findViewById(R.id.slots);
        pharmab = (Button) findViewById(R.id.pharmacyb);
        areab = (Button) findViewById(R.id.areab);
        pharma_spinner = (AutoCompleteTextView) findViewById(R.id.pharmacy_spinner);
        area_spinner = (AutoCompleteTextView) findViewById(R.id.area_spinner);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.colayout);
        sales_person = coordinatorLayout.findViewById(R.id.bottom_sheet);

        bottomSheetBehavior = BottomSheetBehavior.from(sales_person);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        Button btn = findViewById(R.id.add_new_area);
        btn.setOnClickListener(this);

        fetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = area_spinner.getText().toString();
                Uri uri = Uri.parse("http://192.168.0.9/getPharma.php")
                        .buildUpon()
                        .appendQueryParameter("area_name",s)
                        .appendQueryParameter("search","Submit Query")
                        .build();
                RequestQueue requestQueue = Volley.newRequestQueue(AddAreaActivity.this);
                StringRequest stringRequest = new StringRequest(Request.Method.GET, uri.toString(),
                        new Response.Listener<String>() {
                            ArrayAdapter arrayAdapter;
                            @Override
                            public void onResponse(String response) {
                                String s;
                                arrayList = new ArrayList<>();
                                try {
                                    JSONObject jo = new JSONObject(response);
                                    JSONArray ja = jo.getJSONArray("result");
                                    int count = 0;
                                    while (count < ja.length()) {
                                        JSONObject joa = ja.getJSONObject(count);
                                        s = joa.getString("firm_name");
                                        arrayList.add(s);
                                        count++;
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                arrayAdapter = new ArrayAdapter(AddAreaActivity.this, R.layout.support_simple_spinner_dropdown_item, arrayList);
                                pharma_spinner.setAdapter(arrayAdapter);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
                );
                requestQueue.add(stringRequest);
            }
        });
        //Spinners
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.slots, android.R.layout.simple_list_item_1);
        slots.setAdapter(adapter);
        arrayList = new ArrayList<String>();
        getdata();

        ArrayAdapter<CharSequence> aadapter = ArrayAdapter.createFromResource(this, R.array.pharma, android.R.layout.simple_list_item_1);
        pharma_spinner.setAdapter(aadapter);

        Button btn2 = findViewById(R.id.openPlaceOrder);

        btn2.setOnClickListener(this);
        totalSales.setOnClickListener(this);
        oredrs.setOnClickListener(this);
        returns.setOnClickListener(this);
        earnings.setOnClickListener(this);
        profile.setOnClickListener(this);
        areab.setOnClickListener(this);
        pharmab.setOnClickListener(this);
        newPharma.setOnClickListener(this);
        newArea.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        pharma_spinner = (AutoCompleteTextView) findViewById(R.id.pharmacy_spinner);
        area_spinner = (AutoCompleteTextView) findViewById(R.id.area_spinner);
        switch (v.getId()) {

            case R.id.profile:    intent = new Intent(AddAreaActivity.this, SalesPersonDetails.class);
                                  intent.putExtra("text",((Button)v).getText());
                                  startActivity(intent);
                                  break;
            case R.id.pharmacyb:  pharma_spinner.showDropDown();
                                  break;
            case R.id.areab:      area_spinner.showDropDown();
                                  break;
            case R.id.openPlaceOrder:
                intent = new Intent(this, PlaceOrdersActivity.class);
                startActivity(intent);
                break;
            case R.id.add_new_pharma:    intent = new Intent(AddAreaActivity.this, NewPharmacy.class);
                                         intent.putExtra("area","Shastri Nagar");
                                         intent.putExtra("city","New Delhi");
                                         intent.putExtra("state","Delhi");
                                         intent.putExtra("pincode","110031");
                                         startActivity(intent);
                                         break;

            case R.id.add_new_area:    intent = new Intent(AddAreaActivity.this, NewArea.class);
                                        startActivity(intent);
                                        break;
        }
    }
    private void getdata() {
        StringRequest stringRequest = new StringRequest("http://192.168.0.9/getArea.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject j = null;
                        try {
                            j = new JSONObject(response);
                            result = j.getJSONArray(JSON_ARRAY);
                            name(result);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
    private void name(JSONArray j) {
        for (int i = 0; i < j.length(); i++) {
            try {
                JSONObject json = j.getJSONObject(i);
                arrayList.add(json.getString(FoodNameArray));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // arrayList.add(0,"Select Employee");
        area_spinner.setAdapter(new ArrayAdapter<String>(AddAreaActivity.this, android.R.layout.simple_spinner_dropdown_item, arrayList));
    }
}