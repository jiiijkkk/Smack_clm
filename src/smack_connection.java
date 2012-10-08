import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;

public class smack_connection {
	private Connection connection;
	private String resource;
	private String username;
	private String password;
	private CLM_ui ui;
	private ConnectionListener connection_listener;
	private AccountManager account_manager;
	private Presence my_presence;
	private Roster roster;
	private RosterListener roster_listener;
	private Collection<RosterEntry> entries;
	private smack_chat chat;
	private Collection<smack_chat> chats;
	private ChatManager chatmanager;
	public smack_connection(String resource , CLM_ui ui){
	   	// Create the configuration for this new connection
		this.resource=resource;
	   	ConnectionConfiguration config = new ConnectionConfiguration(this.resource, 5222, "localhost");
	   	config.setCompressionEnabled(true);
	   	config.setSASLAuthenticationEnabled(true);
	   	this.connection = new XMPPConnection(config);
	   	this.ui = ui;
	}
	public boolean connect(){
	   	while(true){
		   	try {
				this.connection.connect();
				return true;
			} catch (XMPPException e1) {
				return false;
			}
	   	} 
	}
	public void init_after_connection(){
//	   	build_connection_listener();
	   	this.account_manager = new AccountManager(this.connection);
	   	this.roster = this.connection.getRoster();
	    this.my_presence = new Presence(Presence.Type.available);
	    this.chat = null;
    	this.chatmanager = this.connection.getChatManager();
    	build_roster_listener();
    	build_chats();
	}
	public void build_connection_listener(){	//not yet
		this.connection_listener = new ConnectionListener(){

			@Override
			public void connectionClosed() {
				System.out.println("a");
			}

			@Override
			public void connectionClosedOnError(Exception arg0) {

				System.out.println("a");
			}

			@Override
			public void reconnectingIn(int arg0) {

				System.out.println("a");
			}

			@Override
			public void reconnectionFailed(Exception arg0) {

				System.out.println("a");
			}

			@Override
			public void reconnectionSuccessful() {

				System.out.println("a");
			}
			
		};
	   	this.connection.addConnectionListener(this.connection_listener);
	}
	public boolean signup(String username, String password){
		this.username = username;
		this.password = password;
		try {
			this.account_manager.createAccount(this.username, this.password);
		} catch (XMPPException e) {
			// Auto-generated catch block
			//e.printStackTrace();
			return false;
		}
		return true;
	}
	public boolean login(String username, String password){
		this.username = username;
		this.password = password;
    	// Log into the server
    	try {
			this.connection.login(
				this.username ,
				this.password ,
				this.resource
			);
		} catch (XMPPException e1) {
			// Auto-generated catch block
			//e1.printStackTrace();
	    	return false;
		}
    	return true;
	}
	public void change_password(String password){
		this.password=password;
		try {
			this.account_manager.changePassword(this.password);
		} catch (XMPPException e) {
			// Auto-generated catch block
			//e.printStackTrace();
		}
	}
	public void delete_account(){
		try {
			this.account_manager.deleteAccount();
		} catch (XMPPException e) {
			// Auto-generated catch block
			//e.printStackTrace();
		}
		this.connection.disconnect();
	}
	public void set_presence_mode(String str){
		if(str.equals("a"))this.my_presence.setMode(Presence.Mode.available);
		else if(str.equals("w"))this.my_presence.setMode(Presence.Mode.away);
		else if(str.equals("c"))this.my_presence.setMode(Presence.Mode.chat);
		else if(str.equals("d"))this.my_presence.setMode(Presence.Mode.dnd);
		else if(str.equals("x"))this.my_presence.setMode(Presence.Mode.xa);
		this.connection.sendPacket(this.my_presence);
	}
	public void set_presence_status(String str){
		this.my_presence.setStatus(str);
		this.connection.sendPacket(this.my_presence);
	}
	public void build_roster_listener(){
		this.roster_listener = new RosterListener() {
			// Ignored events public void entriesAdded(Collection<String> addresses) {}
		    public void entriesDeleted(Collection<String> addresses) {
		    	CLM_ui.echo_entriesDeleted(addresses);
		    	get_entries();
		    }
		    public void entriesUpdated(Collection<String> addresses) {
		    	CLM_ui.echo_entriesUpdated(addresses);
		    	get_entries();
		    }
		    public void presenceChanged(Presence presence) {
		    	CLM_ui.echo_presenceChanged(presence);
		    	get_entries();
		    }
			@Override
			public void entriesAdded(Collection<String> arg0) {
				CLM_ui.echo_entriesAdded(arg0);
		    	get_entries();
		    	for(String sub_arg0 : arg0){
		    		add_chat(sub_arg0);
		    	}
			}
		};
		roster.addRosterListener(this.roster_listener);
	}
	public Collection<RosterEntry> get_entries(){
		this.roster.reload();
		this.entries = this.roster.getEntries();
		//this chat?
		return this.entries;
	}
	public Collection<RosterEntry> get_list(){
		this.entries = this.roster.getEntries();
		List<RosterEntry> result = new ArrayList<RosterEntry>();
		for(RosterEntry entry : this.entries){
			if(
				entry.getType() == RosterPacket.ItemType.both &&	//both friend
				this.roster.getPresence(entry.getUser()).isAvailable()	//online only
			){
				result.add(entry);
			}
		}
		return result;
	}	
	public Collection<RosterEntry> get_offline(){
		this.entries = this.roster.getEntries();
		List<RosterEntry> result = new ArrayList<RosterEntry>();
		for(RosterEntry entry : this.entries){
			if(
				entry.getType() == RosterPacket.ItemType.both &&	//both friend
				!this.roster.getPresence(entry.getUser()).isAvailable()	//online only
			){
				result.add(entry);
			}
		}
		return result;
	}
	public Collection<RosterEntry> get_request(){
		this.entries = this.roster.getEntries();
		List<RosterEntry> result = new ArrayList<RosterEntry>();
		for(RosterEntry entry : this.entries){
			if(
				entry.getType() == RosterPacket.ItemType.from
			){
				result.add(entry);
			}
		}
		return result;
	}
	public void add_entry(String jid, String name, String[] group){	// not yet
		group = new String[1];
		group[0] = null;
		jid = "a@v-virtualbox";
		try {
			this.roster.createEntry(jid, "a", group);
		} catch (XMPPException e) {
			// Auto-generated catch block
			//e.printStackTrace();
		}
		get_entries();
	}
	public void accept_entry(String jid, String name, String[] group){
		jid = "a@v-virtualbox";
		Presence presence = new Presence(Presence.Type.subscribe);
		presence.setTo(jid);
		this.connection.sendPacket(presence);
		
	}
	public void build_chats(){
    	get_entries();
    	this.chats = new ArrayList<smack_chat>();
    	for(RosterEntry entry : this.entries){
    		smack_chat chat=new smack_chat(this.chatmanager, entry);
			this.chats.add(chat);
    		chat.start_chat();
    	}
	}
	public void add_chat(String jid){
		smack_chat chat=new smack_chat(this.chatmanager, this.roster.getEntry(jid));
		this.chats.add(chat);
		chat.start_chat();
	}
	public boolean check_chat(){
		if(this.chat==null)return false;
		if(this.chat.get_entry()==null)return false;
		if(this.chat.get_entry().getName()==null)return false;
		return true;
	}
	public boolean set_chat_by_name_of_entry(String name){
		build_chats();
		for(smack_chat chat : this.chats){
			if(chat.get_entry().getName()!=null){
				if(chat.get_entry().getName().equals(name)){
					this.chat = chat;
					return true;
				}
			}
		}
		return false;
	}
	public smack_chat get_chat(){
		return this.chat;
	}
	public void send_message(String str){
		if(check_chat()){
			Message newMessage = new Message();
			newMessage.setBody(str);
			newMessage.setProperty("favoriteColor", "red");
			try {
				this.chat.get_chat().sendMessage(newMessage);
			} catch (XMPPException e) {
				// Auto-generated catch blockl
				e.printStackTrace();
			}
		}
		else{
			System.out.println("No chat target!");
		}
	}
	public void disconnect(){
		for( smack_chat chat : this.chats){
			chat.remove_message_listener();
		}
		this.roster.removeRosterListener(this.roster_listener);
		this.connection.disconnect();
	}
}
