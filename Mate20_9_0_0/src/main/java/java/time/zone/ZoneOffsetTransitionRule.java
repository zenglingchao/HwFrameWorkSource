package java.time.zone;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.chrono.IsoChronology;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

public final class ZoneOffsetTransitionRule implements Serializable {
    private static final long serialVersionUID = 6889046316657758795L;
    private final byte dom;
    private final DayOfWeek dow;
    private final Month month;
    private final ZoneOffset offsetAfter;
    private final ZoneOffset offsetBefore;
    private final ZoneOffset standardOffset;
    private final LocalTime time;
    private final TimeDefinition timeDefinition;
    private final boolean timeEndOfDay;

    public enum TimeDefinition {
        UTC,
        WALL,
        STANDARD;

        public LocalDateTime createDateTime(LocalDateTime dateTime, ZoneOffset standardOffset, ZoneOffset wallOffset) {
            switch (this) {
                case UTC:
                    return dateTime.plusSeconds((long) (wallOffset.getTotalSeconds() - ZoneOffset.UTC.getTotalSeconds()));
                case STANDARD:
                    return dateTime.plusSeconds((long) (wallOffset.getTotalSeconds() - standardOffset.getTotalSeconds()));
                default:
                    return dateTime;
            }
        }
    }

    public static ZoneOffsetTransitionRule of(Month month, int dayOfMonthIndicator, DayOfWeek dayOfWeek, LocalTime time, boolean timeEndOfDay, TimeDefinition timeDefnition, ZoneOffset standardOffset, ZoneOffset offsetBefore, ZoneOffset offsetAfter) {
        int i = dayOfMonthIndicator;
        LocalTime localTime = time;
        Month month2 = month;
        Objects.requireNonNull((Object) month2, "month");
        Objects.requireNonNull((Object) localTime, "time");
        TimeDefinition timeDefinition = timeDefnition;
        Objects.requireNonNull((Object) timeDefinition, "timeDefnition");
        ZoneOffset zoneOffset = standardOffset;
        Objects.requireNonNull((Object) zoneOffset, "standardOffset");
        ZoneOffset zoneOffset2 = offsetBefore;
        Objects.requireNonNull((Object) zoneOffset2, "offsetBefore");
        Objects.requireNonNull((Object) offsetAfter, "offsetAfter");
        if (i < -28 || i > 31 || i == 0) {
            throw new IllegalArgumentException("Day of month indicator must be between -28 and 31 inclusive excluding zero");
        } else if (!timeEndOfDay || localTime.equals(LocalTime.MIDNIGHT)) {
            return new ZoneOffsetTransitionRule(month2, i, dayOfWeek, localTime, timeEndOfDay, timeDefinition, zoneOffset, zoneOffset2, offsetAfter);
        } else {
            throw new IllegalArgumentException("Time must be midnight when end of day flag is true");
        }
    }

