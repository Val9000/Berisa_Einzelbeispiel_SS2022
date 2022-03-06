package com.example.berisa_einzelbeispiel_ss2022;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.view.ContentInfo;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private EditText et_MatrikelNr;
    private TextView txtView_Response;

    /*
        We could easily just make a TCP Connection and do the work there...but
        that is not a good way to do the job.

        We need to create some kind of background worker / runnable to do
        our work without blocking the main thread of the app.

        So the user could sill use the app while in the background we are doing work or
        waiting for an request as we do in this case here.

        What happens if we do not use a background task ?
        We will get an exception : Caused by: android.os.NetworkOnMainThreadException
        at android.os.StrictMode$AndroidBlockGuardPolicy.onNetwork(StrictMode.java:1605)
        We can modify the guard policy by just using 2 lines of code and the app will run.
        Modifying StrictMode is NOT a fix. -> Bad Practice
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // init
        txtView_Response = (TextView) findViewById(R.id.txtView_Response);
        txtView_Response.setSingleLine(false);

        et_MatrikelNr = (EditText) findViewById(R.id.et_MatrikelNr);
    }


    // Listeners
    public void onClick_btn_sendTCPRequest(View view) throws InterruptedException {
        String matrikelNr = et_MatrikelNr.getText().toString();
        if (matrikelNr.length() == 0) {
            txtView_Response.setText("Enter MatrikelNr");
            return;
        }
        /*
        If the older versions of Android would support CompletableFutures I would do the Async TCP Task
        by just one line of code :
        CompletableFuture.supplyAsync(()-> getTCPServerMsg(matrikelNr))
                    .thenAccept(s-> runOnUiThread(() -> txtView_Response.setText(s)));

        Also there used to be another way to solve this kind of tasks, by using AsyncTask.
        https://developer.android.com/reference/android/os/AsyncTask
        "But it got deprecated because it was would cause Context leaks, missed callbacks,
        or crashes on configuration changes." (same goes for the CompletableFuture alternative)

        I think my solution down below also leaks data but I'm not sure how to test this.
        I also couldn't find any better way to implement a async call and  update the UI without
        blocking main thread where context leaks where taken into consideration.

        I have found two solutions but I'm not sure which one is better.
        */

        /*
        Solution 1 :
        new Thread(() -> {
            //Background work here
            String response = getTCPServerMsg(matrikelNr);
            runOnUiThread(() -> {
                //UI Thread work here
                if (response != null) txtView_Response.setText(response);
                else
                    txtView_Response.setText("Something happened when sending MatrikelNr to Server");
            });
        }).start();
        */

        // Solution 2 :
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            //Background work here
            String response = getTCPServerMsg(matrikelNr);
            handler.post(() -> {
                //UI Thread work here
                if (response != null) txtView_Response.setText(response);
                else
                    txtView_Response.setText("Something happened when sending MatrikelNr to Server");

            });
        });

    }

    public void onClick_btn_CalcSubFeature4(View view) {

    }


    // Logic
    private String getTCPServerMsg(String martikelNr) {
        try {
            Socket clientSocket = new Socket("se2-isys.aau.at", 53212);
            DataOutputStream writer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer.writeBytes(martikelNr + "\n");
            writer.flush();
            final String response = reader.readLine();
            clientSocket.close();
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}