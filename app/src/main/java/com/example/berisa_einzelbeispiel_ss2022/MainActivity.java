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

        /*
        Reading the problem sheet again, I have found this link :
        https://medium.com/hootsuite-engineering/asynchronous-android-programming-the-good-the-bad-and-the-ugly-c48a0b73665f

        After reading the article I would say that my 2nd Solution is better since the static handler is
        holding a weak reference to the activity, so I can call it's methods to update update the UI
        without creating memory leak. My solution is considered "bad" when making multiple network calls.
        In my case there is only one. So I would say its an "okay" solution, without using any external
        libraries.

        For multiple network calls we should rather use RxJava ! (I guess)
        Using RxJava for this case here, in my opinion it would be an overkill.
        As the job is done with my solution 2.
         */
    }

    /*
    In my opinion background worker is here not needed, since its a very short calculation.
    If it were more complex, then we need to handle this with a thread as seen above.
     */
    public void onClick_btn_CalcSubFeature4(View view) {
        String s_matrikelNr = et_MatrikelNr.getText().toString();
        if(s_matrikelNr.length() == 0){
            txtView_Response.setText("Enter MatrikelNr");
            return;
        }
        int matrikelNr = Integer.valueOf(s_matrikelNr);
        txtView_Response.setText("Digit Sum to Binary of MatrikelNr : " + digitSumToBinary(matrikelNr));
    }


    // Logic, could be moved to an extra (static) class but I think not needed for this case
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

    /*
     My martikelnumber = 11943292 % 7 = 4. So I implemented task 4:
     Form a cross sum of the matriculation number and then display it as a binary number
     */

    private String digitSumToBinary(int number) {
        int sum = 0;
        do {
            sum += number % 10;
            number /= 10;
        } while (number > 0);
        return (String) Integer.toBinaryString(sum);
    }


}