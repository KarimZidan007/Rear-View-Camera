package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class GuideLinesView extends View {

    private Paint redPaint, yellowPaint, greenPaint;

    public GuideLinesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Initialize paints for each color
        redPaint = createPaint(0xFFFF0000); // Red
        yellowPaint = createPaint(0xFFFFFF00); // Yellow
        greenPaint = createPaint(0xFF00FF00); // Green
    }

    /**
     * Helper method to create a Paint object with specified color, stroke width, and style.
     */
    private Paint createPaint(int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStrokeWidth(10f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        return paint;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        float centerX = width / 2f;

        // Define offsets and positions for the guidelines
        float startOffset = 250f; // Reduced horizontal offset from center to bring lines closer
        float endOffset = 350f; // Reduced horizontal offset for line narrowing
        float startY = height * 0.5f + 30; // Vertical position of the start
        float endY = height * 0.7f; // Vertical position of the end

        // Draw red lines (near zone)
        drawGuideline(canvas, centerX, startY, endY, startOffset, endOffset, greenPaint);

        // Draw yellow lines (medium zone)
        drawGuideline(canvas, centerX, endY, endY + 150, endOffset, endOffset + 80, yellowPaint);

        // Draw green lines (far zone)
        drawGuideline(canvas, centerX, endY + 150, endY + 300, endOffset + 80, endOffset + 160, redPaint);
    }

    /**
     * Draws a pair of guidelines on the canvas.
     *
     * @param canvas The canvas on which to draw.
     * @param centerX The x-coordinate of the center.
     * @param startY The starting y-coordinate of the guideline.
     * @param endY The ending y-coordinate of the guideline.
     * @param startOffset The horizontal offset for the left guideline.
     * @param endOffset The horizontal offset for the right guideline.
     * @param paint The paint to use for drawing.
     */
    private void drawGuideline(Canvas canvas, float centerX, float startY, float endY, float startOffset, float endOffset, Paint paint) {

        if (paint == redPaint)
        {
            // Left guideline
            Path leftPath = new Path();
            leftPath.moveTo(centerX - startOffset, startY-100);
            leftPath.lineTo(centerX - endOffset, endY-100);
            canvas.drawPath(leftPath, paint);

            // Right guideline
            Path rightPath = new Path();
            rightPath.moveTo(centerX + startOffset, startY-100);
            rightPath.lineTo(centerX + endOffset, endY-100);
            canvas.drawPath(rightPath, paint);
        }
     else {
            // Left guideline
            Path leftPath = new Path();
            leftPath.moveTo(centerX - startOffset, startY-100);
            leftPath.lineTo(centerX - endOffset, endY-100);
            canvas.drawPath(leftPath, paint);

            // Right guideline
            Path rightPath = new Path();
            rightPath.moveTo(centerX + startOffset, startY-100);
            rightPath.lineTo(centerX + endOffset, endY-100);
            canvas.drawPath(rightPath, paint);
        }

    }
}
