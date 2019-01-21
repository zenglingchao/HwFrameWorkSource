package android.icu.impl;

import android.icu.impl.TextTrieMap$Node.StepResult;
import android.icu.lang.UCharacter;
import android.icu.text.UTF16;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

public class TextTrieMap<V> {
    boolean _ignoreCase;
    private Node _root = new Node();

    public static class CharIterator implements Iterator<Character> {
        private boolean _ignoreCase;
        private int _nextIdx;
        private Character _remainingChar;
        private int _startIdx;
        private CharSequence _text;

        CharIterator(CharSequence text, int offset, boolean ignoreCase) {
            this._text = text;
            this._startIdx = offset;
            this._nextIdx = offset;
            this._ignoreCase = ignoreCase;
        }

        public boolean hasNext() {
            if (this._nextIdx == this._text.length() && this._remainingChar == null) {
                return false;
            }
            return true;
        }

        public Character next() {
            if (this._nextIdx == this._text.length() && this._remainingChar == null) {
                return null;
            }
            Character next;
            if (this._remainingChar != null) {
                next = this._remainingChar;
                this._remainingChar = null;
            } else if (this._ignoreCase) {
                int cp = UCharacter.foldCase(Character.codePointAt(this._text, this._nextIdx), true);
                this._nextIdx += Character.charCount(cp);
                char[] chars = Character.toChars(cp);
                Character next2 = Character.valueOf(chars[0]);
                if (chars.length == 2) {
                    this._remainingChar = Character.valueOf(chars[1]);
                }
                next = next2;
            } else {
                next = Character.valueOf(this._text.charAt(this._nextIdx));
                this._nextIdx++;
            }
            return next;
        }

        public void remove() {
            throw new UnsupportedOperationException("remove() not supproted");
        }

        public int nextIndex() {
            return this._nextIdx;
        }

        public int processedLength() {
            if (this._remainingChar == null) {
                return this._nextIdx - this._startIdx;
            }
            throw new IllegalStateException("In the middle of surrogate pair");
        }
    }

    private class Node {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private List<Node> _children;
        private char[] _text;
        private List<V> _values;

        public class StepResult {
            public Node node;
            public int offset;
        }

        static {
            Class cls = TextTrieMap.class;
        }

        private Node() {
        }

        private Node(char[] text, List<V> values, List<Node> children) {
            this._text = text;
            this._values = values;
            this._children = children;
        }

        public int charCount() {
            return this._text == null ? 0 : this._text.length;
        }

        public boolean hasChildFor(char ch) {
            int i = 0;
            while (this._children != null && i < this._children.size()) {
                Node child = (Node) this._children.get(i);
                if (ch < child._text[0]) {
                    break;
                } else if (ch == child._text[0]) {
                    return true;
                } else {
                    i++;
                }
            }
            return false;
        }

        public Iterator<V> values() {
            if (this._values == null) {
                return null;
            }
            return this._values.iterator();
        }

        public void add(CharIterator chitr, V value) {
            StringBuilder buf = new StringBuilder();
            while (chitr.hasNext()) {
                buf.append(chitr.next());
            }
            add(TextTrieMap.toCharArray(buf), 0, value);
        }

        public Node findMatch(CharIterator chitr) {
            if (this._children == null || !chitr.hasNext()) {
                return null;
            }
            Node match = null;
            Character ch = chitr.next();
            for (Node child : this._children) {
                if (ch.charValue() < child._text[0]) {
                    break;
                } else if (ch.charValue() == child._text[0]) {
                    if (child.matchFollowing(chitr)) {
                        match = child;
                    }
                }
            }
            return match;
        }

        public void takeStep(char ch, int offset, android.icu.impl.TextTrieMap$Node.StepResult result) {
            if (offset == charCount()) {
                int i = 0;
                while (this._children != null && i < this._children.size()) {
                    Node child = (Node) this._children.get(i);
                    if (ch < child._text[0]) {
                        break;
                    } else if (ch == child._text[0]) {
                        result.node = child;
                        result.offset = 1;
                        return;
                    } else {
                        i++;
                    }
                }
            } else if (this._text[offset] == ch) {
                result.node = this;
                result.offset = offset + 1;
                return;
            }
            result.node = null;
            result.offset = -1;
        }

        private void add(char[] text, int offset, V value) {
            if (text.length == offset) {
                this._values = addValue(this._values, value);
            } else if (this._children == null) {
                this._children = new LinkedList();
                this._children.add(new Node(TextTrieMap.subArray(text, offset), addValue(null, value), null));
            } else {
                ListIterator<Node> litr = this._children.listIterator();
                while (litr.hasNext()) {
                    Node next = (Node) litr.next();
                    if (text[offset] < next._text[0]) {
                        litr.previous();
                        break;
                    } else if (text[offset] == next._text[0]) {
                        int matchLen = next.lenMatches(text, offset);
                        if (matchLen == next._text.length) {
                            next.add(text, offset + matchLen, value);
                        } else {
                            next.split(matchLen);
                            next.add(text, offset + matchLen, value);
                        }
                        return;
                    }
                }
                litr.add(new Node(TextTrieMap.subArray(text, offset), addValue(null, value), null));
            }
        }

