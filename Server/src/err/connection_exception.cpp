#include <string>

#include <err/connection_exception.hpp>


namespace mm_server::err {
    const char* connection_exception::what() const throw() {
        return "Connection closed";
    }
}
