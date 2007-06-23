package yaffs2.port;

import java.io.RandomAccessFile; 

import static yaffs2.port.yaffsfs_C.*;
import static yaffs2.port.yaffsfs_H.*;
import static yaffs2.utils.Unix.*;
import static yaffs2.utils.emulation.FileEmulationUnix.*;
import static yaffs2.utils.emulation.Utils.*;
import static yaffs2.utils.Constants.*;
import static yaffs2.utils.Utils.*;

public class Dtest_C {
	/*
	 * YAFFS: Yet Another Flash File System. A NAND-flash specific file system.
	 *
	 * Copyright (C) 2002-2007 Aleph One Ltd.
	 *   for Toby Churchill Ltd and Brightstar Engineering
	 *
	 * Created by Charles Manning <charles@aleph1.co.uk>
	 *
	 * This program is free software; you can redistribute it and/or modify
	 * it under the terms of the GNU General Public License version 2 as
	 * published by the Free Software Foundation.
	 */

	/*
	* Test code for the "direct" interface. 
	*/
	
	//void dumpDir(const char *dname);

	static byte[] xx = new byte[600];//char xx[600];
	static final int xxIndex = 0;

	static void copy_in_a_file(String yaffsName, String inName)
	{
		RandomAccessFile inh;
		int outh;
		/*unsigned char buffer[100];*/
		byte[] buffer = new byte[100]; final int bufferIndex = 0;
		int ni,no;
		inh = open(inName,O_RDONLY);
		outh = yaffs_open(StringToByteArray(yaffsName), 0, O_CREAT | O_RDWR | O_TRUNC, S_IREAD | S_IWRITE);
		
		while((ni = read(inh,buffer,bufferIndex,100)) > 0)
		{
			no = yaffs_write(outh,buffer,bufferIndex,ni);
			if(ni != no)
			{
				printf("problem writing yaffs file\n");
			}
			
		}
		
		yaffs_close(outh);
		close(inh);
	}

	static void make_a_file(byte[] yaffsName,int yaffsNameIndex,byte bval,int sizeOfFile)
	{
		int outh;
		int i;
		//unsigned char buffer[100];
		byte[] buffer = new byte[100]; final int bufferIndex = 0;

		outh = yaffs_open(yaffsName, yaffsNameIndex, O_CREAT | O_RDWR | O_TRUNC, S_IREAD | S_IWRITE);
		
		memset(buffer,bufferIndex,bval,100);
		
		do{
			i = sizeOfFile;
			if(i > 100) i = 100;
			sizeOfFile -= i;
			
			yaffs_write(outh,buffer,bufferIndex,i);
			
		} while (sizeOfFile > 0);
		
			
		yaffs_close(outh);

	}

	static void make_pattern_file(byte[] fn,int fnIndex,int size)
	{
		int outh;
		int marker;
		int i;
		outh = yaffs_open(fn, fnIndex, O_CREAT | O_RDWR | O_TRUNC, S_IREAD | S_IWRITE);
		yaffs_lseek(outh,size-1,SEEK_SET);
		yaffs_write(outh,new byte[] {'A'},0,1);
		
		for(i = 0; i < size; i+=256)
		{
			marker = ~i;
			yaffs_lseek(outh,i,SEEK_SET);
			yaffs_write(outh,IntToByteArray(marker),0,SIZEOF_INT);
		}
		yaffs_close(outh);
		
	}

	static boolean check_pattern_file(byte[] fn, int fnIndex)
	{
		int h;
		byte[] marker = new byte[SIZEOF_INT];
		int i;
		int size;
		boolean ok = true;
		
		h = yaffs_open(fn, fnIndex, O_RDWR,0);
		size = yaffs_lseek(h,0,SEEK_END);
			
		for(i = 0; i < size; i+=256)
		{
			yaffs_lseek(h,i,SEEK_SET);
			yaffs_read(h,marker,0,SIZEOF_INT);
			ok = (getIntFromByteArray(marker,0) == ~i);
			if(!ok)
			{
			   printf("pattern check failed on file %a, size %d at position %d. Got %x instead of %x\n",
						fn,fnIndex,size,i,getIntFromByteArray(marker,0),~i);
			}
		}
		yaffs_close(h);
		return ok;
	}





	static boolean dump_file_data(byte[] fn, int fnIndex)
	{
		int h;
		int marker;
		int i = 0;
		int size;
		boolean ok = true;
		/*unsigned*/ byte[] b = new byte[1];
		
		h = yaffs_open(fn, fnIndex, O_RDWR,0);
					
		printf("%a\n",fn,fnIndex);
		while(yaffs_read(h,b,0,1)> 0)
		{
			printf("%02y",b);
			i++;
			if(i > 32) 
			{
			   printf("\n");
			   i = 0;;
			 }
		}
		printf("\n");
		yaffs_close(h);
		return ok;
	}



	static void dump_file(byte[] fn, int fnIndex)
	{
		int i;
		int size;
		int h;
		
		h = yaffs_open(fn,fnIndex,O_RDONLY,0);
		if(h < 0)
		{
			printf("*****\nDump file %a does not exist\n",fn,fnIndex);
		}
		else
		{
			size = yaffs_lseek(h,0,SEEK_SET);
			printf("*****\nDump file %a size %d\n",fn,fnIndex,size);
			for(i = 0; i < size; i++)
			{
				
			}
		}
	}

	static void create_file_of_size(byte[] fn,int fnIndex,int syze)
	{
		int h;
		int n;
		
		//char xx[200];
		byte[] xx = new byte[200]; final int xxIndex = 0; 
		
		int iterations = (syze + strlen(fn,fnIndex) -1)/ strlen(fn,fnIndex);	// BUG FOUND
		//int iterations = (syze + yaffs2.utils.FileNameLength.fnLength(fn, fnIndex) -1)/ yaffs2.utils.FileNameLength.fnLength(fn, fnIndex);
		
		h = yaffs_open(fn, fnIndex, O_CREAT | O_RDWR | O_TRUNC, S_IREAD | S_IWRITE);
			
		while (iterations > 0)
		{
			sprintf(xx,xxIndex,"%a %8d",fn,fnIndex,iterations);
			yaffs_write(h,xx,xxIndex,strlen(xx, xxIndex));
			iterations--;
		}
		yaffs_close (h);
	}

	static void verify_file_of_size(byte[] fn,int fnIndex,int syze)
	{
		int h;
		int n;
		
		byte[] xx = new byte[200]; final int xxIndex = 0;
		byte[] yy = new byte[200]; final int yyIndex = 0;
		int l;
		
		int iterations = (syze + strlen(fn,fnIndex) -1)/ strlen(fn,fnIndex);
		
		h = yaffs_open(fn, fnIndex, O_RDONLY, S_IREAD | S_IWRITE);
			
		while (iterations > 0)
		{
			sprintf(xx,xxIndex,"%a %8d",fn,fnIndex,iterations);
			l = strlen(xx,xxIndex);
			
			yaffs_read(h,yy,xxIndex,l);
			yy[l] = 0;
			
			if(strcmp(xx,xxIndex,yy,yyIndex) != 0){
				printf("=====>>>>> verification of file %a failed near position %d\n",fn,fnIndex,yaffs_lseek(h,0,SEEK_CUR));
			}
			iterations--;
		}
		yaffs_close (h);
	}

	static void create_resized_file_of_size(/*const char **/ byte[] fn,int fnIndex,
			int syze1,int reSyze, int syze2)
	{
		int h;
		int n;
		
		
		int iterations;
		
		h = yaffs_open(fn, fnIndex, O_CREAT | O_RDWR | O_TRUNC, S_IREAD | S_IWRITE);
			
		iterations = (syze1 + strlen(fn,fnIndex) -1)/ strlen(fn,fnIndex);
		while (iterations > 0)
		{
			yaffs_write(h,fn,fnIndex,strlen(fn,fnIndex));
			iterations--;
		}
		
		yaffs_truncate(h,reSyze);
		
		yaffs_lseek(h,0,SEEK_SET);
		iterations = (syze2 + strlen(fn,fnIndex) -1)/ strlen(fn,fnIndex);
		while (iterations > 0)
		{
			yaffs_write(h,fn,fnIndex,strlen(fn,fnIndex));
			iterations--;
		}
		
		yaffs_close (h);
	}


	static void do_some_file_stuff(byte[] path, int pathIndex)
	{

		byte[] fn = new byte[100]; final int fnIndex = 0;

		sprintf(fn,fnIndex,"%a/%s",path,pathIndex,"f1");
		create_file_of_size(fn,fnIndex,10000);

		sprintf(fn,fnIndex,"%a/%s",path,pathIndex,"fdel");
		create_file_of_size(fn,fnIndex,10000);
		yaffs_unlink(fn,fnIndex);

		sprintf(fn,fnIndex,"%a/%s",path,pathIndex,"f2");
		
		create_resized_file_of_size(fn,fnIndex,10000,3000,4000);
	}

	static void yaffs_backward_scan_test(byte[] path, int pathIndex)
	{
		byte[] fn = new byte[100]; final int fnIndex = 0;
		
		yaffs2.utils.Globals.configuration.yaffs_StartUp();	
		
		yaffs_mount(path,pathIndex);
		
		do_some_file_stuff(path,pathIndex);
		
		sprintf(fn,fnIndex,"%a/ddd",path,pathIndex);
		
		yaffs_mkdir(fn,fnIndex,0);
		
		do_some_file_stuff(fn,fnIndex);
		
		yaffs_unmount(path,pathIndex);
		
		yaffs_mount(path,pathIndex);
	}

