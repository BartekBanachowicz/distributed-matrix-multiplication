#pragma once

#include <map>
#include <mutex>
#include <atomic>
#include <thread>
#include <array>
#include <condition_variable>

#include <sys/epoll.h>

#include <utils/TCPSocket.hpp>


namespace mm_server::utils {
    class ServerRunner {
        public:
            ServerRunner(int argc, char *argv[]);
            ~ServerRunner();
            void run();

        private:
            std::atomic<bool> running = false;
            int socket_descriptor;
            int epoll_descriptor;
            std::mutex threads_mutex;
            std::unique_lock<std::mutex> threads_lock{threads_mutex, std::defer_lock};
            std::condition_variable threads_var;
            std::array<bool, 5> threads_idle = {true, true, true, true, true};
            std::array<std::thread, 5> threads;
            std::map<int, TCPSocket> clients;
            std::mutex clients_mutex;

            void accept_connection();
            void close_connection(int descriptor);
            void handle_connection(epoll_event event);
            void handle_connection_wrapper(epoll_event event, int thread_i);
            void manage_connections();


    };
}
