package org.apache.xml.serializer.dom3;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import org.apache.xalan.templates.Constants;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xml.serializer.utils.MsgKey;
import org.apache.xml.serializer.utils.Utils;
import org.apache.xml.serializer.utils.XML11Char;
import org.apache.xml.serializer.utils.XMLChar;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.ls.LSSerializerFilter;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.LocatorImpl;

final class DOM3TreeWalker {
    private static final int CANONICAL = 1;
    private static final int CDATA = 2;
    private static final int CHARNORMALIZE = 4;
    private static final int COMMENTS = 8;
    private static final int DISCARDDEFAULT = 32768;
    private static final int DTNORMALIZE = 16;
    private static final int ELEM_CONTENT_WHITESPACE = 32;
    private static final int ENTITIES = 64;
    private static final int IGNORE_CHAR_DENORMALIZE = 131072;
    private static final int INFOSET = 128;
    private static final int NAMESPACEDECLS = 512;
    private static final int NAMESPACES = 256;
    private static final int NORMALIZECHARS = 1024;
    private static final int PRETTY_PRINT = 65536;
    private static final int SCHEMAVALIDATE = 8192;
    private static final int SPLITCDATA = 2048;
    private static final int VALIDATE = 4096;
    private static final int WELLFORMED = 16384;
    private static final int XMLDECL = 262144;
    private static final String XMLNS_PREFIX = "xmlns";
    private static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";
    private static final String XML_PREFIX = "xml";
    private static final String XML_URI = "http://www.w3.org/XML/1998/namespace";
    private static final Hashtable s_propKeys = new Hashtable();
    private Properties fDOMConfigProperties = null;
    private int fElementDepth = 0;
    private DOMErrorHandler fErrorHandler = null;
    private int fFeatures = 0;
    private LSSerializerFilter fFilter = null;
    private boolean fInEntityRef = false;
    private boolean fIsLevel3DOM = false;
    private boolean fIsXMLVersion11 = false;
    private LexicalHandler fLexicalHandler = null;
    protected NamespaceSupport fLocalNSBinder;
    private LocatorImpl fLocator = new LocatorImpl();
    protected NamespaceSupport fNSBinder;
    private String fNewLine = null;
    boolean fNextIsRaw = false;
    private SerializationHandler fSerializer = null;
    private int fWhatToShowFilter;
    private String fXMLVersion = null;

    DOM3TreeWalker(SerializationHandler serialHandler, DOMErrorHandler errHandler, LSSerializerFilter filter, String newLine) {
        this.fSerializer = serialHandler;
        this.fErrorHandler = errHandler;
        this.fFilter = filter;
        this.fLexicalHandler = null;
        this.fNewLine = newLine;
        this.fNSBinder = new NamespaceSupport();
        this.fLocalNSBinder = new NamespaceSupport();
        this.fDOMConfigProperties = this.fSerializer.getOutputFormat();
        this.fSerializer.setDocumentLocator(this.fLocator);
        initProperties(this.fDOMConfigProperties);
        try {
            LocatorImpl locatorImpl = this.fLocator;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(System.getProperty("user.dir"));
            stringBuilder.append(File.separator);
            stringBuilder.append("dummy.xsl");
            locatorImpl.setSystemId(stringBuilder.toString());
        } catch (SecurityException e) {
        }
    }

    public void traverse(Node top) throws SAXException {
        this.fSerializer.startDocument();
        if (top.getNodeType() != (short) 9) {
            Document ownerDoc = top.getOwnerDocument();
            if (ownerDoc != null && ownerDoc.getImplementation().hasFeature("Core", "3.0")) {
                this.fIsLevel3DOM = true;
            }
        } else if (((Document) top).getImplementation().hasFeature("Core", "3.0")) {
            this.fIsLevel3DOM = true;
        }
        if (this.fSerializer instanceof LexicalHandler) {
            this.fLexicalHandler = this.fSerializer;
        }
        if (this.fFilter != null) {
            this.fWhatToShowFilter = this.fFilter.getWhatToShow();
        }
        Node pos = top;
        while (pos != null) {
            startNode(pos);
            Node nextNode = pos.getFirstChild();
            while (nextNode == null) {
                endNode(pos);
                if (top.equals(pos)) {
                    break;
                }
                nextNode = pos.getNextSibling();
                if (nextNode == null) {
                    pos = pos.getParentNode();
                    if (pos == null || top.equals(pos)) {
                        if (pos != null) {
                            endNode(pos);
                        }
                        nextNode = null;
                    }
                }
            }
            pos = nextNode;
        }
        this.fSerializer.endDocument();
    }

    public void traverse(Node pos, Node top) throws SAXException {
        this.fSerializer.startDocument();
        if (pos.getNodeType() != (short) 9) {
            Document ownerDoc = pos.getOwnerDocument();
            if (ownerDoc != null && ownerDoc.getImplementation().hasFeature("Core", "3.0")) {
                this.fIsLevel3DOM = true;
            }
        } else if (((Document) pos).getImplementation().hasFeature("Core", "3.0")) {
            this.fIsLevel3DOM = true;
        }
        if (this.fSerializer instanceof LexicalHandler) {
            this.fLexicalHandler = this.fSerializer;
        }
        if (this.fFilter != null) {
            this.fWhatToShowFilter = this.fFilter.getWhatToShow();
        }
        while (pos != null) {
            startNode(pos);
            Node nextNode = pos.getFirstChild();
            while (nextNode == null) {
                endNode(pos);
                if (top != null && top.equals(pos)) {
                    break;
                }
                nextNode = pos.getNextSibling();
                if (nextNode == null) {
                    pos = pos.getParentNode();
                    if (pos == null || (top != null && top.equals(pos))) {
                        nextNode = null;
                        break;
                    }
                }
            }
            pos = nextNode;
        }
        this.fSerializer.endDocument();
    }

