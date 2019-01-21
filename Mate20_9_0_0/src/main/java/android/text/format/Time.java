package android.text.format;

import android.telephony.NetworkScanRequest;
import android.util.JlogConstants;
import android.util.TimeFormatException;
import android.view.WindowManager.LayoutParams;
import java.io.IOException;
import java.util.Locale;
import java.util.TimeZone;
import libcore.util.ZoneInfo;
import libcore.util.ZoneInfo.WallTime;
import libcore.util.ZoneInfoDB;

@Deprecated
public class Time {
    private static final int[] DAYS_PER_MONTH = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    public static final int EPOCH_JULIAN_DAY = 2440588;
    public static final int FRIDAY = 5;
    public static final int HOUR = 3;
    public static final int MINUTE = 2;
    public static final int MONDAY = 1;
    public static final int MONDAY_BEFORE_JULIAN_EPOCH = 2440585;
    public static final int MONTH = 5;
    public static final int MONTH_DAY = 4;
    public static final int SATURDAY = 6;
    public static final int SECOND = 1;
    public static final int SUNDAY = 0;
    public static final int THURSDAY = 4;
    public static final String TIMEZONE_UTC = "UTC";
    public static final int TUESDAY = 2;
    public static final int WEDNESDAY = 3;
    public static final int WEEK_DAY = 7;
    public static final int WEEK_NUM = 9;
    public static final int YEAR = 6;
    public static final int YEAR_DAY = 8;
    private static final String Y_M_D = "%Y-%m-%d";
    private static final String Y_M_D_T_H_M_S_000 = "%Y-%m-%dT%H:%M:%S.000";
    private static final String Y_M_D_T_H_M_S_000_Z = "%Y-%m-%dT%H:%M:%S.000Z";
    private static final int[] sThursdayOffset = new int[]{-3, 3, 2, 1, 0, -1, -2};
    public boolean allDay;
    private TimeCalculator calculator;
    public long gmtoff;
    public int hour;
    public int isDst;
    public int minute;
    public int month;
    public int monthDay;
    public int second;
    public String timezone;
    public int weekDay;
    public int year;
    public int yearDay;

    private static class TimeCalculator {
        public String timezone;
        public final WallTime wallTime = new WallTime();
        private ZoneInfo zoneInfo;

        public TimeCalculator(String timezoneId) {
            this.zoneInfo = lookupZoneInfo(timezoneId);
        }

        public long toMillis(boolean ignoreDst) {
            if (ignoreDst) {
                this.wallTime.setIsDst(-1);
            }
            int r = this.wallTime.mktime(this.zoneInfo);
            if (r == -1) {
                return -1;
            }
            return ((long) r) * 1000;
        }

        public void setTimeInMillis(long millis) {
            int intSeconds = (int) (millis / 1000);
            updateZoneInfoFromTimeZone();
            this.wallTime.localtime(intSeconds, this.zoneInfo);
        }

        public String format(String format) {
            if (format == null) {
                format = "%c";
            }
            return new TimeFormatter().format(format, this.wallTime, this.zoneInfo);
        }

        private void updateZoneInfoFromTimeZone() {
            if (!this.zoneInfo.getID().equals(this.timezone)) {
                this.zoneInfo = lookupZoneInfo(this.timezone);
            }
        }

        private static ZoneInfo lookupZoneInfo(String timezoneId) {
            StringBuilder stringBuilder;
            try {
                ZoneInfo zoneInfo = ZoneInfoDB.getInstance().makeTimeZone(timezoneId);
                if (zoneInfo == null) {
                    zoneInfo = ZoneInfoDB.getInstance().makeTimeZone("GMT");
                }
                if (zoneInfo != null) {
                    return zoneInfo;
                }
                stringBuilder = new StringBuilder();
                stringBuilder.append("GMT not found: \"");
                stringBuilder.append(timezoneId);
                stringBuilder.append("\"");
                throw new AssertionError(stringBuilder.toString());
            } catch (IOException e) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("Error loading timezone: \"");
                stringBuilder.append(timezoneId);
                stringBuilder.append("\"");
                throw new AssertionError(stringBuilder.toString(), e);
            }
        }

        public void switchTimeZone(String timezone) {
            int seconds = this.wallTime.mktime(this.zoneInfo);
            this.timezone = timezone;
            updateZoneInfoFromTimeZone();
            this.wallTime.localtime(seconds, this.zoneInfo);
        }

