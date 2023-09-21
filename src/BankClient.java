import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

@SuppressWarnings("serial")
class BankClient extends JFrame{
	
	private DataOutputStream toServer = null;
	private DataInputStream fromServer = null;
	private Socket socket = null;
	private JTextArea chatArea;
	private JTextField inputField;
	private JButton sendButton;
	private JPanel inputPanel;
	private JScrollPane scrollPane;
	
	public BankClient() throws IOException {
		  try {
			  socket = new Socket("localhost", 8000);
		      // Create an input stream to receive data from the server
		      fromServer = new DataInputStream(socket.getInputStream());
		      // Create an output stream to send data to the server
		      toServer = new DataOutputStream(socket.getOutputStream());
		    }
		    catch (IOException ex) {
		    }
		  
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(800,500);
		this.setTitle("Bank Simulator");
		this.setResizable(false);
		this.getContentPane().setBackground(Color.WHITE);
	    ChatArea(); 
		Input();
		this.add(scrollPane,BorderLayout.CENTER);
		this.add(inputPanel,BorderLayout.SOUTH);
	    this.setVisible(true);
	    
	  
	}
	private void ChatArea() {
		
		chatArea = new JTextArea(100,30);
		chatArea.setBackground(Color.white);
		chatArea.setFont(new java.awt.Font("Serif", 7, 22)); 
		chatArea.setForeground(Color.black);
		chatArea.setText("Welcome to the Bank Simulator! \n"
				+ "Enter 'new' to open a new account!\n"
				+ "Enetr 'help' to get more instructions\n");
		chatArea.setEditable(false);
		scrollPane = new JScrollPane(chatArea);
	}
	private void Input() {
		sendButton = new JButton("SEND");
		inputField = new JTextField(15);
		inputPanel = new JPanel();
		inputField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				
			}
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_ENTER) {
					sendButton.doClick();
				}		
			}
			@Override
			public void keyReleased(KeyEvent e) {
				
			}
			
		});
		sendButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				 try {
					 if((e.getSource()==sendButton)&&(!inputField.getText().equals(""))) {
							String userChat = inputField.getText().toLowerCase().trim();
							userChat = userChat.replaceAll("\\p{Punct}", "");
							chatArea.append("User: " + inputField.getText() + "\n");
							toServer.writeUTF(userChat);
							toServer.flush();
							inputField.setText("");
						    botReply(fromServer.readUTF());	
					    }
				      }
				      catch (IOException ex) {
				        System.err.println(ex);
				      }
			       }
		        });
		inputPanel.add(inputField);
		inputPanel.add(sendButton);
	}
	
	private void botReply(String reply) {
		chatArea.append("Computer: " + reply + "\n");
	}
	public static void main(String[] args) throws IOException {
    	 new BankClient();
    }
    
}


