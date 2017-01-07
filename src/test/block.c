
int
main(int argc, char **argv)
{
    int *addr1;
    int *addr2;

    int i = 1;

    {
        int i = 2;

        addr1 = &i;
    }
    {
        int i = 3;

        addr2 = &i;
    }

    return 0;
}
