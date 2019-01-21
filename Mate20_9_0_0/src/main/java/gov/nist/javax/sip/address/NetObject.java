package gov.nist.javax.sip.address;

import gov.nist.core.GenericObject;
import gov.nist.core.GenericObjectList;
import gov.nist.core.InternalErrorHandler;
import gov.nist.core.Separators;
import java.lang.reflect.Field;

public abstract class NetObject extends GenericObject {
    protected static final String CORE_PACKAGE = "gov.nist.core";
    protected static final String GRUU = "gr";
    protected static final String LR = "lr";
    protected static final String MADDR = "maddr";
    protected static final String METHOD = "method";
    protected static final String NET_PACKAGE = "gov.nist.javax.sip.address";
    protected static final String PARSER_PACKAGE = "gov.nist.javax.sip.parser";
    protected static final String PHONE = "phone";
    protected static final String SIP = "sip";
    protected static final String SIPS = "sips";
    protected static final String TCP = "tcp";
    protected static final String TLS = "tls";
    protected static final String TRANSPORT = "transport";
    protected static final String TTL = "ttl";
    protected static final String UDP = "udp";
    protected static final String USER = "user";
    protected static final long serialVersionUID = 6149926203633320729L;

    /* JADX WARNING: Missing block: B:72:0x012e, code skipped:
            if (r5.equals(gov.nist.javax.sip.address.NetObject.class) == false) goto L_0x0133;
     */
    /* JADX WARNING: Missing block: B:74:0x0132, code skipped:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean equals(Object that) {
        Object obj = that;
        if (!getClass().equals(that.getClass())) {
            return false;
        }
        Class<?> myclass = getClass();
        Class<?> hisclass = that.getClass();
        Class<?> myclass2 = myclass;
        while (true) {
            Field[] fields = myclass2.getDeclaredFields();
            Field[] hisfields = hisclass.getDeclaredFields();
            int i = 0;
            while (true) {
                int i2 = i;
                if (i2 >= fields.length) {
                    break;
                }
                Field f = fields[i2];
                Field g = hisfields[i2];
                if ((f.getModifiers() & 2) != 2) {
                    Class<?> fieldType = f.getType();
                    String fieldName = f.getName();
                    if (!(fieldName.compareTo("stringRepresentation") == 0 || fieldName.compareTo("indentation") == 0)) {
                        try {
                            if (fieldType.isPrimitive()) {
                                String fname = fieldType.toString();
                                if (fname.compareTo("int") == 0) {
                                    if (f.getInt(this) != g.getInt(obj)) {
                                        return false;
                                    }
                                } else if (fname.compareTo("short") == 0) {
                                    if (f.getShort(this) != g.getShort(obj)) {
                                        return false;
                                    }
                                } else if (fname.compareTo("char") == 0) {
                                    if (f.getChar(this) != g.getChar(obj)) {
                                        return false;
                                    }
                                } else if (fname.compareTo("long") == 0) {
                                    if (f.getLong(this) != g.getLong(obj)) {
                                        return false;
                                    }
                                } else if (fname.compareTo("boolean") == 0) {
                                    if (f.getBoolean(this) != g.getBoolean(obj)) {
                                        return false;
                                    }
                                } else if (fname.compareTo("double") == 0) {
                                    if (f.getDouble(this) != g.getDouble(obj)) {
                                        return false;
                                    }
                                } else if (fname.compareTo("float") == 0 && f.getFloat(this) != g.getFloat(obj)) {
                                    return false;
                                }
                            } else if (g.get(obj) != f.get(this)) {
                                if (f.get(this) == null && g.get(obj) != null) {
                                    return false;
                                }
                                if ((g.get(obj) == null && f.get(obj) != null) || !f.get(this).equals(g.get(obj))) {
                                    return false;
                                }
                            }
                        } catch (IllegalAccessException ex1) {
                            InternalErrorHandler.handleException(ex1);
                        }
                    }
                }
                i = i2 + 1;
            }
            myclass2 = myclass2.getSuperclass();
            hisclass = hisclass.getSuperclass();
        }
    }

    /* JADX WARNING: Missing block: B:120:0x01b8, code skipped:
            r1 = r3;
     */
    /* JADX WARNING: Missing block: B:121:0x01bf, code skipped:
            if (r5.equals(gov.nist.javax.sip.address.NetObject.class) == false) goto L_0x01c4;
     */
    /* JADX WARNING: Missing block: B:123:0x01c3, code skipped:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean match(Object other) {
        Exception ex1;
        boolean z;
        NetObject netObject;
        Object obj = this;
        if (other == null) {
            return true;
        }
        boolean z2 = false;
        if (!getClass().equals(other.getClass())) {
            return false;
        }
        GenericObject that = (GenericObject) other;
        Class<?> hisclass = other.getClass();
        Class<?> myclass = getClass();
        Class<?> hisclass2 = hisclass;
        while (true) {
            Field[] fields = myclass.getDeclaredFields();
            Field[] hisfields = hisclass2.getDeclaredFields();
            int i = z2;
            while (true) {
                int i2 = i;
                if (i2 >= fields.length) {
                    break;
                }
                Field f = fields[i2];
                Field g = hisfields[i2];
                if ((f.getModifiers() & 2) != 2) {
                    Class<?> fieldType = f.getType();
                    String fieldName = f.getName();
                    if (!(fieldName.compareTo("stringRepresentation") == 0 || fieldName.compareTo("indentation") == 0)) {
                        try {
                            if (fieldType.isPrimitive()) {
                                String fname = fieldType.toString();
                                if (fname.compareTo("int") == 0) {
                                    try {
                                        if (f.getInt(obj) != g.getInt(that)) {
                                            return false;
                                        }
                                    } catch (IllegalAccessException e) {
                                        ex1 = e;
                                        z = false;
                                        InternalErrorHandler.handleException(ex1);
                                        i = i2 + 1;
                                        z2 = z;
                                        netObject = this;
                                    }
                                } else if (fname.compareTo("short") == 0) {
                                    if (f.getShort(obj) != g.getShort(that)) {
                                        return false;
                                    }
                                } else if (fname.compareTo("char") == 0) {
                                    if (f.getChar(obj) != g.getChar(that)) {
                                        return false;
                                    }
                                } else if (fname.compareTo("long") == 0) {
                                    if (f.getLong(obj) != g.getLong(that)) {
                                        return false;
                                    }
                                } else if (fname.compareTo("boolean") == 0) {
                                    if (f.getBoolean(obj) != g.getBoolean(that)) {
                                        return false;
                                    }
                                } else if (fname.compareTo("double") == 0) {
                                    if (f.getDouble(obj) != g.getDouble(that)) {
                                        return false;
                                    }
                                } else if (fname.compareTo("float") == 0 && f.getFloat(obj) != g.getFloat(that)) {
                                    return false;
                                }
                            }
                            Object myObj = f.get(obj);
                            Object hisObj = g.get(that);
                            if (hisObj != null && myObj == null) {
                                return false;
                            }
                            if (hisObj != null || myObj == null) {
                                if (hisObj != null || myObj != null) {
                                    if ((hisObj instanceof String) && (myObj instanceof String)) {
                                        if (!((String) hisObj).equals("")) {
                                            if (((String) myObj).compareToIgnoreCase((String) hisObj) != 0) {
                                                return false;
                                            }
                                        }
                                    } else if (GenericObject.isMySubclass(myObj.getClass()) && GenericObject.isMySubclass(hisObj.getClass()) && myObj.getClass().equals(hisObj.getClass()) && ((GenericObject) hisObj).getMatcher() != null) {
                                        if (!((GenericObject) hisObj).getMatcher().match(((GenericObject) myObj).encode())) {
                                            return false;
                                        }
                                    } else if (GenericObject.isMySubclass(myObj.getClass()) && !((GenericObject) myObj).match(hisObj)) {
                                        return false;
                                    } else {
                                        if (GenericObjectList.isMySubclass(myObj.getClass()) && !((GenericObjectList) myObj).match(hisObj)) {
                                            return false;
                                        }
                                    }
                                }
                            }
                            z = false;
                            i = i2 + 1;
                            z2 = z;
                            netObject = this;
                            z = false;
                        } catch (IllegalAccessException e2) {
                            ex1 = e2;
                            z = z2;
                            InternalErrorHandler.handleException(ex1);
                            i = i2 + 1;
                            z2 = z;
                            netObject = this;
                        }
                        i = i2 + 1;
                        z2 = z;
                        netObject = this;
                    }
                }
                z = z2;
                i = i2 + 1;
                z2 = z;
                netObject = this;
            }
            myclass = myclass.getSuperclass();
            hisclass2 = hisclass2.getSuperclass();
            z2 = z;
            netObject = this;
        }
    }

    public String debugDump() {
        this.stringRepresentation = "";
        Class<?> myclass = getClass();
        sprint(myclass.getName());
        sprint("{");
        Field[] fields = myclass.getDeclaredFields();
        for (Field f : fields) {
            if ((f.getModifiers() & 2) != 2) {
                Class<?> fieldType = f.getType();
                String fieldName = f.getName();
                if (!(fieldName.compareTo("stringRepresentation") == 0 || fieldName.compareTo("indentation") == 0)) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(fieldName);
                    stringBuilder.append(Separators.COLON);
                    sprint(stringBuilder.toString());
                    try {
                        if (fieldType.isPrimitive()) {
                            String fname = fieldType.toString();
                            StringBuilder stringBuilder2 = new StringBuilder();
                            stringBuilder2.append(fname);
                            stringBuilder2.append(Separators.COLON);
                            sprint(stringBuilder2.toString());
                            if (fname.compareTo("int") == 0) {
                                sprint(f.getInt(this));
                            } else if (fname.compareTo("short") == 0) {
                                sprint(f.getShort(this));
                            } else if (fname.compareTo("char") == 0) {
                                sprint(f.getChar(this));
                            } else if (fname.compareTo("long") == 0) {
                                sprint(f.getLong(this));
                            } else if (fname.compareTo("boolean") == 0) {
                                sprint(f.getBoolean(this));
                            } else if (fname.compareTo("double") == 0) {
                                sprint(f.getDouble(this));
                            } else if (fname.compareTo("float") == 0) {
                                sprint(f.getFloat(this));
                            }
                        } else if (GenericObject.class.isAssignableFrom(fieldType)) {
                            if (f.get(this) != null) {
                                sprint(((GenericObject) f.get(this)).debugDump(this.indentation + 1));
                            } else {
                                sprint("<null>");
                            }
                        } else if (!GenericObjectList.class.isAssignableFrom(fieldType)) {
                            if (f.get(this) != null) {
                                stringBuilder = new StringBuilder();
                                stringBuilder.append(f.get(this).getClass().getName());
                                stringBuilder.append(Separators.COLON);
                                sprint(stringBuilder.toString());
                            } else {
                                stringBuilder = new StringBuilder();
                                stringBuilder.append(fieldType.getName());
                                stringBuilder.append(Separators.COLON);
                                sprint(stringBuilder.toString());
                            }
                            sprint("{");
                            if (f.get(this) != null) {
                                sprint(f.get(this).toString());
                            } else {
                                sprint("<null>");
                            }
                            sprint("}");
                        } else if (f.get(this) != null) {
                            sprint(((GenericObjectList) f.get(this)).debugDump(this.indentation + 1));
                        } else {
                            sprint("<null>");
                        }
                    } catch (IllegalAccessException e) {
                    }
                }
            }
        }
        sprint("}");
        return this.stringRepresentation;
    }

    public String debugDump(int indent) {
        int save = this.indentation;
        this.indentation = indent;
        String retval = debugDump();
        this.indentation = save;
        return retval;
    }

    public String toString() {
        return encode();
    }
}
