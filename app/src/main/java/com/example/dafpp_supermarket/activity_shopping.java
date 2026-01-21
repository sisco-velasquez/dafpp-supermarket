package com.example.dafpp_supermarket;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class activity_shopping extends AppCompatActivity {

    TextView tvTitle, tvPrice, tvStock, tvSelectedLabel;
    ImageView imgPreviewLarge;
    TextInputEditText etDestination, etPhone, etQuantity; // Added etQuantity
    Button btnBuy, btnTrack;
    LinearLayout cardCoke, cardFanta, cardSprite;

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    String currentBranchId;
    String selectedProduct = null;
    int currentStock = 0;
    int unitPrice = 0; // Renamed from currentPrice to unitPrice for clarity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentBranchId = getIntent().getStringExtra("BRANCH_ID");

        // Auth Guard
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Views
        tvTitle = findViewById(R.id.tv_branch_title);
        tvPrice = findViewById(R.id.tv_price);
        tvStock = findViewById(R.id.tv_stock_status);
        tvSelectedLabel = findViewById(R.id.tv_selected_label);
        imgPreviewLarge = findViewById(R.id.img_preview_large);

        etDestination = findViewById(R.id.et_destination);
        etPhone = findViewById(R.id.et_phone);
        etQuantity = findViewById(R.id.et_quantity); // Link new input

        btnBuy = findViewById(R.id.btn_buy);
        btnTrack = findViewById(R.id.btn_track_orders);

        cardCoke = findViewById(R.id.card_coke);
        cardFanta = findViewById(R.id.card_fanta);
        cardSprite = findViewById(R.id.card_sprite);

        tvTitle.setText("Shopping at " + getBranchName(currentBranchId));

        // Default State
        tvSelectedLabel.setText("Please select a drink");
        btnBuy.setEnabled(false);
        btnBuy.setAlpha(0.5f);

        // Click Listeners
        cardCoke.setOnClickListener(v -> selectProduct("Coke"));
        cardFanta.setOnClickListener(v -> selectProduct("Fanta"));
        cardSprite.setOnClickListener(v -> selectProduct("Sprite"));

        // --- BUY BUTTON LOGIC (UPDATED) ---
        btnBuy.setOnClickListener(v -> {
            if (selectedProduct == null) return;

            String destination = etDestination.getText().toString();
            String phone = etPhone.getText().toString().trim();
            String qtyStr = etQuantity.getText().toString().trim();

            // Validations
            if (TextUtils.isEmpty(destination) || TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(qtyStr)) {
                Toast.makeText(this, "Enter Quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse Quantity
            int quantityToBuy = Integer.parseInt(qtyStr);
            if (quantityToBuy <= 0) {
                Toast.makeText(this, "Quantity must be at least 1", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check Stock
            if (currentStock >= quantityToBuy) {
                // Calculate Total Price
                int totalAmount = unitPrice * quantityToBuy;

                Toast.makeText(this, "Processing Ksh " + totalAmount + "...", Toast.LENGTH_LONG).show();
                btnBuy.setEnabled(false);

                // M-Pesa Trigger
                DarajaApiClient daraja = new DarajaApiClient();
                daraja.triggerStkPush(phone, totalAmount, new DarajaApiClient.MpesaListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(activity_shopping.this, "Enter PIN on phone!", Toast.LENGTH_LONG).show();
                        placeOrder(destination, quantityToBuy, totalAmount);
                        btnBuy.setEnabled(true);
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(activity_shopping.this, "Payment Failed: " + error, Toast.LENGTH_LONG).show();
                        btnBuy.setEnabled(true);
                    }
                });

            } else {
                Toast.makeText(this, "Only " + currentStock + " items left in stock!", Toast.LENGTH_SHORT).show();
            }
        });

        btnTrack.setOnClickListener(v -> startActivity(new Intent(this, activity_order_tracking.class)));
    }

    private void selectProduct(String productName) {
        selectedProduct = productName;
        tvSelectedLabel.setText("Selected: " + productName);
        btnBuy.setEnabled(true);
        btnBuy.setAlpha(1.0f);

        // Visual Reset
        cardCoke.setBackgroundColor(Color.WHITE);
        cardFanta.setBackgroundColor(Color.WHITE);
        cardSprite.setBackgroundColor(Color.WHITE);

        if (productName.equals("Coke")) {
            cardCoke.setBackgroundColor(Color.parseColor("#FFEBEE"));
            imgPreviewLarge.setImageResource(R.drawable.coke);
        } else if (productName.equals("Fanta")) {
            cardFanta.setBackgroundColor(Color.parseColor("#FFF3E0"));
            imgPreviewLarge.setImageResource(R.drawable.fanta);
        } else if (productName.equals("Sprite")) {
            cardSprite.setBackgroundColor(Color.parseColor("#E8F5E9"));
            imgPreviewLarge.setImageResource(R.drawable.sprite);
        }
        fetchProductDetails();
    }

    private void fetchProductDetails() {
        tvStock.setText("Checking...");
        String productId = "product_" + selectedProduct.toLowerCase();

        db.collection("branches").document(currentBranchId)
                .collection("inventory").document(productId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        DocumentSnapshot doc = task.getResult();
                        Long stockLong = doc.getLong("quantity");
                        Long priceLong = doc.getLong("price");

                        currentStock = (stockLong != null) ? stockLong.intValue() : 0;
                        unitPrice = (priceLong != null) ? priceLong.intValue() : 0;

                        tvPrice.setText("Price: Ksh " + unitPrice); // Show Unit Price
                        tvStock.setText("Stock: " + currentStock);
                    } else {
                        currentStock = 0;
                        tvPrice.setText("Not Stocked");
                    }
                });
    }

    private void placeOrder(String destination, int qty, int totalPay) {
        String productId = "product_" + selectedProduct.toLowerCase();

        // 1. Decrement Stock by Quantity Purchased (Not just -1)
        db.collection("branches").document(currentBranchId)
                .collection("inventory").document(productId)
                .update("quantity", FieldValue.increment(-qty));

        // 2. Save Order Details
        Map<String, Object> order = new HashMap<>();
        order.put("user_id", mAuth.getCurrentUser().getUid());
        order.put("user_email", mAuth.getCurrentUser().getEmail());
        order.put("branch_id", currentBranchId);
        order.put("product", selectedProduct);
        order.put("quantity", qty); // Save Quantity
        order.put("amount", totalPay); // Save Total Amount Paid
        order.put("destination", destination);
        order.put("status", "Pending");
        order.put("date", new Date());

        db.collection("orders").add(order);
        db.collection("transactions").add(order);

        Toast.makeText(this, "Order Placed Successfully!", Toast.LENGTH_LONG).show();
        etDestination.setText("");
        etQuantity.setText("1"); // Reset quantity to 1
        fetchProductDetails();
    }

    private String getBranchName(String id) {
        if (id == null) return "Supermarket";
        return id;
    }
}