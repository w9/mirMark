package edu.hawaii.mirMark.ui.server.actions;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import edu.hawaii.mirMark.ui.client.actions.ProjectActions;
import edu.hawaii.mirMark.ui.servlets.MirMarkUploader;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.function.Function;

@SuppressWarnings ({"GwtServiceNotRegistered"})
public class ProjectActionsImpl extends RemoteServiceServlet implements ProjectActions {
    public float [][] mirsToUtrs;
    public HashMap<String, Integer> utrs = new HashMap<>();
    public HashMap<String, Integer> mirs = new HashMap<>();
    public String matrixFeaturesFile = MirMarkUploader.BASE_DIRECTORY + "/utr_features.matrix";
    public String matrixUtrsFile = matrixFeaturesFile + ".utrs";
    public String matrixMirsFile = matrixFeaturesFile + ".mirs";
    private int numUtrs = 0;

    public ProjectActionsImpl() {
        System.out.println(System.getProperty("user.dir"));
        try {
            Files.readAllLines(Paths.get(matrixUtrsFile), StandardCharsets.UTF_8)
                    .stream().map(st -> st.split("\\s+"))
                    .forEach(u -> utrs.put(u[0], Integer.parseInt(u[1])));

            numUtrs = utrs.values().stream().max(Comparator.comparing(Function.identity())).get() + 1;
            boolean [] checked = new boolean[numUtrs];
            Arrays.fill(checked, Boolean.FALSE);
            utrs.values().stream().forEach(p -> checked[p] = true);
            System.err.println("Num utrs" +
                    ": " + numUtrs);

            Files.readAllLines(Paths.get(matrixMirsFile), StandardCharsets.UTF_8)
                    .stream().map(st -> st.split("\\s+"))
                    .forEach(m -> mirs.put(m[0], Integer.parseInt(m[1])));

            RandomAccessFile ras = new RandomAccessFile(matrixFeaturesFile, "r");
            FileChannel chan = ras.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(4*numUtrs);
//            DataInputStream dis = new DataInputStream(new FileInputStream(matrixFeaturesFile));
            float f;
            int i = 0, j = 0;
            mirsToUtrs = new float[mirs.size()][numUtrs];
            System.err.println("Reading in file...");
            boolean tenpercent = false;
            boolean half = false;
            for(i = 0; i < mirs.size(); i++) {
                if(!tenpercent && i/(1.0+mirs.size()) > 0.10) {
                    System.err.println("Ten percent reached: " + i + "/" + mirs.size());
                    tenpercent = true;
                }
                if(!half && i/(1.0+mirs.size()) > 0.50) {
                    System.err.println("Halfway reached: " + i + "/" + mirs.size());
                    half = true;
                }
                chan.read(buffer);
                buffer.flip();
                FloatBuffer floatBuffer = buffer.order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
                for(j = 0; j < numUtrs; j++) {
                    if(checked[j])
                        mirsToUtrs[i][j] = floatBuffer.get();
                }
            }
            System.err.println("Read in files: " + mirs.size() + " by " + numUtrs + " i=" + i + " j=" + j);
        } catch(Exception exc) {
            exc.printStackTrace();
        }
    }

    @Override
    public String executeSite(String addressId, String timeId) {
        try {
            String directory = MirMarkUploader.BASE_DIRECTORY + "/" + addressId + "/"  + timeId + "/";
            Process p = new ProcessBuilder("siteFeaturesARFF.pl", "mirna.txt", "utr.txt", "fasta.txt", "pairs.txt", "site_features", "../../rf.site.model").directory(new File(directory)).redirectErrorStream(true).start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            BufferedReader brI = new BufferedReader(new InputStreamReader(p.getInputStream()));
            p.waitFor();
            String result = "Error log: ";
            String line;
            while((line = br.readLine()) != null) { result = result + line; }
            while((line = brI.readLine()) != null) { result = result + line; }
            if(p.exitValue() != 0) {
                return result;
            }
            return new String(Files.readAllBytes(Paths.get(directory + "site_features.result")), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "Error in execution: " + e.getMessage();
        }
    }

    @Override
    public String executeUTR(String addressId, String timeId) {
        try {
            String directory = MirMarkUploader.BASE_DIRECTORY + "/" + addressId + "/"  + timeId + "/";
            new ProcessBuilder("utrFeaturesARFF.pl", "mirna.txt", "utr.txt", "fasta.txt", "pairs.txt", "utr_features",
                    "../../rf.utr.model").directory(new File(directory)).start().waitFor();
            return new String(Files.readAllBytes(Paths.get(directory + "utr_features.result")), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Override
    public HashMap<String, Float> selectUTR(String utrSelectText) {
        if(!utrs.containsKey(utrSelectText)) return null;
        HashMap<String, Float> returnValue = new HashMap<>();
        int index = utrs.get(utrSelectText);
        mirs.keySet().stream().forEach(p -> returnValue.put(p, mirsToUtrs[mirs.get(p)][index]));
        return returnValue;
    }

    @Override
    public HashMap<String, Float> selectMir(String mirSelectText) {
        if(!mirs.containsKey(mirSelectText)) return null;
        HashMap<String, Float> returnValue = new HashMap<>();
        int index = mirs.get(mirSelectText);
        utrs.keySet().stream().forEach(p -> returnValue.put(p, mirsToUtrs[index][utrs.get(p)]));
        return returnValue;
    }
}

