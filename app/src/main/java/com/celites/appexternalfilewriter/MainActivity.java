package com.celites.appexternalfilewriter;

import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.ToggleButton;
import com.celites.androidexternalfilewriter.AppExternalFileWriter;
import com.celites.androidexternalfilewriter.SAFFileWriter;
import java.io.File;
import java.io.FileNotFoundException;

public class MainActivity
		extends AppCompatActivity {

	private ToggleButton inCache;
	private SAFFileWriter safFileWriter;
	private DocumentFile testDirectory;
	private File testFolder;
	private AppExternalFileWriter writer;
	private static final int REQUEST_FILE_ACCESS = 2488;

	public void createSubFolderInAppFolder(View v) {
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			testDirectory = safFileWriter.createSubdirectory("test", inCache.isChecked());
		} else {
			try {
				testFolder = writer.createSubDirectory("test", inCache.isChecked());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void writeDataIntoSubFolder(View v) {
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			testDirectory = safFileWriter.createSubdirectory("test", inCache.isChecked());
			try {
				safFileWriter.writeDataToFile(testDirectory, "testFile", getString(R.string.loremipsum), "file/txt");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			try {
				if (testFolder == null) {
					testFolder = writer.createSubDirectory("test", inCache.isChecked());
				}
				writer.writeDataToFile(testFolder, "testFile", getString(R.string.loremipsum));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void writeDataIntoTestFile(View v) {
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			try {
				safFileWriter.writeDataToFile("testFile", "file/txt", getString(R.string.loremipsum), inCache.isChecked());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			try {
				writer.writeDataToFile("testFile", getString(R.string.loremipsum), inCache.isChecked());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void writeTimeStampedFile(View v) {
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			try {
				safFileWriter.writeDataToTimeStampedFile("file/txt", getString(R.string.loremipsum), "txt", inCache.isChecked());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			try {
				writer.writeDataToTimeStampedFile("txt", getString(R.string.loremipsum), inCache.isChecked());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//		writer.handleResult(requestCode, resultCode, data);
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			safFileWriter.handleResult(requestCode, resultCode, data);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		writer = new AppExternalFileWriter(this);
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			safFileWriter = new SAFFileWriter(this, REQUEST_FILE_ACCESS);
		}
		inCache = (ToggleButton) findViewById(R.id.toggleButton);
	}
}
