# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

set(PATH_PROJECT ${PROJECT_SOURCE_DIR}/../../..)
set(LIBS_PATH ${PATH_PROJECT}/libs)
#set(OBOE_PATH ./oboe)

#add_subdirectory(${OBOE_PATH} ./oboe)

#include_directories(${OBOE_PATH}/include)
include_directories(${PATH_PROJECT}/src/main/cpp/include)
#message(STATUS ${PATH_PROJECT}/src/main/cpp/includes)
add_library(avutil
        SHARED
        IMPORTED)
set_target_properties(avutil
        PROPERTIES IMPORTED_LOCATION
        ${LIBS_PATH}/${ANDROID_ABI}/libavutil.so)

add_library(swresample SHARED IMPORTED)
set_target_properties(swresample PROPERTIES IMPORTED_LOCATION ${LIBS_PATH}/${ANDROID_ABI}/libswresample.so)

add_library(avcodec
        SHARED
        IMPORTED
        )

set_target_properties(
        avcodec
        PROPERTIES IMPORTED_LOCATION
        ${LIBS_PATH}/${ANDROID_ABI}/libavcodec.so
)

add_library(avformat
        SHARED
        IMPORTED
        )

set_target_properties(
        avformat
        PROPERTIES IMPORTED_LOCATION
        ${LIBS_PATH}/${ANDROID_ABI}/libavformat.so
)

add_library(swscale
        SHARED
        IMPORTED
        )

set_target_properties(
        swscale
        PROPERTIES IMPORTED_LOCATION
        ${LIBS_PATH}/${ANDROID_ABI}/libswscale.so
)


#add_library(avfilter
#        SHARED
#        IMPORTED
#        )
#
#set_target_properties(
#        avfilter
#        PROPERTIES IMPORTED_LOCATION
#        ${LIBS_PATH}/${ANDROID_ABI}/libavfilter.so
#)

#add_library(avdevice
#        SHARED
#        IMPORTED
#        )
#
#set_target_properties(
#        avdevice
#        PROPERTIES IMPORTED_LOCATION
#        ${LIBS_PATH}/${ANDROID_ABI}/libavdevice.so
#)
#set(UTIL_SOURCE
#
#        util/Native4JavaCallback.cpp
#        util/MessageQueue.cpp
#        )
#
set(
        MEDIA_SOURCE

        decode/FFmpegDemode.cpp
)
#

add_library(
        native-lib

        SHARED

        native-lib.cpp
#        ${UTIL_SOURCE}
        ${MEDIA_SOURCE}
)

set(
        ffmpeg-lib

        avutil
        swresample
        avformat
        avcodec
        swscale
)

find_library(
        log-lib


        log)


target_link_libraries(
        # Specifies the target library.
        native-lib
        android
        EGL
        GLESv2
        mediandk
        OpenMAXAL
#        oboe

        ${ffmpeg-lib}
        ${log-lib})