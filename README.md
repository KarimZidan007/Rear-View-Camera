# Rear View Camera Project on AOSP

This project implements a Rear View Camera system on Android Open Source Project (AOSP) that integrates hardware abstraction layers (HAL), native services, and an application with advanced features like dynamic guidelines and object detection. Additionally, the project extends the Vehicle HAL (VHAL) and binds it to the CarService, enabling seamless integration with the CarPropertyManager for steering angle data retrieval. Below is a detailed explanation of the components and functionality.

---

## Features

1. **Automatic Camera Launch**:

   - The application automatically starts when the vehicle is in reverse gear.

2. **Dynamic Guidelines**:

   - Guidelines are dynamically updated based on the steering angle.

3. **Object Detection**:

   - TensorFlow Lite is used for object detection.

4. **3D Visualization**:

   - Detected objects are displayed on the left side of the screen for enhanced situational awareness.

---

## Architecture Overview

### AOSP Integration

#### Native Service and HAL

- **Reverse Gear Detection**:

  - A GPIO pin is used to detect the reverse gear state.
  - A native service communicates with the HAL to monitor this GPIO.
  - When the reverse gear is detected, the native service signals the application to launch.

- **Steering Angle (VHAL Extension)**:

  - The steering angle is represented using a potentiometer connected via I2C to the board.
  - The Vehicle HAL (VHAL) has been extended to support this functionality, allowing the application to retrieve the current steering angle in real-time.
  - The VHAL extension is bound to the `CarService`, enabling the `CarPropertyManager` to provide the steering angle data to the application seamlessly.

### Application

#### Frameworks Used

- **MVVM (Model-View-ViewModel)**:
  - Ensures a clear separation of concerns and improves maintainability.
- **Camera2 API**:
  - Used to access the rear-view camera with high performance and flexibility.

#### Features

1. **Dynamic Guidelines**:

   - Rendered using a `Canvas`.
   - Adjust dynamically based on the steering angle provided by the VHAL.

2. **Object Detection**:

   - Utilizes a TensorFlow Lite model to detect objects.
   - Real-time processing to identify obstacles behind the vehicle.

3. **3D Visualization**:

   - A 3D representation of detected objects is displayed on the left part of the screen for better awareness.

---

## Getting Started

### Prerequisites

- AOSP setup on a supported board.
- GPIO pin connected to the reverse gear.
- Potentiometer connected to the board via I2C for steering angle simulation.
- TensorFlow Lite model for object detection.

### Cloning the Repository

Clone the project from GitHub:

```bash
git clone https://github.com/KarimZidan007/Rear-View-Camera.git
```

Check all branches to explore various features and components:

```bash
git branch -a
```

### Build and Deployment

1. **AOSP Setup**:

   - Add the native service and HAL implementation to the AOSP source tree.
   - Build the AOSP with the added HAL and native service.

2. **Application**:

   - Import the application module into Android Studio.
   - Connect a supported device or emulator.
   - Build and deploy the app.

### Running the Project

1. Engage reverse gear to start the application automatically.
2. Use the steering wheel to see dynamic updates to the guidelines.
3. Observe detected objects and their visualization on the screen.

---

## Repository Structure
- on the following branches

- **`GearService/`**: Contains the native service and HAL code for reverse gear.
- **`main/`**: Android application implementing the camera interface and visualization.
- **`Aosp_I2c_Module/`**: I2c Module to get the steering angle from adc through i2c bus.

---

## References

- [AOSP Documentation](https://source.android.com/docs)
- [TensorFlow Lite](https://www.tensorflow.org/lite)
- [Camera2 API](https://developer.android.com/reference/android/hardware/camera2)

---



## Contributing

Contributions are welcome! Feel free to open issues or submit pull requests to improve the project.

