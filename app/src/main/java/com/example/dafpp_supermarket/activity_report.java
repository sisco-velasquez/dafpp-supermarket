package com.example.dafpp_supermarket;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.OutputStream;

public class activity_report extends AppCompatActivity {

    // 1. Nairobi Views
    TextView tvNaiCoke, tvNaiFanta, tvNaiSprite, tvNaiTotal;
    // 2. Kisumu Views
    TextView tvKisCoke, tvKisFanta, tvKisSprite, tvKisTotal;
    // 3. Mombasa Views
    TextView tvMomCoke, tvMomFanta, tvMomSprite, tvMomTotal;
    // 4. Nakuru Views
    TextView tvNakCoke, tvNakFanta, tvNakSprite, tvNakTotal;
    // 5. Eldoret Views
    TextView tvEldCoke, tvEldFanta, tvEldSprite, tvEldTotal;

    TextView tvGrandTotal;
    Button btnRefresh, btnDownload; // Added btnDownload
    FirebaseFirestore db;

    // String to hold the text data for the file
    String reportData = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        db = FirebaseFirestore.getInstance();


        tvNaiCoke = findViewById(R.id.tv_nairobi_coke);
        tvNaiFanta = findViewById(R.id.tv_nairobi_fanta);
        tvNaiSprite = findViewById(R.id.tv_nairobi_sprite);
        tvNaiTotal = findViewById(R.id.tv_nairobi_total);


        tvKisCoke = findViewById(R.id.tv_kisumu_coke);
        tvKisFanta = findViewById(R.id.tv_kisumu_fanta);
        tvKisSprite = findViewById(R.id.tv_kisumu_sprite);
        tvKisTotal = findViewById(R.id.tv_kisumu_total);


        tvMomCoke = findViewById(R.id.tv_mombasa_coke);
        tvMomFanta = findViewById(R.id.tv_mombasa_fanta);
        tvMomSprite = findViewById(R.id.tv_mombasa_sprite);
        tvMomTotal = findViewById(R.id.tv_mombasa_total);


        tvNakCoke = findViewById(R.id.tv_nakuru_coke);
        tvNakFanta = findViewById(R.id.tv_nakuru_fanta);
        tvNakSprite = findViewById(R.id.tv_nakuru_sprite);
        tvNakTotal = findViewById(R.id.tv_nakuru_total);


        tvEldCoke = findViewById(R.id.tv_eldoret_coke);
        tvEldFanta = findViewById(R.id.tv_eldoret_fanta);
        tvEldSprite = findViewById(R.id.tv_eldoret_sprite);
        tvEldTotal = findViewById(R.id.tv_eldoret_total);


        tvGrandTotal = findViewById(R.id.tv_grand_total);
        btnRefresh = findViewById(R.id.btn_refresh);
        btnDownload = findViewById(R.id.btn_download); // Link the download button

        // Load Data
        generateReport();

        btnRefresh.setOnClickListener(view -> generateReport());

