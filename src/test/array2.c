int
main(int argc, char **argv)
{
    int a[1];
    a[0] = 0;
    f(a);
    return 0;
}

void
f(int x[])
{
    int save = *x;
    int i = 0;
    x = &i;
}
