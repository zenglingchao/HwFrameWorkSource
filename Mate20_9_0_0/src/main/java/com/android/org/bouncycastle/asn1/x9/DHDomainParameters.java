package com.android.org.bouncycastle.asn1.x9;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import com.android.org.bouncycastle.asn1.DERSequence;
import java.math.BigInteger;
import java.util.Enumeration;

public class DHDomainParameters extends ASN1Object {
    private ASN1Integer g;
    private ASN1Integer j;
    private ASN1Integer p;
    private ASN1Integer q;
    private DHValidationParms validationParms;

    public static DHDomainParameters getInstance(ASN1TaggedObject obj, boolean explicit) {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    public static DHDomainParameters getInstance(Object obj) {
        if (obj == null || (obj instanceof DHDomainParameters)) {
            return (DHDomainParameters) obj;
        }
        if (obj instanceof ASN1Sequence) {
            return new DHDomainParameters((ASN1Sequence) obj);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Invalid DHDomainParameters: ");
        stringBuilder.append(obj.getClass().getName());
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public DHDomainParameters(BigInteger p, BigInteger g, BigInteger q, BigInteger j, DHValidationParms validationParms) {
        if (p == null) {
            throw new IllegalArgumentException("'p' cannot be null");
        } else if (g == null) {
            throw new IllegalArgumentException("'g' cannot be null");
        } else if (q != null) {
            this.p = new ASN1Integer(p);
            this.g = new ASN1Integer(g);
            this.q = new ASN1Integer(q);
            this.j = new ASN1Integer(j);
            this.validationParms = validationParms;
        } else {
            throw new IllegalArgumentException("'q' cannot be null");
        }
    }

    public DHDomainParameters(ASN1Integer p, ASN1Integer g, ASN1Integer q, ASN1Integer j, DHValidationParms validationParms) {
        if (p == null) {
            throw new IllegalArgumentException("'p' cannot be null");
        } else if (g == null) {
            throw new IllegalArgumentException("'g' cannot be null");
        } else if (q != null) {
            this.p = p;
            this.g = g;
            this.q = q;
            this.j = j;
            this.validationParms = validationParms;
        } else {
            throw new IllegalArgumentException("'q' cannot be null");
        }
    }

    private DHDomainParameters(ASN1Sequence seq) {
        if (seq.size() < 3 || seq.size() > 5) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Bad sequence size: ");
            stringBuilder.append(seq.size());
            throw new IllegalArgumentException(stringBuilder.toString());
        }
        Enumeration e = seq.getObjects();
        this.p = ASN1Integer.getInstance(e.nextElement());
        this.g = ASN1Integer.getInstance(e.nextElement());
        this.q = ASN1Integer.getInstance(e.nextElement());
        ASN1Encodable next = getNext(e);
        if (next != null && (next instanceof ASN1Integer)) {
            this.j = ASN1Integer.getInstance(next);
            next = getNext(e);
        }
        if (next != null) {
            this.validationParms = DHValidationParms.getInstance(next.toASN1Primitive());
        }
    }

    private static ASN1Encodable getNext(Enumeration e) {
        return e.hasMoreElements() ? (ASN1Encodable) e.nextElement() : null;
    }

    public ASN1Integer getP() {
        return this.p;
    }

    public ASN1Integer getG() {
        return this.g;
    }

    public ASN1Integer getQ() {
        return this.q;
    }

    public ASN1Integer getJ() {
        return this.j;
    }

    public DHValidationParms getValidationParms() {
        return this.validationParms;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(this.p);
        v.add(this.g);
        v.add(this.q);
        if (this.j != null) {
            v.add(this.j);
        }
        if (this.validationParms != null) {
            v.add(this.validationParms);
        }
        return new DERSequence(v);
    }
}
