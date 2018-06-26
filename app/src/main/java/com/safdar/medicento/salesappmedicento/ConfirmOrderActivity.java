package com.safdar.medicento.salesappmedicento;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.safdar.medicento.salesappmedicento.helperData.Constants;
import com.safdar.medicento.salesappmedicento.helperData.OrderedMedicine;
import com.safdar.medicento.salesappmedicento.networking.SalesDataLoader;

import java.util.ArrayList;

public class ConfirmOrderActivity extends AppCompatActivity implements  LoaderManager.LoaderCallbacks<Object>{
    TextView mSelectedPharmacyName;
    ArrayList<OrderedMedicine> list;
    String selectedPharmacy;
    static float finalTotal;
    static String orderId;
    static String deliveryDate;
    ProgressBar mProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);

        Bundle b = getIntent().getExtras();

        selectedPharmacy = b.getString(Constants.SELECTED_PHARMACY, "");
        mSelectedPharmacyName = findViewById(R.id.pharmacy_selected);
        mSelectedPharmacyName.setText(selectedPharmacy);
        mProgressBar = findViewById(R.id.place_order_progress);

        ListView listView = findViewById(R.id.ordered_medicines_list_confirmation);
        listView.setAdapter(PlaceOrdersActivity.mOrderedMedicineAdapter);

        list = (ArrayList<OrderedMedicine>) PlaceOrdersActivity.mOrderedMedicineAdapter.mList;


        finalTotal= 0;
        for (OrderedMedicine ordMed: list) {
            finalTotal = finalTotal + ordMed.getCost();
        }
        TextView totalTv = findViewById(R.id.total_cost);
        totalTv.setText(String.valueOf(finalTotal));

        Button btn =findViewById(R.id.confirm_order);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLoaderManager().initLoader(Constants.PLACE_ORDER_LOADER_ID , null, ConfirmOrderActivity.this);
            }
        });
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        mProgressBar.setVisibility(View.VISIBLE);
        return new SalesDataLoader(this, Constants.PLACE_ORDER_URL, getString(R.string.place_order_action), list);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        mProgressBar.setVisibility(View.GONE);
        String[] x = (String[]) data;
        orderId = x[0];
        deliveryDate = x[1];
        Intent intent = new Intent();
        intent.putExtra(Constants.ORDER_TOTAL_COST, finalTotal);
        if (getParent() == null) {
            setResult(Activity.RESULT_OK, intent);
        } else {
            getParent().setResult(RESULT_OK);
        }
        finish();
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}