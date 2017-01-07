static int g = 3;
static int c;

int
main(int argc, char **argv)
{
    int i;
    int j = 0;

    i = 2;
    j = 1;
    c = 5;
    i = 9;
    j = 1;
    return 0;
}

int
f(int i)
{
    return i + 1;
}
