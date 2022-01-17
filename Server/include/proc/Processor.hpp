#pragma once

#include <mutex>
#include <string>
#include <vector>
#include <stack>
#include <tuple>


namespace mm_server::proc {
    class Processor {
        public:
            void set_left_matrix(std::vector<std::vector<std::string>> left_matrix);
            void set_right_matrix(std::vector<std::vector<std::string>> right_matrix);
            void run();
            std::string get_status();
            std::vector<std::vector<std::string>> get_update();

        private:
            enum class Status {
                idle,
                running,
                stopped
            };

            Status status = Status::idle;
            std::mutex mtx;
            std::vector<std::vector<double>> left_matrix;
            std::vector<std::vector<double>> right_matrix;
            int size;
            std::vector<std::vector<double>> result_matrix;
            std::stack<std::tuple<int, int>> work_queue;
            std::stack<std::tuple<int, int, double>> results_queue;

    };
}
