package edu.hawaii.mirMark.ui.server;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;

/**
 * Created by xzhu on 5/22/15.
 */
public class Results {
    public static HashMap<String, String> refseq2Symbol;
    public static HashMap<String, ArrayList<String>> symbol2Refseq;
    public static HashMap<String, Integer> utrs;
    public static HashMap<String, Integer> mirs;
    public static float [][] mirMarkProbs;
    public static float [][] targetScanProbs;
    public static float [][] miRandaScores;

    public static void getReady() {
        try {
            // Read in UTR indices as hash table
            utrs = new HashMap<>();
            Files.readAllLines(java.nio.file.Paths.get(Paths.LIST_OF_UTRS), StandardCharsets.UTF_8)
                    .stream().map(st -> st.split("\\s+"))
                    .forEach(u -> utrs.put(u[1], Integer.parseInt(u[0])-1));

            int numUtrs = utrs.values().stream().max(Comparator.comparing(Function.identity())).get() + 1;
            System.err.println("Num utrs: " + numUtrs);



            // Read in miR indices as hash table
            mirs = new HashMap<>();
            Files.readAllLines(java.nio.file.Paths.get(Paths.LIST_OF_MIRS), StandardCharsets.UTF_8)
                    .stream().map(st -> st.split("\\s+"))
                    .forEach(m -> mirs.put(m[1], Integer.parseInt(m[0])-1));

            int numMirs = mirs.size();



            DataInputStream stream = new DataInputStream(new GZIPInputStream(new FileInputStream(Paths.MIR_MARK_MATRIX_GZ)));
            byte[] bytes = new byte[4 * numUtrs];
            FloatBuffer floatBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();

            mirMarkProbs = new float[numMirs][numUtrs];
            System.err.println("Reading in mirMark matrix ...");
            for(int i = 0; i < numMirs; i++) {
                if (i % (numMirs/10) == 0) {
                    System.err.println(i / (numMirs / 10) * 10 + "%");
                }
                stream.readFully(bytes, 0, 4 * numUtrs);
                floatBuffer.rewind();
                for (int j = 0; j < numUtrs; j++) mirMarkProbs[i][j] = floatBuffer.get();
            }
            System.err.println("Read in files: " + numMirs + " by " + numUtrs);



            stream = new DataInputStream(new GZIPInputStream(new FileInputStream(Paths.TARGET_SCAN_MATRIX_GZ)));
            bytes = new byte[4 * numUtrs];
            floatBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();

            targetScanProbs = new float[numMirs][numUtrs];
            System.err.println("Reading in targetScan matrix ...");
            for(int i = 0; i < numMirs; i++) {
                if (i % (numMirs/10) == 0) {
                    System.err.println(i / (numMirs / 10) * 10 + "%");
                }
                stream.readFully(bytes, 0, 4 * numUtrs);
                floatBuffer.rewind();
                for (int j = 0; j < numUtrs; j++) targetScanProbs[i][j] = floatBuffer.get();
            }
            System.err.println("Read in files: " + numMirs + " by " + numUtrs);



            stream = new DataInputStream(new GZIPInputStream(new FileInputStream(Paths.MIRANDA_MATRIX_GZ)));
            bytes = new byte[4 * numUtrs];
            floatBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();

            miRandaScores = new float[numMirs][numUtrs];
            System.err.println("Reading in miRanda matrix ...");
            for(int i = 0; i < numMirs; i++) {
                if (i % (numMirs/10) == 0) {
                    System.err.println(i / (numMirs / 10) * 10 + "%");
                }
                stream.readFully(bytes, 0, 4 * numUtrs);
                floatBuffer.rewind();
                for (int j = 0; j < numUtrs; j++) miRandaScores[i][j] = floatBuffer.get();
            }
            System.err.println("Read in files: " + numMirs + " by " + numUtrs);



            refseq2Symbol = new HashMap<>();
            symbol2Refseq = new HashMap<>();
            Files.readAllLines(java.nio.file.Paths.get(Paths.UTR_METADATA), StandardCharsets.UTF_8)
                    .stream().map(st -> st.split("\t"))
                    .forEach(u -> {
                        if (symbol2Refseq.get(u[1]) == null) {
                            symbol2Refseq.put(u[1], new ArrayList<String>() {{
                                add(u[0]);
                            }});
                        } else {
                            symbol2Refseq.get(u[1]).add(u[0]);
                        }

                        refseq2Symbol.put(u[0], u[1]);
                    });

        } catch(Exception exc) {
            exc.printStackTrace();
        }
    }
}
