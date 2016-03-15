#include <iostream>
#include <cstdlib>
#include <cstring>
#include "player.h"
using namespace std;

int main(int argc, char *argv[]) {    
// Invoke the Java program with the passed arguments.

    string cmd = "java -jar VVV.jar";    
    argv++;
    while (--argc) {
        cmd += " ";
        cmd += *(argv++);
    }
    system(cmd.c_str());
    return 0;
}
