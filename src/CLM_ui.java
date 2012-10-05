import java.util.Collection;
import java.util.Scanner;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;

public class CLM_ui {
	private static String resource = "192.168.200.34";
	private smack_connection connection;
	public CLM_ui(){
		
		this.connection = new smack_connection(resource , this);
		
		if(this.try_connection()){
			boolean flag = true;
			while(flag){
				//this.connection.disconnect();
				Scanner scanner = new Scanner(System.in);
				System.out.println("Please insert \"signup\", \"login\" or \"exit\" :");
				String str = scanner.next();
				if(str.equals("signup") || str.equals("login")){
					if(str.equals("signup"))while(!signup());
			    	else if(str.equals("login"))while(!login());
					this.connection.init_after_connection();
			    	boolean console_result = console();
			    	this.connection.disconnect();
			    	if(console_result)System.out.println("logout!");
			    	else flag = false;
				}
				else if(str.equals("exit"))flag = false; 
			}
		}
		System.out.println("Exit!");
	}
	public boolean try_connection(){
		int i = 5;
		boolean flag = false;
		while(!flag && i > 0){
			flag = this.connection.connect();
			if(flag)return true;
			System.out.println("Connection failed!");
			System.out.println("Reconnecting...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			i--;
		}
		return false;
	}
	public void connection_failed(){
		System.out.println("Connection failed!");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Reconnecting...");
	}
    public boolean console(){	// true for exit
		Scanner scanner = new Scanner(System.in);
		String str = "default";
		while (str != null){
			if(this.connection.check_chat()){
				System.out.print("Talk to " + this.connection.get_chat().get_entry().getName() + "\nor");
			}
			System.out.println("Insert \"/help\" for help :");
			str = scanner.next();
			if(str.equals("/help")){
				System.out.println("/account");
				System.out.println("/presence");
				System.out.println("/list");
				System.out.println("/offline");
				System.out.println("/request");
				System.out.println("/add");
				System.out.println("/accept");
				System.out.println("/chat");
				System.out.println("/logout");
				System.out.println("/exit");
			}
			else if(str.equals("/account")){
				if(!account())return true;
			}
			else if(str.equals("/presence"))presence();
			else if(
				str.equals("/list") ||
				str.equals("/offline") ||
				str.equals("/request")
			)list(str);
			else if(str.equals("/add"))add();
			else if(str.equals("/accept"))accept();
			else if(str.equals("/chat"))chat(str);
			else if(str.equals("/logout"))return true;
			else if(str.equals("/exit")) return false;
			else this.connection.send_message(str);
		}
		return false;
	}
	public boolean signup(){
		this.connection = new smack_connection(resource , this);
		this.connection.connect();
		Scanner scanner = new Scanner(System.in);
		String username;
		String password;
		String password2;
		System.out.print("Username : ");
		username = scanner.next();
		System.out.print("Password : ");
		password = scanner.next();
		System.out.print("Confirm password : ");
		password2 = scanner.next();
		if(!password.equals(password2)){
			System.out.println("Unverified password");
			return false;
		}
		boolean is_login=this.connection.signup(username,password);
		if(is_login){
			this.connection.login(username, password);
			System.out.println("Login as : "+username);
		}
		else System.out.println("Account not existed!");
		return is_login;
	}
	public boolean login(){
		this.connection = new smack_connection(resource , this);
		this.connection.connect();
		Scanner scanner = new Scanner(System.in);
		String username;
		String password;
		System.out.print("Username : ");
		username = scanner.next();
		System.out.print("Password : ");
		password = scanner.next();
		boolean is_login=this.connection.login(username,password);
		if(is_login)System.out.println("Login as : "+username);
		else System.out.println("Invalid username or password or account not enabled!");
		return is_login;
	}
	public boolean account(){
		Scanner scanner = new Scanner(System.in);
		boolean flag = true;
		while(flag){
			System.out.println("\"change\" for change password, \"delete\" for disaccount");
			String str = scanner.next();
			if(str.equals("change")){
				String password = null;
				String password2;
				boolean flag2 = true;
				while(flag2){
					System.out.print("Password : ");
					password = scanner.next();
					System.out.print("Confirm password : ");
					password2 = scanner.next();
					if(!password.equals(password2)){
						System.out.println("Unverified password");
					}
					else flag2 = false;
				}
				connection.change_password(password);
				flag = false;
			}
			else if(str.equals("delete")){
				connection.delete_account();
				flag = false;
				return false;
			}
		}
		return true;
	}
	public void presence(){
		Scanner scanner = new Scanner(System.in);
		boolean flag = true;
		while(flag){
			System.out.println("Insert command or 'h' for help :");
			String str = scanner.next();
			if(str.equals("h")){
				System.out.println("a : available - Available (the default)");
				System.out.println("w : away - Away");
				System.out.println("c : chat - Free to chat");
				System.out.println("d : dnd - Do not disturb.");
				System.out.println("s : Set status");
				System.out.println("us : Unset status");
				//System.out.println("x : xa - Away for an extended period of time");
			}
			else if(str.equals("a")||str.equals("w")||str.equals("c")||str.equals("d")){
				this.connection.set_presence_mode(str);
				flag = false;
			}
			else if (str.equals("s")){
				System.out.println("Inset status :");
				str = scanner.next();
				this.connection.set_presence_status(str);
				flag = false;
			}
			else if (str.equals("us")){
				this.connection.set_presence_status(null);
				flag = false;
			}
			else{
				System.out.println("Wrong command!");
			}
		}
	}
	public void list(String str){
		Collection<RosterEntry> entries = null;
		if(str.equals("/list"))entries = this.connection.get_list();
		else if(str.equals("/offline"))entries = this.connection.get_offline();
		else if(str.equals("/request"))entries = this.connection.get_request();
		for(RosterEntry entry : entries){
				System.out.print(entry.getUser()+" ");
				if(entry.getType() != null)System.out.print(entry.getType().toString()+" ");
				if(entry.getStatus() != null)System.out.print(entry.getStatus().toString()+" ");
				System.out.print("\n");
		}
		System.out.print("\n");
	}
	public void add(){
		this.connection.add_entry(null, null, null);
	}
	public void accept(){
		this.connection.accept_entry(null, null, null);
	}
	public void chat(String str){
		System.out.println("Insert the name of the user you want to talk to : ");
		Scanner scanner = new Scanner(System.in);
		str = scanner.next();
		while(!this.connection.set_chat_by_name_of_entry(str)){
			System.out.println("Username not found!");
			str = scanner.next();
		}
	}
	public static void echo_entriesDeleted(Collection<String> addresses) {
		System.out.println("entriesDeleted : "+addresses);
	}
	public static void echo_entriesUpdated(Collection<String> addresses) {
		System.out.println("entriesUpdated : "+addresses);
	}
	public static void echo_entriesAdded(Collection<String> arg0) {
		// Auto-generated method stub
		System.out.println("entriesAdded : "+arg0);
	}
	public static void echo_presenceChanged(Presence presence){
		System.out.println("Presence changed: " + presence.getFrom() + " " + presence);
	}
}
