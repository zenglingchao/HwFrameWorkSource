package dalvik.system;

final class TransactionAbortError extends InternalError {
    private TransactionAbortError() {
    }

    private TransactionAbortError(String detailMessage) {
        super(detailMessage);
    }

    private TransactionAbortError(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

    private TransactionAbortError(Throwable cause) {
        this(cause == null ? null : cause.toString(), cause);
    }
}
