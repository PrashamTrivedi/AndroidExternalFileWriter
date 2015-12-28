package com.celites.androidexternalfilewriter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import com.ceelites.devutils.ConstantMethods;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Prasham on 12/25/2015.
 */
@TargetApi(VERSION_CODES.LOLLIPOP)
public class SAFFileWriter {

	private String PARENT_URI_KEY = "APP_EXTERNAL_PARENT_FILE_URI";
	private Activity activity;
	private DocumentFile appCacheDirectory;
	private DocumentFile appDirectory;
	private DocumentFile externalCacheDirectory;
	private DocumentFile externalParentFile;
	private final SharedPreferences preferences;
	private int requestCode;
	private static final String canNotCreateDirectory = "Can not create directory: ";
	private static final String canNotWriteFile = "Can not write file: ";


	/**
	 * Inits new SAFFileWriter object, it will first check whether we already have a parent directory with proper uri access or not.
	 *
	 * @param activity:
	 * 		Activity for context and starting request for OPEN_DOCUMENT_TREE
	 * @param requestCode:
	 * 		Request code to listen to OPEN_DOCUMENT_TREE
	 */
	public SAFFileWriter(Activity activity, int requestCode) {
		this.activity = activity;
		this.requestCode = requestCode;
		File[] dirs = ContextCompat.getExternalCacheDirs(activity);
		if (dirs.length > 1) {
			File dir = dirs[1];
			if (dir != null) {
				externalCacheDirectory = DocumentFile.fromFile(dir);
			} else {
				externalCacheDirectory = DocumentFile.fromFile(dirs[0]);
			}
		} else {
			externalCacheDirectory = DocumentFile.fromFile(dirs[0]);
		}
		preferences = PreferenceManager.getDefaultSharedPreferences(activity);
		String externalDirUrl = preferences.getString(PARENT_URI_KEY, "");
		if (ConstantMethods.isEmptyString(externalDirUrl)) {
			Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
			activity.startActivityForResult(intent, requestCode);
		}
	}

	/**
	 * Creates subdirectory in parent directory
	 *
	 * @param parentDirectory
	 * 		: Parent directory where directory with "directoryName" should be created
	 * @param displayName
	 * 		name of subdirectory
	 *
	 * @return File object of created subdirectory
	 *
	 * @throws ExternalFileWriterException
	 * 		if external storage is not available
	 */
	public DocumentFile createSubDirectory(String displayName, DocumentFile parentDirectory) {
		getAppDirectory();
		if (isDirectoryExists(displayName, parentDirectory)) {

			return parentDirectory.createDirectory(displayName);
		} else {
			return parentDirectory.findFile(displayName);
		}
	}

	/**
	 * Get created app directory
	 *
	 * @return File object of created AppDirectory
	 */
	public DocumentFile getAppDirectory() {
		if (appDirectory == null) {
			createAppDirectory();
		}
		return appDirectory;
	}

	/**
	 * Check whether directory with given name exists in parentDirectory or not.
	 *
	 * @param directoryName
	 * 		: Name of the directory to check.
	 * @param parentDirectory
	 * 		: Parent directory where directory with "directoryName" should be present
	 *
	 * @return true if a directory with "directoryName" exists, false otherwise
	 */
	public boolean isDirectoryExists(String displayName, DocumentFile parentDirectory) {
		DocumentFile file = parentDirectory.findFile(displayName);
		return file != null && file.isDirectory();
	}

	/** Creates app directory */
	private void createAppDirectory() {
		String directoryName = activity.getString(activity.getApplicationInfo().labelRes);
		appDirectory = externalParentFile.createDirectory(directoryName);
		appCacheDirectory = externalCacheDirectory.createDirectory(directoryName);
	}

	/**
	 * Creates subdirectory in application directory
	 *
	 * @param directoryName
	 * 		name of subdirectory
	 *
	 * @return File object of created subdirectory
	 *
	 * @throws ExternalFileWriterException
	 * 		if external storage is not available
	 */
	public DocumentFile createSubdirectory(String directoryName, boolean inCache) {
		getAppDirectory();
		DocumentFile appDirectory = getAppDirectory(inCache);
		if (!isDirectoryExists(directoryName, inCache)) {

			return appDirectory.createDirectory(directoryName);
		} else {
			return appDirectory.findFile(directoryName);
		}
	}

	public DocumentFile getAppDirectory(boolean inCache) {
		return (inCache) ? this.appCacheDirectory : this.appDirectory;
	}

	/**
	 * Checks whether directory with given name exists in AppDirectory
	 *
	 * @param directoryName
	 * 		: Name of the directory to check.
	 *
	 * @return true if a directory with "directoryName" exists, false otherwise
	 */
	public boolean isDirectoryExists(String displayName, boolean inCache) {
		DocumentFile file = getDocumentFile(displayName, inCache);
		return file != null && file.isDirectory();
	}

	private DocumentFile getDocumentFile(String displayName, boolean inCache) {
		DocumentFile appDirectory = getAppDirectory(inCache);
		return appDirectory.findFile(displayName);
	}

