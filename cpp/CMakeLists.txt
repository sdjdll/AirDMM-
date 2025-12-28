cmake_minimum_required(VERSION 3.22.1)

project("StringUnit")

add_library(
        StringUnit
        SHARED
        StringUnit.cpp)

find_library(
        log-lib
        log)

target_link_libraries(
        StringUnit
        ${log-lib})
