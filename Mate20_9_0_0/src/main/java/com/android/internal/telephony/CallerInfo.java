package com.android.internal.telephony;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Country;
import android.location.CountryDetector;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.android.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder;
import java.util.Locale;

public class CallerInfo {
    private static final String TAG = "CallerInfo";
    public static final long USER_TYPE_CURRENT = 0;
    public static final long USER_TYPE_WORK = 1;
    private static final boolean VDBG = Rlog.isLoggable(TAG, 2);
    public Drawable cachedPhoto;
    public Bitmap cachedPhotoIcon;
    public String cnapName;
    public Uri contactDisplayPhotoUri;
    public boolean contactExists;
    public long contactIdOrZero;
    public Uri contactRefUri;
    public Uri contactRingtoneUri;
    public String geoDescription;
    public boolean isCachedPhotoCurrent;
    public String lookupKey;
    private boolean mIsEmergency = false;
    private boolean mIsVoiceMail = false;
    public int mVoipDeviceType;
    public String name;
    public int namePresentation;
    public boolean needUpdate;
    public String normalizedNumber;
    public String numberLabel;
    public int numberPresentation;
    public int numberType;
    public String phoneLabel;
    public String phoneNumber;
    public int photoResource;
    public boolean shouldSendToVoicemail;
    public long userType = 0;

