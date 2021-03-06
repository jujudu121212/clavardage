package clavardage;
import java.net.DatagramSocket ;
import java.util.HashMap; 
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.lang.String;
import java.util.Set;
import java.util.Iterator;
import java.util.HashSet;
/*
Conventions (types de messages)
-> Se déclarer en ligne : [001]_Pseudo
-> Réponse à une déclaration en ligne : [011]_Pseudo
-> Déclaration d'un nouveau pseudo : [021]_NouveauPseudo //Ancien pseudo déterminé par l'addresse !
-> Se déconnecter : [002]_Pseudo

*/

public class PreConnectDiscovery implements Runnable {
	private DatagramSocket socket;
	private HashMap<String,InetAddress> onlineUsers = new HashMap<String,InetAddress>();
	private InetAddress broadcastAddress;
	private DatagramPacket myPacket;
	private DatagramPacket rcvPacket;
	byte[] sendBuf;
	byte[] rcvBuf = new byte[256];
	private boolean isActiveManager = false;
	String message;

	public Set<String> getOnlineUsers() {
	   /* if(hasBeenModified == true) {
	      synchronized(this.hasBeenModified) {
			this.hasBeenModified = false;
		   }*/
   		synchronized(onlineUsers) {
			Set<String> s = new HashSet<String>(onlineUsers.keySet());
   			return s;
   		}
	 /*  }
	   else {
	   	return null;
		}*/
	}
	
	public void closeCommunications() { // To properly close the socket
		this.isActiveManager = false;
		socket.close();
		System.out.println("Datagram socket successfully closed");
	}	


	public PreConnectDiscovery() {
		try {
			this.isActiveManager = true;
			message = new String("[000]");
			sendBuf = message.getBytes();
			broadcastAddress = InetAddress.getByName("255.255.255.255");
			myPacket = new DatagramPacket(sendBuf, sendBuf.length, broadcastAddress, 4444);
			rcvPacket = new DatagramPacket(rcvBuf, 0, rcvBuf.length);
			socket = new DatagramSocket(4444);
			socket.send(myPacket);
			System.out.println("PreConnect Discovery : Datagram socket successfully created");
		}
		catch(Exception e) {
			System.out.println("Error while creating DatagramSocket");
		}
	}

	public void run() {
		System.out.println("Network Discovery active");
		try {
			while(this.isActiveManager) {
			socket.receive(rcvPacket);
			String received = new String(rcvPacket.getData(), 0, rcvPacket.getLength());
			System.out.println("[PND] received :" + received);
//!(rcvPacket.getAddress()).equals("localhost")
				if(!(received.equals("[000]"))) {
					if((received.substring(0,6)).equals("[001]_")) { //Déclaration utilisateur en ligne*
						System.out.println(received.substring(6) + " is online");
						synchronized(onlineUsers) {
						onlineUsers.put(received.substring(6),rcvPacket.getAddress());
						}
					}
					if((received.substring(0,6)).equals("[011]_")) { //Déclaration utilisateur en ligne
						System.out.println(received.substring(6) + " is already online");
						synchronized(onlineUsers) {
						if(!onlineUsers.containsKey(received.substring(6))) {
							onlineUsers.put(received.substring(6),rcvPacket.getAddress());
						}
						}
					}
					if((received.substring(0,6)).equals("[002]_")) { //Déclaration déconnexion utilisateur
						System.out.println(received.substring(6) + " is deconnected");
						synchronized(onlineUsers) {			
							onlineUsers.remove(received.substring(6));
						}
					}
					if((received.substring(0,6)).equals("[021]_")) { //Déclaration changement de pseudo
						// Find corresponding old pseudo
						try {
							InetAddress hostAddr = rcvPacket.getAddress();
							Set<String> usersOnTable = onlineUsers.keySet();
							Iterator<String> it = usersOnTable.iterator();
							String theUser = "shouldchange";
							while(it.hasNext()) {	
									String aUser = it.next();
									if((onlineUsers.get(aUser)) != null) {	
										if((onlineUsers.get(aUser)).equals(hostAddr)) {	
											theUser = aUser;
										}
									}
							}
							synchronized(onlineUsers) {									
								System.out.println(theUser + "addr = " + (onlineUsers.get(theUser)).toString() );																		
								onlineUsers.remove(theUser);
								onlineUsers.put(received.substring(6),hostAddr);
							}
							System.out.println(received.substring(6) + " new pseudo");
						}
						catch(Exception e) {
							System.out.println("Error while updating a pseudo...");
							e.printStackTrace();
						}
					}
				}
			}
		}	
		catch(java.net.SocketException exe) {
			//NOTHING, GREAT
		}
		catch(Exception e) {
			System.out.println("Error while sending UDP packet");
			e.printStackTrace();
		}
		while(true) {
			//System.out.println("Test");
		}
	}

}
