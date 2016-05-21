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
				Toast.makeText(m_Context, "蓝牙设备不存在或被关闭，请打开后重新启动服务", Toast.LENGTH_LONG).show();
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
	 * Note:产生端口监听程序。
	 */
	public void CreatePortListen() {
		try {
			// 获取一个本机蓝牙设备
			m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			// 判断是否不存在蓝牙设备或没有打开
			if (m_BluetoothAdapter == null || m_BluetoothAdapter.isEnabled()) {
				// 通过蓝牙设备的ListenUsingRfcommWithServerRecord方法创建一个无线射频通信（RFCOMM）蓝牙端口
				BluetoothServerSocket _BluetoothServerSocket = m_BluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(S_NAME, s_UUID);
				if (m_PortListenThread == null) {
					// 启动端口监听线程
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
	 * Note:端口监听线程
	 */
	public class PortListenThread extends Thread {
		private BluetoothServerSocket m_BluetoothServerSocket;
		BluetoothSocket _BluetoothSocket;

		public PortListenThread(BluetoothServerSocket p_BluetoothServerSocket) {
			// 初始化Socket
			m_BluetoothServerSocket = p_BluetoothServerSocket;
		}

		public void run() {
			try {
				// 调用accept方法接收对方数据请求
				_BluetoothSocket = m_BluetoothServerSocket.accept();// 堵塞线程
				// 获取输出流
				m_OutputStream = _BluetoothSocket.getOutputStream();
				// 修改连接状态
				m_State = m_StateConnected;
				// 建立一个长连接持续接收对方数据
				while (m_State == m_StateConnected) {
					// 获取输入流
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
			// 初始化字节数组
			byte[] _Byte = new byte[8];
			// 读取前8个字节获取数据长度
			m_InputStream.read(_Byte);
			// 将字节转换成String字符
			String _Msg = new String(_Byte);
			// 将String字符转换成int数字
			int _Length = Integer.parseInt(_Msg);
			// 按得到的长度在初始化一个字节数组
			_Byte = new byte[_Length];
			// 继续读取剩余数据
			m_InputStream.read(_Byte);
			// 将两次数据合并为一个完整的数据
			_Msg = _Msg + new String(_Byte);
			// 调用回调，返回到界面处理
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
			// 调用输出流向对方发送数据
			m_OutputStream.write(p_Data.getBytes());
			// 强制输出所有缓冲数据
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