	static byte[] xxzz = new byte[2000]; static int xxzzIndex = 0;


	static void yaffs_device_flush_test(byte[] path, int pathIndex)
	{
		byte[] fn = new byte[100]; int fnIndex = 0;
		int h;
		int i;
		
		yaffs2.utils.Globals.configuration.yaffs_StartUp();	
		
		yaffs_mount(path, pathIndex);
		
		do_some_file_stuff(path, pathIndex);
		
		// Open and add some data to a few files
		for(i = 0; i < 10; i++) {
		
			sprintf(fn,fnIndex,"%a/ff%d",path,pathIndex,i);

			h = yaffs_open(fn, fnIndex, O_CREAT | O_RDWR | O_TRUNC, S_IWRITE | S_IREAD);
			yaffs_write(h,xxzz,xxzzIndex,2000);
			yaffs_write(h,xxzz,xxzzIndex,2000);
		}
		yaffs_unmount(path,pathIndex);
		
		yaffs_mount(path,pathIndex);
	}



	static void short_scan_test(byte[] path, int pathIndex, int fsize, int niterations)
	{
		int i;
		byte[] fn = new byte[100]; final int fnIndex = 0;
		
		sprintf(fn,fnIndex,"%a/%s",path,pathIndex,"f1");
		
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		for(i = 0; i < niterations; i++)
		{
			printf("\n*****************\nIteration %d\n",i);
			yaffs_mount(path,pathIndex);
			printf("\nmount: Directory look-up of %a\n",path,pathIndex);
			dumpDir(path, pathIndex);
			make_a_file(fn, fnIndex,(byte)1,fsize);
			yaffs_unmount(path, pathIndex);
		}
	}



	static void scan_pattern_test(byte[] path, int pathIndex, int fsize, int niterations)
	{
		int i;
		int j;
		byte[][] fn = new byte[3][100]; final int fnIndex = 0;
		boolean result;
		
		sprintf(fn[0],fnIndex,"%a/%s",path,pathIndex,"f0");
		sprintf(fn[1],fnIndex,"%a/%s",path,pathIndex,"f1");
		sprintf(fn[2],fnIndex,"%a/%s",path,pathIndex,"f2");
		
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		for(i = 0; i < niterations; i++)
		{
			printf("\n*****************\nIteration %d\n",i);
			yaffs_mount(path, pathIndex);
			printf("\nmount: Directory look-up of %a\n",path,pathIndex);
			dumpDir(path, pathIndex);
			for(j = 0; j < 3; j++)
			{
				result = dump_file_data(fn[j],fnIndex);
				result = check_pattern_file(fn[j],fnIndex);
				make_pattern_file(fn[j],fnIndex,fsize); 
				result = dump_file_data(fn[j],fnIndex);
				result = check_pattern_file(fn[j],fnIndex);
			}
			yaffs_unmount(path, pathIndex);
		}
	}

	static void fill_disk(byte[] path, int pathIndex, int nfiles)
	{
		int h;
		int n;
		int result;
		int f;
		
		byte[] str = new byte[50]; final int strIndex = 0;
		
		for(n = 0; n < nfiles; n++)
		{
			sprintf(str,strIndex,"%a/%d",path,pathIndex,n);
			
			h = yaffs_open(str, strIndex, O_CREAT | O_RDWR | O_TRUNC, S_IREAD | S_IWRITE);
			
			printf("writing file %a handle %d ",str,strIndex,h);
			
			while ((result = yaffs_write(h,xx,xxIndex,600)) == 600)
			{
				f = yaffs_freespace(path, pathIndex);
			}
			result = yaffs_close(h);
			printf(" close %d\n",result);
		}
	}

	static void fill_disk_and_delete(byte[] path, int pathIndex, int nfiles, int ncycles)
	{
		int i,j;
		byte[] str = new byte[50]; final int strIndex = 0;
		int result;
		
		for(i = 0; i < ncycles; i++)
		{
			printf("@@@@@@@@@@@@@@ cycle %d\n",i);
			fill_disk(path,pathIndex,nfiles);
			
			for(j = 0; j < nfiles; j++)
			{
				sprintf(str,strIndex,"%a/%d",path,pathIndex,j);
				result = yaffs_unlink(str,strIndex);
				printf("unlinking file %a, result %d\n",str,strIndex,result);
			}
		}
	}


	static void fill_files(byte[] path, int pathIndex, int flags, int maxIterations, int siz)
	{
		int i;
		int j;
		byte[] str = new byte[50]; final int strIndex = 0;
		int h;
		
		i = 0;
		
		do{
			sprintf(str,strIndex,"%a/%d",path,pathIndex,i);
			h = yaffs_open(str, strIndex, O_CREAT | O_TRUNC | O_RDWR,S_IREAD | S_IWRITE);
			yaffs_close(h);

			if(h >= 0)
			{
				for(j = 0; j < siz; j++)
				{
					yaffs_write(h,str,strIndex,1);
				}
			}
			
			if((flags & 1) != 0)
			{
				yaffs_unlink(str, strIndex);
			}
			i++;
		} while(h >= 0 && i < maxIterations);
		
		if((flags & 2) != 0)
		{
			i = 0;
			do{
				sprintf(str, strIndex, "%a/%d", path, pathIndex, i);
				printf("unlink %a\n",str,strIndex);
				i++;
			} while(yaffs_unlink(str,strIndex) >= 0);
		}
	}

	static void leave_unlinked_file(byte[] path, int pathIndex,int maxIterations,int siz)
	{
		int i;
		byte[] str = new byte[50]; final int strIndex = 0;
		int h;
		
		i = 0;
		
		do{
			sprintf(str, strIndex,"%a/%d",path, pathIndex,i);
			printf("create %a\n",str, strIndex);
			h = yaffs_open(str, strIndex, O_CREAT | O_TRUNC | O_RDWR,S_IREAD | S_IWRITE);
			if(h >= 0)
			{
				yaffs_unlink(str, strIndex);
			}
			i++;
		} while(h < 0 && i < maxIterations);
		
		if(h >= 0)
		{
			for(i = 0; i < siz; i++)
			{
				yaffs_write(h,str, strIndex,1);
			}
		}
		
		printf("Leaving file %a open\n",str, strIndex);

	}

	static void dumpDirFollow(byte[] dname, int dnameIndex)
	{
		yaffs_DIR d;
		yaffs_dirent de;
		yaffs_stat s = new yaffs_stat();
		byte[] str = new byte[100]; final int strIndex = 0;
				
		d = yaffs_opendir(dname, dnameIndex);
		
		if(!(d != null))
		{
			printf("opendir failed\n");
		}
		else
		{
			while((de = yaffs_readdir(d)) != null)
			{
				sprintf(str, strIndex,"%a/%a",dname, dnameIndex,de.d_name,de.d_nameIndex);
				
				yaffs_stat(str, strIndex, s);
				
				printf("%a length %d mode %X ",de.d_name,de.d_nameIndex,(int)s.st_size,s.st_mode);
				switch(s.st_mode & S_IFMT)
				{
					case S_IFREG: printf("data file"); break;
					case S_IFDIR: printf("directory"); break;
					case S_IFLNK: printf("symlink -->");
								  if(yaffs_readlink(str, strIndex,str, strIndex,100) < 0)
									printf("no alias");
								  else
									printf("\"%a\"",str, strIndex);    
								  break;
					default: printf("unknown"); break;
				}
				
				printf("\n");           
			}
			
			yaffs_closedir(d);
		}
		printf("\n");
		
		printf("Free space in %a is %d\n\n",dname,dnameIndex,(int)yaffs_freespace(dname, dnameIndex));

	}


	static void dump_directory_tree_worker(byte[] dname, int dnameIndex, int recursive)
	{
		yaffs_DIR d;
		yaffs_dirent de;
		yaffs_stat s = new yaffs_stat();
		byte[] str = new byte[1000]; final int strIndex = 0;
				
		d = yaffs_opendir(dname, dnameIndex);
		
		if(!(d != null))
		{
			printf("opendir failed\n");
		}
		else
		{
			while((de = yaffs_readdir(d)) != null)
			{
				sprintf(str, strIndex,"%a/%a",dname,dnameIndex,de.d_name,de.d_nameIndex);
				
				yaffs_lstat(str, strIndex,s);
				
				printf("%a inode %d obj %x length %d mode %X ",str,strIndex,s.st_ino,
						yaffs2.utils.Utils.hashCode(de.d_dont_use),(int)s.st_size,s.st_mode);
				switch(s.st_mode & S_IFMT)
				{
					case S_IFREG: printf("data file"); break;
					case S_IFDIR: printf("directory"); break;
					case S_IFLNK: printf("symlink -->");
								  if(yaffs_readlink(str, strIndex,str, strIndex,100) < 0)
									printf("no alias");
								  else
									printf("\"%a\"",str, strIndex);    
								  break;
					default: printf("unknown"); break;
				}
				
				printf("\n");

				if((s.st_mode & S_IFMT) == S_IFDIR && recursive != 0)
					dump_directory_tree_worker(str, strIndex,1);
								
			}
			
			yaffs_closedir(d);
		}

	}

