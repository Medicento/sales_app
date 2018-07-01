package com.safdar.medicento.salesappmedicento;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.safdar.medicento.salesappmedicento.helperData.Constants;
import com.safdar.medicento.salesappmedicento.helperData.OrderedMedicine;
import com.safdar.medicento.salesappmedicento.helperData.OrderedMedicineAdapter;
import com.safdar.medicento.salesappmedicento.helperData.SavedData;
import com.safdar.medicento.salesappmedicento.networking.SalesDataLoader;

import java.util.ArrayList;

public class ConfirmOrderActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Object>,
        OrderedMedicineAdapter.OverallCostChangeListener {
    TextView mSelectedPharmacyName;
    ArrayList<OrderedMedicine> list;
    ImageView mProgressBar;
    Animation mAnimation;

    TextView mTotalTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);

        mSelectedPharmacyName = findViewById(R.id.pharmacy_selected);
        mSelectedPharmacyName.setText(SavedData.mSelectedPharmacy.getPharmacyName());
        mProgressBar = findViewById(R.id.place_order_progress);

        RecyclerView recyclerView = findViewById(R.id.ordered_medicines_list_confirmation);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final OrderedMedicineAdapter adapter = SavedData.mOrderedMedicineAdapter;
        recyclerView.setAdapter(adapter);
        adapter.setOverallCostChangeListener(this);

        list = adapter.mMedicinesList;
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = (int) viewHolder.itemView.getTag();
                adapter.remove(pos);
            }
        }).attachToRecyclerView(recyclerView);
        mTotalTv = findViewById(R.id.total_cost);
        mTotalTv.setText(adapter.getOverallCost() + "");
        Button btn =findViewById(R.id.confirm_order);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLoaderManager().initLoader(Constants.PLACE_ORDER_LOADER_ID , null, ConfirmOrderActivity.this);
            }
        });

        mAnimation = new AlphaAnimation(1, 0);
        mAnimation.setDuration(200);
        mAnimation.setInterpolator(new LinearInterpolator());
        mAnimation.setRepeatCount(Animation.INFINITE);
        mAnimation.setRepeatMode(Animation.REVERSE);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.startAnimation(mAnimation);
        return new SalesDataLoader(this, Constants.PLACE_ORDER_URL, getString(R.string.place_order_action), list);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        mProgressBar.setVisibility(View.GONE);
        String[] x = (String[]) data;
        Intent intent = new Intent();
        if (getParent() == null) {
            setResult(Activity.RESULT_OK, intent);
        } else {
            getParent().setResult(RESULT_OK);
        }
        SavedData.saveOrderDetails(Float.parseFloat(mTotalTv.getText() + ""), x);
        finish();
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    @Override
    public void onCostChanged(float newCost) {
        mTotalTv.setText(newCost + "");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}