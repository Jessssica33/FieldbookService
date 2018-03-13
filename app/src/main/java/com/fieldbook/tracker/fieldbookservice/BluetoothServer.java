package com.fieldbook.tracker.fieldbookservice;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by jessica on 3/9/18.
 */

public class BluetoothServer {

    private final BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;
    //private Handler mHandler;

    private AcceptThread mAcceptThread;
    private ConnectedThread mConnectedThread;

    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final String TAG = "BluetoothServer";

    public BluetoothServer() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void init() {

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {

            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.i(TAG, deviceName + ": " + deviceHardwareAddress);
                mDevice = device;
                break;
            }
        }

        mAcceptThread = new AcceptThread(mDevice);
        mAcceptThread.start();
    }

    public void write(byte[] bytes) {
        if (mConnectedThread == null) {
            return;
        }
        mConnectedThread.write(bytes);
    }

    public void cancelConnection() {
        if (mAcceptThread !=null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    /*public void reConnect() {
        if (mConnectedThread != null && (mConnectedThread.isAlive()
                || mConnectedThread.isDaemon() || mConnectedThread.isInterrupted())) {
            mConnectedThread.cancel();
            mAcceptThread.cancel();
        }

        mAcceptThread = new AcceptThread(mDevice);
        mAcceptThread.start();

    }


    //need more adjust
    public boolean checkConnectedStatus() {
        if (mConnectedThread == null || !mConnectedThread.isAlive()) {
            return false;
        }
        return true;
    }*/


    private void manageMyConnectedSocket(BluetoothSocket socket) {

        mSocket = socket;

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
    }

    private class AcceptThread extends Thread{
        private final BluetoothServerSocket mmServerSocket;
        private final BluetoothDevice mmDevice;

        public AcceptThread(BluetoothDevice device) {
            BluetoothServerSocket tmp = null;

            mmDevice = device;
            try {
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("BluetoothServer", MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }

            mmServerSocket = tmp;
        }

        public void run() {

            BluetoothSocket socket = null;

            while (true) {

                try {
                    socket = mmServerSocket.accept();
                } catch(IOException e) {

                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {

                    manageMyConnectedSocket(socket);
                    /*Message writtenMsg = mHandler.obtainMessage(
                            MessageConstants.MESSAGE_CONNECT, -1, -1, "connect success");
                    writtenMsg.sendToTarget();*/

                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Close server socket failed", e);
                    }
                    break;
                }
            }
        }

        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            Log.e("error", "dddddd");
        }

        public void run() {
            Log.e("error", "eeeeeee");
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {

                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);

                    // Send the obtained bytes to the UI activity.
                    /*Message readMsg = mHandler.obtainMessage(
                            MessageConstants.MESSAGE_READ, numBytes, -1,
                            mmBuffer);
                    readMsg.sendToTarget();*/
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
                /*Message writtenMsg = mHandler.obtainMessage(
                        MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                writtenMsg.sendToTarget();*/
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                /*Message writeErrorMsg =
                        mHandler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                mHandler.sendMessage(writeErrorMsg);*/
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}
