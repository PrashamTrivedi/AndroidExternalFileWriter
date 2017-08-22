package com.celites.androidexternalfilewriter_kotlin

import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.text.TextUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by Prasham on 4/11/2016.
 */
public class KotlinExternalFileWriter {
	lateinit var appDirectory: File
	lateinit var appCacheDirectory: File


	/**
	 * Created by Prasham on 4/6/2016.
	 */
	public fun Context.createAppDirectory() {
		val directoryName = this.getString(this.getApplicationInfo().labelRes)

		if (isExternalStorageAvailable(false)) {

			appDirectory = File(Environment.getExternalStorageDirectory().toString(), directoryName)
			createDirectory(appDirectory)

			appCacheDirectory = File(this.externalCacheDir, directoryName)
			createDirectory(appCacheDirectory)

		}
	}

	private fun getAppDirectory(inCache: Boolean = false): File {
		return if (inCache) this.appCacheDirectory else this.appDirectory
	}

	public fun createFile(fileName: String, parent: File = appDirectory): File {
		if (isExternalStorageAvailable(true)) {

			try {

				if (parent.isDirectory) {

					val detailFile = File(parent, fileName)
					if (!detailFile.exists()) detailFile.createNewFile()
					else {
						val messege = "File already there "
						throwException(messege)
					}
					return detailFile
				} else {
					throwException("$parent  should be a directory")
				}
			} catch (e: IOException) {
				e.printStackTrace()
				val errorMessege = "IOException " + e
				throwException(errorMessege)
			} catch (e: Exception) {
				e.printStackTrace()
				val errorMessege = "Exception " + e
				throwException(errorMessege)
			}

		}
		return File(parent, fileName)
	}

