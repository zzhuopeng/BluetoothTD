package com.zzhuopeng.mybluetooth1;

import com.zzhuopeng.mybluetooth1.BusinessBluetooth.OnPortListener;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity implements OnClickListener, OnPortListener {

	EditText _editMessage;
	Button _btnSend;

	BusinessBluetooth m_BusinessBluetooth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		InitView();
		InitListener();
	}

	private void InitView() {
		_editMessage = (EditText) findViewById(R.id.editMessage);
		_btnSend = (Button) findViewById(R.id.btnSent);
	}

	private void InitListener() {
		_btnSend.setOnClickListener(this);
	}

	@Override
	public void OnReceiveData(String p_Message) {
		Log.i("BusinessBluetooth", "接收的数据为：" + p_Message);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnSent:
			m_BusinessBluetooth.SendData(_editMessage.getText().toString());
			break;

		default:
			break;
		}
	}
}
