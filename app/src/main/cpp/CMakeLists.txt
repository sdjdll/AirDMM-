cmake_minimum_required(VERSION 3.22.1)
project("sdjini.AirDMM")
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -s -Wl,--gc-sections")

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