    private final void dispatachChars(Node node) throws SAXException {
        if (this.fSerializer != null) {
            this.fSerializer.characters(node);
            return;
        }
        String data = ((Text) node).getData();
        this.fSerializer.characters(data.toCharArray(), 0, data.length());
    }

    protected void startNode(Node node) throws SAXException {
        if (node instanceof Locator) {
            Locator loc = (Locator) node;
            this.fLocator.setColumnNumber(loc.getColumnNumber());
            this.fLocator.setLineNumber(loc.getLineNumber());
            this.fLocator.setPublicId(loc.getPublicId());
            this.fLocator.setSystemId(loc.getSystemId());
        } else {
            this.fLocator.setColumnNumber(0);
            this.fLocator.setLineNumber(0);
        }
        switch (node.getNodeType()) {
            case (short) 1:
                serializeElement((Element) node, true);
                return;
            case (short) 3:
                serializeText((Text) node);
                return;
            case (short) 4:
                serializeCDATASection((CDATASection) node);
                return;
            case (short) 5:
                serializeEntityReference((EntityReference) node, true);
                return;
            case (short) 7:
                serializePI((ProcessingInstruction) node);
                return;
            case (short) 8:
                serializeComment((Comment) node);
                return;
            case (short) 10:
                serializeDocType((DocumentType) node, true);
                return;
            default:
                return;
        }
    }

    protected void endNode(Node node) throws SAXException {
        switch (node.getNodeType()) {
            case (short) 1:
                serializeElement((Element) node, false);
                return;
            case (short) 5:
                serializeEntityReference((EntityReference) node, false);
                return;
            case (short) 10:
                serializeDocType((DocumentType) node, false);
                return;
            default:
                return;
        }
    }

    protected boolean applyFilter(Node node, int nodeType) {
        if (!(this.fFilter == null || (this.fWhatToShowFilter & nodeType) == 0)) {
            switch (this.fFilter.acceptNode(node)) {
                case (short) 2:
                case (short) 3:
                    return false;
            }
        }
        return true;
    }

    protected void serializeDocType(DocumentType node, boolean bStart) throws SAXException {
        String docTypeName = node.getNodeName();
        String publicId = node.getPublicId();
        String systemId = node.getSystemId();
        String internalSubset = node.getInternalSubset();
        if (internalSubset == null || "".equals(internalSubset)) {
            if (bStart) {
                if (this.fLexicalHandler != null) {
                    this.fLexicalHandler.startDTD(docTypeName, publicId, systemId);
                }
            } else if (this.fLexicalHandler != null) {
                this.fLexicalHandler.endDTD();
            }
        } else if (bStart) {
            try {
                Writer writer = this.fSerializer.getWriter();
                StringBuffer dtd = new StringBuffer();
                dtd.append("<!DOCTYPE ");
                dtd.append(docTypeName);
                if (publicId != null) {
                    dtd.append(" PUBLIC \"");
                    dtd.append(publicId);
                    dtd.append('\"');
                }
                if (systemId != null) {
                    if (publicId == null) {
                        dtd.append(" SYSTEM \"");
                    } else {
                        dtd.append(" \"");
                    }
                    dtd.append(systemId);
                    dtd.append('\"');
                }
                dtd.append(" [ ");
                dtd.append(this.fNewLine);
                dtd.append(internalSubset);
                dtd.append("]>");
                dtd.append(new String(this.fNewLine));
                writer.write(dtd.toString());
                writer.flush();
            } catch (IOException e) {
                throw new SAXException(Utils.messages.createMessage(MsgKey.ER_WRITING_INTERNAL_SUBSET, null), e);
            }
        }
    }

    protected void serializeComment(Comment node) throws SAXException {
        if ((this.fFeatures & 8) != 0) {
            String data = node.getData();
            if ((this.fFeatures & 16384) != 0) {
                isCommentWellFormed(data);
            }
            if (this.fLexicalHandler != null && applyFilter(node, 128)) {
                this.fLexicalHandler.comment(data.toCharArray(), 0, data.length());
            }
        }
    }

