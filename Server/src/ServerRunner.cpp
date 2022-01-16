#include <iostream>
#include <stdexcept>
#include <exception>
#include <system_error>
#include <map>
#include <string>
#include <cstring>
#include <unistd.h>

#include <netinet/in.h>
#include <arpa/inet.h>

#include <ServerRunner.hpp>
#include <utils/RequestParser.hpp>
#include <utils/ResponseConstructor.hpp>
#include <utils/Value.hpp>
#include <err/server_exception.hpp>
#include <err/connection_exception.hpp>


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

        if (bind(this->socket_descriptor, (sockaddr*)&address, sizeof(sockaddr)) == -1) {
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
        sockaddr address;
        socklen_t length = sizeof(sockaddr);
        utils::Connection connection;
        epoll_event event;

        if ((event.data.fd = accept(this->socket_descriptor, &address, &length)) == -1) {
            throw err::server_exception("Client connection error");
        }

        event.events = EPOLLIN | EPOLLONESHOT;

        connection.address = std::string(address.sa_data);
        this->connections_mutex.lock();
        this->connections[event.data.fd] = std::move(connection);
        this->connections_mutex.unlock();
        if (epoll_ctl(this->epoll_descriptor, EPOLL_CTL_ADD, event.data.fd, &event) == -1) {
            throw err::server_exception("Epoll addition error");
        }
    }

    void ServerRunner::close_connection(int descriptor, int code) {
        if (code != 0) {
            utils::ResponseConstructor response_constructor(descriptor);
            std::map<std::string, std::string> header;
            header["CODE"] = std::to_string(code);
            response_constructor.post_header(header);
        }

        close(descriptor);
        this->connections_mutex.lock();
        this->connections.erase(this->connections.find(descriptor));
        this->connections_mutex.unlock();
    }

    void ServerRunner::process_unassigned(int descriptor) {
        int value;
        utils::RequestParser request_parser(descriptor);
        utils::ResponseConstructor response_constructor(descriptor);
        std::map<std::string, std::string> header = request_parser.get_header();

        try {
            if (header.at("POST") != "REGISTER") {
                throw err::connection_exception(1);
            }

            switch (this->value_identifier.at(header.at("TYPE"), value)) {
                case utils::Value::unit:
                    this->connections_mutex.lock();
                    this->connections[descriptor].type = 1;
                    this->connections_mutex.unlock();
                    break;
                case utils::Value::client:
                    this->connections_mutex.lock();
                    this->connections[descriptor].type = 2;
                    this->connections_mutex.unlock();
                    break;
                default:
                    std::cout << value << std::endl;
                    throw err::connection_exception(1);
            }
        }
        catch (const std::out_of_range&) {
            throw err::connection_exception(1);
        }

        header.clear();
        header["CODE"] = "0";
        response_constructor.post_header(header);
    }

    void ServerRunner::process_request(int descriptor) {
        int type;
        this->connections_mutex.lock();
        type = this->connections[descriptor].type;
        this->connections_mutex.unlock();

        process_unassigned(descriptor);

        // switch (type) {
        //     case 0:
        //         process_unassigned(descriptor);
        //         break;
        // }
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
            catch (const err::connection_exception& err) {
                this->close_connection(event.data.fd, err.get_code());
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
