package android.speech.tts;

import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.text.TextUtils;
import java.util.List;

public interface ITextToSpeechService extends IInterface {

    public static abstract class Stub extends Binder implements ITextToSpeechService {
        private static final String DESCRIPTOR = "android.speech.tts.ITextToSpeechService";
        static final int TRANSACTION_getClientDefaultLanguage = 8;
        static final int TRANSACTION_getDefaultVoiceNameFor = 15;
        static final int TRANSACTION_getFeaturesForLanguage = 10;
        static final int TRANSACTION_getLanguage = 7;
        static final int TRANSACTION_getVoices = 13;
        static final int TRANSACTION_isLanguageAvailable = 9;
        static final int TRANSACTION_isSpeaking = 5;
        static final int TRANSACTION_loadLanguage = 11;
        static final int TRANSACTION_loadVoice = 14;
        static final int TRANSACTION_playAudio = 3;
        static final int TRANSACTION_playSilence = 4;
        static final int TRANSACTION_setCallback = 12;
        static final int TRANSACTION_speak = 1;
        static final int TRANSACTION_stop = 6;
        static final int TRANSACTION_synthesizeToFileDescriptor = 2;

        private static class Proxy implements ITextToSpeechService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public int speak(IBinder callingInstance, CharSequence text, int queueMode, Bundle params, String utteranceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callingInstance);
                    if (text != null) {
                        _data.writeInt(1);
                        TextUtils.writeToParcel(text, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(queueMode);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(utteranceId);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int synthesizeToFileDescriptor(IBinder callingInstance, CharSequence text, ParcelFileDescriptor fileDescriptor, Bundle params, String utteranceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callingInstance);
                    if (text != null) {
                        _data.writeInt(1);
                        TextUtils.writeToParcel(text, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (fileDescriptor != null) {
                        _data.writeInt(1);
                        fileDescriptor.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(utteranceId);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int playAudio(IBinder callingInstance, Uri audioUri, int queueMode, Bundle params, String utteranceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callingInstance);
                    if (audioUri != null) {
                        _data.writeInt(1);
                        audioUri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(queueMode);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(utteranceId);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int playSilence(IBinder callingInstance, long duration, int queueMode, String utteranceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callingInstance);
                    _data.writeLong(duration);
                    _data.writeInt(queueMode);
                    _data.writeString(utteranceId);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isSpeaking() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean z = false;
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        z = true;
                    }
                    boolean _result = z;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int stop(IBinder callingInstance) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callingInstance);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getLanguage() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getClientDefaultLanguage() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int isLanguageAvailable(String lang, String country, String variant) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(lang);
                    _data.writeString(country);
                    _data.writeString(variant);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getFeaturesForLanguage(String lang, String country, String variant) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(lang);
                    _data.writeString(country);
                    _data.writeString(variant);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int loadLanguage(IBinder caller, String lang, String country, String variant) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller);
                    _data.writeString(lang);
                    _data.writeString(country);
                    _data.writeString(variant);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setCallback(IBinder caller, ITextToSpeechCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<Voice> getVoices() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    List<Voice> _result = _reply.createTypedArrayList(Voice.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int loadVoice(IBinder caller, String voiceName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller);
                    _data.writeString(voiceName);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getDefaultVoiceNameFor(String lang, String country, String variant) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(lang);
                    _data.writeString(country);
                    _data.writeString(variant);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITextToSpeechService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITextToSpeechService)) {
                return new Proxy(obj);
            }
            return (ITextToSpeechService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            String descriptor = DESCRIPTOR;
            if (i != 1598968902) {
                Bundle bundle = null;
                IBinder _arg0;
                CharSequence _arg1;
                int _arg2;
                int _result;
                String[] _result2;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(descriptor);
                        _arg0 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg1 = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
                        } else {
                            _arg1 = null;
                        }
                        _arg2 = data.readInt();
                        if (data.readInt() != 0) {
                            bundle = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        _result = speak(_arg0, _arg1, _arg2, bundle, data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 2:
                        ParcelFileDescriptor _arg22;
                        parcel.enforceInterface(descriptor);
                        _arg0 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg1 = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg22 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg22 = null;
                        }
                        if (data.readInt() != 0) {
                            bundle = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        _result = synthesizeToFileDescriptor(_arg0, _arg1, _arg22, bundle, data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 3:
                        Uri _arg12;
                        parcel.enforceInterface(descriptor);
                        _arg0 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg12 = (Uri) Uri.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg12 = null;
                        }
                        _arg2 = data.readInt();
                        if (data.readInt() != 0) {
                            bundle = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        _result = playAudio(_arg0, _arg12, _arg2, bundle, data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 4:
                        parcel.enforceInterface(descriptor);
                        _result = playSilence(data.readStrongBinder(), data.readLong(), data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 5:
                        parcel.enforceInterface(descriptor);
                        boolean _result3 = isSpeaking();
                        reply.writeNoException();
                        parcel2.writeInt(_result3);
                        return true;
                    case 6:
                        parcel.enforceInterface(descriptor);
                        int _result4 = stop(data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeInt(_result4);
                        return true;
                    case 7:
                        parcel.enforceInterface(descriptor);
                        _result2 = getLanguage();
                        reply.writeNoException();
                        parcel2.writeStringArray(_result2);
                        return true;
                    case 8:
                        parcel.enforceInterface(descriptor);
                        _result2 = getClientDefaultLanguage();
                        reply.writeNoException();
                        parcel2.writeStringArray(_result2);
                        return true;
                    case 9:
                        parcel.enforceInterface(descriptor);
                        int _result5 = isLanguageAvailable(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result5);
                        return true;
                    case 10:
                        parcel.enforceInterface(descriptor);
                        String[] _result6 = getFeaturesForLanguage(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeStringArray(_result6);
                        return true;
                    case 11:
                        parcel.enforceInterface(descriptor);
                        int _result7 = loadLanguage(data.readStrongBinder(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result7);
                        return true;
                    case 12:
                        parcel.enforceInterface(descriptor);
                        setCallback(data.readStrongBinder(), android.speech.tts.ITextToSpeechCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 13:
                        parcel.enforceInterface(descriptor);
                        List<Voice> _result8 = getVoices();
                        reply.writeNoException();
                        parcel2.writeTypedList(_result8);
                        return true;
                    case 14:
                        parcel.enforceInterface(descriptor);
                        int _result9 = loadVoice(data.readStrongBinder(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result9);
                        return true;
                    case 15:
                        parcel.enforceInterface(descriptor);
                        String _result10 = getDefaultVoiceNameFor(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeString(_result10);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            }
            parcel2.writeString(descriptor);
            return true;
        }
    }

    String[] getClientDefaultLanguage() throws RemoteException;

    String getDefaultVoiceNameFor(String str, String str2, String str3) throws RemoteException;

    String[] getFeaturesForLanguage(String str, String str2, String str3) throws RemoteException;

    String[] getLanguage() throws RemoteException;

    List<Voice> getVoices() throws RemoteException;

    int isLanguageAvailable(String str, String str2, String str3) throws RemoteException;

    boolean isSpeaking() throws RemoteException;

    int loadLanguage(IBinder iBinder, String str, String str2, String str3) throws RemoteException;

    int loadVoice(IBinder iBinder, String str) throws RemoteException;

    int playAudio(IBinder iBinder, Uri uri, int i, Bundle bundle, String str) throws RemoteException;

    int playSilence(IBinder iBinder, long j, int i, String str) throws RemoteException;

    void setCallback(IBinder iBinder, ITextToSpeechCallback iTextToSpeechCallback) throws RemoteException;

    int speak(IBinder iBinder, CharSequence charSequence, int i, Bundle bundle, String str) throws RemoteException;

    int stop(IBinder iBinder) throws RemoteException;

    int synthesizeToFileDescriptor(IBinder iBinder, CharSequence charSequence, ParcelFileDescriptor parcelFileDescriptor, Bundle bundle, String str) throws RemoteException;
}
