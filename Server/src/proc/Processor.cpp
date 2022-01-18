#include <stdexcept>

#include <proc/Processor.hpp>
#include <err/request_exception.hpp>


namespace mm_server::proc {
    void Processor::set_left_matrix(const std::vector<std::vector<std::string>>& left_matrix) {
        bool error = false;

        std::vector<std::vector<double>> matrix(
            static_cast<int>(left_matrix.size()),
            std::vector<double>(static_cast<int>(left_matrix.size()), 0.0)
        );

        for (int i = 0; i < static_cast<int>(left_matrix.size()); ++i) {
            if (left_matrix[i].size() != left_matrix.size()) {
                throw err::request_exception(3);
            }

            for (int j = 0; j < static_cast<int>(left_matrix.size()); ++j) {
                try {
                    matrix[i][j] = std::stod(left_matrix[i][j]);
                }
                catch (const std::invalid_argument&) {
                    throw err::request_exception(3);
                }
            }
        }

        this->mtx.lock();
        error = error || (this->status != Status::idle);
        if (!this->right_matrix.empty()) {
            error = error || (matrix.size() != this->right_matrix.size());
            if (!error) { this->status = Status::ready; }
        }
        if (!error) { this->left_matrix = matrix; }
        this->mtx.unlock();

        if (error) { throw err::request_exception(3); }
    }

    void Processor::set_right_matrix(const std::vector<std::vector<std::string>>& right_matrix) {
        bool error = false;;

        std::vector<std::vector<double>> matrix(
            static_cast<int>(right_matrix.size()),
            std::vector<double>(static_cast<int>(right_matrix.size()), 0.0)
        );

        for (int i = 0; i < static_cast<int>(right_matrix.size()); ++i) {
            if (right_matrix[i].size() != right_matrix.size()) { throw err::request_exception(3); }
            for (int j = 0; j < static_cast<int>(right_matrix.size()); ++j) {
                try {
                    matrix[i][j] = std::stod(right_matrix[i][j]);
                }
                catch (const std::invalid_argument&) {
                    throw err::request_exception(3);
                }
            }
        }

        this->mtx.lock();
        error = error || (this->status != Status::idle);
        if (!this->left_matrix.empty()) {
            error = error || (matrix.size() != this->left_matrix.size());
            if (!error) { this->status = Status::ready; }
        }
        if (!error) { this->right_matrix = matrix; }
        this->mtx.unlock();

        if (error) { throw err::request_exception(3);}
    }

    void Processor::run() {
        bool error = false;

        this->mtx.lock();
        error = this->status != Status::ready;
        if (!error) {
            this->status = Status::running;
            this->size = static_cast<int>(this->left_matrix.size());
            for (int i = 0; i < this->size; ++i) {
                for (int j = 0; j < this->size; ++j) {
                    this->work_queue.push_back(std::tuple<int, int>{i, j});
                }
            }

            this->result_matrix = std::vector<std::vector<double>>(
                this->size, std::vector<double>(this->size, 0.0)
            );
        }
        this->mtx.unlock();

        if (error) { throw err::request_exception(5); }
    }

    void Processor::reset() {
        this->mtx.lock();
        this->status = Status::idle;
        this->left_matrix.clear();
        this->right_matrix.clear();
        this->result_matrix.clear();
        this->work_queue.clear();
        this->results_queue.clear();
        this->mtx.unlock();
    }

    std::string Processor::get_status() {
        std::string result;

        this->mtx.lock();
        switch (this->status) {
            case Status::idle:
                result = "IDLE";
                break;
            case Status::ready:
                result = "READY";
                break;
            case Status::running:
                result = "RUNNING";
                break;
            case Status::finished:
                result = "FINISHED";
                break;
        }

        this->mtx.unlock();
        return result;
    }

    std::vector<std::vector<std::string>> Processor::get_results_new() {
        std::vector<std::vector<std::string>> result;
        int x, y;
        double value;

        this->mtx.lock();
        while (!this->results_queue.empty()) {
            std::tie(x, y, value) = this->results_queue.back();
            result.push_back(
                std::vector<std::string>{std::to_string(x), std::to_string(y), std::to_string(value)}
            );
            this->results_queue.pop_back();
        }

        this->mtx.unlock();
        return result;
    }
}
