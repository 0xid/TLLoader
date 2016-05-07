package net.ithard.tlloader;

import android.content.ComponentName;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Process;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView textView;
    Switch switch1;
    Switch switch2;
    Button startBtn;
    Button unBtn;
    Button reboot;
    Process p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);

        //События - инит
        switch1 = (Switch) findViewById(R.id.switch1);
        switch1.setEnabled(false);
        switch1.setOnClickListener(this);

        switch2 = (Switch) findViewById(R.id.switch2);
        switch2.setEnabled(false);
        switch2.setOnClickListener(this);

        startBtn = (Button) findViewById(R.id.startBtn);
        startBtn.setEnabled(false);
        startBtn.setOnClickListener(this);

        unBtn = (Button) findViewById(R.id.unBtn);
        unBtn.setEnabled(false);
        unBtn.setOnClickListener(this);

        reboot = (Button) findViewById(R.id.reboot);
        reboot.setEnabled(true);
        reboot.setOnClickListener(this);

        initFiles();

        //Проверяем наличие директории и файлов
        if (isDirAndFiles()) {
            switch1.setEnabled(true);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.switch1:
                if(switch1.isChecked()){
                    if(isLoked()) {
                        unBtn.setEnabled(true);
                    }else {
                        startBtn.setEnabled(true);
                    }
                    switch2.setEnabled(true);
                }else{
                    startBtn.setEnabled(false);
                    unBtn.setEnabled(false);
                    switch2.setEnabled(false);
                    switch2.setChecked(false);
                }
                break;
            case R.id.switch2:
                if(switch2.isChecked()){
                    unBtn.setEnabled(true);
                    startBtn.setEnabled(true);
                }else{
                    switch1.setChecked(false);
                    startBtn.setEnabled(false);
                    unBtn.setEnabled(false);
                    switch2.setEnabled(false);
                }
                break;
            case R.id.startBtn:
                try {
                    locked();
                    switch1.setChecked(false);
                    startBtn.setEnabled(false);
                    unBtn.setEnabled(false);
                    switch2.setEnabled(false);
                    switch2.setChecked(false);
                } catch (IOException e) {
                    textView.setText("Ошибка: "+e.getMessage());
                    textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                    e.printStackTrace();
                }
                break;
            case R.id.unBtn:
                try {
                    unlocked();
                    switch1.setChecked(false);
                    startBtn.setEnabled(false);
                    unBtn.setEnabled(false);
                    switch2.setEnabled(false);
                    switch2.setChecked(false);
                } catch (IOException e) {
                    textView.setText("Ошибка: "+e.getMessage());
                    textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                    e.printStackTrace();
                }
                break;
            case R.id.reboot:
                startReboot();
                break;
        }
    }

    private boolean isDirAndFiles(){
        Boolean pathFrom = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+getString(R.string.pathFrom)).isDirectory();
        if(!pathFrom){
            textView.setText("Нет директории: " + Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+getString(R.string.pathFrom));
            textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
            return false;
        }
        Boolean flashifwi = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+getString(R.string.pathFrom)+"/"+getString(R.string.flashifwi)).exists();
        if(!flashifwi){
            textView.setText("Нет файла: " + Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+getString(R.string.pathFrom)+"/"+getString(R.string.flashifwi));
            textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
            return false;
        }
        Boolean iflocked = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+getString(R.string.pathFrom)+"/"+getString(R.string.iflocked)).isFile();
        if(!iflocked){
            textView.setText("Нет файла: " + Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+getString(R.string.pathFrom)+"/"+getString(R.string.iflocked));
            textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
            return false;
        }
        Boolean ifunlocked = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+getString(R.string.pathFrom)+"/"+getString(R.string.ifunlocked)).isFile();
        if(!ifunlocked){
            textView.setText("Нет файла: " + Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+getString(R.string.pathFrom)+"/"+getString(R.string.ifunlocked));
            textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
            return false;
        }
        textView.setText("Все файлы на месте: " + "'" + getString(R.string.flashifwi) + "', '" + getString(R.string.iflocked) + "', '" + getString(R.string.ifunlocked) + "'");
        textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.accent_material_light));
        return true;
    }

    private Boolean isLoked(){
        Boolean is = false;
        textView.setText("");
        String md5One = getString(R.string.md5Loked);

        String fileTestOne = getString(R.string.fileTestOne);
        String fileTestTwo = getString(R.string.fileTestTwo);

        String[] comm = new String[]{"su", "-", "root", "-c", "md5", fileTestOne};
        String md5CurrentOne = suExec(comm);

        comm = new String[]{"su", "-", "root", "-c", "md5", fileTestTwo};
        String md5CurrentTwo = suExec(comm);

        if(md5One.equals(md5CurrentOne.toString().substring(0, 32)) && md5One.equals(md5CurrentTwo.toString().substring(0, 32))){
            is = true;
        }
        return is;
    }

    private void startReboot(){
        String[] comm = new String[]{"su", "-", "root", "-c", "reboot"};
        String runCmd = suExec(comm);
    }

    private void locked() throws IOException {
        textView.setText("");
        String path = "/data/media/0/"+getString(R.string.pathFrom);
        String fileStart = path+"/"+getString(R.string.flashifwi);
        String fileBin = path+"/"+getString(R.string.iflocked);

        String[] comm = new String[]{"su", "-", "root", "-c", "chmod", "777", fileStart};
        String runCmd = suExec(comm);

        //flash-ifwi --flash-ifwi /storage/emulated/0/ifwi/if-locked.bin
        comm = new String[]{"su", "-", "root", "-c", fileStart, "--flash-ifwi", fileBin};
        runCmd = suExec(comm);
    }

    private void unlocked() throws IOException {
        textView.setText("");
        String path = "/data/media/0/"+getString(R.string.pathFrom);
        String fileStart = path+"/"+getString(R.string.flashifwi);
        String fileBin = path+"/"+getString(R.string.ifunlocked);

        String[] comm = new String[]{"su", "-", "root", "-c", "chmod", "777", fileStart};
        String runCmd = suExec(comm);

        ////flash-ifwi --flash-ifwi /storage/emulated/0/ifwi/if-locked.bin
        comm = new String[]{"su", "-", "root", "-c", fileStart, "--flash-ifwi", fileBin};
        runCmd = suExec(comm);
    }

    public String suExec(String[] cmd){
        try {
            //Выполняем комманду
            java.lang.Process process = Runtime.getRuntime().exec(cmd);
            //Читаем вывод
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            int read;
            char[] buffer = new char[16384];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0){
                output.append(buffer, 0, read);
            }
            reader.close();
            //Ждём завершения процесса
            process.waitFor();
            textView.setText(textView.getText().toString() + "\n" + output.toString());
            return output.toString();
        }catch (IOException e){
            textView.setText(e.toString());
            textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
            throw new RuntimeException(e);
        }catch (InterruptedException e){
            textView.setText(e.toString());
            textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
            throw new RuntimeException(e);
        }
    }

    public void saveResource(Integer resId, String pathSave) throws IOException {
        InputStream ins = getResources().openRawResource(resId);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int size = 0;
// Read the entire resource into a local byte buffer.
        byte[] buffer = new byte[1024];
        while( (size = ins.read(buffer,0,1024) ) >= 0 ){
            outputStream.write(buffer,0,size);
        }
        ins.close();
        buffer=outputStream.toByteArray();
        FileOutputStream fos = new FileOutputStream(pathSave);
        fos.write(buffer);
        fos.close();
    }

    public void initFiles(){
        Boolean dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+getString(R.string.pathFrom)).isDirectory();
        if(!dir){
            new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+getString(R.string.pathFrom)).mkdir();
        }
        String fileFlashifwi = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+getString(R.string.pathFrom)+"/"+getString(R.string.flashifwi);
        try {
            saveResource(R.raw.flashifwi, fileFlashifwi);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String fieIfLocked = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+getString(R.string.pathFrom)+"/"+getString(R.string.iflocked);
        try {
            saveResource(R.raw.iflocked, fieIfLocked);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String fileIfUnLocked = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+getString(R.string.pathFrom)+"/"+getString(R.string.ifunlocked);
        try {
            saveResource(R.raw.ifunlocked, fileIfUnLocked);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}