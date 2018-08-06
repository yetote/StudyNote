

cmake_minimum_required(VERSION 3.4.1)
set(lib_src_DIR ${CMAKE_SOURCE_DIR}/src/main/jniLibs/armeabi-v7a)

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/native-lib.cpp )

add_library( avutil-lib
             STATIC
             IMPORTED)
set_target_properties( avutil-lib
                       PROPERTIES IMPORTED_LOCATION
                       ${lib_src_DIR}/libavutil.a )
 add_library( swresample-lib
              STATIC
              IMPORTED)
 set_target_properties( swresample-lib
                        PROPERTIES IMPORTED_LOCATION
                        ${lib_src_DIR}/libswresample.a )
 add_library( avcodec-lib
              STATIC
              IMPORTED)
 set_target_properties( avcodec-lib
                        PROPERTIES IMPORTED_LOCATION
                        ${lib_src_DIR}/libavcodec.a )
 add_library( avfilter-lib
              STATIC
              IMPORTED)
 set_target_properties( avfilter-lib
                        PROPERTIES IMPORTED_LOCATION
                        ${lib_src_DIR}/libavfilter.a )
 add_library( swscale-lib
               STATIC
               IMPORTED)
 set_target_properties( swscale-lib
                         PROPERTIES IMPORTED_LOCATION
                         ${lib_src_DIR}/libswscale.a )
 add_library( avdevice-lib
              STATIC
              IMPORTED)
 set_target_properties( avdevice-lib
                        PROPERTIES IMPORTED_LOCATION
                        ${lib_src_DIR}/libavdevice.a )
 add_library( avformat-lib
              STATIC
              IMPORTED)
 set_target_properties( avformat-lib
                        PROPERTIES IMPORTED_LOCATION
                        ${lib_src_DIR}/libavformat.a )



# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
include_directories(${CMAKE_SOURCE_DIR}/src/main/jni/include)
find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       native-lib
                       avutil-lib
                       swresample-lib
                       avcodec-lib
                       avfilter-lib
                       swscale-lib
                       avdevice-lib
                       avformat-lib
                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )