#include <algorithm>
#include <unistd.h>

#include <rqst/ResponseConstructor.hpp>
#include <err/server_exception.hpp>
#include <err/connection_exception.hpp>


namespace mm_server::rqst {
    ResponseConstructor::ResponseConstructor(int descriptor) : descriptor(descriptor) {}

    void ResponseConstructor::initialize() {
        this->buffer.clear();
        this->bytes_sent = 0;
        this->x_i = 1;
        this->y_i = 1;
        this->x = 1;
        this->y = 1;
    }

    void ResponseConstructor::post() {
        int bytes;
        while (this->bytes_sent < static_cast<int>(this->buffer.size())) {
            if ((bytes = write(
                this->descriptor,
                this->buffer.c_str() + this->bytes_sent,
                std::min(1024, static_cast<int>(this->buffer.size()) - this->bytes_sent)
            )) == -1) {
                if (errno == EPIPE) { throw err::connection_exception();}
                throw err::server_exception("Connection write error");
            }

            this->bytes_sent += bytes;
        }

        this->bytes_sent = 0;
    }

    void ResponseConstructor::construct(const std::string& value) {
        if (static_cast<int>(value.size() + this->buffer.size()) <= 1023) {
            this->buffer += value;
        }
        else if (this->buffer.size() == 0) {
            this->buffer = value;
        }

        if (this->x_i == this->x && this->y_i == this->y) {
            this->buffer.push_back('\n');
            this->post();
            return;
        }
        else if (this->y_i == this->y) {
            this->buffer.push_back(';');
            this->x_i += 1;
            this->y_i = 1;
        }
        else {
            this->buffer.push_back(' ');
            this->y_i += 1;
        }

        if (static_cast<int>(this->buffer.size()) >= 1024) {
            this->post();
        }
    }

    void ResponseConstructor::post_header(const std::map<std::string, std::string>& header) {
        this->initialize();
        this->x = static_cast<int>(header.size());
        this->y = 2;
        for (const auto& [key, value] : header) {
            this->construct(key);
            this->construct(value);
        }
    }

    void ResponseConstructor::post_content(const std::vector<std::vector<std::string>>& content) {
        this->initialize();
        this->x = static_cast<int>(content.size());
        for (const std::vector<std::string>& row : content) {
            this->y = static_cast<int>(row.size());
            for (const std::string& item : row) {
                this->construct(item);
            }
        }
    }
}