	static void dump_directory_tree(byte[] dname, int dnameIndex)
	{
		dump_directory_tree_worker(dname, dnameIndex,1);
		printf("\n");
		printf("Free space in %a is %d\n\n",dname,dnameIndex,(int)yaffs_freespace(dname, dnameIndex));
	}

	static void dumpDir(byte[] dname, int dnameIndex)
	{	dump_directory_tree_worker(dname, dnameIndex,0);
		printf("\n");
		printf("Free space in %a is %d\n\n",dname, dnameIndex,(int)yaffs_freespace(dname, dnameIndex));
	}


	static void PermissionsCheck(byte[] path, int pathIndex, /*mode_t*/ int tmode, int tflags,int expectedResult)
	{
		int fd;
		
		if(yaffs_chmod(path, pathIndex,tmode)< 0) printf("chmod failed\n");
		
		fd = yaffs_open(path, pathIndex,tflags,0);
		
		if((fd >= 0) != (expectedResult > 0))
		{
			printf("Permissions check %x %x %d failed\n",tmode,tflags,expectedResult);
		}
		else
		{
			printf("Permissions check %x %x %d OK\n",tmode,tflags,expectedResult);
		}
		
		
		yaffs_close(fd);
		
		
	}


	static int long_test(/*int argc, char *argv[]*/)
	{

		int f;
		int r;

		byte[] buffer = new byte[20]; final int bufferIndex = 0;
		byte[] str = new byte[100]; final int strIndex = 0;

		int h;
		
		int temp_mode;
		yaffs_stat ystat = new yaffs_stat();
		
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
	
		yaffs_mount(StringToByteArray("/flash/boot"), 0);
//		yaffs_mount(StringToByteArray("/boot"), 0);
//		yaffs_mount(StringToByteArray("/data"), 0);
//		yaffs_mount(StringToByteArray("/flash"), 0);
//		yaffs_mount(StringToByteArray("/ram"), 0);
		
		printf("\nDirectory look-up of /flash/boot\n");
		dumpDir(StringToByteArray("/flash/boot"), 0);
//		printf("\nDirectory look-up of /boot\n");
//		dumpDir(StringToByteArray("/boot"), 0);
//		printf("\nDirectory look-up of /data\n");
//		dumpDir(StringToByteArray("/data"), 0);
//		printf("\nDirectory look-up of /flash\n");
//		dumpDir(StringToByteArray("/flash"), 0);

		//leave_unlinked_file("/flash",20000,0);
		//leave_unlinked_file("/data",20000,0);
		
//		leave_unlinked_file(StringToByteArray("/ram"),0,20,0);
		

		f = yaffs_open(StringToByteArray("/flashboot/b1"), 0, O_RDONLY,0);
		
		printf("open /flash/boot/b1 readonly, f=%d\n",f);
		
		f = yaffs_open(StringToByteArray("/flash/boot/b1"), 0, O_CREAT,S_IREAD | S_IWRITE);
		
		printf("open /flash/boot/b1 O_CREAT, f=%d\n",f);
		
		
		r = yaffs_write(f,StringToByteArray("hello"),0,1);
		printf("write %d attempted to write to a read-only file\n",r);
		
		r = yaffs_close(f);
		
		printf("close %d\n",r);

		f = yaffs_open(StringToByteArray("/flash/boot/b1"), 0, O_RDWR,0);
		
		printf("open /flash/boot/b1 O_RDWR,f=%d\n",f);
		
		
		r = yaffs_write(f,StringToByteArray("hello"),0,2);
		printf("write %d attempted to write to a writeable file\n",r);
		r = yaffs_write(f,StringToByteArray("world"),0,3);
		printf("write %d attempted to write to a writeable file\n",r);
		
		r= yaffs_lseek(f,0,SEEK_END);
		printf("seek end %d\n",r);
		memset(buffer,bufferIndex,(byte)0,20);
		r = yaffs_read(f,buffer,bufferIndex,10);
		printf("read %d \"%s\"\n",r,byteArrayToString(buffer,0));
		r= yaffs_lseek(f,0,SEEK_SET);
		printf("seek set %d\n",r);
		memset(buffer,bufferIndex,(byte)0,20);
		r = yaffs_read(f,buffer,bufferIndex,10);
		printf("read %d \"%s\"\n",r,byteArrayToString(buffer,0));
		memset(buffer,bufferIndex,(byte)0,20);
		r = yaffs_read(f,buffer,bufferIndex,10);
		printf("read %d \"%s\"\n",r,byteArrayToString(buffer,0));

		// Check values reading at end.
		// A read past end of file should return 0 for 0 bytes read.
			
		r= yaffs_lseek(f,0,SEEK_END);
		r = yaffs_read(f,buffer,bufferIndex,10);
		printf("read at end returned  %d\n",r); 
		r= yaffs_lseek(f,500,SEEK_END);
		r = yaffs_read(f,buffer,bufferIndex,10);
		printf("read past end returned  %d\n",r);       
		
		r = yaffs_close(f);
		
		printf("close %d\n",r);
		
		copy_in_a_file("/flash/boot/yyfile","xxx");
		
		// Create a file with a long name
		
		copy_in_a_file("/flash/boot/file with a long name","xxx");
		
		
		printf("\nDirectory look-up of /flash/boot\n");
		dumpDir(StringToByteArray("/flash/boot"),0);

		// Check stat
		r = yaffs_stat(StringToByteArray("/flash/boot/file with a long name"),0,ystat);
		
		// Check rename
		
		r = yaffs_rename(StringToByteArray("/flash/boot/file with a long name"),0,StringToByteArray("/flash/boot/r1"),0);
		
		printf("\nDirectory look-up of /flash/boot\n");
		dumpDir(StringToByteArray("/flash/boot"),0);
		
		// Check unlink
		r = yaffs_unlink(StringToByteArray("/flash/boot/r1"),0);
		
		printf("\nDirectory look-up of /flash/boot\n");
		dumpDir(StringToByteArray("/flash/boot"),0);

		// Check mkdir
		
		r = yaffs_mkdir(StringToByteArray("/flash/boot/directory1"),0,0);
		
		printf("\nDirectory look-up of /flash/boot\n");
		dumpDir(StringToByteArray("/flash/boot"),0);
		printf("\nDirectory look-up of /flash/boot/directory1\n");
		dumpDir(StringToByteArray("/flash/boot/directory1"),0);

		// add a file to the directory                  
		copy_in_a_file("/flash/boot/directory1/file with a long name","xxx");
		
		printf("\nDirectory look-up of /flash/boot\n");
		dumpDir(StringToByteArray("/flash/boot"),0);
		printf("\nDirectory look-up of /flash/boot/directory1\n");
		dumpDir(StringToByteArray("/flash/boot/directory1"),0);
		
		//  Attempt to delete directory (should fail)
		
		r = yaffs_rmdir(StringToByteArray("/flash/boot/directory1"),0);
		
		printf("\nDirectory look-up of /flash/boot\n");
		dumpDir(StringToByteArray("/flash/boot"),0);
		printf("\nDirectory look-up of /flash/boot/directory1\n");
		dumpDir(StringToByteArray("/flash/boot/directory1"),0);
		
		// Delete file first, then rmdir should work
		r = yaffs_unlink(StringToByteArray("/flash/boot/directory1/file with a long name"),0);
		r = yaffs_rmdir(StringToByteArray("/flash/boot/directory1"),0);
		
		
		printf("\nDirectory look-up of /flash/boot\n");
		dumpDir(StringToByteArray("/flash/boot"),0);
		printf("\nDirectory look-up of /flash/boot/directory1\n");
		dumpDir(StringToByteArray("/flash/boot/directory1"),0);

//	#if 0
//		fill_disk_and_delete("/boot",20,20);
//				
//		printf("\nDirectory look-up of /boot\n");
//		dumpDir("/boot");
//	#endif

		yaffs_symlink(StringToByteArray("yyfile"),0,StringToByteArray("/flash/boot/slink"),0);
		
		yaffs_readlink(StringToByteArray("/flash/boot/slink"),0,str,strIndex,100);
		printf("symlink alias is %s\n",byteArrayToString(str,0));
		

		printf("\nDirectory look-up of /flash/boot\n");
		dumpDir(StringToByteArray("/flash/boot"),0);
		printf("\nDirectory look-up of /flash/boot (using stat instead of lstat)\n");
		dumpDirFollow(StringToByteArray("/flash/boot"),0);
		printf("\nDirectory look-up of /flash/boot/directory1\n");
		dumpDir(StringToByteArray("/flash/boot/directory1"),0);

		h = yaffs_open(StringToByteArray("/flash/boot/slink"),0,O_RDWR,0);
		
		printf("file length is %d\n",(int)yaffs_lseek(h,0,SEEK_END));
		
		yaffs_close(h);
		
		yaffs_unlink(StringToByteArray("/flash/boot/slink"),0);

		
		printf("\nDirectory look-up of /flash/boot\n");
		dumpDir(StringToByteArray("/flash/boot"),0);
		
		// Check chmod
		
		yaffs_stat(StringToByteArray("/flash/boot/yyfile"),0,ystat);
		temp_mode = ystat.st_mode;
		
		yaffs_chmod(StringToByteArray("/flash/boot/yyfile"),0,0x55555);
		printf("\nDirectory look-up of /flash/boot\n");
		dumpDir(StringToByteArray("/flash/boot"),0);
		
		yaffs_chmod(StringToByteArray("/flash/boot/yyfile"),0,temp_mode);
		printf("\nDirectory look-up of /flash/boot\n");
		dumpDir(StringToByteArray("/flash/boot"),0);
		
		// Permission checks...
		PermissionsCheck(StringToByteArray("/flash/boot/yyfile"),0,0, O_WRONLY,0);
		PermissionsCheck(StringToByteArray("/flash/boot/yyfile"),0,0, O_RDONLY,0);
		PermissionsCheck(StringToByteArray("/flash/boot/yyfile"),0,0, O_RDWR,0);

		PermissionsCheck(StringToByteArray("/flash/boot/yyfile"),0,S_IREAD, O_WRONLY,0);
		PermissionsCheck(StringToByteArray("/flash/boot/yyfile"),0,S_IREAD, O_RDONLY,1);
		PermissionsCheck(StringToByteArray("/flash/boot/yyfile"),0,S_IREAD, O_RDWR,0);

		PermissionsCheck(StringToByteArray("/flash/boot/yyfile"),0,S_IWRITE, O_WRONLY,1);
		PermissionsCheck(StringToByteArray("/flash/boot/yyfile"),0,S_IWRITE, O_RDONLY,0);
		PermissionsCheck(StringToByteArray("/flash/boot/yyfile"),0,S_IWRITE, O_RDWR,0);
		
		PermissionsCheck(StringToByteArray("/flash/boot/yyfile"),0,S_IREAD | S_IWRITE, O_WRONLY,1);
		PermissionsCheck(StringToByteArray("/flash/boot/yyfile"),0,S_IREAD | S_IWRITE, O_RDONLY,1);
		PermissionsCheck(StringToByteArray("/flash/boot/yyfile"),0,S_IREAD | S_IWRITE, O_RDWR,1);

		yaffs_chmod(StringToByteArray("/flash/boot/yyfile"),0,temp_mode);
		
		//create a zero-length file and unlink it (test for scan bug)
		
		h = yaffs_open(StringToByteArray("/flash/boot/zlf"),0,O_CREAT | O_TRUNC | O_RDWR,0);
		yaffs_close(h);
		
		yaffs_unlink(StringToByteArray("/flash/boot/zlf"),0);
		
		
		yaffs_DumpDevStruct(StringToByteArray("/flash/boot"),0);
		
		fill_disk_and_delete(StringToByteArray("/flash/boot"),0,20,20);
		
		yaffs_DumpDevStruct(StringToByteArray("/flash/boot"),0);
		
		fill_files(StringToByteArray("/flash/boot"),0,1,10000,0);
		fill_files(StringToByteArray("/flash/boot"),0,1,10000,5000);
		fill_files(StringToByteArray("/flash/boot"),0,2,10000,0);
		fill_files(StringToByteArray("/flash/boot"),0,2,10000,5000);
		
//		leave_unlinked_file(StringToByteArray("/data"),0,20000,0);
//		leave_unlinked_file(StringToByteArray("/data"),0,20000,5000);
//		leave_unlinked_file(StringToByteArray("/data"),0,20000,5000);
//		leave_unlinked_file(StringToByteArray("/data"),0,20000,5000);
//		leave_unlinked_file(StringToByteArray("/data"),0,20000,5000);
//		leave_unlinked_file(StringToByteArray("/data"),0,20000,5000);
		
		yaffs_DumpDevStruct(StringToByteArray("/flash/boot"),0);
//		yaffs_DumpDevStruct(StringToByteArray("/data"),0);
		
			
			
		return 0;

	}
	

