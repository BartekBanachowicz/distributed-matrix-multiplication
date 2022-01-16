#pragma once

#include <exception>


namespace mm_server::err {
    class connection_exception : public std::exception {
        public:
            connection_exception(int code);
            int get_code() const;
            const char* what() const throw();

        private:
            int code;
    };
}
