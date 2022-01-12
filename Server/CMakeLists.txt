cmake_minimum_required(VERSION 3.0.0)
project(mm_server)

add_executable(mm_server
    src/main.cpp

    include/utils/ServerRunner.hpp
    include/utils/server_exception.hpp
    include/utils/TCPSocket.hpp
    src/utils/ServerRunner.cpp
    src/utils/server_exception.cpp
)
target_include_directories(mm_server PUBLIC include/)
target_link_libraries(mm_server PUBLIC -pthread)
target_compile_features(mm_server PUBLIC cxx_std_17)
target_compile_options(mm_server PUBLIC -Wall -Wextra -Wpedantic)
