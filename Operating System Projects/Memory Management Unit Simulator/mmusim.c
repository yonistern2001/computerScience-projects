#include <stddef.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>

#define SIZEOFDISC 100

typedef unsigned char UCHAR;
typedef unsigned long ULONG;

struct Entry
{
    void* addr;
    void* addr_on_disc;
    char modified;
};

struct Entry* page_table;
void** disc;
int* allocation_order;
int pagesize, pages_available, pmpc, front= 0, end= 0, disc_index= 0;
ULONG max_addrs;

void *Malloc(size_t size) 
{
    void *p;
    if ((p  = malloc(size)) == NULL)
    {
	    printf("Malloc error: error number= %d\n", errno);
        exit(1);
    }
    return p;
}

void *Calloc(size_t num_of_elements, size_t size_of_element) 
{
    void *p;
    if ((p  = calloc(num_of_elements, size_of_element)) == NULL)
    {
	    printf("Calloc error: error number= %d\n", errno);
        exit(1);
    }
    return p;
}

void Posix_memalign(void** memptr, size_t alignment, size_t size) 
{
    int error_num;
    if ((error_num= posix_memalign(memptr, alignment, size)) != 0)
    {
	    printf("posix_memalign error: error number= %d\n", error_num);
        exit(1);
    }
}

void create_disc()
{
    disc= Malloc(sizeof(void*) * SIZEOFDISC);
    for(int i= 0; i < SIZEOFDISC; i++)
    {
        Posix_memalign(disc+i, pagesize, pagesize);
    }
}

// returns true if num eqauls 2 to a power else it returns false
int is_power_of_2(int num)
{
    if(num <= 1)
    {
        return 0;
    }
    // in binary 1 followed by only zeros must be 2 to a certain power
    // 2 equals 10 in binary and shifting it left multiplies it by 2
    while(num != 0)
    {
        if(num & 1)
        {
            if(1 == num)
            {
                return 1;
            }
            return 0;
        }
        num= num >> 1;
    }
    return 0;
}

// convert string representation of hex into a long
ULONG hex_to_long(char* str)
{
    ULONG num= strtol(str, NULL, 16);
    return num;
}

void* evict()
{
    int page= allocation_order[front];
    struct Entry* entry= &page_table[page];
    void* old_addr= entry->addr;
    printf("physical page at 0x%.16lX, which corresponds to virtual page 0x%.16lX, is evicted and ", (ULONG) old_addr, page * (ULONG) pagesize);
    if(entry->modified)
    {
        printf("dirty. Copied to disc at 0x%.16lX and removed from physical memory\n", (ULONG) entry->addr_on_disc);
        memcpy(entry->addr_on_disc, entry->addr, pagesize);
        entry->modified= 0;
    }
    else
    {
        printf("not dirty. Removed from physical memory\n");
    }
    front= (front+1) % pmpc;
    entry->addr= NULL;
    return old_addr;
}

// create a new page in memory
void create_page(struct Entry* entry, ULONG page)
{
    char has_disc_addr= entry->addr_on_disc != NULL;
    if(!has_disc_addr)
    {
        if(disc_index == SIZEOFDISC)
        {
            printf("error: disk storage full\n");
            exit(1);
        }
        entry->addr_on_disc= disc[disc_index++];
        entry->modified= 1;
    }
    if(pages_available != 0)
    {
        Posix_memalign(&(entry->addr), pagesize, pagesize);
        pages_available--;
    }
    else
    {
        entry->addr= evict();
        if(has_disc_addr)
        {
            memcpy(entry->addr, entry->addr_on_disc, pagesize);
        }
    }
    if(!has_disc_addr)
    {
        memset(entry->addr, 0, pagesize);
    }
    allocation_order[end]= (int) page;
    end= (end+1) % pmpc;
    printf("physical page at 0x%.16lX mapped to virtual page at 0x%.16lX\n", (ULONG) entry->addr, page*pagesize);
}

