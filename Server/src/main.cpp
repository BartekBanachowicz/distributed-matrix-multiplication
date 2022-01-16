#include <iostream>

#include <ServerRunner.hpp>
#include <err/server_exception.hpp>


int main(int argc, char *argv[]) {
    try {
        mm_server::ServerRunner server_runner(argc, argv);
        server_runner.run();
    }
    catch (const mm_server::err::server_exception &err) {
        std::cerr << err.what() << std::endl;
        return 1;
    }
}
