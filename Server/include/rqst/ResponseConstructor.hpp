#pragma once

#include <string>
#include <map>
#include <vector>


namespace mm_server::utils {
    class ResponseConstructor {
        public:
            ResponseConstructor(int descriptor);
            void post_header(const std::map<std::string, std::string>& header);
            void post_content(const std::vector<std::vector<std::string>>& content);

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
