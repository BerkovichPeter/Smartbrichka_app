package com.example.smartbrichka;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.toksichniydinosavr.smartbrichka.R;


public class MainActivity extends Activity {
    private static final String TAG = "bluetooth1";

    Button forwardButton, backButton, leftButton, rightButton, stopButton;//Указываем id наших кнопок

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;

    // SPP UUID сервиса
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-адрес Bluetooth модуля
    private static String address = "98:D3:36:00:B7:E9";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        forwardButton = (Button) findViewById(R.id.forwardButton); //Добавляем  имена наших кнопок
        backButton = (Button) findViewById(R.id.backButton);
        leftButton = (Button) findViewById(R.id.leftButton);
        rightButton = (Button) findViewById(R.id.rightButton);
        stopButton = (Button) findViewById(R.id.stopButton);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        forwardButton.setOnClickListener(new OnClickListener()  //Если будет нажата кнопка 1 то
        {
            public void onClick(View v)
            {
                sendData("f");         // Посылаем цифру 1 по bluetooth
                Toast.makeText(getBaseContext(), "вперед", Toast.LENGTH_SHORT).show();  //выводим на устройстве сообщение
            }
        });

        backButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v)
            {
                sendData("b"); // Посылаем цифру 1 по bluetooth
                Toast.makeText(getBaseContext(), "назад", Toast.LENGTH_SHORT).show();
            }
        });
        rightButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v)
            {
                sendData("r"); // Посылаем цифру 1 по bluetooth
                Toast.makeText(getBaseContext(), "направо", Toast.LENGTH_SHORT).show();
            }
        });
        leftButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v)
            {
                sendData("l"); // Посылаем цифру 1 по bluetooth
                Toast.makeText(getBaseContext(), "налево", Toast.LENGTH_SHORT).show();
            }
        });
        stopButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v)
            {
                sendData("s"); // Посылаем цифру 1 по bluetooth
                Toast.makeText(getBaseContext(), "Стоп", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...onResume - попытка соединения...");
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        //Получаем MAC и ID или UUID сервиса. В данном случае UUID

        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

            // Убеждаемся, что ничего не происходит, когда соединяемся и пытаемся передать сообщение
        btAdapter.cancelDiscovery();

        // Устанавливаем соединение
        Log.d(TAG, "...Соединяемся...");
        try {
            btSocket.connect();
            Log.d(TAG, "...Соединение установлено и готово к передачи данных...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Создаем сокет
        Log.d(TAG, "...Создание Socket...");

        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
            }
        }

        try     {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void checkBTState() {
        // Проверяем работоспособность bluetooth
        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth не поддерживается");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth включен...");
            } else {
                //Запрос на включение bluetooth
                Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        Log.d(TAG, "...Посылаем данные: " + message + "...");

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            //  if (address.equals("98:D3:36:00:B7:E9"))
            //    msg = msg + ".\n\nВ переменной address у вас прописан 00:00:00:00:00:00, вам необходимо прописать реальный MAC-адрес Bluetooth модуля";
            // msg = msg +  ".\n\nПроверьте поддержку SPP UUID: " + MY_UUID.toString() + " на Bluetooth модуле, к которому вы подключаетесь.\n\n";

            errorExit("Fatal Error", msg);
        }
    }
}




