package com.android.internal.telephony.uicc.euicc;

import com.android.internal.telephony.uicc.asn1.Asn1Node;
import com.android.internal.telephony.uicc.euicc.apdu.RequestBuilder;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$EuiccCard$AWltG4uFbHn2Xq7ZPpU3U1qOqVM implements ApduRequestBuilder {
    public static final /* synthetic */ -$$Lambda$EuiccCard$AWltG4uFbHn2Xq7ZPpU3U1qOqVM INSTANCE = new -$$Lambda$EuiccCard$AWltG4uFbHn2Xq7ZPpU3U1qOqVM();

    private /* synthetic */ -$$Lambda$EuiccCard$AWltG4uFbHn2Xq7ZPpU3U1qOqVM() {
    }

    public final void build(RequestBuilder requestBuilder) {
        requestBuilder.addStoreData(Asn1Node.newBuilder(48963).build().toHex());
    }
}
