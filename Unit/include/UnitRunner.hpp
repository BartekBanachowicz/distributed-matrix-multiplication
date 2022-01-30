#pragma once


namespace mm_unit {
    class UnitRunner {
        public:
            UnitRunner(int argc, char *argv[]);
            ~UnitRunner();
            void run();

        private:
            int socket_descriptor;
    };
}
