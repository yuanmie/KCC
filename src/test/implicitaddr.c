

int
main(int argc, char** argv)
{
    void *p = &printf;
    
    check(printf, p);
    check(*printf, p);
    check(**printf, p);
    check(***printf, p);
    check(****printf, p);

    return 0;
}

static void
check(void* f, void* p)
{
    if (f == p) {

    }
    else {

    }
}

static int
printf(char *s, ...)
{
    return 1;
}
