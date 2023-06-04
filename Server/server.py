@@ -0,0 +1,92 @@
import multiprocessing as mp
import time
import socket
import connect
import example

def server_connect(list, q):
    # for x in range(1,10):
    #     print("1")
    #     time.sleep(1)        
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
            # 클라이언트에서 문자열 받기
            msg = client_sock.recv(length)
 
            # data를 더 이상 받을 수 없을 때
            if len(data) <= 0:
                break
 
            msg = msg.decode()
            # 각 버튼 별로 해당하는 함수 호출
            if msg == "call":
                q.put(msg)
            
            elif msg == "trash":
                q.put(msg)
            

            print(msg)
            data = msg.encode()
 
            length = len(data)
            # 클라이언트에 문자열 길이 보내기
            client_sock.sendall(length.to_bytes(4, byteorder="little"))
            # 클라이언트에 문자열 보내기
            client_sock.sendall(data)
 
        client_sock.close()
    print("프로그램 종료")
    server_sock.close()
    
    
# 라즈베리파이 연결 부분
# queue 연산하는 방식을 수정해야함, queue를 호출하면 나온 데이터는 삭제가 되도록
def Pi_connect(list, q):
    while(1):
        
        try:
            text = q.get()
            if text == "call":
                # 자율 주행 실행
                print("call 호출")
                example.call_ex()
                
            elif text == "trash":
                # 내부 잔여량 측정
                print("trash 호출")
                example.trash_ex(text)
                
            
            
        except Exception as e:
            print(e)
            time.sleep(1)
            
if __name__ == "__main__":
    q = mp.Queue()
    p0 = mp.Process(target=server_connect, args = (["call","trash"],q))
    p1 = mp.Process(target=Pi_connect, args = (["call","trash"],q))
    p0.start()
    p1.start()
    p0.join()
    p1.join()
    print("main process is done")
