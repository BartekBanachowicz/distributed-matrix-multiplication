#pragma once

#include <string>

namespace mm_server::utils {
    struct TCPSocket {
        int type = 0;
        int descriptor;
        std::string address;
    };
}
