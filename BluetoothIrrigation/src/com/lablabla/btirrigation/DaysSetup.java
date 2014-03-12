package com.lablabla.btirrigation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TimePicker;
import android.widget.Toast;

@SuppressLint("SdCardPath")
public class DaysSetup extends Activity {

	public static final String RETURNED_DATA = "returned_data";
	public static final String dataFile = "/days.data";
	private CheckBox chkSun, chkMon, chkTue, chkWed, chkThu, chkFri, chkSat;
	private TimePicker start1, stop1, start2, stop2;
	private Button bSave;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_days);
		setResult(Activity.RESULT_CANCELED);
		init();
		try {
			loadDaysObject();
		} catch (FileNotFoundException e) {
			File file = new File(dataFile);
			if (!file.exists()) {
				try {
					file.createNewFile();
					Toast.makeText(getApplicationContext(), "Couldn't save data2", Toast.LENGTH_LONG).show();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void loadDaysObject() throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(getApplicationContext().getFilesDir() + dataFile)));
		Days days = (Days) ois.readObject();
		ois.close();
		start1.setCurrentHour(days.getStart1H());
		start1.setCurrentMinute(days.getStart1M());
		stop1.setCurrentHour(days.getStop1H());
		stop1.setCurrentMinute(days.getStop1M());
		start2.setCurrentHour(days.getStart2H());
		start2.setCurrentMinute(days.getStart2M());
		stop2.setCurrentHour(days.getStop2H());
		stop2.setCurrentMinute(days.getStop2M());
		setDays(days.getDays());
	}

	private void setDays(byte days) {
		chkSun.setChecked((days & Days.BIT_SUNDAY) != 0);
		chkMon.setChecked((days & Days.BIT_MONDAY) != 0);
		chkTue.setChecked((days & Days.BIT_TUESDAY) != 0);
		chkWed.setChecked((days & Days.BIT_WEDENSDAY) != 0);
		chkThu.setChecked((days & Days.BIT_THURSDAY) != 0);
		chkFri.setChecked((days & Days.BIT_FRIDAY) != 0);
		chkSat.setChecked((days & Days.BIT_SATURDAY) != 0);
	}

	private void init() {
		start1 = (TimePicker) findViewById(R.id.start1);
		start2 = (TimePicker) findViewById(R.id.start2);
		stop1 = (TimePicker) findViewById(R.id.stop1);
		stop2 = (TimePicker) findViewById(R.id.stop2);
		start1.setIs24HourView(true);
		stop1.setIs24HourView(true);
		start2.setIs24HourView(true);
		stop2.setIs24HourView(true);

		chkSun = (CheckBox) findViewById(R.id.chkSun);
		chkMon = (CheckBox) findViewById(R.id.chkMon);
		chkTue = (CheckBox) findViewById(R.id.chkTue);
		chkWed = (CheckBox) findViewById(R.id.chkWed);
		chkThu = (CheckBox) findViewById(R.id.chkThu);
		chkFri = (CheckBox) findViewById(R.id.chkFri);
		chkSat = (CheckBox) findViewById(R.id.chkSat);
		bSave = (Button) findViewById(R.id.buttonSave);
		bSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				saveAndReturn();
			}
		});
	}

	protected void saveAndReturn() {
		Days days = new Days();

		days.setStart1H(start1.getCurrentHour());
		days.setStart1M(start1.getCurrentMinute());

		days.setStop1H(stop1.getCurrentHour());
		days.setStop1M(stop1.getCurrentMinute());

		days.setStart2H(start2.getCurrentHour());
		days.setStart2M(start2.getCurrentMinute());

		days.setStop2H(stop2.getCurrentHour());
		days.setStop2M(stop2.getCurrentMinute());

		days.setDays(chkSun.isChecked(), chkMon.isChecked(), chkTue.isChecked(), chkWed.isChecked(), chkThu.isChecked(), chkFri.isChecked(), chkSat.isChecked());
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(new File(getApplicationContext().getFilesDir() + DaysSetup.dataFile)));
			oos.writeObject(days);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (oos != null) {
				try {
					oos.flush();
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		Intent intent = new Intent();
		intent.putExtra(DaysSetup.RETURNED_DATA, days);
		setResult(Activity.RESULT_OK, intent);
		finish();

	}
}
