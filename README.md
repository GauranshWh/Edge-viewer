Edge-Viewer: Real-time Android Computer Vision Pipeline
Edge-Viewer is a high-performance Android application that demonstrates a complete end-to-end pipeline for real-time camera frame processing. It captures video, processes it with native C++ and OpenCV for Canny edge detection, and renders the output at high frame rates using OpenGL ES.

The project also includes a minimal TypeScript web viewer to display a sample of the processed output.

Demo
(To create this GIF, record your phone's screen while the app is running using Android Studio's built-in screen recorder, then convert the video to a GIF using an online tool. Add the GIF file to the root of your project.)

Core Features
Real-time Video Processing: Captures and processes camera frames with minimal latency.

Native C++ Performance: All image processing is offloaded from the Android runtime to native C++ using the Android NDK for maximum speed.

OpenCV Integration: Leverages the robust OpenCV library for the Canny edge detection algorithm.

JNI Bridge: A clean and efficient JNI (Java Native Interface) pipeline passes frame data from the Camera2 API directly to native code without intermediate copies.

OpenGL ES Rendering: The processed frames are rendered directly to a texture on the GPU using OpenGL ES 2.0 for a smooth, high-framerate display.

UI Overlay: Displays real-time stats (resolution and FPS) over the live video feed.

TypeScript Web Viewer: A decoupled, simple web page built with TypeScript to showcase a sample processed frame.

Tech Stack
Component

Technology

Android App

Kotlin, Android SDK, Camera2 API

Native Logic

C++, Android NDK, JNI

Processing

OpenCV 4.x

Rendering

OpenGL ES 2.0

Web Viewer

TypeScript, HTML5

Build System

Gradle (Android), CMake (Native), npm/tsc (Web)

Architecture Overview
The application is designed for a high-throughput, one-way data flow to minimize latency and avoid blocking the UI thread.

Camera Capture (Java/Kotlin): The CameraService uses the Camera2 API to capture frames into an ImageReader.

Frame Data Access (Java/Kotlin): For each frame, the raw YUV pixel data is accessed as direct ByteBuffer objects.

JNI Call (Java → C++): The buffers and image metadata (width, height, stride) are passed to a native C++ function in CVProcessor via JNI.

Native Processing (C++): The native function wraps the Y-plane (grayscale) data in a cv::Mat and applies the cv::Canny algorithm. The result is converted to an RGBA format.

OpenGL Rendering (Java/Kotlin): The processed RGBA buffer is passed to a MainRenderer class, which uploads the data to an OpenGL texture.

Display: A GLSurfaceView renders the textured quad to the screen on every frame.

Setup and Build Instructions
1. Android Application
Prerequisites:

Android Studio

Android NDK

An Android device with a camera

Steps:

Clone the repository:

git clone [https://github.com/your-username/Edge-viewer.git](https://github.com/your-username/Edge-viewer.git)

Download OpenCV: Download the OpenCV Android SDK from the official website and unzip it on your computer.

Configure the OpenCV Path:

Open the project in Android Studio.

Navigate to the app/cpp/CMakeLists.txt file.

Find the line set(OpenCV_DIR "...") and replace the placeholder path with the absolute path to the /sdk/native/jni folder inside your unzipped OpenCV SDK.

# Example for Windows
set(OpenCV_DIR "C:/Users/YourName/Downloads/OpenCV-4.9.0-android-sdk/sdk/native/jni")

Sync Gradle: Android Studio should prompt you to sync. If not, go to File > Sync Project with Gradle Files.

Build and Run: Connect your Android device, select it from the device menu, and click the "Run" button.

2. Web Viewer
Prerequisites:

Node.js and npm

TypeScript

Steps:

Navigate to the web directory:

cd Edge-viewer/web

Install TypeScript (if not already installed):

npm install -g typescript

Compile the TypeScript code:

tsc

This will create a dist/main.js file from src/main.ts.

View the page: Open the index.html file in a web browser.