	static int huge_directory_test_on_path(String path)
	{

		yaffs_DIR d;
		yaffs_dirent de;
		yaffs_stat s = new yaffs_stat();

		int f;
		int i;
		int r;
		int total = 0;
		int lastTotal = 0;
		byte[] buffer = new byte[20]; final int bufferIndex = 0;
		
		byte[] str = new byte[100]; final int strIndex = 0;
		byte[] name = new byte[100]; final int nameIndex = 0;
		byte[] name2 = new byte[100]; final int name2Index = 0;
		
		int h;
		int temp_mode;
		yaffs_stat ystat;
		
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		yaffs_mount(StringToByteArray(path),0);
		
		// Create a large number of files
		
		for(i = 0; i < 2000; i++)
		{
		  sprintf(str,strIndex,"%s/%d",path,i);
		  
		   f = yaffs_open(str,strIndex,O_CREAT,S_IREAD | S_IWRITE);
		   yaffs_close(f);
		}
		
		
		
		d = yaffs_opendir(StringToByteArray(path),0);
		i = 0;
		if (d != null) {
		while((de = yaffs_readdir(d)) != null) {
		if (total >lastTotal+100*9*1024||(i & 1023)==0){
		printf("files = %d, total = %d\n",i, total);
		lastTotal = total;
		}
			i++;
			sprintf(str,strIndex,"%s/%s",path,byteArrayToString(de.d_name,de.d_nameIndex));
			yaffs_lstat(str,strIndex,s);
			switch(s.st_mode & S_IFMT){
			case S_IFREG:
		//printf("data file");
		total += s.st_size;
		break;
		}
		}
		
		yaffs_closedir(d);
		}
		
		return 0;
	}

//	static int yaffs_scan_test(const char *path)
//	{
//	}
//
//
	static void rename_over_test(String mountpt)
	{
		int i;
		byte[] a = new byte[100]; final int aIndex = 0;
		byte[] b = new byte[100]; final int bIndex = 0;
		
		sprintf(a,aIndex,"%s/a",mountpt);
		sprintf(b,bIndex,"%s/b",mountpt);
		
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		yaffs_mount(StringToByteArray(mountpt),0);
		i = yaffs_open(a,aIndex,O_CREAT | O_TRUNC | O_RDWR, 0); 
		yaffs_close(i);
		i = yaffs_open(b,bIndex,O_CREAT | O_TRUNC | O_RDWR, 0);
		yaffs_close(i);
		yaffs_rename(a,aIndex,b,bIndex); // rename over
		yaffs_rename(b,bIndex,a,aIndex); // rename back again (not renaimng over)
		yaffs_rename(a,aIndex,b,bIndex); // rename back again (not renaimng over)
		
		
		yaffs_unmount(StringToByteArray(mountpt),0);
		
	}
	
