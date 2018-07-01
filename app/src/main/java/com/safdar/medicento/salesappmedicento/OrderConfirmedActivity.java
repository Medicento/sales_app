package com.safdar.medicento.salesappmedicento;

import android.content.Intent;
import android.os.Bundle;
import android.service.autofill.SaveCallback;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.safdar.medicento.salesappmedicento.helperData.Constants;
import com.safdar.medicento.salesappmedicento.helperData.SavedData;

import java.util.Date;

public class OrderConfirmedActivity extends AppCompatActivity implements View.OnClickListener{

    Button mShareBtn, mPlaceNewOrderBtn;
    TextView mDeliveryDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmed);

        Bundle b = getIntent().getExtras();

        TextView selectedPharmacyTv = findViewById(R.id.pharmacy_selected);
        selectedPharmacyTv.setText(SavedData.mSelectedPharmacy.getPharmacyName());

        TextView orderTotalCost = findViewById(R.id.total_cost);
        orderTotalCost.setText(SavedData.mOverallCost + "");

        mShareBtn = findViewById(R.id.share_order);
        mPlaceNewOrderBtn = findViewById(R.id.place_new_order);
        mShareBtn.setOnClickListener(this);
        mPlaceNewOrderBtn.setOnClickListener(this);
        mDeliveryDate = findViewById(R.id.delivery_date_tv);
        mDeliveryDate.setText(SavedData.mDeliveryDate);

        TextView tv = findViewById(R.id.order_id);
        tv.setText(SavedData.mOrderId);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.share_order:
                Toast.makeText(this, "To be implemented", Toast.LENGTH_SHORT).show();
                break;
            case R.id.place_new_order:
                NavUtils.navigateUpFromSameTask(this);
        }
    }

    @Override
    protected void onPause() {
        SavedData.mOrderedMedicineAdapter = null;
        super.onPause();
    }
}