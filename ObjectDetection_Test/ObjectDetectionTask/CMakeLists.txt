cmake_minimum_required(VERSION 3.16.3)
project(ObjectDetection)

# Find OpenCV package
find_package(OpenCV REQUIRED)

# Include OpenCV directories
include_directories(${OpenCV_INCLUDE_DIRS})

# Create executable
add_executable(ObjectDetection main.cpp)

# Link OpenCV libraries
target_link_libraries(ObjectDetection ${OpenCV_LIBS})