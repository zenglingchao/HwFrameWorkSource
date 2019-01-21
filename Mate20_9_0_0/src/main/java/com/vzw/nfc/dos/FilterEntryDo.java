package com.vzw.nfc.dos;

public class FilterEntryDo extends VzwTlv {
    public static final int TAG = 161;
    private AidMaskDo mAidMaskDo = null;
    private AidRangeDo mAidRangeDo = null;
    private FilterConditionTagDo mFilterConditionTagDo = null;
    private RoutingModeDo mRoutingModeDo = null;
    private VzwPermissionDo mVzwArDo = null;

    public FilterEntryDo(byte[] rawData, int valueIndex, int valueLength) {
        super(rawData, TAG, valueIndex, valueLength);
    }

    public FilterEntryDo(AidRangeDo aid_range, AidMaskDo aid_mask, RoutingModeDo routing_mode, FilterConditionTagDo filter_condition_tag) {
        super(null, TAG, 0, 0);
        this.mAidMaskDo = aid_mask;
        this.mAidRangeDo = aid_range;
        this.mFilterConditionTagDo = filter_condition_tag;
        this.mRoutingModeDo = routing_mode;
    }

    public AidRangeDo getAidRangeDo() {
        return this.mAidRangeDo;
    }

    public AidMaskDo getAidMaskDo() {
        return this.mAidMaskDo;
    }

    public RoutingModeDo getRoutingModeDo() {
        return this.mRoutingModeDo;
    }

    public FilterConditionTagDo getFilterConditionTagDo() {
        return this.mFilterConditionTagDo;
    }

    public VzwPermissionDo getVzwArDo() {
        return this.mVzwArDo;
    }

    public void translate() throws DoParserException {
        this.mAidMaskDo = null;
        this.mAidRangeDo = null;
        this.mFilterConditionTagDo = null;
        this.mRoutingModeDo = null;
        byte[] data = getRawData();
        int index = getValueIndex();
        if (getValueLength() + index <= data.length) {
            do {
                VzwTlv temp = VzwTlv.parse(data, index);
                if (temp.getTag() == AidMaskDo.TAG) {
                    this.mAidMaskDo = new AidMaskDo(data, temp.getValueIndex(), temp.getValueLength());
                    this.mAidMaskDo.translate();
                } else if (temp.getTag() == AidRangeDo.TAG) {
                    this.mAidRangeDo = new AidRangeDo(data, temp.getValueIndex(), temp.getValueLength());
                    this.mAidRangeDo.translate();
                } else if (temp.getTag() == RoutingModeDo.TAG) {
                    this.mRoutingModeDo = new RoutingModeDo(data, temp.getValueIndex(), temp.getValueLength());
                    this.mRoutingModeDo.translate();
                } else if (temp.getTag() == FilterConditionTagDo.TAG) {
                    this.mFilterConditionTagDo = new FilterConditionTagDo(data, temp.getValueIndex(), temp.getValueLength());
                    this.mFilterConditionTagDo.translate();
                } else if (temp.getTag() == VzwPermissionDo.TAG) {
                    this.mVzwArDo = new VzwPermissionDo(data, temp.getValueIndex(), temp.getValueLength());
                    this.mVzwArDo.translate();
                } else {
                    throw new DoParserException("Invalid DO in FILTER_ENTRY_DO!");
                }
                index = temp.getValueIndex() + temp.getValueLength();
            } while (getValueIndex() + getValueLength() > index);
            if (this.mAidMaskDo == null || this.mAidRangeDo == null || this.mRoutingModeDo == null || this.mVzwArDo == null || this.mAidMaskDo.getAidMask().length != this.mAidRangeDo.getAidRange().length) {
                throw new DoParserException("missing DO in FILTER_ENTRY_DO!");
            }
            return;
        }
        throw new DoParserException("Not enough data for FILTER_ENTRY_DO!");
    }
}
