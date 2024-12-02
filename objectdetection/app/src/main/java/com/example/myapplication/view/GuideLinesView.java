package com.example.myapplication.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.example.myapplication.ml.LiteModelSsdMobilenetV11Metadata2;


import android.graphics.LinearGradient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GuideLinesView extends View {

    private Paint redPaint;
    private Paint GuideLinesPaint = new Paint();

    private float curvedOffset;
    private List<LiteModelSsdMobilenetV11Metadata2.DetectionResult> detections = new ArrayList<>();
    public GuideLinesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Initialize paints for each color
        redPaint = createPaint(0xFFFF0000); // Red
        GuideLinesPaint.setStyle(Paint.Style.STROKE);
        GuideLinesPaint.setStrokeWidth(10);
        GuideLinesPaint.setAntiAlias(true);
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




        mergeAndDrawRectangles(detections,canvas,width,height,redPaint);

        // Draw red lines (near zone)
        drawGuideLine(
                canvas,
                centerX + endOffset + 160, endY + 200,
                centerX + endOffset - 50, startY,
                centerX + startOffset + curvedOffset , startY - 100,
                GuideLinesPaint, Color.RED,Color.YELLOW,Color.GREEN
        );
        drawGuideLine(
                canvas,
                centerX - endOffset - 160, endY + 200,
                centerX - endOffset + 50, startY,
                centerX - startOffset + curvedOffset  , startY - 100,
                GuideLinesPaint,Color.RED,Color.YELLOW,Color.GREEN
        );
    }



    public void updateDetections(List <LiteModelSsdMobilenetV11Metadata2.DetectionResult> detectionResults) {
        this.detections = detectionResults;

        invalidate(); // Redraw the view with new detections
    }

    public void drawGuideLine(Canvas canvas, float startX, float startY, float controlX, float controlY, float endX, float endY, Paint paint, int color1, int color2, int color3) {

        // Create a LinearGradient with three colors and sharp transitions
        LinearGradient gradient = new LinearGradient(
                startX, startY,     // Start point of the gradient
                endX, endY,         // End point of the gradient
                new int[]{color1, color1, color2, color2, color3, color3}, // Colors array
                new float[]{0f, 0.33f, 0.34f, 0.66f, 0.67f, 1f},           // Positions array for sharp transitions
                Shader.TileMode.CLAMP // Clamp to prevent tiling
        );
        paint.setShader(gradient);

        // Create a new Path for the curve
        Path path = new Path();
        path.moveTo(startX, startY);
        path.quadTo(controlX, controlY, endX, endY);

        // Draw the path with the gradient
        canvas.drawPath(path, paint);
    }
    public void steeringAngle(int i2cReading)
    {
        curvedOffset = ((800*i2cReading)/26000) - 400;
        invalidate();
    }

    public static void mergeAndDrawRectangles(
            List<LiteModelSsdMobilenetV11Metadata2.DetectionResult> detections,
            Canvas canvas,
            int width,
            int height,
            Paint redPaint
    ) {
        List<RectF> boundingBoxes = new ArrayList<>();

        // Scale bounding boxes to match the view dimensions
        for (LiteModelSsdMobilenetV11Metadata2.DetectionResult detection : detections) {
            if (detection.getScoreAsFloat() >= 0.5) {
                RectF box = detection.getLocationAsRectF();
                float left = (box.left * width) / 300;
                float top = (box.top * height) / 300;
                float right = (box.right * width) / 300;
                float bottom = (box.bottom * height) / 300;
                boundingBoxes.add(new RectF(left, top, right, bottom));
            }
        }

        // Merge intersecting rectangles
        List<RectF> mergedRectangles = mergeRectangles(boundingBoxes);

        // Draw the merged rectangles
        for (RectF rect : mergedRectangles) {
            canvas.drawRect(rect, redPaint);
        }
    }

    public static List<RectF> mergeRectangles(List<RectF> rectangles) {
        List<RectF> result = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();

        for (int i = 0; i < rectangles.size(); i++) {
            if (visited.contains(i)) continue;
            RectF current = rectangles.get(i);
            boolean hasIntersection;

            do {
                hasIntersection = false;
                for (int j = 0; j < rectangles.size(); j++) {
                    if (j != i && !visited.contains(j) && RectF.intersects(current, rectangles.get(j))) {
                        RectF intersecting = rectangles.get(j);
                        current = new RectF(
                                Math.min(current.left, intersecting.left),
                                Math.min(current.top, intersecting.top),
                                Math.max(current.right, intersecting.right),
                                Math.max(current.bottom, intersecting.bottom)
                        );
                        visited.add(j);
                        hasIntersection = true;
                    }
                }
            } while (hasIntersection);

            result.add(current);
            visited.add(i);
        }

        return result;
    }



}
