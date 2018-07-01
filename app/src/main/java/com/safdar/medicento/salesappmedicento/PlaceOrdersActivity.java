package com.safdar.medicento.salesappmedicento;

import android.app.LoaderManager;
import android.content.Context;
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
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.safdar.medicento.salesappmedicento.helperData.Constants;
import com.safdar.medicento.salesappmedicento.helperData.OrderedMedicine;
import com.safdar.medicento.salesappmedicento.helperData.OrderedMedicineAdapter;
import com.safdar.medicento.salesappmedicento.helperData.PhramaSpinnerCustomAdapter;
import com.safdar.medicento.salesappmedicento.helperData.SavedData;
import com.safdar.medicento.salesappmedicento.helperData.AreaSpinnerCustomAdapter;
import com.safdar.medicento.salesappmedicento.networking.SalesDataLoader;
import com.safdar.medicento.salesappmedicento.networking.data.Medicine;
import com.safdar.medicento.salesappmedicento.networking.data.SalesArea;
import com.safdar.medicento.salesappmedicento.networking.data.SalesPerson;
import com.safdar.medicento.salesappmedicento.networking.data.SalesPharmacy;

import java.util.ArrayList;

public class PlaceOrdersActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Object> {

    AreaSpinnerCustomAdapter mAreaAdapter;
    PhramaSpinnerCustomAdapter mPharmaAdapter;
    ArrayAdapter<String> mMedicineAdapter;
    ArrayList<Medicine> mMedicineDataList;
    ArrayList<SalesArea> mSalesAreaDetails;
    ArrayList<SalesPharmacy> mSalesPharmacyDetails;
    AutoCompleteTextView mSelectMedicineTv;

    CoordinatorLayout coordinatorLayout;

    ImageView mNoNetworkImage;
    InputMethodManager im;

    RecyclerView mOrderedMedicinesListView;
    NavigationView mNavigationView;
    OrderedMedicineAdapter mOrderedMedicineAdapter;
    ProgressBar mProgressBar;

    Spinner mSelectAreaSpinner, mSelectPharmacySpinner;

    SharedPreferences mSharedPreferences;
    Snackbar mSnackbar;

    TextView mNoNetworkInfo;
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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mProgressBar = findViewById(R.id.area_pharma_fetch_progress);
        mLoadingWaitView = findViewById(R.id.loading_wait_view);
        coordinatorLayout = findViewById(R.id.coordinator_layout);
        mSnackbar = Snackbar.make(coordinatorLayout, "Please wait while the data is being loaded...", Snackbar.LENGTH_INDEFINITE);
        mNoNetworkImage = findViewById(R.id.no_network_icon);
        mNoNetworkInfo = findViewById(R.id.no_network_info);

        mSelectAreaSpinner = findViewById(R.id.area_edit_tv);
        mSelectPharmacySpinner = findViewById(R.id.pharmacy_edit_tv);
        mSelectMedicineTv = findViewById(R.id.medicine_edit_tv);
        mOrderedMedicinesListView = findViewById(R.id.ordered_medicines_list);
        mOrderedMedicinesListView.setLayoutManager(new LinearLayoutManager(this));
        mOrderedMedicinesListView.setHasFixedSize(true);

