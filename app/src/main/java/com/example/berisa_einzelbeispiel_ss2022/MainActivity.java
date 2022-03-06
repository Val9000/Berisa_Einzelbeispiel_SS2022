package com.example.berisa_einzelbeispiel_ss2022;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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

    }

    public void onClick_btn_CalcSubFeature4(View view) {

    }

}