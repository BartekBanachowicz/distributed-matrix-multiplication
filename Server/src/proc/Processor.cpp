#include <proc/Processor.hpp>
#include <err/connection_exception.hpp>


namespace mm_server::proc {
    void Processor::set_left_matrix(std::vector<std::vector<double>> left_matrix) {
        bool sizes_match = true;

        for (const std::vector<double>& item : left_matrix) {
            if (item.size() != left_matrix.size()) { throw err::connection_exception(10); }
        }

        this->mtx.lock();
        if (!this->right_matrix.empty()) { sizes_match = left_matrix.size() == this->right_matrix.size(); }
        if (sizes_match) { this->left_matrix = left_matrix; }
        this->mtx.unlock();

        if (!sizes_match) { throw err::connection_exception(11); }
    }

    void Processor::set_right_matrix(std::vector<std::vector<double>> right_matrix) {
        bool sizes_match = true;

        for (const std::vector<double>& item : right_matrix) {
            if (item.size() != right_matrix.size()) { throw err::connection_exception(10); }
        }

        this->mtx.lock();
        if (!this->left_matrix.empty()) { sizes_match = right_matrix.size() == this->left_matrix.size(); }
        if (sizes_match) { this->right_matrix = right_matrix; }
        this->mtx.unlock();

        if (!sizes_match) { throw err::connection_exception(11); }
    }

    void Processor::run() {
        bool missing_data;

        this->mtx.lock();
        if (!(missing_data = this->left_matrix.empty() && this->right_matrix.empty())) {
            this->status = Status::running;
            for (int i = 0; i < this->left_matrix.size(); ++i) {
                for (int j = 0; j < this->right_matrix.size(); ++j) {
                    this->work_queue.emplace(i, j);
                }
            }
        }
        this->mtx.unlock();

        if (!missing_data) { throw err::connection_exception(12); }
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

    std::vector<std::string> Processor::get_update() {
        std::vector<std::string> result;
        int x, y, value;

        this->mtx.lock();
        while (!this->results_queue.empty()) {
            std::tie(x, y, value) = this->results_queue.top();
            result.push_back(std::to_string(x));
            result.push_back(std::to_string(y));
            result.push_back(std::to_string(value));
            this->results_queue.pop();
        }

        this->mtx.unlock();
        return result;
    }
}
