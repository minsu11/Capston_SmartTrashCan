import multiprocessing as mp
import socket
import subprocess
import serial

def server_connect(list, q):   
    host = ''
    port = 9999
 
    server_sock = socket.socket(socket.AF_INET)
    server_sock.bind((host, port))
    server_sock.listen(1)
 
    while True:
        print("기다리는 중")
        client_sock, addr = server_sock.accept()
 
        print('Connected by', addr)
        data = client_sock.recv(1024)
        data = data.decode()
        print(data)
 
        while True:
            # 클라이언트에서 받을 문자열의 길이
            data = client_sock.recv(4) 
            length = int.from_bytes(data, "little")
            
            # data를 더 이상 받을 수 없을 때
            if len(data) <= 0:
                break
            
            msg = data.decode()
            # 각 버튼 별로 해당하는 함수 호출
            if msg == "call":
                q.put(msg)
                sensor_data = list.get()
            else:
                sensor_data = list.get()
      
            msg = str(sensor_data).strip()
            data = msg.encode()
            length = len(data)

            # 클라이언트에 문자열 보내기
            client_sock.sendall(data)
        client_sock.close()
    print("프로그램 종료")
    server_sock.close()
    
    

def Pi_connect(list, q):
    while(1):
        try:
            text = q.get()
            if text == "call": # call 버튼 호출 시
                subprocess.run(["python3","detect.py","--weights",
                                "./runs/train/gun_yolov5s_results/weights/best.pt", "--img", "416", "--conf", 
                                "0.5", "--source","0"], capture_output=True)
        except Exception as e:
            print("error")

def Ardu(list, Ardu_q):
    com = serial.Serial("/dev/ttyACM0", 9600,)
    while True:
    
        data = com.readline().decode()
        Ardu_q.put(data)
        print(data)

if __name__ == "__main__":
    q = mp.Queue()
    Ardu_q = mp.Queue()
    p2 = mp.Process(target=Ardu, args= (["call"],Ardu_q))
    p0 = mp.Process(target=server_connect, args = (Ardu_q,q))
    p1 = mp.Process(target=Pi_connect, args = (["call"],q))
    p2.start()
    p0.start()
    p1.start()
    p0.join()
    p1.join()
    p2.join
    print("main process is done")
