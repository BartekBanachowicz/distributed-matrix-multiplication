#include <string>

#include <err/request_exception.hpp>


namespace mm_server::err {
    connection_exception::request_exception(int code) {
        this->code = code;
    }

    int request_exception::get_code() const {
        return this->code;
    }

    const char* request_exception::what() const throw() {
        return (std::string("Request error code: ") + std::to_string(this->code)).c_str();
    }
}
