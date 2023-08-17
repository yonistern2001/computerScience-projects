import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class fat32_reader
{
    public static void main(String[] args)
    {
        String imageRoot= args[0];
        fat32_reader reader= new fat32_reader(imageRoot);
        Scanner scanner= new Scanner(System.in);
        boolean run= true;
        while(run)
        {
            System.out.print(reader.getCurrentWorkingDir()+"] ");
            String command= scanner.nextLine();
            String[] function= command.split(" ");
            run= reader.execute(function);
        }
        scanner.close();
    }

    private final List<String> workingDir;
    private final byte[] FAT;
    private RandomAccessFile disk;
    private int start, bytesPerSec, secPerClus, rsvdSecCnt, numFATs, FATSz32, rootClus;
    private Map<String, Entry> currentDir;
    
    private fat32_reader(String imageRoot)
    {
        try
        {
            this.disk= new RandomAccessFile(imageRoot, "r");
        }
        catch(FileNotFoundException i)
        {
            System.out.println("Error: file not found");
            System.exit(1);
        }

        this.workingDir= new ArrayList<>();
        this.currentDir= new HashMap<>();
        readBootSector();

        diskSeek(rsvdSecCnt * bytesPerSec);
        this.FAT= new byte[FATSz32 * bytesPerSec];
        diskRead(FAT);

        openDir(rootClus);
    }

    private void readBootSector()
    {
        byte[] bytes= new byte[48];
        diskRead(bytes);
        this.rsvdSecCnt= getNum(14, 2, bytes);
        this.bytesPerSec= getNum(11, 2, bytes);
        this.numFATs= getNum(16, 1, bytes);
        this.FATSz32= getNum(36, 4, bytes);
        this.start= (rsvdSecCnt+numFATs*FATSz32)*bytesPerSec;
        this.secPerClus= getNum(13, 1, bytes);
        this.rootClus= getNum(44, 4, bytes);
    }

    private boolean execute(String[] function)
    {
        String funct= function[0];
        int functLen= function.length;
        if(funct.equals("stop") && functLen == 1)
        {
            closeDisk();
            return false;
        }
        else if(funct.equals("info") && functLen == 1)
        {
            info();
        }
        else if(funct.equals("ls") && functLen == 1)
        {
            ls();
        }
        else if(funct.equals("stat") && functLen == 2)
        {
            stat(function[1].toUpperCase());
        }
        else if(funct.equals("size") && functLen == 2)
        {
            size(function[1].toUpperCase());
        }
        else if(funct.equals("cd") && functLen == 2)
        {
            cd(function[1].toUpperCase());
        }
        else if(funct.equals("read") && functLen == 4)
        {
            read(function[1].toUpperCase(), function[2], function[3]);
        }
        else
        {
            System.out.println("Error: invalid command");
        }
        return true;
    }

    private void info()
    {
        System.out.println("BPB_BytesPerSec is 0x"+Integer.toHexString(bytesPerSec)+", "+bytesPerSec);
        System.out.println("BPB_SecPerClus is 0x"+Integer.toHexString(secPerClus)+", "+secPerClus);
        System.out.println("BPB_RsvdSecCnt is 0x"+Integer.toHexString(rsvdSecCnt)+", "+rsvdSecCnt);
        System.out.println("BPB_NumFATs is 0x"+Integer.toHexString(numFATs)+", "+numFATs);
        System.out.println("BPB_FATSz32 is 0x"+Integer.toHexString(FATSz32)+", "+FATSz32);
    }

    private void ls()
    {
        Set<String> items= this.currentDir.keySet();
        List<String> filesAndDir= new ArrayList<>(items);
        if(!filesAndDir.contains("."))
        {
            filesAndDir.add(".");
        }
        if(!filesAndDir.contains(".."))
        {
            filesAndDir.add("..");
        }
        filesAndDir.sort(null);
        for(int i= 0; i < filesAndDir.size()-1; i++)
        {
            String curr= filesAndDir.get(i);
            System.out.print(curr+" ");
        }
        System.out.println(filesAndDir.get(filesAndDir.size()-1));
    }

    private void stat(String file_dirName)
    {
        Entry entry= this.currentDir.get(file_dirName);
        if(this.workingDir.isEmpty() && (file_dirName.equals(".") || file_dirName.equals("..")))
        {
            entry= new Entry();
            entry.attr= 16;
        }
        if(entry == null)
        {
            System.out.println("Error: file/directory does not exist");
            return;
        }
        System.out.println("Size is "+entry.size);
        System.out.print("Attributes");
        for(String currAttr: getAttributes(entry))
        {
            System.out.print(" "+currAttr);
        }
        System.out.printf("\nNext cluster number is 0x%04x\n", entry.firstCluster);
    }

    private List<String> getAttributes(Entry entry)
    {
        List<String> attributes= new ArrayList<>();
        if(entry.isArchive())
        {
            attributes.add("ATTR_ARCHIVE");
        }
        if(entry.isDirectory())
        {
            attributes.add("ATTR_DIRECTORY");
        }
        if(entry.isSystem())
        {
            attributes.add("ATTR_SYSTEM");
        }
        if(entry.isHidden())
        {
            attributes.add("ATTR_HIDDEN");
        }
        if(entry.isReadOnly())
        {
            attributes.add("ATTR_READ_ONLY");
        }
        if(attributes.isEmpty())
        {
            attributes.add("NONE");
        }
        return attributes;
    }

    private void size(String filename)
    {
        Entry fileEntry= currentDir.get(filename);
        if(fileEntry == null || fileEntry.isDirectory())
        {
            System.out.println("Error: "+filename+" is not a file");
            return;
        }
        System.out.println("Size of "+filename+" is "+fileEntry.size+" bytes");
    }

    private void cd(String dirName)
    {
        if(dirName.equals(".") || (dirName.equals("..") && this.workingDir.isEmpty()))
        {
            return;
        }
        Entry dirEntry= currentDir.get(dirName);
        if(dirEntry == null || !dirEntry.isDirectory())
        {
            System.out.println("Error: "+dirName+" is not a directory");
            return;
        }
        if(dirName.equals(".."))
        {
            this.workingDir.remove(workingDir.size()-1);
        }
        else
        {
            this.workingDir.add(dirName);
        }
        openDir(dirEntry.firstCluster);
    }

    private void read(String file, String index, String bytes)
    {
        int numOfBytes;
        int offset;
        try
        {
            numOfBytes= Integer.parseInt(bytes);
            offset= Integer.parseInt(index);
        }
        catch(NumberFormatException i)
        {
            System.out.println("Error: invalid parameter");
            return;
        }

        if(offset < 0)
        {
            System.out.println("Error: OFFSET must be a positive value");
            return;
        }
        if(numOfBytes < 0)
        {
            System.out.println("Error: NUM_BYTES must be greater than zero");
            return;
        }
        Entry fileEntry= currentDir.get(file);
        if(fileEntry == null || fileEntry.isDirectory())
        {
            System.out.println("Error: "+file+" is not a file");
            return;
        }
        if(offset+numOfBytes > fileEntry.size)
        {
            System.out.println("Error: attempt to read data outside of file bounds");
            return;
        }
        byte[] data= read(fileEntry.firstCluster);
        for(int i= offset; i < offset+numOfBytes; i++)
        {
            int curr= data[i];
            if(curr <  127)
            {
                System.out.print((char) curr);
            }
            else
            {
                System.out.printf("0x%02x", curr);
            }
        }
        System.out.println();
    }

    private void openDir(int startClus)
    {
        if(startClus == 0)
        {
            startClus= this.rootClus;
        }

        this.currentDir.clear();
        byte[] data= read(startClus);

        int curCluster= 0;
        while(curCluster < data.length)
        {
            byte attr= data[curCluster+11];
            byte firstByte= data[curCluster];
            if(firstByte == 0)
            {
                return;
            }
            if(firstByte != -27 && ((attr & 0xF) != 0xF) && (attr & 8) != 8)
            {
                String name= getStr(curCluster, 8, data);
                String extension= getStr(curCluster+8, 3, data);
                if(!extension.isEmpty())
                {
                    name= name+"."+extension;
                }
                this.currentDir.put(name, createEntry(curCluster, data, attr));
            }
            curCluster+= 32;
        }
    }

    private Entry createEntry(int curCluster, byte[] data, byte attr)
    {
        Entry entry= new Entry();
        entry.attr= attr;
        entry.size= getNum(curCluster+28, 4, data);
        int firstCluster= getNum(curCluster+20, 2, data);
        firstCluster= firstCluster << 16;
        entry.firstCluster= firstCluster | getNum(curCluster+26, 2, data);
        return entry;
    }

    private byte[] read(int startClus)
    {
        int clusSize= secPerClus * bytesPerSec;
        List<Integer> clusters= getClusters(startClus);
        byte[] data= new byte[clusters.size() * clusSize];
        int index= 0;
        for(int clusNum: clusters)
        {
            byte[] cluster= new byte[clusSize];
            diskSeek(start+(clusNum-rootClus)*clusSize);
            diskRead(cluster);
            for(byte b: cluster)
            {
                data[index++]= b;
            }
        }
        return data;
    }
    
    private List<Integer> getClusters(int clusNum)
    {
        List<Integer> clusters= new ArrayList<>();

        while(!isEOC(clusNum))
        {
            clusters.add(clusNum);
            clusNum= getNum(clusNum*4, 4, FAT);
        }
        return clusters;
    }

    private static boolean isEOC(int clusterNumber)
    {
        if(clusterNumber >= 0xFFFFFF8 && clusterNumber <= 0xFFFFFFF)
        {
            return true;
        }
        return false;
    }

    private String getCurrentWorkingDir()
    {
        if(workingDir.isEmpty())
        {
            return "/";
        }
        String str= "";
        for(String curr: this.workingDir)
        {
            str+= "/"+curr;
        }
        return str;
    }
    
    private String getStr(int start, int size, byte[] bytes)
    {
        StringBuilder str= new StringBuilder();
        for(int i= start; i < start+size; i++)
        {
            byte curr= bytes[i];
            if(curr == ' ')
            {
                return str.toString();
            }
            str.append((char) curr);
        }
        return str.toString();
    }

    private int getNum(int offset, int size, byte[] bytes)
    {
        byte[] flippedBytes= flipBytes(offset, size, bytes);
        int num= 0;
        for(byte curr: flippedBytes)
        {
            num= num << 8;
            int b= curr & 0xFF;
            num= num | b;
        }
        return num;
    }

    private static byte[] flipBytes(int offset, int size, byte[] bytes)
    {
        byte[] flippedBytes= new byte[size];
        for(int i= 0, j= offset+size-1; i < size; i++, j--)
        {
            flippedBytes[i]= bytes[j];
        }
        return flippedBytes;
    }

    private static class Entry
    {
        private byte attr;
        private int size, firstCluster;

        private boolean isReadOnly()
        {
            return (attr & 1) == 1;
        }

        private boolean isHidden()
        {
            return (attr & 2) == 2;
        }

        private boolean isSystem()
        {
            return (attr & 4) == 4;
        }

        private boolean isDirectory()
        {
            return (attr & 16) == 16;
        }

        private boolean isArchive()
        {
            return (attr & 32) == 32;
        }
    }

    private void closeDisk()
    {
        try
        {
            disk.close();
        }
        catch(IOException i)
        {
            throw new UncheckedIOException(i);
        }
    }

    private void diskRead(byte[] bytes)
    {
        try
        {
            disk.read(bytes);
        }
        catch(IOException i)
        {
            throw new UncheckedIOException(i);
        }
    }

    private void diskSeek(long pos)
    {
        try
        {
            disk.seek(pos);
        }
        catch(IOException i)
        {
            throw new UncheckedIOException(i);
        }
    }
}