        public String format2445(boolean hasTime) {
            char[] buf = new char[(hasTime ? 16 : 8)];
            int n = this.wallTime.getYear();
            buf[0] = toChar(n / 1000);
            n %= 1000;
            buf[1] = toChar(n / 100);
            n %= 100;
            buf[2] = toChar(n / 10);
            buf[3] = toChar(n % 10);
            int n2 = this.wallTime.getMonth() + 1;
            buf[4] = toChar(n2 / 10);
            buf[5] = toChar(n2 % 10);
            n = this.wallTime.getMonthDay();
            buf[6] = toChar(n / 10);
            buf[7] = toChar(n % 10);
            if (!hasTime) {
                return new String(buf, 0, 8);
            }
            buf[8] = 'T';
            int n3 = this.wallTime.getHour();
            buf[9] = toChar(n3 / 10);
            buf[10] = toChar(n3 % 10);
            n3 = this.wallTime.getMinute();
            buf[11] = toChar(n3 / 10);
            buf[12] = toChar(n3 % 10);
            n3 = this.wallTime.getSecond();
            buf[13] = toChar(n3 / 10);
            buf[14] = toChar(n3 % 10);
            if (!Time.TIMEZONE_UTC.equals(this.timezone)) {
                return new String(buf, 0, 15);
            }
            buf[15] = 'Z';
            return new String(buf, 0, 16);
        }

        private char toChar(int n) {
            return (n < 0 || n > 9) ? ' ' : (char) (n + 48);
        }

        public String toStringInternal() {
            return String.format("%04d%02d%02dT%02d%02d%02d%s(%d,%d,%d,%d,%d)", new Object[]{Integer.valueOf(this.wallTime.getYear()), Integer.valueOf(this.wallTime.getMonth() + 1), Integer.valueOf(this.wallTime.getMonthDay()), Integer.valueOf(this.wallTime.getHour()), Integer.valueOf(this.wallTime.getMinute()), Integer.valueOf(this.wallTime.getSecond()), this.timezone, Integer.valueOf(this.wallTime.getWeekDay()), Integer.valueOf(this.wallTime.getYearDay()), Integer.valueOf(this.wallTime.getGmtOffset()), Integer.valueOf(this.wallTime.getIsDst()), Long.valueOf(toMillis(false) / 1000)});
        }

        public static int compare(TimeCalculator aObject, TimeCalculator bObject) {
            int i = 0;
            if (aObject.timezone.equals(bObject.timezone)) {
                int diff = aObject.wallTime.getYear() - bObject.wallTime.getYear();
                if (diff != 0) {
                    return diff;
                }
                int diff2 = aObject.wallTime.getMonth() - bObject.wallTime.getMonth();
                if (diff2 != 0) {
                    return diff2;
                }
                diff = aObject.wallTime.getMonthDay() - bObject.wallTime.getMonthDay();
                if (diff != 0) {
                    return diff;
                }
                diff2 = aObject.wallTime.getHour() - bObject.wallTime.getHour();
                if (diff2 != 0) {
                    return diff2;
                }
                diff = aObject.wallTime.getMinute() - bObject.wallTime.getMinute();
                if (diff != 0) {
                    return diff;
                }
                diff2 = aObject.wallTime.getSecond() - bObject.wallTime.getSecond();
                if (diff2 != 0) {
                    return diff2;
                }
                return 0;
            }
            long diff3 = aObject.toMillis(false) - bObject.toMillis(false);
            if (diff3 < 0) {
                i = -1;
            } else if (diff3 > 0) {
                i = 1;
            }
            return i;
        }

        public void copyFieldsToTime(Time time) {
            time.second = this.wallTime.getSecond();
            time.minute = this.wallTime.getMinute();
            time.hour = this.wallTime.getHour();
            time.monthDay = this.wallTime.getMonthDay();
            time.month = this.wallTime.getMonth();
            time.year = this.wallTime.getYear();
            time.weekDay = this.wallTime.getWeekDay();
            time.yearDay = this.wallTime.getYearDay();
            time.isDst = this.wallTime.getIsDst();
            time.gmtoff = (long) this.wallTime.getGmtOffset();
        }

