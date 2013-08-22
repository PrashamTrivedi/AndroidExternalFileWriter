AndroidExternalFileWriter
=========================

A helper class to write files in external android storage, along with it's demo application.

Class is separately located at https://gist.github.com/PrashamTrivedi/6121924

How does it work
=========================
1. Create AppExternalFileWriter object with passing context to it.

2. use writeDataToFile or writeDataToTimeStampedFile variants as per your wish.

3. If you want to write a data where a file name should be a time stamp use writeDataToTimeStampedFile variants.

4. If you want to create a subfolder use suitable createSubDirectory variants.

5. If anything is wrong with external storage, like storage not mounted, corrupt,  shared as mass storage, not enough space available, or even trying to create a library already created. The class will throw ExternalFileWriterException with the message stating what happened.

6. If you want to write a data in external cache memory do following.
    * Check the variants of all the methods where it asks for a boolean variable, if you pass true the file operation is done in external cache , otherwise it will be done in normal external memory.
    * If have already created a directory in cache memory get it from createDirectory method, and pass this directory to any method where a parent is required. These methods work same regardless of parent is in external memory or in cache memory.

Description of Variants
=========================

1. writeDataToFile - Without parent directories
```java
writeDataToFile(String fileName, byte[] data,boolean inCache);
writeDataToFile(String fileName, String data,boolean inCache);
```
Writes data to desired file in Application directory.
          
2. writeDataToFile - With parent directories
```java
writeDataToFile(File parent, String fileName, byte[] data);
writeDataToFile(File parent, String fileName, String data);
```
Writes data to desired file in other directory.
          
3. writeDataToTimeStampedFile variants - Without parent directories
```java
writeDataToTimeStampedFile(String extension, byte[] data,boolean inCache)
writeDataToTimeStampedFile(String extension, String data,boolean inCache)
```
Writes data to desired file with timestamp with extension in Application directory.
          
4. writeDataToTimeStampedFile variants - With parent directories
```java
writeDataToTimeStampedFile(String extension, byte[] data)
writeDataToTimeStampedFile(String extension, String data)
```
Writes data to desired file with timestamp with extension in other directory.

5. createSubDirectory variants
```java
createSubDirectory(File parent, String directoryName)
```
Creates subdirectory in any other directory
```java
createSubDirectory(String directoryName,boolean inCache)
```
Creates subdirectory in application directory
          
Some goodies
=========================

1. ```getAppDirectory()``` : File object of created app directory

2. ```getExternalStorageDirectory()``` : File object of external storage directory

3. ```getExternalCacheDirectory()``` : File object of external cache directory

Note for Android Studio users
======================
1. This is for developers using eclipse, if you are using android studio, the master branch is for you.
