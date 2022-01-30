#include <stdexcept>

#include <rqst/RequestIdentifier.hpp>
#include <err/request_exception.hpp>


namespace mm_server::rqst {
    Key RequestIdentifier::key_at(const std::string& name) const {
        try {
            return this->keys.at(name);
        }
        catch (const std::out_of_range&) {
            throw err::request_exception(2);
        }
    }

    Value RequestIdentifier::value_at(const std::string& name) const {
        try {
            return this->values.at(name);
        }
        catch (const std::out_of_range&) {
            throw err::request_exception(2);
        }
    }
}
