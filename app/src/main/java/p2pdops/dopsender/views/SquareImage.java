package p2pdops.dopsender.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

public class SquareImage extends AppCompatImageView {

    public SquareImage(final Context context) {
        super(context);
    }

    public SquareImage(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImage(final Context context, final AttributeSet attrs,
                final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int width, int height) {
        super.onMeasure(width, height);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        if (measuredWidth > measuredHeight) {
            setMeasuredDimension(measuredHeight, measuredHeight);
        } else {
            setMeasuredDimension(measuredWidth, measuredWidth);

        }

    }

}