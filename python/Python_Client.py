import Image_pb2
import socket               
import time
import struct



def buildReadRequest( uuid ):
    
    r = Image_pb2.Request()    

    r.body.photoPayload.uuid = uuid
    r.header.photoHeader.requestType = Image_pb2.PhotoHeader.read    
    r.header.originator = 1 
    r.header.routing_id = Image_pb2.Header.JOBS
    
    msg = r.SerializeToString()
    return msg

def buildWriteRequest(uuid, imageName, imageData):
    
    r = Image_pb2.Request()    

    r.body.photoPayload.uuid = uuid
    r.body.photoPayload.name = imageName
    r.body.photoPayload.data = imageData
    r.header.photoHeader.requestType = Image_pb2.PhotoHeader.write    
    r.header.originator = 1  
    r.header.routing_id = Image_pb2.Header.JOBS
    
    msg = r.SerializeToString()
    return msg

   


def sendMsg(msg_out, port, host):
    s = socket.socket()         
    s.connect((host, port))        
    msg_len = struct.pack('>L', len(msg_out))    
    s.sendall(msg_len + msg_out)
    len_buf = receiveMsg(s, 4)
    msg_in_len = struct.unpack('>L', len_buf)[0]
    msg_in = receiveMsg(s, msg_in_len)
    
    r = Image_pb2.Request()
    r.ParseFromString(msg_in)
#    print msg_in
#    print r.body.job_status 
#    print r.header.reply_msg
#    print r.body.job_op.data.options
    s.close
    return r

def receiveMsg(socket, n):
    buf = ''
    while n > 0:        
        data = socket.recv(n)                  
        if data == '':
            raise RuntimeError('data not received!')
        buf += data
        n -= len(data)
    return buf  

        
   
if __name__ == '__main__':
    # msg = buildPing(1, 2)
    # UDP_PORT = 8080
    # serverPort = getBroadcastMsg(UDP_PORT) 
    
    host = raw_input("Input Host IP:")
    port = raw_input("Input Host Port:")
    port = int(port)
    
    while True:
        input = raw_input("*******Welcome to our python client! \n*********Kindly select your desirable action:\n0.Quit\n1.Read \n2.Write\n")
        if input == "1":
            print("Pleas input information for read ===========")
            uuid = raw_input("UUID:")
            readRequest = buildReadRequest(uuid) 
            result = sendMsg(readRequest, port, host)
            print("Got Read Feedback ===========")
            print result.body.photoPayload.name
            print result.header.photoHeader.requestType
            print result.header.photoHeader.responseFlag
        if input == "2":  
            print("Pleas input information for write ===========")      
            uuid = raw_input("UUID:")
            imageName = raw_input("Image Name:")
            imagePath = raw_input("Image Path:")
            imageFin = open(imagePath, "rb")
            imageData = imageFin.read()
            readRequest = buildWriteRequest(uuid, imageName, imageData)
            result = sendMsg(readRequest, port, host)
            
            print("Got Write Feedback ===========")
            print result.body.photoPayload.name
            print result.header.photoHeader.requestType
            print result.header.photoHeader.responseFlag
        if input == "0":
            print("Thanks for using our client! See you soon ...")
            break
   
