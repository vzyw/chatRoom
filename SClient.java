/*  客户端程序 SClient.java  */
package network;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class SClient{
	public static void main(String[] args) {
		new Thread(new Window()).run();
	}
}


class Client{
	private static Client singleClient = null;
	private static String host = null;
	private static int    port = 0;
    private static String status= "无连接";

    private Socket             c_socket;
    private DataInputStream    in;
    private DataOutputStream   out;
    private Client() {
		try {
			c_socket = new Socket(host, port);
			in		 = new DataInputStream(c_socket.getInputStream());
		    out		 = new DataOutputStream(c_socket.getOutputStream()); 
		    status	 = "连接成功";
		} catch (Exception e) {
			status = e.getMessage();
		}
	}
    public static Client getInstance(String host,int port) {
		if(singleClient==null){
			Client.host = host;
			Client.port = port;
			singleClient = new Client();
		}	
		if(singleClient.c_socket==null)singleClient = null;
		return singleClient;
	}
    public void send(String message) {
    	try {
			out.writeUTF(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    public String getContent() {
    	String message = null;
    	//if(!c_socket.isConnected())return message;
		try {
			 message = in.readUTF();
		} catch (IOException e) {
			//e.printStackTrace();
		}
		return message;
	}
    public static String getStaut() {
		return status;
	}
    
    public void close() {
		try {
			//out.writeUTF("q");
			c_socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
    
}
 
class Window extends JFrame implements ActionListener,Runnable{
	private JTextField address,port;
	private JLabel _address,_port;
	private Client client;
	private JTextArea content,message;
	private JButton send,connect;
	public Window() {
		setTitle("聊天室");
		setSize(400, 500);
		setLayout(new BorderLayout());
		setResizable(false);
		initComponent();
		client = null;
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {  
				super.windowClosing(e);  
				if(client!=null)client.close();
			 }  
		});
	}
	//初始化所有组件
	private void initComponent(){
		//top panel
		_address = new JLabel("主机地址:");
		_port    = new JLabel("端口号:");
		address  = new JTextField("127.000.000.001");
		port	 = new JTextField("2333");
		connect  = new JButton("连接");
		connect.addActionListener(this);
		JPanel top = new JPanel();
		top.add(_address);
		top.add(address);
		top.add(_port);
		top.add(port);
		top.add(connect);
		this.add(top,BorderLayout.NORTH);
		
		//center panel
		content = new JTextArea(20,20);
		content.setEditable(false);
		content.setLineWrap(true);
		content.setMargin(new Insets(10, 10, 10, 10));
		JScrollPane scroll = new JScrollPane(content);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.add(scroll,BorderLayout.CENTER);
		
		//bottom panel
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
		message = new JTextArea(2,22);
		message.setMargin(new Insets(10, 10, 10, 10));
		send 	= new JButton("发送");
		send.setSize(500,500);
		send.addActionListener(this);
		bottom.add(message);
		bottom.add(send);
		this.add(bottom, BorderLayout.SOUTH);
		
	}

	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==connect){
			if(client!=null)return;
			client = Client.getInstance(address.getText(), Integer.parseInt(port.getText()));
			JOptionPane.showMessageDialog(this, Client.getStaut());
		}else if(e.getSource()==send){
			String str = message.getText();
			if(client==null){
				JOptionPane.showMessageDialog(this, Client.getStaut());
				return;
			}
			client.send(str);
			message.setText("");
		}
		
	}
	
	@Override
	public void run() {
		while(true){
			if(client!=null){
				String string = client.getContent();
				if(string!=null)content.append(string);
			}
				
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {}
		}
	}
}

