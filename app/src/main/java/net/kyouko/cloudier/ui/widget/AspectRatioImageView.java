package net.kyouko.cloudier.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import net.kyouko.cloudier.R;

/**
 * {@link ImageView} with an aspect ratio.
 *
 * @author beta
 */
public class AspectRatioImageView extends ImageView {

    private final static int MEASUREMENT_WIDTH = 0;
    private final static int MEASUREMENT_HEIGHT = 1;


    private float aspectRatio = 1f;
    private int dominantMeasurement = MEASUREMENT_WIDTH;


    public AspectRatioImageView(Context context) {
        this(context, null);
    }


    public AspectRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.AspectRatioImageView);
        aspectRatio = array.getFloat(R.styleable.AspectRatioImageView_aspectRatio, 1f);
        dominantMeasurement = array.getInt(R.styleable.AspectRatioImageView_dominantMeasurement,
                MEASUREMENT_WIDTH);
        array.recycle();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        if (dominantMeasurement == MEASUREMENT_WIDTH) {
            height = (int) (width / aspectRatio);
        } else if (dominantMeasurement == MEASUREMENT_HEIGHT) {
            width = (int) (height * aspectRatio);
        }

        setMeasuredDimension(width, height);
    }

}