	static int resize_stress_test(String path)
	{
	   int a,b,i,j;
	   int x;
	   int r;
	   byte[] aname = new byte[100]; final int anameIndex = 0;
	   byte[] bname = new byte[100]; final int bnameIndex = 0;
	   
	   byte[] abuffer = new byte[1000]; final int abufferIndex = 0;
	   byte[] bbuffer = new byte[1000]; final int bbufferIndex = 0;
	   
	   yaffs2.utils.Globals.configuration.yaffs_StartUp();
	   
	   yaffs_mount(StringToByteArray(path),0);
	   
	   sprintf(aname,anameIndex,"%s%s",path,"/a");
	   sprintf(bname,bnameIndex,"%s%s",path,"/b");
	   
	   memset(abuffer,abufferIndex,(byte)'a',1000);
	   memset(bbuffer,bbufferIndex,(byte)'b',1000);
	   
	   a = yaffs_open(aname, anameIndex, O_CREAT | O_TRUNC | O_RDWR, S_IREAD | S_IWRITE);
	   b = yaffs_open(bname, bnameIndex, O_CREAT | O_TRUNC | O_RDWR, S_IREAD | S_IWRITE);
	   
	   printf(" %s %d %s %d\n",byteArrayToString(aname,0),a,byteArrayToString(bname,0),b);
	  
	   x = 0;
	   
	   for(j = 0; j < 100; j++)
	   {
			yaffs_lseek(a,0,SEEK_END);

			
			for(i = 0; i <20000; i++)
			{
			   //r =        yaffs_lseek(b,i,SEEK_SET);
				//r = yaffs_write(b,bbuffer,1000);
				
				if((x & 0x16) != 0)
				{
					// shrink
					int syz = yaffs_lseek(a,0,SEEK_END);
					
					syz -= 500;
					if(syz < 0) syz = 0;
					yaffs_truncate(a,syz);
					
				}
				else
				{
					//expand
					r = yaffs_lseek(a,i * 500,SEEK_SET);
					r = yaffs_write(a,abuffer,abufferIndex,1000);
				}
				x++;
				
			}
	   }
	   
	   return 0;
	   
	}

	
	static int resize_stress_test_no_grow_complex(String path,int iters)
	{
	   int a,b,i,j;
	   int x;
	   int r;
	   byte[] aname = new byte[100]; final int anameIndex = 0;
	   byte[] bname = new byte[100]; final int bnameIndex = 0;
	   
	   byte[] abuffer = new byte[1000]; final int abufferIndex = 0;
	   byte[] bbuffer = new byte[1000]; final int bbufferIndex = 0;
	   
	   yaffs2.utils.Globals.configuration.yaffs_StartUp();
	   
	   yaffs_mount(StringToByteArray(path),0);
	   
	   sprintf(aname,anameIndex,"%s%s",path,"/a");
	   sprintf(bname,bnameIndex,"%s%s",path,"/b");
	   
	   memset(abuffer,abufferIndex,(byte)'a',1000);
	   memset(bbuffer,bbufferIndex,(byte)'b',1000);
	   
	   a = yaffs_open(aname, anameIndex, O_CREAT | O_TRUNC | O_RDWR, S_IREAD | S_IWRITE);
	   b = yaffs_open(bname, bnameIndex, O_CREAT | O_TRUNC | O_RDWR, S_IREAD | S_IWRITE);
	   
	   printf(" %s %d %s %d\n",byteArrayToString(aname,anameIndex),a,byteArrayToString(bname,bnameIndex),b);
	  
	   x = 0;
	   
	   for(j = 0; j < iters; j++)
	   {
			yaffs_lseek(a,0,SEEK_END);

			
			for(i = 0; i <20000; i++)
			{
			   //r =        yaffs_lseek(b,i,SEEK_SET);
				//r = yaffs_write(b,bbuffer,1000);
				
				if(!(x%20 != 0))
				{
					// shrink
					int syz = yaffs_lseek(a,0,SEEK_END);
					
					while(syz > 4000)
					{
					
						syz -= 2050;
						if(syz < 0) syz = 0;
						yaffs_truncate(a,syz);
						syz = yaffs_lseek(a,0,SEEK_END);
						printf("shrink to %d\n",syz);
					}
					
					
				}
				else
				{
					//expand
					r = yaffs_lseek(a,500,SEEK_END);
					r = yaffs_write(a,abuffer,abufferIndex,1000);
				}
				x++;
				
						
			}
			printf("file size is %d\n",yaffs_lseek(a,0,SEEK_END));

	   }
	   
	   return 0;
	   
	}
//
//	static int resize_stress_test_no_grow(const char *path,int iters)
//	{
//	   int a,b,i,j;
//	   int x;
//	   int r;
//	   char aname[100];
//	   char bname[100];
//	   
//	   char abuffer[1000];
//	   char bbuffer[1000];
//	   
//	   yaffs_StartUp();
//	   
//	   yaffs_mount(path);
//	   
//	   sprintf(aname,"%s%s",path,"/a");
//	   sprintf(bname,"%s%s",path,"/b");
//	   
//	   memset(abuffer,'a',1000);
//	   memset(bbuffer,'b',1000);
//	   
//	   a = yaffs_open(aname, O_CREAT | O_TRUNC | O_RDWR, S_IREAD | S_IWRITE);
//	   b = yaffs_open(bname, O_CREAT | O_TRUNC | O_RDWR, S_IREAD | S_IWRITE);
//	   
//	   printf(" %s %d %s %d\n",aname,a,bname,b);
//	  
//	   x = 0;
//	   
//	   for(j = 0; j < iters; j++)
//	   {
//			yaffs_lseek(a,0,SEEK_END);
//
//			
//			for(i = 0; i <20000; i++)
//			{
//			   //r =        yaffs_lseek(b,i,SEEK_SET);
//				//r = yaffs_write(b,bbuffer,1000);
//				
//				if(!(x%20))
//				{
//					// shrink
//					int syz = yaffs_lseek(a,0,SEEK_END);
//					
//					while(syz > 4000)
//					{
//					
//						syz -= 2050;
//						if(syz < 0) syz = 0;
//						yaffs_truncate(a,syz);
//						syz = yaffs_lseek(a,0,SEEK_END);
//						printf("shrink to %d\n",syz);
//					}
//					
//					
//				}
//				else
//				{
//					//expand
//					r = yaffs_lseek(a,-500,SEEK_END);
//					r = yaffs_write(a,abuffer,1000);
//				}
//				x++;
//				
//						
//			}
//			printf("file size is %d\n",yaffs_lseek(a,0,SEEK_END));
//
//	   }
//	   
//	   return 0;
//	   
//	}
//
//	static int directory_rename_test(void)
//	{
//		int r;
//		yaffs_StartUp();
//		
//		yaffs_mount("/ram");
//		yaffs_mkdir("/ram/a",0);
//		yaffs_mkdir("/ram/a/b",0);
//		yaffs_mkdir("/ram/c",0);
//		
//		printf("\nDirectory look-up of /ram\n");
//		dumpDir("/ram");
//		dumpDir("/ram/a");
//		dumpDir("/ram/a/b");
//
//		printf("Do rename (should fail)\n");
//			
//		r = yaffs_rename("/ram/a","/ram/a/b/d");
//		printf("\nDirectory look-up of /ram\n");
//		dumpDir("/ram");
//		dumpDir("/ram/a");
//		dumpDir("/ram/a/b");
//
//		printf("Do rename (should not fail)\n");
//			
//		r = yaffs_rename("/ram/c","/ram/a/b/d");
//		printf("\nDirectory look-up of /ram\n");
//		dumpDir("/ram");
//		dumpDir("/ram/a");
//		dumpDir("/ram/a/b");
//		
//		
//		return 1;
//		
//	}
//
	static int cache_read_test()
	{
		int a,b,c;
		int i;
		int sizeOfFiles = 500000;
		byte[] buffer = new byte[100]; final int bufferIndex = 0;
		
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		yaffs_mount(StringToByteArray("/flash/boot"), 0);
		
		make_a_file(StringToByteArray("/flash/boot/a"), 0, (byte)'a',sizeOfFiles);
		make_a_file(StringToByteArray("/flash/boot/b"), 0, (byte)'b',sizeOfFiles);

		a = yaffs_open(StringToByteArray("/flash/boot/a"),0,O_RDONLY,0);
		b = yaffs_open(StringToByteArray("/flash/boot/b"),0,O_RDONLY,0);
		c = yaffs_open(StringToByteArray("/flash/boot/c"),0, O_CREAT | O_RDWR | O_TRUNC, S_IREAD | S_IWRITE);

		do{
			i = sizeOfFiles;
			if (i > 100) i = 100;
			sizeOfFiles  -= i;
			yaffs_read(a,buffer,bufferIndex,i);
			yaffs_read(b,buffer,bufferIndex,i);
			yaffs_write(c,buffer,bufferIndex,i);
		} while(sizeOfFiles > 0);
		
		
		return 1;
		
	}
//	XXX not worth translating
//	static int cache_bypass_bug_test(void)
//	{
//		// This test reporoduces a bug whereby YAFFS caching *was* buypassed
//		// resulting in erroneous reads after writes.
//		// This bug has been fixed.
//		
//		int a;
//		int i;
//		char buffer1[1000];
//		char buffer2[1000];
//		
//		memset(buffer1,0,sizeof(buffer1));
//		memset(buffer2,0,sizeof(buffer2));
//			
//		yaffs_StartUp();
//		
//		yaffs_mount("/boot");
//		
//		// Create a file of 2000 bytes.
//		make_a_file("/boot/a",'X',2000);
//
//		a = yaffs_open("/boot/a",O_RDWR, S_IREAD | S_IWRITE);
//		
//		// Write a short sequence to the file.
//		// This will go into the cache.
//		yaffs_lseek(a,0,SEEK_SET);
//		yaffs_write(a,"abcdefghijklmnopqrstuvwxyz",20); 
//
//		// Read a short sequence from the file.
//		// This will come from the cache.
//		yaffs_lseek(a,0,SEEK_SET);
//		yaffs_read(a,buffer1,30); 
//
//		// Read a page size sequence from the file.
//		yaffs_lseek(a,0,SEEK_SET);
//		yaffs_read(a,buffer2,512); 
//		
//		printf("buffer 1 %s\n",buffer1);
//		printf("buffer 2 %s\n",buffer2);
//		
//		if(strncmp(buffer1,buffer2,20))
//		{
//			printf("Cache bypass bug detected!!!!!\n");
//		}
//		
//		
//		return 1;
//	}
//
//
	static int free_space_check()
	{
		int f;
		
			yaffs2.utils.Globals.configuration.yaffs_StartUp();
			yaffs_mount(StringToByteArray("/flash/boot"),0);
		    fill_disk(StringToByteArray("/flash/boot/"),0,2);
		    f = yaffs_freespace(StringToByteArray("/flash/boot"),0);
		    
		    printf("%d free when disk full\n",f);           
		    return 1;
	}

	static int truncate_test()
	{
		int a;
		int r;
		int i;
		int l;

		byte[] y = new byte[10]; final int yIndex = 0;
		byte[] tmp = new byte[1];

		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		yaffs_mount(StringToByteArray("/flash/boot"),0);

		yaffs_unlink(StringToByteArray("/flash/boot/trunctest"),0);
		
		a = yaffs_open(StringToByteArray("/flash/boot/trunctest"),0, O_CREAT | O_TRUNC | O_RDWR,  S_IREAD | S_IWRITE);
		
		yaffs_write(a,StringToByteArray("abcdefghijklmnopqrstuvwzyz"),0,26);
		
		yaffs_truncate(a,3);
		l= yaffs_lseek(a,0,SEEK_END);
		
		printf("truncated length is %d\n",l);

		yaffs_lseek(a,5,SEEK_SET);
		yaffs_write(a,StringToByteArray("1"),0,1);

		yaffs_lseek(a,0,SEEK_SET);
		
		r = yaffs_read(a,y,yIndex,10);

		printf("read %d bytes:",r);

		for(i = 0; i < r; i++)
			{
			tmp[0] = y[i];
			printf("[%y]",tmp);
			}

		printf("\n");

		return 0;

	}





