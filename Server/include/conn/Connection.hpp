#pragma once

#include <string>

#include <conn/Type.hpp>


namespace mm_server::conn {
    struct Connection {
        Type type = Type::unassigned;
        std::string address;
    };
}
