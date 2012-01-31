package edu.gvsu.cis.masl.chat;

import java.io.*; 
import edu.gvsu.cis.masl.channelAPI.ChannelAPI;

public class ChatExample {
	ChatListener chatListener = new ChatListener();
	ChannelAPI channel;
	
	/**
	 * Create and Start Listening to your channel
	 */
	public ChatExample(){
		try {
			channel = new ChannelAPI("http://localhost:8888", "key", chatListener);
			channel.open();
		} catch (IOException e) {
			System.out.println("Oops, had trouble reading...");
		}
	}

	/**
	 * Starts the chat cycle
	 */
	public void startChat(){
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in)); 
		String message = "";
		
		while(!message.equals("exit")){
			try {
				message = input.readLine();
				if(!message.equals("")){
					sendMessage(message);
				}
			} catch (IOException e) {
				System.out.println("Problem grabbing user input!");
			} 
		}
	}
	
	/**
	 * Sends your message on the open channel
	 * @param message
	 */
	public void sendMessage(String message){
		try {
			channel.send(message, "/chat");
		} catch (IOException e) {
			System.out.println("Problem Sending the Message");
		}
	}
	
	/**
	 * Its the Main!
	 * @param args
	 */
	public static void main(String[] args) {
		ChatExample chatExample = new ChatExample();
		chatExample.startChat();
	}
}
