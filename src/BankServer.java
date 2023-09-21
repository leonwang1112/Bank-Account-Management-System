import java.awt.Color;
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import javax.swing.*;


@SuppressWarnings("serial")
public class BankServer extends JFrame implements Runnable {
  private JTextArea logging; // JTextArea for back-end displaying
  private int clientNo = 0;
  public BankServer() {
	  logging = new JTextArea(10,10);
	  logging.setFont(new java.awt.Font("Mufferaw",2,15)); 
	  logging.setForeground(Color.blue);
	  logging.setEditable(false);
	  JScrollPane sp = new JScrollPane(logging);
	  this.add(sp);
	  this.setTitle("Bank Server");
	  this.setSize(800,500);
	  this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	  this.setVisible(true);
	  Thread t = new Thread(this);
	  t.start();
  }

  @SuppressWarnings("resource")
  public void run() {
	  try {
        ServerSocket serverSocket = new ServerSocket(8000);
        logging.append("Bank Server started at "+ new Date() + '\n');
    
        while (true) {
          Socket socket = serverSocket.accept();
          clientNo++;
          logging.append("Starting thread for client " + clientNo +
              " at " + new Date() + '\n');
        InetAddress inetAddress = socket.getInetAddress();
        logging.append("Client " + clientNo + "'s host name is "
          + inetAddress.getHostName() + "\n");
        logging.append("Client " + clientNo + "'s IP Address is "
          + inetAddress.getHostAddress() + "\n");
        new Thread(new HandleAClient(socket, clientNo)).start(); //start a new thread for connection
        }
      }
      catch(IOException ex) {
        System.err.println(ex);
      }
  }

  class HandleAClient implements Runnable {
    private Socket socket; 
    private int userNum;
    private ResultSet resultSet = null;
    private DataInputStream inputFromClient;
    private DataOutputStream outputToClient;
    private PreparedStatement insertStatement, queryStatement, updateStatement; 
    private PreparedStatement insertRegistrationStatement;
    private Connection con;
    private String response;
    private String query;
    private String accountNum;
    private String userName;
    private String phone;
    private String money;
    private String out;
    private Boolean finished = false;
	
    public HandleAClient(Socket socket, int userNum) {
      this.socket = socket;
      this.userNum = userNum;
    }

    public void run() {
    	try {
		    // Connect to a database
		    this.con = DriverManager.getConnection
		      ("jdbc:sqlite:loggingDB.db");
		    System.out.println("Database for Client" + userNum + " Connected");
		} catch (Exception e) {
			System.out.println("Database for Client" + userNum + " Connection failed");
		}
		
		/* sets up the prepared statement for each chats to be inserted into DB */
		String insertSQL = "Insert Into Logging (userId,query,response,date) " +
				"Values (?,?,?,?)";
		try {
			insertStatement = con.prepareStatement(insertSQL);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Logging insertStatement failed");

		}

		String insertRegistrationSQL = "Insert Into Registration (accountNum,userName,phone,money)"
				 + "Values (?,?,?,?)";
		try {
			insertRegistrationStatement = con.prepareStatement(insertRegistrationSQL);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Registration insertStatement failed");
		}

		String updateSQL = "update Registration set money = ? where accountNum = ?";
		try {
			updateStatement = con.prepareStatement(updateSQL);
		}catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Registration updateStatement failed");
		}	
		
