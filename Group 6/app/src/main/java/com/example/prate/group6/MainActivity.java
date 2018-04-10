package com.example.prate.group6;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends Activity implements SensorEventListener {

    SQLiteDatabase db,db1,db2;
    Cursor cursor;
    EditText patientID;
    EditText Age;
    EditText Name;
    RadioGroup Sex;

    String patientIDText = "";
    String ageText = "";
    String nameText = "";
    String sex="";
    String temp="";


    Button b1, b2,b3,b4;
    int flag = 0;
    LineGraphSeries<DataPoint> series1;
    LineGraphSeries<DataPoint> series2;
    LineGraphSeries<DataPoint> series3;
    LineGraphSeries<DataPoint> series11;
    LineGraphSeries<DataPoint> series22;
    LineGraphSeries<DataPoint> series33;
    int x_cord1=0, x_cord2=0,x_cord3 = 0;
    float x_acc;
    float y_acc;
    float z_acc;
    long time_acc;


    private SensorManager senSensorManager;
    private Sensor senAccelerometer;


    int i=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        patientID = (EditText) findViewById(R.id.id_text_field);
        Age = (EditText) findViewById(R.id.age_text_field);
        Name = (EditText) findViewById(R.id.name_text_field);
        Sex = (RadioGroup) findViewById(R.id.radioGroup1);


        //graph View
        final GraphView graph1 = (GraphView) findViewById(R.id.g1); // graph object
        final GraphView graph2 = (GraphView) findViewById(R.id.g2);
        final GraphView graph3 = (GraphView) findViewById(R.id.g3);

        series1 = new LineGraphSeries<DataPoint>();
        series2 = new LineGraphSeries<DataPoint>();
        series3 = new LineGraphSeries<DataPoint>();

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);

        GridLabelRenderer gridLabel1 = graph1.getGridLabelRenderer();
        GridLabelRenderer gridLabel2 = graph2.getGridLabelRenderer();
        GridLabelRenderer gridLabel3 = graph3.getGridLabelRenderer();
        gridLabel1.setHorizontalAxisTitle("TimeSeries");
        gridLabel1.setVerticalAxisTitle("X");
        gridLabel2.setHorizontalAxisTitle("TimeSeries");
        gridLabel2.setVerticalAxisTitle("Y");
        gridLabel3.setHorizontalAxisTitle("TimeSeries");
        gridLabel3.setVerticalAxisTitle("Z");

        Viewport viewport1 = graph1.getViewport();
        viewport1.setXAxisBoundsManual(true);
        viewport1.setMinX(0);
        viewport1.setMaxX(100);
        viewport1.setYAxisBoundsManual(true);
        viewport1.setMinY(-40);
        viewport1.setMaxY(40);

        Viewport viewport2 = graph2.getViewport();
        viewport2.setXAxisBoundsManual(true);
        viewport2.setMinX(0);
        viewport2.setMaxX(100);
        viewport2.setYAxisBoundsManual(true);
        viewport2.setMinY(-40);
        viewport2.setMaxY(40);

        Viewport viewport3 = graph3.getViewport();
        viewport3.setXAxisBoundsManual(true);
        viewport3.setMinX(0);
        viewport3.setMaxX(100);
        viewport3.setYAxisBoundsManual(true);
        viewport3.setMinY(-40);
        viewport3.setMaxY(40);
        //viewport.setScrollable(true);


        try {
            File dir = new File(Environment.getExternalStorageDirectory()+"/Android/data", "CSE535_ASSIGNMENT2");
            try {
                if (!dir.exists()) {
                    dir.mkdir();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            db = SQLiteDatabase.openOrCreateDatabase(Environment.getExternalStorageDirectory()+"/Android/data/CSE535_ASSIGNMENT2/Group6.db", null);
        }
        catch (SQLiteException  e){
            //Handle the error
        }

        //STOP button Functionality
        b1 = (Button) findViewById(R.id.button_stop);
        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                createDB();
                flag = 0;
                graph1.removeAllSeries();// remove 3 series
                graph2.removeAllSeries();
                graph3.removeAllSeries();
                svm_ecl svmobj=new svm_ecl();
                svmobj.create_dataset();
                svmobj.svmTrain();
                svmobj.svmTest();


            }
        });
        //RUN button Functionality
        b2 = (Button) findViewById(R.id.button_run);
        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                patientIDText = patientID.getText().toString();

                ageText = Age.getText().toString();
                nameText = Name.getText().toString();
                if(((RadioButton) findViewById(Sex.getCheckedRadioButtonId()))==null)
                    sex="";
                else
                    sex=((RadioButton) findViewById(Sex.getCheckedRadioButtonId())).getText().toString();

                // Do something in response to button click
                if(patientIDText.matches("") || ageText.matches("") || nameText.matches("") || sex.matches("")) {
                    //Insert
                    Toast.makeText(MainActivity.this, "Please enter all Text Fields", Toast.LENGTH_LONG).show();

                }
                else {
                    try{
                        String table_name = nameText+"_"+patientIDText+"_"+ageText+"_"+sex;

                        table_name=table_name.replaceAll("\\s+","");

                        //Create table query to create table when new patient is there
                        String query = "create table if not exists " + table_name + " (Timestamp TIMESTAMP, x float, y float, z float);";
                        db.execSQL(query);
                        if(temp!=null && !temp.equals(table_name)){         //re initialization the series of all 3 graphs when new patient is there
                            series1 = new LineGraphSeries<DataPoint>();
                            series2 = new LineGraphSeries<DataPoint>();
                            series3 = new LineGraphSeries<DataPoint>();
                            x_cord1=0;
                            x_cord2=0;
                            x_cord3 = 0;
                        }

                        temp = table_name;


                    }
                    catch (Exception e){
                        //Handle the error
                        Log.e("Error", e.toString());
                    }

                    graph1.removeAllSeries();// remove all 3 series
                    graph2.removeAllSeries();
                    graph3.removeAllSeries();
                    flag = 1;                 //Flag to control plotting of graph
                    graph1.addSeries(series1); //3 graph series
                    graph2.addSeries(series2);
                    graph3.addSeries(series3);
                }
            }
        });


        //Upload button functionality
        b3 = (Button) findViewById(R.id.button_upload);
        b3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String filepath=Environment.getExternalStorageDirectory()+"/Android/data/CSE535_ASSIGNMENT2/Group6.db";
                UploadClass uc=new UploadClass(filepath);
                uc.start();
                try {
                    uc.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Toast.makeText(MainActivity.this,uc.getOutput(),Toast.LENGTH_SHORT).show();

            }
        });




        //Download button functionality
        b4 = (Button) findViewById(R.id.button_download);
        b4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DownloadClass dc= new DownloadClass();
                dc.start();
                try {
                    dc.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Toast.makeText(MainActivity.this,dc.getOutput(),Toast.LENGTH_SHORT).show();
                showLast10();                                                                      //function call to display last 10 seconds
            }
        });
    }



    //Function to plot series on the 3 grapghs and insert data in local database
    private void addPlot() {
        // TODO Auto-generated method stub

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        series1.appendData(new DataPoint(x_cord1, x_acc), true, 100);
        series2.appendData(new DataPoint(x_cord2, y_acc), true, 100);
        series3.appendData(new DataPoint(x_cord3, z_acc), true, 100);
        x_cord1++;
        x_cord2++;
        x_cord3++;
        try{
            db.beginTransaction();
            String table_name = nameText+"_"+patientIDText+"_"+ageText+"_"+sex;
            table_name=table_name.replaceAll(" ","");
            String query ="INSERT INTO "+ table_name +" (Timestamp, x, y, z) VALUES ('"+timestamp+"', "+ x_acc +", "+ y_acc +", "+ z_acc +");";
            db.execSQL(query);
            db.setTransactionSuccessful(); //commit your changes
        }
        catch (SQLiteException e){
            //Handle the error
            Log.e("Error", e.toString());
        }
        finally {
            db.endTransaction();
        }

    }


    //Function to plot last 10 seconds. This is called by showLAst10() function defined below
    private void addPlot_last10Sec(int iX, int iY, int iZ) {
        // TODO Auto-generated method stub

        series11.appendData(new DataPoint(cursor.getPosition(), cursor.getFloat(iX)), true, 100);
        series22.appendData(new DataPoint(cursor.getPosition(), cursor.getFloat(iY)), true, 100);
        series33.appendData(new DataPoint(cursor.getPosition(), cursor.getFloat(iZ)), true, 100);

    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            x_acc = event.values[0];
            y_acc = event.values[1];
            z_acc = event.values[2];
            long timestamp = System.currentTimeMillis();
            time_acc = timestamp;
            //System.out.print(x_acc[i]);
            i++;
        }

    }


    //function called when download button is pressed
    public void showLast10() {
        patientIDText = patientID.getText().toString();
        ageText = Age.getText().toString();
        nameText = Name.getText().toString();
        if (((RadioButton) findViewById(Sex.getCheckedRadioButtonId())) == null)
            sex = "";
        else
            sex = ((RadioButton) findViewById(Sex.getCheckedRadioButtonId())).getText().toString();

        // Do something in response to button click
        if (patientIDText.matches("") || ageText.matches("") || nameText.matches("") || sex.matches("")) {
            //Insert
            Toast.makeText(MainActivity.this, "Please enter all Text Fields", Toast.LENGTH_LONG).show();
        }
        else {
            //graph View
            final GraphView graph1 = (GraphView) findViewById(R.id.g1); // graph object
            final GraphView graph2 = (GraphView) findViewById(R.id.g2);
            final GraphView graph3 = (GraphView) findViewById(R.id.g3);

            series11 = new LineGraphSeries<DataPoint>();
            series22 = new LineGraphSeries<DataPoint>();
            series33 = new LineGraphSeries<DataPoint>();

            //Use of cursor to acess rows of table
            db1 = SQLiteDatabase.openOrCreateDatabase(Environment.getExternalStorageDirectory() + "/Android/data/CSE535_ASSIGNMENT2_DOWN/Group6.db", null);
            try {
                db.beginTransaction();
                String table_name = nameText + "_" + patientIDText + "_" + ageText + "_" + sex;
                table_name = table_name.replaceAll(" ", "");

                String[] columns = new String[]{"x", "y", "z"};

                cursor = db1.query(table_name, columns, null, null, null, null, null);

                int rowCount = cursor.getCount();
                int position;

                //Get columns ids of the table
                final int iX = cursor.getColumnIndex("x");
                final int iY = cursor.getColumnIndex("y");
                final int iZ = cursor.getColumnIndex("z");
                graph1.addSeries(series11); //3 graph series
                graph2.addSeries(series22);
                graph3.addSeries(series33);
                if (rowCount >= 30)
                    position = rowCount - 30;
                else
                    position = 1;

                //Loop to access rows of the table
                for (cursor.moveToPosition(position); !cursor.isAfterLast(); cursor.moveToNext()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addPlot_last10Sec(iX, iY, iZ);
                        }
                    });
                }
                cursor.close();
                db.setTransactionSuccessful(); //commit your changes

            } catch (SQLiteException e) {
                //Handle the error
                Log.e("Error", e.toString());
            } finally {
                db.endTransaction();
            }
        }
    }



    //Function that runs the thread to plot on 3 graphs
    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        new Thread(new Runnable() {

            @Override
            public void run() {
                // we add infinite new entries
                for (; ; ) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (flag == 1) {
                                addPlot();
                            }
                        }
                    });

                    // sleep to slow down the add of entries
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // manage error ...
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }



    //to create new Database
    public void createDB() {
        db2 = SQLiteDatabase.openOrCreateDatabase(Environment.getExternalStorageDirectory() + "/Android/data/CSE535_ASSIGNMENT2/Group6_new.db", null);
        //Create table query to create table when new patient is there
        String query = "create table if not exists SVM_input (id INTEGER , AccelX_1 float, AccelY_1 float, AccelZ_1 float, " +
                "AccelX_2 float, AccelY_2 float, AccelZ_2 float, " +
                "AccelX_3 float, AccelY_3 float, AccelZ_3 float, " +
                "AccelX_4 float, AccelY_4 float, AccelZ_4 float, " +
                "AccelX_5 float, AccelY_5 float, AccelZ_5 float, " +
                "AccelX_6 float, AccelY_6 float, AccelZ_6 float, " +
                "AccelX_7 float, AccelY_7 float, AccelZ_7 float, " +
                "AccelX_8 float, AccelY_8 float, AccelZ_8 float, " +
                "AccelX_9 float, AccelY_9 float, AccelZ_9 float, " +
                "AccelX_10 float, AccelY_10 float, AccelZ_10 float, " +
                "AccelX_11 float, AccelY_11 float, AccelZ_11 float, " +
                "AccelX_12 float, AccelY_12 float, AccelZ_12 float, " +
                "AccelX_13 float, AccelY_13 float, AccelZ_13 float, " +
                "AccelX_14 float, AccelY_14 float, AccelZ_14 float, " +
                "AccelX_15 float, AccelY_15 float, AccelZ_15 float, " +
                "AccelX_16 float, AccelY_16 float, AccelZ_16 float, " +
                "AccelX_17 float, AccelY_17 float, AccelZ_17 float, " +
                "AccelX_18 float, AccelY_18 float, AccelZ_18 float, " +
                "AccelX_19 float, AccelY_19 float, AccelZ_19 float, " +
                "AccelX_20 float, AccelY_20 float, AccelZ_20 float, " +
                "AccelX_21 float, AccelY_21 float, AccelZ_21 float, " +
                "AccelX_22 float, AccelY_22 float, AccelZ_22 float, " +
                "AccelX_23 float, AccelY_23 float, AccelZ_23 float, " +
                "AccelX_24 float, AccelY_24 float, AccelZ_24 float, " +
                "AccelX_25 float, AccelY_25 float, AccelZ_25 float, " +
                "AccelX_26 float, AccelY_26 float, AccelZ_26 float, " +
                "AccelX_27 float, AccelY_27 float, AccelZ_27 float, " +
                "AccelX_28 float, AccelY_28 float, AccelZ_28 float, " +
                "AccelX_29 float, AccelY_29 float, AccelZ_29 float, " +
                "AccelX_30 float, AccelY_30 float, AccelZ_30 float, " +
                "AccelX_31 float, AccelY_31 float, AccelZ_31 float, " +
                "AccelX_32 float, AccelY_32 float, AccelZ_32 float, " +
                "AccelX_33 float, AccelY_33 float, AccelZ_33 float, " +
                "AccelX_34 float, AccelY_34 float, AccelZ_34 float, " +
                "AccelX_35 float, AccelY_35 float, AccelZ_35 float, " +
                "AccelX_36 float, AccelY_36 float, AccelZ_36 float, " +
                "AccelX_37 float, AccelY_37 float, AccelZ_37 float, " +
                "AccelX_38 float, AccelY_38 float, AccelZ_38 float, " +
                "AccelX_39 float, AccelY_39 float, AccelZ_39 float, " +
                "AccelX_40 float, AccelY_40 float, AccelZ_40 float, " +
                "AccelX_41 float, AccelY_41 float, AccelZ_41 float, " +
                "AccelX_42 float, AccelY_42 float, AccelZ_42 float, " +
                "AccelX_43 float, AccelY_43 float, AccelZ_43 float, " +
                "AccelX_44 float, AccelY_44 float, AccelZ_44 float, " +
                "AccelX_45 float, AccelY_45 float, AccelZ_45 float, " +
                "AccelX_46 float, AccelY_46 float, AccelZ_46 float, " +
                "AccelX_47 float, AccelY_47 float, AccelZ_47 float, " +
                "AccelX_48 float, AccelY_48 float, AccelZ_48 float, " +
                "AccelX_49 float, AccelY_49 float, AccelZ_49 float, " +
                "AccelX_50 float, AccelY_50 float, AccelZ_50 float, Label varchar(15));";
        db2.execSQL(query);
        try {
            db.beginTransaction();


            String[] columns = new String[]{"x", "y", "z"};


            //int rowCount = cursor.getCount();
            ArrayList<Float> tempX = new ArrayList<Float>();
            ArrayList<Float> tempY = new ArrayList<Float>();
            ArrayList<Float> tempZ = new ArrayList<Float>();
            String[] label = new String[]{"walking","jumping","running"};



            int position = 1,counter=1;
            //Loop to access rows of the table
            for (int i = 0; i < 3; i++) {

                cursor = db.query(label[i]+"_"+label[i]+"_1_Male", columns, null, null, null, null, null);
                //Get columns ids of the table
                final int iX = cursor.getColumnIndex("x");
                final int iY = cursor.getColumnIndex("y");
                final int iZ = cursor.getColumnIndex("z");


                for (cursor.moveToPosition(0); !cursor.isAfterLast(); cursor.moveToNext()) {

                    tempX.add(cursor.getFloat(iX));
                    tempY.add(cursor.getFloat(iY));
                    tempZ.add(cursor.getFloat(iZ));
                    if (position == 50) {
                        query = "INSERT INTO SVM_input VALUES ("+counter+", "+
                                tempX.get(0) + ", " + tempY.get(0) + ", " + tempZ.get(0) + "," +
                                tempX.get(1) + ", " + tempY.get(1) + ", " + tempZ.get(1) + "," +
                                tempX.get(2) + ", " + tempY.get(2) + ", " + tempZ.get(2) + "," +
                                tempX.get(3) + ", " + tempY.get(3) + ", " + tempZ.get(3) + "," +
                                tempX.get(4) + ", " + tempY.get(4) + ", " + tempZ.get(4) + "," +
                                tempX.get(5) + ", " + tempY.get(5) + ", " + tempZ.get(5) + "," +
                                tempX.get(6) + ", " + tempY.get(6) + ", " + tempZ.get(6) + "," +
                                tempX.get(7) + ", " + tempY.get(7) + ", " + tempZ.get(7) + "," +
                                tempX.get(8) + ", " + tempY.get(8) + ", " + tempZ.get(8) + "," +
                                tempX.get(9) + ", " + tempY.get(9) + ", " + tempZ.get(9) + "," +
                                tempX.get(10) + ", " + tempY.get(10) + ", " + tempZ.get(10) + "," +
                                tempX.get(11) + ", " + tempY.get(11) + ", " + tempZ.get(11) + "," +
                                tempX.get(12) + ", " + tempY.get(12) + ", " + tempZ.get(12) + "," +
                                tempX.get(13) + ", " + tempY.get(13) + ", " + tempZ.get(13) + "," +
                                tempX.get(14) + ", " + tempY.get(14) + ", " + tempZ.get(14) + "," +
                                tempX.get(15) + ", " + tempY.get(15) + ", " + tempZ.get(15) + "," +
                                tempX.get(16) + ", " + tempY.get(16) + ", " + tempZ.get(16) + "," +
                                tempX.get(17) + ", " + tempY.get(17) + ", " + tempZ.get(17) + "," +
                                tempX.get(18) + ", " + tempY.get(18) + ", " + tempZ.get(18) + "," +
                                tempX.get(19) + ", " + tempY.get(19) + ", " + tempZ.get(19) + "," +
                                tempX.get(20) + ", " + tempY.get(20) + ", " + tempZ.get(20) + "," +
                                tempX.get(21) + ", " + tempY.get(21) + ", " + tempZ.get(21) + "," +
                                tempX.get(22) + ", " + tempY.get(22) + ", " + tempZ.get(22) + "," +
                                tempX.get(23) + ", " + tempY.get(23) + ", " + tempZ.get(23) + "," +
                                tempX.get(24) + ", " + tempY.get(24) + ", " + tempZ.get(24) + "," +
                                tempX.get(25) + ", " + tempY.get(25) + ", " + tempZ.get(25) + "," +
                                tempX.get(26) + ", " + tempY.get(26) + ", " + tempZ.get(26) + "," +
                                tempX.get(27) + ", " + tempY.get(27) + ", " + tempZ.get(27) + "," +
                                tempX.get(28) + ", " + tempY.get(28) + ", " + tempZ.get(28) + "," +
                                tempX.get(29) + ", " + tempY.get(29) + ", " + tempZ.get(29) + "," +
                                tempX.get(30) + ", " + tempY.get(30) + ", " + tempZ.get(30) + "," +
                                tempX.get(31) + ", " + tempY.get(31) + ", " + tempZ.get(31) + "," +
                                tempX.get(32) + ", " + tempY.get(32) + ", " + tempZ.get(32) + "," +
                                tempX.get(33) + ", " + tempY.get(33) + ", " + tempZ.get(33) + "," +
                                tempX.get(34) + ", " + tempY.get(34) + ", " + tempZ.get(34) + "," +
                                tempX.get(35) + ", " + tempY.get(35) + ", " + tempZ.get(35) + "," +
                                tempX.get(36) + ", " + tempY.get(36) + ", " + tempZ.get(36) + "," +
                                tempX.get(37) + ", " + tempY.get(37) + ", " + tempZ.get(37) + "," +
                                tempX.get(38) + ", " + tempY.get(38) + ", " + tempZ.get(38) + "," +
                                tempX.get(39) + ", " + tempY.get(39) + ", " + tempZ.get(39) + "," +
                                tempX.get(40) + ", " + tempY.get(40) + ", " + tempZ.get(40) + "," +
                                tempX.get(41) + ", " + tempY.get(41) + ", " + tempZ.get(41) + "," +
                                tempX.get(42) + ", " + tempY.get(42) + ", " + tempZ.get(42) + "," +
                                tempX.get(43) + ", " + tempY.get(43) + ", " + tempZ.get(43) + "," +
                                tempX.get(44) + ", " + tempY.get(44) + ", " + tempZ.get(44) + "," +
                                tempX.get(45) + ", " + tempY.get(45) + ", " + tempZ.get(45) + "," +
                                tempX.get(46) + ", " + tempY.get(46) + ", " + tempZ.get(46) + "," +
                                tempX.get(47) + ", " + tempY.get(47) + ", " + tempZ.get(47) + "," +
                                tempX.get(48) + ", " + tempY.get(48) + ", " + tempZ.get(48) + "," +
                                tempX.get(49) + ", " + tempY.get(49) + ", " + tempZ.get(49) + ", '"+
                                label[i]+"');";

                        db2.execSQL(query);
                        position = 0;
                        counter++;
                        tempX = new ArrayList<Float>();
                        tempY = new ArrayList<Float>();
                        tempZ = new ArrayList<Float>();
                    }

                    position++;
                }
            }
            cursor.close();
            db.setTransactionSuccessful(); //commit your changes

        } catch (SQLiteException e) {
            //Handle the error
            Log.e("Error", e.toString());
        } finally {
            db.endTransaction();
        }

    }

}