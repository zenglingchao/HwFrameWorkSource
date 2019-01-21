package org.apache.xalan.templates;

import org.apache.xalan.res.XSLTErrorResources;

public class ElemText extends ElemTemplateElement {
    static final long serialVersionUID = 1383140876182316711L;
    private boolean m_disableOutputEscaping = false;

    public void setDisableOutputEscaping(boolean v) {
        this.m_disableOutputEscaping = v;
    }

    public boolean getDisableOutputEscaping() {
        return this.m_disableOutputEscaping;
    }

    public int getXSLToken() {
        return 42;
    }

    public String getNodeName() {
        return "text";
    }

    public ElemTemplateElement appendChild(ElemTemplateElement newChild) {
        if (newChild.getXSLToken() != 78) {
            error(XSLTErrorResources.ER_CANNOT_ADD, new Object[]{newChild.getNodeName(), getNodeName()});
        }
        return super.appendChild(newChild);
    }
}