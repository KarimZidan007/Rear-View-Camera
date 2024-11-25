#include <iostream>
#include <opencv2/opencv.hpp>
#include <fstream>
#include <vector>
#include <cmath>

// Load class list from file
std::vector<std::string> load_class_list() {
    std::vector<std::string> class_list;
    std::ifstream ifs("/home/mostafa/Desktop/ITI/GP_Tasks/ObjectDetectionTask/classes.txt");
    std::string line;
    while (getline(ifs, line)) {
        class_list.push_back(line);
    }
    return class_list;
}

// Smooth bounding box using moving average
cv::Rect smoothBoundingBox(cv::Rect currentBox, cv::Rect previousBox, float alpha = 0.8) {
    int x = static_cast<int>(alpha * previousBox.x + (1 - alpha) * currentBox.x);
    int y = static_cast<int>(alpha * previousBox.y + (1 - alpha) * currentBox.y);
    int width = static_cast<int>(alpha * previousBox.width + (1 - alpha) * currentBox.width);
    int height = static_cast<int>(alpha * previousBox.height + (1 - alpha) * currentBox.height);
    return cv::Rect(x, y, width, height);
}

// Calculate Intersection over Union (IoU) between two bounding boxes
float computeIoU(const cv::Rect& boxA, const cv::Rect& boxB) {
    int x1 = std::max(boxA.x, boxB.x);
    int y1 = std::max(boxA.y, boxB.y);
    int x2 = std::min(boxA.x + boxA.width, boxB.x + boxB.width);
    int y2 = std::min(boxA.y + boxA.height, boxB.y + boxB.height);
    
    int width = std::max(0, x2 - x1);
    int height = std::max(0, y2 - y1);
    int intersection = width * height;
    
    int areaA = boxA.width * boxA.height;
    int areaB = boxB.width * boxB.height;
    
    int unionArea = areaA + areaB - intersection;
    
    return static_cast<float>(intersection) / unionArea;
}

int main() {
    std::vector<std::string> class_list = load_class_list();
    // Load pre-trained MobileNet SSD model and configuration
    std::string model = "/home/mostafa/Desktop/ITI/GP_Tasks/ObjectDetectionTask/mobilenet_iter_73000.caffemodel";
    std::string config = "/home/mostafa/Desktop/ITI/GP_Tasks/ObjectDetectionTask/deploy.prototxt";
    cv::dnn::Net net = cv::dnn::readNetFromCaffe(config, model);

    // Use webcam for real-time detection
    cv::VideoCapture cap(0);
    if (!cap.isOpened()) {
        std::cerr << "Error: Couldn't open the webcam." << std::endl;
        return -1;
    }

    // To store detected bounding boxes from previous frames
    std::vector<cv::Rect> previousBoxes;
    std::vector<std::string> previousLabels;

    while (true) {
        cv::Mat frame;
        cap >> frame;

        if (frame.empty()) {
            std::cerr << "Error: Couldn't capture frame." << std::endl;
            break;
        }

        // Prepare the frame for the neural network
        cv::Mat blob = cv::dnn::blobFromImage(frame, 0.007843, cv::Size(300, 300), 127.5);
        net.setInput(blob);

        // Forward pass
        cv::Mat detection = net.forward();

        // Vectors to hold detections
        std::vector<cv::Rect> boxes;
        std::vector<float> confidences;
        std::vector<int> class_ids;

        // Process the detection
        float* data = (float*)detection.ptr<float>(0);
        for (int i = 0; i < detection.size[2]; i++) {
            float confidence = data[i * 7 + 2]; // Confidence score
            if (confidence > 0.6) {            // Threshold for confidence
                int classId = static_cast<int>(data[i * 7 + 1]);
                int left = static_cast<int>(data[i * 7 + 3] * frame.cols);
                int top = static_cast<int>(data[i * 7 + 4] * frame.rows);
                int right = static_cast<int>(data[i * 7 + 5] * frame.cols);
                int bottom = static_cast<int>(data[i * 7 + 6] * frame.rows);

                // Create a rectangle for the bounding box
                cv::Rect box(left, top, right - left, bottom - top);
                boxes.push_back(box);
                confidences.push_back(confidence);
                class_ids.push_back(classId);
            }
        }

        // Apply Non-Maximum Suppression
        std::vector<int> indices;
        float nmsThreshold = 0.3; // Suppress boxes with IoU > 0.3
        cv::dnn::NMSBoxes(boxes, confidences, 0.6, nmsThreshold, indices);

        // Object tracking by associating boxes across frames based on IoU
        std::vector<cv::Rect> updatedBoxes;
        std::vector<std::string> updatedLabels;

        for (int idx : indices) {
            cv::Rect box = boxes[idx];
            int classId = class_ids[idx];
            std::string label = class_list[classId];

            bool matched = false;
            for (size_t j = 0; j < previousBoxes.size(); ++j) {
                // Compare current detection with previous boxes using IoU
                if (computeIoU(box, previousBoxes[j]) > 0.5) { // Threshold for IoU
                    updatedBoxes.push_back(smoothBoundingBox(box, previousBoxes[j]));
                    updatedLabels.push_back(previousLabels[j]); // Retain previous label
                    matched = true;
                    break;
                }
            }

            // If no match, add new box to the updated list
            if (!matched) {
                updatedBoxes.push_back(box);
                updatedLabels.push_back(label);
            }

            // Draw bounding box
            cv::rectangle(frame, box, cv::Scalar(0, 255, 0), 2);

            // Add label
            int baseline = 0;
            cv::Size label_size = cv::getTextSize(label, cv::FONT_HERSHEY_SIMPLEX, 0.5, 1, &baseline);
            int top = std::max(box.y, label_size.height);
            cv::rectangle(frame, 
                          cv::Point(box.x, top - label_size.height - 10), 
                          cv::Point(box.x + label_size.width, top + baseline - 10), 
                          cv::Scalar(0, 255, 0), cv::FILLED);
            cv::putText(frame, label, cv::Point(box.x, top - 5), 
                        cv::FONT_HERSHEY_SIMPLEX, 0.5, cv::Scalar(0, 0, 0), 1);
        }

        // Update previous boxes and labels for next frame
        previousBoxes = updatedBoxes;
        previousLabels = updatedLabels;

        // Display the frame with detections
        cv::imshow("Real-time Object Detection", frame);

        // Exit on pressing 'q'
        int key = cv::waitKey(1);
        if (key == 'q' || key == 27) break;
    }

    cap.release();
    cv::destroyAllWindows();

    return 0;
}

