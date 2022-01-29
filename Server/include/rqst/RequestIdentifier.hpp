#pragma once

#include <string>
#include <map>

#include <rqst/Key.hpp>
#include <rqst/Value.hpp>


namespace mm_server::rqst {
    class RequestIdentifier {
        public:
            Key key_at(const std::string& name) const;
            Value value_at(const std::string& name) const;

        private:
            const std::map<std::string, Key> keys = {
                {"GET", Key::get},
                {"PUT", Key::put},
                {"POST", Key::post}
            };
            const std::map<std::string, Value> values = {
                {"STATUS", Value::status},
                {"TASK", Value::task},
                {"RESULT", Value::result},
                {"UPDATE-NEW", Value::update_new},
                {"UPDATE-FULL", Value::update_full},
                {"REGISTER-CLIENT", Value::register_client},
                {"REGISTER-UNIT", Value::register_unit},
                {"PROCESS-RESET", Value::process_reset},
                {"LEFT-MATRIX", Value::left_matrix},
                {"RIGHT-MATRIX", Value::right_matrix}
            };
    };
}
