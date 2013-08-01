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


Description of Variants
=========================

1. writeDataToFile - Without parents

  writeDataToFile(String fileName, byte[] data)
  writeDataToFile(String fileName, java.lang.String data)
          Writes data to desired file in Application directory.
          
2. writeDataToFile - With Parents

  writeDataToFile(File parent, String fileName, byte[] data)
  writeDataToFile(File parent, String fileName, String data)
          Writes data to desired file in other directory.
          
3. writeDataToTimeStampedFile variants - Without parents

  writeDataToTimeStampedFile(String extension, byte[] data)
  writeDataToTimeStampedFile(String extension, String data)
          Writes data to desired file with timestamp with extension in Application directory.
          
4. writeDataToTimeStampedFile variants - With parents

  writeDataToTimeStampedFile(java.lang.String extension, byte[] data)
  writeDataToTimeStampedFile(java.lang.String extension, java.lang.String data)
          Writes data to desired file with timestamp with extension in other directory.

5. createSubDirectory variants
  createSubDirectory(java.io.File parent, java.lang.String directoryName)
          Creates subdirectory in application directory
  createSubDirectory(java.lang.String directoryName)
          Creates subdirectory in any other directory
          
some other useful methods

1. getAppDirectory()
          get File object of created app directory
2. getExternalStorageDirectory()
          get File object of external storage directory
