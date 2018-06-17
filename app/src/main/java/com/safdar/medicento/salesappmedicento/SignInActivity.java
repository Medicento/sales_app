package com.safdar.medicento.salesappmedicento;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SignInActivity extends AppCompatActivity {

    EditText mEmailEditTv;
    EditText mPasswordEditTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mEmailEditTv = findViewById(R.id.editTextEmail);
        mPasswordEditTv = findViewById(R.id.editTextPassword);
        Button btn = findViewById(R.id.login_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = PlaceOrdersActivity.mSharedPreferences.edit();
                editor.putString(PlaceOrdersActivity.USER_EMAIL, mEmailEditTv.getText().toString());
                editor.putString(PlaceOrdersActivity.USER_PASSWORD, mPasswordEditTv.getText().toString());
                editor.apply();
                Intent intent = new Intent(SignInActivity.this, PlaceOrdersActivity.class);
                startActivity(intent);
            }
        });
    }
}
