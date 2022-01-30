#include <err/unit_exception.hpp>


namespace mm_unit::err {
    unit_exception::unit_exception(std::string message) {
        this->message = message;
    }

    const char* unit_exception::what() const throw() { return this->message.c_str(); }
}
