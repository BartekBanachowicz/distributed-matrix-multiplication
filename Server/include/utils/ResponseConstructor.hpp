#pragma once

#include <string>
#include <map>


namespace mm_server::utils {
    class ResponseConstructor {
        public:
            ResponseConstructor(int descriptor);
            void post_header(const std::map<std::string, std::string>& header);

        private:
            int descriptor;
            std::string buffer;
            int bytes_sent;
            int x;
            int y;
            int x_i;
            int y_i;

            void initialize(int x, int y);
            void construct(const std::string& value);
            void post();

    };
}
