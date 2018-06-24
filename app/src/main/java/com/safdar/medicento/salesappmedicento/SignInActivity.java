package com.safdar.medicento.salesappmedicento;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.safdar.medicento.salesappmedicento.helperData.Constants;
import com.safdar.medicento.salesappmedicento.networking.SalesDataLoader;
import com.safdar.medicento.salesappmedicento.networking.data.SalesPerson;

public class SignInActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Object>  {

    EditText mEmailEditTv;
    EditText mPasswordEditTv;

    String mUserEmail;
    String mPassword;

    ProgressBar mProgressBar;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mEmailEditTv = findViewById(R.id.editTextEmail);
        mPasswordEditTv = findViewById(R.id.editTextPassword);
        mProgressBar = findViewById(R.id.sign_in_progress);
        mProgressBar.setVisibility(View.GONE);

        Button btn = findViewById(R.id.login_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserEmail = mEmailEditTv.getText().toString();
                mPassword = mPasswordEditTv.getText().toString();
                if (isInputEmpty(mUserEmail, mPassword)) {
                    Toast.makeText(SignInActivity.this, "Please enter data for sign in!", Toast.LENGTH_SHORT).show();
                    return;
                }
                mProgressBar.setVisibility(View.VISIBLE);
                getLoaderManager().initLoader(Constants.LOG_IN_LOADER_ID, null, SignInActivity.this);

            }
        });
    }

    private boolean isInputEmpty(String userEmail, String password) {
        return userEmail.isEmpty() || password.isEmpty();
    }

    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        if (id == Constants.LOG_IN_LOADER_ID) {
            Uri baseUri = Uri.parse(Constants.USER_LOGIN_URL);
            Uri.Builder builder = baseUri.buildUpon();

            builder.appendQueryParameter("useremail", mUserEmail);
            builder.appendQueryParameter("password", mPassword);

            return new SalesDataLoader(this, builder.toString(), getString(R.string.login_action));
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {
        mProgressBar.setVisibility(View.GONE);
        SalesPerson salesPerson = (SalesPerson) data;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (salesPerson != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Constants.SALE_PERSON_EMAIL, mUserEmail);
            editor.putString(Constants.SALE_PERSON_NAME, salesPerson.getName());
            editor.putFloat(Constants.SALE_PERSON_TOTAL_SALES, salesPerson.getTotalSales());
            editor.putInt(Constants.SALE_PERSON_RETURNS, salesPerson.getReturn());
            editor.putFloat(Constants.SALE_PERSON_EARNINGS, salesPerson.getEarnings());
            editor.putString(Constants.SALE_PERSON_ID, salesPerson.getId());
            editor.putString(Constants.SALE_PERSON_ALLOCATED_AREA_ID, salesPerson.getAllocatedArea());
            editor.apply();
            Intent intent = new Intent();
            if (getParent() == null) {
                setResult(Activity.RESULT_OK, intent);
            } else {
                getParent().setResult(RESULT_OK);
            }
            finish();
        } else {
            mEmailEditTv.setText("");
            mPasswordEditTv.setText("");
            Toast.makeText(this, "Incorrect user email or password!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {

    }
}