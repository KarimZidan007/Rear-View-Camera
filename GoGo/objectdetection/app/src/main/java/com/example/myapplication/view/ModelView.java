package com.example.myapplication.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.myapplication.R;
import com.example.myapplication.ml.LiteModelSsdMobilenetV11Metadata2;
import java.util.ArrayList;
import java.util.List;


public class ModelView extends View {
    public FrameLayout layout ;
    private List<ImageView> imageViews = new ArrayList<>();

    private List<LiteModelSsdMobilenetV11Metadata2.DetectionResult> detections = new ArrayList<>();
    public ModelView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        modelDetections(detections,width,height);

    }



    public void updateDetections(List <LiteModelSsdMobilenetV11Metadata2.DetectionResult> detectionResults) {
        this.detections = detectionResults;
        Log.i("TAG", "updateDetections: ");
        removeAllImageViews();
        invalidate(); // Redraw the view with new detections
    }

    private void modelDetections(List<LiteModelSsdMobilenetV11Metadata2.DetectionResult> detections, int width, int height) {
        Log.i("TAG", "modelDetections: before call remove");

        Log.i("TAG", "modelDetections: after call remove \n");
        for (LiteModelSsdMobilenetV11Metadata2.DetectionResult detect : detections) {
            Log.i("TAG", "handleMultipleDetections: "+detect.getScoreAsFloat());

            if (detect.getScoreAsFloat() >= 0.5) {
                // Get the bounding box from detection result
                RectF box = detect.getLocationAsRectF();
                Log.i("TAG", "handleMultipleDetections: "+detect.getCategoryAsString());

                // Scale the bounding box coordinates to the actual screen size
                float left = ((box.left * width)/300);
                float top = ((box.top * height)/300);
                float right = (box.right * width)/300;
                float bottom = ((box.bottom * height)/300);

                // Create a scaled box (optional, if you want to adjust margins or padding)
                RectF scaledBox = new RectF(left, top, right, bottom);

                // Calculate the width and height of the ImageView
                int imgWidth = (int) (scaledBox.right - scaledBox.left);
                int imgHeight = (int) (scaledBox.bottom - scaledBox.top);

                // Create a new ImageView for the detection icon
                ImageView detectionImageView = new ImageView(this.getContext());
                if ("person".equals(detect.getCategoryAsString()))
                {

                    detectionImageView.setImageResource(R.drawable.person); // PNG for detection
                }else if ("car".equals(detect.getCategoryAsString())){
                    detectionImageView.setImageResource(R.drawable.car);
                }
                else
                {
                    detectionImageView.setImageResource(R.drawable.cone);
                }
                Log.i("TAG", "modelDetections: create ");
                // Set layout parameters based on scaled bounding box
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(150, 150);
                params.leftMargin = (int) left;
                params.topMargin = (int) top+height;
                // Apply the layout parameters to the ImageView
                detectionImageView.setLayoutParams(params);
                Log.i("TAG", "modelDetections: create before add");
                // Add the ImageView to the layout
                layout.addView(detectionImageView);
                Log.i("TAG", "modelDetections: create before add");

                imageViews.add(detectionImageView);

            }
        }
        Log.i("TAG", "modelDetections: out of for loop ");
    }

    private void removeAllImageViews() {
        // Loop through the list of ImageViews and remove them
        if (imageViews == null)
        {
            Log.i("TAG", "removeAllImageViews: null");

        }        if (imageViews.isEmpty())
        {

        }
        else {
            for (ImageView imageView : imageViews) {
                layout.removeView(imageView);  // Remove from FrameLayout
            }
        }

        Log.i("TAG", "removeAllImageViews: ");
        // Clear the list of ImageViews
        imageViews.clear();
    }




}
