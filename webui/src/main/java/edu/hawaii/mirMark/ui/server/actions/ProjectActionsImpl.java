package edu.hawaii.mirMark.ui.server.actions;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import edu.hawaii.mirMark.ui.client.actions.ProjectActions;
import edu.hawaii.mirMark.ui.servlets.MirMarkUploader;
import edu.hawaii.mirMark.ui.shared.UtrLevelResultEntry;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

@SuppressWarnings ({"GwtServiceNotRegistered"})
public class ProjectActionsImpl extends RemoteServiceServlet implements ProjectActions {
    private final String utrDictionaryFILE = MirMarkUploader.BASE_DIRECTORY + "/UTR_dictionary.txt";
    private HashMap<String, String> refseq2Symbol = new HashMap<>();
    private HashMap<String, String> symbol2Refseq = new HashMap<>();
    public float [][] mirsToUtrs;
    public HashMap<String, Integer> utrs = new HashMap<>();
    public HashMap<String, Integer> mirs = new HashMap<>();
    public String matrixFeaturesFile = MirMarkUploader.BASE_DIRECTORY + "/utr_features.matrix";
    public String matrixUtrsFile = matrixFeaturesFile + ".utrs";
    public String matrixMirsFile = matrixFeaturesFile + ".mirs";
    private int numUtrs = 0;

    public ProjectActionsImpl() {
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
            //DataInputStream dis = new DataInputStream(new FileInputStream(matrixFeaturesFile));
            float f;
            int i = 0, j = 0;
            mirsToUtrs = new float[mirs.size()][numUtrs];
            System.err.println("Reading in file...");
            for(i = 0; i < mirs.size(); i++) {
                if (i % (mirs.size()/10) == 0) {
                    System.err.println(i / (mirs.size() / 10) + "0%");
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

        // Create HashMap -- "GeneSymbol2UTR"
        try {
            Files.readAllLines(Paths.get(utrDictionaryFILE), StandardCharsets.UTF_8)
                    .stream().map(st -> st.split("\t"))
                    .forEach(u -> {
                        symbol2Refseq.put(u[1], u[0]);
                        refseq2Symbol.put(u[0], u[1]);
                    });
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
    public ArrayList<UtrLevelResultEntry> queryRefseqId(String refseqId, String threshold) {
        if(!utrs.containsKey(refseqId)) return null;

        String symbol = refseq2Symbol.get(refseqId);
        ArrayList<UtrLevelResultEntry> returnTable = new ArrayList<>();
        int index = utrs.get(refseqId);
        mirs.keySet().stream().forEach(p -> {
            if (mirsToUtrs[mirs.get(p)][index] > Float.parseFloat(threshold)) {
                returnTable.add(new UtrLevelResultEntry(symbol, refseqId, p, mirsToUtrs[mirs.get(p)][index]));
            }
        });
        System.out.println("returnTable.size() = " + returnTable.size());
        return returnTable;
    }

    @Override
    public ArrayList<UtrLevelResultEntry> queryMirName(String mirName, String threshold) {
        if(!mirs.containsKey(mirName)) return null;

        ArrayList<UtrLevelResultEntry> returnTable = new ArrayList<>();
        int index = mirs.get(mirName);
        utrs.keySet().stream().forEach(p -> {
            if (mirsToUtrs[index][utrs.get(p)] > Float.parseFloat(threshold)) {
                String pSymbol = refseq2Symbol.get(p);
                if (pSymbol == null) pSymbol = "N/A";
                returnTable.add(new UtrLevelResultEntry(pSymbol, p, mirName, mirsToUtrs[index][utrs.get(p)]));
            }
        });
        return returnTable;
    }

    @Override
    public ArrayList<UtrLevelResultEntry> querySymbol(String symbol, String threshold) {
        // TODO: Here we should dispatch an Event -- "The gene symbol cannot be translated to RefSeq ID."
        if (!symbol2Refseq.containsKey(symbol)) return null;
        String refseqId = symbol2Refseq.get(symbol);

        return queryRefseqId(refseqId, threshold);
    }
}

