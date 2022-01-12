#include <iostream>
#include <exception>
#include <system_error>
#include <map>
#include <string>
#include <cstring>
#include <unistd.h>

#include <netinet/in.h>
#include <arpa/inet.h>

#include <utils/ServerRunner.hpp>
#include <utils/server_exception.hpp>


namespace mm_server::utils {
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
                    throw server_exception("Program received an invalid ipv4 address");
                }
                break;
            default:
                throw server_exception("Wrong number of program arguments received. Usage: mm_server [<ip_address>] <port_number>");
        }

        // verify and set up the port
        try {
            port = std::stoi(std::string(argv[argc - 1]));
        }
        catch (std::exception&) {
            throw server_exception("Program received an invalid port number");
        }

        if (port < 1024) {
            throw server_exception("Port number must be greater than 1023");
        }

        address.sin_port = htons(port);

        // create and set up the socket
        if ((this->socket_descriptor = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
            throw server_exception("Server socket creation error");
        }

        option_value = 1;
        if (setsockopt(this->socket_descriptor, SOL_SOCKET, SO_REUSEADDR, (char*)&option_value, sizeof(int)) == -1) {
            throw server_exception("Server socket option set error (SO_REUSEADDR)");
        }

        if (bind(this->socket_descriptor, (sockaddr*)&address, sizeof(sockaddr)) == -1) {
            throw server_exception("Server socket bind error");
        }

        // creates the epoll instance
        if ((this->epoll_descriptor = epoll_create1(0)) == -1) {
            throw server_exception("Epoll creation error");
        }

        event.events = EPOLLIN | EPOLLONESHOT;
        event.data.fd = this->socket_descriptor;
        if (epoll_ctl(this->epoll_descriptor, EPOLL_CTL_ADD, this->socket_descriptor, &event) == -1) {
            throw server_exception("Epoll addition error");
        }
    }

    ServerRunner::~ServerRunner() {
        this->clients_mutex.lock();
        for (const auto& [key, value] : this->clients) {
            close(value.descriptor);
        }

        this->clients_mutex.unlock();
        close(this->socket_descriptor);
    }

    void ServerRunner::accept_connection() {
        sockaddr address;
        socklen_t length = sizeof(sockaddr);
        TCPSocket tcp_socket;
        epoll_event event;

        if ((tcp_socket.descriptor = accept(this->socket_descriptor, &address, &length)) == -1) {
            throw server_exception("Client connection error");
        }

        tcp_socket.address = std::string(address.sa_data);

        event.events = EPOLLIN | EPOLLONESHOT;
        event.data.fd = tcp_socket.descriptor;
        std::cout << "Connected: " << tcp_socket.address << std::endl;
        this->clients_mutex.lock();
        this->clients[tcp_socket.descriptor] = std::move(tcp_socket);
        this->clients_mutex.unlock();
        if (epoll_ctl(this->epoll_descriptor, EPOLL_CTL_ADD, event.data.fd, &event) == -1) {
            throw server_exception("Epoll addition error");
        }
    }

    void ServerRunner::close_connection(int descriptor) {
        close(descriptor);
        this->clients_mutex.lock();
        this->clients.erase(this->clients.find(descriptor));
        this->clients_mutex.unlock();
    }


    void ServerRunner::handle_connection(epoll_event event) {
        char buffer[256];
        int bytes_read;
        bool client;

        this->clients_mutex.lock();
        client = (this->clients.count(event.data.fd)) == 1 ? true : false;
        this->clients_mutex.unlock();

        if (client) {
            switch (bytes_read = read(event.data.fd, buffer, 255)) {
                case -1:
                    throw server_exception("Client read error");
                    break;
                case 0:
                    this->close_connection(event.data.fd);
                    return;
                default:
                    buffer[bytes_read] = 0;
                    std::cout << "Received " << buffer << std::endl;
            }
        }
        else if (event.data.fd == this->socket_descriptor) {
            this->accept_connection();
        }
        else {
            throw server_exception("Closed socket connection attempt");
        }

        event.events = EPOLLIN | EPOLLONESHOT;
        if (epoll_ctl(this->epoll_descriptor, EPOLL_CTL_MOD, event.data.fd, &event) == -1) {
            throw server_exception("Epoll addition error");
        }
    }

    void ServerRunner::handle_connection_wrapper(epoll_event event, int thread_i) {
        try {
            this->handle_connection(event);
        }
        catch (server_exception& err) {
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
                throw server_exception("Epoll wait error");
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
            throw server_exception("Server socket listen error");
        }

        this->manage_connections();
    }
}
