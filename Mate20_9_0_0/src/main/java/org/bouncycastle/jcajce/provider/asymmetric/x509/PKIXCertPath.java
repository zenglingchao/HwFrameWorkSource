package org.bouncycastle.jcajce.provider.asymmetric.x509;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.security.NoSuchProviderException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.pkcs.ContentInfo;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.SignedData;
import org.bouncycastle.jcajce.util.BCJcaJceHelper;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

public class PKIXCertPath extends CertPath {
    static final List certPathEncodings;
    private List certificates;
    private final JcaJceHelper helper = new BCJcaJceHelper();

    static {
        ArrayList arrayList = new ArrayList();
        arrayList.add("PkiPath");
        arrayList.add("PEM");
        arrayList.add("PKCS7");
        certPathEncodings = Collections.unmodifiableList(arrayList);
    }

    PKIXCertPath(InputStream inputStream, String str) throws CertificateException {
        super("X.509");
        StringBuilder stringBuilder;
        try {
            if (!str.equalsIgnoreCase("PkiPath")) {
                if (!str.equalsIgnoreCase("PKCS7")) {
                    if (!str.equalsIgnoreCase("PEM")) {
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("unsupported encoding: ");
                        stringBuilder.append(str);
                        throw new CertificateException(stringBuilder.toString());
                    }
                }
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                this.certificates = new ArrayList();
                CertificateFactory createCertificateFactory = this.helper.createCertificateFactory("X.509");
                while (true) {
                    Certificate generateCertificate = createCertificateFactory.generateCertificate(bufferedInputStream);
                    if (generateCertificate == null) {
                        break;
                    }
                    this.certificates.add(generateCertificate);
                }
            } else {
                ASN1Primitive readObject = new ASN1InputStream(inputStream).readObject();
                if (readObject instanceof ASN1Sequence) {
                    Enumeration objects = ((ASN1Sequence) readObject).getObjects();
                    this.certificates = new ArrayList();
                    CertificateFactory createCertificateFactory2 = this.helper.createCertificateFactory("X.509");
                    while (objects.hasMoreElements()) {
                        this.certificates.add(0, createCertificateFactory2.generateCertificate(new ByteArrayInputStream(((ASN1Encodable) objects.nextElement()).toASN1Primitive().getEncoded(ASN1Encoding.DER))));
                    }
                } else {
                    throw new CertificateException("input stream does not contain a ASN1 SEQUENCE while reading PkiPath encoded data to load CertPath");
                }
            }
            this.certificates = sortCerts(this.certificates);
        } catch (IOException e) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("IOException throw while decoding CertPath:\n");
            stringBuilder.append(e.toString());
            throw new CertificateException(stringBuilder.toString());
        } catch (NoSuchProviderException e2) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("BouncyCastle provider not found while trying to get a CertificateFactory:\n");
            stringBuilder.append(e2.toString());
            throw new CertificateException(stringBuilder.toString());
        }
    }

    PKIXCertPath(List list) {
        super("X.509");
        this.certificates = sortCerts(new ArrayList(list));
    }

    private List sortCerts(List list) {
        if (list.size() < 2) {
            return list;
        }
        int i;
        X500Principal issuerX500Principal = ((X509Certificate) list.get(0)).getIssuerX500Principal();
        for (i = 1; i != list.size(); i++) {
            if (!issuerX500Principal.equals(((X509Certificate) list.get(i)).getSubjectX500Principal())) {
                i = 0;
                break;
            }
            issuerX500Principal = ((X509Certificate) list.get(i)).getIssuerX500Principal();
        }
        i = 1;
        if (i != 0) {
            return list;
        }
        ArrayList arrayList = new ArrayList(list.size());
        ArrayList arrayList2 = new ArrayList(list);
        for (int i2 = 0; i2 < list.size(); i2++) {
            int i3;
            X509Certificate x509Certificate = (X509Certificate) list.get(i2);
            X500Principal subjectX500Principal = x509Certificate.getSubjectX500Principal();
            for (int i4 = 0; i4 != list.size(); i4++) {
                if (((X509Certificate) list.get(i4)).getIssuerX500Principal().equals(subjectX500Principal)) {
                    i3 = 1;
                    break;
                }
            }
            i3 = 0;
            if (i3 == 0) {
                arrayList.add(x509Certificate);
                list.remove(i2);
            }
        }
        if (arrayList.size() > 1) {
            return arrayList2;
        }
        for (int i5 = 0; i5 != arrayList.size(); i5++) {
            X500Principal issuerX500Principal2 = ((X509Certificate) arrayList.get(i5)).getIssuerX500Principal();
            for (int i6 = 0; i6 < list.size(); i6++) {
                X509Certificate x509Certificate2 = (X509Certificate) list.get(i6);
                if (issuerX500Principal2.equals(x509Certificate2.getSubjectX500Principal())) {
                    arrayList.add(x509Certificate2);
                    list.remove(i6);
                    break;
                }
            }
        }
        return list.size() > 0 ? arrayList2 : arrayList;
    }

    private ASN1Primitive toASN1Object(X509Certificate x509Certificate) throws CertificateEncodingException {
        try {
            return new ASN1InputStream(x509Certificate.getEncoded()).readObject();
        } catch (Exception e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Exception while encoding certificate: ");
            stringBuilder.append(e.toString());
            throw new CertificateEncodingException(stringBuilder.toString());
        }
    }

    private byte[] toDEREncoded(ASN1Encodable aSN1Encodable) throws CertificateEncodingException {
        try {
            return aSN1Encodable.toASN1Primitive().getEncoded(ASN1Encoding.DER);
        } catch (IOException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Exception thrown: ");
            stringBuilder.append(e);
            throw new CertificateEncodingException(stringBuilder.toString());
        }
    }

    public List getCertificates() {
        return Collections.unmodifiableList(new ArrayList(this.certificates));
    }

    public byte[] getEncoded() throws CertificateEncodingException {
        Iterator encodings = getEncodings();
        if (encodings.hasNext()) {
            Object next = encodings.next();
            if (next instanceof String) {
                return getEncoded((String) next);
            }
        }
        return null;
    }

    public byte[] getEncoded(String str) throws CertificateEncodingException {
        ASN1EncodableVector aSN1EncodableVector;
        if (str.equalsIgnoreCase("PkiPath")) {
            aSN1EncodableVector = new ASN1EncodableVector();
            ListIterator listIterator = this.certificates.listIterator(this.certificates.size());
            while (listIterator.hasPrevious()) {
                aSN1EncodableVector.add(toASN1Object((X509Certificate) listIterator.previous()));
            }
            return toDEREncoded(new DERSequence(aSN1EncodableVector));
        }
        int i = 0;
        if (str.equalsIgnoreCase("PKCS7")) {
            ContentInfo contentInfo = new ContentInfo(PKCSObjectIdentifiers.data, null);
            aSN1EncodableVector = new ASN1EncodableVector();
            while (i != this.certificates.size()) {
                aSN1EncodableVector.add(toASN1Object((X509Certificate) this.certificates.get(i)));
                i++;
            }
            return toDEREncoded(new ContentInfo(PKCSObjectIdentifiers.signedData, new SignedData(new ASN1Integer(1), new DERSet(), contentInfo, new DERSet(aSN1EncodableVector), null, new DERSet())));
        } else if (str.equalsIgnoreCase("PEM")) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PemWriter pemWriter = new PemWriter(new OutputStreamWriter(byteArrayOutputStream));
            while (i != this.certificates.size()) {
                try {
                    pemWriter.writeObject(new PemObject("CERTIFICATE", ((X509Certificate) this.certificates.get(i)).getEncoded()));
                    i++;
                } catch (Exception e) {
                    throw new CertificateEncodingException("can't encode certificate for PEM encoded path");
                }
            }
            pemWriter.close();
            return byteArrayOutputStream.toByteArray();
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("unsupported encoding: ");
            stringBuilder.append(str);
            throw new CertificateEncodingException(stringBuilder.toString());
        }
    }

    public Iterator getEncodings() {
        return certPathEncodings.iterator();
    }
}
