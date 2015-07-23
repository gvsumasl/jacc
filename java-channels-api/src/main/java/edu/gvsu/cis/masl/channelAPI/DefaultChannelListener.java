package edu.gvsu.cis.masl.channelAPI;

public class DefaultChannelListener implements ChannelListener {
    /**
     * Called when we open a connection to the server
     */
	@Override
    public void onOpen() {
        System.out.println("Channel Opened!");
        System.out.println("This is the \"Default Class\" You Should Really Implement Your Own Version of \"ChannelListener\" :-)");
    }

	/**
     * Called when we receive a message from the server
     */
    @Override
    public void onMessage(String message) {
        System.out.println("Message: " + message);
        System.out.println("This is the \"Default Class\" You Should Really Implement Your Own Version of \"ChannelListener\" :-)");
    }
    
    /**
     * Called when we close the channel to the server
     */
    @Override
    public void onClose() {
        System.out.println("Channel Closed!");
        System.out.println("This is the \"Default Class\" You Should Really Implement Your Own Version of \"ChannelListener\" :-)");
    }

    /**
     * Called when we receive an error message from the server
     */
	@Override
	public void onError(Integer errorCode, String description) {
		System.out.println("Channel Error");
		System.out.println("Error Occured: " + description + " Error Code:" + errorCode);
		System.out.println("This is the \"Default Class\" You Should Really Implement Your Own Version of \"ChannelListener\" :-)");
	}
};
