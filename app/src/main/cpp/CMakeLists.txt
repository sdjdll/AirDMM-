cmake_minimum_required(VERSION 3.22.1)
project("sdjini.AirDMM")

add_library(
        Couple
        SHARED
        libCouple.c
)

add_library(
        stringLib
        SHARED
        formatString.c
)

target_link_libraries(Couple)
target_link_libraries(stringLib)

if(CMAKE_BUILD_TYPE STREQUAL "Release")
    add_custom_command(TARGET libcouple POST_BUILD
        COMMAND ${CMAKE_STRIP}$<TARGET_FILE:libcouple>
    )
endif()
