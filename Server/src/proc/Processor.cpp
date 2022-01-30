#include <iostream>
#include <stdexcept>

#include <proc/Processor.hpp>
#include <err/request_exception.hpp>


namespace mm_server::proc {
    void Processor::initialize() {
        this->status = Status::idle;
        this->size = 0;
        this->left_matrix.clear();
        this->right_matrix.clear();
        this->result_matrix.clear();
        this->work_queue.clear();
        this->results_queue.clear();
        this->assigned_tasks.clear();
    }

    void Processor::set_matrix(
        const std::vector<std::vector<std::string>>& source_matrix,
        std::vector<std::vector<double>>& destination_matrix
    ) {
        bool error = false;

        std::vector<std::vector<double>> matrix(
            static_cast<int>(source_matrix.size()),
            std::vector<double>(static_cast<int>(source_matrix.size()), 0.0)
        );

        for (int i = 0; i < static_cast<int>(source_matrix.size()); ++i) {
            if (source_matrix[i].size() != source_matrix.size()) {
                throw err::request_exception(3);
            }

            for (int j = 0; j < static_cast<int>(source_matrix.size()); ++j) {
                try {
                    matrix[i][j] = std::stod(source_matrix[i][j]);
                }
                catch (const std::invalid_argument&) {
                    throw err::request_exception(3);
                }
            }
        }

        this->mtx.lock();
        if (this->status == Status::finished) { this->initialize(); }

        error = error || (this->status != Status::idle);
        if (this->size != 0) {
            error = error || (static_cast<int>(matrix.size()) != this->size);
            if (!error) {
                this->status = Status::running;
                for (int i = 0; i < this->size; ++i) {
                    for (int j = 0; j < this->size; ++j) {
                        this->work_queue.push_back(std::tuple<int, int>{i, j});
                    }
                }

                this->result_matrix = std::vector<std::vector<double>>(
                    this->size, std::vector<double>(this->size, 0.0)
                );
            }
        }
        if (!error) {
            this->size = static_cast<int>(matrix.size());
            destination_matrix = matrix;
        }
        this->mtx.unlock();

        if (error) { throw err::request_exception(5); }
    }

    void Processor::set_left_matrix(const std::vector<std::vector<std::string>>& left_matrix) {
        this->set_matrix(left_matrix, this->left_matrix);
    }

    void Processor::set_right_matrix(const std::vector<std::vector<std::string>>& right_matrix) {
        this->set_matrix(right_matrix, this->right_matrix);
    }

    void Processor::reset() {
        this->mtx.lock();
        this->initialize();
        this->mtx.unlock();
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

    std::vector<std::vector<std::string>> Processor::get_results_full() {
        this->mtx.lock();
        std::vector<std::vector<std::string>> result(this->size, std::vector<std::string>());
        for (int i = 0; i < this->size; ++i) {
            for (int j = 0; j < this->size; ++j) {
                result[i].push_back(std::to_string(this->result_matrix[i][j]));
            }
        }

        this->results_queue.clear();
        this->mtx.unlock();
        return result;
    }

    std::vector<std::vector<std::string>> Processor::assign_task(int descriptor) {
        int x, y;
        bool error = false;
        std::vector<std::vector<std::string>> rowcolumn(2, std::vector<std::string>());

        this->mtx.lock();
        error = error || (this->status != Status::running);
        error = error || (this->work_queue.empty());
        error = error || (this->assigned_tasks.count(descriptor) == 1);
        if (!error) {
            std::tie(x, y) = this->work_queue.back();
            this->work_queue.pop_back();

            this->assigned_tasks[descriptor] = std::make_tuple(x, y);
            for (int i = 0; i < this->size; ++i) {
                rowcolumn[0].push_back(std::to_string(this->left_matrix[x][i]));
                rowcolumn[1].push_back(std::to_string(this->right_matrix[i][y]));
            }
        }
        this->mtx.unlock();

        if (error) { throw err::request_exception(5); }

        return rowcolumn;
    }

    void Processor::revoke_task(int descriptor) {
        this->mtx.lock();
        if (this->assigned_tasks.count(descriptor) == 1) {
            if (this->status == Status::running) {
                this->work_queue.push_back(this->assigned_tasks[descriptor]);
            }

            this->assigned_tasks.erase(descriptor);
        }
        this->mtx.unlock();
    }

    void Processor::collect_result(
        int descriptor,
        const std::vector<std::vector<std::string>>& result
    ) {
        int x, y;
        double value;
        bool error = false;

        if (static_cast<int>(result.size()) != 1) { throw err::request_exception(3); }
        if (static_cast<int>(result.front().size()) != 1) { throw err::request_exception(3); }
        try {
            value = std::stod(result.front().front());
        }
        catch (const std::invalid_argument&) {
            throw err::request_exception(3);
        }

        this->mtx.lock();
        error = error || (this->status != Status::running);
        error = error || (this->assigned_tasks.count(descriptor) == 0);
        if (!error) {
            std::tie(x, y) = this->assigned_tasks[descriptor];
            this->assigned_tasks.erase(descriptor);
            this->result_matrix[x][y] = value;
            this->results_queue.push_back(std::make_tuple(x, y, value));
            if (this->work_queue.empty() && this->assigned_tasks.empty()) {
                this->status = Status::finished;
            }
        }
        this->mtx.unlock();

        if (error) { throw err::request_exception(5); }
    }
}
