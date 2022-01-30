#include <unistd.h>

#include <rqst/ResponseParser.hpp>
#include <err/unit_exception.hpp>


namespace mm_unit::rqst {
    ResponseParser::ResponseParser(int descriptor) : descriptor(descriptor) {}

    void ResponseParser::initialize() {
        this->header.clear();
        this->content.clear();
        this->content_line.clear();
        this->key.clear();
        this->value.clear();
    }

    void ResponseParser::newline_handler() {
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
                throw err::unit_exception("Protocol error");
        }
    }

    void ResponseParser::space_handler() {
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
                throw err::unit_exception("Protocol error");
        }
    }

    void ResponseParser::semicolon_handler() {
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
                throw err::unit_exception("Protocol error");
        }
    }

    void ResponseParser::char_handler() {
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
                throw err::unit_exception("Protocol error");
        }
    }

    void ResponseParser::parse() {
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

    void ResponseParser::get() {
        while (true) {
            this->bytes_read = read(this->descriptor, this->buffer, 1024);
            this->i = 0;
            if (this->bytes_read == -1) {
                throw err::unit_exception("Read error");
            }

            if (this->bytes_read == 0) {
                throw err::unit_exception("Server down");
            }

            this->parse();
            if (this->state == 6) { return; }
        }
    }

    std::map<std::string, std::string> ResponseParser::get_header() {
        this->initialize();
        this->state = 0;
        this->get();
        if (this->state != 6) {
            throw err::unit_exception("Protocol error");
        }

        return this->header;
    }

    std::vector<std::vector<std::string>> ResponseParser::get_content() {
        this->initialize();
        this->state = 4;
        this->parse();
        if (this->state != 6) {
            this->get();
        }

        if (this->state != 6) {
            throw err::unit_exception("Protocol error");
        }

        return this->content;
    }
}
