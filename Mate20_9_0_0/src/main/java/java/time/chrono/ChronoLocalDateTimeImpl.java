package java.time.chrono;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.ValueRange;
import java.util.Objects;
import sun.security.x509.InvalidityDateExtension;

final class ChronoLocalDateTimeImpl<D extends ChronoLocalDate> implements ChronoLocalDateTime<D>, Temporal, TemporalAdjuster, Serializable {
    static final int HOURS_PER_DAY = 24;
    static final long MICROS_PER_DAY = 86400000000L;
    static final long MILLIS_PER_DAY = 86400000;
    static final int MINUTES_PER_DAY = 1440;
    static final int MINUTES_PER_HOUR = 60;
    static final long NANOS_PER_DAY = 86400000000000L;
    static final long NANOS_PER_HOUR = 3600000000000L;
    static final long NANOS_PER_MINUTE = 60000000000L;
    static final long NANOS_PER_SECOND = 1000000000;
    static final int SECONDS_PER_DAY = 86400;
    static final int SECONDS_PER_HOUR = 3600;
    static final int SECONDS_PER_MINUTE = 60;
    private static final long serialVersionUID = 4556003607393004514L;
    private final transient D date;
    private final transient LocalTime time;

    static <R extends ChronoLocalDate> ChronoLocalDateTimeImpl<R> of(R date, LocalTime time) {
        return new ChronoLocalDateTimeImpl(date, time);
    }

    static <R extends ChronoLocalDate> ChronoLocalDateTimeImpl<R> ensureValid(Chronology chrono, Temporal temporal) {
        ChronoLocalDateTimeImpl<R> other = (ChronoLocalDateTimeImpl) temporal;
        if (chrono.equals(other.getChronology())) {
            return other;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Chronology mismatch, required: ");
        stringBuilder.append(chrono.getId());
        stringBuilder.append(", actual: ");
        stringBuilder.append(other.getChronology().getId());
        throw new ClassCastException(stringBuilder.toString());
    }

    private ChronoLocalDateTimeImpl(D date, LocalTime time) {
        Objects.requireNonNull((Object) date, InvalidityDateExtension.DATE);
        Objects.requireNonNull((Object) time, "time");
        this.date = date;
        this.time = time;
    }

    private ChronoLocalDateTimeImpl<D> with(Temporal newDate, LocalTime newTime) {
        if (this.date == newDate && this.time == newTime) {
            return this;
        }
        return new ChronoLocalDateTimeImpl(ChronoLocalDateImpl.ensureValid(this.date.getChronology(), newDate), newTime);
    }

    public D toLocalDate() {
        return this.date;
    }

    public LocalTime toLocalTime() {
        return this.time;
    }

    public boolean isSupported(TemporalField field) {
        boolean z = true;
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            if (!(f.isDateBased() || f.isTimeBased())) {
                z = false;
            }
            return z;
        }
        if (field == null || !field.isSupportedBy(this)) {
            z = false;
        }
        return z;
    }