    protected void serializeElement(Element node, boolean bStart) throws SAXException {
        if (bStart) {
            this.fElementDepth++;
            if ((this.fFeatures & 16384) != 0) {
                isElementWellFormed(node);
            }
            if (applyFilter(node, 1)) {
                if ((this.fFeatures & 256) != 0) {
                    this.fNSBinder.pushContext();
                    this.fLocalNSBinder.reset();
                    recordLocalNSDecl(node);
                    fixupElementNS(node);
                }
                this.fSerializer.startElement(node.getNamespaceURI(), node.getLocalName(), node.getNodeName());
                serializeAttList(node);
            } else {
                return;
            }
        }
        this.fElementDepth--;
        if (applyFilter(node, 1)) {
            this.fSerializer.endElement(node.getNamespaceURI(), node.getLocalName(), node.getNodeName());
            if ((this.fFeatures & 256) != 0) {
                this.fNSBinder.popContext();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:90:0x0217  */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x027e  */
    /* JADX WARNING: Removed duplicated region for block: B:114:0x027b  */
    /* JADX WARNING: Removed duplicated region for block: B:126:0x0291 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x028c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void serializeAttList(Element node) throws SAXException {
        int nAttrs;
        NamedNodeMap atts = node.getAttributes();
        int nAttrs2 = atts.getLength();
        int i = 0;
        while (i < nAttrs2) {
            NamedNodeMap atts2;
            boolean addAttr;
            int indexOf;
            int index;
            String prefix;
            Node attr = atts.item(i);
            String localName = attr.getLocalName();
            String attrName = attr.getNodeName();
            String attrPrefix = attr.getPrefix() == null ? "" : attr.getPrefix();
            String attrValue = attr.getNodeValue();
            String type = null;
            if (this.fIsLevel3DOM) {
                type = ((Attr) attr).getSchemaTypeInfo().getTypeName();
            }
            String type2 = type == null ? "CDATA" : type;
            type = attr.getNamespaceURI();
            if (type != null && type.length() == 0) {
                type = null;
                attrName = attr.getLocalName();
            }
            String attrNS = type;
            boolean isSpecified = ((Attr) attr).getSpecified();
            boolean addAttr2 = true;
            boolean applyFilter = false;
            boolean z = attrName.equals("xmlns") || attrName.startsWith(Constants.ATTRNAME_XMLNS);
            boolean xmlnsAttr = z;
            if ((this.fFeatures & 16384) != 0) {
                isAttributeWellFormed(attr);
            }
            if ((this.fFeatures & 256) == 0 || xmlnsAttr) {
                atts2 = atts;
                nAttrs = nAttrs2;
            } else if (attrNS != null) {
                attrPrefix = attrPrefix == null ? "" : attrPrefix;
                String declAttrPrefix = this.fNSBinder.getPrefix(attrNS);
                String declAttrNS = this.fNSBinder.getURI(attrPrefix);
                StringBuilder stringBuilder;
                if (!"".equals(attrPrefix) && !"".equals(declAttrPrefix) && attrPrefix.equals(declAttrPrefix)) {
                    atts2 = atts;
                    nAttrs = nAttrs2;
                } else if (declAttrPrefix == null || "".equals(declAttrPrefix)) {
                    atts2 = atts;
                    if (attrPrefix == null || "".equals(attrPrefix) != null || declAttrNS != null) {
                        nAttrs = nAttrs2;
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append("NS");
                        int counter = 1 + 1;
                        stringBuilder2.append(1);
                        atts = stringBuilder2.toString();
                        while (this.fLocalNSBinder.getURI(atts) != null) {
                            stringBuilder2 = new StringBuilder();
                            stringBuilder2.append("NS");
                            int counter2 = counter + 1;
                            stringBuilder2.append(counter);
                            atts = stringBuilder2.toString();
                            counter = counter2;
                        }
                        stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(atts);
                        stringBuilder2.append(":");
                        stringBuilder2.append(localName);
                        attrName = stringBuilder2.toString();
                        if ((this.fFeatures & 512) != 0) {
                            SerializationHandler serializationHandler = this.fSerializer;
                            stringBuilder = new StringBuilder();
                            stringBuilder.append(Constants.ATTRNAME_XMLNS);
                            stringBuilder.append(atts);
                            serializationHandler.addAttribute("http://www.w3.org/2000/xmlns/", atts, stringBuilder.toString(), "CDATA", attrNS);
                            this.fNSBinder.declarePrefix(atts, attrNS);
                            this.fLocalNSBinder.declarePrefix(atts, attrNS);
                        }
                        attrPrefix = atts;
                    } else if ((this.fFeatures & 512) != null) {
                        atts = this.fSerializer;
                        StringBuilder stringBuilder3 = new StringBuilder();
                        nAttrs = nAttrs2;
                        stringBuilder3.append(Constants.ATTRNAME_XMLNS);
                        stringBuilder3.append(attrPrefix);
                        atts.addAttribute("http://www.w3.org/2000/xmlns/", attrPrefix, stringBuilder3.toString(), "CDATA", attrNS);
                        this.fNSBinder.declarePrefix(attrPrefix, attrNS);
                        this.fLocalNSBinder.declarePrefix(attrPrefix, attrNS);
                    } else {
                        nAttrs = nAttrs2;
                    }
                } else {
                    String str = declAttrPrefix;
                    if (declAttrPrefix.length() > 0) {
                        stringBuilder = new StringBuilder();
                        stringBuilder.append(declAttrPrefix);
                        atts2 = atts;
                        stringBuilder.append(":");
                        stringBuilder.append(localName);
                        attrName = stringBuilder.toString();
                    } else {
                        atts2 = atts;
                        attrName = localName;
                    }
                    nAttrs = nAttrs2;
                    attrPrefix = str;
                }
                atts = attrName;
                String str2 = attrPrefix;
                if (((this.fFeatures & 32768) == 0 && isSpecified) || (this.fFeatures & 32768) == 0) {
                    applyFilter = true;
                } else {
                    addAttr2 = false;
                }
                if (!(!applyFilter || this.fFilter == null || (this.fFilter.getWhatToShow() & 2) == 0 || xmlnsAttr)) {
                    switch (this.fFilter.acceptNode(attr)) {
                        case (short) 2:
                        case (short) 3:
                            addAttr2 = false;
                            break;
                    }
                }
                addAttr = addAttr2;
                if (!addAttr && xmlnsAttr) {
                    if (!((this.fFeatures & 512) == 0 || localName == null || "".equals(localName))) {
                        this.fSerializer.addAttribute(attrNS, localName, atts, type2, attrValue);
                    }
                    attrName = attrValue;
                } else if (addAttr || xmlnsAttr) {
                    attrName = attrValue;
                    attrPrefix = localName;
                } else if ((this.fFeatures & 512) == 0 || attrNS == null) {
                    attrName = attrValue;
                    this.fSerializer.addAttribute("", localName, atts, type2, attrName);
                } else {
                    type = attrNS;
                    attrName = attrValue;
                    attrPrefix = localName;
                    this.fSerializer.addAttribute(attrNS, localName, atts, type2, attrName);
                }
                if (xmlnsAttr && (this.fFeatures & 512) != 0) {
                    indexOf = atts.indexOf(":");
                    index = indexOf;
                    if (indexOf >= 0) {
                        prefix = "";
                    } else {
                        prefix = atts.substring(index + 1);
                    }
                    if ("".equals(prefix)) {
                        this.fSerializer.namespaceAfterStartElement(prefix, attrName);
                    }
                }
                i++;
                atts = atts2;
                nAttrs2 = nAttrs;
            } else {
                atts2 = atts;
                nAttrs = nAttrs2;
                if (localName == null) {
                    atts = Utils.messages.createMessage(MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, new Object[]{attrName});
                    if (this.fErrorHandler != null) {
                        this.fErrorHandler.handleError(new DOMErrorImpl((short) 2, atts, MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, null, null, null));
                    }
                }
            }
            atts = attrName;
            if ((this.fFeatures & 32768) == 0) {
            }
            addAttr2 = false;
            switch (this.fFilter.acceptNode(attr)) {
                case (short) 2:
                case (short) 3:
                    break;
            }
            addAttr = addAttr2;
            if (!addAttr) {
            }
            if (addAttr) {
            }
            attrName = attrValue;
            attrPrefix = localName;
            indexOf = atts.indexOf(":");
            index = indexOf;
            if (indexOf >= 0) {
            }
            if ("".equals(prefix)) {
            }
            i++;
            atts = atts2;
            nAttrs2 = nAttrs;
        }
        nAttrs = nAttrs2;
    }

    protected void serializePI(ProcessingInstruction node) throws SAXException {
        ProcessingInstruction pi = node;
        String name = pi.getNodeName();
        if ((this.fFeatures & 16384) != 0) {
            isPIWellFormed(node);
        }
        if (applyFilter(node, 64)) {
            if (name.equals("xslt-next-is-raw")) {
                this.fNextIsRaw = true;
            } else {
                this.fSerializer.processingInstruction(name, pi.getData());
            }
        }
    }

    protected void serializeCDATASection(CDATASection node) throws SAXException {
        if ((this.fFeatures & 16384) != 0) {
            isCDATASectionWellFormed(node);
        }
        if ((this.fFeatures & 2) != 0) {
            String nodeValue = node.getNodeValue();
            int endIndex = nodeValue.indexOf(SerializerConstants.CDATA_DELIMITER_CLOSE);
            String msg;
            if ((this.fFeatures & 2048) != 0) {
                if (endIndex >= 0) {
                    String relatedData = nodeValue.substring(0, endIndex + 2);
                    msg = Utils.messages.createMessage(MsgKey.ER_CDATA_SECTIONS_SPLIT, null);
                    if (this.fErrorHandler != null) {
                        this.fErrorHandler.handleError(new DOMErrorImpl((short) 1, msg, MsgKey.ER_CDATA_SECTIONS_SPLIT, null, relatedData, null));
                    }
                }
            } else if (endIndex >= 0) {
                msg = nodeValue.substring(0, endIndex + 2);
                String msg2 = Utils.messages.createMessage(MsgKey.ER_CDATA_SECTIONS_SPLIT, null);
                if (this.fErrorHandler != null) {
                    this.fErrorHandler.handleError(new DOMErrorImpl((short) 2, msg2, MsgKey.ER_CDATA_SECTIONS_SPLIT));
                }
                return;
            }
            if (applyFilter(node, 8)) {
                if (this.fLexicalHandler != null) {
                    this.fLexicalHandler.startCDATA();
                }
                dispatachChars(node);
                if (this.fLexicalHandler != null) {
                    this.fLexicalHandler.endCDATA();
                }
            } else {
                return;
            }
        }
        dispatachChars(node);
    }

    protected void serializeText(Text node) throws SAXException {
        if (this.fNextIsRaw) {
            this.fNextIsRaw = false;
            this.fSerializer.processingInstruction("javax.xml.transform.disable-output-escaping", "");
            dispatachChars(node);
            this.fSerializer.processingInstruction("javax.xml.transform.enable-output-escaping", "");
        } else {
            boolean bDispatch = false;
            if ((this.fFeatures & 16384) != 0) {
                isTextWellFormed(node);
            }
            boolean isElementContentWhitespace = false;
            if (this.fIsLevel3DOM) {
                isElementContentWhitespace = node.isElementContentWhitespace();
            }
            if (!isElementContentWhitespace) {
                bDispatch = true;
            } else if ((this.fFeatures & 32) != 0) {
                bDispatch = true;
            }
            if (applyFilter(node, 4) && bDispatch) {
                dispatachChars(node);
            }
        }
    }

    protected void serializeEntityReference(EntityReference node, boolean bStart) throws SAXException {
        EntityReference eref;
        if (bStart) {
            eref = node;
            if ((this.fFeatures & 64) != 0) {
                if ((this.fFeatures & 16384) != 0) {
                    isEntityReferneceWellFormed(node);
                }
                if ((this.fFeatures & 256) != 0) {
                    checkUnboundPrefixInEntRef(node);
                }
            }
            if (this.fLexicalHandler != null) {
                this.fLexicalHandler.startEntity(eref.getNodeName());
                return;
            }
            return;
        }
        eref = node;
        if (this.fLexicalHandler != null) {
            this.fLexicalHandler.endEntity(eref.getNodeName());
        }
    }

    protected boolean isXMLName(String s, boolean xml11Version) {
        if (s == null) {
            return false;
        }
        if (xml11Version) {
            return XML11Char.isXML11ValidName(s);
        }
        return XMLChar.isValidName(s);
    }

    protected boolean isValidQName(String prefix, String local, boolean xml11Version) {
        boolean validNCName = false;
        if (local == null) {
            return false;
        }
        if (xml11Version) {
            if ((prefix == null || XML11Char.isXML11ValidNCName(prefix)) && XML11Char.isXML11ValidNCName(local)) {
                validNCName = true;
            }
        } else if ((prefix == null || XMLChar.isValidNCName(prefix)) && XMLChar.isValidNCName(local)) {
            validNCName = true;
        }
        return validNCName;
    }

    protected boolean isWFXMLChar(String chardata, Character refInvalidChar) {
        if (chardata == null || chardata.length() == 0) {
            return true;
        }
        char[] dataarray = chardata.toCharArray();
        int datalength = dataarray.length;
        int i;
        int i2;
        char ch;
        int i3;
        char ch2;
        if (this.fIsXMLVersion11) {
            i = 0;
            while (i < datalength) {
                i2 = i + 1;
                if (XML11Char.isXML11Invalid(dataarray[i])) {
                    ch = dataarray[i2 - 1];
                    if (XMLChar.isHighSurrogate(ch) && i2 < datalength) {
                        i3 = i2 + 1;
                        ch2 = dataarray[i2];
                        if (XMLChar.isLowSurrogate(ch2) && XMLChar.isSupplemental(XMLChar.supplemental(ch, ch2))) {
                            i = i3;
                        }
                    }
                    refInvalidChar = new Character(ch);
                    return false;
                }
                i = i2;
            }
        } else {
            i = 0;
            while (i < datalength) {
                i2 = i + 1;
                if (XMLChar.isInvalid(dataarray[i])) {
                    ch = dataarray[i2 - 1];
                    if (XMLChar.isHighSurrogate(ch) && i2 < datalength) {
                        i3 = i2 + 1;
                        ch2 = dataarray[i2];
                        if (XMLChar.isLowSurrogate(ch2) && XMLChar.isSupplemental(XMLChar.supplemental(ch, ch2))) {
                            i = i3;
                        }
                    }
                    refInvalidChar = new Character(ch);
                    return false;
                }
                i = i2;
            }
        }
        return true;
    }

    protected Character isWFXMLChar(String chardata) {
        if (chardata == null || chardata.length() == 0) {
            return null;
        }
        char[] dataarray = chardata.toCharArray();
        char datalength = dataarray.length;
        char i = 0;
        char i2;
        char i3;
        if (this.fIsXMLVersion11) {
            while (true) {
                i2 = i;
                if (i2 >= datalength) {
                    break;
                }
                i = i2 + 1;
                if (XML11Char.isXML11Invalid(dataarray[i2])) {
                    i2 = dataarray[i - 1];
                    if (!XMLChar.isHighSurrogate(i2) || i >= datalength) {
                        break;
                    }
                    i3 = i + 1;
                    i = dataarray[i];
                    if (XMLChar.isLowSurrogate(i) && XMLChar.isSupplemental(XMLChar.supplemental(i2, i))) {
                        i = i3;
                    }
                }
            }
            return new Character(i2);
        }
        while (true) {
            i2 = i;
            if (i2 >= datalength) {
                break;
            }
            i = i2 + 1;
            if (XMLChar.isInvalid(dataarray[i2])) {
                i2 = dataarray[i - 1];
                if (!XMLChar.isHighSurrogate(i2) || i >= datalength) {
                    break;
                }
                i3 = i + 1;
                i = dataarray[i];
                if (XMLChar.isLowSurrogate(i) && XMLChar.isSupplemental(XMLChar.supplemental(i2, i))) {
                    i = i3;
                }
            }
        }
        return new Character(i2);
        return null;
    }

    protected void isCommentWellFormed(String data) {
        if (data != null && data.length() != 0) {
            char[] dataarray = data.toCharArray();
            char datalength = dataarray.length;
            Object[] objArr = null;
            char c = '-';
            char c2;
            char i;
            DOMErrorImpl dOMErrorImpl;
            if (this.fIsXMLVersion11) {
                c2 = 0;
                while (c2 < datalength) {
                    i = c2 + 1;
                    c2 = dataarray[c2];
                    String msg;
                    if (XML11Char.isXML11Invalid(c2)) {
                        if (XMLChar.isHighSurrogate(c2) && i < datalength) {
                            char i2 = i + 1;
                            i = dataarray[i];
                            if (XMLChar.isLowSurrogate(i) && XMLChar.isSupplemental(XMLChar.supplemental(c2, i))) {
                                c2 = i2;
                            } else {
                                i = i2;
                            }
                        }
                        msg = Utils.messages.createMessage(MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT, new Object[]{new Character(c2)});
                        if (this.fErrorHandler != null) {
                            this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_WF_INVALID_CHARACTER, null, null, null));
                        }
                    } else if (c2 == '-' && i < datalength && dataarray[i] == '-') {
                        msg = Utils.messages.createMessage(MsgKey.ER_WF_DASH_IN_COMMENT, objArr);
                        if (this.fErrorHandler != null) {
                            DOMErrorHandler dOMErrorHandler = this.fErrorHandler;
                            DOMErrorImpl dOMErrorImpl2 = dOMErrorImpl;
                            dOMErrorImpl = new DOMErrorImpl((short) 3, msg, MsgKey.ER_WF_INVALID_CHARACTER, null, null, null);
                            dOMErrorHandler.handleError(dOMErrorImpl2);
                        }
                    }
                    c2 = i;
                    objArr = null;
                }
            } else {
                c2 = 0;
                while (c2 < datalength) {
                    char i3 = c2 + 1;
                    c2 = dataarray[c2];
                    String msg2;
                    if (XMLChar.isInvalid(c2)) {
                        if (XMLChar.isHighSurrogate(c2) && i3 < datalength) {
                            i = i3 + 1;
                            i3 = dataarray[i3];
                            if (XMLChar.isLowSurrogate(i3) && XMLChar.isSupplemental(XMLChar.supplemental(c2, i3))) {
                                c2 = i;
                            } else {
                                i3 = i;
                            }
                        }
                        msg2 = Utils.messages.createMessage(MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT, new Object[]{new Character(c2)});
                        if (this.fErrorHandler != null) {
                            this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg2, MsgKey.ER_WF_INVALID_CHARACTER, null, null, null));
                        }
                        c2 = i3;
                    } else {
                        if (c2 == c && i3 < datalength && dataarray[i3] == c) {
                            msg2 = Utils.messages.createMessage(MsgKey.ER_WF_DASH_IN_COMMENT, null);
                            if (this.fErrorHandler != null) {
                                DOMErrorHandler dOMErrorHandler2 = this.fErrorHandler;
                                DOMErrorImpl dOMErrorImpl3 = dOMErrorImpl;
                                dOMErrorImpl = new DOMErrorImpl((short) 3, msg2, MsgKey.ER_WF_INVALID_CHARACTER, null, null, null);
                                dOMErrorHandler2.handleError(dOMErrorImpl3);
                            }
                        }
                        c2 = i3;
                    }
                    c = '-';
                }
            }
        }
    }

    protected void isElementWellFormed(Node node) {
        boolean isNameWF;
        if ((this.fFeatures & 256) != 0) {
            isNameWF = isValidQName(node.getPrefix(), node.getLocalName(), this.fIsXMLVersion11);
        } else {
            isNameWF = isXMLName(node.getNodeName(), this.fIsXMLVersion11);
        }
        if (!isNameWF) {
            String msg = Utils.messages.createMessage(MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, new Object[]{"Element", node.getNodeName()});
            if (this.fErrorHandler != null) {
                this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, null, null, null));
            }
        }
    }

    protected void isAttributeWellFormed(Node node) {
        boolean isNameWF;
        if ((this.fFeatures & 256) != 0) {
            isNameWF = isValidQName(node.getPrefix(), node.getLocalName(), this.fIsXMLVersion11);
        } else {
            isNameWF = isXMLName(node.getNodeName(), this.fIsXMLVersion11);
        }
        int i = 0;
        if (!isNameWF) {
            String msg = Utils.messages.createMessage(MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, new Object[]{"Attr", node.getNodeName()});
            if (this.fErrorHandler != null) {
                this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, null, null, null));
            }
        }
        if (node.getNodeValue().indexOf(60) >= 0) {
            String msg2 = Utils.messages.createMessage(MsgKey.ER_WF_LT_IN_ATTVAL, new Object[]{((Attr) node).getOwnerElement().getNodeName(), node.getNodeName()});
            if (this.fErrorHandler != null) {
                this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg2, MsgKey.ER_WF_LT_IN_ATTVAL, null, null, null));
            }
        }
        NodeList children = node.getChildNodes();
        while (true) {
            int i2 = i;
            if (i2 < children.getLength()) {
                Node child = children.item(i2);
                if (child != null) {
                    short nodeType = child.getNodeType();
                    if (nodeType == (short) 3) {
                        isTextWellFormed((Text) child);
                    } else if (nodeType == (short) 5) {
                        isEntityReferneceWellFormed((EntityReference) child);
                    }
                }
                i = i2 + 1;
            } else {
                return;
            }
        }
    }

    protected void isPIWellFormed(ProcessingInstruction node) {
        if (!isXMLName(node.getNodeName(), this.fIsXMLVersion11)) {
            String msg = Utils.messages.createMessage(MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, new Object[]{"ProcessingInstruction", node.getTarget()});
            if (this.fErrorHandler != null) {
                this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, null, null, null));
            }
        }
        if (isWFXMLChar(node.getData()) != null) {
            String msg2 = Utils.messages.createMessage(MsgKey.ER_WF_INVALID_CHARACTER_IN_PI, new Object[]{Integer.toHexString(Character.getNumericValue(invalidChar.charValue()))});
            if (this.fErrorHandler != null) {
                this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg2, MsgKey.ER_WF_INVALID_CHARACTER, null, null, null));
            }
        }
    }

    protected void isCDATASectionWellFormed(CDATASection node) {
        if (isWFXMLChar(node.getData()) != null) {
            String msg = Utils.messages.createMessage(MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA, new Object[]{Integer.toHexString(Character.getNumericValue(invalidChar.charValue()))});
            if (this.fErrorHandler != null) {
                this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_WF_INVALID_CHARACTER, null, null, null));
            }
        }
    }

    protected void isTextWellFormed(Text node) {
        if (isWFXMLChar(node.getData()) != null) {
            String msg = Utils.messages.createMessage(MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT, new Object[]{Integer.toHexString(Character.getNumericValue(invalidChar.charValue()))});
            if (this.fErrorHandler != null) {
                this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_WF_INVALID_CHARACTER, null, null, null));
            }
        }
    }

    protected void isEntityReferneceWellFormed(EntityReference node) {
        if (!isXMLName(node.getNodeName(), this.fIsXMLVersion11)) {
            String msg = Utils.messages.createMessage(MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, new Object[]{"EntityReference", node.getNodeName()});
            if (this.fErrorHandler != null) {
                this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, null, null, null));
            }
        }
        Node parent = node.getParentNode();
        DocumentType docType = node.getOwnerDocument().getDoctype();
        if (docType != null) {
            NamedNodeMap entities = docType.getEntities();
            for (int i = 0; i < entities.getLength(); i++) {
                String msg2;
                Entity ent = (Entity) entities.item(i);
                String nodeName = node.getNodeName() == null ? "" : node.getNodeName();
                String nodeNamespaceURI;
                if (node.getNamespaceURI() == null) {
                    nodeNamespaceURI = "";
                } else {
                    nodeNamespaceURI = node.getNamespaceURI();
                }
                String entName = ent.getNodeName() == null ? "" : ent.getNodeName();
                String entNamespaceURI = ent.getNamespaceURI() == null ? "" : ent.getNamespaceURI();
                if (parent.getNodeType() == (short) 1 && entNamespaceURI.equals(nodeNamespaceURI) && entName.equals(nodeName) && ent.getNotationName() != null) {
                    msg2 = Utils.messages.createMessage(MsgKey.ER_WF_REF_TO_UNPARSED_ENT, new Object[]{node.getNodeName()});
                    if (this.fErrorHandler != null) {
                        this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg2, MsgKey.ER_WF_REF_TO_UNPARSED_ENT, null, null, null));
                    }
                }
                if (parent.getNodeType() == (short) 2 && entNamespaceURI.equals(nodeNamespaceURI) && entName.equals(nodeName) && !(ent.getPublicId() == null && ent.getSystemId() == null && ent.getNotationName() == null)) {
                    msg2 = Utils.messages.createMessage(MsgKey.ER_WF_REF_TO_EXTERNAL_ENT, new Object[]{node.getNodeName()});
                    if (this.fErrorHandler != null) {
                        this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg2, MsgKey.ER_WF_REF_TO_EXTERNAL_ENT, null, null, null));
                    }
                }
            }
        }
    }

    protected void checkUnboundPrefixInEntRef(Node node) {
        Node child = node.getFirstChild();
        while (child != null) {
            Node next = child.getNextSibling();
            if (child.getNodeType() == (short) 1) {
                String prefix = child.getPrefix();
                if (prefix != null && this.fNSBinder.getURI(prefix) == null) {
                    String msg = Utils.messages.createMessage("unbound-prefix-in-entity-reference", new Object[]{node.getNodeName(), child.getNodeName(), prefix});
                    if (this.fErrorHandler != null) {
                        this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg, "unbound-prefix-in-entity-reference", null, null, null));
                    }
                }
                NamedNodeMap attrs = child.getAttributes();
                for (int i = 0; i < attrs.getLength(); i++) {
                    String attrPrefix = attrs.item(i).getPrefix();
                    if (attrPrefix != null && this.fNSBinder.getURI(attrPrefix) == null) {
                        String msg2 = Utils.messages.createMessage("unbound-prefix-in-entity-reference", new Object[]{node.getNodeName(), child.getNodeName(), attrs.item(i)});
                        if (this.fErrorHandler != null) {
                            this.fErrorHandler.handleError(new DOMErrorImpl((short) 3, msg2, "unbound-prefix-in-entity-reference", null, null, null));
                        }
                    }
                }
            }
            if (child.hasChildNodes()) {
                checkUnboundPrefixInEntRef(child);
            }
            child = next;
        }
    }

    protected void recordLocalNSDecl(Node node) {
        NamedNodeMap atts = ((Element) node).getAttributes();
        int length = atts.getLength();
        for (int i = 0; i < length; i++) {
            Node attr = atts.item(i);
            String localName = attr.getLocalName();
            String attrPrefix = attr.getPrefix();
            String attrValue = attr.getNodeValue();
            String attrNS = attr.getNamespaceURI();
            String str = (localName == null || "xmlns".equals(localName)) ? "" : localName;
            localName = str;
            attrPrefix = attrPrefix == null ? "" : attrPrefix;
            attrValue = attrValue == null ? "" : attrValue;
            if ("http://www.w3.org/2000/xmlns/".equals(attrNS == null ? "" : attrNS)) {
                if ("http://www.w3.org/2000/xmlns/".equals(attrValue)) {
                    str = Utils.messages.createMessage(MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND, new Object[]{attrPrefix, "http://www.w3.org/2000/xmlns/"});
                    if (this.fErrorHandler != null) {
                        this.fErrorHandler.handleError(new DOMErrorImpl((short) 2, str, MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND, null, null, null));
                    }
                } else if (!"xmlns".equals(attrPrefix)) {
                    this.fNSBinder.declarePrefix("", attrValue);
                } else if (attrValue.length() != 0) {
                    this.fNSBinder.declarePrefix(localName, attrValue);
                }
            }
        }
    }

    protected void fixupElementNS(Node node) throws SAXException {
        String namespaceURI = ((Element) node).getNamespaceURI();
        String prefix = ((Element) node).getPrefix();
        String localName = ((Element) node).getLocalName();
        String inScopeNamespaceURI;
        if (namespaceURI != null) {
            prefix = prefix == null ? "" : prefix;
            inScopeNamespaceURI = this.fNSBinder.getURI(prefix);
            if (inScopeNamespaceURI == null || !inScopeNamespaceURI.equals(namespaceURI)) {
                if ((this.fFeatures & 512) != 0) {
                    if ("".equals(prefix) || "".equals(namespaceURI)) {
                        ((Element) node).setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", namespaceURI);
                    } else {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(Constants.ATTRNAME_XMLNS);
                        stringBuilder.append(prefix);
                        ((Element) node).setAttributeNS("http://www.w3.org/2000/xmlns/", stringBuilder.toString(), namespaceURI);
                    }
                }
                this.fLocalNSBinder.declarePrefix(prefix, namespaceURI);
                this.fNSBinder.declarePrefix(prefix, namespaceURI);
            }
        } else if (localName == null || "".equals(localName)) {
            inScopeNamespaceURI = Utils.messages.createMessage(MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, new Object[]{node.getNodeName()});
            if (this.fErrorHandler != null) {
                this.fErrorHandler.handleError(new DOMErrorImpl((short) 2, inScopeNamespaceURI, MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, null, null, null));
            }
        } else {
            namespaceURI = this.fNSBinder.getURI("");
            if (namespaceURI != null && namespaceURI.length() > 0) {
                ((Element) node).setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "");
                this.fLocalNSBinder.declarePrefix("", "");
                this.fNSBinder.declarePrefix("", "");
            }
        }
    }

    static {
        s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}cdata-sections", new Integer(2));
        s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}comments", new Integer(8));
        s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}element-content-whitespace", new Integer(32));
        s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}entities", new Integer(64));
        s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}namespaces", new Integer(256));
        s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}namespace-declarations", new Integer(512));
        s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}split-cdata-sections", new Integer(2048));
        s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}well-formed", new Integer(16384));
        s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}discard-default-content", new Integer(32768));
        s_propKeys.put("{http://www.w3.org/TR/DOM-Level-3-LS}format-pretty-print", "");
        s_propKeys.put("omit-xml-declaration", "");
        s_propKeys.put("{http://xml.apache.org/xerces-2j}xml-version", "");
        s_propKeys.put("encoding", "");
        s_propKeys.put("{http://xml.apache.org/xerces-2j}entities", "");
    }

    protected void initProperties(Properties properties) {
        Enumeration keys = properties.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            Object iobj = s_propKeys.get(key);
            if (iobj != null) {
                String version;
                if (iobj instanceof Integer) {
                    int BITFLAG = ((Integer) iobj).intValue();
                    if (properties.getProperty(key).endsWith("yes")) {
                        this.fFeatures |= BITFLAG;
                    } else {
                        this.fFeatures &= ~BITFLAG;
                    }
                } else if ("{http://www.w3.org/TR/DOM-Level-3-LS}format-pretty-print".equals(key)) {
                    if (properties.getProperty(key).endsWith("yes")) {
                        this.fSerializer.setIndent(true);
                        this.fSerializer.setIndentAmount(3);
                    } else {
                        this.fSerializer.setIndent(false);
                    }
                } else if ("omit-xml-declaration".equals(key)) {
                    if (properties.getProperty(key).endsWith("yes")) {
                        this.fSerializer.setOmitXMLDeclaration(true);
                    } else {
                        this.fSerializer.setOmitXMLDeclaration(false);
                    }
                } else if ("{http://xml.apache.org/xerces-2j}xml-version".equals(key)) {
                    version = properties.getProperty(key);
                    if (SerializerConstants.XMLVERSION11.equals(version)) {
                        this.fIsXMLVersion11 = true;
                        this.fSerializer.setVersion(version);
                    } else {
                        this.fSerializer.setVersion(SerializerConstants.XMLVERSION10);
                    }
                } else if ("encoding".equals(key)) {
                    version = properties.getProperty(key);
                    if (version != null) {
                        this.fSerializer.setEncoding(version);
                    }
                } else if ("{http://xml.apache.org/xerces-2j}entities".equals(key)) {
                    if (properties.getProperty(key).endsWith("yes")) {
                        this.fSerializer.setDTDEntityExpansion(false);
                    } else {
                        this.fSerializer.setDTDEntityExpansion(true);
                    }
                }
            }
        }
        if (this.fNewLine != null) {
            this.fSerializer.setOutputProperty(OutputPropertiesFactory.S_KEY_LINE_SEPARATOR, this.fNewLine);
        }
    }
}
