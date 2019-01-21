package android.view.textclassifier;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.RemoteAction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.LocaleList;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.ArrayMap;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.textclassifier.TextClassifier.Utils;
import com.android.internal.util.Preconditions;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class TextClassification implements Parcelable {
    public static final Creator<TextClassification> CREATOR = new Creator<TextClassification>() {
        public TextClassification createFromParcel(Parcel in) {
            return new TextClassification(in, null);
        }

        public TextClassification[] newArray(int size) {
            return new TextClassification[size];
        }
    };
    static final TextClassification EMPTY = new Builder().build();
    private static final String LOG_TAG = "TextClassification";
    private static final int MAX_LEGACY_ICON_SIZE = 192;
    private final List<RemoteAction> mActions;
    private final EntityConfidence mEntityConfidence;
    private final String mId;
    private final Drawable mLegacyIcon;
    private final Intent mLegacyIntent;
    private final String mLegacyLabel;
    private final OnClickListener mLegacyOnClickListener;
    private final String mText;

    public static final class Builder {
        private List<RemoteAction> mActions = new ArrayList();
        private final Map<String, Float> mEntityConfidence = new ArrayMap();
        private String mId;
        private Drawable mLegacyIcon;
        private Intent mLegacyIntent;
        private String mLegacyLabel;
        private OnClickListener mLegacyOnClickListener;
        private String mText;

        public Builder setText(String text) {
            this.mText = text;
            return this;
        }

        public Builder setEntityType(String type, float confidenceScore) {
            this.mEntityConfidence.put(type, Float.valueOf(confidenceScore));
            return this;
        }

        public Builder addAction(RemoteAction action) {
            Preconditions.checkArgument(action != null);
            this.mActions.add(action);
            return this;
        }

        @Deprecated
        public Builder setIcon(Drawable icon) {
            this.mLegacyIcon = icon;
            return this;
        }

        @Deprecated
        public Builder setLabel(String label) {
            this.mLegacyLabel = label;
            return this;
        }

        @Deprecated
        public Builder setIntent(Intent intent) {
            this.mLegacyIntent = intent;
            return this;
        }

        @Deprecated
        public Builder setOnClickListener(OnClickListener onClickListener) {
            this.mLegacyOnClickListener = onClickListener;
            return this;
        }

        public Builder setId(String id) {
            this.mId = id;
            return this;
        }

        public TextClassification build() {
            return new TextClassification(this.mText, this.mLegacyIcon, this.mLegacyLabel, this.mLegacyIntent, this.mLegacyOnClickListener, this.mActions, this.mEntityConfidence, this.mId, null);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    private @interface IntentType {
        public static final int ACTIVITY = 0;
        public static final int SERVICE = 1;
        public static final int UNSUPPORTED = -1;
    }

    public static final class Options {
        private LocaleList mDefaultLocales;
        private ZonedDateTime mReferenceTime;
        private final Request mRequest;
        private final TextClassificationSessionId mSessionId;

        public Options() {
            this(null, null);
        }

        private Options(TextClassificationSessionId sessionId, Request request) {
            this.mSessionId = sessionId;
            this.mRequest = request;
        }

        public static Options from(TextClassificationSessionId sessionId, Request request) {
            Options options = new Options(sessionId, request);
            options.setDefaultLocales(request.getDefaultLocales());
            options.setReferenceTime(request.getReferenceTime());
            return options;
        }

        public Options setDefaultLocales(LocaleList defaultLocales) {
            this.mDefaultLocales = defaultLocales;
            return this;
        }

        public Options setReferenceTime(ZonedDateTime referenceTime) {
            this.mReferenceTime = referenceTime;
            return this;
        }

        public LocaleList getDefaultLocales() {
            return this.mDefaultLocales;
        }

        public ZonedDateTime getReferenceTime() {
            return this.mReferenceTime;
        }

        public Request getRequest() {
            return this.mRequest;
        }

        public TextClassificationSessionId getSessionId() {
            return this.mSessionId;
        }
    }

    public static final class Request implements Parcelable {
        public static final Creator<Request> CREATOR = new Creator<Request>() {
            public Request createFromParcel(Parcel in) {
                return new Request(in, null);
            }

            public Request[] newArray(int size) {
                return new Request[size];
            }
        };
        private final LocaleList mDefaultLocales;
        private final int mEndIndex;
        private final ZonedDateTime mReferenceTime;
        private final int mStartIndex;
        private final CharSequence mText;

        public static final class Builder {
            private LocaleList mDefaultLocales;
            private final int mEndIndex;
            private ZonedDateTime mReferenceTime;
            private final int mStartIndex;
            private final CharSequence mText;

            public Builder(CharSequence text, int startIndex, int endIndex) {
                Utils.checkArgument(text, startIndex, endIndex);
                this.mText = text;
                this.mStartIndex = startIndex;
                this.mEndIndex = endIndex;
            }

            public Builder setDefaultLocales(LocaleList defaultLocales) {
                this.mDefaultLocales = defaultLocales;
                return this;
            }

            public Builder setReferenceTime(ZonedDateTime referenceTime) {
                this.mReferenceTime = referenceTime;
                return this;
            }

            public Request build() {
                return new Request(this.mText, this.mStartIndex, this.mEndIndex, this.mDefaultLocales, this.mReferenceTime, null);
            }
        }

        /* synthetic */ Request(Parcel x0, AnonymousClass1 x1) {
            this(x0);
        }

        /* synthetic */ Request(CharSequence x0, int x1, int x2, LocaleList x3, ZonedDateTime x4, AnonymousClass1 x5) {
            this(x0, x1, x2, x3, x4);
        }

        private Request(CharSequence text, int startIndex, int endIndex, LocaleList defaultLocales, ZonedDateTime referenceTime) {
            this.mText = text;
            this.mStartIndex = startIndex;
            this.mEndIndex = endIndex;
            this.mDefaultLocales = defaultLocales;
            this.mReferenceTime = referenceTime;
        }

        public CharSequence getText() {
            return this.mText;
        }

        public int getStartIndex() {
            return this.mStartIndex;
        }

        public int getEndIndex() {
            return this.mEndIndex;
        }

        public LocaleList getDefaultLocales() {
            return this.mDefaultLocales;
        }

        public ZonedDateTime getReferenceTime() {
            return this.mReferenceTime;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mText.toString());
            dest.writeInt(this.mStartIndex);
            dest.writeInt(this.mEndIndex);
            int i = 0;
            dest.writeInt(this.mDefaultLocales != null ? 1 : 0);
            if (this.mDefaultLocales != null) {
                this.mDefaultLocales.writeToParcel(dest, flags);
            }
            if (this.mReferenceTime != null) {
                i = 1;
            }
            dest.writeInt(i);
            if (this.mReferenceTime != null) {
                dest.writeString(this.mReferenceTime.toString());
            }
        }

        private Request(Parcel in) {
            this.mText = in.readString();
            this.mStartIndex = in.readInt();
            this.mEndIndex = in.readInt();
            ZonedDateTime zonedDateTime = null;
            this.mDefaultLocales = in.readInt() == 0 ? null : (LocaleList) LocaleList.CREATOR.createFromParcel(in);
            if (in.readInt() != 0) {
                zonedDateTime = ZonedDateTime.parse(in.readString());
            }
            this.mReferenceTime = zonedDateTime;
        }
    }

    /* synthetic */ TextClassification(Parcel x0, AnonymousClass1 x1) {
        this(x0);
    }

    /* synthetic */ TextClassification(String x0, Drawable x1, String x2, Intent x3, OnClickListener x4, List x5, Map x6, String x7, AnonymousClass1 x8) {
        this(x0, x1, x2, x3, x4, x5, x6, x7);
    }

    private TextClassification(String text, Drawable legacyIcon, String legacyLabel, Intent legacyIntent, OnClickListener legacyOnClickListener, List<RemoteAction> actions, Map<String, Float> entityConfidence, String id) {
        this.mText = text;
        this.mLegacyIcon = legacyIcon;
        this.mLegacyLabel = legacyLabel;
        this.mLegacyIntent = legacyIntent;
        this.mLegacyOnClickListener = legacyOnClickListener;
        this.mActions = Collections.unmodifiableList(actions);
        this.mEntityConfidence = new EntityConfidence((Map) entityConfidence);
        this.mId = id;
    }

    public String getText() {
        return this.mText;
    }

    public int getEntityCount() {
        return this.mEntityConfidence.getEntities().size();
    }

    public String getEntity(int index) {
        return (String) this.mEntityConfidence.getEntities().get(index);
    }

    public float getConfidenceScore(String entity) {
        return this.mEntityConfidence.getConfidenceScore(entity);
    }

    public List<RemoteAction> getActions() {
        return this.mActions;
    }

    @Deprecated
    public Drawable getIcon() {
        return this.mLegacyIcon;
    }

    @Deprecated
    public CharSequence getLabel() {
        return this.mLegacyLabel;
    }

    @Deprecated
    public Intent getIntent() {
        return this.mLegacyIntent;
    }

    public OnClickListener getOnClickListener() {
        return this.mLegacyOnClickListener;
    }

    public String getId() {
        return this.mId;
    }

    public String toString() {
        return String.format(Locale.US, "TextClassification {text=%s, entities=%s, actions=%s, id=%s}", new Object[]{this.mText, this.mEntityConfidence, this.mActions, this.mId});
    }

    public static OnClickListener createIntentOnClickListener(PendingIntent intent) {
        Preconditions.checkNotNull(intent);
        return new -$$Lambda$TextClassification$ysasaE5ZkXkkzjVWIJ06GTV92-g(intent);
    }

    static /* synthetic */ void lambda$createIntentOnClickListener$0(PendingIntent intent, View v) {
        try {
            intent.send();
        } catch (CanceledException e) {
            Log.e(LOG_TAG, "Error sending PendingIntent", e);
        }
    }

    public static PendingIntent createPendingIntent(Context context, Intent intent, int requestCode) {
        switch (getIntentType(intent, context)) {
            case 0:
                return PendingIntent.getActivity(context, requestCode, intent, 134217728);
            case 1:
                return PendingIntent.getService(context, requestCode, intent, 134217728);
            default:
                return null;
        }
    }

    private static int getIntentType(Intent intent, Context context) {
        Preconditions.checkArgument(context != null);
        Preconditions.checkArgument(intent != null);
        ResolveInfo activityRI = context.getPackageManager().resolveActivity(intent, 0);
        if (activityRI != null) {
            if (context.getPackageName().equals(activityRI.activityInfo.packageName)) {
                return 0;
            }
            if (activityRI.activityInfo.exported && hasPermission(context, activityRI.activityInfo.permission)) {
                return 0;
            }
        }
        ResolveInfo serviceRI = context.getPackageManager().resolveService(intent, 0);
        if (serviceRI != null) {
            if (context.getPackageName().equals(serviceRI.serviceInfo.packageName)) {
                return 1;
            }
            if (serviceRI.serviceInfo.exported && hasPermission(context, serviceRI.serviceInfo.permission)) {
                return 1;
            }
        }
        return -1;
    }

    private static boolean hasPermission(Context context, String permission) {
        return permission == null || context.checkSelfPermission(permission) == 0;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mText);
        dest.writeTypedList(this.mActions);
        this.mEntityConfidence.writeToParcel(dest, flags);
        dest.writeString(this.mId);
    }

    private TextClassification(Parcel in) {
        this.mText = in.readString();
        this.mActions = in.createTypedArrayList(RemoteAction.CREATOR);
        if (this.mActions.isEmpty()) {
            this.mLegacyIcon = null;
            this.mLegacyLabel = null;
            this.mLegacyOnClickListener = null;
        } else {
            RemoteAction action = (RemoteAction) this.mActions.get(0);
            this.mLegacyIcon = maybeLoadDrawable(action.getIcon());
            this.mLegacyLabel = action.getTitle().toString();
            this.mLegacyOnClickListener = createIntentOnClickListener(((RemoteAction) this.mActions.get(0)).getActionIntent());
        }
        this.mLegacyIntent = null;
        this.mEntityConfidence = (EntityConfidence) EntityConfidence.CREATOR.createFromParcel(in);
        this.mId = in.readString();
    }

    private static Drawable maybeLoadDrawable(Icon icon) {
        if (icon == null) {
            return null;
        }
        int type = icon.getType();
        if (type == 1) {
            return new BitmapDrawable(Resources.getSystem(), icon.getBitmap());
        }
        if (type == 3) {
            return new BitmapDrawable(Resources.getSystem(), BitmapFactory.decodeByteArray(icon.getDataBytes(), icon.getDataOffset(), icon.getDataLength()));
        }
        if (type != 5) {
            return null;
        }
        return new AdaptiveIconDrawable(null, new BitmapDrawable(Resources.getSystem(), icon.getBitmap()));
    }
}