        try {
	        inputFromClient = new DataInputStream(
	          socket.getInputStream());
	        outputToClient = new DataOutputStream(
	          socket.getOutputStream());
	        while (true) {
	          query = inputFromClient.readUTF();
	          
	          //print instructions when user types in help
	         if(query.contains("help")) {
	        	  response = " Enter 'new' to create a new account\n"
	        			  	+" Enter 'account' to get detail of selected account\n"
	        			  	+" Enter 'add' to add money to selected account\n"
	        			  	+" Enter 'withdraw' to withdraw money from selected account\n";
	        	  outputToClient.writeUTF(response);
	          }
	         
	         //create new account when key word new is included
	         else if(query.contains("new")) {
	        	 response = " Let's start! What is your account number?" ;      
	      		 outputToClient.writeUTF(response);
	      		 createAccount();
	         }
	         
	         //invoke addMoney when keyword add is included
	         else if(query.contains("add")) {
	        	 //response = " Enter the value you want to add to your account" ;  
	        	 response = " What is your account number?" ;
	      		 outputToClient.writeUTF(response);
	      		 query = inputFromClient.readUTF();
	      		 addMoney(query);
	         }
	         
	         //invoke withdrawMoney when keywod withdraw is included
	         else if(query.contains("withdraw")) {
	        	 response = " What is your account number?" ;
	      		 outputToClient.writeUTF(response);
	      		 query = inputFromClient.readUTF();
	      		 withdrawMoney(query);
	         }
	         
	         //when account is typed in, invoke checkAccount to display account information
	          else if(query.contains("account")) {
	        	  response = " What is your account number?" ;      
	      		  outputToClient.writeUTF(response);
	      		  query = inputFromClient.readUTF();
	        	  checkAccount(query); 
              }

	         //if no instruction detected, display message
              else {
				response = "No such instruction. Please try again.";
				outputToClient.writeUTF(response); 
		      }
	          logging.append("Query received from user " + this.userNum + " : " + query + '\n');
	          logging.append("Bot response to user" + this.userNum + " : " + response + '\n'); 
            }
         }catch(IOException ex) {
	    	 System.out.println("Trade failed or ended");
        }
     }
    
	public synchronized void insertDB(String userId, String query, String response, Date date) {

      try {
  		insertStatement.setString(1, userId);
		insertStatement.setString(2, query);
		insertStatement.setString(3, response);
		insertStatement.setString(4, date.toString());
		insertStatement.execute();
  		}catch (Exception e) {
  			System.out.println("LoggingDB Insertion Failed");
  		}         	       
	 }
	 
	public synchronized void insertRegistrationDB(String accountNum, String userName, String phone, 
			String money) {
		try {
    	  insertRegistrationStatement.setString(1, accountNum);
    	  insertRegistrationStatement.setString(2, userName);
    	  insertRegistrationStatement.setString(3, phone);
    	  insertRegistrationStatement.setString(4, money);
    	  insertRegistrationStatement.execute();
  		}catch (Exception e) {
  			System.out.println("Registration Insertion Failed");
  		}         	       
	 }
	
	public synchronized void updateDB(String accountNum, String money) {
		try {
			updateStatement.setString(1, money);
			//System.out.println("money is " + money);
			updateStatement.setString(2, accountNum);
			//System.out.println("account num is " + accountNum);
			updateStatement.executeUpdate();
		}catch (Exception e) {
  			System.out.println("Registration Update Failed");
  		}      
	}
	
	public synchronized void createAccount() throws IOException {
		while(!finished) {
			
			query = inputFromClient.readUTF();
			
			while(!isNumeric(query)) {
				response = "It is not number, please re-enter your account number.";
				outputToClient.writeUTF(response);
				query = inputFromClient.readUTF();
			}
			accountNum = query;
			
			response = "What is your name?";
			outputToClient.writeUTF(response);
			query = inputFromClient.readUTF();
			userName = query;

			response = "What is your phone number?";
			outputToClient.writeUTF(response);
			query = inputFromClient.readUTF();
			while(!isNumeric(query)) {
				response = "It is not a number, please re-enter.";
				outputToClient.writeUTF(response);
				query = inputFromClient.readUTF();
			}
			phone = query;
			
			response = "What is your deposit in your account?";
			outputToClient.writeUTF(response);
			query = inputFromClient.readUTF();
			while(!isNumeric(query)) {
				response = "It is not a number, please re-enter.";
				outputToClient.writeUTF(response);
				query = inputFromClient.readUTF();
			}
			money = query;
			
			response = "Information receivded. Account created. Thank you!\n"
						+ "Your account number is " + accountNum + "\n"
						+ "You have $" + money + " in your account.\n"; 
			outputToClient.writeUTF(response);
			if(money != null) {
				break;
			}
			}
			insertRegistrationDB(accountNum, userName, phone, money);
	}
	
