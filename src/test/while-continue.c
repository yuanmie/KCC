

int
main(int argc, char **argv)
{
    int i;

    i = 3;
    while (i) {
        i--;
        continue;
        puts("NG");
    }
    puts("OK");

    return 0;
}

int puts(char * i){
    return i;
}