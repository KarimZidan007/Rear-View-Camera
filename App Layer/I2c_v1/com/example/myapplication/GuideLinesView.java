package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.myapplication.ml.LiteModelSsdMobilenetV11Metadata2;


import java.util.ArrayList;
import java.util.List;

public class GuideLinesView extends View {

    private Paint redPaint, yellowPaint, greenPaint,bluePaint;
    private float curvedOffset;
    private List<LiteModelSsdMobilenetV11Metadata2.DetectionResult> detections = new ArrayList<>();
    public GuideLinesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Initialize paints for each color
        redPaint = createPaint(0xFFFF0000); // Red
        yellowPaint = createPaint(0xFFFFFF00); // Yellow
        greenPaint = createPaint(0xFF00FF00); // Green
        bluePaint = createPaint(0xFF0000FF);
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
        for (LiteModelSsdMobilenetV11Metadata2.DetectionResult detection : detections) {
            // Scale the bounding box to match the view dimensions

//            if (detection.getScoreAsFloat() >= 0.5) {
//                RectF box = detection.getLocationAsRectF();
//                detection.getLocationAsRectF();
//                float left = (box.left * width)/300;
//                float top = (box.top * height)/300;
//                float right = (box.right * width)/300;
//                float bottom = (box.bottom * height)/300;
//
//                RectF scaledBox = new RectF(left, top, right, bottom);
//                canvas.drawRect(scaledBox, redPaint);

           // }
        }
        // Draw red lines (near zone)
        drawGuideline(canvas, centerX, startY, endY, startOffset, endOffset, greenPaint);

        // Draw yellow lines (medium zone)
        drawGuideline(canvas, centerX, endY, endY + 150, endOffset, endOffset + 80, yellowPaint);

        // Draw green lines (far zone)
        drawGuideline(canvas, centerX, endY + 150, endY + 300, endOffset + 80, endOffset + 160, redPaint);
        drawCurvedLine(
                canvas,
                centerX + endOffset + 160, endY + 200,
                centerX + endOffset - 50, startY,
                centerX + startOffset + curvedOffset , startY - 100,
                bluePaint
        );

        // Right line: curve to the right
        drawCurvedLine(
                canvas,
                centerX - endOffset - 160, endY + 200,
                centerX - endOffset + 50, startY,
                centerX - startOffset + curvedOffset  , startY - 100,
                bluePaint
        );
    }


    private void drawGuideline(Canvas canvas, float centerX, float startY, float endY, float startOffset, float endOffset, Paint paint) {


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
    public void updateDetections(List <LiteModelSsdMobilenetV11Metadata2.DetectionResult> detectionResults) {
        this.detections = detectionResults;
        Log.i("TAG", "updateDetections: ");

        invalidate(); // Redraw the view with new detections
    }

    public void drawCurvedLine(Canvas canvas, float startX, float startY, float controlX, float controlY, float endX, float endY, Paint paint) {
        // Create a new Path
        Path leftPath = new Path();
        leftPath.moveTo(startX, startY);
        leftPath.quadTo(controlX, controlY, endX, endY);
        canvas.drawPath(leftPath, paint);
    }

    public void steeringAngle(int i2cReading)
    {
        curvedOffset = ((800*i2cReading)/26454) - 400;
        Log.d("GuideLinesView", "steeringAngle called, curvedOffset = " + curvedOffset);
        invalidate();
    }
}
