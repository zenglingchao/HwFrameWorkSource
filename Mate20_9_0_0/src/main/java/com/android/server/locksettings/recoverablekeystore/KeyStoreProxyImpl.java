package com.android.server.locksettings.recoverablekeystore;

import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class KeyStoreProxyImpl implements KeyStoreProxy {
    private static final String ANDROID_KEY_STORE_PROVIDER = "AndroidKeyStore";
    private final KeyStore mKeyStore;

    public KeyStoreProxyImpl(KeyStore keyStore) {
        this.mKeyStore = keyStore;
    }

    public boolean containsAlias(String alias) throws KeyStoreException {
        return this.mKeyStore.containsAlias(alias);
    }

    public Key getKey(String alias, char[] password) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        return this.mKeyStore.getKey(alias, password);
    }

    public void setEntry(String alias, Entry entry, ProtectionParameter protParam) throws KeyStoreException {
        this.mKeyStore.setEntry(alias, entry, protParam);
    }

    public void deleteEntry(String alias) throws KeyStoreException {
        this.mKeyStore.deleteEntry(alias);
    }

    public static KeyStore getAndLoadAndroidKeyStore() throws KeyStoreException {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE_PROVIDER);
        try {
            keyStore.load(null);
            return keyStore;
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new KeyStoreException("Unable to load keystore.", e);
        }
    }
}