        // Download Listener
        btnDownload.setOnClickListener(view -> saveReportToFile());
    }

    private void generateReport() {
        db.collection("transactions").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        calculateBranchTotals(task.getResult());
                    } else {
                        Toast.makeText(activity_report.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void calculateBranchTotals(QuerySnapshot documents) {
        // Variables to hold running totals for ALL 5 branches
        int naiCoke = 0, naiFanta = 0, naiSprite = 0;
        int kisCoke = 0, kisFanta = 0, kisSprite = 0;
        int momCoke = 0, momFanta = 0, momSprite = 0;
        int nakCoke = 0, nakFanta = 0, nakSprite = 0;
        int eldCoke = 0, eldFanta = 0, eldSprite = 0;

        int grandTotal = 0;

        for (DocumentSnapshot doc : documents) {
            Long amountL = doc.getLong("amount");
            String product = doc.getString("product");
            String branchId = doc.getString("branch_id");

            int amount = (amountL != null) ? amountL.intValue() : 0;
            if (product == null || branchId == null) continue;

            grandTotal += amount;


            switch (branchId) {
                case "branch_nairobi_hq":
                    if (product.equalsIgnoreCase("Coke")) naiCoke += amount;
                    else if (product.equalsIgnoreCase("Fanta")) naiFanta += amount;
                    else if (product.equalsIgnoreCase("Sprite")) naiSprite += amount;
                    break;

                case "branch_kisumu":
                    if (product.equalsIgnoreCase("Coke")) kisCoke += amount;
                    else if (product.equalsIgnoreCase("Fanta")) kisFanta += amount;
                    else if (product.equalsIgnoreCase("Sprite")) kisSprite += amount;
                    break;

                case "branch_mombasa":
                    if (product.equalsIgnoreCase("Coke")) momCoke += amount;
                    else if (product.equalsIgnoreCase("Fanta")) momFanta += amount;
                    else if (product.equalsIgnoreCase("Sprite")) momSprite += amount;
                    break;

                case "branch_nakuru":
                    if (product.equalsIgnoreCase("Coke")) nakCoke += amount;
                    else if (product.equalsIgnoreCase("Fanta")) nakFanta += amount;
                    else if (product.equalsIgnoreCase("Sprite")) nakSprite += amount;
                    break;

                case "branch_eldoret":
                    if (product.equalsIgnoreCase("Coke")) eldCoke += amount;
                    else if (product.equalsIgnoreCase("Fanta")) eldFanta += amount;
                    else if (product.equalsIgnoreCase("Sprite")) eldSprite += amount;
                    break;
            }
        }




        tvNaiCoke.setText("Coke: Ksh " + naiCoke);
        tvNaiFanta.setText("Fanta: Ksh " + naiFanta);
        tvNaiSprite.setText("Sprite: Ksh " + naiSprite);
        tvNaiTotal.setText("Total: Ksh " + (naiCoke + naiFanta + naiSprite));


        tvKisCoke.setText("Coke: Ksh " + kisCoke);
        tvKisFanta.setText("Fanta: Ksh " + kisFanta);
        tvKisSprite.setText("Sprite: Ksh " + kisSprite);
        tvKisTotal.setText("Total: Ksh " + (kisCoke + kisFanta + kisSprite));


        tvMomCoke.setText("Coke: Ksh " + momCoke);
        tvMomFanta.setText("Fanta: Ksh " + momFanta);
        tvMomSprite.setText("Sprite: Ksh " + momSprite);
        tvMomTotal.setText("Total: Ksh " + (momCoke + momFanta + momSprite));


        tvNakCoke.setText("Coke: Ksh " + nakCoke);
        tvNakFanta.setText("Fanta: Ksh " + nakFanta);
        tvNakSprite.setText("Sprite: Ksh " + nakSprite);
        tvNakTotal.setText("Total: Ksh " + (nakCoke + nakFanta + nakSprite));


        tvEldCoke.setText("Coke: Ksh " + eldCoke);
        tvEldFanta.setText("Fanta: Ksh " + eldFanta);
        tvEldSprite.setText("Sprite: Ksh " + eldSprite);
        tvEldTotal.setText("Total: Ksh " + (eldCoke + eldFanta + eldSprite));


        tvGrandTotal.setText("Grand Total: Ksh " + grandTotal);

        // --- PREPARE DATA FOR DOWNLOAD ---
        StringBuilder sb = new StringBuilder();
        sb.append("--- DAFPP SUPERMARKET SALES REPORT ---\n\n");

        sb.append("NAIROBI HQ:\n");
        sb.append("Coke: ").append(naiCoke).append("\nFanta: ").append(naiFanta).append("\nSprite: ").append(naiSprite).append("\n\n");

        sb.append("KISUMU BRANCH:\n");
        sb.append("Coke: ").append(kisCoke).append("\nFanta: ").append(kisFanta).append("\nSprite: ").append(kisSprite).append("\n\n");

        sb.append("MOMBASA BRANCH:\n");
        sb.append("Coke: ").append(momCoke).append("\nFanta: ").append(momFanta).append("\nSprite: ").append(momSprite).append("\n\n");

        sb.append("NAKURU BRANCH:\n");
        sb.append("Coke: ").append(nakCoke).append("\nFanta: ").append(nakFanta).append("\nSprite: ").append(nakSprite).append("\n\n");

        sb.append("ELDORET BRANCH:\n");
        sb.append("Coke: ").append(eldCoke).append("\nFanta: ").append(eldFanta).append("\nSprite: ").append(eldSprite).append("\n\n");

        sb.append("GRAND TOTAL: Ksh ").append(grandTotal);

        // Save to global variable
        reportData = sb.toString();
    }

    private void saveReportToFile() {
        if (reportData.isEmpty()) {
            Toast.makeText(this, "No data to save yet. Refresh first.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String fileName = "SalesReport_" + System.currentTimeMillis() + ".txt";

            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);

            Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);

            if (uri != null) {
                OutputStream outputStream = getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    outputStream.write(reportData.getBytes());
                    outputStream.close();
                    Toast.makeText(this, "Report Saved to Documents!", Toast.LENGTH_LONG).show();
                }
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error saving file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}