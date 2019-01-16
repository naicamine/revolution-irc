package io.mrarm.irc.util.theme.live;

import android.content.res.Resources;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import io.mrarm.irc.R;
import io.mrarm.irc.util.StyledAttributesHelper;

public class ThemedView {

    private static final int[] THEME_ATTRS = { android.R.attr.background, R.attr.backgroundTint };

    static void setupTheming(View view, LiveThemeComponent component, AttributeSet attrs, int defStyleAttr) {
        Resources.Theme t = component.getTheme();
        StyledAttributesHelper r = StyledAttributesHelper.obtainStyledAttributes(view.getContext(), t, attrs, THEME_ATTRS, defStyleAttr);
        component.addColorAttr(r, android.R.attr.background, view::setBackgroundColor);
        component.addColorAttr(r, R.attr.backgroundTint, null, (c) -> ViewCompat.setBackgroundTintList(view, c));
        r.recycle();
    }

}
