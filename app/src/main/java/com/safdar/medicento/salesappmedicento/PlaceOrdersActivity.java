package com.safdar.medicento.salesappmedicento;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.safdar.medicento.salesappmedicento.helperData.Constants;
import com.safdar.medicento.salesappmedicento.networking.SalesDataLoader;
import com.safdar.medicento.salesappmedicento.networking.data.SalesArea;
import com.safdar.medicento.salesappmedicento.networking.data.SalesPharmacy;

import java.util.ArrayList;
import java.util.List;

public class PlaceOrdersActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, LoaderManager.LoaderCallbacks<Object> {


    Spinner mSelectAreaTv;
    Spinner mSelectPharmacyTv;
    TextView mErrorInPharmacyTv;

    AutoCompleteTextView mSelectMedicineTv;

    TextView mErrorInMedicineTv;
    TextView mSelectedMedicineTv;
    TextView mSelectedMedicineCompanyTv;
    TextView mSelectedMedicineRateTv;
    ImageView mNoNetworkImage;
    TextView mNoNetworkInfo;

    Button mIncQty;
    Button mDecQty;

    int mSelectedMedicineIndex;
    ListView mOrderedMedicinesListView;
    ArrayAdapter<String> mAreaNameAdapter;
    ArrayAdapter<CharSequence> mMedicineAdapter;
    ArrayAdapter<String> mPharmacyAdapter;
    static ArrayList<SalesArea> mSalesAreaDetails;
    ArrayList<SalesPharmacy> mSalesPharmacyDetails;
    public static OrderedMedicineAdapter mOrderedMedicineAdapter;
    InputMethodManager im;
    ProgressBar mProgressBar;
    static SharedPreferences mSharedPreferences;
    Snackbar mSnackbar;
    CoordinatorLayout coordinatorLayout;

/////////////////////////////////////////AppCompat, Navigation, ClickListener_Overrides//////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onResume() {
        super.onResume();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (mSharedPreferences.getString(Constants.SALE_PERSON_NAME, "anonymous").equals("anonymous")) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivityForResult(intent, Constants.RC_SIGN_IN);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Welcome!!", Toast.LENGTH_SHORT).show();

                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                addSalesPersonDetailsToNavDrawer(navigationView);
            } else {
                Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_orders);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        addSalesPersonDetailsToNavDrawer(navigationView);
        mProgressBar = findViewById(R.id.area_pharma_fetch_progress);
        coordinatorLayout = findViewById(R.id.coordinator_layout);
        mSnackbar = Snackbar.make(coordinatorLayout, "Please wait while the data is being loaded...", Snackbar.LENGTH_INDEFINITE);
        mSnackbar.show();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        setupGUIAndInitializeDataMembers();


        List<OrderedMedicine> medicines = new ArrayList<>();
        mOrderedMedicineAdapter = new OrderedMedicineAdapter(this, R.layout.item_ordered_medicine, medicines);
        mOrderedMedicinesListView.setAdapter(mOrderedMedicineAdapter);


        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(Constants.SALES_AREA_LOADER_ID, null, this);
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
        getMenuInflater().inflate(R.menu.place_orders, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (id == R.id.action_proceed) {
            if (canWeJumpToConfirm()) {
                Intent intent = new Intent(PlaceOrdersActivity.this, ConfirmOrderActivity.class);
                intent.putExtra(Constants.SELECTED_PHARMACY, mSelectPharmacyTv.getSelectedItem().toString());
                startActivity(intent);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.sign_out) {
            clearUserDetails();
            Intent intent = new Intent(this, SignInActivity.class);
            startActivityForResult(intent, Constants.RC_SIGN_IN);
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

    private void addSalesPersonDetailsToNavDrawer(NavigationView nv) {

        View headerView = nv.getHeaderView(0);
        TextView navHeaderSalesmanName = headerView.findViewById(R.id.username_header);
        TextView navHeaderSalesmanEmail = headerView.findViewById(R.id.user_email_header);

        navHeaderSalesmanName.setText(mSharedPreferences.getString(Constants.SALE_PERSON_NAME, ""));
        navHeaderSalesmanEmail.setText(mSharedPreferences.getString(Constants.SALE_PERSON_EMAIL, ""));

        Menu menu = nv.getMenu();
        menu.getItem(1).setTitle(menu.getItem(1).getTitle().toString() + mSharedPreferences.getFloat(Constants.SALE_PERSON_TOTAL_SALES, 0));
        menu.getItem(2).setTitle(menu.getItem(2).getTitle().toString() + mSharedPreferences.getFloat(Constants.SALE_PERSON_NO_OF_ORDERS, 0));
        menu.getItem(3).setTitle(menu.getItem(3).getTitle().toString() + mSharedPreferences.getInt(Constants.SALE_PERSON_RETURNS, 0));
        menu.getItem(4).setTitle(menu.getItem(4).getTitle().toString() + mSharedPreferences.getFloat(Constants.SALE_PERSON_EARNINGS, 0));
    }

    private void clearUserDetails() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(Constants.SALE_PERSON_EMAIL, "");
        editor.putString(Constants.SALE_PERSON_NAME, "anonymous");
        editor.putFloat(Constants.SALE_PERSON_TOTAL_SALES, 0);
        editor.putInt(Constants.SALE_PERSON_RETURNS, 0);
        editor.putFloat(Constants.SALE_PERSON_EARNINGS, 0);
        editor.putString(Constants.SALE_PERSON_ID, "");
        editor.putString(Constants.SALE_PERSON_ALLOCATED_AREA_ID, "");
        editor.apply();
    }

    private void showSelectedItemDetails(int pos) {
        mSelectedMedicineTv.setText(mMedicineAdapter.getItem(pos));
        mSelectedMedicineCompanyTv.setText("Cipla");
        mSelectedMedicineRateTv.setText("20");
        mSelectedMedicineIndex = pos;
        mSelectMedicineTv.setText("");
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
        mNoNetworkImage = findViewById(R.id.no_network_icon);
        mNoNetworkInfo = findViewById(R.id.no_network_info);

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

        mSelectAreaTv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                repopulateThePharmacyList(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void repopulateThePharmacyList(int index) {
        ArrayList<String> pharmacyList = new ArrayList<>();
        if (mSalesPharmacyDetails != null) {
            for (SalesPharmacy salesPharmacy : mSalesPharmacyDetails) {
                if (mSalesAreaDetails.get(index).getId().equals(salesPharmacy.getAreaId())) {
                    pharmacyList.add(salesPharmacy.getPharmacyName());
                }
            }
            if (mPharmacyAdapter != null) {
                mPharmacyAdapter = null;
            }
            mPharmacyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, pharmacyList);
            mSelectPharmacyTv.setAdapter(mPharmacyAdapter);
        }
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
        }
        return true;
    }
//////////////////////////////////////Loader_Callbacks///////////////////////////////////////////////////////////////////////////////
    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        mProgressBar.setVisibility(View.VISIBLE);
        if (id == Constants.SALES_AREA_LOADER_ID) {
            Uri baseUri = Uri.parse(Constants.AREA_DATA_URL);
            Uri.Builder builder = baseUri.buildUpon();
            return new SalesDataLoader(this, builder.toString(), getString(R.string.fetch_area_action));
        } else if (id == Constants.SALES_PHARMACY_LOADER) {
            Uri baseUri = Uri.parse(Constants.PHARMACY_DATA_URL);
            Uri.Builder builder = baseUri.buildUpon();
            return new SalesDataLoader(this, builder.toString(), getString(R.string.fetch_pharmacy_action));
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {
        mProgressBar.setVisibility(View.GONE);
        switch (loader.getId()) {
            case Constants.SALES_AREA_LOADER_ID:
                mSalesAreaDetails = (ArrayList<SalesArea>) data;
                ArrayList<String> areaList = new ArrayList<>();
                for (SalesArea salesArea: mSalesAreaDetails) {
                    areaList.add(salesArea.getAreaName());
                }
                mAreaNameAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, areaList);
                mSelectAreaTv.setAdapter(mAreaNameAdapter);
                getLoaderManager().initLoader(Constants.SALES_PHARMACY_LOADER, null, this);
                break;
            case Constants.SALES_PHARMACY_LOADER:
                mSalesPharmacyDetails = (ArrayList<SalesPharmacy>) data;
                ArrayList<String> pharmacyList = new ArrayList<>();
                for (SalesPharmacy salesPharmacy: mSalesPharmacyDetails) {
                    if (mSalesAreaDetails.get(0).getId().equals(salesPharmacy.getAreaId())) {
                        pharmacyList.add(salesPharmacy.getPharmacyName());
                    }
                }
                mPharmacyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, pharmacyList);
                mSelectPharmacyTv.setAdapter(mPharmacyAdapter);
                mSnackbar.dismiss();
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {

    }
}