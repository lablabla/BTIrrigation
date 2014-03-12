package com.lablabla.btirrigation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	private static final char COM_SEPARATOR = ';';
	private static final char COM_STRING_SEPARATOR = '-';
	private static final int COM_RESET_TIME = 0;
	private static final int COM_SET_TIMES = 1;
	private static final int COM_V1_ON = 2;
	private static final int COM_V1_OFF = 3;
	private static final int COM_V2_ON = 4;
	private static final int COM_V2_OFF = 5;
	private static final int COM_REQUEST_INFO = 6;
	private static final int COM_SET_BACKLIGHT = 7;
	private static final int COM_UPDATE_TIME = 8;
	private static final int COM_SHOW_TOAST = 9;

	private static final String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
	private static final int REQUEST_ENABLE_BT = 0;
	private static final int REQUEST_CONNECT_DEVICE = 1;
	protected static final int REQUEST_SETUP_DAYS = 2;
	protected static final int TEXT_ID = 0;

	private Listener listener;
	private OutputStream btOutput = null;
	private InputStream btInput = null;
	private BluetoothAdapter btAdapter;
	private Button bSetTimes, bUpdateTime, bSetBacklight, bRefreshData;
	private ToggleButton tbV1, tbV2;
	private TextView tvStatus;
	private EditText timeText;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			enableUI();
			timeText.setText("COM ERROR");
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
	}

	private void init() {
		bSetTimes = (Button) findViewById(R.id.bSetTimes);
		bUpdateTime = (Button) findViewById(R.id.bUpdateTime);
		bSetBacklight = (Button) findViewById(R.id.bSetBacklight);
		bRefreshData = (Button) findViewById(R.id.bRefreshData);
		tbV1 = (ToggleButton) findViewById(R.id.tbV1);
		tbV2 = (ToggleButton) findViewById(R.id.tbV2);
		tvStatus = (TextView) findViewById(R.id.tvStatus);
		timeText = (EditText) findViewById(R.id.etTime);
		bSetTimes.setOnClickListener(setTime);
		bUpdateTime.setOnClickListener(updateTime);
		bSetBacklight.setOnClickListener(setBacklight);
		bRefreshData.setOnClickListener(refreshData);
		tbV1.setOnCheckedChangeListener(toggleV1);
		tbV2.setOnCheckedChangeListener(toggleV2);

		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter == null) {
			Toast.makeText(getApplicationContext(), "No Bluetooth on this device.", Toast.LENGTH_SHORT).show();
			finish();
		}
		if (!btAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}
		Intent serverIntent = new Intent(this, DeviceListActivity.class);
		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);

	}

	private void setBacklightTime(String timeString) {
		int time;
		try {
			time = Integer.parseInt(timeString);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			time = 5;
		}
		if (time > 30) {
			time = 30;
		}
		time *= 1000;
		sendCommand(COM_SET_BACKLIGHT, String.valueOf(time));
	}

	OnClickListener refreshData = new OnClickListener() {

		@Override
		public void onClick(View v) {
			sendCommand(COM_REQUEST_INFO, "");
		}
	};

	OnClickListener setBacklight = new OnClickListener() {

		@Override
		public void onClick(View v) {
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle("Set LCD Backlight");
			builder.setMessage("Enter time in seconds:");

			final EditText input = new EditText(MainActivity.this);
			input.setInputType(InputType.TYPE_CLASS_NUMBER);
			input.setId(TEXT_ID);
			builder.setView(input);

			builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					setBacklightTime(input.getText().toString());
					return;
				}

			});

			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
			});

			builder.create().show();
		}
	};

	OnClickListener setTime = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent setupIntent = new Intent(MainActivity.this, DaysSetup.class);
			startActivityForResult(setupIntent, REQUEST_SETUP_DAYS);
		}
	};

	OnClickListener updateTime = new OnClickListener() {

		@Override
		public void onClick(View v) {
			SimpleDateFormat sdf = new SimpleDateFormat("MM dd yyyy-HH:mm:ss");
			Date date = new Date();
			sendCommand(COM_RESET_TIME, sdf.format(date));
		}
	};

	CompoundButton.OnCheckedChangeListener toggleV1 = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (isChecked) {
				sendCommand(COM_V1_ON, "");
			} else {
				sendCommand(COM_V1_OFF, "");
			}
		}
	};

	CompoundButton.OnCheckedChangeListener toggleV2 = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (isChecked) {
				sendCommand(COM_V2_ON, "");
			} else {
				sendCommand(COM_V2_OFF, "");
			}
		}
	};

	protected void sendCommand(int commandInt, String command) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(commandInt);
			sb.append(COM_SEPARATOR);
			sb.append(command);
			sb.append('\0');
			if (btOutput != null) {
				btOutput.write(sb.toString().getBytes());
				disableUI();
				mHandler.sendEmptyMessageDelayed(0, 1000);
				Log.v("COM", sb.toString());
			} else {
				Toast.makeText(getApplicationContext(), "No connected to remote device.", Toast.LENGTH_LONG).show();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void enableUI() {
		bRefreshData.setEnabled(true);
		bSetBacklight.setEnabled(true);
		bSetTimes.setEnabled(true);
		bUpdateTime.setEnabled(true);
		tbV1.setEnabled(true);
		tbV2.setEnabled(true);
	}
	
	private void disableUI() {
		bRefreshData.setEnabled(false);
		bSetBacklight.setEnabled(false);
		bSetTimes.setEnabled(false);
		bUpdateTime.setEnabled(false);
		tbV1.setEnabled(false);
		tbV2.setEnabled(false);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			if (resultCode == Activity.RESULT_OK) {
				String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				BluetoothDevice device = btAdapter.getRemoteDevice(address);
				ConnectThread connect = new ConnectThread(device);
				connect.start();
			}
			break;
		case REQUEST_SETUP_DAYS:
			if (resultCode == Activity.RESULT_OK) {
				Days days = (Days) data.getExtras().getSerializable(DaysSetup.RETURNED_DATA);
				StringBuilder sb = new StringBuilder();
				sb.append(days.getStart1H());
				sb.append(COM_STRING_SEPARATOR);
				sb.append(days.getStart1M());
				sb.append(COM_STRING_SEPARATOR);
				sb.append(days.getStop1H());
				sb.append(COM_STRING_SEPARATOR);
				sb.append(days.getStop1M());
				sb.append(COM_STRING_SEPARATOR);
				sb.append(days.getStart2H());
				sb.append(COM_STRING_SEPARATOR);
				sb.append(days.getStart2M());
				sb.append(COM_STRING_SEPARATOR);
				sb.append(days.getStop2H());
				sb.append(COM_STRING_SEPARATOR);
				sb.append(days.getStop2M());
				sb.append(COM_STRING_SEPARATOR);
				sb.append(days.getDays());
				sendCommand(COM_SET_TIMES, sb.toString());
			}
		}
	}

	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			BluetoothSocket tmp = null;
			mmDevice = device;

			try {
				tmp = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
			} catch (IOException e) {
			}
			mmSocket = tmp;
		}

		public void run() {
			btAdapter.cancelDiscovery();

			try {
				mmSocket.connect();
			} catch (IOException connectException) {
				try {
					mmSocket.close();
				} catch (IOException closeException) {
				}
				return;
			}
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					tvStatus.setText("Status: Connected");
					try {
						btOutput = mmSocket.getOutputStream();
						btInput = mmSocket.getInputStream();
						listener = new Listener();
						listener.start();
						sendCommand(COM_REQUEST_INFO, "");
						SimpleDateFormat sdf = new SimpleDateFormat("MM dd yyyy-HH:mm:ss");
						Date date = new Date();
						sendCommand(COM_RESET_TIME, sdf.format(date));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		}

	}

	private class Listener extends Thread {
		private boolean _run;
		private BufferedReader reader;

		public Listener() {
			_run = true;
			reader = new BufferedReader(new InputStreamReader(btInput));
		}

		@Override
		public void run() {
			while (_run) {
				try {
					if (btInput.available() > 0) {
						String line = reader.readLine();
						int command = line.charAt(0) - 48;
						if (command == COM_REQUEST_INFO) {
							Log.d("REQUEST", line);
							String receivedCommand = line.substring(line.indexOf(COM_SEPARATOR) + 1);
							handleCommand(receivedCommand);
						} else if (command == COM_UPDATE_TIME) {
							String time = line.substring(line.indexOf(COM_SEPARATOR) + 1);
							updateTimeUI(time);
						} else if (command == COM_SHOW_TOAST) {
							String msg = line.substring(line.indexOf(COM_SEPARATOR) + 1);
							showToast(msg);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private void showToast(String msg) {
			final String message = msg;
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
					enableUI();
				}
			});
		}

		private void handleCommand(String receivedCommand) {
			final String[] commands = receivedCommand.split("-");
			ObjectOutputStream oos = null;
			if (commands.length > 10) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						tbV1.setChecked(Boolean.parseBoolean(commands[0]));
						tbV2.setChecked(Boolean.parseBoolean(commands[1]));
					}
				});
				Days days = new Days();
				days.setStart1H(Integer.parseInt(commands[2]));
				days.setStart1M(Integer.parseInt(commands[3]));
				days.setStop1H(Integer.parseInt(commands[4]));
				days.setStop1M(Integer.parseInt(commands[5]));
				days.setStart2H(Integer.parseInt(commands[6]));
				days.setStart2M(Integer.parseInt(commands[7]));
				days.setStop2H(Integer.parseInt(commands[8]));
				days.setStop2M(Integer.parseInt(commands[9]));
				days.setDays(Integer.parseInt(commands[10]));
				try {
					oos = new ObjectOutputStream(new FileOutputStream(new File(getApplicationContext().getFilesDir() + DaysSetup.dataFile)));
					oos.writeObject(days);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (oos != null) {
					try {
						oos.flush();
						oos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}

		private void updateTimeUI(String time) {
			final String timeFinal = time;
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					timeText.setText(timeFinal);
				}
			});
		}
	}
}