	public synchronized void addMoney(String c) throws IOException {
		
		String sqlQuery = "select * from Registration where accountNum "
  			    + " = " + "'" + c + "'";
		System.out.println(sqlQuery);
		response = " Enter the value you want to add to your account" ;      
 		outputToClient.writeUTF(response);
 		query = inputFromClient.readUTF();
 		
    	  try {
    		queryStatement = con.prepareStatement(sqlQuery );
 		  } catch (SQLException e) {
 			 response = "No connecting to database!";
			 outputToClient.writeUTF(response);
 		  }
          try {
        	  resultSet = queryStatement.executeQuery();
		  } catch (SQLException e) {
			  response = "No connecting to database!";
			  outputToClient.writeUTF(response);
		  }
          try {
        	  accountNum = resultSet.getString(1);
        	  money = resultSet.getString(4);
        	  money = String.valueOf(Integer.parseInt(money) + Integer.parseInt(query));
          }catch (Exception e) {
 			 response = "Query Failed, please re-check your account number and start again!";
 			 outputToClient.writeUTF(response);
          }
          updateDB(accountNum,money);
          response = "Add value complete! " + query + "$ is added to your account!";
          outputToClient.writeUTF(response);
	}
	
	public synchronized void withdrawMoney(String c) throws IOException{
		
		String sqlQuery = "select * from Registration where accountNum "
  			    + " = " + "'" + c + "'";
		response = " Enter the value you want to withdraw from your account" ;      
 		outputToClient.writeUTF(response);
 		
    	  try {
    		queryStatement = con.prepareStatement(sqlQuery );
 		  } catch (SQLException e) {
 			 response = "No connecting to database!";
			 outputToClient.writeUTF(response);
 		  }
          try {
        	  resultSet = queryStatement.executeQuery();
		  } catch (SQLException e) {
			  response = "No connecting to database!";
			  outputToClient.writeUTF(response);
		  }
          try {
        	  while(true) {
        		  query = inputFromClient.readUTF();
        		  accountNum = resultSet.getString(1);
        		  money = resultSet.getString(4);
        		  if (Integer.parseInt(money) - Integer.parseInt(query) <0) {
        			  response = "Cannot withdraw more money than you have in your account!";
        			  outputToClient.writeUTF(response);
        		  }
        		  else {
        			  break;
        		  }
        	  }
        	  money = String.valueOf(Integer.parseInt(money) - Integer.parseInt(query));
          }catch (Exception e) {
 			 response = "Query Failed, please re-check your account number and start again!";
 			 outputToClient.writeUTF(response);
          }
          updateDB(accountNum,money);
          response = "Withdraw value complete! " + query + "$ is withdrawed from your account!";
          outputToClient.writeUTF(response);
	}
	
	public synchronized boolean isNumeric(String strNum) {

	    if (strNum == null) {
	        return false;
	    }
	    try {
	    	Long l = Long.parseLong(strNum);
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	    return true;
	}
	public synchronized void checkAccount(String c) throws IOException {

		String sqlQuery = "select * from Registration where accountNum "
  			    + " = " + "'" + c + "'";

    	  try {
    		queryStatement = con.prepareStatement(sqlQuery );
 		  } catch (SQLException e) {
 			 response = "No connecting to database!";
			 outputToClient.writeUTF(response);
 		  }
          try {
        	  resultSet = queryStatement.executeQuery();
		  } catch (SQLException e) {
			  response = "No connecting to database!";
			  outputToClient.writeUTF(response);
		  }
          try {
			while (resultSet.next()) {
				out = "\nAccount number = " + resultSet.getString(1) + "\n" + "User name = " + resultSet.getString(2) + "\n" 
						+ "Phone number = " + resultSet.getString(3) + "\n" +  "Value = " + resultSet.getString(4) + "\n";}
						outputToClient.writeUTF(out);
		  } catch (Exception e) {
			 response = "Query Failed, please re-check your account number and start again!";
			 outputToClient.writeUTF(response);
		}

	}
	}

  public static void main(String[] args) {
    new BankServer(); 

	
  }
}