#include <iostream>

#include <UnitRunner.hpp>
#include <err/unit_exception.hpp>


int main(int argc, char *argv[]) {
    try {
        mm_unit::UnitRunner unit_runner(argc, argv);
        unit_runner.run();
    }
    catch (const mm_unit::err::unit_exception &err) {
        std::cerr << err.what() << std::endl;
        return 1;
    }
}