	static void fill_disk_test(String mountpt)
	{
		int i;
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		for(i = 0; i < 5; i++)
		{
			yaffs_mount(StringToByteArray(mountpt),0);
			fill_disk_and_delete(StringToByteArray(mountpt),0,100,i+1);
			yaffs_unmount(StringToByteArray(mountpt),0);
		}
		
	}



	static void lookup_test(String mountpt)
	{
		int i;
		int h;
		byte[] a = new byte[100]; final int aIndex = 0;
		byte[] b = new byte[100]; final int bIndex = 0;
		

		yaffs_DIR d;
		yaffs_dirent de;
		yaffs_stat s = new yaffs_stat();
		byte[] str = new byte[100]; final int strIndex = 0;

		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		yaffs_mount(StringToByteArray(mountpt),0);
					
		d = yaffs_opendir(StringToByteArray(mountpt),0);
		
		if(!(d != null))
		{
			printf("opendir failed\n");
		}
		else
		{
			
			for(i = 0; (de = yaffs_readdir(d)) != null; i++)
			{
				printf("unlinking %s\n",byteArrayToString(de.d_name,de.d_nameIndex));
				yaffs_unlink(de.d_name,de.d_nameIndex);
			}
			
			printf("%d files deleted\n",i);
		}
		
		
		for(i = 0; i < 2000; i++){
		sprintf(a,aIndex,"%s/%d",mountpt,i);
			h =  yaffs_open(a,aIndex,O_CREAT | O_TRUNC | O_RDWR, 0);
			yaffs_close(h);
		}

		yaffs_rewinddir(d);
		for(i = 0; (de = yaffs_readdir(d)) != null; i++)
		{
			printf("%d  %s\n",i,byteArrayToString(de.d_name,de.d_nameIndex));
		}	
		
		printf("%d files listed\n\n\n",i);
		
		yaffs_rewinddir(d);
		yaffs_readdir(d);
		yaffs_readdir(d);
		yaffs_readdir(d);
		
		for(i = 0; i < 2000; i++){
			sprintf(a,aIndex,"%s/%d",mountpt,i);
			yaffs_unlink(a,aIndex);
		}
		
			
		yaffs_unmount(StringToByteArray(mountpt),0);
		
	}

	static void link_test(String mountpt)
	{
		int i;
		int h;
		byte[] a = new byte[100]; final int aIndex = 0;
		byte[] b = new byte[100]; final int bIndex = 0;
		byte[] c = new byte[100]; final int cIndex = 0;
		
		int  f0;
		int f1;
		int f2;
		int f3;
		sprintf(a,aIndex,"%s/aaa",mountpt);
		sprintf(b,bIndex,"%s/bbb",mountpt);
		sprintf(c,cIndex,"%s/ccc",mountpt);
		
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		yaffs_mount(StringToByteArray(mountpt),0);
		
		
		h = yaffs_open(a, aIndex, O_CREAT | O_TRUNC | O_RDWR, S_IREAD | S_IWRITE);
		for(i = 0; i < 100; i++)
			yaffs_write(h,a,aIndex,100);
		
		yaffs_close(h);
		
		yaffs_unlink(b,bIndex);
		yaffs_unlink(c,cIndex);
		yaffs_link(a,aIndex,b,bIndex);
		yaffs_link(a,aIndex,c,cIndex);
		yaffs_unlink(b,bIndex);
		yaffs_unlink(c,cIndex);
		yaffs_unlink(a,aIndex);
		
		
		yaffs_unmount(StringToByteArray(mountpt),0);
		yaffs_mount(StringToByteArray(mountpt),0);
		
		printf("link test done\n");	
		
	}

	static void freespace_test(String mountpt)
	{
		int i;
		int h;
		byte[] a = new byte[100]; final int aIndex = 0;
		byte[] b = new byte[100]; final int bIndex = 0;
		
		int  f0;
		int f1;
		int f2;
		int f3;
		sprintf(a,aIndex,"%s/aaa",mountpt);
		
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		yaffs_mount(StringToByteArray(mountpt),0);
		
		f0 = yaffs_freespace(StringToByteArray(mountpt),0);
		
		h = yaffs_open(a, aIndex, O_CREAT | O_TRUNC | O_RDWR, S_IREAD | S_IWRITE);
		
		for(i = 0; i < 100; i++)
			yaffs_write(h,a,aIndex,100);
		
		yaffs_close(h);
		
		f1 = yaffs_freespace(StringToByteArray(mountpt),0);
		
		yaffs_unlink(a,aIndex);
		
		f2 = yaffs_freespace(StringToByteArray(mountpt),0);
		
			
		yaffs_unmount(StringToByteArray(mountpt),0);
		yaffs_mount(StringToByteArray(mountpt),0);
		
		f3 = yaffs_freespace(StringToByteArray(mountpt),0);
		
		printf("%d\n%d\n%d\n%d\n",f0, f1,f2,f3);
		
		
	}

	static void simple_rw_test(String mountpt)
	{
		int i;
		int h;
		byte[] a = new byte[100]; final int aIndex = 0;
		byte[] tmp;
		
		//int x;
		byte[] x;
		int result;

		sprintf(a,aIndex,"%s/aaa",mountpt);
		
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		yaffs_mount(StringToByteArray(mountpt),0);
		
		yaffs_unlink(a,aIndex);
		
		h = yaffs_open(a,aIndex,O_CREAT| O_TRUNC | O_RDWR, S_IREAD | S_IWRITE);
		
		for(i = 100000;i < 200000; i++){
			tmp = StringToByteArray(Integer.toString(i));				// TODO test it
			result = yaffs_write(h,tmp,0,tmp.length);/*(h,&i,sizeof(i))*/

			if(result != 7)
			{
				printf("write error\n");
				System.exit(1);
			}
		}
		
		//yaffs_close(h);
		
		// h = yaffs_open(a,O_RDWR, S_IREAD | S_IWRITE);
		
		
		yaffs_lseek(h,0,SEEK_SET);
		
		for(i = 100000; i < 200000; i++){
			x = StringToByteArray(Integer.toString(i));
			result = yaffs_read(h,x,0,x.length);
			
			if(result != 7 || strcmp(x,0,StringToByteArray(Integer.toString(i)),0) != 0){
				printf("read error %d %x %s\n",i,result,byteArrayToString(x,0));
			}
		}
		
		printf("Simple rw test passed\n");
		
		
		
	}


	static void scan_deleted_files_test(String mountpt)
	{
		byte[] fn = new byte[100]; final int fnIndex = 0;
		byte[] sub = new byte[100]; final int subIndex = 0;
		
		//const char *p;
		byte[] p; int pIndex;
		
		int i;
		int j;
		int k;
		int h;
		
		sprintf(sub,subIndex,"%s/sdir",mountpt);
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		for(j = 0; j < 10; j++)
		{
			printf("\n\n>>>>>>> Run %d <<<<<<<<<<<<<\n\n",j);
			yaffs_mount(StringToByteArray(mountpt),0);
			yaffs_mkdir(sub,subIndex,0);
			
			// TODO verify
			p = ((j & 0) != 0) ? StringToByteArray(mountpt): sub;
		
			for(i = 0; i < 100; i++)
			{
			  sprintf(fn,fnIndex,"%s/%d",byteArrayToString(p, 0),i);  
			  
			  if((i & 1) != 0)
			  {
				  h = yaffs_open(fn,fnIndex,O_CREAT | O_TRUNC | O_RDWR, S_IREAD | S_IWRITE);
				  for(k = 0; k < 1000; k++)
					  yaffs_write(h,fn,fnIndex,100);
				  yaffs_close(h);
			  }
			  else
			    	yaffs_mkdir(fn,fnIndex,0);
			}
			
			for(i = 0; i < 10; i++)
			{
			  sprintf(fn,fnIndex,"%s/%d",byteArrayToString(p, 0),i);  
			  if((i & 1) != 0) 
			  	yaffs_unlink(fn,fnIndex);
			  else
			  	yaffs_rmdir(fn,fnIndex);
			  
			}
					
			yaffs_unmount(StringToByteArray(mountpt),0);
		}
		
		
		

	}


	static void write_10k(int h)
	{
	   int i;
	   byte[] s = StringToByteArray("0123456789");
	   for(i = 0; i < 1000; i++)
	     yaffs_write(h,s,0,10);

	}
	static void write_200k_file(byte[] fn, int fnIndex, byte[] fdel, int fdelIndex, byte[] fdel1, int fdel1Index)
	{
	   int h1;
	   int i;
	   int offs;
	   
	   h1 = yaffs_open(fn, fnIndex, O_CREAT | O_TRUNC | O_RDWR, S_IREAD | S_IWRITE);
	   
	   for(i = 0; i < 100000; i+= 10000)
	   {
	   	write_10k(h1);
	   }
	   
	   offs = yaffs_lseek(h1,0,SEEK_CUR);
	   if( offs != 100000)
	   {
	   	printf("Could not write file\n");
	   }
	   
	   yaffs_unlink(fdel,fdelIndex);
	   for(i = 0; i < 100000; i+= 10000)
	   {
	   	write_10k(h1);
	   }
	   
	   offs = yaffs_lseek(h1,0,SEEK_CUR);
	   if( offs != 200000)
	   {
	   	printf("Could not write file\n");
	   }
	   
	   yaffs_close(h1);
	   yaffs_unlink(fdel1,fdelIndex);
	   
	}


