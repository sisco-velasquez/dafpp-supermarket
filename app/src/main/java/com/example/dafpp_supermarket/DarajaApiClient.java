package com.example.dafpp_supermarket;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DarajaApiClient {


    private static final String CONSUMER_KEY = "lAgF1NZIEgL9JiMsWAoNyFIpSWHZ6VjrQpnCVlHyPEATlqOP";
    private static final String CONSUMER_SECRET = "4GWdGQUI4jQVWvLNBkyLZ3EpGDAHxUs7Tq2oPjGJyEtAtmqUBCjsVNftc1htvIyR";
    private static final String BUSINESS_SHORT_CODE = "174379"; // Default Sandbox Paybill
    private static final String PASSKEY = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919"; // Default Sandbox Passkey


    private final OkHttpClient client = new OkHttpClient();
    private String accessToken;

    public interface MpesaListener {
        void onSuccess();
        void onError(String error);
    }

    // get the Access Token
    public void triggerStkPush(String phoneNumber, int amount, MpesaListener listener) {
        String auth = CONSUMER_KEY + ":" + CONSUMER_SECRET;
        String encodedAuth = Base64.encodeToString(auth.getBytes(StandardCharsets.ISO_8859_1), Base64.NO_WRAP);

        Request request = new Request.Builder()
                .url("https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials")
                .addHeader("Authorization", "Basic " + encodedAuth)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnMainThread(() -> listener.onError("Token Failed: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);
                        accessToken = jsonObject.getString("access_token");

                        // Token received! Now send the actual STK Push
                        performStkPush(phoneNumber, amount, listener);

                    } catch (Exception e) {
                        runOnMainThread(() -> listener.onError("Token Parse Error"));
                    }
                } else {
                    runOnMainThread(() -> listener.onError("Auth Failed: " + response.code()));
                }
            }
        });
    }

    // Perform the STK Push
    private void performStkPush(String phoneNumber, int amount, MpesaListener listener) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
            String password = Base64.encodeToString((BUSINESS_SHORT_CODE + PASSKEY + timestamp).getBytes(), Base64.NO_WRAP);

            JSONObject json = new JSONObject();
            json.put("BusinessShortCode", BUSINESS_SHORT_CODE);
            json.put("Password", password);
            json.put("Timestamp", timestamp);
            json.put("TransactionType", "CustomerPayBillOnline");
            json.put("Amount", amount);
            json.put("PartyA", phoneNumber); // The phone sending money
            json.put("PartyB", BUSINESS_SHORT_CODE); // The paybill receiving money
            json.put("PhoneNumber", phoneNumber);
            json.put("CallBackURL", "https://mydomain.com/path"); // Not needed for simple student demo, but required by API
            json.put("AccountReference", "DAFPP Supermarket");
            json.put("TransactionDesc", "Buying Drinks");

            RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url("https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnMainThread(() -> listener.onError("STK Failed: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnMainThread(() -> listener.onSuccess());
                    } else {
                        String errorBody = response.body().string();
                        runOnMainThread(() -> listener.onError("M-Pesa Error: " + errorBody));
                    }
                }
            });

        } catch (Exception e) {
            runOnMainThread(() -> listener.onError("JSON Error"));
        }
    }

    // Helper to switch back to the main UI thread (because OkHttp runs in background)
    private void runOnMainThread(Runnable action) {
        new Handler(Looper.getMainLooper()).post(action);
    }
}