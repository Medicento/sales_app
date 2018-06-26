package com.safdar.medicento.salesappmedicento;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.safdar.medicento.salesappmedicento.helperData.Constants;
import com.safdar.medicento.salesappmedicento.helperData.OrderedMedicine;
import com.safdar.medicento.salesappmedicento.networking.SalesDataLoader;

import java.util.ArrayList;

public class ConfirmOrderActivity extends AppCompatActivity implements  LoaderManager.LoaderCallbacks<Object>{
    TextView mSelectedPharmacyName;
    ArrayList<OrderedMedicine> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);

        Bundle b = getIntent().getExtras();

        final String selectedPharmacy = b.getString(Constants.SELECTED_PHARMACY, "");
        mSelectedPharmacyName = findViewById(R.id.pharmacy_selected);
        mSelectedPharmacyName.setText(selectedPharmacy);

        ListView listView = findViewById(R.id.ordered_medicines_list_confirmation);
        listView.setAdapter(PlaceOrdersActivity.mOrderedMedicineAdapter);

        list = (ArrayList<OrderedMedicine>) PlaceOrdersActivity.mOrderedMedicineAdapter.mList;


        float total = 0;
        for (OrderedMedicine ordMed: list) {
            total = total + ordMed.getCost();
        }
        TextView totalTv = findViewById(R.id.total_cost);
        totalTv.setText(String.valueOf(total));

        Button btn =findViewById(R.id.confirm_order);
        final float finalTotal = total;
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLoaderManager().initLoader(Constants.PLACE_ORDER_LOADER_ID , null, ConfirmOrderActivity.this);
                Intent intent = new Intent(ConfirmOrderActivity.this, OrderConfirmedActivity.class);
                intent.putExtra(Constants.SELECTED_PHARMACY, selectedPharmacy);
                intent.putExtra(Constants.ORDER_TOTAL_COST, finalTotal);
                startActivity(intent);
            }
        });
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new SalesDataLoader(this, Constants.PLACE_ORDER_URL, getString(R.string.place_order_action), list);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        Toast.makeText(this, "Order Placed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}