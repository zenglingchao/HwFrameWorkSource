package sun.security.x509;

import java.io.IOException;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class CertificatePolicyMap {
    private CertificatePolicyId issuerDomain;
    private CertificatePolicyId subjectDomain;

    public CertificatePolicyMap(CertificatePolicyId issuer, CertificatePolicyId subject) {
        this.issuerDomain = issuer;
        this.subjectDomain = subject;
    }

    public CertificatePolicyMap(DerValue val) throws IOException {
        if (val.tag == (byte) 48) {
            this.issuerDomain = new CertificatePolicyId(val.data.getDerValue());
            this.subjectDomain = new CertificatePolicyId(val.data.getDerValue());
            return;
        }
        throw new IOException("Invalid encoding for CertificatePolicyMap");
    }

    public CertificatePolicyId getIssuerIdentifier() {
        return this.issuerDomain;
    }

    public CertificatePolicyId getSubjectIdentifier() {
        return this.subjectDomain;
    }

    public String toString() {
        String s = new StringBuilder();
        s.append("CertificatePolicyMap: [\nIssuerDomain:");
        s.append(this.issuerDomain.toString());
        s.append("SubjectDomain:");
        s.append(this.subjectDomain.toString());
        s.append("]\n");
        return s.toString();
    }

    public void encode(DerOutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        this.issuerDomain.encode(tmp);
        this.subjectDomain.encode(tmp);
        out.write((byte) 48, tmp);
    }
}
