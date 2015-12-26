package com.celites.appexternalfilewriter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.ToggleButton;
import com.celites.androidexternalfilewriter.AppExternalFileWriter;
import com.celites.androidexternalfilewriter.ExternalFileWriterException;
import java.io.File;

public class MainActivity
		extends AppCompatActivity {

	private ToggleButton inCache;
	private File testFolder;
	private AppExternalFileWriter writer;

	public void createSubFolderInAppFolder(View v) {
		try {
			testFolder = writer.createSubDirectory("test", inCache.isChecked());
		} catch (ExternalFileWriterException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void writeDataIntoSubFolder(View v) {

		try {
			if (testFolder == null) {
				testFolder = writer.createSubDirectory("test", inCache.isChecked());
			}
			writer.writeDataToFile(testFolder, "testFile", getString(R.string.loremipsum));
		} catch (ExternalFileWriterException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeDataIntoTestFile(View v) {
		try {
			writer.writeDataToFile("testFile", getString(R.string.loremipsum), inCache.isChecked());
		} catch (ExternalFileWriterException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeTimeStampedFile(View v) {
		try {
			writer.writeDataToTimeStampedFile("txt", getString(R.string.loremipsum), inCache.isChecked());
		} catch (ExternalFileWriterException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		writer.handleResult(requestCode, resultCode, data);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		writer = new AppExternalFileWriter(this, true);
		inCache = (ToggleButton) findViewById(R.id.toggleButton);
	}
}
