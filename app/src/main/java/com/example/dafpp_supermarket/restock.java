package com.example.dafpp_supermarket;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class restock extends AppCompatActivity {

    Spinner spinnerBranch, spinnerProduct;
    TextInputEditText etQuantity, etPrice;
    Button btnUpdate;
    FirebaseFirestore db;

    String[] branches = {"Nairobi HQ", "Kisumu", "Mombasa", "Nakuru", "Eldoret"};
    String[] products = {"Coke", "Fanta", "Sprite"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restock);

        db = FirebaseFirestore.getInstance();

        // Initialize Views
        spinnerBranch = findViewById(R.id.spinner_branch);
        spinnerProduct = findViewById(R.id.spinner_product);
        etQuantity = findViewById(R.id.et_quantity);
        etPrice = findViewById(R.id.et_price);
        btnUpdate = findViewById(R.id.btn_update_stock);

        // Setup Spinners
        ArrayAdapter<String> branchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, branches);
        spinnerBranch.setAdapter(branchAdapter);

        ArrayAdapter<String> productAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, products);
        spinnerProduct.setAdapter(productAdapter);

        // Update Button Logic
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateInventory();
            }
        });
    }

    private void updateInventory() {
        // Get Inputs
        String selectedBranchName = spinnerBranch.getSelectedItem().toString();
        String selectedProduct = spinnerProduct.getSelectedItem().toString();
        String quantityStr = etQuantity.getText().toString();
        String priceStr = etPrice.getText().toString(); // Get Price

        // Validate Inputs
        if (TextUtils.isEmpty(quantityStr)) {
            etQuantity.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(priceStr)) {
            etPrice.setError("Required");
            return;
        }

        int quantityToAdd = Integer.parseInt(quantityStr);
        int newPrice = Integer.parseInt(priceStr);

        // Map the IDs
        String branchId = getBranchId(selectedBranchName);
        String productId = "product_" + selectedProduct.toLowerCase();

        // Database Reference
        DocumentReference productRef = db.collection("branches")
                .document(branchId)
                .collection("inventory")
                .document(productId);

        // Check and Update
        productRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {

                    productRef.update(
                            "quantity", FieldValue.increment(quantityToAdd),
                            "price", newPrice // Update the price to whatever Admin typed
                    ).addOnSuccessListener(aVoid -> {
                        Toast.makeText(restock.this, "Stock & Price Updated!", Toast.LENGTH_SHORT).show();
                        clearInputs();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(restock.this, "Error updating", Toast.LENGTH_SHORT).show();
                    });

                } else {

                    Map<String, Object> newProductData = new HashMap<>();
                    newProductData.put("name", selectedProduct);
                    newProductData.put("quantity", quantityToAdd);
                    newProductData.put("price", newPrice);

                    productRef.set(newProductData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(restock.this, "New Product Created!", Toast.LENGTH_SHORT).show();
                                clearInputs();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(restock.this, "Error creating product", Toast.LENGTH_SHORT).show();
                            });
                }
            } else {
                Toast.makeText(restock.this, "Connection Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearInputs() {
        etQuantity.setText("");
        etPrice.setText("");
    }

    private String getBranchId(String name) {
        switch (name) {
            case "Nairobi HQ": return "branch_nairobi_hq";
            case "Kisumu": return "branch_kisumu";
            case "Mombasa": return "branch_mombasa";
            case "Nakuru": return "branch_nakuru";
            case "Eldoret": return "branch_eldoret";
            default: return "branch_nairobi_hq";
        }
    }
}