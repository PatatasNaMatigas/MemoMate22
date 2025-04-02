package org.memomate.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextClock;

public class StrokedTextClockView extends TextClock {

    private Paint strokePaint;
    private Paint textPaint;
    private Runnable runnable;

    public StrokedTextClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        invalidate();
    }

    private void init() {
        setWillNotDraw(false); // Ensure custom drawing

        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(16);
        strokePaint.setColor(Color.parseColor("#313030"));
        strokePaint.setTextAlign(Paint.Align.CENTER);
        strokePaint.setTypeface(getTypeface());

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(getCurrentTextColor());
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(getTypeface());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        String text = getText().toString();
        float x = getWidth() / 2f;

        // Calculate the base y position for vertical centering
        float textHeight = textPaint.descent() - textPaint.ascent();
        float y = (getHeight() / 2f) + (textHeight / 2f) - textPaint.descent();

        strokePaint.setTextSize(getTextSize());
        textPaint.setTextSize(getTextSize());

        // Draw the stroke text
        canvas.drawText(text, x, y, strokePaint);

        // Draw the actual text
        canvas.drawText(text, x, y, textPaint);

        if (runnable != null)
            runnable.run();
    }

    public void onTimeChanged(Runnable runnable) {
        this.runnable = runnable;
    }
}
