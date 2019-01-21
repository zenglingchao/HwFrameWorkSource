package android.net.sip;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;
import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.address.URI;

public class SipProfile implements Parcelable, Serializable, Cloneable {
    public static final Creator<SipProfile> CREATOR = new Creator<SipProfile>() {
        public SipProfile createFromParcel(Parcel in) {
            return new SipProfile(in, null);
        }

        public SipProfile[] newArray(int size) {
            return new SipProfile[size];
        }
    };
    private static final int DEFAULT_PORT = 5060;
    private static final String TCP = "TCP";
    private static final String UDP = "UDP";
    private static final long serialVersionUID = 1;
    private Address mAddress;
    private String mAuthUserName;
    private boolean mAutoRegistration;
    private transient int mCallingUid;
    private String mDomain;
    private String mPassword;
    private int mPort;
    private String mProfileName;
    private String mProtocol;
    private String mProxyAddress;
    private boolean mSendKeepAlive;

    public static class Builder {
        private AddressFactory mAddressFactory;
        private String mDisplayName;
        private SipProfile mProfile = new SipProfile();
        private String mProxyAddress;
        private SipURI mUri;

        public Builder(SipProfile profile) {
            try {
                this.mAddressFactory = SipFactory.getInstance().createAddressFactory();
                if (profile != null) {
                    try {
                        this.mProfile = (SipProfile) profile.clone();
                        this.mProfile.mAddress = null;
                        this.mUri = profile.getUri();
                        this.mUri.setUserPassword(profile.getPassword());
                        this.mDisplayName = profile.getDisplayName();
                        this.mProxyAddress = profile.getProxyAddress();
                        this.mProfile.mPort = profile.getPort();
                        return;
                    } catch (CloneNotSupportedException e) {
                        throw new RuntimeException("should not occur", e);
                    }
                }
                throw new NullPointerException();
            } catch (PeerUnavailableException e2) {
                throw new RuntimeException(e2);
            }
        }

