package com.example.dafpp_supermarket;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    LinearLayout layoutAdmin, layoutCustomer;
    TextView tvWelcome;
    Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
        layoutAdmin = findViewById(R.id.layout_admin);
        layoutCustomer = findViewById(R.id.layout_customer);
        tvWelcome = findViewById(R.id.tv_welcome);
        btnLogout = findViewById(R.id.btn_logout);

        // Get the Role passed from Login Activity
        String userRole = getIntent().getStringExtra("USER_ROLE");

        // Show/Hide interfaces based on Role
        if (userRole != null && userRole.equals("Admin")) {
            // SHOW ADMIN DASHBOARD
            layoutAdmin.setVisibility(View.VISIBLE);
            layoutCustomer.setVisibility(View.GONE);
            tvWelcome.setText("Welcome, Admin");
        } else {
            // SHOW CUSTOMER DASHBOARD (Default)
            layoutAdmin.setVisibility(View.GONE);
            layoutCustomer.setVisibility(View.VISIBLE);
            tvWelcome.setText("Welcome, Valued Customer");
        }

        // Logout Logic
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });


        // Restock Button
        Button btnRestock = findViewById(R.id.btn_restock);
        btnRestock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, restock.class);
                startActivity(intent);
            }
        });

        //View Reports Button
        Button btnReports = findViewById(R.id.btn_view_reports);
        btnReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, activity_report.class);
                startActivity(intent);
            }
        });



        //Find the buttons
        Button btnNairobi = findViewById(R.id.btn_shop_nairobi);
        Button btnKisumu = findViewById(R.id.btn_shop_kisumu);
        Button btnMombasa = findViewById(R.id.btn_shop_mombasa);
        Button btnNakuru = findViewById(R.id.btn_shop_nakuru);
        Button btnEldoret = findViewById(R.id.btn_shop_eldoret);

        //Add Click Listeners
        btnNairobi.setOnClickListener(v -> openShopping("branch_nairobi_hq"));
        btnKisumu.setOnClickListener(v -> openShopping("branch_kisumu"));
        btnMombasa.setOnClickListener(v -> openShopping("branch_mombasa"));
        btnNakuru.setOnClickListener(v -> openShopping("branch_nakuru"));
        btnEldoret.setOnClickListener(v -> openShopping("branch_eldoret"));
    }


    // Opens the Shopping page and tells it which branch to load
    private void openShopping(String branchId) {
        Intent intent = new Intent(MainActivity.this, activity_shopping.class);
        intent.putExtra("BRANCH_ID", branchId);
        startActivity(intent);
    }
}