    public ValueRange range(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return field.rangeRefinedBy(this);
        }
        return ((ChronoField) field).isTimeBased() ? this.time.range(field) : this.date.range(field);
    }

    public int get(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return range(field).checkValidIntValue(getLong(field), field);
        }
        return ((ChronoField) field).isTimeBased() ? this.time.get(field) : this.date.get(field);
    }

    public long getLong(TemporalField field) {
        if (!(field instanceof ChronoField)) {
            return field.getFrom(this);
        }
        return ((ChronoField) field).isTimeBased() ? this.time.getLong(field) : this.date.getLong(field);
    }

    public ChronoLocalDateTimeImpl<D> with(TemporalAdjuster adjuster) {
        if (adjuster instanceof ChronoLocalDate) {
            return with((ChronoLocalDate) adjuster, this.time);
        }
        if (adjuster instanceof LocalTime) {
            return with(this.date, (LocalTime) adjuster);
        }
        if (adjuster instanceof ChronoLocalDateTimeImpl) {
            return ensureValid(this.date.getChronology(), (ChronoLocalDateTimeImpl) adjuster);
        }
        return ensureValid(this.date.getChronology(), (ChronoLocalDateTimeImpl) adjuster.adjustInto(this));
    }

    public ChronoLocalDateTimeImpl<D> with(TemporalField field, long newValue) {
        if (!(field instanceof ChronoField)) {
            return ensureValid(this.date.getChronology(), field.adjustInto(this, newValue));
        }
        if (((ChronoField) field).isTimeBased()) {
            return with(this.date, this.time.with(field, newValue));
        }
        return with(this.date.with(field, newValue), this.time);
    }

    public ChronoLocalDateTimeImpl<D> plus(long amountToAdd, TemporalUnit unit) {
        if (!(unit instanceof ChronoUnit)) {
            return ensureValid(this.date.getChronology(), unit.addTo(this, amountToAdd));
        }
        switch ((ChronoUnit) unit) {
            case NANOS:
                return plusNanos(amountToAdd);
            case MICROS:
                return plusDays(amountToAdd / MICROS_PER_DAY).plusNanos((amountToAdd % MICROS_PER_DAY) * 1000);
            case MILLIS:
                return plusDays(amountToAdd / MILLIS_PER_DAY).plusNanos((amountToAdd % MILLIS_PER_DAY) * 1000000);
            case SECONDS:
                return plusSeconds(amountToAdd);
            case MINUTES:
                return plusMinutes(amountToAdd);
            case HOURS:
                return plusHours(amountToAdd);
            case HALF_DAYS:
                return plusDays(amountToAdd / 256).plusHours((amountToAdd % 256) * 12);
            default:
                return with(this.date.plus(amountToAdd, unit), this.time);
        }
    }

    private ChronoLocalDateTimeImpl<D> plusDays(long days) {
        return with(this.date.plus(days, ChronoUnit.DAYS), this.time);
    }

    private ChronoLocalDateTimeImpl<D> plusHours(long hours) {
        return plusWithOverflow(this.date, hours, 0, 0, 0);
    }

    private ChronoLocalDateTimeImpl<D> plusMinutes(long minutes) {
        return plusWithOverflow(this.date, 0, minutes, 0, 0);
    }

    ChronoLocalDateTimeImpl<D> plusSeconds(long seconds) {
        return plusWithOverflow(this.date, 0, 0, seconds, 0);
    }

    private ChronoLocalDateTimeImpl<D> plusNanos(long nanos) {
        return plusWithOverflow(this.date, 0, 0, 0, nanos);
    }

    private ChronoLocalDateTimeImpl<D> plusWithOverflow(D newDate, long hours, long minutes, long seconds, long nanos) {
        Temporal temporal = newDate;
        if ((((hours | minutes) | seconds) | nanos) == 0) {
            return with(temporal, this.time);
        }
        LocalTime newTime;
        long totDays = (((nanos / NANOS_PER_DAY) + (seconds / 86400)) + (minutes / 1440)) + (hours / 24);
        long totNanos = (((nanos % NANOS_PER_DAY) + ((seconds % 86400) * NANOS_PER_SECOND)) + ((minutes % 1440) * NANOS_PER_MINUTE)) + ((hours % 24) * NANOS_PER_HOUR);
        long curNoD = this.time.toNanoOfDay();
        long totNanos2 = totNanos + curNoD;
        totDays += Math.floorDiv(totNanos2, (long) NANOS_PER_DAY);
        long newNoD = Math.floorMod(totNanos2, (long) NANOS_PER_DAY);
        if (newNoD == curNoD) {
            newTime = this.time;
        } else {
            newTime = LocalTime.ofNanoOfDay(newNoD);
        }
        return with(temporal.plus(totDays, ChronoUnit.DAYS), newTime);
    }

    public ChronoZonedDateTime<D> atZone(ZoneId zone) {
        return ChronoZonedDateTimeImpl.ofBest(this, zone, null);
    }

    public long until(Temporal endExclusive, TemporalUnit unit) {
        Objects.requireNonNull((Object) endExclusive, "endExclusive");
        ChronoLocalDateTime<D> end = getChronology().localDateTime(endExclusive);
        if (!(unit instanceof ChronoUnit)) {
            Objects.requireNonNull((Object) unit, "unit");
            return unit.between(this, end);
        } else if (unit.isTimeBased()) {
            long amount = end.getLong(ChronoField.EPOCH_DAY) - this.date.getLong(ChronoField.EPOCH_DAY);
            switch ((ChronoUnit) unit) {
                case NANOS:
                    amount = Math.multiplyExact(amount, (long) NANOS_PER_DAY);
                    break;
                case MICROS:
                    amount = Math.multiplyExact(amount, (long) MICROS_PER_DAY);
                    break;
                case MILLIS:
                    amount = Math.multiplyExact(amount, (long) MILLIS_PER_DAY);
                    break;
                case SECONDS:
                    amount = Math.multiplyExact(amount, 86400);
                    break;
                case MINUTES:
                    amount = Math.multiplyExact(amount, 1440);
                    break;
                case HOURS:
                    amount = Math.multiplyExact(amount, 24);
                    break;
                case HALF_DAYS:
                    amount = Math.multiplyExact(amount, 2);
                    break;
            }
            return Math.addExact(amount, this.time.until(end.toLocalTime(), unit));
        } else {
            ChronoLocalDate endDate = end.toLocalDate();
            if (end.toLocalTime().isBefore(this.time)) {
                endDate = endDate.minus(1, ChronoUnit.DAYS);
            }
            return this.date.until(endDate, unit);
        }
    }

    private Object writeReplace() {
        return new Ser((byte) 2, this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(this.date);
        out.writeObject(this.time);
    }

    static ChronoLocalDateTime<?> readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        return ((ChronoLocalDate) in.readObject()).atTime((LocalTime) in.readObject());
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ChronoLocalDateTime)) {
            return false;
        }
        if (compareTo((ChronoLocalDateTime) obj) != 0) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return toLocalDate().hashCode() ^ toLocalTime().hashCode();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(toLocalDate().toString());
        stringBuilder.append('T');
        stringBuilder.append(toLocalTime().toString());
        return stringBuilder.toString();
    }
}
