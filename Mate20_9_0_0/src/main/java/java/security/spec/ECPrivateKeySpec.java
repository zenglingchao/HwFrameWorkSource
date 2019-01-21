package java.security.spec;

import java.math.BigInteger;

public class ECPrivateKeySpec implements KeySpec {
    private ECParameterSpec params;
    private BigInteger s;

    public ECPrivateKeySpec(BigInteger s, ECParameterSpec params) {
        if (s == null) {
            throw new NullPointerException("s is null");
        } else if (params != null) {
            this.s = s;
            this.params = params;
        } else {
            throw new NullPointerException("params is null");
        }
    }

    public BigInteger getS() {
        return this.s;
    }

    public ECParameterSpec getParams() {
        return this.params;
    }
}
