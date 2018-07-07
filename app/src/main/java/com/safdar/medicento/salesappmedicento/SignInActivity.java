package com.safdar.medicento.salesappmedicento;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.safdar.medicento.salesappmedicento.helperData.Constants;
import com.safdar.medicento.salesappmedicento.networking.SalesDataLoader;
import com.safdar.medicento.salesappmedicento.networking.data.SalesPerson;

public class SignInActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Object> {

    EditText mEmailEditTv;
    EditText mPasswordEditTv;

    String mUserEmail;
    String mUserPassword;

    ProgressBar mProgressBar;
    Animation mAnimation;
    ImageView mLogo;
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mLogo = findViewById(R.id.medicento_logo);
        mEmailEditTv = findViewById(R.id.email_edit_tv);
        mPasswordEditTv = findViewById(R.id.password_edit_tv);
        Button btn = findViewById(R.id.sign_in_btn);
        mProgressBar = findViewById(R.id.sign_in_progress);
        mProgressBar.setVisibility(View.GONE);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserEmail = mEmailEditTv.getText().toString();
                mUserPassword = mPasswordEditTv.getText().toString();
                if (isInputEmpty(mUserEmail, mUserPassword)) {
                    Toast.makeText(SignInActivity.this, "Please enter data for sign in!", Toast.LENGTH_SHORT).show();
                    return;
                }
                mProgressBar.setVisibility(View.VISIBLE);
                getLoaderManager().initLoader(Constants.LOG_IN_LOADER_ID, null, SignInActivity.this);
            }
        });
        mAnimation = new AlphaAnimation(1, 0);
        mAnimation.setDuration(2000);
        mAnimation.setInterpolator(new LinearInterpolator());
        mAnimation.setRepeatCount(Animation.INFINITE);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mLogo.startAnimation(mAnimation);
    }


    private boolean isInputEmpty(String userEmail, String password) {
        return userEmail.isEmpty() || password.isEmpty();
    }


    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        mAnimation.setDuration(200);
        mLogo.startAnimation(mAnimation);
        if (id == Constants.LOG_IN_LOADER_ID) {
            Uri baseUri = Uri.parse(Constants.USER_LOGIN_URL);
            Uri.Builder builder = baseUri.buildUpon();

            builder.appendQueryParameter("useremail", mUserEmail);
            builder.appendQueryParameter("password", mUserPassword);

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
            editor.putString(Constants.USER_PASSWORD, mUserPassword);
            editor.putString(Constants.SALE_PERSON_NAME, salesPerson.getName());
            editor.putFloat(Constants.SALE_PERSON_TOTAL_SALES, salesPerson.getTotalSales());
            editor.putInt(Constants.SALE_PERSON_NO_OF_ORDERS, salesPerson.getNoOfOrder());
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