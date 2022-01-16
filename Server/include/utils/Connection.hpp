#pragma once

#include <string>

namespace mm_server::utils {
    struct Connection {
        int type = 0;
        std::string address;
    };
}
