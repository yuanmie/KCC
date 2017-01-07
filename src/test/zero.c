extern int printf(char * format, ...);
struct ss{
    int a;
    int b;
};

union u{
    int a;
    float b;
};

struct s{
    struct ss sss;
    int a[2];
    int b;
    union u uu;
};



int
main(int argc, char **argv)
{

    return 0;
}
