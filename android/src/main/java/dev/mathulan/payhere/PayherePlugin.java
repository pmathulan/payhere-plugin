package dev.mathulan.payhere;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Payhere imports.
 */
import lk.payhere.androidsdk.*;
import lk.payhere.androidsdk.model.*;
import lk.payhere.androidsdk.util.*;
@NativePlugin
public class PayherePlugin extends Plugin {
    private final static int PAYHERE_REQUEST = 111010;
    protected PluginCall call;
    private static final String TAG = "PAYHERE_PLUGIN";
//    public void echo(PluginCall call) {
//        String value = call.getString("value");
//
//        JSObject ret = new JSObject();
//        ret.put("value", value);
//        call.success(ret);
//    }
    @PluginMethod
    public void checkout(String message, PluginCall call) {
        Log.i(TAG, message);
        this.call = call;
        InitRequest req = new InitRequest();
        boolean isSandboxEnabled = false;
        try {
            JSONObject data = new JSONObject(message);

            if(data.has("sandboxEnabled")) {
                isSandboxEnabled = data.getBoolean("sandboxEnabled");
            }

            req.setMerchantId(data.getString("merchantId"));       // Your Merchant ID
            req.setMerchantSecret(data.getString("merchantSecret")); // Your Merchant Secret (Add your app at Settings > Domains & Credentials, to get this)
            req.setCurrency(data.getString("currency"));             // Currency code LKR/USD/GBP/EUR/AUD
            req.setAmount(data.getDouble("amount"));             // Final Amount to be charged
            req.setOrderId(data.getString("orderId"));        // Unique Reference ID
            req.setNotifyUrl(data.getString("notifyURL"));        // Unique Reference ID
            req.setItemsDescription(data.getString("itemsDescription"));  // Item description title
            req.setCustom1(data.optString("custom1"));
            req.setCustom2(data.optString("custom2"));

            // customer details
            if(data.has("customer")) {
                req.getCustomer().setFirstName(data.getJSONObject("customer").optString("firstName"));
                req.getCustomer().setLastName(data.getJSONObject("customer").optString("lastName"));
                req.getCustomer().setEmail(data.getJSONObject("customer").optString("email"));
                req.getCustomer().setPhone(data.getJSONObject("customer").optString("phone"));
            }

            // billing details
            if(data.has("billing")) {
                req.getCustomer().getAddress().setAddress(data.getJSONObject("billing").optString("address"));
                req.getCustomer().getAddress().setCity(data.getJSONObject("billing").optString("city"));
                req.getCustomer().getAddress().setCountry(data.getJSONObject("billing").optString("country"));
            }

            //shipping details
            if(data.has("shipping")) {
                req.getCustomer().getDeliveryAddress().setAddress(data.getJSONObject("shipping").optString("address"));
                req.getCustomer().getDeliveryAddress().setCity(data.getJSONObject("shipping").optString("city"));
                req.getCustomer().getDeliveryAddress().setCountry(data.getJSONObject("shipping").optString("country"));
            }

            if(data.has("items")) {
                JSONArray items = data.getJSONArray("items");
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    req.getItems().add(new Item(item.getString("id"), item.getString("name"), item.getInt("quantity"), item.getDouble("amount")));
                }
            }


        } catch (JSONException $je) {
            call.error(getFormattedResponse("error",-100,"Invalid data provided",null).toString());
        }
        Intent intent = new Intent(getActivity(), PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(isSandboxEnabled ? PHConfigs.SANDBOX_URL : PHConfigs.LIVE_URL);
            startActivityForResult(call, intent, PAYHERE_REQUEST); //unique request ID like private final static int PAYHERE_REQUEST = 11010;
    }

    @Override
    protected void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
        super.handleOnActivityResult(requestCode, resultCode, data);

        // Get the previously saved call
        PluginCall savedCall = getSavedCall();
        JSObject responseObj = new JSObject();

        if (requestCode == PAYHERE_REQUEST && data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);

            if (response.isSuccess()) {
                responseObj = getFormattedResponse("success",201,"Payment completed successfully",response.getData());
            } else {
                responseObj = getFormattedResponse("error",-1,"Payment failed",response.getData());
            }

        }else{

            if(resultCode == 0 ) {
                responseObj = getFormattedResponse("error",0,"Cancelled by user",null);
            }else{
                responseObj = getFormattedResponse("error",resultCode,"Something went wrong",null);
            }

        }

        Log.d(TAG, responseObj.toString());
        call.success(responseObj);
    }

    private JSObject getFormattedResponse(String status, int statusCode, String message, StatusResponse data ){
        JSObject responseObj = new JSObject();
        responseObj.put("status", status);
        responseObj.put("statusCode",statusCode);
        responseObj.put("message", message);
        if(data!= null){
            responseObj.put("data",data);
        }
        return responseObj;
    }
}
