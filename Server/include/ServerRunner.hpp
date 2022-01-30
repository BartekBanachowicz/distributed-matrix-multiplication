#pragma once

#include <map>
#include <mutex>
#include <atomic>
#include <thread>
#include <array>
#include <condition_variable>

#include <sys/epoll.h>

#include <conn/Connection.hpp>
#include <rqst/RequestIdentifier.hpp>
#include <rqst/Value.hpp>
#include <proc/Processor.hpp>


namespace mm_server {
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
            std::condition_variable threads_var;
            std::array<bool, 5> threads_idle = {true, true, true, true, true};
            std::map<int, conn::Connection> connections;
            std::mutex connections_mutex;
            rqst::RequestIdentifier request_identifier;
            proc::Processor processor;

            void accept_connection();
            void close_connection(int descriptor);
            void get_handler(
                int descriptor,
                conn::Type connection_type,
                rqst::Value request_value,
                std::map<std::string, std::string>& header,
                std::vector<std::vector<std::string>>& content
            );
            void put_handler(int descriptor, conn::Type connection_type, rqst::Value request_value);
            void post_handler(
                int descriptor,
                conn::Type connection_type,
                rqst::Value request_value,
                std::vector<std::vector<std::string>> content
            );
            void process_request(int descriptor);
            void handle_connection(epoll_event event);
            void handle_connection_wrapper(epoll_event event, int thread_i);
            void manage_connections();


    };
}
