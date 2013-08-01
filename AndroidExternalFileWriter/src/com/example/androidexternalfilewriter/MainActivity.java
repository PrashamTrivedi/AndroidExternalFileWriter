package com.example.androidexternalfilewriter;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.example.androidexternalfilewriter.AppExternalFileWriter.ExternalFileWriterException;
import com.example.androidexternalfilewriter.R.string;

public class MainActivity extends Activity {

	private AppExternalFileWriter writer;
	private File testFolder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		writer = new AppExternalFileWriter(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void createSubFolderInAppFolder(View v) {
		try {
			testFolder = writer.createSubDirectory("test");
		} catch (ExternalFileWriterException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeDataIntoSubFolder(View v) {

		try {
			if (testFolder == null) {
				testFolder = writer.createSubDirectory("test");
			}
			writer.writeDataToFile(testFolder, "testFile", getString(string.loremipsum));
		} catch (ExternalFileWriterException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeDataIntoTestFile(View v) {
		try {
			writer.writeDataToFile("testFile", getString(string.loremipsum));
		} catch (ExternalFileWriterException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeTimeStampedFile(View v) {
		try {
			writer.writeDataToTimeStampedFile("txt", getString(string.loremipsum));
		} catch (ExternalFileWriterException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
