#include <unistd.h>

#include <rqst/RequestParser.hpp>
#include <err/server_exception.hpp>
#include <err/request_exception.hpp>
#include <err/connection_exception.hpp>


namespace mm_server::rqst {
    RequestParser::RequestParser(int descriptor) : descriptor(descriptor) {}

    void RequestParser::initialize() {
        this->header.clear();
        this->content.clear();
        this->content_line.clear();
        this->key.clear();
        this->value.clear();
    }

    void RequestParser::newline_handler() {
        switch (this->state) {
            case 3:
                this->header[std::move(this->key)] = std::move(this->value);
                this->key.clear();
                this->value.clear();
                this->state = 6;
                break;
            case 5:
                this->content_line.push_back(std::move(this->value));
                this->value.clear();
                this->content.push_back(std::move(this->content_line));
                this->state = 6;
                break;
            default:
                throw err::request_exception(1);
        }
    }

    void RequestParser::space_handler() {
        switch (this->state) {
            case 0: case 2: case 4:
                break;
            case 1:
                this->state = 2;
                break;
            case 5:
                this->content_line.push_back(std::move(this->value));
                this->value.clear();
                this->state = 4;
                break;
            default:
                throw err::request_exception(1);
        }
    }

    void RequestParser::semicolon_handler() {
        switch (this->state) {
            case 3:
                this->header[std::move(this->key)] = std::move(this->value);
                this->key.clear();
                this->value.clear();
                this->state = 0;
                break;
            case 5:
                this->content_line.push_back(std::move(this->value));
                this->value.clear();
                this->content.push_back(std::move(this->content_line));
                this->state = 4;
                break;
            default:
                throw err::request_exception(1);
        }
    }

    void RequestParser::char_handler() {
        switch (this->state) {
            case 0:
                this->state = 1;
                [[fallthrough]];
            case 1:
                this->key.push_back(this->buffer[this->i]);
                break;
            case 2:
                this->state = 3;
                [[fallthrough]];
            case 3:
                this->value.push_back(this->buffer[this->i]);
                break;
            case 4:
                this->state = 5;
                [[fallthrough]];
            case 5:
                this->value.push_back(this->buffer[this->i]);
                break;
            default:
                throw err::request_exception(1);
        }
    }

    void RequestParser::parse() {
        while (this->i < this->bytes_read) {
            switch (this->buffer[this->i]) {
                case ' ':
                    this->space_handler();
                    break;
                case ';':
                    this->semicolon_handler();
                    break;
                case '\n':
                    this->newline_handler();
                    ++this->i;
                    return;
                default:
                    this->char_handler();
            }

            ++this->i;
        }
    }

    void RequestParser::get() {
        while (true) {
            this->bytes_read = read(this->descriptor, this->buffer, 1024);
            this->i = 0;
            if (this->bytes_read == -1) {
                throw err::server_exception("Connection read error");
            }

            if (this->bytes_read == 0) {
                throw err::connection_exception();
            }

            this->parse();
            if (this->state == 6) { return; }
        }
    }

    std::map<std::string, std::string> RequestParser::get_header() {
        this->initialize();
        this->state = 0;
        this->get();
        if (this->state != 6) {
            throw err::request_exception(1);
        }

        return this->header;
    }

    std::vector<std::vector<std::string>> RequestParser::get_content() {
        this->initialize();
        this->state = 4;
        this->parse();
        if (this->state != 6) {
            this->get();
        }

        if (this->state != 6) {
            throw err::request_exception(1);
        }

        return this->content;
    }
}
