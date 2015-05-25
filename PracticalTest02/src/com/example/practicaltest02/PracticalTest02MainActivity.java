package com.example.practicaltest02;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PracticalTest02MainActivity extends Activity {
	
	private int port = 0;
	private Server server = null;
	private EditText portEditText = null;
	private Button startServerButton = null;
	private EditText cityEditText = null;
	private TextView temperature = null;
	private Button searchButton = null;
	
	private class Server extends Thread {
		
		ServerSocket ssocket = null;
		
		public Server(int port) {
			
			try {
				this.ssocket = new ServerSocket(port);
				Log.d("ServerSocket Opened", "Opened ServerSocket successully on port " + port);
			} catch (IOException e) {
				
				Log.e("ServerSocket error", "Could not open ServerSocket");
			}
		}
		
		@Override
		public void run() {
			
			while(!isInterrupted()) {
				
				try {
					
					Socket socket = ssocket.accept();
					Log.d("Server", "AcceptedRequest");
					ServerResolver resolver = new ServerResolver(socket);
					resolver.start();
					Log.d("Server", "ResolverStarted");
				} catch (IOException e) {
					
					e.printStackTrace();
				}
			}
		}
		
		public void stopThread() {
			if (ssocket != null) {
				interrupt();
				if (ssocket != null) {
					try {
					
						ssocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					}
				}
			}
	}
	
	private class ServerResolver extends Thread {
		
		Socket socket = null;
		
		public ServerResolver(Socket socket) {
			
			this.socket = socket;
		}
		
		@Override
		public void run() {
			
			try {
				Log.d("Resolver", "Started");
				BufferedReader reader = Utilities.getReader(socket);
				PrintWriter writer = Utilities.getWriter(socket);
				
				Log.d("Resolver", "Got Reader/Writer");
				
				String line = null;
				String city = "";
				
				Log.d("Resolver", "Before While");
				//while((line = reader.readLine()) != null || reader.) {
					line = reader.readLine();
					Log.d("Resolver", "Read Line: " + line);
					city += line;	
				//}
				
				Log.d("Resolver", "Read City: " + city);
				//Lanseaza cererea Get
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet("http://www.wunderground.com/cgi-bin/findweather/getForecast?query=" + city);
				HttpResponse httpGetResponse = httpClient.execute(httpGet);
				HttpEntity httpGetEntity = httpGetResponse.getEntity();	
				
				String content = EntityUtils.toString(httpGetEntity);
				
				Log.d("Resolver", content);
				
				writer.println(content.replace("\n", "alabala"));
				writer.flush();
				
				Thread.sleep(1000);
				
				Log.d("Resolver", "Done");
				
			} catch (IOException e) {
				
				Log.d("Resolver", Arrays.toString(e.getStackTrace()));
				Log.d("Resolver", "Exited with error");
				return;
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
		}
		
	}
	
	private class Client extends Thread {
		
		private String city = null;
		private int port = 0;
		
		public Client(String city, int port) {
			
			this.city = city;
			this.port = port;
		}
		
		@Override
		public void run() {
			
			try {
				
				Log.d("Client", "Started");
				
				Socket socket = new Socket("localhost", this.port);
				
				Log.d("Client", "Opened Socket");
				
				BufferedReader reader = Utilities.getReader(socket);
				PrintWriter writer = Utilities.getWriter(socket);
				
				writer.println(this.city);
				writer.flush();
				
				Log.d("Client", "Wrote city: " + this.city);
				
				Thread.sleep(10000);
				
				String line = null;
				String response = "";
				
				//while((line = reader.readLine()) != null) {
					
					line = reader.readLine();
					response += line;
				//}
				
					response = response.replace("alabala", "\n");
				Log.d("Clinet", "Server Response: " + response);
				
				
				Log.d("Client", "Done");
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_practical_test02_main);
		
		this.portEditText = (EditText)findViewById(R.id.port);
		this.startServerButton = (Button)findViewById(R.id.startServer);
		this.cityEditText = (EditText)findViewById(R.id.city);
		this.temperature = (TextView)findViewById(R.id.temperature);
		this.searchButton = (Button)findViewById(R.id.search);
		
		startServerButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(portEditText.getText() != null && portEditText.getText().toString().length() > 0) {
					
					port = Integer.parseInt(portEditText.getText().toString());
					server = new Server(port);
					server.start();
				}
				else {
					
					Toast.makeText(getApplicationContext(), "Introduceti portul", Toast.LENGTH_LONG).show();
				}
			}
		});
		
		searchButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(cityEditText.getText() != null && cityEditText.getText().toString().length() > 0) {
					Client client = new Client(cityEditText.getText().toString(), port);
					client.start();
				}
				else {
					
					Toast.makeText(getApplicationContext(), "Introduceti orasul", Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.practical_test02_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onDestroy() {
		
		server.stopThread();
		
		super.onDestroy();
	}
}
