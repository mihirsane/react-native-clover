package com.infuse.clover.bridge.payments;

import android.util.Log;

import com.clover.sdk.v3.base.Reference;
import com.clover.sdk.v3.base.Tender;
import com.clover.sdk.v3.merchant.TipSuggestion;
import com.clover.sdk.v3.payments.Credit;
import com.clover.sdk.v3.payments.Payment;
import com.clover.sdk.v3.payments.Refund;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.NoSuchKeyException;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.UnexpectedNativeTypeException;
import com.facebook.react.bridge.ReadableArray;

import java.util.ArrayList;
import java.util.List;

public class Payments {
    // Bridge constants for Clover per-transaction settings.
    public static final String CARD_ENTRY_METHODS = "cardEntryMethods";
    public static final String DISABLE_DUPLICATE_CHECKING = "disableDuplicateChecking";
    public static final String DISABLE_RESTART_TRANSACTION_ON_FAIL = "disableRestartTransactionOnFail";
    public static final String DISABLE_PRINTING = "disablePrinting";
    public static final String DISABLE_RECEIPT_SELECTION = "disableReceiptSelection";
    public static final String SIGNATURE_THRESHOLD = "signatureThreshold";
    public static final String SIGNATURE_ENTRY_LOCATION = "signatureEntryLocation";
    public static final String AUTO_ACCEPT_SIGNATURE = "autoAcceptSignature";
    public static final String TIP_AMOUNT = "tipAmount";
    public static final String TIPPABLE_AMOUNT = "tippableAmount";
    public static final String TIP_MODE = "tipMode";
    public static final String TIP_SUGGESTIONS = "tipSuggestions";
    public static final String SET_FULL_REFUND = "setFullRefund";
    public static final String AMOUNT = "amount";
    public static final String ORDER_ID = "orderId";
    public static final String PAYMENT_ID = "paymentId";
    public static final String REFUND_ID = "refundId";
    public static final String VOID_REASON = "voidReason";
    public static final String EXTERNAL_PAYMENT_ID = "externalPaymentId";
    public static final String GENERATE_EXTERNAL_PAYMENT_ID = "generateExternalPaymentId";

    public static WritableMap mapPayment(Payment payment) {
        WritableMap map = Arguments.createMap();
        map.putString("id", payment.getId());
        map.putString("externalPaymentId", payment.getExternalPaymentId());
        map.putInt("amount", payment.getAmount().intValue());
        map.putString("createdTime", payment.getCreatedTime().toString());

        map.putBoolean("offline", payment.getOffline());

        // Check for tip amount, flex/mini2 and station 2018 format differently
        // For some reason hasTipAmount returns true even when null
        int tipAmount = 0;
        if (payment.getTipAmount() != null) {
            tipAmount = payment.getTipAmount().intValue();
        }
        map.putInt("tipAmount", tipAmount);
        // clientCreatedTime seems to be null
        // modifiedTime seems to be null

        // Add in Tender
        map.putMap("tender", buildTenderMap(payment.getTender()));

        // Add in Order Ref
        map.putMap("order", buildReference(payment.getOrder()));

        return map;
    }

    public static WritableMap mapRefund(Refund refund) {
        WritableMap map = Arguments.createMap();

        map.putString("id", refund.getId());
        map.putInt("amount", refund.getAmount().intValue());
        map.putString("createdTime", refund.getCreatedTime().toString());

        // Add in Payment Ref
        map.putMap("payment", buildReference(refund.getPayment()));

        return map;
    }

    public static WritableMap mapCredit(Credit credit) {
        WritableMap map = Arguments.createMap();

        map.putString("id", credit.getId());
        map.putInt("amount", credit.getAmount().intValue());
        map.putString("createdTime", credit.getCreatedTime().toString());

        // Add in Order Ref
        map.putMap("order", buildReference(credit.getOrderRef()));

        // Add in Tender map
        map.putMap("tender", buildTenderMap(credit.getTender()));

        return map;
    }

    private static WritableMap buildTenderMap(Tender tender) {
        WritableMap map = Arguments.createMap();
        if (tender != null) {
            map.putString("id", tender.getId());
            map.putString("label", tender.getLabel());
        }
        return map;
    }

    private static WritableMap buildReference(Reference ref) {
        WritableMap map = Arguments.createMap();
        map.putString("id", ref.getId());
        return map;
    }

    private Payments() {
        throw new AssertionError();
    }

    public static List<com.clover.sdk.v3.payments.api.TipSuggestion> buildTipSuggestions(ReadableArray tips) {
        final String NAME_PARAMETER = "name";
        final String PERCENTAGE_PARAMETER = "percentage";
        List<com.clover.sdk.v3.payments.api.TipSuggestion> tipSuggestions = new ArrayList<>();
        for (int i = 0; i < tips.size(); i++) {
            ReadableMap tip = tips.getMap(i);
            try {

                com.clover.sdk.v3.payments.api.TipSuggestion tipSuggestion = new com.clover.sdk.v3.payments.api.TipSuggestion(tip.getString(NAME_PARAMETER),null,(long) tip.getInt(PERCENTAGE_PARAMETER));
                tipSuggestions.add(tipSuggestion);
            } catch(NoSuchKeyException | UnexpectedNativeTypeException e) {
                Log.e("ReactNativeClover", "Skipping invalid TipSuggestion at index: " + i, e);
            }
        }
        return tipSuggestions;
    }
}
