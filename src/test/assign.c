
int global_x = 0;
int common_x;

struct s {
    int* ptr;
};

int
main(int argc, char **argv)
{
    int x, y;
    static int static_x = 0;
    static int scomm_x;
    int* ptrs[2];
    struct s s;
    int* ptr;
    int integers[8];
    char buf[8];
    char *p = buf;


    
    x = 1;
    printf("%d", x);

    x = 77;
    y = 77;
    x = y = 2;
    printf(";%d;%d", x, y);

    
    argc = 3;
    printf(";%d", argc);

    
    global_x = 4;
    common_x = 5;
    printf(";%d;%d", global_x, common_x);

    
    static_x = 6;
    scomm_x = 7;
    printf(";%d;%d", static_x, scomm_x);

    
    ptrs[0] = &x;
    *ptrs[0] = 8;
    printf(";%d;%d", *ptrs[0], x);

    
    s.ptr = &x;
    x = 9;
    printf(";%d", *s.ptr);

    
    ptr = &x;
    *ptr++ = 10;
    ptr--;
    printf(";%d", *ptr);

    
    integers[0] = integers[1] = integers[2] = integers[3] = 777;
    ptr = integers;
    *ptr++ = 11;
    printf(";%d;%d", integers[0], integers[1]);

    
    *(p + 1) = 'S';
    printf(";%c", p[1]);

    
    ptrs[0] = 0;
    ptrs[1] = &x;
    **(ptrs + 1) = 12;
    printf(";%d", *ptrs[1]);


    return 0;
}

static int
printf(char *s, ...)
{
    return 1;
}