package com.example.rachitabhagchandani.clienttest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.security.KeyManagementException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class MainActivity extends AppCompatActivity {
    Thread publishThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button)findViewById(R.id.publish);
        final EditText editText1 = (EditText)findViewById(R.id.text);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("main", "button pressed");
                String mes = editText1.getText().toString();
                publishMessage(mes);
                Toast.makeText(getApplicationContext(), mes, Toast.LENGTH_LONG).show();
            }
        });
    }

    private BlockingDeque<String> queue = new LinkedBlockingDeque<String>();
    void publishMessage(String message) {
        try {
            Log.e("main", "publish_function");
            //Log.d("","[q] " + message);
            queue.putLast(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setupConnectionFactory();
    }

    ConnectionFactory factory = new ConnectionFactory();
    private void setupConnectionFactory() {
        Log.e("main", "func");
        String uri = "amqp://test:test@192.168.43.106:15672";
        try {
            factory.setAutomaticRecoveryEnabled(false);
            factory.setUri(uri);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        publishToAMQP();
    }

    public void publishToAMQP()
    {
        publishThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Log.e("main", "uiyutuyfup");
                        Connection connection = factory.newConnection();
                        Channel ch = connection.createChannel();
                        ch.confirmSelect();
                        Log.e("main", "yoyo1");

                        while (true) {
                            Log.e("main", "yoyo2");
                            String message = queue.takeFirst();
                            try{
                                Log.e("main", "yoyo3");
                                ch.basicPublish("", "hello", null, message.getBytes());
                                Log.e("main", "here");
                                ch.waitForConfirmsOrDie();
                            } catch (Exception e){
                                Log.e("main", "yoyo5");
                                //Log.d("","[f] " + message);
                                queue.putFirst(message);
                                throw e;
                            }
                        }
                    } catch (InterruptedException e) {
                        Log.e("main", e.getMessage());
                        break;
                    } catch (Exception e) {
                        Log.e("main", e.toString());
                        //Log.d("", "Connection broken: " + e.getClass().getName());
                        try {
                            Thread.sleep(5000); //sleep and then try again
                        } catch (InterruptedException e1) {
                            break;
                        }
                    }
                }
            }
        });
        publishThread.start();
    }
}
