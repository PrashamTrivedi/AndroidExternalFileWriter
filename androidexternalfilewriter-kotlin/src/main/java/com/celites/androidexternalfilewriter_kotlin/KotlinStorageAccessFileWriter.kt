package com.celites.androidexternalfilewriter_kotlin

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v4.provider.DocumentFile
import android.text.TextUtils
import com.celites.kutils.isEmptyString
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by Prasham on 4/11/2016.
 */
class KotlinStorageAccessFileWriter(activity: Activity, requestCode: Int) {

    private val PARENT_URI_KEY = "APP_EXTERNAL_PARENT_FILE_URI"
    lateinit var activity: Activity
    lateinit var appCacheDirectory: DocumentFile
    lateinit var appDirectory: DocumentFile
    lateinit var externalCacheDirectory: DocumentFile
    lateinit var externalParentFile: DocumentFile
    lateinit var preferences: SharedPreferences
    var requestCode: Int = 0
    private val canNotCreateDirectory = "Can not create directory: "
    private val canNotWriteFile = "Can not write file: "

    /**
     * Inits new SAFFileWriter object, it will first check whether we already have a parent directory with proper uri access or not.

     * @param activity:
     * * 		Activity for context and starting request for OPEN_DOCUMENT_TREE
     * *
     * @param requestCode:
     * * 		Request code to listen to OPEN_DOCUMENT_TREE
     */
    init {
        val dirs = ContextCompat.getExternalCacheDirs(activity)
        if (dirs.size > 1) {
            val dir = dirs[1]
            if (dir != null) {
                externalCacheDirectory = DocumentFile.fromFile(dir)
            } else {
                externalCacheDirectory = DocumentFile.fromFile(dirs[0])
            }
        } else {
            externalCacheDirectory = DocumentFile.fromFile(dirs[0])
        }
        preferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val externalDirUrl = preferences.getString(PARENT_URI_KEY, "")
        if (externalDirUrl.isEmptyString()) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            activity.startActivityForResult(intent, requestCode)
        }
    }


    /**
     * Creates subdirectory in parent directory

     * @param parentDirectory
     * * 		: Parent directory where directory with "directoryName" should be created
     * *
     * @param displayName
     * * 		name of subdirectory
     * *
     * *
     * @return File object of created subdirectory
     * *
     * *
     * @throws ExternalFileWriterException
     * * 		if external storage is not available
     */
    fun createSubDirectory(displayName: String, parentDirectory: DocumentFile): DocumentFile {
        getAppDirectory()
        if (isDirectoryExists(displayName, parentDirectory)) {

            return parentDirectory.createDirectory(displayName)
        } else {
            return parentDirectory.findFile(displayName)
        }
    }


    /**
     * Check whether directory with given name exists in parentDirectory or not.

     * @param directoryName
     * * 		: Name of the directory to check.
     * *
     * @param parentDirectory
     * * 		: Parent directory where directory with "directoryName" should be present
     * *
     * *
     * @return true if a directory with "directoryName" exists, false otherwise
     */
    fun isDirectoryExists(displayName: String, parentDirectory: DocumentFile): Boolean {
        val file = parentDirectory.findFile(displayName)
        return file != null && file.isDirectory
    }

    /** Creates app directory  */
    private fun createAppDirectory() {
        val directoryName = activity.getString(activity.applicationInfo.labelRes)
        appDirectory = externalParentFile?.createDirectory(directoryName)
        appCacheDirectory = externalCacheDirectory?.createDirectory(directoryName)
    }

    /**
     * Creates subdirectory in application directory

     * @param directoryName
     * * 		name of subdirectory
     * *
     * *
     * @return File object of created subdirectory
     * *
     * *
     * @throws ExternalFileWriterException
     * * 		if external storage is not available
     */
    fun createSubdirectory(directoryName: String, inCache: Boolean): DocumentFile? {
        getAppDirectory()
        val appDirectory = getAppDirectory(inCache)
        if (!isDirectoryExists(directoryName, inCache)) {

            return appDirectory?.createDirectory(directoryName)
        } else {
            return appDirectory?.findFile(directoryName)
        }
    }

    fun getAppDirectory(inCache: Boolean = false): DocumentFile {
        return if (inCache) appCacheDirectory else appDirectory
    }

    /**
     * Checks whether directory with given name exists in AppDirectory

     * @param directoryName
     * * 		: Name of the directory to check.
     * *
     * *
     * @return true if a directory with "directoryName" exists, false otherwise
     */
    fun isDirectoryExists(displayName: String, inCache: Boolean): Boolean {
        val file = getDocumentFile(displayName, inCache)
        return file != null && file.isDirectory
    }

    private fun getDocumentFile(displayName: String, inCache: Boolean): DocumentFile? {
        val appDirectory = getAppDirectory(inCache)
        return appDirectory?.findFile(displayName)
    }

    fun handleResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == this.requestCode) {
                val treeUri = data.data
                externalParentFile = DocumentFile.fromTreeUri(activity, treeUri)
                preferences.edit().putString(PARENT_URI_KEY, externalParentFile?.getUri().toString())
                getAppDirectory()

            }
        }
    }

    /**
     * Check whether file with given name exists in parentDirectory or not.

     * @param fileName
     * * 		: Name of the file to check.
     * *
     * @param parentDirectory
     * * 		: Parent directory where directory with "fileName" should be present
     * *
     * *
     * @return true if a file  with "fileName" exists, false otherwise
     */
    fun isFileExists(displayName: String, inCache: Boolean = false): Boolean {
        val file = getDocumentFile(displayName, inCache)
        return file != null && file.isFile
    }

    @Throws(FileNotFoundException::class)
    fun writeDataToFile(fileName: String, mimeType: String, data: ByteArray, inCache: Boolean = false) {
        getAppDirectory()
        val appDir = getAppDirectory(inCache)
        writeDataToFile(appDir, fileName, data, mimeType)
    }

    /**
     * Writes data to the file. The file will be created in the directory name same as app.

     * @param fileName
     * * 		name of the file
     * *
     * @param data
     * * 		data to write
     * *
     * *
     * @throws ExternalFileWriterException
     * * 		if external storage is not available or free space is less than size of the data
     */
    @Throws(FileNotFoundException::class)
    fun writeDataToFile(parent: DocumentFile, fileName: String, data: ByteArray, mimeType: String) {
        val file = createFile(fileName, parent, mimeType)
        writeDataToFile(file, data)
    }

    private fun createFile(fileName: String, parent: DocumentFile, mimeType: String): DocumentFile {
        if (!isFileExists(fileName, parent)) {

            return parent.createFile(mimeType, fileName)
        } else {
            return parent.findFile(fileName)
        }
    }

    /**
     * Write byte array to file. Will show error if given file is a directory.

     * @param file
     * * 		: File where data is to be written.
     * *
     * @param data
     * * 		byte array which you want to write a file. If size of this is greater than size available, it will show error.
     */
    @Throws(FileNotFoundException::class)
    private fun writeDataToFile(file: DocumentFile, data: ByteArray) {
        val fileDescriptor = activity.contentResolver.openFileDescriptor(file.uri, "w")
        var out: FileOutputStream? = null
        if (fileDescriptor != null) {
            out = FileOutputStream(fileDescriptor.fileDescriptor)
            try {
                out.write(data)
                out.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    /**
     * Checks whether file with given name exists in AppDirectory

     * @param fileName
     * * 		: Name of the file to check.
     * *
     * *
     * @return true if a file with "directoryName" exists, false otherwise
     */
    fun isFileExists(displayName: String, parentDirectory: DocumentFile): Boolean {
        val file = parentDirectory.findFile(displayName)
        return file != null && file.isFile
    }

    @Throws(FileNotFoundException::class)
    fun writeDataToFile(fileName: String, mimeType: String, data: String, inCache: Boolean) {
        val appDir = getAppDirectory(inCache)
        writeDataToFile(appDir, fileName, data, mimeType)
    }

    /**
     * Write data in file of a parent directory

     * @param parent
     * * 		parent directory
     * *
     * @param fileName
     * * 		desired filename
     * *
     * @param data
     * * 		data
     * *
     * *
     * @throws ExternalFileWriterException
     * * 		if external storage is not available or free space is less than size of the data
     */
    @Throws(FileNotFoundException::class)
    fun writeDataToFile(parent: DocumentFile, fileName: String, data: String, mimeType: String) {
        val file = createFile(fileName, parent, mimeType)
        writeDataToFile(file, data)
    }

    /**
     * Write byte array to file. Will show error if given file is a directory.

     * @param file
     * * 		: File where data is to be written.
     * *
     * @param data
     * * 		String which you want to write a file. If size of this is greater than size available, it will show error.
     */
    @Throws(FileNotFoundException::class)
    private fun writeDataToFile(file: DocumentFile, data: String) {
        val stringBuffer = data.toByteArray()
        writeDataToFile(file, stringBuffer)
    }

    @Throws(FileNotFoundException::class)
    fun writeDataToTimeStampedFile(mimeType: String, data: String, extension: String, inCache: Boolean) {
        val appDir = getAppDirectory(inCache)
        val fileExtension = if (TextUtils.isEmpty(extension)) "" else "." + extension
        val fileName = "${System.currentTimeMillis()}$fileExtension"
        writeDataToFile(appDir, fileName, data, mimeType)
    }

    @Throws(FileNotFoundException::class)
    fun writeDataToTimeStampedFile(mimeType: String, data: ByteArray, extension: String, inCache: Boolean) {
        val appDir = getAppDirectory(inCache)
        val fileExtension = if (TextUtils.isEmpty(extension)) "" else "." + extension
        val fileName = "${System.currentTimeMillis()}$fileExtension"
        writeDataToFile(appDir, fileName, data, mimeType)
    }

    @Throws(FileNotFoundException::class)
    fun writeDataToTimeStampedFile(mimeType: String, data: String, extension: String, inCache: Boolean, parent: DocumentFile) {
        val fileExtension = if (TextUtils.isEmpty(extension)) "" else "." + extension
        val fileName = "${System.currentTimeMillis()}$fileExtension"
        writeDataToFile(parent, fileName, data, mimeType)
    }

    @Throws(FileNotFoundException::class)
    fun writeDataToTimeStampedFile(mimeType: String, data: ByteArray, extension: String, inCache: Boolean, parent: DocumentFile) {
        val fileExtension = if (TextUtils.isEmpty(extension)) "" else "." + extension
        val fileName = "${System.currentTimeMillis()}$fileExtension"
        writeDataToFile(parent, fileName, data, mimeType)
    }

    private fun createFile(fileName: String, inCache: Boolean, mimeType: String): DocumentFile {
        return createFile(fileName, getAppDirectory(inCache), mimeType)
    }

}