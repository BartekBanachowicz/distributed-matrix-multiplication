#pragma once

#include <map>
#include <vector>
#include <string>


namespace mm_server::utils {
    class RequestParser {
        public:
            RequestParser(int descriptor);
            std::map<std::string, std::string> get_header();
            std::vector<std::vector<double>> get_content();

        private:
            std::map<std::string, std::string> header;
            std::vector<std::vector<double>> content;
            std::vector<double> content_line;
            std::string key;
            std::string value;
            int descriptor;
            int state = 0;
            int i = 0;
            int bytes_read = 0;
            char buffer[1024];

            void initialize();
            void newline_handler();
            void space_handler();
            void semicolon_handler();
            void char_handler();
            void parse();
            void get();
    };
}
