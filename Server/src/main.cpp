#include <iostream>

#include <utils/ServerRunner.hpp>
#include <utils/server_exception.hpp>


using namespace mm_server;


int main(int argc, char *argv[]) {
    try {
        utils::ServerRunner server_runner(argc, argv);
        server_runner.run();
    }
    catch (utils::server_exception &err) {
        std::cerr << err.what() << std::endl;
        return 1;
    }
}
