#pragma once

#include <exception>


namespace mm_server::err {
    class connection_exception : public std::exception {
        public:
            const char* what() const throw();
    };
}
