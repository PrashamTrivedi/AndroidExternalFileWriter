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
	private int requestCode;
	private static final String canNotCreateDirectory = "Can not create directory: ";
	private static final String canNotWriteFile = "Can not write file: ";


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
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
		String externalDirUrl = preferences.getString(PARENT_URI_KEY, "");
		if (ConstantMethods.isEmptyString(externalDirUrl)) {
			Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
			activity.startActivityForResult(intent, requestCode);
		}
	}

	public DocumentFile createSubDirectory(String displayName, DocumentFile parentDirectory) {
		return parentDirectory.createDirectory(displayName);
	}

	public DocumentFile createSubdirectory(String directoryName, boolean inCache) {
		DocumentFile appDirectory = getAppDirectory(inCache);
		return appDirectory.createDirectory(directoryName);
	}

	public DocumentFile getAppDirectory() {
		if (appDirectory == null) {
			createAppDirectory();
		}
		return appDirectory;
	}

	private void createAppDirectory() {
		String directoryName = activity.getString(activity.getApplicationInfo().labelRes);
		appDirectory = externalParentFile.createDirectory(directoryName);
		appCacheDirectory = externalCacheDirectory.createDirectory(directoryName);
	}

	public void handleResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == this.requestCode) {
				Uri treeUri = data.getData();
				externalParentFile = DocumentFile.fromTreeUri(activity, treeUri);


			}
		}
	}

	public boolean isDirectoryExists(String displayName, DocumentFile parentDirectory) {
		DocumentFile file = parentDirectory.findFile(displayName);
		return file != null && file.isDirectory();
	}

	public boolean isDirectoryExists(String displayName, boolean inCache) {
		DocumentFile file = getDocumentFile(displayName, inCache);
		return file != null && file.isDirectory();
	}

	private DocumentFile getDocumentFile(String displayName, boolean inCache) {
		DocumentFile appDirectory = getAppDirectory(inCache);
		return appDirectory.findFile(displayName);
	}

	public boolean isFileExists(String displayName, boolean inCache) {
		DocumentFile file = getDocumentFile(displayName, inCache);
		return file != null && file.isFile();
	}

	public boolean isFileExists(String displayName, DocumentFile parentDirectory) {
		DocumentFile file = parentDirectory.findFile(displayName);
		return file != null && file.isFile();
	}

	public void writeDataToFile(String fileName, String mimeType, byte[] data, boolean inCache) throws FileNotFoundException {
		DocumentFile appDir = getAppDirectory(inCache);
		writeDataToFile(appDir, fileName, data, mimeType);
	}

	public void writeDataToFile(DocumentFile parent, String fileName, byte[] data, String mimeType) throws FileNotFoundException {
		DocumentFile file = createFile(fileName, parent, mimeType);
		writeDataToFile(file, data);
	}

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

	public void writeDataToFile(String fileName, String mimeType, String data, boolean inCache) throws FileNotFoundException {
		DocumentFile appDir = getAppDirectory(inCache);
		writeDataToFile(appDir, fileName, data, mimeType);
	}

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

	private DocumentFile createFile(String fileName, DocumentFile parent, String mimeType) {
		return parent.createFile(mimeType, fileName);
	}

	public DocumentFile getAppDirectory(boolean inCache) {
		return (inCache) ? this.appCacheDirectory : this.appDirectory;
	}

}
