AndroidExternalFileWriter
=========================

[ ![Download](https://api.bintray.com/packages/prashamtrivedi/maven/AndroidExternalFileWriter/images/download.svg) ](https://bintray.com/prashamtrivedi/maven/AndroidExternalFileWriter/_latestVersion)

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-AndroidExternalFileWriter-blue.svg?style=flat)](http://android-arsenal.com/details/1/1796)

A helper class to write files in external android storage, along with it's demo application.

Class is separately located at https://gist.github.com/PrashamTrivedi/6121924

Maven Availibility
==================
This library is available in JCenter. In your gradle file enter
```groovy
compile 'com.creativeelites:AndroidExternalFileWriter:1.2'
```
How does it work
=========================
1. Create AppExternalFileWriter object with passing context to it.

2. Use writeDataToFile or writeDataToTimeStampedFile variants as per your wish.

3. If you want to write a data where a file name should be a time stamp use writeDataToTimeStampedFile variants.

4. If you want to create a subdirectory use suitable createSubDirectory variants.

5. If anything is wrong with external storage, like storage not mounted, corrupt,  shared as mass storage, not enough space available, or even trying to create a library already created. The class will throw ExternalFileWriterException with the message stating what happened.

6. If you want to write a data in external cache memory do following.
    * Check the variants of all the methods where it asks for a boolean variable, if you pass true the file operation is done in external cache , otherwise it will be done in normal external memory.
    * If have already created a directory in cache memory get it from createDirectory method, and pass this directory to any method where a parent is required. These methods work same regardless of parent is in external memory or in cache memory.

7. Checks whether certain directory or file exists on certain location or not with help of isFileExists or isDirectoryExists variants

8. Deletes entire directory with deleteDirectory method
 (Note : This method only cares about removing entire directory with its subcontents, if you want to check whether directory is empty or not and use some error message, I recommend to use File.delete() method.)

Description of Variants
=========================

- writeDataToFile Variants 
	- Without parent directories
	```java
	writeDataToFile(String fileName, byte[] data,boolean inCache);
	writeDataToFile(String fileName, String data,boolean inCache);
	```         
	- With parent directories
	```java
	writeDataToFile(File parent, String fileName, byte[] data);
	writeDataToFile(File parent, String fileName, String data);
	```

          
- writeDataToTimeStampedFile variants 
	- Without parent directories
	```java
	writeDataToTimeStampedFile(String extension, byte[] data,boolean inCache)
	writeDataToTimeStampedFile(String extension, String data,boolean inCache)
	```
	- With parent directories
	```java
	writeDataToTimeStampedFile(String extension, byte[] data)
	writeDataToTimeStampedFile(String extension, String data)
	```

- createSubDirectory variants

	- Creates subdirectory in any other directory
	 ```java
		createSubDirectory(File parent, String directoryName)
	```
	- Creates subdirectory in application directory.
	```java
		createSubDirectory(String directoryName,boolean inCache)
	```


- isDirectoryExists variants

	- Checks whether directory with given name exists in AppDirectory Or Cache directory
	```java
	isDirectoryExists(String directoryName, boolean checkInCache)
	```
	- Checks whether directory with given name exists in parentDirectory or not.
	```java
	isDirectoryExists(String directoryName, File parentDirectory)
	```

- isFileExists variants
	- Checks whether file with given name exists in AppDirectory
	```java
	isFileExists(String fileName, boolean checkInCache)
	```
	- Check whether directory with given name exists in parentDirectory or not.
	```java
	isFileExists(String fileName, File parentDirectory)
	```
- Delete Directory
	- Deletes given directory with all its subdirectories and its files.
	```java
	deleteDirectory(File directory)
	```


Some goodies
=========================

1. ```getAppDirectory()``` : File object of created app directory

2. ```getExternalStorageDirectory()``` : File object of external storage directory

3. ```getExternalCacheDirectory()``` : File object of external cache directory

# License
	   Copyright 2015 Prasham Trivedi
	   Licensed under the Apache License, Version 2.0 (the "License");
	   you may not use this file except in compliance with the License.
	   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

	   Unless required by applicable law or agreed to in writing, software
	   distributed under the License is distributed on an "AS IS" BASIS,
	   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	   See the License for the specific language governing permissions and
	   limitations under the License.
