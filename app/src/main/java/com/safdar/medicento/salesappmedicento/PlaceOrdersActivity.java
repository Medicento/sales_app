package com.safdar.medicento.salesappmedicento;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.safdar.medicento.salesappmedicento.fragments.OrderConfirmed;
import com.safdar.medicento.salesappmedicento.fragments.PlaceOrderFragment;
import com.safdar.medicento.salesappmedicento.helperData.AreaSpinnerCustomAdapter;
import com.safdar.medicento.salesappmedicento.helperData.Constants;
import com.safdar.medicento.salesappmedicento.helperData.OrderedMedicineAdapter;
import com.safdar.medicento.salesappmedicento.helperData.PhramaSpinnerCustomAdapter;
import com.safdar.medicento.salesappmedicento.helperData.SavedData;
import com.safdar.medicento.salesappmedicento.networking.SalesDataLoader;
import com.safdar.medicento.salesappmedicento.networking.data.Medicine;
import com.safdar.medicento.salesappmedicento.networking.data.SalesArea;
import com.safdar.medicento.salesappmedicento.networking.data.SalesPerson;
import com.safdar.medicento.salesappmedicento.networking.data.SalesPharmacy;

import java.util.ArrayList;

public class PlaceOrdersActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Object>,PlaceOrderFragment.OnPlaceOrderChangeListener {

    public static ArrayList<Medicine> mMedicineDataList;
    ArrayList<SalesArea> mSalesAreaDetails;
    ArrayList<SalesPharmacy> mSalesPharmacyDetails;

    FragmentManager mFragmentManager;
    PlaceOrderFragment mPlaceOrder;
    CoordinatorLayout coordinatorLayout;
    OrderConfirmed mOrderConfirmed;

    ImageView mNoNetworkImage;
    InputMethodManager im;

    NavigationView mNavigationView;
    OrderedMedicineAdapter mOrderedMedicineAdapter;
    ProgressBar mProgressBar;

    Spinner mSelectAreaSpinner, mSelectPharmacySpinner;

    SharedPreferences mSharedPreferences;
    Snackbar mSnackbar;

    TextView mNoNetworkInfo;
    Toolbar mToolbar;
    View mLoadingWaitView;

    boolean pharmaLoadFlag, medicineLoadFlag;

    SalesPharmacy mSelectedPharmacy;
    SalesArea mSelectedArea;

