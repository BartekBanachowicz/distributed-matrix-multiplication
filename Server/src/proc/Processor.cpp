#include <stdexcept>

#include <proc/Processor.hpp>
#include <err/request_exception.hpp>


namespace mm_server::proc {
    void Processor::set_left_matrix(std::vector<std::vector<std::string>> left_matrix) {
        bool sizes_match = true;

        std::vector<std::vector<double>> matrix(
            static_cast<int>(left_matrix.size()),
            std::vector<double>(static_cast<int>(left_matrix.size()), 0.0)
        );

        for (int i = 0; i < left_matrix.size(); ++i) {
            if (left_matrix[i].size() != left_matrix.size()) { throw err::request_exception(11); }
            for (int j = 0; j < left_matrix.size(); ++j) {
                try {
                    matrix[i][j] = std::stod(left_matrix[i][j]);
                }
                catch (const std::invalid_argument&) {
                    throw err::request_exception(2);
                }
            }
        }

        this->mtx.lock();
        if (!this->right_matrix.empty()) { sizes_match = left_matrix.size() == this->right_matrix.size(); }
        if (sizes_match) { this->left_matrix = left_matrix; }
        this->mtx.unlock();

        if (!sizes_match) { throw err::request_exception(12); }
    }

    void Processor::set_right_matrix(std::vector<std::vector<std::string>> right_matrix) {
        bool sizes_match = true;
        std::vector<std::vector<double>> matrix(
            static_cast<int>(right_matrix.size()),
            std::vector<double>(static_cast<int>(right_matrix.size()), 0.0)
        );

        for (int i = 0; i < right_matrix.size(); ++i) {
            if (right_matrix[i].size() != right_matrix.size()) { throw err::request_exception(11); }
            for (int j = 0; j < right_matrix.size(); ++j) {
                try {
                    matrix[i][j] = std::stod(right_matrix[i][j]);
                }
                catch (const std::invalid_argument&) {
                    throw err::request_exception(2);
                }
            }
        }

        this->mtx.lock();
        if (!this->left_matrix.empty()) { sizes_match = right_matrix.size() == this->left_matrix.size(); }
        if (sizes_match) { this->right_matrix = matrix; }
        this->mtx.unlock();

        if (!sizes_match) { throw err::request_exception(12); }
    }

    void Processor::run() {
        bool missing_data;

        this->mtx.lock();
        if (!(missing_data = this->left_matrix.empty() && this->right_matrix.empty())) {
            this->status = Status::running;
            this->size = static_cast<int>(this->left_matrix.size());
            for (int i = 0; i < this->size; ++i) {
                for (int j = 0; j < this->size; ++j) {
                    this->work_queue.push(std::tuple<int, int>{i, j});
                }
            }

            this->result_matrix = std::vector<std::vector<double>>(
                this->size, std::vector<double>(this->size, 0.0)
            );
        }
        this->mtx.unlock();

        if (missing_data) { throw err::request_exception(6); }
    }

    std::string Processor::get_status() {
        std::string result;

        this->mtx.lock();
        switch (this->status) {
            case Status::idle:
                result = "IDLE";
                break;
            case Status::running:
                result = "RUNNING";
                break;
            case Status::stopped:
                result = "STOPPED";
                break;
        }

        this->mtx.unlock();
        return result;
    }

    std::vector<std::vector<std::string>> Processor::get_update() {
        std::vector<std::vector<std::string>> result;
        int x, y;
        double value;

        this->mtx.lock();
        while (!this->results_queue.empty()) {
            std::tie(x, y, value) = this->results_queue.top();
            result.push_back(
                std::vector<std::string>{std::to_string(x), std::to_string(y), std::to_string(value)}
            );
            this->results_queue.pop();
        }

        this->mtx.unlock();
        return result;
    }
}
