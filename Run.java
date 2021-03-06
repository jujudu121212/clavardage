package clavardage;

import java.awt.*;
import java.awt.event.WindowListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;

@SuppressWarnings("serial")
public class Run extends Frame
{
   private static Run r;
   private static JLabel lblInfo;
   private static JLabel lblError;
   private Dialog login;
   private TextField box;
   private static PreConnectDiscovery discovery;
   private static JRadioButton modeSelectionUDP;
   private static JRadioButton modeSelectionHTTP;
   private static JRadioButton modeSelectionBDD;
   private static JRadioButton modeSelectionFile;
   private static JLabel lblServerAddressHTTP;
   private static JLabel lblServerPortHTTP;
   private static TextField serverAddressHTTP;
   private static TextField serverPortHTTP;

   class MyButtonValidateListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
	    if(modeSelectionUDP.isSelected()) {
		String wantedPseudo = box.getText();
		if(wantedPseudo.equals("")) {
			lblError.setText("Impossible to login in with an empty pseudo !");
		} else {
			if(discovery.getOnlineUsers().contains(wantedPseudo)) {
				lblError.setText("Impossible to login ; " + wantedPseudo + " is already Online.");
			}
			else {
				try {
					discovery.closeCommunications();
				}
				catch(Exception ex){
					ex.printStackTrace();
				}
				finally {
					if(modeSelectionBDD.isSelected()) {
						new MainWindow(wantedPseudo,true,null,null,true); //true means UDP-Based network discovery
						r.dispose();
					}
					else {
						new MainWindow(wantedPseudo,true,null,null,false); //true means UDP-Based network discovery
						r.dispose();
					}
				}
			}
		}
		} else {
			//lblError.setText("Careful : beta version");	
			String wantedPseudo = box.getText();
			if(wantedPseudo.equals("")) {
				lblError.setText("Impossible to login in with an empty pseudo !");
			} else {
				try {
					String address = "http://" + serverAddressHTTP.getText() + ":" + serverPortHTTP.getText() + "/presenceserver/connect?display=false&type=isfree&pseudo=" + wantedPseudo;			
					System.out.println("Trying to connect to \"" + address + "\"");
					URL url = new URL(address);
					HttpURLConnection con = (HttpURLConnection) url.openConnection();
					int responseCode = con.getResponseCode();
					if(responseCode == 200) {
						BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
						System.out.println("Response code from server = " + responseCode);
						String resp = in.readLine();
						System.out.println("Response message from server = " + resp);
						in.close();
						con.disconnect();
						if(resp.equals("SERVER_REPLY:YES")) {
							try {
								discovery.closeCommunications();
							}
							catch(Exception ex){
								ex.printStackTrace();
							}
							finally {
								if(modeSelectionBDD.isSelected()) {
									new MainWindow(wantedPseudo,false,serverAddressHTTP.getText(), serverPortHTTP.getText(),true); //false means HTTP presence server based network discovery
									r.dispose();
								} else {
									new MainWindow(wantedPseudo,false,serverAddressHTTP.getText(), serverPortHTTP.getText(),false); //false means HTTP presence server based network discovery
									r.dispose();
								}
							}
						}
						else if(resp.equals("SERVER_REPLY:NO")) {
							lblError.setText("Impossible to login ; " + wantedPseudo + " is already Online.");
						}
						else {
							lblError.setText("Received unknown response from server");
						}
					}
					else {
						lblError.setText("Can not connect to server");
						System.out.println("Fatal error while connecting to server");
						System.out.println("RESTART APPLICATION or RESTART PERFORMING TASK");
					}
				} catch(Exception ex) {
					lblError.setText("Communication error : You might have misspelled server name or port");
					System.out.println("Communication error with HTTP presence server :");
					//ex.printStackTrace();
				}
			}
	    }
      }
   }

	public class httpListener implements ActionListener
   {
      private boolean firstSel = true;
      public void actionPerformed(ActionEvent e)
      {
	 		//lblError.setText("HTTP Mode selected");
			lblServerAddressHTTP.setEnabled(true);
			lblServerPortHTTP.setEnabled(true);
			serverAddressHTTP.setEnabled(true);
			serverPortHTTP.setEnabled(true);
			if(this.firstSel == true){
				this.firstSel = false;
				serverAddressHTTP.setText("localhost");
				serverPortHTTP.setText("8080");	
			}	
      }
   }

	public class udpListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
	 		//lblError.setText("UDP Mode selected");
			lblServerAddressHTTP.setEnabled(false);
			lblServerPortHTTP.setEnabled(false);
			serverAddressHTTP.setEnabled(false);
			serverPortHTTP.setEnabled(false);
      }
   }
	
   public class MyButtonExitListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
	 		try {
				discovery.closeCommunications();
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
         login.dispose();
         System.exit(0);
      }
   }

	WindowListener exitListener = new WindowAdapter() {

		@Override
		public void windowClosing(WindowEvent e) {
		     try {
				discovery.closeCommunications();
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
         login.dispose();
         System.exit(0);
		}
	};

   public Run()
   {
			this.setTitle("Chat Room");	
			discovery = new PreConnectDiscovery();					
			lblServerAddressHTTP = new JLabel("Enter HTTP presence Server Address :");
			serverAddressHTTP = new TextField();
			lblServerPortHTTP = new JLabel("Enter HTTP presence Server Port :");
			serverPortHTTP = new TextField();
			lblServerAddressHTTP.setEnabled(false);
			lblServerPortHTTP.setEnabled(false);
			serverAddressHTTP.setEnabled(false);
			serverPortHTTP.setEnabled(false);		
			modeSelectionUDP = new JRadioButton("Local Mode (UDP-Based)");
			modeSelectionHTTP = new JRadioButton("Distant Mode (HTTP-Based)");
			modeSelectionHTTP.addActionListener(new httpListener());
			modeSelectionUDP.addActionListener(new udpListener());
			ButtonGroup group = new ButtonGroup();
            		group.add(modeSelectionUDP);
			group.add(modeSelectionHTTP);
			//modeSelectionHTTP.setEnabled(false);
			modeSelectionUDP.setEnabled(true);
			modeSelectionHTTP.setEnabled(true);
			modeSelectionUDP.setSelected(true);
    			//modeSelectionUDP.setMnemonic(KeyEvent.VK_R);
   			//modeSelectionUDP.setActionCommand(rabbitString);
			/* SELECTION MODE D'HISTORIQUE */
			modeSelectionBDD = new JRadioButton("History save in a database (requires mySQL to be installed)");
			modeSelectionFile = new JRadioButton("Save history in File System");
			//modeSelectionBDD.addActionListener(new bddListener());
			//modeSelectionFile.addActionListener(new fileListener());
			ButtonGroup group2 = new ButtonGroup();
            		group2.add(modeSelectionBDD);
			group2.add(modeSelectionFile);
			//modeSelectionHTTP.setEnabled(false);
			modeSelectionBDD.setEnabled(true);
			modeSelectionFile.setEnabled(true);
			modeSelectionFile.setSelected(true);

			(new Thread(discovery)).start();
			box = new TextField();
            login = new Dialog(this);
			lblError = new JLabel("");
            lblInfo = new JLabel("Welcome. Enter your pseudo please.",
                  JLabel.CENTER); // Construct by invoking a constructor via the new
                                 // operator
            login.setLayout(new GridLayout(0, 1));
            JButton validate = new JButton("Validate Pseudo");        
            validate.addActionListener(new MyButtonValidateListener());
            JButton exit = new JButton("Quit");
            exit.addActionListener(new MyButtonExitListener());		   
            login.setSize(850, 400);
            login.add(lblInfo);   
			login.add(lblError);
			login.add(box);
            		login.add(validate);
			login.add(new JLabel("---- Network discovery mode selection : ----",SwingConstants.CENTER));
			login.add(modeSelectionUDP);   
			login.add(modeSelectionHTTP);  
			login.add(lblServerAddressHTTP);
			login.add(serverAddressHTTP);
			login.add(lblServerPortHTTP);
			login.add(serverPortHTTP);
			login.add(new JLabel("---- History saving mode selection ----",SwingConstants.CENTER));
			login.add(modeSelectionBDD);   
			login.add(modeSelectionFile); 
		        login.add(exit);    
			login.setVisible(true);
			login.addWindowListener(exitListener);
   }

	public static void main(String argv[]) {
		r = new Run();
	}
	
}
