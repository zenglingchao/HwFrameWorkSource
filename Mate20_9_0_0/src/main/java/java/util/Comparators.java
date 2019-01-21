package java.util;

import java.io.Serializable;

class Comparators {

    static final class NullComparator<T> implements Comparator<T>, Serializable {
        private static final long serialVersionUID = -7569533591570686392L;
        private final boolean nullFirst;
        private final Comparator<T> real;

        NullComparator(boolean nullFirst, Comparator<? super T> real) {
            this.nullFirst = nullFirst;
            this.real = real;
        }

        public int compare(T a, T b) {
            int i = 1;
            int i2 = 0;
            if (a == null) {
                if (b == null) {
                    i = 0;
                } else if (this.nullFirst) {
                    i = -1;
                }
                return i;
            } else if (b == null) {
                if (!this.nullFirst) {
                    i = -1;
                }
                return i;
            } else {
                if (this.real != null) {
                    i2 = this.real.compare(a, b);
                }
                return i2;
            }
        }

        public Comparator<T> thenComparing(Comparator<? super T> other) {
            Objects.requireNonNull(other);
            return new NullComparator(this.nullFirst, this.real == null ? other : this.real.thenComparing((Comparator) other));
        }

        public Comparator<T> reversed() {
            return new NullComparator(this.nullFirst ^ 1, this.real == null ? null : this.real.reversed());
        }
    }

    enum NaturalOrderComparator implements Comparator<Comparable<Object>> {
        INSTANCE;

        public int compare(Comparable<Object> c1, Comparable<Object> c2) {
            return c1.compareTo(c2);
        }

        public Comparator<Comparable<Object>> reversed() {
            return Comparator.reverseOrder();
        }
    }

    private Comparators() {
        throw new AssertionError((Object) "no instances");
    }
}
