#include <err/server_exception.hpp>


namespace mm_server::err {
    server_exception::server_exception(std::string message) {
        this->message = message;
    }

    const char* server_exception::what() const throw() { return this->message.c_str(); }
}
