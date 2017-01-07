

int
main(int argc, char** argv)
{
    int (char*, ...)* f;

    f = myputs;
    f(";OK");

    f = &myputs;
    f(";OK");

    return 0;
}

static int
myputs(char *s, ...)
{
    return 1;
}
