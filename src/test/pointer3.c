

int
main(int argc, char **argv)
{
    int ary[4];
    int* ptr = ary;

    *ptr = 1;
    ptr[1] = 777;
    ptr[2] = 3;
    ptr[3] = 4;
    printf("%d;%d;%d;%d", *ptr, ptr[1], ptr[2], ptr[3]);
    printf(";%d;%d;%d;%d", ary[0], ary[1], ary[2], ary[3]);

    return 0;
}

static int
printf(char *s, ...)
{
    return 1;
}
