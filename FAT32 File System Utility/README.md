FAT32 Reader

Developed a program to mount FAT32 images and explore them through the command line. To increase efficiency, I only kept the FAT table and the current directory in memory. I stored the current directory in a HashMap to allow for fast lookup times for files and directories.

Command line functions
* stop- terminate program
* info- print info about the image
* ls- lists all files and directories in the current directory
* stat- print info about a specific file or directory (size, attributes and cluster number)
* size- print size of file
* cd- change current directory
* read- print data from file using the offset and the number of bytes its told to read