	static void verify_200k_file(byte[] fn, int fnIndex)
	{
	   int h1;
	   int i;
	   byte[] x = new byte[11]; final int xIndex = 0;
	   byte[] s=StringToByteArray("0123456789"); final int sIndex = 0;
	   int errCount = 0;
	   
	   h1 = yaffs_open(fn, fnIndex, O_RDONLY, 0);
	   
	   for(i = 0; i < 200000 && errCount < 10; i+= 10)
	   {
	   	yaffs_read(h1,x,xxIndex,10);
		if(strncmp(x,xIndex,s,sIndex,10) != 0)
		{
			printf("File %s verification failed at %d\n",byteArrayToString(fn, fnIndex),i);
			errCount++;
		}
	   }
	   if(errCount >= 10)
	   	printf("Too many errors... aborted\n");
	      
	   yaffs_close(h1);	   
		
	}

	
	static void check_resize_gc_bug(String mountpt)
	{

		byte[] a = new byte[30]; final int aIndex = 0;
		byte[] b = new byte[30]; final int bIndex = 0;
		byte[] c = new byte[30]; final int cIndex = 0;
		byte[] tmp = new byte[30]; final int tmpIndex = 0;
		
		int i;
		
		sprintf(a,aIndex,"%s/a",mountpt);
		sprintf(b,bIndex,"%s/b",mountpt);
		sprintf(c,cIndex,"%s/c",mountpt);
		memset(tmp,tmpIndex,(byte)0,30);
	
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		yaffs_mount(StringToByteArray(mountpt),0);
		yaffs_unlink(a,aIndex);
		yaffs_unlink(b,bIndex);
		
		for(i = 0; i < 50; i++)
		{  
		   printf("A\n");write_200k_file/*(a,"",c)*/(a,aIndex,tmp,tmpIndex,c,cIndex);		// TODO verify
		   printf("B\n");verify_200k_file(a,aIndex);
		   printf("C\n");write_200k_file(b,bIndex,a,aIndex,c,cIndex);
		   printf("D\n");verify_200k_file(b,bIndex);
		   yaffs_unmount(StringToByteArray(mountpt),0);
		   yaffs_mount(StringToByteArray(mountpt),0);
		   printf("E\n");verify_200k_file(a,aIndex);
		   printf("F\n");verify_200k_file(b,bIndex);
		}
			
	}


	static void multi_mount_test(String mountpt,int nmounts)
	{

		byte[] a = new byte[30]; final int aIndex = 0;
		byte[] b = new byte[30]; final int bIndex = 0;
		byte[] c = new byte[30]; final int cIndex = 0;
		byte[] xx = new byte[1000]; final int xxIndex = 0;
		
		int i;
		int j;
		
		sprintf(a,aIndex,"%s/a",mountpt);

		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		for(i = 0; i < nmounts; i++){
			int h0;
			int h1;
			int len0;
			int len1;
			
			//static char xx[1000];
			
			printf("############### Iteration %d   Start\n",i);
			if(true || i == 0 || i == 5) 
				yaffs_mount(StringToByteArray(mountpt),0);

			dump_directory_tree(StringToByteArray(mountpt),0);
			
			
			yaffs_mkdir(a,aIndex,0);
			
			sprintf(xx,xxIndex,"%s/0",byteArrayToString(a, aIndex));
			h0 = yaffs_open(xx, xxIndex,O_RDWR | O_CREAT | O_TRUNC, S_IREAD | S_IWRITE);
			
			sprintf(xx,xxIndex,"%s/1",byteArrayToString(a, aIndex));
			h1 = yaffs_open(xx, xxIndex, O_RDWR | O_CREAT | O_TRUNC, S_IREAD | S_IWRITE);
			
			for(j = 0; j < 200; j++){
			   yaffs_write(h0,xx,xxIndex,1000);
			   yaffs_write(h1,xx,xxIndex,1000);
			}
			
			len0 = yaffs_lseek(h0,0,SEEK_END);
			len1 = yaffs_lseek(h1,0,SEEK_END);
			
			yaffs_lseek(h0,0,SEEK_SET);
			yaffs_lseek(h1,0,SEEK_SET);

			for(j = 0; j < 200; j++){
			   yaffs_read(h0,xx,xxIndex,1000);
			   yaffs_read(h1,xx,xxIndex,1000);
			}
			
			
			yaffs_truncate(h0,0);
			yaffs_close(h0);
			yaffs_close(h1);
			
			printf("########### %d\n",i);
			dump_directory_tree(StringToByteArray(mountpt),0);

			if(true || i == 4 || i == nmounts -1)
				yaffs_unmount(StringToByteArray(mountpt),0);
		}
	}


	static void small_mount_test(String mountpt,int nmounts)
	{

		byte[] a = new byte[30]; final int aIndex = 0;
		byte[] b = new byte[30]; final int bIndex = 0;
		byte[] c = new byte[30]; final int cIndex = 0;
		
		int i;
		int j;

		int h0;
		int h1;
		int len0;
		int len1;
		int nread;
		
		sprintf(a,aIndex,"%s/a",mountpt);

		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		byte[] xx = new byte[1000]; final int xxIndex = 0;

		for(i = 0; i < nmounts; i++){
			
			//static char xx[1000];
			
			
			printf("############### Iteration %d   Start\n",i);
			if(true || i == 0 || i == 5) 
				yaffs_mount(StringToByteArray(mountpt),0);

			dump_directory_tree(StringToByteArray(mountpt),0);
			
			yaffs_mkdir(a,aIndex,0);
			
			sprintf(xx,xxIndex,"%s/0",byteArrayToString(a, aIndex));
			if(i ==0){
			
				h0 = yaffs_open(xx, xxIndex, O_RDWR | O_CREAT | O_TRUNC, S_IREAD | S_IWRITE);
				for(j = 0; j < 130; j++)
					yaffs_write(h0,xx,xxIndex,1000);
				yaffs_close(h0);
			}
			
			h0 = yaffs_open(xx,xxIndex,O_RDONLY,0);
			
			sprintf(xx,xxIndex,"%s/1",byteArrayToString(a, aIndex));
			h1 = yaffs_open(xx, xxIndex, O_RDWR | O_CREAT | O_TRUNC, S_IREAD | S_IWRITE);
			
			while((nread = yaffs_read(h0,xx,xxIndex,1000)) > 0)
				yaffs_write(h1,xx,xxIndex,nread);
			
			
			len0 = yaffs_lseek(h0,0,SEEK_END);
			len1 = yaffs_lseek(h1,0,SEEK_END);
			
			yaffs_lseek(h0,0,SEEK_SET);
			yaffs_lseek(h1,0,SEEK_SET);

			for(j = 0; j < 200; j++){
			   yaffs_read(h0,xx,xxIndex,1000);
			   yaffs_read(h1,xx,xxIndex,1000);
			}
			
			yaffs_close(h0);
			yaffs_close(h1);
			
			printf("########### %d\n",i);
			dump_directory_tree(StringToByteArray(mountpt),0);

			if(true || i == 4 || i == nmounts -1)
				yaffs_unmount(StringToByteArray(mountpt),0);
		}
	}


	static boolean early_exit;

	static byte[] _STATIC_LOCAL_small_overwrite_test_xx = new byte[8000];
	
	static void small_overwrite_test(String mountpt,int nmounts)
	{

		byte[] a = new byte[30]; final int aIndex = 0;
		byte[] b = new byte[30]; final int bIndex = 0;
		byte[] c = new byte[30]; final int cIndex = 0;
		
		int i;
		int j;

		int h0;
		int h1;
		int len0;
		int len1;
		int nread;
		
		sprintf(a,aIndex,"%s/a",mountpt);

		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		
		
		for(i = 0; i < nmounts; i++){
			
//			static char xx[8000];
			
			printf("############### Iteration %d   Start\n",i);
			if(true)
				yaffs_mount(StringToByteArray(mountpt), 0);

			dump_directory_tree(StringToByteArray(mountpt), 0);
			
			yaffs_mkdir(a, aIndex,0);
			
			sprintf(_STATIC_LOCAL_small_overwrite_test_xx,0,"%a/0",a,aIndex);
			h0 = yaffs_open(_STATIC_LOCAL_small_overwrite_test_xx,0, O_RDWR | O_CREAT | O_TRUNC, S_IREAD | S_IWRITE);
			sprintf(_STATIC_LOCAL_small_overwrite_test_xx,0,"%a/1",a,aIndex);
			h1 = yaffs_open(_STATIC_LOCAL_small_overwrite_test_xx,0, O_RDWR | O_CREAT | O_TRUNC, S_IREAD | S_IWRITE);
			
			for(j = 0; j < 1000000; j+=1000){
				yaffs_truncate(h0,j);
				yaffs_lseek(h0,j,SEEK_SET);
				yaffs_write(h0,_STATIC_LOCAL_small_overwrite_test_xx,0,7000);
				yaffs_write(h1,_STATIC_LOCAL_small_overwrite_test_xx,0,7000);
				
				if(early_exit)
					System.exit(0);
			}
			
			yaffs_close(h0);
			
			printf("########### %d\n",i);
			dump_directory_tree(StringToByteArray(mountpt), 0);

			if(true)
				yaffs_unmount(StringToByteArray(mountpt), 0);
		}
	}


