package com.celites.androidexternalfilewriter_kotlin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.provider.DocumentFile
import android.text.TextUtils
import com.celites.kutils.isEmptyString
import com.celites.kutils.remove
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by Prasham on 4/11/2016.
 */

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class KotlinStorageAccessFileWriter(private val requestCode: Int) {


	public val PARENT_URI_KEY = "APP_EXTERNAL_PARENT_FILE_URI"
	var activity: Activity? = null
		set(value) {
			value?.let {
				field = value
				context = value
				initProcessWithActivity(requestCode, value)
			}

		}
	var fragment: Fragment? = null
		set(value) {
			value?.let {
				field = value
				context = value.context as Context
				initProcessWithFragment(requestCode, value)
			}

		}


	public fun startWithContext(context: Context) {
		this.context = context
		val isExternaDirAvailable = isExternalDirAvailable()
		if (isExternaDirAvailable) {
			createAppDirectory()
		}
	}

	lateinit var context: Context
	lateinit var appCacheDirectory: DocumentFile
	lateinit var appDirectory: DocumentFile
	lateinit var externalCacheDirectory: DocumentFile
	lateinit var externalParentFile: DocumentFile
	lateinit var preferences: SharedPreferences
	private val canNotCreateDirectory = "Can not create directory: "
	private val canNotWriteFile = "Can not write file: "


	@RequiresApi(Build.VERSION_CODES.LOLLIPOP) private fun initProcessWithActivity(requestCode: Int,
																				   activity: Activity) {
		initCacheDirs()
		preferences = PreferenceManager.getDefaultSharedPreferences(context)
		val isExternaDirAvailable = isExternalDirAvailable()
		if (!isExternaDirAvailable) {

			val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
			activity.startActivityForResult(intent, requestCode)
		}
	}

	@RequiresApi(Build.VERSION_CODES.LOLLIPOP) private fun initProcessWithFragment(requestCode: Int,
																				   fragment: Fragment) {
		initCacheDirs()
		preferences = PreferenceManager.getDefaultSharedPreferences(context)
		val isExternaDirAvailable = isExternalDirAvailable()
		if (!isExternaDirAvailable) {
			val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
			fragment.startActivityForResult(intent, requestCode)
		}
	}


	fun isExternalDirAvailable(context: Context = this.context): Boolean {
		initCacheDirs(context)
		preferences = PreferenceManager.getDefaultSharedPreferences(context)
		val externalDirUrl = preferences.getString(PARENT_URI_KEY, "")
		val isExternalDirEmpty = externalDirUrl.isEmptyString()
		if (!isExternalDirEmpty) {
			externalParentFile = DocumentFile.fromTreeUri(context, Uri.parse(externalDirUrl))
			try {
				createAppDirectory(context)
			} catch (e: Exception) {
				preferences.remove(PARENT_URI_KEY)
				return false
			}
		}
		return !isExternalDirEmpty
	}


	private fun initCacheDirs(context: Context = this.context) {
		val dirs = ContextCompat.getExternalCacheDirs(context)
		externalCacheDirectory = if (dirs.size > 1) {
			val dir = dirs[1]
			if (dir != null) {
				DocumentFile.fromFile(dir)
			} else {
				DocumentFile.fromFile(dirs[0])
			}
		} else {
			DocumentFile.fromFile(dirs[0])
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
		return if (isDirectoryExists(displayName, parentDirectory)) {

			parentDirectory.createDirectory(displayName)
		} else {
			parentDirectory.findFile(displayName)
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

	@RequiresApi(Build.VERSION_CODES.KITKAT)
	fun hasPermissions(file: DocumentFile): Boolean {
		val persistedUriPermissions = context.getContentResolver().persistedUriPermissions
		val filterForPermission = persistedUriPermissions.filter { it.uri == file.uri && it.isReadPermission && it.isWritePermission }
		return filterForPermission.isNotEmpty()
	}

	/** Creates app directory  */
	private fun createAppDirectory(context: Context = this.context) {
		val directoryName = context.getString(context.applicationInfo.labelRes)
		if (isDirectoryExists(directoryName, externalParentFile)) {
			appDirectory = externalParentFile.findFile(directoryName)
		} else {
			appDirectory = externalParentFile.createDirectory(directoryName)
		}
		if (isDirectoryExists(directoryName, externalCacheDirectory)) {
			appCacheDirectory = externalCacheDirectory.findFile(directoryName)
		} else {
			appCacheDirectory = externalCacheDirectory.createDirectory(directoryName)
		}

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
	fun createSubdirectory(directoryName: String, inCache: Boolean = false): DocumentFile? {
		getAppDirectory()
		val appDirectory = getAppDirectory(inCache)
		return if (!isDirectoryExists(directoryName, inCache)) {

			appDirectory.createDirectory(directoryName)
		} else {
			appDirectory.findFile(directoryName)
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
		return appDirectory.findFile(displayName)
	}

	fun handleResult(requestCode: Int, resultCode: Int, data: Intent?,
					 handlingFinished: () -> Unit = {},
					 askForPersistableUriPermission: Boolean = true) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == this.requestCode) {
				data?.let {
					val treeUri = it.data
					if (askForPersistableUriPermission) {
						val takeFlags = it.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
						context.contentResolver.takePersistableUriPermission(treeUri, takeFlags)
					}
					externalParentFile = DocumentFile.fromTreeUri(context, treeUri)
					preferences.edit().putString(PARENT_URI_KEY,
							externalParentFile.getUri().toString()).apply()
					createAppDirectory()
					handlingFinished()
				}


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
	fun writeDataToFile(fileName: String, mimeType: String, data: ByteArray,
						inCache: Boolean = false) {
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
	fun writeDataToFile(parent: DocumentFile, fileName: String, data: ByteArray, mimeType: String,
						onFileWritten: (DocumentFile) -> Unit = {}) {
		val file = createFile(fileName, parent, mimeType)
		writeDataToFile(file = file, data = data, onFileWritten = onFileWritten)
	}

	private fun createFile(fileName: String, parent: DocumentFile,
						   mimeType: String) = if (!isFileExists(fileName, parent)) {

		parent.createFile(mimeType, fileName)
	} else {
		parent.findFile(fileName)
	}

	/**
	 * Write byte array to file. Will show error if given file is a directory.

	 * @param file
	 * * 		: File where data is to be written.
	 * *
	 * @param data
	 * * 		byte array which you want to write a file. If size of this is greater than size available, it will show error.
	 */
	@Throws(FileNotFoundException::class) private fun writeDataToFile(file: DocumentFile,
																	  data: ByteArray,
																	  onFileWritten: (DocumentFile) -> Unit = {}) {
		val fileDescriptor = context.contentResolver.openFileDescriptor(file.uri, "w")
		val out: FileOutputStream?
		if (fileDescriptor != null) {
			out = FileOutputStream(fileDescriptor.fileDescriptor)
			try {
				out.write(data)
				out.close()
				onFileWritten(file)
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
	fun writeDataToFile(fileName: String, mimeType: String, data: String, inCache: Boolean,
						onFileWritten: (DocumentFile) -> Unit = {}) {
		val appDir = getAppDirectory(inCache)
		writeDataToFile(parent = appDir, fileName = fileName, data = data, mimeType = mimeType,
				onFileWritten = onFileWritten)
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
	fun writeDataToFile(parent: DocumentFile, fileName: String, data: String, mimeType: String,
						onFileWritten: (DocumentFile) -> Unit = {}) {
		val file = createFile(fileName, parent, mimeType)
		writeDataToFile(file = file, data = data, onFileWritten = onFileWritten)
	}

	/**
	 * Write byte array to file. Will show error if given file is a directory.

	 * @param file
	 * * 		: File where data is to be written.
	 * *
	 * @param data
	 * * 		String which you want to write a file. If size of this is greater than size available, it will show error.
	 */
	@Throws(FileNotFoundException::class) private fun writeDataToFile(file: DocumentFile,
																	  data: String,
																	  onFileWritten: (DocumentFile) -> Unit = {}) {
		val stringBuffer = data.toByteArray()
		writeDataToFile(file = file, data = stringBuffer, onFileWritten = onFileWritten)
	}

	@Throws(FileNotFoundException::class)
	fun writeDataToTimeStampedFile(mimeType: String, data: String, extension: String,
								   filePrefix: String = "", inCache: Boolean,
								   onFileWritten: (DocumentFile) -> Unit = {}) {
		val appDir = getAppDirectory(inCache)
		val fileExtension = if (TextUtils.isEmpty(extension)) "" else "." + extension
		val fileName = "$filePrefix${System.currentTimeMillis()}$fileExtension"
		writeDataToFile(parent = appDir, fileName = fileName, data = data, mimeType = mimeType,
				onFileWritten = onFileWritten)
	}

	@Throws(FileNotFoundException::class)
	fun writeDataToTimeStampedFile(mimeType: String, data: ByteArray, extension: String,
								   filePrefix: String = "", inCache: Boolean,
								   onFileWritten: (DocumentFile) -> Unit) {
		val appDir = getAppDirectory(inCache)
		val fileExtension = if (TextUtils.isEmpty(extension)) "" else "." + extension
		val fileName = "$filePrefix${System.currentTimeMillis()}$fileExtension"
		writeDataToFile(parent = appDir, fileName = fileName, data = data, mimeType = mimeType,
				onFileWritten = onFileWritten)
	}

	@Throws(FileNotFoundException::class)
	fun writeDataToTimeStampedFile(mimeType: String, data: String, extension: String,
								   filePrefix: String = "", inCache: Boolean, parent: DocumentFile,
								   onFileWritten: (DocumentFile) -> Unit = {}) {
		val fileExtension = if (TextUtils.isEmpty(extension)) "" else "." + extension
		val fileName = "$filePrefix${System.currentTimeMillis()}$fileExtension"
		writeDataToFile(parent = parent, fileName = fileName, data = data, mimeType = mimeType,
				onFileWritten = onFileWritten)
	}

	@Throws(FileNotFoundException::class)
	fun writeDataToTimeStampedFile(mimeType: String, data: ByteArray, extension: String,
								   filePrefix: String = "", inCache: Boolean, parent: DocumentFile,
								   onFileWritten: (DocumentFile) -> Unit = {}) {
		val fileExtension = if (TextUtils.isEmpty(extension)) "" else "." + extension
		val fileName = "${System.currentTimeMillis()}$fileExtension"
		writeDataToFile(parent = parent, fileName = fileName, data = data, mimeType = mimeType,
				onFileWritten = onFileWritten)
	}

	private fun createFile(fileName: String, inCache: Boolean, mimeType: String): DocumentFile {
		return createFile(fileName, getAppDirectory(inCache), mimeType)
	}

	public fun moveFile(file: DocumentFile, destinationDir: DocumentFile): Boolean {
		copyFile(destinationDir, file)
		return file.delete()
	}

	public fun KotlinStorageAccessFileWriter.copyFile(destinationDir: DocumentFile,
													  file: DocumentFile,
													  onFileWritten: (DocumentFile) -> Unit = {}) {
		val bytesFromFile = getBytesFromFile(file)
		writeDataToFile(parent = destinationDir, fileName = file.name, data = bytesFromFile,
				mimeType = file.type, onFileWritten = onFileWritten)
	}

	public fun getBytesFromFile(file: DocumentFile): ByteArray {
		val inputStream = context.contentResolver.openInputStream(file.uri)
		return inputStream.readBytes()
	}
}