package io.mrarm.irc.newui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import io.mrarm.irc.util.StyledAttributesHelper;

public class SlideableFragmentContainer extends FrameLayout {

    private static final float MIN_VELOCITY = 20;

    private FragmentManager mFragmentManager;
    private int mTouchSlop;
    private float mMinVelocity;
    private View mTouchDragView;
    private float mTouchDragStartX;
    private VelocityTracker mTouchDragVelocity;
    private boolean mTouchDragUnsetBg;
    private int mFallbackBackgroundColor;

    public SlideableFragmentContainer(@NonNull Context context) {
        this(context, null);
    }

    public SlideableFragmentContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideableFragmentContainer(@NonNull Context context, @Nullable AttributeSet attrs,
                                      int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mFallbackBackgroundColor = StyledAttributesHelper.getColor(
                context, android.R.attr.colorBackground, 0);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMinVelocity = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MIN_VELOCITY,
                context.getResources().getDisplayMetrics());
    }

    public void setFragmentManager(FragmentManager mgr) {
        mFragmentManager = mgr;
    }

    public void push(Fragment fragment) {
        mFragmentManager.beginTransaction()
                .add(getId(), fragment)
                .commit();
    }

    public void pop(Fragment fragment) {
        mFragmentManager.beginTransaction()
                .remove(fragment)
                .commit();
    }

    public void pop() {
        pop(mFragmentManager.findFragmentById(getId()));
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        if (getChildCount() > 1) {
            elevateView(child);
            child.setTranslationX(getWidth());
            child.animate().translationX(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            deelevateView(child);
                        }
                    }).start();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (getChildCount() <= 1)
                    break;
                mTouchDragVelocity = VelocityTracker.obtain();
                mTouchDragView = getChildAt(getChildCount() - 1);
                mTouchDragStartX = ev.getX();
                mTouchDragVelocity.addMovement(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchDragView != null) {
                    mTouchDragVelocity.addMovement(ev);
                    if (ev.getX() - mTouchDragStartX > mTouchSlop) {
                        elevateView(mTouchDragView);
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTouchDragView != null) {
                    mTouchDragView = null;
                    mTouchDragVelocity.recycle();
                    mTouchDragVelocity = null;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE: {
                if (mTouchDragView != null) {
                    mTouchDragVelocity.addMovement(ev);
                    mTouchDragView.setTranslationX(Math.max(ev.getX() - mTouchDragStartX, 0));
                }
                return true;
            }
            case MotionEvent.ACTION_UP: {
                if (mTouchDragView != null) {
                    mTouchDragVelocity.computeCurrentVelocity(1000);
                    if (mTouchDragVelocity.getXVelocity() > mMinVelocity) {
                        View v = mTouchDragView;
                        v.animate().translationX(getWidth())
                                .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                pop();
                                deelevateView(v);
                                v.animate().setListener(null);
                            }
                        }).start();
                    } else {
                        mTouchDragView.animate().translationX(0).start();
                    }
                    mTouchDragView = null;
                    mTouchDragVelocity.recycle();
                    mTouchDragVelocity = null;
                }
                return true;
            }
        }
        return super.onTouchEvent(ev);
    }

    private void elevateView(View v) {
        if (v.getBackground() == null) {
            mTouchDragUnsetBg = true;
            v.setBackgroundColor(mFallbackBackgroundColor);
        }
        ViewCompat.setElevation(v,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2.f,
                        getResources().getDisplayMetrics()));
    }

    private void deelevateView(View v) {
        /*
        if (mTouchDragUnsetBg) {
            v.setBackground(null);
        }
        */
        ViewCompat.setElevation(v, 0.f);
    }

}
