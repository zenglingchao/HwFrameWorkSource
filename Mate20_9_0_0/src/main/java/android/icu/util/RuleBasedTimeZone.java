package android.icu.util;

import android.icu.impl.Grego;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.List;

public class RuleBasedTimeZone extends BasicTimeZone {
    private static final long serialVersionUID = 7580833058949327935L;
    private AnnualTimeZoneRule[] finalRules;
    private List<TimeZoneRule> historicRules;
    private transient List<TimeZoneTransition> historicTransitions;
    private final InitialTimeZoneRule initialRule;
    private volatile transient boolean isFrozen = false;
    private transient boolean upToDate;

    public RuleBasedTimeZone(String id, InitialTimeZoneRule initialRule) {
        super(id);
        this.initialRule = initialRule;
    }

    public void addTransitionRule(TimeZoneRule rule) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify a frozen RuleBasedTimeZone instance.");
        } else if (rule.isTransitionRule()) {
            if (!(rule instanceof AnnualTimeZoneRule) || ((AnnualTimeZoneRule) rule).getEndYear() != Integer.MAX_VALUE) {
                if (this.historicRules == null) {
                    this.historicRules = new ArrayList();
                }
                this.historicRules.add(rule);
            } else if (this.finalRules == null) {
                this.finalRules = new AnnualTimeZoneRule[2];
                this.finalRules[0] = (AnnualTimeZoneRule) rule;
            } else if (this.finalRules[1] == null) {
                this.finalRules[1] = (AnnualTimeZoneRule) rule;
            } else {
                throw new IllegalStateException("Too many final rules");
            }
            this.upToDate = false;
        } else {
            throw new IllegalArgumentException("Rule must be a transition rule");
        }
    }

    public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) {
        int year2;
        if (era == 0) {
            year2 = 1 - year;
        } else {
            year2 = year;
        }
        int[] offsets = new int[2];
        getOffset((Grego.fieldsToDay(year2, month, day) * 86400000) + ((long) milliseconds), true, 3, 1, offsets);
        return offsets[0] + offsets[1];
    }

    public void getOffset(long time, boolean local, int[] offsets) {
        getOffset(time, local, 4, 12, offsets);
    }

    @Deprecated
    public void getOffsetFromLocal(long date, int nonExistingTimeOpt, int duplicatedTimeOpt, int[] offsets) {
        getOffset(date, true, nonExistingTimeOpt, duplicatedTimeOpt, offsets);
    }

    public int getRawOffset() {
        int[] offsets = new int[2];
        getOffset(System.currentTimeMillis(), false, offsets);
        return offsets[0];
    }

    public boolean inDaylightTime(Date date) {
        int[] offsets = new int[2];
        getOffset(date.getTime(), false, offsets);
        return offsets[1] != 0;
    }

    public void setRawOffset(int offsetMillis) {
        throw new UnsupportedOperationException("setRawOffset in RuleBasedTimeZone is not supported.");
    }

    public boolean useDaylightTime() {
        long now = System.currentTimeMillis();
        int[] offsets = new int[2];
        getOffset(now, false, offsets);
        if (offsets[1] != 0) {
            return true;
        }
        TimeZoneTransition tt = getNextTransition(now, false);
        if (tt == null || tt.getTo().getDSTSavings() == 0) {
            return false;
        }
        return true;
    }

    public boolean observesDaylightTime() {
        long time = System.currentTimeMillis();
        int[] offsets = new int[2];
        getOffset(time, false, offsets);
        if (offsets[1] != 0) {
            return true;
        }
        BitSet checkFinals = this.finalRules == null ? null : new BitSet(this.finalRules.length);
        while (true) {
            TimeZoneTransition tt = getNextTransition(time, false);
            if (tt == null) {
                break;
            }
            TimeZoneRule toRule = tt.getTo();
            if (toRule.getDSTSavings() != 0) {
                return true;
            }
            if (checkFinals != null) {
                for (int i = 0; i < this.finalRules.length; i++) {
                    if (this.finalRules[i].equals(toRule)) {
                        checkFinals.set(i);
                    }
                }
                if (checkFinals.cardinality() == this.finalRules.length) {
                    break;
                }
            }
            time = tt.getTime();
        }
        return false;
    }

    public boolean hasSameRules(TimeZone other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RuleBasedTimeZone)) {
            return false;
        }
        RuleBasedTimeZone otherRBTZ = (RuleBasedTimeZone) other;
        if (!this.initialRule.isEquivalentTo(otherRBTZ.initialRule)) {
            return false;
        }
        if (this.finalRules != null && otherRBTZ.finalRules != null) {
            int i = 0;
            while (i < this.finalRules.length) {
                if ((this.finalRules[i] != null || otherRBTZ.finalRules[i] != null) && (this.finalRules[i] == null || otherRBTZ.finalRules[i] == null || !this.finalRules[i].isEquivalentTo(otherRBTZ.finalRules[i]))) {
                    return false;
                }
                i++;
            }
        } else if (!(this.finalRules == null && otherRBTZ.finalRules == null)) {
            return false;
        }
        if (this.historicRules == null || otherRBTZ.historicRules == null) {
            if (!(this.historicRules == null && otherRBTZ.historicRules == null)) {
                return false;
            }
        } else if (this.historicRules.size() != otherRBTZ.historicRules.size()) {
            return false;
        } else {
            for (TimeZoneRule rule : this.historicRules) {
                boolean foundSameRule = false;
                for (TimeZoneRule orule : otherRBTZ.historicRules) {
                    if (rule.isEquivalentTo(orule)) {
                        foundSameRule = true;
                        break;
                    }
                }
                if (!foundSameRule) {
                    return false;
                }
            }
        }
        return true;
    }

    public TimeZoneRule[] getTimeZoneRules() {
        int size = 1;
        if (this.historicRules != null) {
            size = 1 + this.historicRules.size();
        }
        if (this.finalRules != null) {
            if (this.finalRules[1] != null) {
                size += 2;
            } else {
                size++;
            }
        }
        TimeZoneRule[] rules = new TimeZoneRule[size];
        rules[0] = this.initialRule;
        int idx = 1;
        if (this.historicRules != null) {
            while (idx < this.historicRules.size() + 1) {
                rules[idx] = (TimeZoneRule) this.historicRules.get(idx - 1);
                idx++;
            }
        }
        if (this.finalRules != null) {
            int idx2 = idx + 1;
            rules[idx] = this.finalRules[0];
            if (this.finalRules[1] != null) {
                rules[idx2] = this.finalRules[1];
            }
            idx = idx2;
        }
        return rules;
    }

    public TimeZoneTransition getNextTransition(long base, boolean inclusive) {
        complete();
        if (this.historicTransitions == null) {
            return null;
        }
        boolean isFinal = false;
        TimeZoneTransition result = (TimeZoneTransition) this.historicTransitions.get(0);
        long tt = result.getTime();
        TimeZoneTransition tzt;
        long j;
        if (tt > base || (inclusive && tt == base)) {
            tzt = result;
            j = tt;
        } else {
            int idx = this.historicTransitions.size() - 1;
            tzt = (TimeZoneTransition) this.historicTransitions.get(idx);
            j = tzt.getTime();
            if (inclusive && j == base) {
                result = tzt;
            } else if (j > base) {
                idx--;
                result = tzt;
                while (true) {
                    tt = tzt;
                    if (idx <= 0) {
                        break;
                    }
                    result = (TimeZoneTransition) this.historicTransitions.get(idx);
                    j = result.getTime();
                    if (j < base || (!inclusive && j == base)) {
                        break;
                    }
                    idx--;
                    tzt = result;
                }
                tzt = result;
                result = tt;
            } else if (this.finalRules == null) {
                return null;
            } else {
                TimeZoneTransition tzt2;
                Date start0 = this.finalRules[0].getNextStart(base, this.finalRules[1].getRawOffset(), this.finalRules[1].getDSTSavings(), inclusive);
                Date start02 = start0;
                Date start1 = this.finalRules[1].getNextStart(base, this.finalRules[0].getRawOffset(), this.finalRules[0].getDSTSavings(), inclusive);
                if (start1.after(start02)) {
                    tzt2 = new TimeZoneTransition(start02.getTime(), this.finalRules[1], this.finalRules[0]);
                } else {
                    tzt2 = new TimeZoneTransition(start1.getTime(), this.finalRules[0], this.finalRules[1]);
                }
                isFinal = true;
                tzt = tzt2;
                result = tzt2;
            }
        }
        TimeZoneRule from = result.getFrom();
        TimeZoneRule to = result.getTo();
        if (from.getRawOffset() == to.getRawOffset() && from.getDSTSavings() == to.getDSTSavings()) {
            if (isFinal) {
                return null;
            }
            result = getNextTransition(result.getTime(), false);
        }
        return result;
    }

    public TimeZoneTransition getPreviousTransition(long base, boolean inclusive) {
        complete();
        if (this.historicTransitions == null) {
            return null;
        }
        TimeZoneTransition result;
        TimeZoneRule from;
        TimeZoneRule to;
        TimeZoneTransition tzt = (TimeZoneTransition) this.historicTransitions.get(0);
        long tt = tzt.getTime();
        if (inclusive && tt == base) {
            result = tzt;
        } else if (tt >= base) {
            return null;
        } else {
            int idx = this.historicTransitions.size() - 1;
            TimeZoneTransition tzt2 = (TimeZoneTransition) this.historicTransitions.get(idx);
            long tt2 = tzt2.getTime();
            if (inclusive && tt2 == base) {
                result = tzt2;
                tzt = tzt2;
                from = result.getFrom();
                to = result.getTo();
                result = getPreviousTransition(result.getTime(), false);
                return result;
            } else if (tt2 < base) {
                if (this.finalRules != null) {
                    Date start0 = this.finalRules[0].getPreviousStart(base, this.finalRules[1].getRawOffset(), this.finalRules[1].getDSTSavings(), inclusive);
                    Date start02 = start0;
                    Date start1 = this.finalRules[1].getPreviousStart(base, this.finalRules[0].getRawOffset(), this.finalRules[0].getDSTSavings(), inclusive);
                    if (start1.before(start02)) {
                        tzt = new TimeZoneTransition(start02.getTime(), this.finalRules[1], this.finalRules[0]);
                    } else {
                        tzt = new TimeZoneTransition(start1.getTime(), this.finalRules[0], this.finalRules[1]);
                    }
                } else {
                    tzt = tzt2;
                }
                result = tzt;
                from = result.getFrom();
                to = result.getTo();
                if (from.getRawOffset() == to.getRawOffset() && from.getDSTSavings() == to.getDSTSavings()) {
                    result = getPreviousTransition(result.getTime(), false);
                }
                return result;
            } else {
                for (idx--; idx >= 0; idx--) {
                    tzt2 = (TimeZoneTransition) this.historicTransitions.get(idx);
                    tt2 = tzt2.getTime();
                    if (tt2 < base || (inclusive && tt2 == base)) {
                        break;
                    }
                }
                tt = tt2;
                result = tzt2;
            }
        }
        from = result.getFrom();
        to = result.getTo();
        result = getPreviousTransition(result.getTime(), false);
        return result;
    }

    public Object clone() {
        if (isFrozen()) {
            return this;
        }
        return cloneAsThawed();
    }

    private void complete() {
        if (!this.upToDate) {
            if (this.finalRules == null || this.finalRules[1] != null) {
                boolean z;
                if (this.historicRules == null && this.finalRules == null) {
                    z = true;
                } else {
                    long tt;
                    TimeZoneRule curRule = this.initialRule;
                    long lastTransitionTime = Grego.MIN_MILLIS;
                    if (this.historicRules != null) {
                        BitSet done = new BitSet(this.historicRules.size());
                        while (true) {
                            int i;
                            int curStdOffset = curRule.getRawOffset();
                            int curDstSavings = curRule.getDSTSavings();
                            long nextTransitionTime = Grego.MAX_MILLIS;
                            TimeZoneRule nextRule = null;
                            int i2 = 0;
                            while (true) {
                                int i3 = i2;
                                if (i3 >= this.historicRules.size()) {
                                    break;
                                }
                                if (done.get(i3)) {
                                    i = i3;
                                } else {
                                    TimeZoneRule r = (TimeZoneRule) this.historicRules.get(i3);
                                    TimeZoneRule nextRule2 = r;
                                    i = i3;
                                    Date d = r.getNextStart(lastTransitionTime, curStdOffset, curDstSavings, 0);
                                    if (d == null) {
                                        done.set(i);
                                    } else if (!(nextRule2 == curRule || (nextRule2.getName().equals(curRule.getName()) && nextRule2.getRawOffset() == curRule.getRawOffset() && nextRule2.getDSTSavings() == curRule.getDSTSavings()))) {
                                        tt = d.getTime();
                                        if (tt < nextTransitionTime) {
                                            nextRule = nextRule2;
                                            nextTransitionTime = tt;
                                        }
                                    }
                                }
                                i2 = i + 1;
                            }
                            if (nextRule == null) {
                                boolean bDoneAll = true;
                                for (i = 0; i < this.historicRules.size(); i++) {
                                    if (!done.get(i)) {
                                        bDoneAll = false;
                                        break;
                                    }
                                }
                                if (bDoneAll) {
                                    break;
                                }
                            }
                            if (this.finalRules != null) {
                                for (int i4 = 0; i4 < 2; i4++) {
                                    if (this.finalRules[i4] != curRule) {
                                        Date d2 = this.finalRules[i4].getNextStart(lastTransitionTime, curStdOffset, curDstSavings, false);
                                        if (d2 != null) {
                                            long tt2 = d2.getTime();
                                            if (tt2 < nextTransitionTime) {
                                                nextTransitionTime = tt2;
                                                nextRule = this.finalRules[i4];
                                            }
                                        }
                                    }
                                }
                            }
                            long nextTransitionTime2 = nextTransitionTime;
                            if (nextRule == null) {
                                break;
                            }
                            if (this.historicTransitions == null) {
                                this.historicTransitions = new ArrayList();
                            }
                            this.historicTransitions.add(new TimeZoneTransition(nextTransitionTime2, curRule, nextRule));
                            lastTransitionTime = nextTransitionTime2;
                            curRule = nextRule;
                        }
                    }
                    tt = lastTransitionTime;
                    if (this.finalRules != null) {
                        if (this.historicTransitions == null) {
                            this.historicTransitions = new ArrayList();
                        }
                        Date d0 = this.finalRules[0].getNextStart(tt, curRule.getRawOffset(), curRule.getDSTSavings(), false);
                        Date d1 = this.finalRules[1].getNextStart(tt, curRule.getRawOffset(), curRule.getDSTSavings(), false);
                        if (d1.after(d0)) {
                            this.historicTransitions.add(new TimeZoneTransition(d0.getTime(), curRule, this.finalRules[0]));
                            this.historicTransitions.add(new TimeZoneTransition(this.finalRules[1].getNextStart(d0.getTime(), this.finalRules[0].getRawOffset(), this.finalRules[0].getDSTSavings(), false).getTime(), this.finalRules[0], this.finalRules[1]));
                            z = true;
                        } else {
                            this.historicTransitions.add(new TimeZoneTransition(d1.getTime(), curRule, this.finalRules[1]));
                            z = true;
                            this.historicTransitions.add(new TimeZoneTransition(this.finalRules[0].getNextStart(d1.getTime(), this.finalRules[1].getRawOffset(), this.finalRules[1].getDSTSavings(), false).getTime(), this.finalRules[1], this.finalRules[0]));
                        }
                    } else {
                        z = true;
                    }
                }
                this.upToDate = z;
                return;
            }
            throw new IllegalStateException("Incomplete final rules");
        }
    }

    private void getOffset(long time, boolean local, int NonExistingTimeOpt, int DuplicatedTimeOpt, int[] offsets) {
        boolean z = local;
        int i = NonExistingTimeOpt;
        int i2 = DuplicatedTimeOpt;
        complete();
        TimeZoneRule rule = null;
        if (this.historicTransitions == null) {
            rule = this.initialRule;
        } else if (time < getTransitionTime((TimeZoneTransition) this.historicTransitions.get(0), z, i, i2)) {
            rule = this.initialRule;
        } else {
            int idx = this.historicTransitions.size() - 1;
            if (time > getTransitionTime((TimeZoneTransition) this.historicTransitions.get(idx), z, i, i2)) {
                if (this.finalRules != null) {
                    rule = findRuleInFinal(time, local, NonExistingTimeOpt, DuplicatedTimeOpt);
                }
                if (rule == null) {
                    rule = ((TimeZoneTransition) this.historicTransitions.get(idx)).getTo();
                }
            } else {
                while (idx >= 0 && time < getTransitionTime((TimeZoneTransition) this.historicTransitions.get(idx), z, i, i2)) {
                    idx--;
                }
                rule = ((TimeZoneTransition) this.historicTransitions.get(idx)).getTo();
            }
        }
        offsets[0] = rule.getRawOffset();
        offsets[1] = rule.getDSTSavings();
    }

    private TimeZoneRule findRuleInFinal(long time, boolean local, int NonExistingTimeOpt, int DuplicatedTimeOpt) {
        if (this.finalRules == null || this.finalRules.length != 2 || this.finalRules[0] == null || this.finalRules[1] == null) {
            return null;
        }
        long base = time;
        if (local) {
            base -= (long) getLocalDelta(this.finalRules[1].getRawOffset(), this.finalRules[1].getDSTSavings(), this.finalRules[0].getRawOffset(), this.finalRules[0].getDSTSavings(), NonExistingTimeOpt, DuplicatedTimeOpt);
        }
        Date start0 = this.finalRules[0].getPreviousStart(base, this.finalRules[1].getRawOffset(), this.finalRules[1].getDSTSavings(), true);
        base = time;
        if (local) {
            base -= (long) getLocalDelta(this.finalRules[0].getRawOffset(), this.finalRules[0].getDSTSavings(), this.finalRules[1].getRawOffset(), this.finalRules[1].getDSTSavings(), NonExistingTimeOpt, DuplicatedTimeOpt);
        }
        Date start1 = this.finalRules[1].getPreviousStart(base, this.finalRules[0].getRawOffset(), this.finalRules[0].getDSTSavings(), true);
        if (start0 != null && start1 != null) {
            return start0.after(start1) ? this.finalRules[0] : this.finalRules[1];
        } else if (start0 != null) {
            return this.finalRules[0];
        } else {
            if (start1 != null) {
                return this.finalRules[1];
            }
            return null;
        }
    }

    private static long getTransitionTime(TimeZoneTransition tzt, boolean local, int NonExistingTimeOpt, int DuplicatedTimeOpt) {
        long time = tzt.getTime();
        if (local) {
            return time + ((long) getLocalDelta(tzt.getFrom().getRawOffset(), tzt.getFrom().getDSTSavings(), tzt.getTo().getRawOffset(), tzt.getTo().getDSTSavings(), NonExistingTimeOpt, DuplicatedTimeOpt));
        }
        return time;
    }

    private static int getLocalDelta(int rawBefore, int dstBefore, int rawAfter, int dstAfter, int NonExistingTimeOpt, int DuplicatedTimeOpt) {
        int offsetBefore = rawBefore + dstBefore;
        int offsetAfter = rawAfter + dstAfter;
        boolean stdToDst = false;
        boolean dstToStd = dstBefore != 0 && dstAfter == 0;
        if (dstBefore == 0 && dstAfter != 0) {
            stdToDst = true;
        }
        if (offsetAfter - offsetBefore >= 0) {
            if (((NonExistingTimeOpt & 3) == 1 && dstToStd) || ((NonExistingTimeOpt & 3) == 3 && stdToDst)) {
                return offsetBefore;
            }
            if (((NonExistingTimeOpt & 3) == 1 && stdToDst) || ((NonExistingTimeOpt & 3) == 3 && dstToStd)) {
                return offsetAfter;
            }
            if ((NonExistingTimeOpt & 12) == 12) {
                return offsetBefore;
            }
            return offsetAfter;
        } else if (((DuplicatedTimeOpt & 3) == 1 && dstToStd) || ((DuplicatedTimeOpt & 3) == 3 && stdToDst)) {
            return offsetAfter;
        } else {
            if (((DuplicatedTimeOpt & 3) == 1 && stdToDst) || ((DuplicatedTimeOpt & 3) == 3 && dstToStd)) {
                return offsetBefore;
            }
            if ((DuplicatedTimeOpt & 12) == 4) {
                return offsetBefore;
            }
            return offsetAfter;
        }
    }

    public boolean isFrozen() {
        return this.isFrozen;
    }

    public TimeZone freeze() {
        complete();
        this.isFrozen = true;
        return this;
    }

    public TimeZone cloneAsThawed() {
        RuleBasedTimeZone tz = (RuleBasedTimeZone) super.cloneAsThawed();
        if (this.historicRules != null) {
            tz.historicRules = new ArrayList(this.historicRules);
        }
        if (this.finalRules != null) {
            tz.finalRules = (AnnualTimeZoneRule[]) this.finalRules.clone();
        }
        tz.isFrozen = false;
        return tz;
    }
}