        public Builder(String uriString) throws ParseException {
            try {
                this.mAddressFactory = SipFactory.getInstance().createAddressFactory();
                if (uriString != null) {
                    URI uri = this.mAddressFactory.createURI(fix(uriString));
                    if (uri instanceof SipURI) {
                        this.mUri = (SipURI) uri;
                        this.mProfile.mDomain = this.mUri.getHost();
                        return;
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(uriString);
                    stringBuilder.append(" is not a SIP URI");
                    throw new ParseException(stringBuilder.toString(), 0);
                }
                throw new NullPointerException("uriString cannot be null");
            } catch (PeerUnavailableException e) {
                throw new RuntimeException(e);
            }
        }

        public Builder(String username, String serverDomain) throws ParseException {
            try {
                this.mAddressFactory = SipFactory.getInstance().createAddressFactory();
                if (username == null || serverDomain == null) {
                    throw new NullPointerException("username and serverDomain cannot be null");
                }
                this.mUri = this.mAddressFactory.createSipURI(username, serverDomain);
                this.mProfile.mDomain = serverDomain;
            } catch (PeerUnavailableException e) {
                throw new RuntimeException(e);
            }
        }

        private String fix(String uriString) {
            if (uriString.trim().toLowerCase().startsWith("sip:")) {
                return uriString;
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("sip:");
            stringBuilder.append(uriString);
            return stringBuilder.toString();
        }

        public Builder setAuthUserName(String name) {
            this.mProfile.mAuthUserName = name;
            return this;
        }

        public Builder setProfileName(String name) {
            this.mProfile.mProfileName = name;
            return this;
        }

        public Builder setPassword(String password) {
            this.mUri.setUserPassword(password);
            return this;
        }

        public Builder setPort(int port) throws IllegalArgumentException {
            if (port > 65535 || port < 1000) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("incorrect port arugment: ");
                stringBuilder.append(port);
                throw new IllegalArgumentException(stringBuilder.toString());
            }
            this.mProfile.mPort = port;
            return this;
        }

        public Builder setProtocol(String protocol) throws IllegalArgumentException {
            if (protocol != null) {
                protocol = protocol.toUpperCase();
                if (protocol.equals(SipProfile.UDP) || protocol.equals(SipProfile.TCP)) {
                    this.mProfile.mProtocol = protocol;
                    return this;
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("unsupported protocol: ");
                stringBuilder.append(protocol);
                throw new IllegalArgumentException(stringBuilder.toString());
            }
            throw new NullPointerException("protocol cannot be null");
        }

        public Builder setOutboundProxy(String outboundProxy) {
            this.mProxyAddress = outboundProxy;
            return this;
        }

        public Builder setDisplayName(String displayName) {
            this.mDisplayName = displayName;
            return this;
        }

        public Builder setSendKeepAlive(boolean flag) {
            this.mProfile.mSendKeepAlive = flag;
            return this;
        }

        public Builder setAutoRegistration(boolean flag) {
            this.mProfile.mAutoRegistration = flag;
            return this;
        }

        public SipProfile build() {
            this.mProfile.mPassword = this.mUri.getUserPassword();
            this.mUri.setUserPassword(null);
            try {
                if (TextUtils.isEmpty(this.mProxyAddress)) {
                    if (!this.mProfile.mProtocol.equals(SipProfile.UDP)) {
                        this.mUri.setTransportParam(this.mProfile.mProtocol);
                    }
                    if (this.mProfile.mPort != SipProfile.DEFAULT_PORT) {
                        this.mUri.setPort(this.mProfile.mPort);
                    }
                } else {
                    this.mProfile.mProxyAddress = ((SipURI) this.mAddressFactory.createURI(fix(this.mProxyAddress))).getHost();
                }
                this.mProfile.mAddress = this.mAddressFactory.createAddress(this.mDisplayName, this.mUri);
                return this.mProfile;
            } catch (InvalidArgumentException e) {
                throw new RuntimeException(e);
            } catch (ParseException e2) {
                throw new RuntimeException(e2);
            }
        }
    }

    /* synthetic */ SipProfile(Parcel x0, AnonymousClass1 x1) {
        this(x0);
    }

    private SipProfile() {
        this.mProtocol = UDP;
        this.mPort = DEFAULT_PORT;
        this.mSendKeepAlive = false;
        this.mAutoRegistration = true;
        this.mCallingUid = 0;
    }

    private SipProfile(Parcel in) {
        this.mProtocol = UDP;
        this.mPort = DEFAULT_PORT;
        boolean z = false;
        this.mSendKeepAlive = false;
        this.mAutoRegistration = true;
        this.mCallingUid = 0;
        this.mAddress = (Address) in.readSerializable();
        this.mProxyAddress = in.readString();
        this.mPassword = in.readString();
        this.mDomain = in.readString();
        this.mProtocol = in.readString();
        this.mProfileName = in.readString();
        this.mSendKeepAlive = in.readInt() != 0;
        if (in.readInt() != 0) {
            z = true;
        }
        this.mAutoRegistration = z;
        this.mCallingUid = in.readInt();
        this.mPort = in.readInt();
        this.mAuthUserName = in.readString();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeSerializable(this.mAddress);
        out.writeString(this.mProxyAddress);
        out.writeString(this.mPassword);
        out.writeString(this.mDomain);
        out.writeString(this.mProtocol);
        out.writeString(this.mProfileName);
        out.writeInt(this.mSendKeepAlive);
        out.writeInt(this.mAutoRegistration);
        out.writeInt(this.mCallingUid);
        out.writeInt(this.mPort);
        out.writeString(this.mAuthUserName);
    }

    public int describeContents() {
        return 0;
    }

    public SipURI getUri() {
        return (SipURI) this.mAddress.getURI();
    }

    public String getUriString() {
        if (TextUtils.isEmpty(this.mProxyAddress)) {
            return getUri().toString();
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("sip:");
        stringBuilder.append(getUserName());
        stringBuilder.append("@");
        stringBuilder.append(this.mDomain);
        return stringBuilder.toString();
    }

    public Address getSipAddress() {
        return this.mAddress;
    }

    public String getDisplayName() {
        return this.mAddress.getDisplayName();
    }

    public String getUserName() {
        return getUri().getUser();
    }

    public String getAuthUserName() {
        return this.mAuthUserName;
    }

    public String getPassword() {
        return this.mPassword;
    }

    public String getSipDomain() {
        return this.mDomain;
    }

    public int getPort() {
        return this.mPort;
    }

    public String getProtocol() {
        return this.mProtocol;
    }

    public String getProxyAddress() {
        return this.mProxyAddress;
    }

    public String getProfileName() {
        return this.mProfileName;
    }

    public boolean getSendKeepAlive() {
        return this.mSendKeepAlive;
    }

    public boolean getAutoRegistration() {
        return this.mAutoRegistration;
    }

    public void setCallingUid(int uid) {
        this.mCallingUid = uid;
    }

    public int getCallingUid() {
        return this.mCallingUid;
    }

    private Object readResolve() throws ObjectStreamException {
        if (this.mPort == 0) {
            this.mPort = DEFAULT_PORT;
        }
        return this;
    }
}
