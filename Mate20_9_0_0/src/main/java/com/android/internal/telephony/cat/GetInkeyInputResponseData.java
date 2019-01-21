package com.android.internal.telephony.cat;

import com.android.internal.telephony.EncodeException;
import com.android.internal.telephony.GsmAlphabet;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/* compiled from: ResponseData */
class GetInkeyInputResponseData extends ResponseData {
    protected static final byte GET_INKEY_NO = (byte) 0;
    protected static final byte GET_INKEY_YES = (byte) 1;
    private static final String TAG = "ResponseData";
    public String mInData;
    private boolean mIsPacked;
    private boolean mIsUcs2;
    private boolean mIsYesNo;
    private boolean mYesNoResponse;

    public GetInkeyInputResponseData(String inData, boolean ucs2, boolean packed) {
        this.mIsUcs2 = ucs2;
        this.mIsPacked = packed;
        this.mInData = inData;
        this.mIsYesNo = false;
    }

    public GetInkeyInputResponseData(boolean yesNoResponse) {
        this.mIsUcs2 = false;
        this.mIsPacked = false;
        this.mInData = "";
        this.mIsYesNo = true;
        this.mYesNoResponse = yesNoResponse;
    }

    public void format(ByteArrayOutputStream buf) {
        if (buf != null) {
            byte[] data;
            buf.write(128 | ComprehensionTlvTag.TEXT_STRING.value());
            int i = 0;
            if (this.mIsYesNo) {
                data = new byte[]{this.mYesNoResponse};
            } else if (this.mInData == null || this.mInData.length() <= 0) {
                data = new byte[0];
            } else {
                try {
                    if (this.mIsUcs2) {
                        data = this.mInData.getBytes("UTF-16BE");
                    } else if (this.mIsPacked) {
                        data = GsmAlphabet.stringToGsm7BitPacked(this.mInData, 0, 0);
                        byte[] data2 = new byte[(data.length - 1)];
                        System.arraycopy(data, 1, data2, 0, data.length - 1);
                        data = data2;
                    } else {
                        data = GsmAlphabet.stringToGsm8BitPacked(this.mInData);
                    }
                } catch (UnsupportedEncodingException e) {
                    data = new byte[0];
                } catch (EncodeException e2) {
                    data = new byte[0];
                }
            }
            if (data.length + 1 <= 255) {
                ResponseData.writeLength(buf, data.length + 1);
            } else {
                data = new byte[0];
            }
            if (this.mIsUcs2) {
                buf.write(8);
            } else if (this.mIsPacked) {
                buf.write(0);
            } else {
                buf.write(4);
            }
            int length = data.length;
            while (i < length) {
                buf.write(data[i]);
                i++;
            }
        }
    }
}
