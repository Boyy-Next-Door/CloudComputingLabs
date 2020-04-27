#include<iostream>
#include <stdlib.h>
using namespace std;



int main(int argc, char* argv[]){
    string path="/home/jts/Desktop/Lab2/run.sh";
    string s="sh "+path+" ";
   
    for(int i=1; argv[i]!=NULL ; i++){
        s += (string)argv[i]+" ";
    }
    
    const char * mystr=s.c_str(); 
    system(mystr);
    return 0;
}