        private boolean matchFollowing(CharIterator chitr) {
            for (int idx = 1; idx < this._text.length; idx++) {
                if (!chitr.hasNext()) {
                    return false;
                }
                if (chitr.next().charValue() != this._text[idx]) {
                    return false;
                }
            }
            return true;
        }

        private int lenMatches(char[] text, int offset) {
            int textLen = text.length - offset;
            int limit = this._text.length < textLen ? this._text.length : textLen;
            int len = 0;
            while (len < limit && this._text[len] == text[offset + len]) {
                len++;
            }
            return len;
        }

        private void split(int offset) {
            char[] childText = TextTrieMap.subArray(this._text, offset);
            this._text = TextTrieMap.subArray(this._text, 0, offset);
            Node child = new Node(childText, this._values, this._children);
            this._values = null;
            this._children = new LinkedList();
            this._children.add(child);
        }

        private List<V> addValue(List<V> list, V value) {
            if (list == null) {
                list = new LinkedList();
            }
            list.add(value);
            return list;
        }
    }

    public class ParseState {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private Node node;
        private int offset = 0;
        private StepResult result;

        static {
            Class cls = TextTrieMap.class;
        }

        ParseState(Node start) {
            this.node = start;
            Objects.requireNonNull(start);
            this.result = new StepResult();
        }

        public void accept(int cp) {
            if (TextTrieMap.this._ignoreCase) {
                cp = UCharacter.foldCase(cp, true);
            }
            int count = Character.charCount(cp);
            this.node.takeStep(count == 1 ? (char) cp : UTF16.getLeadSurrogate(cp), this.offset, this.result);
            if (count == 2 && this.result.node != null) {
                this.result.node.takeStep(UTF16.getTrailSurrogate(cp), this.result.offset, this.result);
            }
            this.node = this.result.node;
            this.offset = this.result.offset;
        }

        public Iterator<V> getCurrentMatches() {
            if (this.node == null || this.offset != this.node.charCount()) {
                return null;
            }
            return this.node.values();
        }

        public boolean atEnd() {
            return this.node == null || (this.node.charCount() == this.offset && this.node._children == null);
        }
    }

    public interface ResultHandler<V> {
        boolean handlePrefixMatch(int i, Iterator<V> it);
    }

    private static class LongestMatchHandler<V> implements ResultHandler<V> {
        private int length;
        private Iterator<V> matches;

        private LongestMatchHandler() {
            this.matches = null;
            this.length = 0;
        }

        public boolean handlePrefixMatch(int matchLength, Iterator<V> values) {
            if (matchLength > this.length) {
                this.length = matchLength;
                this.matches = values;
            }
            return true;
        }

        public Iterator<V> getMatches() {
            return this.matches;
        }

        public int getMatchLength() {
            return this.length;
        }
    }

    public TextTrieMap(boolean ignoreCase) {
        this._ignoreCase = ignoreCase;
    }

    public TextTrieMap<V> put(CharSequence text, V val) {
        this._root.add(new CharIterator(text, 0, this._ignoreCase), val);
        return this;
    }

    public Iterator<V> get(String text) {
        return get(text, 0);
    }

    public Iterator<V> get(CharSequence text, int start) {
        return get(text, start, null);
    }

    public Iterator<V> get(CharSequence text, int start, int[] matchLen) {
        ResultHandler handler = new LongestMatchHandler();
        find(text, start, handler);
        if (matchLen != null && matchLen.length > 0) {
            matchLen[0] = handler.getMatchLength();
        }
        return handler.getMatches();
    }

    public void find(CharSequence text, ResultHandler<V> handler) {
        find(text, 0, (ResultHandler) handler);
    }

    public void find(CharSequence text, int offset, ResultHandler<V> handler) {
        find(this._root, new CharIterator(text, offset, this._ignoreCase), (ResultHandler) handler);
    }

    /* JADX WARNING: Missing block: B:13:0x001d, code skipped:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void find(Node node, CharIterator chitr, ResultHandler<V> handler) {
        Iterator<V> values = node.values();
        if (values == null || handler.handlePrefixMatch(chitr.processedLength(), values)) {
            Node nextMatch = node.findMatch(chitr);
            if (nextMatch != null) {
                find(nextMatch, chitr, (ResultHandler) handler);
            }
        }
    }

    public ParseState openParseState(int startingCp) {
        if (this._ignoreCase) {
            startingCp = UCharacter.foldCase(startingCp, true);
        }
        if (this._root.hasChildFor(Character.charCount(startingCp) == 1 ? (char) startingCp : UTF16.getLeadSurrogate(startingCp))) {
            return new ParseState(this._root);
        }
        return null;
    }

    private static char[] toCharArray(CharSequence text) {
        char[] array = new char[text.length()];
        for (int i = 0; i < array.length; i++) {
            array[i] = text.charAt(i);
        }
        return array;
    }

    private static char[] subArray(char[] array, int start) {
        if (start == 0) {
            return array;
        }
        char[] sub = new char[(array.length - start)];
        System.arraycopy(array, start, sub, 0, sub.length);
        return sub;
    }

    private static char[] subArray(char[] array, int start, int limit) {
        if (start == 0 && limit == array.length) {
            return array;
        }
        char[] sub = new char[(limit - start)];
        System.arraycopy(array, start, sub, 0, limit - start);
        return sub;
    }
}
