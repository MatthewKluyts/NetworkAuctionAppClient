package com.example.exampracticeclient;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Client extends AppCompatActivity {

    private static final String SERVER_IP = "10.0.2.2";  // Emulator's loopback address
    private static final int SERVER_PORT = 12345;// Server port
    private Thread readThread;
    private Thread writeThread;
    private BlockingQueue<String> outgoingMessages;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private EditText bidInput;

    private TextView auctionDetails;
    private Button bidButton;

    private Spinner auctionSpinner;
    private Button joinAuctionButton;
    private String selectedAuction;


    private AuctionLogAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bidInput = findViewById(R.id.bidInput);
        auctionDetails = findViewById(R.id.auctionDetails);
        bidButton = findViewById(R.id.bidButton);

        outgoingMessages = new LinkedBlockingQueue<>();

        Intent intent = getIntent();

        // Retrieve the name passed from FirstActivity
        String clientName  = intent.getStringExtra("Name");

        RecyclerView auctionLog = findViewById(R.id.recyclerViewUpdates);
        auctionLog.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AuctionLogAdapter();
        auctionLog.setAdapter(adapter);

        // Add auction updates to adapter
        adapter.addMessage("Welcome to the auction!");

        auctionSpinner = findViewById(R.id.auctionSpinner);
        joinAuctionButton = findViewById(R.id.joinAuctionButton);

        // Fetch auctions from server (mocked here for simplicity)
        List<String> auctions = Arrays.asList("Laptop", "Phone", "Tablet");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, auctions);
        auctionSpinner.setAdapter(adapter);

        joinAuctionButton.setOnClickListener(v -> {
            selectedAuction = auctionSpinner.getSelectedItem().toString();
            joinAuction(selectedAuction);
        });

        // Connect to the server as soon as the app starts
        connectToServer(clientName);

        bidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When the user clicks the bid button, send a new bid to the server
                placeBid();
            }
        });
    }

    private void connectToServer(String clientName) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);



                readThread = new ReadThread();
                writeThread = new WriteThread();

                readThread.start();
                writeThread.start();

                // Send the client's name as the first message
                if (clientName != null && !clientName.isEmpty()) {
                    sendName(clientName);
                } else {
                    sendName("Anonymous");
                }

            } catch (IOException e) {
                e.printStackTrace();
                updateUI("Error: " + e.getMessage());
            }
        });
    }

   private void sendName(String clientName)
   {
      sendMessage(clientName);
   }

    private void placeBid() {

            int bid = Integer.parseInt(bidInput.getText().toString());
            sendBidToServer(bid);

    }

    private void joinAuction(String auctionName) {
        sendMessage("JOIN " + auctionName);
        showToast("Joined auction for " + auctionName);
    }

    private void sendBidToServer(int bidAmount) {

        String msg = "Bid placed: " + bidAmount;
        sendMessage(msg);

    }

    private void showToast(final String message) {
        // Show a toast on the UI thread
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(Client.this, message, Toast.LENGTH_SHORT).show());
    }

    private void updateUI(final String message) {
        // Update the auction details on the UI thread
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> auctionDetails.setText(message));
    }

    private void updateAuctionDetails(String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> adapter.addMessage(message));
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    // Blocking read for incoming messages
                    String message = in.readLine();

                    if (message != null) {
                        if (message.startsWith("Time left: ")) {
                            // Update the UI with time remaining
                            updateAuctionDetails(message);
                        } else if (message.startsWith("Auction ended")) {
                            // Show auction ended message
                            updateAuctionDetails(message);

                        } else if (message.startsWith("Invalid bid")) {
                            // Update the UI with valid bid updates
                            showToast(message);
                        } else if (message.startsWith("Welcome ")) {
                             updateUI(message);
                        }
                        else
                        {
                            updateUI(message);
                            updateAuctionDetails(message);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                cleanup();
            }
        }
    }

    private class WriteThread extends Thread {
        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    // Blocking dequeue of messages to send
                    String message = outgoingMessages.take();
                    out.println(message);

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                cleanup();
            }
        }
    }

    // Send a message to the server
    public void sendMessage(String message) {
        outgoingMessages.offer(message);
    }

    private void cleanup() {
        try {
            if (socket != null) socket.close();
            if (readThread != null) readThread.interrupt();
            if (writeThread != null) writeThread.interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close the connection when the app is destroyed
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
