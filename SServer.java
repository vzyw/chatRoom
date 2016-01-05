package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class SServer{
	public static void main(String[] args) {
		Server server = Server.getInstance();
		new ServerThread(server).start();
		server.run();
	}
}
class ServerThread extends Thread{
	private Server server;
	public ServerThread(Server server) {
		this.server = server;
	}
	@Override
	public void run() {
		while(true){
			server.sendMessage();
			try {
				sleep(1000);
			} catch (InterruptedException e3) {}
		}
	}
}

class ClientThread extends Thread{
	 private DataInputStream    in;
	 private DataOutputStream   out;
	 private Socket socket;
	 private InetAddress address;
	 public boolean isStop;
	 
	 public ClientThread(Socket socket) {
		 this.socket = socket;
		 isStop = true;
		 address = null;
		 try {
			 in	 = new DataInputStream(socket.getInputStream());
		     out = new DataOutputStream(socket.getOutputStream()); 
		     address = socket.getLocalAddress();
		     Server.addMessage(address+":上线\n");
		     isStop = false;
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
	 }
	 @Override
	public synchronized void run() {
		while(true){
			try {
				if(isStop){
					socket.close();
					return;
				}
				socket.sendUrgentData(0xFF);
				String message = in.readUTF();
				System.out.println(message+".");
				if(message!=null)Server.addMessage(address + ":" +message+"\n");
			} catch (EOFException e1) {
				isStop = true;
				Server.onlineNums--;
				Server.addMessage(socket.getLocalAddress()+":下线\n");
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			
			try {
				sleep(200);
			} catch (InterruptedException e3) {}
		}
	}
	 
	public void send(String message) {
		if(!isStop){
			try {
				out.writeUTF(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
class Server{ 
	private static int 			port 		 = 2333;
	private static ServerSocket s_socket 	 = null;
	private static Server 		singleServer = null;

	private static LinkedBlockingQueue<String> messageQueue;
	private static ArrayList<ClientThread> clients;
	
	public static int onlineNums=0;
	private Server() {
		try{ 
			s_socket=new ServerSocket(port);
			System.out.println("服务器正在运行....");
		}catch(IOException e1){System.out.println("ERRO:"+e1);} 
		
		messageQueue = new LinkedBlockingQueue<>();
		clients  	 = new ArrayList<>();
	}
	
	public static Server getInstance(){
		if(singleServer==null){
			singleServer = new Server();
		}
		return singleServer;
	}
	
	public static void addMessage(String message) {
		messageQueue.add(message);
	}
	
	public void sendMessage(){
		String message = messageQueue.poll();
		if(message==null)return;
		for (ClientThread clientThread : clients) {
			if(clientThread.isStop) continue;
			clientThread.send(message);
		}
	}
	
	public void run(){
		Socket socket;
		while (true){
			try {
				socket = s_socket.accept();
				onlineNums++;
				System.out.println("当前在线人数:"+onlineNums);
				ClientThread client = new ClientThread(socket);
				client.start();
				clients.add(client);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}



