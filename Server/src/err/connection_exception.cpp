#include <string>

#include <err/connection_exception.hpp>


namespace mm_server::err {
    connection_exception::connection_exception(int code) {
        this->code = code;
    }

    int connection_exception::get_code() const {
        return this->code;
    }

    const char* connection_exception::what() const throw() {
        return (std::string("Connection termination code: ") + std::to_string(this->code)).c_str();
    }
}
