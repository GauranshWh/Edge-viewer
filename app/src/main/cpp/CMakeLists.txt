# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

cmake_minimum_required(VERSION 3.22.1)

project("edge_viewer")

# ===================================================================
# START: ADD THIS NEW SECTION HERE
# ===================================================================
# Define the path to your OpenCV Android SDK using the path you provided.
# Note: CMake prefers forward slashes, even on Windows.
set(OpenCV_DIR "C:/libs/OpenCV-android-sdk/sdk/native/jni")
find_package(OpenCV REQUIRED)
# ===================================================================
# END: NEW SECTION
# ===================================================================

add_library(${CMAKE_PROJECT_NAME} SHARED
        native-lib.cpp)

# ===================================================================
# START: MODIFY THIS SECTION AT THE END
# ===================================================================
target_link_libraries(${CMAKE_PROJECT_NAME}
        # List libraries link to the target library
        android
        log
        # Add the OpenCV library so the linker can find it
        ${OpenCV_LIBS})
# ===================================================================
# END: MODIFIED SECTION
# ===================================================================