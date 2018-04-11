package com.example.prate.group6;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

public class SVM extends AppCompatActivity {

    double train[][],test[][],train_label[],test_label[];
    double acc;
    String stat;

    TextView t,t2;
    ProgressBar simpleProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_svm);

        t=(TextView)findViewById(R.id.textView_acc);
        t2=(TextView)findViewById(R.id.textView_stat);
        simpleProgressBar=(ProgressBar) findViewById(R.id.simpleProgressBar); // initiate the progress bar


        train=new double[48][150];
        test=new double[12][150];
        train_label=new double[48];
        test_label=new double[48];

        Bundle bundle = getIntent().getExtras();
        train = (double[][]) bundle.getSerializable("train_data");
        test = (double[][]) bundle.getSerializable("test_data");
        train_label = (double[]) bundle.getSerializable("train_label");
        test_label = (double[]) bundle.getSerializable("test_label");

        //Handler to get messgaes from threads
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                t.setText("Accuracy "+acc);
            }
        };

        final Handler handler1 = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                t2.setText(stat);
            }
        };

   //Training and testing the data and finding accuracy
        final svm_ecl s= new svm_ecl();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                simpleProgressBar.setVisibility(View.VISIBLE);
                simpleProgressBar.setProgress(0);
                simpleProgressBar.setMax(100);


                try {
                    for (int i = 0; i <= 100; i = i + 10) {
                        if(i==0)
                        {s.svmTrain(train, train_label);}
                        simpleProgressBar.setProgress(i);
                        if(i<=60)
                        stat="Training "+(i+40)+"% completed";
                        else {
                            if (i < 100)
                                stat = "Training Completed....Testing the model";
                            else
                                stat= "Training and Testing Completed.";
                        }

                        handler1.sendEmptyMessage(0);
                        Thread.sleep(1000);
                    }

                    acc = s.svmTest(test, test_label);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                handler.sendEmptyMessage(0);
            }
        });
    }
}
