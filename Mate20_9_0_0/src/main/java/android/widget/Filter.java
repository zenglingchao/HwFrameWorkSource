package android.widget;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public abstract class Filter {
    private static final int FILTER_TOKEN = -791613427;
    private static final int FINISH_TOKEN = -559038737;
    private static final String LOG_TAG = "Filter";
    private static final String THREAD_NAME = "Filter";
    private Delayer mDelayer;
    private final Object mLock = new Object();
    private Handler mResultHandler = new ResultsHandler();
    private Handler mThreadHandler;

    public interface Delayer {
        long getPostingDelay(CharSequence charSequence);
    }

    public interface FilterListener {
        void onFilterComplete(int i);
    }

    protected static class FilterResults {
        public int count;
        public Object values;
    }

    private static class RequestArguments {
        CharSequence constraint;
        FilterListener listener;
        FilterResults results;

        private RequestArguments() {
        }
    }

    private class RequestHandler extends Handler {
        public RequestHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Message message;
            int what = msg.what;
            if (what == Filter.FILTER_TOKEN) {
                RequestArguments args = msg.obj;
                try {
                    args.results = Filter.this.performFiltering(args.constraint);
                } catch (Exception message2) {
                    args.results = new FilterResults();
                    Log.w("Filter", "An exception occured during performFiltering()!", message2);
                } catch (Throwable th) {
                    message2 = Filter.this.mResultHandler.obtainMessage(what);
                    message2.obj = args;
                    message2.sendToTarget();
                }
                message2 = Filter.this.mResultHandler.obtainMessage(what);
                message2.obj = args;
                message2.sendToTarget();
                synchronized (Filter.this.mLock) {
                    if (Filter.this.mThreadHandler != null) {
                        Filter.this.mThreadHandler.sendMessageDelayed(Filter.this.mThreadHandler.obtainMessage(Filter.FINISH_TOKEN), 3000);
                    }
                }
            } else if (what == Filter.FINISH_TOKEN) {
                synchronized (Filter.this.mLock) {
                    if (Filter.this.mThreadHandler != null) {
                        Filter.this.mThreadHandler.getLooper().quit();
                        Filter.this.mThreadHandler = null;
                    }
                }
            }
        }
    }

    private class ResultsHandler extends Handler {
        private ResultsHandler() {
        }

        public void handleMessage(Message msg) {
            RequestArguments args = msg.obj;
            Filter.this.publishResults(args.constraint, args.results);
            if (args.listener != null) {
                args.listener.onFilterComplete(args.results != null ? args.results.count : -1);
            }
        }
    }

    protected abstract FilterResults performFiltering(CharSequence charSequence);

    protected abstract void publishResults(CharSequence charSequence, FilterResults filterResults);

    public void setDelayer(Delayer delayer) {
        synchronized (this.mLock) {
            this.mDelayer = delayer;
        }
    }

    public final void filter(CharSequence constraint) {
        filter(constraint, null);
    }

    public final void filter(CharSequence constraint, FilterListener listener) {
        synchronized (this.mLock) {
            if (this.mThreadHandler == null) {
                HandlerThread thread = new HandlerThread("Filter", 10);
                thread.start();
                this.mThreadHandler = new RequestHandler(thread.getLooper());
            }
            long delay = this.mDelayer == null ? 0 : this.mDelayer.getPostingDelay(constraint);
            Message message = this.mThreadHandler.obtainMessage(FILTER_TOKEN);
            CharSequence charSequence = null;
            RequestArguments args = new RequestArguments();
            if (constraint != null) {
                charSequence = constraint.toString();
            }
            args.constraint = charSequence;
            args.listener = listener;
            message.obj = args;
            this.mThreadHandler.removeMessages(FILTER_TOKEN);
            this.mThreadHandler.removeMessages(FINISH_TOKEN);
            this.mThreadHandler.sendMessageDelayed(message, delay);
        }
    }

    public CharSequence convertResultToString(Object resultValue) {
        return resultValue == null ? "" : resultValue.toString();
    }
}
