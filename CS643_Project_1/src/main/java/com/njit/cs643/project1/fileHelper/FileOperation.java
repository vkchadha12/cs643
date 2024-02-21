package com.njit.cs643.project1.fileHelper;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileOperation {
    public FileWriter getFileWriter(String fileName ) throws IOException {
        if(Files.exists(Paths.get(fileName)))
            return  new FileWriter(fileName, Boolean.TRUE);
        return new FileWriter(fileName);
    }

    public void  deleteFile(String fileName) throws IOException {
        if(Files.exists(Paths.get(fileName)))
            Files.delete(Path.of(fileName));
    }

    public  void write(String fileName , String[] data)
            throws IOException
    {
        // initialize a string
        try {
            FileWriter fw = getFileWriter(fileName);
            // read each character from string and write
            // into FileWriter
            for (int i = 0; i < data.length; i++)
                fw.write(data[i] );
            // close the file
            fw.close();
        }
        catch (Exception e) {
            e.getStackTrace();
        }
    }
}
