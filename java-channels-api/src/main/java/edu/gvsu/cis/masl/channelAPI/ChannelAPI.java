package edu.gvsu.cis.masl.channelAPI;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;


public class ChannelAPI {
	
    private enum ReadyState {CONNECTING, OPEN, CLOSING, CLOSED};

    private String BASE_URL = "http://localhost:8888"; //Defaults to LocalHost
    private static final String CHANNEL_URL = "/_ah/channel/";
    private String channelId = null;
    private String applicationKey = null;
    private String clientId = null;
    private ChannelService channelListener = new ChannelListener();
    private ReadyState readyState = ReadyState.CLOSED;
    private Integer TIMEOUT_MS = 500;
    private HttpClient httpClient = new DefaultHttpClient();
    private Thread thPoll = null;
    
    /**
     * Create A Channel, Using URL, ChannelKey and a ChannelService
     * @param URL - Server Location - http://localhost:8888
     * @param channelKey - Unique Identifier for channel groups, server uses this to push data to clients, can have multiple
     *  clients on the same key, but only 1 channel per client
     * @param channelService -An Implementation of the ChannelService class, this is where the function methods will get called when
     *  the server pushes data
     * @throws IOException JSON Related
     * @throws ClientProtocolException Connection Related
     */
    public ChannelAPI(String URL, String channelKey, ChannelService channelService) throws IOException, ClientProtocolException {
    	this.clientId = null;
    	this.BASE_URL = URL;
    	this.channelId = createChannel(channelKey);
    	this.applicationKey = channelKey;
    	
    	if (channelService != null) {
            this.channelListener = channelService;
        }
    }
    
    /**
     * Create a Channel on the Server and return the channelID + Key
     * @param key
     * @return String: Channel 'Token/ID'
     * @throws IOException
     * @throws ClientProtocolException
     */
    private String createChannel(String key) throws IOException, ClientProtocolException{
    	String token = "";
		HttpClient staticClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(BASE_URL + "/?c=" + key);
		try{
			XHR xhr = new XHR(staticClient.execute(httpGet));
			System.out.println(xhr.getResponseText());
			JSONObject json = new JSONObject(xhr.getResponseText());
			token = json.getString("token");
		} catch (JSONException e) {
			System.out.println("Error: Parsing JSON");
		}
    	return token;
    }

    /**
     * Connect to the Channel
     * @throws IOException
     */
    public void open() throws IOException {
        this.readyState = ReadyState.CONNECTING;
        connect(sendGet(getUrl("connect")));
    }

    /**
     * Close the Channel, Channel is gone from the server now, a new Channel
     * is required if you wish to reconnect, you can't re-use the old one
     * @throws IOException
     */
    public void close() throws IOException {
        this.readyState = ReadyState.CLOSING;
        disconnect(sendGet(getUrl("disconnect")));
    }

    /**
     * 
     * @param command
     * @return
     * @throws IOException
     */
    private String getUrl(String command) throws IOException {
        String url = BASE_URL + CHANNEL_URL + "dev?command=" + command + "&channel=";
        url += URLEncoder.encode(this.channelId, "UTF-8");
        if (this.clientId != null) {
            url += "&client=" + URLEncoder.encode(this.clientId, "UTF-8");
        }
        return url;
    };

    /**
     * If you successfully connect to the Channel, start pulling data.
     * If you fail to connect, the error is printed to the terminal and
     * the connection is closed.
     * @param xhr
     */
    private void connect(XHR xhr) {
        if (xhr.isSuccess()) {
            this.clientId = xhr.getResponseText();
            this.readyState = ReadyState.OPEN;
            this.channelListener.onOpen();
            poll();
        } else {
            this.readyState = ReadyState.CLOSING;
            this.channelListener.onError(xhr.getStatus(), xhr.getStatusText());
            this.readyState = ReadyState.CLOSED;
            this.channelListener.onClose();
        }
    }

    /**
     * Closing the channel
     * @param xhr
     */
    private void disconnect(XHR xhr) {
        this.readyState = ReadyState.CLOSED;
        this.channelListener.onClose();
    }

    /**
     * 
     * @param xhr
     */
    private void forwardMessage(XHR xhr) {
        if (xhr.isSuccess()) {
            String data = StringUtils.chomp(xhr.getResponseText());
            if (!StringUtils.isEmpty(data)) {
                this.channelListener.onMessage(data);
            }
            poll();
        } else {
            this.channelListener.onError(xhr.getStatus(), xhr.getStatusText());
        }
    }

    /**
     * Grabbing Data
     */
    private void poll() {
        if (thPoll == null) {
            thPoll = new Thread(new Runnable() {

                @Override
                public void run() {
                    XHR xhr = null;
                    try {
                        Thread.sleep(TIMEOUT_MS);
                        xhr = sendGet(getUrl("poll"));
                        thPoll = null;
                        forwardMessage(xhr);
                    } catch (Exception e) {
                        thPoll = null;
                    }
                }

            });
            thPoll.start();
        }
    }

    /**
     * 
     * @param xhr
     */
    private void forwardSendComplete(XHR xhr) {
        if (!xhr.isSuccess()) {
            this.channelListener.onError(xhr.getStatus(), xhr.getStatusText());
        }
    }

    /**
     * Used to send a message to the server
     * @param message
     * @param urlPattern - where the server should look for the message. ex: "/chat"
     * @return true
     * @throws IOException
     */
    public boolean send(String message, String urlPattern) throws IOException {
    	if (this.readyState != ReadyState.OPEN) {
            return false;
        }
        String url = BASE_URL + urlPattern;
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("channelKey", this.applicationKey));
        params.add(new BasicNameValuePair("message", message));
        forwardSendComplete(sendPost(url, params));
        return true;
    }

    /**
     * Send an HTTPPOST, convenience function
     * @param url
     * @param params
     * @return XHR, nice responses from httpRequests
     * @throws IOException
     */
    private XHR sendPost(String url, List<NameValuePair> params) throws IOException {
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(entity);
        return new XHR(httpClient.execute(httpPost));
    }

    /**
     * Send an HTTPGET, convenience function
     * @param url
     * @return XHR, nice responses from httpRequests
     * @throws IOException
     */
    private XHR sendGet(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        return new XHR(httpClient.execute(httpGet));
    }
	
	/**
	 * Set a new ChannelListener
	 * @param channelListener
	 */
	public void setChannelListener(ChannelService channelListener) {
	    if (channelListener != null) {
		    this.channelListener = channelListener;
		}
	}
}