    ZoneOffsetTransitionRule(Month month, int dayOfMonthIndicator, DayOfWeek dayOfWeek, LocalTime time, boolean timeEndOfDay, TimeDefinition timeDefnition, ZoneOffset standardOffset, ZoneOffset offsetBefore, ZoneOffset offsetAfter) {
        this.month = month;
        this.dom = (byte) dayOfMonthIndicator;
        this.dow = dayOfWeek;
        this.time = time;
        this.timeEndOfDay = timeEndOfDay;
        this.timeDefinition = timeDefnition;
        this.standardOffset = standardOffset;
        this.offsetBefore = offsetBefore;
        this.offsetAfter = offsetAfter;
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    private Object writeReplace() {
        return new Ser((byte) 3, this);
    }

    void writeExternal(DataOutput out) throws IOException {
        int timeSecs = this.timeEndOfDay ? 86400 : this.time.toSecondOfDay();
        int stdOffset = this.standardOffset.getTotalSeconds();
        int beforeDiff = this.offsetBefore.getTotalSeconds() - stdOffset;
        int afterDiff = this.offsetAfter.getTotalSeconds() - stdOffset;
        int timeByte = timeSecs % 3600 == 0 ? this.timeEndOfDay ? 24 : this.time.getHour() : 31;
        int stdOffsetByte = stdOffset % 900 == 0 ? (stdOffset / 900) + 128 : 255;
        int beforeByte = (beforeDiff == 0 || beforeDiff == 1800 || beforeDiff == 3600) ? beforeDiff / 1800 : 3;
        int afterByte = (afterDiff == 0 || afterDiff == 1800 || afterDiff == 3600) ? afterDiff / 1800 : 3;
        out.writeInt((((((((this.month.getValue() << 28) + ((this.dom + 32) << 22)) + ((this.dow == null ? 0 : this.dow.getValue()) << 19)) + (timeByte << 14)) + (this.timeDefinition.ordinal() << 12)) + (stdOffsetByte << 4)) + (beforeByte << 2)) + afterByte);
        if (timeByte == 31) {
            out.writeInt(timeSecs);
        }
        if (stdOffsetByte == 255) {
            out.writeInt(stdOffset);
        }
        if (beforeByte == 3) {
            out.writeInt(this.offsetBefore.getTotalSeconds());
        }
        if (afterByte == 3) {
            out.writeInt(this.offsetAfter.getTotalSeconds());
        }
    }

    static ZoneOffsetTransitionRule readExternal(DataInput in) throws IOException {
        int data = in.readInt();
        Month month = Month.of(data >>> 28);
        int dom = ((264241152 & data) >>> 22) - 32;
        int dowByte = (3670016 & data) >>> 19;
        DayOfWeek dow = dowByte == 0 ? null : DayOfWeek.of(dowByte);
        int timeByte = (507904 & data) >>> 14;
        TimeDefinition defn = TimeDefinition.values()[(data & 12288) >>> 12];
        int stdByte = (data & 4080) >>> 4;
        int beforeByte = (data & 12) >>> 2;
        int afterByte = data & 3;
        LocalTime time = timeByte == 31 ? LocalTime.ofSecondOfDay((long) in.readInt()) : LocalTime.of(timeByte % 24, 0);
        ZoneOffset std = ZoneOffset.ofTotalSeconds(stdByte == 255 ? in.readInt() : (stdByte - 128) * 900);
        return of(month, dom, dow, time, timeByte == 24, defn, std, ZoneOffset.ofTotalSeconds(beforeByte == 3 ? in.readInt() : std.getTotalSeconds() + (beforeByte * 1800)), ZoneOffset.ofTotalSeconds(afterByte == 3 ? in.readInt() : std.getTotalSeconds() + (afterByte * 1800)));
    }

    public Month getMonth() {
        return this.month;
    }

    public int getDayOfMonthIndicator() {
        return this.dom;
    }

    public DayOfWeek getDayOfWeek() {
        return this.dow;
    }

    public LocalTime getLocalTime() {
        return this.time;
    }

    public boolean isMidnightEndOfDay() {
        return this.timeEndOfDay;
    }

    public TimeDefinition getTimeDefinition() {
        return this.timeDefinition;
    }

    public ZoneOffset getStandardOffset() {
        return this.standardOffset;
    }

    public ZoneOffset getOffsetBefore() {
        return this.offsetBefore;
    }

    public ZoneOffset getOffsetAfter() {
        return this.offsetAfter;
    }

    public ZoneOffsetTransition createTransition(int year) {
        LocalDate date;
        if (this.dom < (byte) 0) {
            date = LocalDate.of(year, this.month, (this.month.length(IsoChronology.INSTANCE.isLeapYear((long) year)) + 1) + this.dom);
            if (this.dow != null) {
                date = date.with(TemporalAdjusters.previousOrSame(this.dow));
            }
        } else {
            date = LocalDate.of(year, this.month, this.dom);
            if (this.dow != null) {
                date = date.with(TemporalAdjusters.nextOrSame(this.dow));
            }
        }
        if (this.timeEndOfDay) {
            date = date.plusDays(1);
        }
        return new ZoneOffsetTransition(this.timeDefinition.createDateTime(LocalDateTime.of(date, this.time), this.standardOffset, this.offsetBefore), this.offsetBefore, this.offsetAfter);
    }

    public boolean equals(Object otherRule) {
        boolean z = true;
        if (otherRule == this) {
            return true;
        }
        if (!(otherRule instanceof ZoneOffsetTransitionRule)) {
            return false;
        }
        ZoneOffsetTransitionRule other = (ZoneOffsetTransitionRule) otherRule;
        if (!(this.month == other.month && this.dom == other.dom && this.dow == other.dow && this.timeDefinition == other.timeDefinition && this.time.equals(other.time) && this.timeEndOfDay == other.timeEndOfDay && this.standardOffset.equals(other.standardOffset) && this.offsetBefore.equals(other.offsetBefore) && this.offsetAfter.equals(other.offsetAfter))) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return ((this.standardOffset.hashCode() ^ ((((((this.time.toSecondOfDay() + this.timeEndOfDay) << 15) + (this.month.ordinal() << 11)) + ((this.dom + 32) << 5)) + ((this.dow == null ? 7 : this.dow.ordinal()) << 2)) + this.timeDefinition.ordinal())) ^ this.offsetBefore.hashCode()) ^ this.offsetAfter.hashCode();
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("TransitionRule[");
        buf.append(this.offsetBefore.compareTo(this.offsetAfter) > 0 ? "Gap " : "Overlap ");
        buf.append(this.offsetBefore);
        buf.append(" to ");
        buf.append(this.offsetAfter);
        buf.append(", ");
        if (this.dow == null) {
            buf.append(this.month.name());
            buf.append(' ');
            buf.append(this.dom);
        } else if (this.dom == (byte) -1) {
            buf.append(this.dow.name());
            buf.append(" on or before last day of ");
            buf.append(this.month.name());
        } else if (this.dom < (byte) 0) {
            buf.append(this.dow.name());
            buf.append(" on or before last day minus ");
            buf.append((-this.dom) - 1);
            buf.append(" of ");
            buf.append(this.month.name());
        } else {
            buf.append(this.dow.name());
            buf.append(" on or after ");
            buf.append(this.month.name());
            buf.append(' ');
            buf.append(this.dom);
        }
        buf.append(" at ");
        buf.append(this.timeEndOfDay ? "24:00" : this.time.toString());
        buf.append(" ");
        buf.append(this.timeDefinition);
        buf.append(", standard offset ");
        buf.append(this.standardOffset);
        buf.append(']');
        return buf.toString();
    }
}
