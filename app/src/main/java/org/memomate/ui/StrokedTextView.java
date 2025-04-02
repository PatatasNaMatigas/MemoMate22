package org.memomate.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

public class StrokedTextView extends androidx.appcompat.widget.AppCompatTextView {

    private Paint strokePaint;

    public StrokedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setStyle(Paint.Style.STROKE); // Stroke mode
        strokePaint.setStrokeWidth(90); // Adjust thickness
        strokePaint.setColor(Color.parseColor("#313030")); // Outline color
        strokePaint.setTypeface(getTypeface());
        strokePaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        String text = getText().toString();
        float x = getWidth() / 2f;
        float y = getBaseline();

        // Ensure stroke text size dynamically updates
        strokePaint.setTextSize(getTextSize());

        // Draw the stroke
        setPaintToStroke();
        canvas.drawText(text, x, y, strokePaint);

        // Draw the normal text on top
        setPaintToFill();
        super.onDraw(canvas);
    }

    private void setPaintToStroke() {
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(16); // Thickness
        strokePaint.setColor(Color.parseColor("#313030"));
        setLayerType(LAYER_TYPE_SOFTWARE, strokePaint); // Ensure smoothness
    }

    private void setPaintToFill() {
        getPaint().setStyle(Paint.Style.FILL);
    }
}