    public static CallerInfo getCallerInfo(Context context, Uri contactRef, Cursor cursor) {
        CallerInfo info = new CallerInfo();
        info.photoResource = 0;
        info.phoneLabel = null;
        info.numberType = 0;
        info.numberLabel = null;
        info.cachedPhoto = null;
        info.isCachedPhotoCurrent = false;
        info.contactExists = false;
        info.userType = 0;
        if (VDBG) {
            Rlog.v(TAG, "getCallerInfo() based on cursor...");
        }
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("display_name");
                if (columnIndex != -1) {
                    info.name = cursor.getString(columnIndex);
                }
                columnIndex = cursor.getColumnIndex("number");
                if (columnIndex != -1) {
                    info.phoneNumber = cursor.getString(columnIndex);
                }
                columnIndex = cursor.getColumnIndex("normalized_number");
                if (columnIndex != -1) {
                    info.normalizedNumber = cursor.getString(columnIndex);
                }
                columnIndex = cursor.getColumnIndex("label");
                if (columnIndex != -1) {
                    int typeColumnIndex = cursor.getColumnIndex("type");
                    if (typeColumnIndex != -1) {
                        info.numberType = cursor.getInt(typeColumnIndex);
                        info.numberLabel = cursor.getString(columnIndex);
                        info.phoneLabel = Phone.getDisplayLabel(context, info.numberType, info.numberLabel).toString();
                    }
                }
                columnIndex = getColumnIndexForPersonId(contactRef, cursor);
                String str;
                StringBuilder stringBuilder;
                if (columnIndex != -1) {
                    long contactId = cursor.getLong(columnIndex);
                    if (!(contactId == 0 || Contacts.isEnterpriseContactId(contactId))) {
                        info.contactIdOrZero = contactId;
                        if (VDBG) {
                            str = TAG;
                            stringBuilder = new StringBuilder();
                            stringBuilder.append("==> got info.contactIdOrZero: ");
                            stringBuilder.append(info.contactIdOrZero);
                            Rlog.v(str, stringBuilder.toString());
                        }
                    }
                    if (Contacts.isEnterpriseContactId(contactId)) {
                        info.userType = 1;
                    }
                } else {
                    str = TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Couldn't find contact_id column for ");
                    stringBuilder.append(contactRef);
                    Rlog.w(str, stringBuilder.toString());
                }
                int columnIndex2 = cursor.getColumnIndex("lookup");
                if (columnIndex2 != -1) {
                    info.lookupKey = cursor.getString(columnIndex2);
                }
                columnIndex2 = cursor.getColumnIndex("photo_uri");
                if (columnIndex2 == -1 || cursor.getString(columnIndex2) == null) {
                    info.contactDisplayPhotoUri = null;
                } else {
                    info.contactDisplayPhotoUri = Uri.parse(cursor.getString(columnIndex2));
                }
                columnIndex2 = cursor.getColumnIndex("custom_ringtone");
                if (columnIndex2 == -1 || cursor.getString(columnIndex2) == null) {
                    info.contactRingtoneUri = null;
                } else if (TextUtils.isEmpty(cursor.getString(columnIndex2))) {
                    info.contactRingtoneUri = Uri.EMPTY;
                } else {
                    info.contactRingtoneUri = Uri.parse(cursor.getString(columnIndex2));
                }
                int columnIndex3 = cursor.getColumnIndex("send_to_voicemail");
                boolean z = columnIndex3 != -1 && cursor.getInt(columnIndex3) == 1;
                info.shouldSendToVoicemail = z;
                info.contactExists = true;
                columnIndex3 = cursor.getColumnIndex("data7");
                if (columnIndex3 != -1) {
                    info.mVoipDeviceType = cursor.getInt(columnIndex3);
                }
            }
            cursor.close();
        }
        info.needUpdate = false;
        info.name = normalize(info.name);
        info.contactRefUri = contactRef;
        return info;
    }

    public static CallerInfo getCallerInfo(Context context, Uri contactRef) {
        ContentResolver cr = CallerInfoAsyncQuery.getCurrentProfileContentResolver(context);
        if (cr == null) {
            return null;
        }
        try {
            return getCallerInfo(context, contactRef, cr.query(contactRef, null, null, null, null));
        } catch (RuntimeException re) {
            Rlog.e(TAG, "Error getting caller info.", re);
            return null;
        }
    }

    public static CallerInfo getCallerInfo(Context context, String number) {
        if (VDBG) {
            Rlog.v(TAG, "getCallerInfo() based on number...");
        }
        return getCallerInfo(context, number, SubscriptionManager.getDefaultSubscriptionId());
    }

    public static CallerInfo getCallerInfo(Context context, String number, int subId) {
        if (TextUtils.isEmpty(number)) {
            return null;
        }
        if (PhoneNumberUtils.isLocalEmergencyNumber(context, number)) {
            return new CallerInfo().markAsEmergency(context);
        }
        if (PhoneNumberUtils.isVoiceMailNumber(subId, number)) {
            return new CallerInfo().markAsVoiceMail();
        }
        CallerInfo info = doSecondaryLookupIfNecessary(context, number, getCallerInfo(context, Uri.withAppendedPath(PhoneLookup.ENTERPRISE_CONTENT_FILTER_URI, Uri.encode(number))));
        if (TextUtils.isEmpty(info.phoneNumber)) {
            info.phoneNumber = number;
        }
        return info;
    }

    static CallerInfo doSecondaryLookupIfNecessary(Context context, String number, CallerInfo previousResult) {
        if (previousResult.contactExists || !PhoneNumberUtils.isUriNumber(number)) {
            return previousResult;
        }
        String username = PhoneNumberUtils.getUsernameFromUriNumber(number);
        if (PhoneNumberUtils.isGlobalPhoneNumber(username)) {
            return getCallerInfo(context, Uri.withAppendedPath(PhoneLookup.ENTERPRISE_CONTENT_FILTER_URI, Uri.encode(username)));
        }
        return previousResult;
    }

    public boolean isEmergencyNumber() {
        return this.mIsEmergency;
    }

    public boolean isVoiceMailNumber() {
        return this.mIsVoiceMail;
    }

    CallerInfo markAsEmergency(Context context) {
        this.phoneNumber = context.getString(17039986);
        this.photoResource = 17303082;
        this.mIsEmergency = true;
        return this;
    }

    CallerInfo markAsVoiceMail() {
        return markAsVoiceMail(SubscriptionManager.getDefaultSubscriptionId());
    }

    CallerInfo markAsVoiceMail(int subId) {
        this.mIsVoiceMail = true;
        try {
            this.phoneNumber = TelephonyManager.getDefault().getVoiceMailAlphaTag(subId);
        } catch (SecurityException se) {
            Rlog.e(TAG, "Cannot access VoiceMail.", se);
        }
        return this;
    }

    private static String normalize(String s) {
        if (s == null || s.length() > 0) {
            return s;
        }
        return null;
    }

    private static int getColumnIndexForPersonId(Uri contactRef, Cursor cursor) {
        String str;
        if (VDBG) {
            str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("- getColumnIndexForPersonId: contactRef URI = '");
            stringBuilder.append(contactRef);
            stringBuilder.append("'...");
            Rlog.v(str, stringBuilder.toString());
        }
        str = contactRef.toString();
        String columnName = null;
        if (str.startsWith("content://com.android.contacts/data/phones")) {
            if (VDBG) {
                Rlog.v(TAG, "'data/phones' URI; using RawContacts.CONTACT_ID");
            }
            columnName = "contact_id";
        } else if (str.startsWith("content://com.android.contacts/data")) {
            if (VDBG) {
                Rlog.v(TAG, "'data' URI; using Data.CONTACT_ID");
            }
            columnName = "contact_id";
        } else if (str.startsWith("content://com.android.contacts/phone_lookup")) {
            if (VDBG) {
                Rlog.v(TAG, "'phone_lookup' URI; using PhoneLookup._ID");
            }
            columnName = "_id";
        } else {
            String str2 = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Unexpected prefix for contactRef '");
            stringBuilder2.append(str);
            stringBuilder2.append("'");
            Rlog.w(str2, stringBuilder2.toString());
        }
        int columnIndex = columnName != null ? cursor.getColumnIndex(columnName) : -1;
        if (VDBG) {
            String str3 = TAG;
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append("==> Using column '");
            stringBuilder3.append(columnName);
            stringBuilder3.append("' (columnIndex = ");
            stringBuilder3.append(columnIndex);
            stringBuilder3.append(") for person_id lookup...");
            Rlog.v(str3, stringBuilder3.toString());
        }
        return columnIndex;
    }

    public void updateGeoDescription(Context context, String fallbackNumber) {
        this.geoDescription = getGeoDescription(context, TextUtils.isEmpty(this.phoneNumber) ? fallbackNumber : this.phoneNumber);
    }

    public static String getGeoDescription(Context context, String number) {
        if (VDBG) {
            Rlog.v(TAG, "getGeoDescription('XXXXXX')...");
        }
        if (TextUtils.isEmpty(number)) {
            return null;
        }
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        PhoneNumberOfflineGeocoder geocoder = PhoneNumberOfflineGeocoder.getInstance();
        Locale locale = context.getResources().getConfiguration().locale;
        String countryIso = getCurrentCountryIso(context, locale);
        PhoneNumber pn = null;
        try {
            if (VDBG) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("parsing 'XXXXXX' for countryIso '");
                stringBuilder.append(countryIso);
                stringBuilder.append("'...");
                Rlog.v(str, stringBuilder.toString());
            }
            pn = util.parse(number, countryIso);
            if (VDBG) {
                Rlog.v(TAG, "- parsed number: XXXXXX");
            }
        } catch (NumberParseException e) {
            if (VDBG) {
                Rlog.w(TAG, "getGeoDescription: NumberParseException for incoming number 'XXXXXX'");
            }
        } catch (RuntimeException e2) {
            Rlog.e(TAG, "parsed number RuntimeException.");
            return "";
        }
        if (pn == null) {
            return null;
        }
        String description;
        try {
            description = geocoder.getDescriptionForNumber(pn, locale);
        } catch (NullPointerException e3) {
            if (VDBG) {
                Rlog.w(TAG, "getDescriptionForNumber NullPointerException");
            }
            description = "";
        } catch (RuntimeException e4) {
            if (VDBG) {
                Rlog.w(TAG, "getDescriptionForNumber RuntimeException");
            }
            description = "";
        }
        if (VDBG) {
            Rlog.v(TAG, "- got description: 'XXXXXX'");
        }
        return description;
    }

    private static String getCurrentCountryIso(Context context, Locale locale) {
        String countryIso = null;
        CountryDetector detector = (CountryDetector) context.getSystemService("country_detector");
        if (detector != null) {
            Country country = detector.detectCountry();
            if (country != null) {
                countryIso = country.getCountryIso();
            } else {
                Rlog.e(TAG, "CountryDetector.detectCountry() returned null.");
            }
        }
        if (countryIso != null) {
            return countryIso;
        }
        countryIso = locale.getCountry();
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("No CountryDetector; falling back to countryIso based on locale: ");
        stringBuilder.append(countryIso);
        Rlog.w(str, stringBuilder.toString());
        return countryIso;
    }

    protected static String getCurrentCountryIso(Context context) {
        return getCurrentCountryIso(context, Locale.getDefault());
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(128);
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append(super.toString());
        stringBuilder2.append(" { ");
        stringBuilder.append(stringBuilder2.toString());
        stringBuilder2 = new StringBuilder();
        stringBuilder2.append("name ");
        stringBuilder2.append(this.name == null ? "null" : "non-null");
        stringBuilder.append(stringBuilder2.toString());
        stringBuilder2 = new StringBuilder();
        stringBuilder2.append(", phoneNumber ");
        stringBuilder2.append(this.phoneNumber == null ? "null" : "non-null");
        stringBuilder.append(stringBuilder2.toString());
        stringBuilder.append(" }");
        return stringBuilder.toString();
    }
}
