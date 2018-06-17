package com.safdar.medicento.salesappmedicento;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class SalesPersonDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_person_details);
    }
    public void signOut(View view) {
        Intent intent = new Intent(SalesPersonDetails.this,AddAreaActivity.class);
        startActivity(intent);
    }
    public void goBack(View view) {
        Intent intent = new Intent(SalesPersonDetails.this,AddAreaActivity.class);
        startActivity(intent);
    }
}

