package com.safdar.medicento.salesappmedicento;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.safdar.medicento.salesappmedicento.networking.SalesDataLoader;
import com.safdar.medicento.salesappmedicento.networking.data.SalesArea;
import com.safdar.medicento.salesappmedicento.networking.data.SalesPharmacy;

import java.util.ArrayList;
import java.util.List;

public class PlaceOrdersActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, LoaderManager.LoaderCallbacks<Object> {
    public static final String SELECTED_PHARMACY = "selected_pharmacy";
    public static final String USER_PASSWORD = "password";
    public static final String USER_EMAIL = "email";
    private static final int SALES_AREA_LOADER_ID = 1;
    private static final int SALES_PHARMACY_LOADER = 2;
    private static final String AREA_DATA_URL = "https://medicento-api.herokuapp.com/area";
    private static final String PHARMACY_DATA_URL = "https://medicento-api.herokuapp.com/pharma";

    Spinner mSelectAreaTv;
    Spinner mSelectPharmacyTv;
    TextView mErrorInPharmacyTv;
    AutoCompleteTextView mSelectMedicineTv;
    TextView mErrorInMedicineTv;
    TextView mSelectedMedicineTv;
    TextView mSelectedMedicineCompanyTv;
    TextView mSelectedMedicineRateTv;
    Button mIncQty;
    Button mDecQty;
    int mSelectedMedicineIndex;
    ListView mOrderedMedicinesListView;
    ArrayAdapter<String> mAreaNameAdapter;
    ArrayAdapter<CharSequence> mMedicineAdapter;
    ArrayAdapter<String> mPharmacyAdapter;
    ArrayList<SalesArea> mSalesAreaDetails;
    ArrayList<SalesPharmacy> mSalesPharmacyDetails;
    public static OrderedMedicineAdapter mOrderedMedicineAdapter;
    InputMethodManager im;
    Boolean validPharmacyFlag = false;
    static SharedPreferences mSharedPreferences;

/////////////////////////////////////////AppCompat, Navigation, ClickListener_Overrides//////////////////////////////////////////////////////////////////////////////////


    @Override
    protected void onResume() {
        super.onResume();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (mSharedPreferences.getString(USER_EMAIL, "anonymous").equals("anonymous")) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_orders);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        setupGUIAndInitializeDataMembers();


