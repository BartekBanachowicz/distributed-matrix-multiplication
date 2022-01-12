#pragma once

#include <exception>
#include <string>


namespace mm_server::utils {
    struct server_exception: public std::exception {
        std::string message;

        server_exception(std::string message);

        virtual const char* what() const throw();
    };
}
