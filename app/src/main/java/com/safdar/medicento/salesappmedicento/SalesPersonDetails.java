package com.safdar.medicento.salesappmedicento;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.safdar.medicento.salesappmedicento.helperData.Constants;
import com.safdar.medicento.salesappmedicento.networking.data.SalesArea;

import java.util.ArrayList;

public class SalesPersonDetails extends AppCompatActivity {

    SharedPreferences mSharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_person_details);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        TextView name = findViewById(R.id.about_me_name);
        TextView allocatedArea = findViewById(R.id.about_me_allocated_area);
        name.setText(mSharedPreferences.getString(Constants.SALE_PERSON_NAME, ""));
        String areaId = mSharedPreferences.getString(Constants.SALE_PERSON_ALLOCATED_AREA_ID, "");
        for (SalesArea salesArea: PlaceOrdersActivity.mSalesAreaDetails) {
            if (areaId.equals(salesArea.getId())) {
                allocatedArea.setText(salesArea.getAreaName());
                return;
            }
        }
    }
}

