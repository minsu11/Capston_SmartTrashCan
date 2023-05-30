package com.example.capstonedisign;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
public class MainActivity extends AppCompatActivity {

    int sended_num=0;
    boolean selected = false;
    Button connect_btn;
    Button plus_btn, minus_btn;
    EditText ip_edit;
    TextView show_text;

    // about socket
    private Handler mHandler;

    private Socket socket;

    private DataOutputStream outstream;
    private DataInputStream instream;

    private int port = 9999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connect_btn = (Button) findViewById(R.id.button);
        connect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });

        plus_btn = (Button) findViewById(R.id.plus_btn);
        plus_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected = true;
                sended_num += 1;
            }
        });
        minus_btn = (Button) findViewById(R.id.minus_btn);
        minus_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected = true;
                sended_num -= 1;
            }
        });

        ip_edit = (EditText) findViewById(R.id.editText);
        show_text = (TextView) findViewById(R.id.textView);
    }

    void connect(){
        mHandler = new Handler(Looper.getMainLooper());
        Log.w("connect","연결 하는중");

        Thread checkUpdate = new Thread(){
            public void run(){
                // Get ip
                String newip = String.valueOf(ip_edit.getText());

                // Access server
                try{
                    socket = new Socket(newip, port);
                    Log.w("서버 접속됨", "서버 접속됨");
                }catch (IOException e1){
                    Log.w("서버 접속 못함", "서버 접속 못함");
                    e1.printStackTrace();
                }

                Log.w("edit 넘어가야 할 값 : ","안드로이드에서 서버로 연결 요청");

                try{
                    outstream = new DataOutputStream(socket.getOutputStream());
                    instream = new DataInputStream(socket.getInputStream());
                    outstream.writeUTF("안드로이드에서 서버로 연결 요청");
                }catch(IOException e){
                    e.printStackTrace();
                    Log.w("버퍼","버퍼 생성 잘못 됨");
                }
                Log.w("버퍼","버퍼 생성 잘 됨");

                try{
                    while(true){
                        String msg = "java test message - ";
                        if (selected == true){
                            msg = msg + sended_num;
                            byte[] data = msg.getBytes();
                            ByteBuffer b1 = ByteBuffer.allocate(4);
                            b1.order(ByteOrder.LITTLE_ENDIAN);
                            b1.putInt(data.length);
                            outstream.write(b1.array(),0,4);
                            outstream.write(data);

                            data = new byte[4];
                            instream.read(data,0,4);
                            ByteBuffer b2 = ByteBuffer.wrap(data);
                            b2.order(ByteOrder.LITTLE_ENDIAN);
                            int length = b2.getInt();
                            data = new byte[length];
                            instream.read(data,0,length);

                            msg = new String(data,"UTF-8");
                            show_text.setText(msg);
                            selected = false;
                        }
                    }
                }catch(Exception e){

                }
            }
        };
        checkUpdate.start();
    }
}