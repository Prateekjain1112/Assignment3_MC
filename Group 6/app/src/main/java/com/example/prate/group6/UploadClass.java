package com.example.prate.group6;

/**
 * Created by prate on 14-03-2018.
 */
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
public class UploadClass extends Thread{

    String filepath;
    String output;
    int serverResponseCode = 0;

    UploadClass(String filepath)
    {
        this.filepath=filepath;
    }

    public void run() {
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(filepath);


        if (!sourceFile.isFile()) {



            Log.e("uploadFile", "Source File not exist :"
                    +filepath);

        }
        else
        {

            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL("http://impact.asu.edu/CSE535Spring18Folder/UploadToServer.php");

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);



                dos = new DataOutputStream(conn.getOutputStream());


                // Send parameter #1
                //Parameter1  contains value of Filepath to Group6.db
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\"" + lineEnd + lineEnd);
                dos.writeBytes(filepath.replace("Group6.db","") + lineEnd);

                // Send parameter #2
                //Parameter 2 have the filename that needs to uploaded
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"name\"" + lineEnd + lineEnd);
                dos.writeBytes("Group6.db" + lineEnd);

                // Send a binary file
                //Parameter 3 has the value of name of binary file that contains all the data that needs to be uploaded
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + "Group6.db" +"\"" + lineEnd);
                dos.writeBytes(lineEnd);


                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }


                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if(serverResponseCode == 200){

                    output= "Database File uploaded";
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {


                ex.printStackTrace();

                output= "Error in uploading";


            } catch (Exception e) {


                e.printStackTrace();

                output= "Error in uploading ";
            }


        }


    }

    public String getOutput()
    {
        return output;
    }          //returns the output depending on whether file is downloaded or not
}
