cmake_minimum_required(VERSION 3.0.0)
project(mm_server)

add_executable(mm_server
    src/main.cpp

    include/ServerRunner.hpp
    src/ServerRunner.cpp

    include/err/server_exception.hpp
    include/err/connection_exception.hpp
    src/err/server_exception.cpp
    src/err/connection_exception.cpp

    include/utils/RequestParser.hpp
    include/utils/ResponseConstructor.hpp
    include/utils/Connection.hpp
    include/utils/ValueIdentifier.hpp
    include/utils/Value.hpp
    include/utils/Type.hpp
    src/utils/RequestParser.cpp
    src/utils/ResponseConstructor.cpp
    src/utils/ValueIdentifier.cpp

    include/proc/Processor.hpp
    src/proc/Processor.cpp
)
target_include_directories(mm_server PUBLIC include/)
target_link_libraries(mm_server PUBLIC -pthread)
target_compile_features(mm_server PUBLIC cxx_std_17)
target_compile_options(mm_server PUBLIC -Wall -Wextra -Wpedantic)
