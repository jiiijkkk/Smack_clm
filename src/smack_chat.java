import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Message;


public class smack_chat {
	private ChatManager chatmanager;
	private Chat chat;
	private RosterEntry entry;
	private MessageListener myMessageListener;
	public smack_chat(ChatManager chatmanager, RosterEntry entry){
		this.chatmanager = chatmanager;
		this.entry = entry;
		this.myMessageListener = new MessageListener() {
    	    public void processMessage(Chat chat, Message message) {
				if (message.getType() == Message.Type.chat) {
					System.out.println(chat.getParticipant() + " says: " + message.getBody());
				}
    	    }
    	};
	}
	public void remove_message_listener(){
		this.myMessageListener = null;
	}
	public void start_chat(){
		this.chat = chatmanager.createChat(this.entry.getUser(), myMessageListener);
	}
	public Chat get_chat(){
		return this.chat;
	}
	public RosterEntry get_entry(){
		return this.entry;
	}
	public String get_thread_id(){
		return this.chat.getThreadID();
	}
}
