package net.kyouko.cloudier.style;

import android.text.TextPaint;
import android.text.style.ClickableSpan;

/**
 * Custom span for links without underlines.
 *
 * @author beta
 */
public abstract class ClickableWithoutUnderlineSpan extends ClickableSpan {

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(ds.linkColor);
        ds.setUnderlineText(false);
    }

}
