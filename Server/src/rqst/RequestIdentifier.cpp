#include <stdexcept>

#include <rqst/RequestIdentifier.hpp>
#include <err/request_exception.hpp>


namespace mm_server::utils {
    Key RequestIdentifier::key_at(const std::string& name) const {
        try {
            return this->keys.at(name);
        }
        catch (const std::out_of_range&) {
            throw err::request_exception(2);
        }
    }

    Value RequestIdentifier::value_at(const Key& key, const std::string& name) const {
        try {
            switch (key) {
                case Key::get:
                    return this->get_values.at(name);
                case Key::put:
                    return this->put_values.at(name);
                default:
                    return this->post_values.at(name);
            }
        }
        catch (const std::out_of_range&) {
            throw err::request_exception(2);
        }
    }
}
