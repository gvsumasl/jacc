package edu.gvsu.cis.masl.chat;

import java.io.*;

import org.apache.http.client.ClientProtocolException;

import edu.gvsu.cis.masl.channelAPI.ChannelAPI;
import edu.gvsu.cis.masl.channelAPI.ChannelAPI.ChannelException;


public class ChatExample {
	ChatListener chatListener = new ChatListener();
	ChannelAPI channel = new ChannelAPI();
	
	/**
	 * Create and Start Listening to your channel
	 */
	public ChatExample(){
		try {
			channel = new ChannelAPI("http://example.appspot.com", "key", chatListener); //Production Example
			//channel = new ChannelAPI("http://localhost:8888", "key", chatListener);    //Local Dev Example
			channel.open();
		} catch (Exception e){
			System.out.println("Something went wrong...");
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
		this.close();
	}
	
	/**
	 * Closes the channel
	 * @throws ChannelException 
	 */
	public void close(){
		try {
			channel.close();
		} catch (Exception e){
			System.out.println("Problem Closing Channel");
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
