package com.zzhuopeng.mybluetooth1;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class BusinessBluetooth {
	private static final String s_Tag = "BusinessBluetooth";
	private BluetoothAdapter m_BluetoothAdapter;
	private Context m_Context;
	private InputStream m_InputStream;
	private OutputStream m_OutputStream;
	private PortListenThread m_PortListenThread;
	private int m_State;
	private int m_StateConnected = 0;
	private int m_StateDisconnected = 0;
	private boolean m_IsNormalClosed = false;
	private Message m_Message = new Message();
	private OnPortListener m_OnPortListener;
	private Handler m_Handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				Toast.makeText(m_Context, "�����豸�����ڻ򱻹رգ���򿪺�������������", Toast.LENGTH_LONG).show();
			}
		};
	};

	private static final String S_NAME = "BlutoothChat";// "TimaBluetooth"
	private static final UUID s_UUID = UUID.randomUUID();
			//UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	public interface OnPortListener {
		public abstract void OnReceiveData(String p_Message);
	}

	public BusinessBluetooth(Context p_Context) {
		m_Context = p_Context;
		m_Message.what = 1;
		m_OnPortListener = (OnPortListener) p_Context;
	}

	/**
	 * Note:�����˿ڼ�������
	 */
	public void CreatePortListen() {
		try {
			// ��ȡһ�����������豸
			m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			// �ж��Ƿ񲻴��������豸��û�д�
			if (m_BluetoothAdapter == null || m_BluetoothAdapter.isEnabled()) {
				// ͨ�������豸��ListenUsingRfcommWithServerRecord��������һ��������Ƶͨ�ţ�RFCOMM�������˿�
				BluetoothServerSocket _BluetoothServerSocket = m_BluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(S_NAME, s_UUID);
				if (m_PortListenThread == null) {
					// �����˿ڼ����߳�
					m_PortListenThread = new PortListenThread(_BluetoothServerSocket);
					m_PortListenThread.start();
				} else {
					m_Handler.sendMessage(m_Message);
				}
			}
		} catch (Exception e) {
			Log.i(s_Tag, "CreatePortListen:" + e.getMessage());
			CreatePortListen();
		}
	}

	/**
	 * Note:�˿ڼ����߳�
	 */
	public class PortListenThread extends Thread {
		private BluetoothServerSocket m_BluetoothServerSocket;
		BluetoothSocket _BluetoothSocket;

		public PortListenThread(BluetoothServerSocket p_BluetoothServerSocket) {
			// ��ʼ��Socket
			m_BluetoothServerSocket = p_BluetoothServerSocket;
		}

		public void run() {
			try {
				// ����accept�������նԷ���������
				_BluetoothSocket = m_BluetoothServerSocket.accept();// �����߳�
				// ��ȡ�����
				m_OutputStream = _BluetoothSocket.getOutputStream();
				// �޸�����״̬
				m_State = m_StateConnected;
				// ����һ�������ӳ������նԷ�����
				while (m_State == m_StateConnected) {
					// ��ȡ������
					m_InputStream = _BluetoothSocket.getInputStream();
					ReceiveData();
				}
			} catch (Exception e) {
				Log.i(s_Tag, e.getMessage());
				if (!m_BluetoothAdapter.isEnabled()) {
					m_Handler.sendMessage(m_Message);
				}
			}
		}

		public void Close() {
			try {
				m_BluetoothServerSocket.close();
				if (_BluetoothSocket != null) {
					_BluetoothSocket.close();
				}
				if (m_OutputStream != null) {
					m_OutputStream.close();
				}
				if (m_InputStream != null) {
					m_InputStream.close();
				}
			} catch (Exception e) {
				Log.i(s_Tag, e.getMessage());
			}
		}
	}

	public void ReceiveData() {
		try {
			// ��ʼ���ֽ�����
			byte[] _Byte = new byte[8];
			// ��ȡǰ8���ֽڻ�ȡ���ݳ���
			m_InputStream.read(_Byte);
			// ���ֽ�ת����String�ַ�
			String _Msg = new String(_Byte);
			// ��String�ַ�ת����int����
			int _Length = Integer.parseInt(_Msg);
			// ���õ��ĳ����ڳ�ʼ��һ���ֽ�����
			_Byte = new byte[_Length];
			// ������ȡʣ������
			m_InputStream.read(_Byte);
			// ���������ݺϲ�Ϊһ������������
			_Msg = _Msg + new String(_Byte);
			// ���ûص������ص����洦��
			m_OnPortListener.OnReceiveData(_Msg);
		} catch (Exception e) {
			Log.i(s_Tag, e.getMessage());
			if (!m_IsNormalClosed) {
				Close(false);
				CreatePortListen();
			}
		}
	}

	public void SendData(String p_Data) {
		try {
			// �����������Է���������
			m_OutputStream.write(p_Data.getBytes());
			// ǿ��������л�������
			m_OutputStream.flush();
		} catch (Exception e) {
			Log.i(s_Tag, e.getMessage());
		}
	}

	public void Close(boolean p_IsNormalClosed) {
		m_IsNormalClosed = p_IsNormalClosed;
		m_State = m_StateDisconnected;
		if (m_PortListenThread != null) {
			m_PortListenThread.Close();
			m_PortListenThread = null;
		}
	}
}
