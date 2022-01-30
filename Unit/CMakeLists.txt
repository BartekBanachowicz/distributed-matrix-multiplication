cmake_minimum_required(VERSION 3.0.0)
project(mm_unit)

add_executable(mm_unit
    src/main.cpp

    include/UnitRunner.hpp
    src/UnitRunner.cpp

    include/err/unit_exception.hpp
    src/err/unit_exception.cpp

    include/rqst/ResponseParser.hpp
    include/rqst/RequestConstructor.hpp
    src/rqst/ResponseParser.cpp
    src/rqst/RequestConstructor.cpp

)
target_include_directories(mm_unit PUBLIC include/)
target_compile_features(mm_unit PUBLIC cxx_std_17)
target_compile_options(mm_unit PUBLIC -Wall -Wextra -Wpedantic)