    String[] salesPersonDetailsLabel = {
            "Total Sales        :",
            "No of Orders     :",
            "Returns              :",
            "Earnings            :"
    };
    boolean calledOnAreaLoadingFinished, calledOnPharmacyLoadingFinished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_orders);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mProgressBar = findViewById(R.id.area_pharma_fetch_progress);
        mLoadingWaitView = findViewById(R.id.loading_wait_view);
        coordinatorLayout = findViewById(R.id.coordinator_layout);
        mSnackbar = Snackbar.make(coordinatorLayout, "Please wait while the data is being loaded...", Snackbar.LENGTH_INDEFINITE);
        //mNoNetworkImage = findViewById(R.id.no_network_icon);
        //mNoNetworkInfo = findViewById(R.id.no_network_info);

        addSalesPersonDetailsToNavDrawer();

        mPlaceOrder = new PlaceOrderFragment();
        mPlaceOrder.setAreaPharmaSelectListener(this);

        mFragmentManager = getSupportFragmentManager();

        mFragmentManager.beginTransaction().add(R.id.main_container, mPlaceOrder).commit();
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(Constants.SALES_AREA_LOADER_ID, null, this);
        getLoaderManager().initLoader(Constants.MEDICINE_DATA_LOADER_ID, null, this);
        getLoaderManager().initLoader(Constants.LOG_IN_LOADER_ID, null, this);
    }

    /*This method adds totalSales,orders, returns, earnings to
     *the nav menu of the app
     */
    private void addSalesPersonDetailsToNavDrawer() {
        View headerView = mNavigationView.getHeaderView(0);
        TextView navHeaderSalesmanName = headerView.findViewById(R.id.username_header);
        TextView navHeaderSalesmanEmail = headerView.findViewById(R.id.user_email_header);

        navHeaderSalesmanName.setText(mSharedPreferences.getString(Constants.SALE_PERSON_NAME, ""));
        navHeaderSalesmanEmail.setText(mSharedPreferences.getString(Constants.SALE_PERSON_EMAIL, ""));

        Menu menu = mNavigationView.getMenu();
        if (mSharedPreferences.getFloat(Constants.SALE_PERSON_TOTAL_SALES, -1) == -1) {
            return;
        }
        menu.getItem(1).setTitle(salesPersonDetailsLabel[0] + mSharedPreferences.getFloat(Constants.SALE_PERSON_TOTAL_SALES, -1));
        menu.getItem(2).setTitle(salesPersonDetailsLabel[1] + mSharedPreferences.getInt(Constants.SALE_PERSON_NO_OF_ORDERS, 0));
        menu.getItem(3).setTitle(salesPersonDetailsLabel[2] + mSharedPreferences.getInt(Constants.SALE_PERSON_RETURNS, -1));
        menu.getItem(4).setTitle(salesPersonDetailsLabel[3] + mSharedPreferences.getFloat(Constants.SALE_PERSON_EARNINGS, -1));
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Constants.RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Welcome!!", Toast.LENGTH_SHORT).show();
                mNavigationView = findViewById(R.id.nav_view);
                addSalesPersonDetailsToNavDrawer();
            } else {
                Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
                finish();
            }
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
            if (mPlaceOrder.isInOrderMode()) {
                mPlaceOrder.transformIntoConfirmOrder();
                mToolbar.setTitle("Confirm Order!");
                return true;
            } else {
                mPlaceOrder.placeOrder();
                mToolbar.setTitle("Placing Order...");
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.sign_out) {
            clearUserDetails();
            Intent intent = new Intent(this, SignInActivity.class);
            startActivityForResult(intent, Constants.RC_SIGN_IN);
        } else if (id == R.id.total_sales) {
            return true;
        } else if (id == R.id.no_of_orders) {
            return true;
        } else if (id == R.id.returns) {
            return true;
        } else if (id == R.id.add_new_pharmacy) {
            Intent intent = new Intent(this, NewPharmacy.class);
            startActivity(intent);
        } else if (id == R.id.add_new_area) {
            Intent intent = new Intent(this, NewArea.class);
            startActivity(intent);
        } else if (id == R.id.about_me) {
            Intent intent = new Intent(this, SalesPersonDetails.class);
            for (SalesArea salesArea : mSalesAreaDetails) {
                if (mSharedPreferences.getString(Constants.SALE_PERSON_ALLOCATED_AREA_ID, "").equals(salesArea.getId())) {
                    intent.putExtra(Constants.SALE_PERSON_ALLOCATED_AREA_NAME, salesArea.getAreaName());
                }
            }
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /*This method deletes all the details of the
     *sales person when he sign out of the app
     */

    private void clearUserDetails() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(Constants.SALE_PERSON_EMAIL, "");
        editor.putString(Constants.SALE_PERSON_NAME, "");
        editor.putFloat(Constants.SALE_PERSON_TOTAL_SALES, 0);
        editor.putInt(Constants.SALE_PERSON_RETURNS, 0);
        editor.putFloat(Constants.SALE_PERSON_EARNINGS, 0);
        editor.putString(Constants.SALE_PERSON_ID, "");
        editor.putString(Constants.SALE_PERSON_ALLOCATED_AREA_ID, "");
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (mPlaceOrder.isInOrderMode()) {
            super.onBackPressed();
        } else {
            mToolbar.setTitle("Place Order");
            mPlaceOrder.transformIntoPlaceOrder();
        }
    }

    /*
        These are the loader callbacks
     */
    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        mSnackbar.show();
        mProgressBar.setVisibility(View.VISIBLE);
        if (id == Constants.SALES_AREA_LOADER_ID) {
            Uri baseUri = Uri.parse(Constants.AREA_DATA_URL);
            Uri.Builder builder = baseUri.buildUpon();
            return new SalesDataLoader(this, builder.toString(), getString(R.string.fetch_area_action));
        } else if (id == Constants.SALES_PHARMACY_LOADER) {
            Uri baseUri = Uri.parse(Constants.PHARMACY_DATA_URL);
            Uri.Builder builder = baseUri.buildUpon();
            return new SalesDataLoader(this, builder.toString(), getString(R.string.fetch_pharmacy_action));
        } else if (id == Constants.MEDICINE_DATA_LOADER_ID) {
            Uri baseUri = Uri.parse(Constants.MEDICINE_DATA_URL);
            Uri.Builder builder = baseUri.buildUpon();
            return new SalesDataLoader(this, builder.toString(), getString(R.string.fetch_medicine_action));
        } else if (id == Constants.LOG_IN_LOADER_ID) {
            Uri baseUri = Uri.parse(Constants.USER_LOGIN_URL);
            Uri.Builder builder = baseUri.buildUpon();


            builder.appendQueryParameter("useremail", mSharedPreferences.getString(Constants.SALE_PERSON_EMAIL, ""));
            builder.appendQueryParameter("password", mSharedPreferences.getString(Constants.USER_PASSWORD, ""));
            return new SalesDataLoader(this, builder.toString(), getString(R.string.login_action));
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {
        mProgressBar.setVisibility(View.GONE);
        switch (loader.getId()) {
            case Constants.SALES_AREA_LOADER_ID:
                mSalesAreaDetails = (ArrayList<SalesArea>) data;
                calledOnAreaLoadingFinished = true;
                if (mSalesAreaDetails.isEmpty()) {
                    //mNoNetworkImage.setVisibility(View.VISIBLE);
                    //mNoNetworkInfo.setVisibility(View.VISIBLE);
                    pharmaLoadFlag = true;
                    medicineLoadFlag = true;
                    dismissLoadingIndicators();
                    return;
                }
                mPlaceOrder.setAreaAdapter(new AreaSpinnerCustomAdapter(this, R.layout.spinner_item_layout, mSalesAreaDetails));
                getLoaderManager().initLoader(Constants.SALES_PHARMACY_LOADER, null, this);
                break;
            case Constants.SALES_PHARMACY_LOADER:
                calledOnPharmacyLoadingFinished = true;
                mSalesPharmacyDetails = (ArrayList<SalesPharmacy>) data;
                ArrayList<SalesPharmacy> pharmacyList = new ArrayList<>();
                for (SalesPharmacy salesPharmacy : mSalesPharmacyDetails) {
                    if (mSalesAreaDetails.get(0).getId().equals(salesPharmacy.getAreaId())) {
                        pharmacyList.add(salesPharmacy);
                    }
                }
                mPlaceOrder.setPharmaAdapter(new PhramaSpinnerCustomAdapter(this, R.layout.spinner_item_layout, pharmacyList));
                pharmaLoadFlag = true;
                dismissLoadingIndicators();
                break;
            case Constants.MEDICINE_DATA_LOADER_ID:
                mMedicineDataList = (ArrayList<Medicine>) data;
                ArrayList<String> medicineList = new ArrayList<>();
                for (Medicine medicine : mMedicineDataList) {
                    medicineList.add(medicine.getMedicentoName());
                }
                mPlaceOrder.setMedicineAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, medicineList));
                medicineLoadFlag = true;
                dismissLoadingIndicators();
                break;
            case Constants.LOG_IN_LOADER_ID:
                SalesPerson salesPerson = (SalesPerson) data;
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                if (salesPerson != null) {
                    editor.putFloat(Constants.SALE_PERSON_TOTAL_SALES, salesPerson.getTotalSales());
                    editor.putInt(Constants.SALE_PERSON_NO_OF_ORDERS, salesPerson.getNoOfOrder());
                    editor.putInt(Constants.SALE_PERSON_RETURNS, salesPerson.getReturn());
                    editor.putFloat(Constants.SALE_PERSON_EARNINGS, salesPerson.getEarnings());
                    editor.apply();
                } else {
                    Toast.makeText(this, "Something went wrong please sign in again!", Toast.LENGTH_SHORT).show();
                }
                addSalesPersonDetailsToNavDrawer();
                break;
        }
    }

    /*This method removes/dismiss the progress bar,
     *Snackbar and the translucent view
     */
    private void dismissLoadingIndicators() {
        if (medicineLoadFlag && pharmaLoadFlag) {
            mProgressBar.setVisibility(View.GONE);
            mLoadingWaitView.setVisibility(View.GONE);
            mSnackbar.dismiss();
        }
    }

    /*This method fills the pharmacy spinner again
     *when a different area is selected
     */
    private void repopulateThePharmacyList() {
//        mOrderedMedicineAdapter.reset();
        ArrayList<SalesPharmacy> pharmacyList = new ArrayList<>();
        if (mSalesPharmacyDetails != null) {
            for (SalesPharmacy salesPharmacy : mSalesPharmacyDetails) {
                if (mSelectedArea.getId().equals(salesPharmacy.getAreaId())) {
                    pharmacyList.add(salesPharmacy);
                }
            }
            if (pharmacyList.size() == 0) {
                Toast.makeText(this, "No Pharmacy available for this area", Toast.LENGTH_SHORT).show();
            }
            mPlaceOrder.setPharmaAdapter(new PhramaSpinnerCustomAdapter(this, R.layout.spinner_item_layout, pharmacyList));
        }

    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (mSharedPreferences.getString(Constants.SALE_PERSON_NAME, "").isEmpty()) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivityForResult(intent, Constants.RC_SIGN_IN);
        }
    }

    @Override
    protected void onPause() {
        SavedData.saveAdapter(mOrderedMedicineAdapter);
        SavedData.saveAreaAndPharmacy(mSelectedArea, mSelectedPharmacy);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        SavedData.mOrderedMedicineAdapter = null;
        super.onDestroy();
    }

    @Override
    public void onAreaSelected(SalesArea salesArea) {
        mSelectedArea = salesArea;
        repopulateThePharmacyList();
    }

    @Override
    public void onPharmaSelected(SalesPharmacy salesPharmacy) {
        mSelectedPharmacy = salesPharmacy;
    }

    @Override
    public void onOrderPlaced(OrderedMedicineAdapter adapter, String[] output) {
        getLoaderManager().initLoader(Constants.LOG_IN_LOADER_ID, null, this);
        mOrderConfirmed = new OrderConfirmed();
        mOrderConfirmed.setIdAndDeliveryDate(output);
        mOrderConfirmed.setAdapter(adapter);
        mOrderConfirmed.setSelectedPharmacy(mSelectedPharmacy);
        mToolbar.setTitle("Order Placed!");
        mFragmentManager.beginTransaction().replace(R.id.main_container, mOrderConfirmed).commit();
    }
}