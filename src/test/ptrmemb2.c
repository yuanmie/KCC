

struct st {
    int x[4];
};

int
main(void)
{
    struct st s;
    struct st* p = &s;
    p->x[1] = 7;
    printf("%d\n", p->x[1]);
    return 0;
}

static int
printf(char *s, ...)
{
    return 1;
}