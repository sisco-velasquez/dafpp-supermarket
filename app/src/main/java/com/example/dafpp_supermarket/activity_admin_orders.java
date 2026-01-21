package com.example.dafpp_supermarket;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class activity_admin_orders extends AppCompatActivity {

    LinearLayout ordersContainer;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        ordersContainer = findViewById(R.id.orders_container);
        db = FirebaseFirestore.getInstance();

        loadOrders();
    }

    private void loadOrders() {
        // Clear previous list so we don't double up when refreshing
        ordersContainer.removeAllViews();

        // Fetch Orders ordered by Date (Newest first)
        db.collection("orders")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Add a Header "Manage Orders" back manually if needed,
                        // or rely on XML static header (if your XML container is separate).
                        // Here we just append cards.
                        for (DocumentSnapshot doc : task.getResult()) {
                            addOrderCard(doc);
                        }
                    } else {
                        Toast.makeText(activity_admin_orders.this, "Error loading orders", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addOrderCard(DocumentSnapshot doc) {
        // 1. Inflate the row layout
        View view = getLayoutInflater().inflate(R.layout.activity_item_order, null);

        // 2. Find views inside that row
        TextView tvProduct = view.findViewById(R.id.tv_order_product);
        TextView tvUser = view.findViewById(R.id.tv_order_user);       // NEW: User Email
        TextView tvBranch = view.findViewById(R.id.tv_order_branch);   // NEW: Branch Name
        TextView tvDest = view.findViewById(R.id.tv_order_destination);
        TextView tvStatus = view.findViewById(R.id.tv_order_status);
        Button btnUpdate = view.findViewById(R.id.btn_update_status);

        // 3. Get Data from Firestore
        String product = doc.getString("product");
        String email = doc.getString("user_email");   // NEW: Get Email
        String branchId = doc.getString("branch_id"); // NEW: Get Branch ID
        String destination = doc.getString("destination");
        String status = doc.getString("status");

        // Use quantity if available, else default
        Long qtyL = doc.getLong("quantity");
        int qty = (qtyL != null) ? qtyL.intValue() : 1;

        // 4. Set Data
        tvProduct.setText(product + " (x" + qty + ")");
        tvUser.setText("User: " + (email != null ? email : "Unknown")); // Show Email
        tvBranch.setText("From: " + formatBranchName(branchId));        // Show Formatted Branch
        tvDest.setText("To: " + destination);
        tvStatus.setText(status);

        // Color Code the Status
        if ("Pending".equals(status)) {
            tvStatus.setTextColor(Color.parseColor("#FF9800")); // Orange
        } else if ("Shipped".equals(status)) {
            tvStatus.setTextColor(Color.parseColor("#2196F3")); // Blue
        } else if ("Delivered".equals(status)) {
            tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
        }

        // 5. Button Click -> Show Popup to Change Status
        btnUpdate.setOnClickListener(v -> showStatusDialog(doc.getId(), status));

        // 6. Add this card to the main screen
        ordersContainer.addView(view);
    }

    // --- Helper Method to make Branch Names Readable ---
    private String formatBranchName(String branchId) {
        if (branchId == null) return "Unknown Branch";
        if (branchId.contains("nairobi")) return "Nairobi HQ";
        if (branchId.contains("kisumu")) return "Kisumu Branch";
        if (branchId.contains("mombasa")) return "Mombasa Branch";
        if (branchId.contains("nakuru")) return "Nakuru Branch";
        if (branchId.contains("eldoret")) return "Eldoret Branch";
        return branchId; // Fallback
    }

    private void showStatusDialog(String docId, String currentStatus) {
        String[] statuses = {"Pending", "Shipped", "Delivered", "Cancelled"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Order Status");
        builder.setItems(statuses, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newStatus = statuses[which];
                updateOrderStatus(docId, newStatus);
            }
        });
        builder.show();
    }

    private void updateOrderStatus(String docId, String newStatus) {
        db.collection("orders").document(docId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(activity_admin_orders.this, "Status Updated!", Toast.LENGTH_SHORT).show();
                    // Refresh the list to show new status
                    loadOrders();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(activity_admin_orders.this, "Update Failed", Toast.LENGTH_SHORT).show();
                });
    }
}