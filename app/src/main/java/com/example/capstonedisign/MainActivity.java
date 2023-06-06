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
    String sended_string = "";
    boolean call_selected = false;
    Button call_btn;

    TextView show_text;

    TextView ex_txt;

    // about socket
    private Handler mHandler;

    private Socket socket;

    private DataOutputStream outstream;
    private DataInputStream instream;

    // 현재는 노트북 ip 주소, 추후 라즈베리파이 mac주소 들어갈 예정
    // 핫스팟 = 172.20.10.3, 192.168.166.136

    private String ip_net = "172.20.10.3" ;

    private String sensor_data = "";
    private int port = 9999;
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

        show_text = (TextView) findViewById(R.id.textView);
        ex_txt = (TextView) findViewById(R.id.textView2);
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
                            outstream.write(b1.array(), 0, 4);
                            outstream.write(data);

                            data = new byte[4];
                            instream.read(data, 0, 4);
                            ByteBuffer b2 = ByteBuffer.wrap(data);
                            b2.order(ByteOrder.LITTLE_ENDIAN);
                            int length = b2.getInt();
                            data = new byte[length];
                            instream.read(data, 0, length);

                            sended_string = new String(data, "UTF-8");
                            show_text.setText(sended_string);
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
                            Log.w("write","보냈음");
                            // outstream.write(data);

                            //data = new byte[4];
                            //instream.read(data, 0, 4);
                            //ByteBuffer b2 = ByteBuffer.wrap(data);
                            //b2.order(ByteOrder.LITTLE_ENDIAN);
                            // int length = b2.getInt();
                            Log.w("data","받기 준비");
                            int length = 4;
                            data = new byte[length];

                            instream.read(data, 0, length);
                            Log.w("read", "받았음");

                            sensor_data = new String(data, "UTF-8");
                            Log.w("sensor_Data", sensor_data);
                            ex_txt.setText(sensor_data);

                        }


                    }
                }catch(Exception e){

                }
            }
        };
        checkUpdate.start();
    }
}