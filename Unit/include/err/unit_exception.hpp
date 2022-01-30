#pragma once

#include <exception>
#include <string>


namespace mm_unit::err {
    class unit_exception : public std::exception {
        public:
            unit_exception(std::string message);
            const char* what() const throw();

        private:
            std::string message;
    };
}
