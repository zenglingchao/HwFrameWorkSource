package org.bouncycastle.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BERApplicationSpecific extends ASN1ApplicationSpecific {
    public BERApplicationSpecific(int i, ASN1Encodable aSN1Encodable) throws IOException {
        this(true, i, aSN1Encodable);
    }

    public BERApplicationSpecific(int i, ASN1EncodableVector aSN1EncodableVector) {
        super(true, i, getEncodedVector(aSN1EncodableVector));
    }

    public BERApplicationSpecific(boolean z, int i, ASN1Encodable aSN1Encodable) throws IOException {
        boolean z2 = z || aSN1Encodable.toASN1Primitive().isConstructed();
        super(z2, i, getEncoding(z, aSN1Encodable));
    }

    BERApplicationSpecific(boolean z, int i, byte[] bArr) {
        super(z, i, bArr);
    }

    private static byte[] getEncodedVector(ASN1EncodableVector aSN1EncodableVector) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i = 0;
        while (i != aSN1EncodableVector.size()) {
            try {
                byteArrayOutputStream.write(((ASN1Object) aSN1EncodableVector.get(i)).getEncoded(ASN1Encoding.BER));
                i++;
            } catch (IOException e) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("malformed object: ");
                stringBuilder.append(e);
                throw new ASN1ParsingException(stringBuilder.toString(), e);
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    private static byte[] getEncoding(boolean z, ASN1Encodable aSN1Encodable) throws IOException {
        byte[] encoded = aSN1Encodable.toASN1Primitive().getEncoded(ASN1Encoding.BER);
        if (z) {
            return encoded;
        }
        int lengthOfHeader = ASN1ApplicationSpecific.getLengthOfHeader(encoded);
        byte[] bArr = new byte[(encoded.length - lengthOfHeader)];
        System.arraycopy(encoded, lengthOfHeader, bArr, 0, bArr.length);
        return bArr;
    }

    void encode(ASN1OutputStream aSN1OutputStream) throws IOException {
        aSN1OutputStream.writeTag(this.isConstructed ? 96 : 64, this.tag);
        aSN1OutputStream.write(128);
        aSN1OutputStream.write(this.octets);
        aSN1OutputStream.write(0);
        aSN1OutputStream.write(0);
    }
}