// converts the virtual address into a physical address
// creates a new page if its address to page in memmory is null
void* get_physical_address(void* virtual_address)
{
    ULONG vir= (ULONG) virtual_address;
    ULONG page= vir / pagesize;
    ULONG offset=  vir % pagesize;

    struct Entry* entry= page_table + page;
    if(entry->addr == NULL)
    {
        create_page(entry, page);
    }
    return entry->addr + offset;
}

void readbyte(void* location)
{
    if(max_addrs < (ULONG) location)
    {
        printf("readbyte: segmentation fault\n");
        return;
    }

    UCHAR* address= (UCHAR*) get_physical_address(location);
    UCHAR value= *address;
    printf("readbyte: VM location 0x%.16lX, which is PM location 0x%.16lX, contains value 0x%.2X\n", (ULONG) location, (ULONG) address, value);
}

void writebyte(void* location, UCHAR value)
{
    if(max_addrs < (ULONG) location)
    {
        printf("writebyte: segmentation fault\n");
        return;
    }

    UCHAR* address= (UCHAR*) get_physical_address(location);
    page_table[((ULONG) location)/pagesize].modified= 1;
    *address= value;
    printf("writebyte: VM location 0x%.16lX, which is PM location 0x%.16lX, now contains value 0x%.2X\n", (ULONG) location, (ULONG) address, value);
}

void exitP()
{
    for(int i= 0; i < disc_index; i++)
    {
        free(disc[i]);
    }
    free(disc);
    for(int i= front; i != end; i= (i+1)%pmpc)
    {
        free(page_table[allocation_order[i]].addr);
    }
    free(page_table);
    free(allocation_order);
    exit(0);
}

// split string by spaces and store in array
// returns number of tokens or -1 if its greater than the size of the array
int split_str(char* curr, char** strs, int array_size)
{
    int index= 0;
    char* prev= curr;
    char c;
    while((c= *curr) != '\0' && c != '\n')
    {
        if(c == ' ')
        {
            strs[index++]= prev;
            prev= curr+1;
            *curr= '\0';
            
            if(index == array_size)
            {
                return -1;
            }
        }
        curr++;
    }
    strs[index++]= prev;
    *curr= '\0';
    return index;
}

// run the function the user requested
void run(char** arguments, int size)
{
    char* command;
    ULONG location;
    UCHAR value;

    if(size >= 1)
    {
        command= arguments[0];
        if(strcmp(command, "exit") == 0 && size == 1)
        {
            exitP();
        }
    }
    if(size >= 2)
    {
        location= hex_to_long(arguments[1]);
        if(strcmp(command, "readbyte") == 0 && size == 2)
        {
            readbyte((void*) location);
            return;
        }
    }
    if(strcmp(arguments[0], "writebyte") == 0 && size == 3)
    {
        value= (UCHAR) hex_to_long(arguments[2]);
        writebyte((void*) location, value);
        return;
    }
    printf("invalid input\n");
}

int main(int argc, char** argv)
{
    if(argc != 4)
    {
        printf("incorrect number of arguments\n");
        return 1;
    }
    pagesize= atoi(argv[1]);
    int vmpc= atoi(argv[2]);
    pmpc= atoi(argv[3]);
    if(!is_power_of_2(pagesize))
    {
        printf("error: pagesize must be power of 2\n");
        return 1;
    }
    if(vmpc <= 0 || pmpc <= 0)
    {
        printf("error: vmpc or pmpc is less than 1\n");
        return 1;
    }
    pages_available= pmpc;
    create_disc();
    max_addrs= (vmpc * (ULONG) pagesize)-1;
    page_table= Calloc(vmpc, sizeof(struct Entry));
    allocation_order= Malloc(pmpc*sizeof(int));
    char input[100];
    char* strs[3];
    int num_of_strs;

    while(1)
    {
        printf(">");
        fgets(input, 100, stdin);
        num_of_strs= split_str(input, strs, 3);

        run(strs, num_of_strs);
    }
}