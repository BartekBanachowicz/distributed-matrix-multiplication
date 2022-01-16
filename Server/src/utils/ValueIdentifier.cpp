#include <stdexcept>

#include <utils/ValueIdentifier.hpp>


namespace mm_server::utils {
    Value ValueIdentifier::at(const std::string& name, int& result) const {
        try {
            result = std::stoi(name);
        }
        catch (const std::invalid_argument&) {
            return this->values.at(name);
        }

        return Value::numeric;
    }
}
