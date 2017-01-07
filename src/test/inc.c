

int
main(int argc, char **argv)
{
    int i = 0;
    int j;

    ++i;
    j = ++i;


    i++;

    j = i++;






    return 0;
}

static int
inc(int i)
{
    return ++i;
}
