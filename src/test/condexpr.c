

int
main(int argc, char **argv)
{
    char* a = 1 ? "OK" : "NG";
    char *b = 0 ? "NG" : "OK";
    char *c = (0==0 ? "OK" : "NG");
    char *d = ( 0==1 ? "NG" : "OK");
    char *e = (0==1 ? "NG" : "OK");

    return 0;
}
