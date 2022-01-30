#pragma once

#include <mutex>
#include <string>
#include <vector>
#include <tuple>
#include <unordered_map>


namespace mm_server::proc {
    class Processor {
        public:
            void set_left_matrix(const std::vector<std::vector<std::string>>& left_matrix);
            void set_right_matrix(const std::vector<std::vector<std::string>>& right_matrix);
            void reset();
            std::string get_status();
            std::vector<std::vector<std::string>> get_results_new();
            std::vector<std::vector<std::string>> get_results_full();
            std::vector<std::vector<std::string>> assign_task(int descriptor);
            void revoke_task(int descriptor);
            void collect_result(int descriptor, const std::vector<std::vector<std::string>>& result);

        private:
            enum class Status {
                idle,
                running,
                finished
            };

            Status status = Status::idle;
            std::mutex mtx;
            std::vector<std::vector<double>> left_matrix;
            std::vector<std::vector<double>> right_matrix;
            int size = 0;
            std::vector<std::vector<double>> result_matrix;
            std::vector<std::tuple<int, int>> work_queue;
            std::vector<std::tuple<int, int, double>> results_queue;
            std::unordered_map<int, std::tuple<int, int>> assigned_tasks;

            void initialize();
            void set_matrix(
                const std::vector<std::vector<std::string>>& source_matrix,
                std::vector<std::vector<double>>& destination_matrix
            );

    };
}
