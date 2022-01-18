#pragma once

namespace mm_server::rqst {
    enum class Value {
        status,
        update_new,
        update_full,
        register_client,
        register_unit,
        process_start,
        process_reset,
        left_matrix,
        right_matrix
    };
}
