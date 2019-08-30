
cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_BUILD_TYPE "Release")
set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -s")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -s")

add_library(
        playin
        SHARED
        playin.cpp)

find_library(
        log-lib
        log)


include_directories(${CMAKE_SOURCE_DIR}/ffmpeg/include)


target_link_libraries(
        playin
        -lm -lz android
        ${CMAKE_SOURCE_DIR}/ffmpeg/libs/${ANDROID_ABI}/libavformat.a
        ${CMAKE_SOURCE_DIR}/ffmpeg/libs/${ANDROID_ABI}/libavcodec.a
        ${CMAKE_SOURCE_DIR}/ffmpeg/libs/${ANDROID_ABI}/libswscale.a
        ${CMAKE_SOURCE_DIR}/ffmpeg/libs/${ANDROID_ABI}/libavutil.a
        ${log-lib}
)





#add_library(
#        libavformat
#        SHARED
#        IMPORTED
#)
#set_target_properties(
#        libavformat
#        PROPERTIES IMPORTED_LOCATION
#        ${CMAKE_SOURCE_DIR}/ffmpeg/libs/${ANDROID_ABI}/libavformat.so
#)
#
#add_library(
#        libavcodec
#        SHARED
#        IMPORTED
#)
#set_target_properties(
#        libavcodec
#        PROPERTIES IMPORTED_LOCATION
#        ${CMAKE_SOURCE_DIR}/ffmpeg/libs/${ANDROID_ABI}/libavcodec.so
#)
#
#add_library(
#        libswscale
#        SHARED
#        IMPORTED
#)
#set_target_properties(
#        libswscale
#        PROPERTIES IMPORTED_LOCATION
#        ${CMAKE_SOURCE_DIR}/ffmpeg/libs/${ANDROID_ABI}/libswscale.so
#)
#
#
#
#add_library(
#        libavutil
#        SHARED
#        IMPORTED
#)
#set_target_properties(
#        libavutil
#        PROPERTIES IMPORTED_LOCATION
#        ${CMAKE_SOURCE_DIR}/ffmpeg/libs/${ANDROID_ABI}/libavutil.so
#)
#
#
#target_link_libraries(
#        playin
#        libavformat
#        libavcodec
#        libswscale
#        libavutil
#        android -lm -lz
#        ${log-lib}
#)
