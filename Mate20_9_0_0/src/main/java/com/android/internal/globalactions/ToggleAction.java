package com.android.internal.globalactions;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class ToggleAction implements Action {
    private static final String TAG = "ToggleAction";
    protected int mDisabledIconResid;
    protected int mDisabledStatusMessageResId;
    protected int mEnabledIconResId;
    protected int mEnabledStatusMessageResId;
    protected int mMessageResId;
    protected State mState = State.Off;

    public enum State {
        Off(false),
        TurningOn(true),
        TurningOff(true),
        On(false);
        
        private final boolean inTransition;

        private State(boolean intermediate) {
            this.inTransition = intermediate;
        }

        public boolean inTransition() {
            return this.inTransition;
        }
    }

    public abstract void onToggle(boolean z);

    public ToggleAction(int enabledIconResId, int disabledIconResid, int message, int enabledStatusMessageResId, int disabledStatusMessageResId) {
        this.mEnabledIconResId = enabledIconResId;
        this.mDisabledIconResid = disabledIconResid;
        this.mMessageResId = message;
        this.mEnabledStatusMessageResId = enabledStatusMessageResId;
        this.mDisabledStatusMessageResId = disabledStatusMessageResId;
    }

    void willCreate() {
    }

    public CharSequence getLabelForAccessibility(Context context) {
        return context.getString(this.mMessageResId);
    }

    public View create(Context context, View convertView, ViewGroup parent, LayoutInflater inflater) {
        willCreate();
        View v = inflater.inflate(17367149, parent, false);
        ImageView icon = (ImageView) v.findViewById(16908294);
        TextView messageView = (TextView) v.findViewById(16908299);
        TextView statusView = (TextView) v.findViewById(16909371);
        boolean enabled = isEnabled();
        if (messageView != null) {
            messageView.setText(this.mMessageResId);
            messageView.setEnabled(enabled);
        }
        boolean on = this.mState == State.On || this.mState == State.TurningOn;
        if (icon != null) {
            icon.setImageDrawable(context.getDrawable(on ? this.mEnabledIconResId : this.mDisabledIconResid));
            icon.setEnabled(enabled);
        }
        if (statusView != null) {
            statusView.setText(on ? this.mEnabledStatusMessageResId : this.mDisabledStatusMessageResId);
            statusView.setVisibility(0);
            statusView.setEnabled(enabled);
        }
        v.setEnabled(enabled);
        return v;
    }

    public final void onPress() {
        if (this.mState.inTransition()) {
            Log.w(TAG, "shouldn't be able to toggle when in transition");
            return;
        }
        boolean nowOn = this.mState != State.On;
        onToggle(nowOn);
        changeStateFromPress(nowOn);
    }

    public boolean isEnabled() {
        return this.mState.inTransition() ^ 1;
    }

    protected void changeStateFromPress(boolean buttonOn) {
        this.mState = buttonOn ? State.On : State.Off;
    }

    public void updateState(State state) {
        this.mState = state;
    }
}
