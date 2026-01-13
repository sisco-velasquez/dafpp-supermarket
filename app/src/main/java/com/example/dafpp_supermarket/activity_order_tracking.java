package com.example.dafpp_supermarket;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class activity_order_tracking extends AppCompatActivity {

    TextView tvStatus;
    Button btnRefresh;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        tvStatus = findViewById(R.id.tv_order_status);
        btnRefresh = findViewById(R.id.btn_refresh_tracking);

        loadOrders();

        btnRefresh.setOnClickListener(v -> loadOrders());
    }

    private void loadOrders() {
        String myUserId = mAuth.getCurrentUser().getUid();
        tvStatus.setText("Checking...");

        db.collection("orders")
                .whereEqualTo("user_id", myUserId) // Only show MY orders
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        StringBuilder builder = new StringBuilder();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String product = doc.getString("product");
                            String status = doc.getString("status");
                            String dest = doc.getString("destination");

                            builder.append("Item: ").append(product).append("\n");
                            builder.append("To: ").append(dest).append("\n");
                            builder.append("Status: ").append(status).append("\n");
                            builder.append("----------------\n");
                        }
                        if (builder.length() == 0) {
                            tvStatus.setText("No orders found.");
                        } else {
                            tvStatus.setText(builder.toString());
                        }
                    } else {
                        tvStatus.setText("Error loading orders.");
                    }
                });
    }
}