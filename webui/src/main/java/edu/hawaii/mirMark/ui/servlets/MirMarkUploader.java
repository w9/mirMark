package edu.hawaii.mirMark.ui.servlets;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.FileUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

public class MirMarkUploader extends HttpServlet {
    public static final String BASE_DIRECTORY = System.getProperty("user.home") + "/data/mirMark";

    public void doPost(HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException {

        ServletFileUpload upload = new ServletFileUpload();

        String ip = request.getRemoteAddr();
        String time = "" + System.currentTimeMillis();

        String directory = BASE_DIRECTORY + "/" + ip + "/" + time;
        FileUtils.mkdir(directory);

        boolean canCreate = true;

        try {
            FileItemIterator iter = upload.getItemIterator(request);

            boolean hasPairsFile = false;

            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                String nameFormElement = item.getFieldName();
//                String nameFile = item.getName();
                InputStream stream = item.openStream();
                File outFile = new File(directory + "/" + nameFormElement + ".txt");
                canCreate = canCreate && outFile.createNewFile();
                hasPairsFile = hasPairsFile || (nameFormElement.equals("pairs") && stream.available() > 0);
                IOUtils.copy(stream, new FileOutputStream(outFile));
            }
            if(!hasPairsFile) {
                Process p = new ProcessBuilder("generatePairs.pl", "mirna.txt", "utr.txt", "pairs.txt").directory(new File(directory)).redirectErrorStream(true).start();
                p.waitFor();
            }
        } catch(Exception e){
            throw new RuntimeException(e);
        }

        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            out.println("" + canCreate + "," + ip + "," + time);
        } finally {
            out.close();
        }

    }

}
