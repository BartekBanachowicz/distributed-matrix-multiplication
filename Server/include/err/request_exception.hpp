#pragma once

#include <exception>


namespace mm_server::err {
    class request_exception : public std::exception {
        public:
            request_exception(int code);
            int get_code() const;
            const char* what() const throw();

        private:
            int code;
    };
}
