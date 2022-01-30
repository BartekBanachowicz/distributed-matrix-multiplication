#include <iostream>
#include <vector>
#include <map>
#include <exception>
#include <string>
#include <cstring>
#include <csignal>
#include <unistd.h>
#include <chrono>
#include <thread>

#include <netinet/in.h>
#include <arpa/inet.h>

#include <UnitRunner.hpp>
#include <err/unit_exception.hpp>
#include <rqst/ResponseParser.hpp>
#include <rqst/RequestConstructor.hpp>


namespace mm_unit {
    UnitRunner::UnitRunner(int argc, char *argv[]) {
        sockaddr_in address;

        std::memset(&address, 0, sizeof(sockaddr_in));
        address.sin_family = AF_INET;

        // verify and set up the ip address
        if (argc != 3) {
            throw err::unit_exception(
                "Wrong number of program arguments received. Usage: mm_server [<ip_address>] <port_number>"
            );
        }

        if (inet_pton(AF_INET, argv[1], &address.sin_addr.s_addr) < 0) {
            throw err::unit_exception("Program received an invalid ipv4 address");
        }

        // verify and set up the port
        try {
            address.sin_port = htons(std::stoi(std::string(argv[argc - 1])));
        }
        catch (std::exception&) {
            throw err::unit_exception("Program received an invalid port number");
        }

        // create and set up the socket
        if ((this->socket_descriptor = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
            throw err::unit_exception("Unit socket creation error");
        }

        if (connect(this->socket_descriptor, (sockaddr*)(&address), sizeof(sockaddr_in)) == -1) {
            throw err::unit_exception("Server connection error");
        }
    }

    UnitRunner::~UnitRunner() {
        close(this->socket_descriptor);
    }

    void UnitRunner::run() {
        std::signal(SIGPIPE, SIG_IGN);
        try {
            rqst::RequestConstructor request_constructor(this->socket_descriptor);
            rqst::ResponseParser response_parser(this->socket_descriptor);
            std::map<std::string, std::string> header;
            std::vector<std::vector<std::string>> content;
            std::chrono::milliseconds timespan(5000);
            double result;

            header["PUT"] = "REGISTER-UNIT";
            request_constructor.post_header(header);
            header = response_parser.get_header();
            if (header.count("CODE") == 0 || static_cast<int>(header.size()) != 1) {
                throw err::unit_exception("Protocol error");
            }
            if (std::stoi(header["CODE"]) != 0) { throw err::unit_exception("Unit registration error"); }

            while (true) {
                header.clear();
                header["GET"] = "TASK";
                request_constructor.post_header(header);
                header = response_parser.get_header();
                if (header.count("CODE") == 0 || static_cast<int>(header.size()) > 2) {
                    throw err::unit_exception("Protocol error");
                }
                if (std::stoi(header["CODE"]) == 5) {
                    std::this_thread::sleep_for(timespan);
                    continue;
                }
                if (std::stoi(header["CODE"]) != 0) { throw err::unit_exception("Protocol error"); }
                content = response_parser.get_content();
                if (static_cast<int>(content.size()) != 2) { throw err::unit_exception("Protocol error"); }
                if (content.front().size() != content.back().size()) { throw err::unit_exception("Protocol error"); }
                result = 0.0;
                for (int i = 0; i < static_cast<int>(content.front().size()); ++i) {
                    result += std::stoi(content.front()[i]) * std::stoi(content.back()[i]);
                }
                header.clear();
                header["POST"] = "RESULT";
                request_constructor.post_header(header);
                content.clear();
                content = std::vector<std::vector<std::string>>{std::vector<std::string>{std::to_string(result)}};
                request_constructor.post_content(content);
                header = response_parser.get_header();
                if (header.count("CODE") == 0 || static_cast<int>(header.size()) != 1) {
                    throw err::unit_exception("Protocol error");
                }
                if ((std::stoi(header["CODE"]) != 5) && (std::stoi(header["CODE"]) != 0)) {
                    throw err::unit_exception("Protocol error");
                }
            }
        }
        catch (const err::unit_exception& err) {
            std::cerr << err.what() << std::endl;
        }

    }
}
