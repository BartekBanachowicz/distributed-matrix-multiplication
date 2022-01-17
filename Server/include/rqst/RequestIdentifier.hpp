#pragma once

#include <string>
#include <map>

#include <rqst/Key.hpp>
#include <rqst/Value.hpp>


namespace mm_server::rqst {
    class RequestIdentifier {
        public:
            Key key_at(const std::string& name) const;
            Value value_at(const Key& key, const std::string& name) const;

        private:
            const std::map<std::string, Key> keys = {
                {"GET", Key::get},
                {"PUT", Key::put},
                {"POST", Key::post}
            };
            const std::map<std::string, Value> get_values = {
                {"STATUS", Value::status},
                {"UPDATE-NEW", Value::update_new},
                {"UPDATE-FULL", Value::update_full}
            };
            const std::map<std::string, Value> put_values = {
                {"REGISTER-CLIENT", Value::register_client},
                {"REGISTER-UNIT", Value::register_unit},
                {"PROCESS-START", Value::process_start},
                {"PROCESS-STOP", Value::process_stop},
                {"PROCESS-RESET", Value::process_reset}
            };
            const std::map<std::string, Value> post_values = {
                {"LEFT-MATRIX", Value::left_matrix},
                {"RIGHT-MATRIX", Value::right_matrix}
            };
    };
}
