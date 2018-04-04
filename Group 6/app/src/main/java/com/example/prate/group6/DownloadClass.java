package com.example.prate.group6;

/**
 * Created by prate on 14-03-2018.
 */
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
public class DownloadClass extends Thread{

    String output;
    boolean download = true;


    public void run() {
        {
            File dir = new File(Environment.getExternalStorageDirectory()+"/Android/data","CSE535_ASSIGNMENT2_DOWN");
            try {
                if (!dir.exists()) {
                    dir.mkdir();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            //download the file
            try {
                InputStream input = null;
                try {
                    URL url = new URL("http://impact.asu.edu/CSE535Spring18Folder/Group6.db"); // link of the file which has to be downloaded)
                    input = url.openStream();
                    OutputStream output = new FileOutputStream(new File(dir, "Group6.db"));   //open output stream to file group6.db
                    download = true;
                    try {
                        byte[] buffer = new byte[1024];
                        int bytesRead = 0;
                        while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
                            output.write(buffer, 0, bytesRead);
                            download = true;
                        }
                        output.close();
                    } catch (Exception exception) {

                        download = false;

                    }
                } catch (Exception exception) {
                    output = "error";
                    download = false;
                } finally {
                    input.close();
                }
            } catch (Exception exception) {
                download = false;

            }

        }
    }
    public String getOutput()                      //give the output whether the file is downloaded correct or not
    {
        if(download==true)
            return "File dowloaded";
        else
            return "error in downloading";
    }
}
