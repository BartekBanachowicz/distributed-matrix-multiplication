#pragma once

#include <string>

#include <utils/Type.hpp>


namespace mm_server::conn {
    struct Connection {
        Type type = Type::unassigned;
        std::string address;
    };
}
