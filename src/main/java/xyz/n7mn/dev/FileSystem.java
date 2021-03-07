package xyz.n7mn.dev;

import java.io.*;
import java.nio.charset.StandardCharsets;

class FileSystem {

    public static String fileRead(String filePass){
        File file = new File(filePass);
        if (!file.isFile()){
            System.out.println("存在しないファイル : " + filePass);
            return "";
        }

        BufferedReader buffer = null;
        String json;
        try {
            FileInputStream input = new FileInputStream(file);
            InputStreamReader stream = new InputStreamReader(input, StandardCharsets.UTF_8);
            buffer = new BufferedReader(stream);
            StringBuffer sb = new StringBuffer();

            int ch = buffer.read();
            while (ch != -1){
                sb.append((char) ch);
                ch = buffer.read();
            }

            json = sb.toString();

        } catch (FileNotFoundException e) {
            System.out.println("存在しないファイル : " + filePass);
            json = "";
        } catch (IOException e) {
            e.printStackTrace();
            json = "";
        } finally {
            try {
                if (buffer != null){
                    buffer.close();
                }
            } catch (IOException e) {
                json = "";
            }
        }

        return json;
    }

    public static void fileWrite(String filePass, String content){

        try {
            PrintWriter p_writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePass), StandardCharsets.UTF_8)));
            p_writer.print(content);
            p_writer.close();
        } catch (FileNotFoundException e) {
            // e.printStackTrace();
        }

    }
}
