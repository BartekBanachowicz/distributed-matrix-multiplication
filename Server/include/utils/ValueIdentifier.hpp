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
                {"UNIT", Value::unit},
                {"CLIENT", Value::client}
            };
    };
}