	static void yaffs_touch(byte[] fn, int fnIndex)
	{
		yaffs_chmod(fn, fnIndex, S_IREAD | S_IWRITE);
	}
	
// TODO needs testing
	static void checkpoint_fill_test(String mountpt, int nmounts)
	{

		byte[] a = new byte[50]; final int aIndex = 0;
		byte[] b = new byte[50]; final int bIndex = 0;
		byte[] c = new byte[50]; final int cIndex = 0;
		
		byte[] tmp = StringToByteArray("test Data");
		
		memcpy(c, cIndex, tmp, 0, tmp.length);
		
		
		
		int i;
		int j;
		int h;
		
		sprintf(a,aIndex,"%s/a",mountpt);
		
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		
		for(i = 0; i < nmounts; i++){
			printf("############### Iteration %d   Start\n",i);
			yaffs_mount(StringToByteArray(mountpt), 0);
			dump_directory_tree(StringToByteArray(mountpt), 0);
			yaffs_mkdir(a,aIndex,0);
			
			sprintf(b,bIndex,"%a/zz",a, aIndex);
			
			h = yaffs_open(b,bIndex,O_CREAT | O_RDWR,S_IREAD |S_IWRITE);
			
			while(yaffs_write(h,c,cIndex,50) == 50){}
			
			yaffs_close(h);
			
			for(j = 0; j < 2; j++){
				printf("touch %d\n",j);
				yaffs_touch(b,bIndex);
				yaffs_unmount(StringToByteArray(mountpt), 0);
				yaffs_mount(StringToByteArray(mountpt), 0);
			}

			dump_directory_tree(StringToByteArray(mountpt), 0);
			yaffs_unmount(StringToByteArray(mountpt), 0);
		}
	}

	static void make_file2(byte[] name1, int name1Index, byte[] name2, int name2Index, int syz)
	{

		byte[] xx = new byte[2500]; final int xxIndex = 0;
		byte[] tmp = StringToByteArray("abcdefghijklmnopqrstuvwxyz"); 
		memcpy(xx,xxIndex,tmp,0,tmp.length);
		int i;
		int h1=-1,h2=-1;
		int n = 1;


		if(name1 != null)
			h1 = yaffs_open(name1,name1Index,O_CREAT | O_TRUNC | O_RDWR, S_IREAD | S_IWRITE);
		if(name2 != null)
			h2 = yaffs_open(name2,name2Index,O_CREAT | O_TRUNC | O_RDWR, S_IREAD | S_IWRITE);
		
		while(syz > 0 && n > 0){
			i = (syz > 2500) ? 2500 : syz;
			n = yaffs_write(h1,xx,xxIndex,i);
			n = yaffs_write(h2,xx,xxIndex,i);
			syz -= 500;
		}
		yaffs_close(h1);
		yaffs_close(h2);
		
	}
//
//
//	extern void SetCheckpointReservedBlocks(int n);

	static void checkpoint_upgrade_test(String mountpt,int nmounts)
	{

		byte[] a = new byte[50]; final int aIndex = 0;
		byte[] b = new byte[50]; final int bIndex = 0;
		byte[] c = new byte[50]; final int cIndex = 0;
		byte[] d = new byte[50]; final int dIndex = 0;
		
		int i;
		int j;
		int h;
		
		sprintf(a,aIndex,"%s/a",mountpt);

		printf("Create start condition\n");
		yaffs2.utils.Globals.configuration.yaffs_StartUp();
		yaffs2.utils.Globals.configuration.SetCheckpointReservedBlocks(0);
		yaffs_mount(StringToByteArray(mountpt),0);
		yaffs_mkdir(a,aIndex,0);
		sprintf(b,bIndex,"%s/zz",byteArrayToString(a, aIndex));
		sprintf(c,cIndex,"%s/xx",byteArrayToString(a, aIndex));
		make_file2(b,bIndex,c,cIndex,2000000);
		sprintf(d,dIndex,"%s/aa",byteArrayToString(a, aIndex));
		make_file2(d,dIndex,null,0,500000000);
		dump_directory_tree(StringToByteArray(mountpt),0);
		
		printf("Umount/mount attempt full\n");
		yaffs_unmount(StringToByteArray(mountpt),0);
		
		yaffs2.utils.Globals.configuration.SetCheckpointReservedBlocks(10);
		yaffs_mount(StringToByteArray(mountpt),0);
		
		printf("unlink small file\n");
		yaffs_unlink(c,cIndex);
		dump_directory_tree(StringToByteArray(mountpt),0);
			
		printf("Umount/mount attempt\n");
		yaffs_unmount(StringToByteArray(mountpt),0);
		yaffs_mount(StringToByteArray(mountpt),0);
		
		for(j = 0; j < 500; j++){
			printf("***** touch %d\n",j);
			dump_directory_tree(StringToByteArray(mountpt),0);
			yaffs_touch(b,bIndex);
			yaffs_unmount(StringToByteArray(mountpt),0);
			yaffs_mount(StringToByteArray(mountpt),0);
		}

		for(j = 0; j < 500; j++){
			printf("***** touch %d\n",j);
			dump_directory_tree(StringToByteArray(mountpt),0);
			yaffs_touch(b,bIndex);
			yaffs_unmount(StringToByteArray(mountpt),0);
			yaffs_mount(StringToByteArray(mountpt),0);
		}
	}
	
	static void huge_array_test(String mountpt,int n)
	{

		byte[] a = new byte[50]; final int aIndex = 0;

		
		int i;
		int j;
		int h;
		
		int fnum;
		
		sprintf(a,aIndex,"mount point %s",mountpt);
		

		
		yaffs2.utils.Globals.configuration.yaffs_StartUp();

		yaffs_mount(StringToByteArray(mountpt),0);
		
		while(n>0){
			n--;
			fnum = 0;
			printf("\n\n START run\n\n");
			while(yaffs_freespace(StringToByteArray(mountpt),0) > 25000000){
				sprintf(a,aIndex,"%s/file%d",mountpt,fnum);
				fnum++;
				printf("create file %s\n",a);
				create_file_of_size(a,aIndex,10000000);
				printf("verifying file %s\n",a);
				verify_file_of_size(a,aIndex,10000000);
			}
			
			printf("\n\n verification/deletion\n\n");
			
			for(i = 0; i < fnum; i++){
				sprintf(a,aIndex,"%s/file%d",mountpt,i);
				printf("verifying file %s\n",a);
				verify_file_of_size(a,aIndex,10000000);
				printf("deleting file %s\n",a);
				yaffs_unlink(a,aIndex);
			}
			printf("\n\n done \n\n");
				
			   
		}
	}
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		yaffs2.utils.Globals.startUp(
				new yaffs2.platform.emulation.yaffscfg2k_C(),
				new yaffs2.platform.emulation.PortConfiguration(),
				new yaffs2.platform.emulation.DebugConfiguration());
		
// PASSED:
		
		//cache_read_test();
		//scan_pattern_test(StringToByteArray("/flash/boot"),0,10000,10);
		//yaffs_backward_scan_test(StringToByteArray("/flash/boot"),0);
		//scan_pattern_test(StringToByteArray("/flash/boot"),0,10000,100);
		//yaffs_device_flush_test(StringToByteArray("/flash/boot"),0);
		//short_scan_test(StringToByteArray("/flash/boot"),0,40000,10);
		//short_scan_test(StringToByteArray("/flash/boot"),0,40000,20);
		//short_scan_test(StringToByteArray("/flash/boot"),0,40000,100);
		//small_overwrite_test("/flash/boot",100);
		//small_overwrite_test("/flash/boot",10);
		//checkpoint_fill_test("/flash/boot",10);
		//long_test();		
		//resize_stress_test("/flash/boot");
		//resize_stress_test_no_grow_complex("/flash/boot",1);
		//resize_stress_test_no_grow_complex("/flash/boot",20);
		//huge_array_test("/flash/boot",1);
		//huge_array_test("/flash/boot",10);
		//checkpoint_upgrade_test("/flash/boot",1);
		//checkpoint_upgrade_test("/flash/boot",10);
		//small_mount_test("/flash/boot",1);
		//small_mount_test("/flash/boot",1000);
		//multi_mount_test("/flash/boot",10);
		//rename_over_test("/flash/boot");
		//truncate_test();
		//huge_directory_test_on_path("/flash/boot");
		//scan_deleted_files_test("/flash/boot");
		//check_resize_gc_bug("/flash/boot");
		//lookup_test("/flash/boot");
		//simple_rw_test("/flash/boot");
		//free_space_check();
		//freespace_test("/flash/boot");
		//link_test("/flash/boot");
		//fill_disk_test("/flash/boot");
		
// MAY NEED RETESTING:
		
		//small_overwrite_test("/flash/boot",1000);		
		//(should be fine, takes about half a day on my windows)		
		
// TO DO:
		  
// NOT DONE:	

		// cache_bypass_bug_test();
		// (i suppose its not worth translating)		 
		 
		 //return 0;
	}

}