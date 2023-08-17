//* Generic */
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include <pthread.h>
#include <semaphore.h>

/* Network */
#include <netdb.h>
#include <sys/socket.h>

#define BUF_SIZE 100

pthread_barrier_t barrier;
volatile unsigned current_file= 0;
sem_t* semaphores;
int num_of_threads;
char* filename1;
char* filename2;
char* host;
char* portnum;

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

void Pthread_create(pthread_t *tidp, pthread_attr_t *attrp, void * (*routine)(void *), void *argp) 
{
    if (pthread_create(tidp, attrp, routine, argp) != 0)
    {
	    printf("pthread_create error: error number= %d\n", errno);
      exit(1);
    }
}

int Pthread_barrier_wait(pthread_barrier_t* barrier)
{
  int status= pthread_barrier_wait(barrier);
  if(status != PTHREAD_BARRIER_SERIAL_THREAD && status != 0)
  {
    	printf("pthread_barrier_wait error: error number= %d\n", errno);
      exit(1);
  }
  return status;
}

void Sem_init(sem_t* sem, int pshared, unsigned int value)
{
  if(sem_init(sem, pshared, value) != 0)
  {
    printf("sem_init error: error number= %d\n", errno);
    exit(1);
  }
}

void Sem_wait(sem_t* sem)
{
  if(sem_wait(sem) != 0)
  {
    printf("sem_wait error: error number= %d\n", errno);
    exit(1);
  }
}

void Sem_post(sem_t* sem)
{
  if(sem_post(sem) != 0)
  {
    printf("sem_post error: error number= %d\n", errno);
    exit(1);
  }
}

void Pthread_barrier_init(pthread_barrier_t* barrier, void* attr, unsigned num_of_threads)
{
  if(pthread_barrier_init(barrier, attr, num_of_threads) != 0)
  {
    printf("pthread_barrier_init error: error number= %d\n", errno);
    exit(1);
  }
}

// Get host information (used to establishConnection)
struct addrinfo *getHostInfo(char* host, char* port)
{
  int r;
  struct addrinfo hints, *getaddrinfo_res;
  // Setup hints
  memset(&hints, 0, sizeof(hints));
  hints.ai_family = AF_INET;
  hints.ai_socktype = SOCK_STREAM;
  if ((r = getaddrinfo(host, port, &hints, &getaddrinfo_res)))
  {
    fprintf(stderr, "[getHostInfo:21:getaddrinfo] %s\n", gai_strerror(r));
    return NULL;
  }

  return getaddrinfo_res;
}

// Establish connection with host
int establishConnection(struct addrinfo *info)
{
  if (info == NULL) return -1;

  int clientfd;
  for (;info != NULL; info = info->ai_next)
  {
    if ((clientfd = socket(info->ai_family, info->ai_socktype, info->ai_protocol)) < 0)
    {
      perror("[establishConnection:35:socket]");
      continue;
    }

    if (connect(clientfd, info->ai_addr, info->ai_addrlen) < 0)
    {
      close(clientfd);
      perror("[establishConnection:42:connect]");
      continue;
    }

    freeaddrinfo(info);
    return clientfd;
  }

  freeaddrinfo(info);
  return -1;
}

// Send GET request
void GET(int clientfd, char *host, char *port, char *path)
{
  char req[1000] = {0};
  sprintf(req, "GET %s HTTP/1.1\r\nConnection: close\r\nHost: %s:%s\r\n\r\n", path,host,port);
  send(clientfd, req, strlen(req), 0);
}

// send request to server and return file descriptor
int request(char* filename)
{
  // Establish connection with <hostname>:<port>
  int clientfd = establishConnection(getHostInfo(host, portnum));
  if (clientfd == -1)
  {
    fprintf(stderr, "[main:179] Failed to connect to: %s:%s%s \n", host, portnum, filename);
    exit(3);
  }

  // Send GET request > stdout
  GET(clientfd, host, portnum, filename);
  return clientfd;
}

// receive information and print to stdout
void receive(int clientfd, char buf[])
{
  while (recv(clientfd, buf, BUF_SIZE, 0) > 0)
  {
    fputs(buf, stdout);
    memset(buf, 0, BUF_SIZE);
  }
  //close file descriptor
  close(clientfd);
}

// return current file
char* get_file(unsigned file_num)
{
  if(file_num == 0)
  {
    return filename1;
  }
  return filename2;
}

// CONCUR thread
void *concur_thread(void *ptr)
{
  unsigned file_num= 0;
  char* filename;
  char buf[BUF_SIZE];
  while(1)
  {
    filename= get_file(file_num);
    int fd= request(filename);
    receive(fd, buf);
    // switch file
    file_num= !file_num;
    Pthread_barrier_wait(&barrier);
  }
}

// FIFO thread
void *fifo_thread(void *ptr)
{
  int thread_num= *((int*) ptr);
  char* filename;
  char buf[BUF_SIZE];

  while (1)
  {
    if(thread_num != 0)
    {
      // put current thread to sleep
      Sem_wait(&semaphores[thread_num]);
    }

    filename= get_file(current_file);
    // switch file
    current_file= !current_file;
    
    int fd= request(filename);

    // if its not the last thread signal the next thread to run
    if(thread_num != num_of_threads-1)
    {
      Sem_post(&semaphores[thread_num+1]);
    }

    receive(fd, buf);
    
    // wait until all threads reach here before running thread 0
    Pthread_barrier_wait(&barrier);
  }
}

int main(int argc, char **argv)
{
  if (argc != 6 && argc != 7)
  {
    fprintf(stderr, "incorrect number of arguments\n");
    return 1;
  }

  host= argv[1];
  portnum= argv[2];
  num_of_threads= atoi(argv[3]);
  char* schedalg= argv[4];
  filename1= argv[5];
  if(argc == 7)
  {
    filename2= argv[6];
  }
  else
  {
    filename2= filename1;
  }

  if(num_of_threads <= 0)
  {
    printf("invalid number of threads\n");
    return 1;
  }
  
  Pthread_barrier_init(&barrier, NULL, (unsigned) num_of_threads);

  pthread_t threads[num_of_threads];
  if(!strcmp(schedalg, "FIFO"))
  {
    // allocate memmory on the heap to store the thread numbers
    int* curr=(int*) Malloc(sizeof(int)*num_of_threads);
    semaphores= (sem_t*) Malloc(sizeof(sem_t)*num_of_threads);

    for(int i= 0; i < num_of_threads; i++)
    {
      Sem_init(&semaphores[i], 0, 0);    
      *curr= i;
      Pthread_create(&threads[i], NULL, fifo_thread, (void*) curr++);
    }
  }
  else if(!strcmp(schedalg, "CONCUR"))
  {
    for(int i= 0; i < num_of_threads; i++)
    {
      Pthread_create(&threads[i], NULL, concur_thread, NULL);
    }
  }
  else
  {
    printf("invalid schedalg: %s\n", schedalg);
    return 1;
  }

  // wait for thread to finish (which will never happen)
  pthread_join(threads[0], NULL);
}