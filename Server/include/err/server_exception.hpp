#pragma once

#include <exception>
#include <string>


namespace mm_server::err {
    class server_exception : public std::exception {
        public:
            server_exception(std::string message);
            const char* what() const throw();

        private:
            std::string message;
    };
}
