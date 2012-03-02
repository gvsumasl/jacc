package edu.gvsu.cis.masl.chat;

import edu.gvsu.cis.masl.channelAPI.ChannelService;

public class ChatListener implements ChannelService{

	/**
	 * Method gets called when we initially connect to the server
	 */
	@Override
	public void onOpen() {
		
	}

	/**
	 * Method gets called when the server sends a message.
	 */
	@Override
	public void onMessage(String message) {
		System.out.println("Server push: " + message);
	}

	/**
	 * Method gets called when we close the connection to the server.
	 */
	@Override
	public void onClose() {
		
	}

	/**
	 * Method gets called when an error occurs on the server.
	 */
	@Override
	public void onError(Integer errorCode, String description) {
		System.out.println("Error: " + errorCode + " Reason: " + description);
	}

}
