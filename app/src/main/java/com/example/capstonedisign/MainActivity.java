package com.example.capstonedisign;
import androidx.appcompat.app.AppCompatActivity;

import java.util.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
    String sended_string = "";
    boolean call_selected = false;
    Button call_btn;

    ProgressBar progressBar;

    TextView Trash_txt;

    // about socket
    private Handler mHandler;

    private Socket socket;

    private DataOutputStream outstream;
    private DataInputStream instream;

    private String ip_net = "192.168.166.136";

    private int port = 9999;

    private String sensor_data = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        server_connect();

        call_btn = (Button) findViewById(R.id.call_btn);
        call_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                call_selected = true;
                sended_string = "call";
            }
        });
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        Trash_txt = (TextView) findViewById(R.id.TrashView);
    }

    void server_connect(){
        System.out.println("확인");
        mHandler = new Handler(Looper.getMainLooper());
        Log.w("connect","연결 하는중");
        Thread checkUpdate = new Thread(){
            public void run(){

                // Access server
                try{
                    socket = new Socket(ip_net, port);
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

                        if (call_selected == true) { //
                            byte[] data = sended_string.getBytes();
                            ByteBuffer b1 = ByteBuffer.allocate(4);
                            b1.order(ByteOrder.LITTLE_ENDIAN);
                            b1.putInt(data.length);
                            outstream.write(data);

                            int length = 4;
                            data = new byte[length];
                            instream.read(data, 0, length);

                            sended_string = new String(data, "UTF-8");
                            call_selected = false;
                        }
                        // 쓰레기 내부량
                        else{
                            Log.w("sensor","보냄");
                            sensor_data = "sens";
                            byte[] data = sensor_data.getBytes();
                            ByteBuffer b1 = ByteBuffer.allocate(4);
                            b1.order(ByteOrder.LITTLE_ENDIAN);
                            b1.putInt(data.length);
                            outstream.write(b1.array(), 0, 4);

                            int length = 4;
                            data = new byte[length];
                            instream.read(data, 0, length);

                            sensor_data = new String(data, "UTF-8");
                            Trash_txt.setText(sensor_data);
                            int Int_sensor = Integer.valueOf(sensor_data);
                            progressBar.setProgress(Int_sensor);

                        }


                    }
                }catch(Exception e){

                }
            }
        };
        checkUpdate.start();
    }
}