	/**
	 * Creates subdirectory in parent directory

	 * @param parent
	 * * 		: Parent directory where directory with "directoryName" should be created
	 * *
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
	@Throws(ExternalFileWriterException::class)
	fun createSubDirectory(parent: File = appDirectory, directoryName: String): File? {
		if (isExternalStorageAvailable(false)) {

			getAppDirectory()

			if (!parent.isDirectory) throwException("$parent.name Must be a directory ")

			val subDirectory = File(parent, directoryName)

			return createDirectory(subDirectory)
		} else return null
	}

	/**
	 * Deletes given directory with all its subdirectories and its files.

	 * @param directory
	 * * 		: Directory to delete
	 */
	fun deleteDirectory(directory: File?) {
		if (directory != null) {
			if (directory.isDirectory) directory.listFiles().filterNotNull().forEach {
				if (it.isDirectory) deleteDirectory(it)
				else it.delete()
			}

			directory.delete()
		}
		//		return false;
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
	 * * 		if external storage is not available or free space is
	 * * 		less than size of the data
	 */
	@Throws(ExternalFileWriterException::class)
	fun writeDataToFile(parent: File = appDirectory, fileName: String, data: ByteArray,
						onFileWritten: (File?) -> Unit = {}) {
		if (isExternalStorageAvailable(true)) {
			getAppDirectory()

			val file = createFile(fileName, parent)

			writeDataToFile(file = file, data = data, onFileWritten = onFileWritten)
		}
	}

	/**
	 * Write byte array to file. Will show error if given file is a directory.

	 * @param file
	 * * 		: File where data is to be written.
	 * *
	 * @param data
	 * * 		String which you want to write a file. If size of this is
	 * * 		greater than size available, it will show error.
	 */
	@Throws(ExternalFileWriterException::class) private fun writeDataToFile(file: File,
																			data: String,
																			onFileWritten: (File?) -> Unit = {}) {
		val stringBuffer = data.toByteArray()
		writeDataToFile(file = file, data = stringBuffer, onFileWritten = onFileWritten)
	}

	private fun getAvailableSpace(): Double {
		val stat = StatFs(Environment.getExternalStorageDirectory().path)
		return stat.availableBlocks.toDouble() * stat.blockSize.toDouble()
	}


	/**
	 * Write byte array to file. Will show error if given file is a directory.

	 * @param file
	 * * 		: File where data is to be written.
	 * *
	 * @param data
	 * * 		byte array which you want to write a file. If size of this is
	 * * 		greater than size available, it will show error.
	 */
	@Throws(ExternalFileWriterException::class) private fun writeDataToFile(file: File?,
																			data: ByteArray?,
																			onFileWritten: (File?) -> Unit = {}) {
		if (isExternalStorageAvailable(true)) {
			if (file?.isDirectory == true) {
				throwException("$file is not a file, can not write data in it")
			} else {
				if (data != null) {
					val dataSize = data.size.toDouble()
					val remainingSize = getAvailableSpace()
					if (dataSize >= remainingSize) {
						throwException("Not enough size available")
					} else {
						try {
							val out = FileOutputStream(file)
							out.write(data)
							out.close()
							onFileWritten(file)
						} catch (e: IOException) {
							e.printStackTrace()
						} catch (e: Exception) {
							e.printStackTrace()
						}

					}
				}

			}
		}
	}


	/**
	 * Checks whether directory with given name exists in AppDirectory

	 * @param directoryName
	 * * 		: Name of the directory to check.
	 * *
	 * *
	 * @return true if a directory with "directoryName" exists, false otherwise
	 */
	fun isDirectoryExists(directoryName: String, checkInCache: Boolean): Boolean {
		val parentDirectory = if (checkInCache) appCacheDirectory else appDirectory
		return isDirectoryExists(directoryName, parentDirectory)
	}

	/**
	 * Writes data to the file. The file will be created in the directory name
	 * same as app.
	 *
	 *
	 * Name of the file will be the timestamp.extension
	 *

	 * @param extension
	 * * 		extension of the file, pass null if you don't want to have
	 * * 		extension.
	 * *
	 * @param data
	 * * 		data to write
	 * *
	 * @param inCache
	 * * 		Pass true if you want to write data in External Cache. false if you want to write data in external directory.
	 * *
	 * *
	 * @throws ExternalFileWriterException
	 * * 		if external storage is not available or free space is
	 * * 		less than size of the data
	 */
	@Throws(ExternalFileWriterException::class)
	fun writeDataToTimeStampedFile(extension: String, data: String, inCache: Boolean,
								   onFileWritten: (File?) -> Unit = {}) {
		if (isExternalStorageAvailable(true)) {
			getAppDirectory()

			val fileExtension = if (TextUtils.isEmpty(extension)) "" else "." + extension
			val fileName = "${System.currentTimeMillis()}$fileExtension"

			val file = createFile(fileName, getAppDirectory(inCache))

			writeDataToFile(file = file, data = data, onFileWritten = onFileWritten)
		}
	}

	/**
	 * Writes data to the file. The file will be created in the directory name
	 * same as app.
	 *
	 *
	 * Name of the file will be the timestamp.extension
	 *

	 * @param parent
	 * * 		parent directory path
	 * *
	 * @param extension
	 * * 		extension of the file, pass null if you don't want to have
	 * * 		extension.
	 * *
	 * @param data
	 * * 		data to write
	 * *
	 * *
	 * @throws ExternalFileWriterException
	 * * 		if external storage is not available or free space is
	 * * 		less than size of the data
	 */
	@Throws(ExternalFileWriterException::class)
	fun writeDataToTimeStampedFile(parent: File, extension: String, data: String,
								   onFileWritten: (File?) -> Unit = {}) {
		if (isExternalStorageAvailable(true)) {
			getAppDirectory()

			val fileExtension = if (TextUtils.isEmpty(extension)) "" else "." + extension
			val fileName = "${System.currentTimeMillis()}$fileExtension"

			val file = createFile(fileName, parent)

			writeDataToFile(file = file, data = data, onFileWritten = onFileWritten)
		}
	}

	/**
	 * Writes data to the file. The file will be created in the directory name
	 * same as app.
	 *
	 *
	 * Name of the file will be the timestamp.extension
	 *

	 * @param extension
	 * * 		extension of the file, pass null if you don't want to have
	 * * 		extension.
	 * *
	 * @param data
	 * * 		data to write
	 * *
	 * *
	 * @throws ExternalFileWriterException
	 * * 		if external storage is not available or free space is
	 * * 		less than size of the data
	 */
	@Throws(ExternalFileWriterException::class)
	fun writeDataToTimeStampedFile(extension: String, data: ByteArray, inCache: Boolean,
								   onFileWritten: (File?) -> Unit = {}) {
		if (isExternalStorageAvailable(true)) {
			getAppDirectory()

			val fileExtension = if (TextUtils.isEmpty(extension)) "" else "." + extension
			val fileName = "${System.currentTimeMillis()}$fileExtension"

			val file = createFile(fileName, getAppDirectory(inCache))

			writeDataToFile(file = file, data = data, onFileWritten = onFileWritten)
		}
	}

	/**
	 * Writes data to the file. The file will be created in the directory name
	 * same as app.
	 *
	 *
	 * Name of the file will be the timestamp.extension
	 *

	 * @param parent
	 * * 		parent directory path
	 * *
	 * @param extension
	 * * 		extension of the file, pass null if you don't want to have
	 * * 		extension.
	 * *
	 * @param data
	 * * 		data to write
	 * *
	 * *
	 * @throws ExternalFileWriterException
	 * * 		if external storage is not available or free space is
	 * * 		less than size of the data
	 */
	@Throws(ExternalFileWriterException::class)
	fun writeDataToTimeStampedFile(parent: File, extension: String, data: ByteArray,
								   onFileWritten: (File?) -> Unit = {}) {
		if (isExternalStorageAvailable(true)) {
			getAppDirectory()

			val fileExtension = if (TextUtils.isEmpty(extension)) "" else "." + extension
			val fileName = "${System.currentTimeMillis()}$fileExtension"

			val file = createFile(fileName, parent)

			writeDataToFile(file = file, data = data, onFileWritten = onFileWritten)
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
	fun isFileExists(fileName: String, parentDirectory: File): Boolean {
		val directoryToCheck = File(parentDirectory, fileName)
		return directoryToCheck.exists() && directoryToCheck.isFile
	}

	/**
	 * Checks whether file with given name exists in AppDirectory

	 * @param fileName
	 * * 		: Name of the file to check.
	 * *
	 * *
	 * @return true if a file with "directoryName" exists, false otherwise
	 */
	fun isFileExists(fileName: String, checkInCache: Boolean): Boolean {
		val parentDirectory = if (checkInCache) appCacheDirectory else appDirectory
		return isFileExists(fileName, parentDirectory)
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
	fun isDirectoryExists(directoryName: String, parentDirectory: File): Boolean {
		val directoryToCheck = File(parentDirectory, directoryName)
		return directoryToCheck.exists() && directoryToCheck.isDirectory
	}

	@Throws(ExternalFileWriterException::class) private fun createDirectory(directory: File): File {
		if (!directory.exists() || !directory.isDirectory) {
			if (directory.mkdirs()) {
				val messege = "directory $directory created : Path " + directory.path

			} else {
				if (directory.exists()) {
					if (directory.isDirectory) {
						val messege = "directory $directory Already exists : Path $directory.path"

					} else {
						val messege = "$directory should be a directory but found a file : Path $directory.path"
						throwException(messege)
					}

				}
			}
		}
		return directory
	}

	private val canNotWriteFile = "Can not write file: "
	private val canNotCreateDirectory = "Can not create directory: "
	@Throws(ExternalFileWriterException::class) private fun isExternalStorageAvailable(
			isForFile: Boolean): Boolean {
		val errorStarter = if (isForFile) canNotWriteFile else canNotCreateDirectory

		val storageState = Environment.getExternalStorageState()

		if (storageState == Environment.MEDIA_MOUNTED) {
			return true
		} else if (storageState == Environment.MEDIA_BAD_REMOVAL) {
			throwException(errorStarter + "Media was removed before it was unmounted.")
		} else if (storageState == Environment.MEDIA_CHECKING) {
			throwException(
					errorStarter + "Media is present and being disk-checked, " + "Please wait and try after some time")
		} else if (storageState == Environment.MEDIA_MOUNTED_READ_ONLY) {
			throwException(errorStarter + "Presented Media is read only")
		} else if (storageState == Environment.MEDIA_NOFS) {
			throwException(errorStarter + "Blank or unsupported file media")
		} else if (storageState == Environment.MEDIA_SHARED) {
			throwException(errorStarter + "Media is shared with USB mass storage")
		} else if (storageState == Environment.MEDIA_REMOVED) {
			throwException(errorStarter + "Media is not present")
		} else if (storageState == Environment.MEDIA_UNMOUNTABLE) {
			throwException(errorStarter + "Media is present but cannot be mounted")
		} else if (storageState == Environment.MEDIA_UNMOUNTED) {
			throwException(errorStarter + "Media is present but not mounted")
		}

		return false
	}

	@Throws(ExternalFileWriterException::class) private fun throwException(errorMessege: String) {
		throw ExternalFileWriterException(errorMessege)
	}

	/**
	 * Exception to report back developer about media state or storage state if
	 * writing is not
	 * possible
	 */
	class ExternalFileWriterException(messege: String) : Exception(messege)
}