	public void handleResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == this.requestCode) {
				Uri treeUri = data.getData();
				externalParentFile = DocumentFile.fromTreeUri(activity, treeUri);
				preferences.edit().putString(PARENT_URI_KEY, String.valueOf(externalParentFile.getUri()));
				getAppDirectory();

			}
		}
	}

	/**
	 * Check whether file with given name exists in parentDirectory or not.
	 *
	 * @param fileName
	 * 		: Name of the file to check.
	 * @param parentDirectory
	 * 		: Parent directory where directory with "fileName" should be present
	 *
	 * @return true if a file  with "fileName" exists, false otherwise
	 */
	public boolean isFileExists(String displayName, boolean inCache) {
		DocumentFile file = getDocumentFile(displayName, inCache);
		return file != null && file.isFile();
	}

	public void writeDataToFile(String fileName, String mimeType, byte[] data, boolean inCache) throws FileNotFoundException {
		getAppDirectory();
		DocumentFile appDir = getAppDirectory(inCache);
		writeDataToFile(appDir, fileName, data, mimeType);
	}

	/**
	 * Writes data to the file. The file will be created in the directory name same as app.
	 *
	 * @param fileName
	 * 		name of the file
	 * @param data
	 * 		data to write
	 *
	 * @throws ExternalFileWriterException
	 * 		if external storage is not available or free space is less than size of the data
	 */
	public void writeDataToFile(DocumentFile parent, String fileName, byte[] data, String mimeType) throws FileNotFoundException {
		DocumentFile file = createFile(fileName, parent, mimeType);
		writeDataToFile(file, data);
	}

	private DocumentFile createFile(String fileName, DocumentFile parent, String mimeType) {
		if (!isFileExists(fileName, parent)) {

			return parent.createFile(mimeType, fileName);
		} else {
			return parent.findFile(fileName);
		}
	}

	/**
	 * Write byte array to file. Will show error if given file is a directory.
	 *
	 * @param file
	 * 		: File where data is to be written.
	 * @param data
	 * 		byte array which you want to write a file. If size of this is greater than size available, it will show error.
	 */
	private void writeDataToFile(DocumentFile file, byte[] data) throws FileNotFoundException {
		ParcelFileDescriptor fileDescriptor = activity.getContentResolver().openFileDescriptor(file.getUri(), "w");
		FileOutputStream out = null;
		if (fileDescriptor != null) {
			out = new FileOutputStream(fileDescriptor.getFileDescriptor());
			try {
				out.write(data);
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Checks whether file with given name exists in AppDirectory
	 *
	 * @param fileName
	 * 		: Name of the file to check.
	 *
	 * @return true if a file with "directoryName" exists, false otherwise
	 */
	public boolean isFileExists(String displayName, DocumentFile parentDirectory) {
		DocumentFile file = parentDirectory.findFile(displayName);
		return file != null && file.isFile();
	}

	public void writeDataToFile(String fileName, String mimeType, String data, boolean inCache) throws FileNotFoundException {
		DocumentFile appDir = getAppDirectory(inCache);
		writeDataToFile(appDir, fileName, data, mimeType);
	}

	/**
	 * Write data in file of a parent directory
	 *
	 * @param parent
	 * 		parent directory
	 * @param fileName
	 * 		desired filename
	 * @param data
	 * 		data
	 *
	 * @throws ExternalFileWriterException
	 * 		if external storage is not available or free space is less than size of the data
	 */
	public void writeDataToFile(DocumentFile parent, String fileName, String data, String mimeType) throws FileNotFoundException {
		DocumentFile file = createFile(fileName, parent, mimeType);
		writeDataToFile(file, data);
	}

	/**
	 * Write byte array to file. Will show error if given file is a directory.
	 *
	 * @param file
	 * 		: File where data is to be written.
	 * @param data
	 * 		String which you want to write a file. If size of this is greater than size available, it will show error.
	 */
	private void writeDataToFile(DocumentFile file, String data) throws FileNotFoundException {
		byte[] stringBuffer = data.getBytes();
		writeDataToFile(file, stringBuffer);
	}

	public void writeDataToTimeStampedFile(String mimeType, String data, String extension, boolean inCache) throws FileNotFoundException {
		DocumentFile appDir = getAppDirectory(inCache);
		String fileExtension = (TextUtils.isEmpty(extension)) ? "" : "." + extension;
		String fileName = System.currentTimeMillis() + fileExtension;
		writeDataToFile(appDir, fileName, data, mimeType);
	}

	public void writeDataToTimeStampedFile(String mimeType, byte[] data, String extension, boolean inCache) throws FileNotFoundException {
		DocumentFile appDir = getAppDirectory(inCache);
		String fileExtension = (TextUtils.isEmpty(extension)) ? "" : "." + extension;
		String fileName = System.currentTimeMillis() + fileExtension;
		writeDataToFile(appDir, fileName, data, mimeType);
	}

	public void writeDataToTimeStampedFile(String mimeType, String data, String extension, boolean inCache, DocumentFile parent) throws
	                                                                                                                             FileNotFoundException {
		String fileExtension = (TextUtils.isEmpty(extension)) ? "" : "." + extension;
		String fileName = System.currentTimeMillis() + fileExtension;
		writeDataToFile(parent, fileName, data, mimeType);
	}

	public void writeDataToTimeStampedFile(String mimeType, byte[] data, String extension, boolean inCache, DocumentFile parent) throws
	                                                                                                                             FileNotFoundException {
		String fileExtension = (TextUtils.isEmpty(extension)) ? "" : "." + extension;
		String fileName = System.currentTimeMillis() + fileExtension;
		writeDataToFile(parent, fileName, data, mimeType);
	}

	private DocumentFile createFile(String fileName, boolean inCache, String mimeType) {
		return createFile(fileName, getAppDirectory(inCache), mimeType);
	}

}
