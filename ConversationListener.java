package clavardage;

import java.awt.*;
import java.io.IOException;
import java.net.Socket;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import javax.swing.JLabel;

public class ConversationListener implements Runnable {
private BufferedReader in;
private Socket distantSocket; 
private TextArea messagesDisplay;
private JLabel lblInfos;
private boolean active;
private boolean once = true;
private History myHist;

	public boolean isActive() {
		return this.active;
	}

	public ConversationListener(Socket mysock, TextArea tA, JLabel l, History h){
		this.myHist = h;
		this.messagesDisplay = tA;
		this.lblInfos = l;
		//this.dW = dialogW;
		this.distantSocket = mysock;
		try {
			this.in = new BufferedReader(new InputStreamReader(distantSocket.getInputStream()));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			this.active = false;
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	public String receive() {
		String input = null;
		try {
		    	input = in.readLine(); // ici bloquant
				} catch (IOException e) {
					System.err.println("An error occured while attempting to read from socket. stopping listener loop");
					this.active = false;
					//e.printStackTrace();
				}
		if(input == null) {
		 this.active = false;
		 }
		else {
		System.out.print("Message reçu : ");  
		System.out.println(input);  
		}
		return input;
	}


	@Override
	public void run() {
		this.active = true;
		while(active){
			// Nothing	
			String r = receive();
			if(r != null) {
		  	 	 String received = "[" + (new java.util.Date()).toString() + "] " + r;	
				/* Label displ = new Label(received);                
                 dW.add(displ);*/
				synchronized(messagesDisplay) {
					messagesDisplay.append("Received: " + received + "\n");
				}
				myHist.updateHist("Received: " + received);
                 //dW.setSize(850, dW.getHeight()+20);
			}
		}
		if(once) {
			System.out.println("conversation listener not active");
			lblInfos.setText(lblInfos.getText() + " DISTANT USER DISCONNECTED");
			once = false;
		}	
	}
}
