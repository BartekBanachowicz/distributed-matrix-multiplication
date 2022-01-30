cmake_minimum_required(VERSION 3.0.0)
project(mm_server)

add_executable(mm_server
    src/main.cpp

    include/ServerRunner.hpp
    src/ServerRunner.cpp

    include/err/server_exception.hpp
    include/err/request_exception.hpp
    include/err/connection_exception.hpp
    src/err/server_exception.cpp
    src/err/request_exception.cpp
    src/err/connection_exception.cpp

    include/rqst/RequestParser.hpp
    include/rqst/ResponseConstructor.hpp
    include/rqst/RequestIdentifier.hpp
    include/rqst/Value.hpp
    include/rqst/Key.hpp
    src/rqst/RequestParser.cpp
    src/rqst/ResponseConstructor.cpp
    src/rqst/RequestIdentifier.cpp

    include/conn/Connection.hpp
    include/conn/Type.hpp

    include/proc/Processor.hpp
    src/proc/Processor.cpp
)
target_include_directories(mm_server PUBLIC include/)
target_link_libraries(mm_server PUBLIC -pthread)
target_compile_features(mm_server PUBLIC cxx_std_17)
target_compile_options(mm_server PUBLIC -Wall -Wextra -Wpedantic)
