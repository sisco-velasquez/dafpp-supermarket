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
    TextInputEditText etDestination, etPhone;
    Button btnBuy, btnTrack;
    LinearLayout cardCoke, cardFanta, cardSprite;

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    String currentBranchId;
    String selectedProduct = null; // CHANGE: Default is now null (No selection)
    int currentStock = 0;
    int currentPrice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentBranchId = getIntent().getStringExtra("BRANCH_ID");

        // AUTH GUARD: Check if User is Logged In
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "You must be logged in to shop!", Toast.LENGTH_SHORT).show();
            // Send back to Login Page
            Intent intent = new Intent(this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return; // Stop running this activity
        }

        // Initialize Views
        tvTitle = findViewById(R.id.tv_branch_title);
        tvPrice = findViewById(R.id.tv_price);
        tvStock = findViewById(R.id.tv_stock_status);
        tvSelectedLabel = findViewById(R.id.tv_selected_label);
        imgPreviewLarge = findViewById(R.id.img_preview_large);
        etDestination = findViewById(R.id.et_destination);
        etPhone = findViewById(R.id.et_phone);
        btnBuy = findViewById(R.id.btn_buy);
        btnTrack = findViewById(R.id.btn_track_orders);
        cardCoke = findViewById(R.id.card_coke);
        cardFanta = findViewById(R.id.card_fanta);
        cardSprite = findViewById(R.id.card_sprite);

        tvTitle.setText("Shopping at " + getBranchName(currentBranchId));

        tvSelectedLabel.setText("Please select a drink above");
        tvPrice.setText("");
        tvStock.setText("");
        btnBuy.setEnabled(false); // Disable button
        btnBuy.setAlpha(0.5f);    // Make it look disabled (greyed out)

        // Set Click Listeners
        cardCoke.setOnClickListener(v -> selectProduct("Coke"));
        cardFanta.setOnClickListener(v -> selectProduct("Fanta"));
        cardSprite.setOnClickListener(v -> selectProduct("Sprite"));

        // Buy Button Logic
        btnBuy.setOnClickListener(v -> {
            // Extra safety check
            if (selectedProduct == null) {
                Toast.makeText(this, "Please select a product first", Toast.LENGTH_SHORT).show();
                return;
            }

            String destination = etDestination.getText().toString();
            String phoneNumber = etPhone.getText().toString().trim();

            if (TextUtils.isEmpty(destination)) {
                Toast.makeText(this, "Please enter a destination", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(this, "Enter M-Pesa Phone Number", Toast.LENGTH_SHORT).show();
                return;
            }

            // Start Payment Flow
            if (currentStock > 0) {
                Toast.makeText(this, "Sending M-Pesa Request...", Toast.LENGTH_LONG).show();
                btnBuy.setEnabled(false);

                DarajaApiClient daraja = new DarajaApiClient();
                daraja.triggerStkPush(phoneNumber, currentPrice, new DarajaApiClient.MpesaListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(activity_shopping.this, "Enter PIN on your phone!", Toast.LENGTH_LONG).show();
                        placeOrder(destination);
                        btnBuy.setEnabled(true);
                    }
                    @Override
                    public void onError(String error) {
                        Toast.makeText(activity_shopping.this, "Payment Failed: " + error, Toast.LENGTH_LONG).show();
                        btnBuy.setEnabled(true);
                    }
                });
            } else {
                Toast.makeText(this, "Out of Stock!", Toast.LENGTH_SHORT).show();
            }
        });

        btnTrack.setOnClickListener(v -> {
            Intent intent = new Intent(this, activity_order_tracking.class);
            startActivity(intent);
        });
    }

    private void selectProduct(String productName) {
        selectedProduct = productName;
        tvSelectedLabel.setText("Selected: " + productName);

        // Enable the button now that a selection is made
        btnBuy.setEnabled(true);
        btnBuy.setAlpha(1.0f);

        // Reset colors
        cardCoke.setBackgroundColor(Color.WHITE);
        cardFanta.setBackgroundColor(Color.WHITE);
        cardSprite.setBackgroundColor(Color.WHITE);

        // Highlight selection
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
                        currentPrice = (priceLong != null) ? priceLong.intValue() : 0;

                        tvPrice.setText("Price: Ksh " + currentPrice);
                        tvStock.setText("Availability: " + currentStock + " items");
                    } else {
                        currentStock = 0;
                        tvPrice.setText("Availability: Not Stocked");
                    }
                });
    }

    private void placeOrder(String destination) {
        String productId = "product_" + selectedProduct.toLowerCase();
        db.collection("branches").document(currentBranchId)
                .collection("inventory").document(productId)
                .update("quantity", FieldValue.increment(-1));

        Map<String, Object> order = new HashMap<>();
        order.put("user_id", mAuth.getCurrentUser().getUid());
        order.put("branch_id", currentBranchId);
        order.put("product", selectedProduct);
        order.put("amount", currentPrice);
        order.put("destination", destination);
        order.put("status", "Pending");
        order.put("date", new Date());

        db.collection("orders").add(order);
        db.collection("transactions").add(order);

        Toast.makeText(this, "Order Placed Successfully!", Toast.LENGTH_LONG).show();
        etDestination.setText("");
        fetchProductDetails();
    }

    private String getBranchName(String id) {
        if (id == null) return "Supermarket";
        return id;
    }
}