        public void copyFieldsFromTime(Time time) {
            this.wallTime.setSecond(time.second);
            this.wallTime.setMinute(time.minute);
            this.wallTime.setHour(time.hour);
            this.wallTime.setMonthDay(time.monthDay);
            this.wallTime.setMonth(time.month);
            this.wallTime.setYear(time.year);
            this.wallTime.setWeekDay(time.weekDay);
            this.wallTime.setYearDay(time.yearDay);
            this.wallTime.setIsDst(time.isDst);
            this.wallTime.setGmtOffset((int) time.gmtoff);
            if (!time.allDay || (time.second == 0 && time.minute == 0 && time.hour == 0)) {
                this.timezone = time.timezone;
                updateZoneInfoFromTimeZone();
                return;
            }
            throw new IllegalArgumentException("allDay is true but sec, min, hour are not 0.");
        }
    }

    public Time(String timezoneId) {
        if (timezoneId != null) {
            initialize(timezoneId);
            return;
        }
        throw new NullPointerException("timezoneId is null!");
    }

    public Time() {
        initialize(TimeZone.getDefault().getID());
    }

    public Time(Time other) {
        initialize(other.timezone);
        set(other);
    }

    private void initialize(String timezoneId) {
        this.timezone = timezoneId;
        this.year = 1970;
        this.monthDay = 1;
        this.isDst = -1;
        this.calculator = new TimeCalculator(timezoneId);
    }

    public long normalize(boolean ignoreDst) {
        this.calculator.copyFieldsFromTime(this);
        long timeInMillis = this.calculator.toMillis(ignoreDst);
        this.calculator.copyFieldsToTime(this);
        return timeInMillis;
    }

    public void switchTimezone(String timezone) {
        this.calculator.copyFieldsFromTime(this);
        this.calculator.switchTimeZone(timezone);
        this.calculator.copyFieldsToTime(this);
        this.timezone = timezone;
    }

    public int getActualMaximum(int field) {
        int n;
        int i;
        switch (field) {
            case 1:
                return 59;
            case 2:
                return 59;
            case 3:
                return 23;
            case 4:
                n = DAYS_PER_MONTH[this.month];
                i = 28;
                if (n != 28) {
                    return n;
                }
                int y = this.year;
                if (y % 4 == 0 && (y % 100 != 0 || y % 400 == 0)) {
                    i = 29;
                }
                return i;
            case 5:
                return 11;
            case 6:
                return LayoutParams.TYPE_PRESENTATION;
            case 7:
                return 6;
            case 8:
                n = this.year;
                i = (n % 4 != 0 || (n % 100 == 0 && n % 400 != 0)) ? 364 : JlogConstants.JLID_APP_FRONZED_BEGIN;
                return i;
            case 9:
                throw new RuntimeException("WEEK_NUM not implemented");
            default:
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("bad field=");
                stringBuilder.append(field);
                throw new RuntimeException(stringBuilder.toString());
        }
    }

    public void clear(String timezoneId) {
        if (timezoneId != null) {
            this.timezone = timezoneId;
            this.allDay = false;
            this.second = 0;
            this.minute = 0;
            this.hour = 0;
            this.monthDay = 0;
            this.month = 0;
            this.year = 0;
            this.weekDay = 0;
            this.yearDay = 0;
            this.gmtoff = 0;
            this.isDst = -1;
            return;
        }
        throw new NullPointerException("timezone is null!");
    }

    public static int compare(Time a, Time b) {
        if (a == null) {
            throw new NullPointerException("a == null");
        } else if (b != null) {
            a.calculator.copyFieldsFromTime(a);
            b.calculator.copyFieldsFromTime(b);
            return TimeCalculator.compare(a.calculator, b.calculator);
        } else {
            throw new NullPointerException("b == null");
        }
    }

    public String format(String format) {
        this.calculator.copyFieldsFromTime(this);
        return this.calculator.format(format);
    }

    public String toString() {
        TimeCalculator calculator = new TimeCalculator(this.timezone);
        calculator.copyFieldsFromTime(this);
        return calculator.toStringInternal();
    }

    public boolean parse(String s) {
        if (s == null) {
            throw new NullPointerException("time string is null");
        } else if (!parseInternal(s)) {
            return false;
        } else {
            this.timezone = TIMEZONE_UTC;
            return true;
        }
    }

    private boolean parseInternal(String s) {
        int len = s.length();
        if (len >= 8) {
            boolean inUtc = false;
            this.year = ((getChar(s, 0, 1000) + getChar(s, 1, 100)) + getChar(s, 2, 10)) + getChar(s, 3, 1);
            this.month = (getChar(s, 4, 10) + getChar(s, 5, 1)) - 1;
            this.monthDay = getChar(s, 6, 10) + getChar(s, 7, 1);
            if (len <= 8) {
                this.allDay = true;
                this.hour = 0;
                this.minute = 0;
                this.second = 0;
            } else if (len >= 15) {
                checkChar(s, 8, 'T');
                this.allDay = false;
                this.hour = getChar(s, 9, 10) + getChar(s, 10, 1);
                this.minute = getChar(s, 11, 10) + getChar(s, 12, 1);
                this.second = getChar(s, 14, 1) + getChar(s, 13, 10);
                if (len > 15) {
                    checkChar(s, 15, 'Z');
                    inUtc = true;
                }
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("String is too short: \"");
                stringBuilder.append(s);
                stringBuilder.append("\" If there are more than 8 characters there must be at least 15.");
                throw new TimeFormatException(stringBuilder.toString());
            }
            this.weekDay = 0;
            this.yearDay = 0;
            this.isDst = -1;
            this.gmtoff = 0;
            return inUtc;
        }
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("String is too short: \"");
        stringBuilder2.append(s);
        stringBuilder2.append("\" Expected at least 8 characters.");
        throw new TimeFormatException(stringBuilder2.toString());
    }

    private void checkChar(String s, int spos, char expected) {
        if (s.charAt(spos) != expected) {
            throw new TimeFormatException(String.format("Unexpected character 0x%02d at pos=%d.  Expected 0x%02d ('%c').", new Object[]{Integer.valueOf(s.charAt(spos)), Integer.valueOf(spos), Integer.valueOf(expected), Character.valueOf(expected)}));
        }
    }

    private static int getChar(String s, int spos, int mul) {
        char c = s.charAt(spos);
        if (Character.isDigit(c)) {
            return Character.getNumericValue(c) * mul;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Parse error at pos=");
        stringBuilder.append(spos);
        throw new TimeFormatException(stringBuilder.toString());
    }

    public boolean parse3339(String s) {
        if (s == null) {
            throw new NullPointerException("time string is null");
        } else if (!parse3339Internal(s)) {
            return false;
        } else {
            this.timezone = TIMEZONE_UTC;
            return true;
        }
    }

    private boolean parse3339Internal(String s) {
        String str = s;
        int len = s.length();
        if (len >= 10) {
            boolean inUtc = false;
            this.year = ((getChar(str, 0, 1000) + getChar(str, 1, 100)) + getChar(str, 2, 10)) + getChar(str, 3, 1);
            checkChar(str, 4, '-');
            this.month = (getChar(str, 5, 10) + getChar(str, 6, 1)) - 1;
            checkChar(str, 7, '-');
            this.monthDay = getChar(str, 8, 10) + getChar(str, 9, 1);
            if (len >= 19) {
                checkChar(str, 10, 'T');
                this.allDay = false;
                int hour = getChar(str, 11, 10) + getChar(str, 12, 1);
                checkChar(str, 13, ':');
                int minute = getChar(str, 14, 10) + getChar(str, 15, 1);
                checkChar(str, 16, ':');
                this.second = getChar(str, 17, 10) + getChar(str, 18, 1);
                int tzIndex = 19;
                if (19 < len && str.charAt(19) == '.') {
                    do {
                        tzIndex++;
                        if (tzIndex >= len) {
                            break;
                        }
                    } while (Character.isDigit(str.charAt(tzIndex)));
                }
                int offset = 0;
                if (len > tzIndex) {
                    int offset2;
                    char c = str.charAt(tzIndex);
                    if (c == '+') {
                        offset2 = -1;
                    } else if (c == '-') {
                        offset2 = 1;
                    } else if (c == 'Z') {
                        offset2 = 0;
                    } else {
                        throw new TimeFormatException(String.format("Unexpected character 0x%02d at position %d.  Expected + or -", new Object[]{Integer.valueOf(c), Integer.valueOf(tzIndex)}));
                    }
                    offset = offset2;
                    inUtc = true;
                    if (offset != 0) {
                        if (len >= tzIndex + 6) {
                            hour += (getChar(str, tzIndex + 1, 10) + getChar(str, tzIndex + 2, 1)) * offset;
                            int n = (getChar(str, tzIndex + 4, 10) + getChar(str, tzIndex + 5, 1)) * offset;
                            minute += n;
                            int i = n;
                        } else {
                            throw new TimeFormatException(String.format("Unexpected length; should be %d characters", new Object[]{Integer.valueOf(tzIndex + 6)}));
                        }
                    }
                }
                this.hour = hour;
                this.minute = minute;
                if (offset != 0) {
                    normalize(false);
                }
            } else {
                this.allDay = true;
                this.hour = 0;
                this.minute = 0;
                this.second = 0;
            }
            this.weekDay = 0;
            this.yearDay = 0;
            this.isDst = -1;
            this.gmtoff = 0;
            return inUtc;
        }
        throw new TimeFormatException("String too short --- expected at least 10 characters.");
    }

    public static String getCurrentTimezone() {
        return TimeZone.getDefault().getID();
    }

    public void setToNow() {
        set(System.currentTimeMillis());
    }

    public long toMillis(boolean ignoreDst) {
        this.calculator.copyFieldsFromTime(this);
        return this.calculator.toMillis(ignoreDst);
    }

    public void set(long millis) {
        this.allDay = false;
        this.calculator.timezone = this.timezone;
        this.calculator.setTimeInMillis(millis);
        this.calculator.copyFieldsToTime(this);
    }

    public String format2445() {
        this.calculator.copyFieldsFromTime(this);
        return this.calculator.format2445(this.allDay ^ 1);
    }

    public void set(Time that) {
        this.timezone = that.timezone;
        this.allDay = that.allDay;
        this.second = that.second;
        this.minute = that.minute;
        this.hour = that.hour;
        this.monthDay = that.monthDay;
        this.month = that.month;
        this.year = that.year;
        this.weekDay = that.weekDay;
        this.yearDay = that.yearDay;
        this.isDst = that.isDst;
        this.gmtoff = that.gmtoff;
    }

    public void set(int second, int minute, int hour, int monthDay, int month, int year) {
        this.allDay = false;
        this.second = second;
        this.minute = minute;
        this.hour = hour;
        this.monthDay = monthDay;
        this.month = month;
        this.year = year;
        this.weekDay = 0;
        this.yearDay = 0;
        this.isDst = -1;
        this.gmtoff = 0;
    }

    public void set(int monthDay, int month, int year) {
        this.allDay = true;
        this.second = 0;
        this.minute = 0;
        this.hour = 0;
        this.monthDay = monthDay;
        this.month = month;
        this.year = year;
        this.weekDay = 0;
        this.yearDay = 0;
        this.isDst = -1;
        this.gmtoff = 0;
    }

    public boolean before(Time that) {
        return compare(this, that) < 0;
    }

    public boolean after(Time that) {
        return compare(this, that) > 0;
    }

    public int getWeekNumber() {
        int closestThursday = this.yearDay + sThursdayOffset[this.weekDay];
        if (closestThursday >= 0 && closestThursday <= 364) {
            return (closestThursday / 7) + 1;
        }
        Time temp = new Time(this);
        temp.monthDay += sThursdayOffset[this.weekDay];
        temp.normalize(true);
        return (temp.yearDay / 7) + 1;
    }

    public String format3339(boolean allDay) {
        if (allDay) {
            return format(Y_M_D);
        }
        if (TIMEZONE_UTC.equals(this.timezone)) {
            return format(Y_M_D_T_H_M_S_000_Z);
        }
        String base = format(Y_M_D_T_H_M_S_000);
        String sign = this.gmtoff < 0 ? "-" : "+";
        int offset = (int) Math.abs(this.gmtoff);
        int minutes = (offset % NetworkScanRequest.MAX_SEARCH_MAX_SEC) / 60;
        int hours = offset / NetworkScanRequest.MAX_SEARCH_MAX_SEC;
        return String.format(Locale.US, "%s%s%02d:%02d", new Object[]{base, sign, Integer.valueOf(hours), Integer.valueOf(minutes)});
    }

    public static boolean isEpoch(Time time) {
        if (getJulianDay(time.toMillis(true), 0) == EPOCH_JULIAN_DAY) {
            return true;
        }
        return false;
    }

    public static int getJulianDay(long millis, long gmtoff) {
        return ((int) ((millis + (1000 * gmtoff)) / DateUtils.DAY_IN_MILLIS)) + EPOCH_JULIAN_DAY;
    }

    public long setJulianDay(int julianDay) {
        long millis = ((long) (julianDay - EPOCH_JULIAN_DAY)) * DateUtils.DAY_IN_MILLIS;
        set(millis);
        this.monthDay += julianDay - getJulianDay(millis, this.gmtoff);
        this.hour = 0;
        this.minute = 0;
        this.second = 0;
        return normalize(true);
    }

    public static int getWeeksSinceEpochFromJulianDay(int julianDay, int firstDayOfWeek) {
        int diff = 4 - firstDayOfWeek;
        if (diff < 0) {
            diff += 7;
        }
        return (julianDay - (EPOCH_JULIAN_DAY - diff)) / 7;
    }

    public static int getJulianMondayFromWeeksSinceEpoch(int week) {
        return MONDAY_BEFORE_JULIAN_EPOCH + (week * 7);
    }
}