        List<OrderedMedicine> medicines = new ArrayList<>();
        mOrderedMedicineAdapter = new OrderedMedicineAdapter(this, R.layout.item_ordered_medicine, medicines);
        mOrderedMedicinesListView.setAdapter(mOrderedMedicineAdapter);


        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(SALES_AREA_LOADER_ID, null, this);
        loaderManager.initLoader(SALES_PHARMACY_LOADER, null, this);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.place_orders, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (id == R.id.action_proceed) {
            if (canWeJumpToConfirm()) {
                Intent intent = new Intent(PlaceOrdersActivity.this, ConfirmOrderActivity.class);
                intent.putExtra(SELECTED_PHARMACY, mSelectPharmacyTv.getSelectedItem().toString());
                startActivity(intent);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.sign_out) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(USER_EMAIL, "anonymous");
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        } else if (id == R.id.total_sales) {

        } else if (id == R.id.no_of_orders) {

        } else if (id == R.id.returns) {

        } else if (id == R.id.add_new_pharmacy) {
            Intent intent = new Intent(this, NewPharmacy.class);
            startActivity(intent);
        } else if (id == R.id.add_new_area) {
            Intent intent = new Intent(this, NewArea.class);
            startActivity(intent);
        } else if (id == R.id.about_me) {
            Intent intent = new Intent(this, SalesPersonDetails.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onClick(View v) {
        OrderedMedicine orderedMedicine;
        im.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        if (isOrderShowcaseEmpty()) {
            Toast.makeText(this, "Please select some medicine!!", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (v.getId()) {
            case R.id.inc_qty:
                orderedMedicine = new OrderedMedicine(mSelectedMedicineTv.getText().toString(),
                        mSelectedMedicineCompanyTv.getText().toString(),
                        1,
                        20,
                        20
                );
                mOrderedMedicineAdapter.add(orderedMedicine);
                break;
            case R.id.dec_qty:
                orderedMedicine = new OrderedMedicine(mSelectedMedicineTv.getText().toString(),
                        mSelectedMedicineCompanyTv.getText().toString(),
                        1,
                        20,
                        20
                );
                int qtyLeft = mOrderedMedicineAdapter.sub(orderedMedicine);
                if (qtyLeft == 0) {
                    clearOrderShowcase();
                }
                break;
        }
    }
/////////////////////////////////////////Helper_Methods//////////////////////////////////////////////////////////////////////////////////

    private void showSelectedItemDetails(int pos) {
        mSelectedMedicineTv.setText(mMedicineAdapter.getItem(pos));
        mSelectedMedicineCompanyTv.setText("Cipla");
        mSelectedMedicineRateTv.setText("20");
        mSelectedMedicineIndex = pos;
    }

    private void setupGUIAndInitializeDataMembers() {

        /////////////////Views Initialization///////////////////////
        mSelectPharmacyTv = findViewById(R.id.pharmacy_edit_tv);
        mSelectAreaTv = findViewById(R.id.area_edit_tv);
        mErrorInPharmacyTv = findViewById(R.id.error_in_pharmacy_edit_tv);
        mSelectMedicineTv = findViewById(R.id.medicine_edit_tv);
        mErrorInMedicineTv = findViewById(R.id.error_in_medicine_edit_tv);
        mSelectedMedicineTv = findViewById(R.id.selected_medicine);
        mSelectedMedicineCompanyTv = findViewById(R.id.selected_medicine_company);
        mSelectedMedicineRateTv = findViewById(R.id.selected_medicine_rate);
        mIncQty = findViewById(R.id.inc_qty);
        mDecQty = findViewById(R.id.dec_qty);
        mOrderedMedicinesListView = findViewById(R.id.ordered_medicines_list);

        mIncQty.setOnClickListener(this);
        mDecQty.setOnClickListener(this);

        mMedicineAdapter = ArrayAdapter.createFromResource(this, R.array.medicines, android.R.layout.simple_list_item_1);
        mSelectMedicineTv.setAdapter(mMedicineAdapter);
        mSelectMedicineTv.setThreshold(0);


        mSelectMedicineTv.setValidator(new AutoCompleteTextView.Validator() {
            @Override
            public boolean isValid(CharSequence text) {
                if (mMedicineAdapter.getPosition(text.toString()) == -1) {
                    mErrorInMedicineTv.setVisibility(View.VISIBLE);
                    clearOrderShowcase();
                    return false;
                } else {
                    mErrorInMedicineTv.setVisibility(View.GONE);
                    return true;
                }
            }

            @Override
            public CharSequence fixText(CharSequence invalidText) {
                return null;
            }
        });
        mSelectMedicineTv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectMedicineTv.clearFocus();
                showSelectedItemDetails(position);
            }
        });
    }


    private void clearOrderShowcase() {
        mSelectedMedicineTv.setText("Name");
        mSelectedMedicineCompanyTv.setText("Company");
        mSelectedMedicineRateTv.setText("Rate");
        mSelectMedicineTv.setText("");
    }

    private boolean isOrderShowcaseEmpty() {
        if (mSelectedMedicineTv.getText().equals("Name") &&
                mSelectedMedicineCompanyTv.getText().equals("Company") ) {
            return true;
        } else
            return false;
    }

    private boolean canWeJumpToConfirm () {
        if (mOrderedMedicineAdapter.isEmpty()) {
            Toast.makeText(this, "Please select some medicines first", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!validPharmacyFlag) {
            Toast.makeText(this, "Please select some valid pharmacy", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
//////////////////////////////////////Loader_Callbacks///////////////////////////////////////////////////////////////////////////////
    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        if (id == SALES_AREA_LOADER_ID) {
            Uri baseUri = Uri.parse(AREA_DATA_URL);
            Uri.Builder builder = baseUri.buildUpon();
            return new SalesDataLoader(this, builder.toString(), getString(R.string.fetch_area_action));
        } else if (id == SALES_PHARMACY_LOADER) {
            Uri baseUri = Uri.parse(PHARMACY_DATA_URL);
            Uri.Builder builder = baseUri.buildUpon();
            return new SalesDataLoader(this, builder.toString(), getString(R.string.fetch_pharmacy_action));
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {
        switch (loader.getId()) {
            case SALES_AREA_LOADER_ID:
                mSalesAreaDetails = (ArrayList<SalesArea>) data;
                ArrayList<String> areaList = new ArrayList<>();
                for (SalesArea salesArea: mSalesAreaDetails) {
                    areaList.add(salesArea.getAreaName());
                }
                mAreaNameAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, areaList);
                mSelectAreaTv.setAdapter(mAreaNameAdapter);
                break;
            case SALES_PHARMACY_LOADER:
                mSalesPharmacyDetails = (ArrayList<SalesPharmacy>) data;
                ArrayList<String> pharmacyList = new ArrayList<>();
                for (SalesPharmacy salesPharmacy: mSalesPharmacyDetails) {
                    pharmacyList.add(salesPharmacy.getPharmacyName());
                }
                mPharmacyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, pharmacyList);
                mSelectPharmacyTv.setAdapter(mPharmacyAdapter);
        }
    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {

    }
}