        addSalesPersonDetailsToNavDrawer();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        mSelectAreaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedArea = mAreaAdapter.getItem(position);
                if (!calledOnAreaLoadingFinished) {
                    repopulateThePharmacyList();
                } else {
                    calledOnAreaLoadingFinished = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSelectPharmacySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!calledOnPharmacyLoadingFinished) {
                    mOrderedMedicineAdapter.reset();
                } else {
                    calledOnPharmacyLoadingFinished = false;
                }
                mSelectedPharmacy = mPharmaAdapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mOrderedMedicineAdapter = SavedData.mOrderedMedicineAdapter;
        mOrderedMedicineAdapter = new OrderedMedicineAdapter(new ArrayList<OrderedMedicine>());
        mOrderedMedicinesListView.setAdapter(mOrderedMedicineAdapter);
        mSelectMedicineTv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectMedicineTv.setText("");
                im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                Medicine medicine = null;
                for (Medicine med : mMedicineDataList) {
                    if (med.getMedicentoName().equals(mMedicineAdapter.getItem(position))) {
                        medicine = med;
                        break;
                    }
                }
                mOrderedMedicineAdapter.add(new OrderedMedicine(medicine.getMedicentoName(),
                        medicine.getCompanyName(),
                        1,
                        medicine.getPrice(),
                        medicine.getPrice(),
                        mSelectedPharmacy.getId()));
                mOrderedMedicinesListView.smoothScrollToPosition(0);
            }
        });
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = (int) viewHolder.itemView.getTag();
                mOrderedMedicineAdapter.remove(pos);
            }
        }).attachToRecyclerView(mOrderedMedicinesListView);

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

    /*This method fills the pharmacy spinner again
     *when a different area is selected
     */
    private void repopulateThePharmacyList() {
        mOrderedMedicineAdapter.reset();
        ArrayList<SalesPharmacy> pharmacyList = new ArrayList<>();
        if (mSalesPharmacyDetails != null) {
            for (SalesPharmacy salesPharmacy : mSalesPharmacyDetails) {
                if (mSelectedArea.getId().equals(salesPharmacy.getAreaId())) {
                    pharmacyList.add(salesPharmacy);
                }
            }
            if (pharmacyList.size() == 0) {
                Toast.makeText(this, "No Pharmacy available for this area", Toast.LENGTH_SHORT).show();
                mPharmaAdapter = new PhramaSpinnerCustomAdapter(this, R.layout.spinner_item_layout, new ArrayList<SalesPharmacy>());
                mSelectPharmacySpinner.setAdapter(mPharmaAdapter);
                return;
            }
            mPharmaAdapter = new PhramaSpinnerCustomAdapter(this, R.layout.spinner_item_layout, pharmacyList);
            mSelectPharmacySpinner.setAdapter(mPharmaAdapter);
        }

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
        } else if (requestCode == Constants.RC_CONFIRM_ORDER) {
            if (resultCode == RESULT_OK) {
                SavedData.mOrderedMedicineAdapter = null;
                Intent intent = new Intent(this, OrderConfirmedActivity.class);
                startActivity(intent);
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
            if (canOrderBeConfirmed()) {
                Intent intent = new Intent(PlaceOrdersActivity.this, ConfirmOrderActivity.class);
                intent.putExtra(Constants.SELECTED_PHARMACY, mSelectPharmacySpinner.getSelectedItem().toString());
                startActivityForResult(intent, Constants.RC_CONFIRM_ORDER);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /*This method confirms that the any pharmacy is selected
     *and that we have ordered something as well
     */
    private boolean canOrderBeConfirmed() {
        if (mSelectPharmacySpinner.getSelectedItem() == null) {
            Toast.makeText(this, "No Pharmacy selected", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (mOrderedMedicineAdapter.mMedicinesList.size() == 0) {
            Toast.makeText(this, "Please select some medicines first", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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
        } else {
            super.onBackPressed();
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
                ArrayList<String> areaList = new ArrayList<>();
                if (mSalesAreaDetails.isEmpty()) {
                    mNoNetworkImage.setVisibility(View.VISIBLE);
                    mNoNetworkInfo.setVisibility(View.VISIBLE);
                    pharmaLoadFlag = true;
                    medicineLoadFlag = true;
                    dismissLoadingIndicators();
                    return;
                }
                for (SalesArea salesArea : mSalesAreaDetails) {
                    areaList.add(salesArea.getAreaName());
                }
                mAreaAdapter = new AreaSpinnerCustomAdapter(this, R.layout.spinner_item_layout, mSalesAreaDetails);
                mSelectAreaSpinner.setAdapter(mAreaAdapter);
                getLoaderManager().initLoader(Constants.SALES_PHARMACY_LOADER, null, this);
                break;
            case Constants.SALES_PHARMACY_LOADER:
                calledOnPharmacyLoadingFinished = true;
                mSalesPharmacyDetails = (ArrayList<SalesPharmacy>) data;
                ArrayList<SalesPharmacy> pharmacyList = new ArrayList<>();
                boolean first = true;
                for (SalesPharmacy salesPharmacy : mSalesPharmacyDetails) {
                    if (mSalesAreaDetails.get(0).getId().equals(salesPharmacy.getAreaId())) {
                        pharmacyList.add(salesPharmacy);
                    }
                }
                mPharmaAdapter = new PhramaSpinnerCustomAdapter(this, R.layout.spinner_item_layout, pharmacyList);
                mSelectPharmacySpinner.setAdapter(mPharmaAdapter);
                pharmaLoadFlag = true;
                dismissLoadingIndicators();
                break;
            case Constants.MEDICINE_DATA_LOADER_ID:
                mMedicineDataList = (ArrayList<Medicine>) data;
                ArrayList<String> medicineList = new ArrayList<>();
                for (Medicine medicine : mMedicineDataList) {
                    medicineList.add(medicine.getMedicentoName());
                }
                mMedicineAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, medicineList);
                mSelectMedicineTv.setAdapter(mMedicineAdapter);
                mSelectMedicineTv.setThreshold(0);
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
            mSelectMedicineTv.setEnabled(true);
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
            return;
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
}