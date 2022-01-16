#pragma once

#include <string>
#include <map>

#include <utils/Value.hpp>


namespace mm_server::utils {
    class ValueIdentifier {
        public:
            Value at(const std::string& name, int& result) const;

        private:
            const std::map<std::string, Value> values = {
                {"REGISTER", Value::enter},
                {"LEFT-MATRIX", Value::left_matrix},
                {"RIGHT-MATRIX", Value::right_matrix},
                {"START", Value::start},
                {"STATUS", Value::status},
                {"UNIT", Value::unit},
                {"CLIENT", Value::client}
            };
    };
}
