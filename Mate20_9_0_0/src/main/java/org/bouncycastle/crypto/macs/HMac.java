package org.bouncycastle.crypto.macs;

import java.util.Hashtable;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.ExtendedDigest;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.pqc.jcajce.spec.McElieceCCA2KeyGenParameterSpec;
import org.bouncycastle.util.Integers;
import org.bouncycastle.util.Memoable;

public class HMac implements Mac {
    private static final byte IPAD = (byte) 54;
    private static final byte OPAD = (byte) 92;
    private static Hashtable blockLengths = new Hashtable();
    private int blockLength;
    private Digest digest;
    private int digestSize;
    private byte[] inputPad;
    private Memoable ipadState;
    private Memoable opadState;
    private byte[] outputBuf;

    static {
        blockLengths.put("GOST3411", Integers.valueOf(32));
        blockLengths.put("MD2", Integers.valueOf(16));
        blockLengths.put("MD4", Integers.valueOf(64));
        blockLengths.put("MD5", Integers.valueOf(64));
        blockLengths.put("RIPEMD128", Integers.valueOf(64));
        blockLengths.put("RIPEMD160", Integers.valueOf(64));
        blockLengths.put(McElieceCCA2KeyGenParameterSpec.SHA1, Integers.valueOf(64));
        blockLengths.put(McElieceCCA2KeyGenParameterSpec.SHA224, Integers.valueOf(64));
        blockLengths.put(McElieceCCA2KeyGenParameterSpec.SHA256, Integers.valueOf(64));
        blockLengths.put(McElieceCCA2KeyGenParameterSpec.SHA384, Integers.valueOf(128));
        blockLengths.put(McElieceCCA2KeyGenParameterSpec.SHA512, Integers.valueOf(128));
        blockLengths.put("Tiger", Integers.valueOf(64));
        blockLengths.put("Whirlpool", Integers.valueOf(64));
    }

    public HMac(Digest digest) {
        this(digest, getByteLength(digest));
    }

    private HMac(Digest digest, int i) {
        this.digest = digest;
        this.digestSize = digest.getDigestSize();
        this.blockLength = i;
        this.inputPad = new byte[this.blockLength];
        this.outputBuf = new byte[(this.blockLength + this.digestSize)];
    }

    private static int getByteLength(Digest digest) {
        if (digest instanceof ExtendedDigest) {
            return ((ExtendedDigest) digest).getByteLength();
        }
        Integer num = (Integer) blockLengths.get(digest.getAlgorithmName());
        if (num != null) {
            return num.intValue();
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("unknown digest passed: ");
        stringBuilder.append(digest.getAlgorithmName());
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    private static void xorPad(byte[] bArr, int i, byte b) {
        for (int i2 = 0; i2 < i; i2++) {
            bArr[i2] = (byte) (bArr[i2] ^ b);
        }
    }

    public int doFinal(byte[] bArr, int i) {
        this.digest.doFinal(this.outputBuf, this.blockLength);
        if (this.opadState != null) {
            ((Memoable) this.digest).reset(this.opadState);
            this.digest.update(this.outputBuf, this.blockLength, this.digest.getDigestSize());
        } else {
            this.digest.update(this.outputBuf, 0, this.outputBuf.length);
        }
        int doFinal = this.digest.doFinal(bArr, i);
        for (i = this.blockLength; i < this.outputBuf.length; i++) {
            this.outputBuf[i] = (byte) 0;
        }
        if (this.ipadState != null) {
            ((Memoable) this.digest).reset(this.ipadState);
            return doFinal;
        }
        this.digest.update(this.inputPad, 0, this.inputPad.length);
        return doFinal;
    }

    public String getAlgorithmName() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.digest.getAlgorithmName());
        stringBuilder.append("/HMAC");
        return stringBuilder.toString();
    }

    public int getMacSize() {
        return this.digestSize;
    }

    public Digest getUnderlyingDigest() {
        return this.digest;
    }

    public void init(CipherParameters cipherParameters) {
        this.digest.reset();
        byte[] key = ((KeyParameter) cipherParameters).getKey();
        int length = key.length;
        if (length > this.blockLength) {
            this.digest.update(key, 0, length);
            this.digest.doFinal(this.inputPad, 0);
            length = this.digestSize;
        } else {
            System.arraycopy(key, 0, this.inputPad, 0, length);
        }
        while (length < this.inputPad.length) {
            this.inputPad[length] = (byte) 0;
            length++;
        }
        System.arraycopy(this.inputPad, 0, this.outputBuf, 0, this.blockLength);
        xorPad(this.inputPad, this.blockLength, IPAD);
        xorPad(this.outputBuf, this.blockLength, OPAD);
        if (this.digest instanceof Memoable) {
            this.opadState = ((Memoable) this.digest).copy();
            ((Digest) this.opadState).update(this.outputBuf, 0, this.blockLength);
        }
        this.digest.update(this.inputPad, 0, this.inputPad.length);
        if (this.digest instanceof Memoable) {
            this.ipadState = ((Memoable) this.digest).copy();
        }
    }

    public void reset() {
        this.digest.reset();
        this.digest.update(this.inputPad, 0, this.inputPad.length);
    }

    public void update(byte b) {
        this.digest.update(b);
    }

    public void update(byte[] bArr, int i, int i2) {
        this.digest.update(bArr, i, i2);
    }
}
