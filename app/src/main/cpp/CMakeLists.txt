cmake_minimum_required(VERSION 3.22.1)
project("aplicacionedgeimpulse17")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

include_directories(
        ${CMAKE_CURRENT_SOURCE_DIR}
        ${CMAKE_CURRENT_SOURCE_DIR}/edge-impulse-sdk
        ${CMAKE_CURRENT_SOURCE_DIR}/model-parameters
        ${CMAKE_CURRENT_SOURCE_DIR}/tflite-model
)

file(GLOB_RECURSE EI_SOURCES
        "${CMAKE_CURRENT_SOURCE_DIR}/edge-impulse-sdk/classifier/*.cpp"
        "${CMAKE_CURRENT_SOURCE_DIR}/edge-impulse-sdk/dsp/*.cpp"
        "${CMAKE_CURRENT_SOURCE_DIR}/edge-impulse-sdk/dsp/*.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/edge-impulse-sdk/tensorflow/*.cpp"
        "${CMAKE_CURRENT_SOURCE_DIR}/edge-impulse-sdk/tensorflow/*.cc"
        "${CMAKE_CURRENT_SOURCE_DIR}/edge-impulse-sdk/tensorflow/*.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/edge-impulse-sdk/CMSIS/*.cpp"
        "${CMAKE_CURRENT_SOURCE_DIR}/edge-impulse-sdk/CMSIS/*.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/edge-impulse-sdk/porting/posix/*.cpp"
        "${CMAKE_CURRENT_SOURCE_DIR}/edge-impulse-sdk/porting/posix/*.c"
        "${CMAKE_CURRENT_SOURCE_DIR}/edge-impulse-sdk/porting/posix/*.cc"
        "${CMAKE_CURRENT_SOURCE_DIR}/tflite-model/*.cpp"
        "${CMAKE_CURRENT_SOURCE_DIR}/tflite-model/*.c"
)

add_library(
        ${CMAKE_PROJECT_NAME}
        SHARED
        native-lib.cpp
        ${EI_SOURCES}
)

find_library(log-lib log)

target_link_libraries(
        ${CMAKE_PROJECT_NAME}
        ${log-lib}
)
