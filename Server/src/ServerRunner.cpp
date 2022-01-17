#include <iostream>
#include <exception>
#include <map>
#include <string>
#include <cstring>
#include <unistd.h>

#include <netinet/in.h>
#include <arpa/inet.h>

#include <ServerRunner.hpp>
#include <conn/Type.hpp>
#include <err/server_exception.hpp>
#include <err/request_exception.hpp>
#include <err/connection_exception.hpp>
#include <rqst/Key.hpp>
#include <rqst/RequestParser.hpp>
#include <rqst/ResponseConstructor.hpp>


namespace mm_server {
    ServerRunner::ServerRunner(int argc, char *argv[]) {
        int port;
        int option_value;
        sockaddr_in address;
        epoll_event event;

        std::memset(&address, 0, sizeof(sockaddr_in));
        address.sin_family = AF_INET;

        // verify and set up the ip address
        switch (argc) {
            case 2:
                address.sin_addr.s_addr = htonl(INADDR_ANY);
                break;
            case 3:
                if (inet_pton(AF_INET, argv[1], &address.sin_addr.s_addr) < 0) {
                    throw err::server_exception("Program received an invalid ipv4 address");
                }
                break;
            default:
                throw err::server_exception("Wrong number of program arguments received. Usage: mm_server [<ip_address>] <port_number>");
        }

        // verify and set up the port
        try {
            port = std::stoi(std::string(argv[argc - 1]));
        }
        catch (std::exception&) {
            throw err::server_exception("Program received an invalid port number");
        }

        if (port < 1024) {
            throw err::server_exception("Port number must be greater than 1023");
        }

        address.sin_port = htons(port);

        // create and set up the socket
        if ((this->socket_descriptor = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
            throw err::server_exception("Server socket creation error");
        }

        option_value = 1;
        if (setsockopt(this->socket_descriptor, SOL_SOCKET, SO_REUSEADDR, (char*)&option_value, sizeof(int)) == -1) {
            throw err::server_exception("Server socket option set error (SO_REUSEADDR)");
        }

        if (bind(this->socket_descriptor, (sockaddr*)(&address), sizeof(sockaddr_in)) == -1) {
            throw err::server_exception("Server socket bind error");
        }

        // creates the epoll instance
        if ((this->epoll_descriptor = epoll_create1(0)) == -1) {
            throw err::server_exception("Epoll creation error");
        }

        event.events = EPOLLIN | EPOLLONESHOT;
        event.data.fd = this->socket_descriptor;
        if (epoll_ctl(this->epoll_descriptor, EPOLL_CTL_ADD, this->socket_descriptor, &event) == -1) {
            throw err::server_exception("Epoll addition error");
        }
    }

    ServerRunner::~ServerRunner() {
        this->connections_mutex.lock();
        for (const auto& [key, value] : this->connections) {
            close(key);
        }

        this->connections_mutex.unlock();
        close(this->socket_descriptor);
    }

    void ServerRunner::accept_connection() {
        char buffer[INET6_ADDRSTRLEN];
        sockaddr_in address;
        socklen_t length = sizeof(sockaddr_in);
        conn::Connection connection;
        epoll_event event;

        if ((event.data.fd = accept(this->socket_descriptor,(sockaddr*)(&address), &length)) == -1) {
            throw err::server_exception("Client connection error");
        }

        event.events = EPOLLIN | EPOLLONESHOT;

        if (inet_ntop(address.sin_family, &address.sin_addr, buffer, INET6_ADDRSTRLEN) == nullptr) {
            throw err::server_exception("Client address read error");
        }

        connection.address = std::string(buffer);
        std::cout << connection.address << std::endl;
        this->connections_mutex.lock();
        this->connections[event.data.fd] = std::move(connection);
        this->connections_mutex.unlock();
        if (epoll_ctl(this->epoll_descriptor, EPOLL_CTL_ADD, event.data.fd, &event) == -1) {
            throw err::server_exception("Epoll addition error");
        }
    }

    void ServerRunner::close_connection(int descriptor) {
        close(descriptor);
        this->connections_mutex.lock();
        this->connections.erase(this->connections.find(descriptor));
        this->connections_mutex.unlock();
    }

    void ServerRunner::process_unassigned(int descriptor) {
        utils::RequestParser request_parser(descriptor);
        utils::ResponseConstructor response_constructor(descriptor);
        std::map<std::string, std::string> header = request_parser.get_header();
        int value;
        bool server_busy = false;

        try {
            if (this->value_identifier.at(header.at("POST"), value) != utils::Value::enter) {
                throw err::request_exception(1);
            }

            switch (this->value_identifier.at(header.at("TYPE"), value)) {
                case utils::Value::unit:
                    this->connections_mutex.lock();
                    this->connections[descriptor].type = utils::Type::unit;
                    this->connections_mutex.unlock();
                    break;
                case utils::Value::client:
                    this->connections_mutex.lock();
                    for (const auto& [key, value] : this->connections) {
                        if (value.type == utils::Type::client) { server_busy = true; }
                    }

                    this->connections[descriptor].type = utils::Type::client;
                    this->connections_mutex.unlock();
                    break;
                default:
                    throw err::request_exception(1);
            }
        }
        catch (const std::out_of_range&) {
            throw err::request_exception(1);
        }

        if (server_busy) { throw err::request_exception(2); }

        header.clear();
        header["CODE"] = "0";
        response_constructor.post_header(header);
    }

    void ServerRunner::process_client(int descriptor) {
        utils::RequestParser request_parser(descriptor);
        utils::ResponseConstructor response_constructor(descriptor);
        std::map<std::string, std::string> request_header = request_parser.get_header();
        std::map<std::string, std::string> response_header;
        int value;
        bool units_ready = false;
        std::vector<std::string> units;
        std::vector<std::string> results;

        response_header["CODE"] = "0";

        try {
            if (!request_header["GET"].empty()) {
                if (this->value_identifier.at(request_header["GET"], value) == utils::Value::status) {
                    this->connections_mutex.lock();
                    for (const auto& [key, value] : this->connections) {
                        if (value.type == utils::Type::unit) { units.push_back(value.address); }
                    }

                    this->connections_mutex.unlock();
                    response_header["STATUS"] = this->processor.get_status();
                    if (units.size() != 0) { response_header["UNITS"] = std::to_string(units.size()); }
                    results = this->processor.get_update();
                    if (results.size() != 0) { response_header["RESULTS"] = std::to_string(results.size()); }
                    response_constructor.post_header(response_header);
                    response_constructor.post_content(units, 1);
                    response_constructor.post_content(results, 3);
                }
            }
            else if (!request_header["POST"].empty()) {
                switch (this->value_identifier.at(request_header["POST"], value)) {
                    case utils::Value::left_matrix:
                        this->processor.set_left_matrix(request_parser.get_content());
                        break;
                    case utils::Value::right_matrix:
                        this->processor.set_right_matrix(request_parser.get_content());
                        break;
                    case utils::Value::start:
                        this->connections_mutex.lock();
                        for (const auto& [key, value] : this->connections) {
                            if (value.type == utils::Type::unit) { units_ready = true; }
                        }

                        this->connections_mutex.unlock();
                        // UNCOMMENT WHEN UNIT READY
                        // if (!units_ready) { throw err::request_exception(13); }

                        this->processor.run();
                        break;
                }

                response_constructor.post_header(response_header);
            }
            else {
                throw err::request_exception(1);
            }
        }
        catch (const std::out_of_range&) {
            throw err::request_exception(1);
        }
    }

    void ServerRunner::get_handler(
        rqst::Value request_value,
        std::map<std::string, std::string>& header,
        std::vector<std::vector<std::string>>& content
    ) {
        switch (request_value) {
            case rqst::Value::status:
                this->connections_mutex.lock();
                for (const auto& [key, value] : this->connections) {
                    if (value.type == utils::Type::unit) {
                        content.push_back(std::vector<std::string>{value.address});
                    }
                }

                this->connections_mutex.unlock();
                header["STATUS"] = this->processor.get_status();
                header["UNITS"] = std::to_string(content.size());
                break;
            case rqst::Value::update_new:
                content = this->processor.get_update();
                header["RESULTS"] = std::to_string(content.size());
                break;
            case rqst::Value::update_full:
                // NOT IMPLEMENTED
                throw err::request_exception(8);
                break;
        }
    }

    void ServerRunner::process_request(int descriptor) {
        utils::Type type;
        this->connections_mutex.lock();
        type = this->connections[descriptor].type;
        this->connections_mutex.unlock();

        rqst::RequestParser request_parser(descriptor);
        rqst::ResponseConstructor response_constructor(descriptor);
        std::map<std::string, std::string> header;
        std::vector<std::vector<std::string>> content;

        try {
            header = request_parser.get_header();
            if (header.size() != 1) {
                throw err::request_exception(1);
            }

            rqst::Key request_key = this->request_identifier.key_at(header.begin()->first);
            rqst::Value request_value = this->request_identifier.value_at(request_key, header.begin()->second);
            header.clear();
            header["CODE"] = "0";
            switch (key) {
                case rqst::Key::get:
                    this->get_handler(request_value, header, content);
                    break;
                case rqst::Key::put:
                    this->put_handler(request_value);
                    break;
                case rqst::Key::post:
                    this->post_handler(request_value, request_parser.get_content());
                    break;
            }
        }
        catch (const err::request_exception& err) {
            header.clear();
            content.clear();
            header["CODE"] = std::to_string(err.get_code());
        }

        response_constructor.post_header(header);
        response_constructor.post_content(header);
    }

    void ServerRunner::handle_connection(epoll_event event) {
        bool client;

        this->connections_mutex.lock();
        client = (this->connections.count(event.data.fd)) == 1 ? true : false;
        this->connections_mutex.unlock();

        if (client) {
            try {
                this->process_request(event.data.fd);
            }
            catch (const err::connection_exception&) {
                this->close_connection(event.data.fd);
                return;
            }
        }
        else if (event.data.fd == this->socket_descriptor) {
            this->accept_connection();
        }
        else {
            throw err::server_exception("Closed socket connection attempt");
        }

        event.events = EPOLLIN | EPOLLONESHOT;
        if (epoll_ctl(this->epoll_descriptor, EPOLL_CTL_MOD, event.data.fd, &event) == -1) {
            throw err::server_exception("Epoll addition error");
        }
    }

    void ServerRunner::handle_connection_wrapper(epoll_event event, int thread_i) {
        try {
            this->handle_connection(event);
        }
        catch (const err::server_exception& err) {
            std::cerr << err.what() << std::endl;
            this->running = false;
        }

        this->threads_lock.lock();
        this->threads_idle[thread_i] = true;
        this->threads[thread_i].detach();
        this->threads_lock.unlock();
        this->threads_var.notify_one();
    }

    void ServerRunner::manage_connections() {
        epoll_event events[10];
        int ready_descriptors;
        int thread_i;
        bool thread_free;

        while (this->running) {
            if ((ready_descriptors = epoll_wait(this->epoll_descriptor, events, 10, 10)) == -1) {
                throw err::server_exception("Epoll wait error");
            }

            for (int i = 0; i < ready_descriptors; ++i) {
                if (this->running) {
                    this->threads_lock.lock();

                    while (true) {
                        thread_free = false;
                        for (thread_i = 0; thread_i < this->threads_idle.size(); ++thread_i) {
                            if (this->threads_idle[thread_i]) {
                                thread_free = true;
                                break;
                            }
                        }

                        if (thread_free) {
                            this->threads_idle[thread_i] = false;
                            this->threads[thread_i] = std::thread(&ServerRunner::handle_connection_wrapper, this, events[i], thread_i);
                            break;
                        }
                        else {
                            this->threads_var.wait(this->threads_lock);
                        }
                    }

                    this->threads_lock.unlock();
                }
            }
        }
    }

    void ServerRunner::run() {
        this->running = true;

        if (listen(this->socket_descriptor, 5) == -1) {
            throw err::server_exception("Server socket listen error");
        }

        this->manage_connections();
    }
}
