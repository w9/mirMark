package edu.hawaii.mirMark.ui.server.actions;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import edu.hawaii.mirMark.ui.client.actions.ProjectActions;
import edu.hawaii.mirMark.ui.server.Results;
import edu.hawaii.mirMark.ui.shared.SiteLevelResultEntry;
import edu.hawaii.mirMark.ui.shared.UtrLevelResultEntry;
import org.bson.Document;

import java.util.ArrayList;

import static com.mongodb.client.model.Filters.*;


public class ProjectActionsImpl extends RemoteServiceServlet implements ProjectActions {
    private float[][] getMatrixFromMethod(String methodOfChoice) {
        switch (methodOfChoice) {
            case Methods.MIR_MARK:
                return Results.mirMarkProbs;
            case Methods.TARGET_SCAN:
                return Results.targetScanProbs;
            case Methods.MIRANDA:
                return Results.miRandaScores;
            case Methods.MIRTARBASE:
                return Results.mirTarBasePositives;
            default:
                return Results.mirMarkProbs;
        }
    }

    public class Methods {
        public static final String MIR_MARK = "mirMark";
        public static final String TARGET_SCAN = "targetScan";
        public static final String MIRANDA = "miRanda";
        public static final String MIRTARBASE = "mirTarBase";
    }

    @Override
    public ArrayList<UtrLevelResultEntry> queryRefseqId(String refseqId, String threshold, String methodOfChoice) {
        float[][] matrixToBeFilter = getMatrixFromMethod(methodOfChoice);

        if(!Results.utrs.containsKey(refseqId)) return null;

        // TODO: Do not return more than 1000 results.
        String symbol = Results.refseq2Symbol.get(refseqId);
        ArrayList<UtrLevelResultEntry> returnTable = new ArrayList<>();
        int index = Results.utrs.get(refseqId);
        Results.mirs.keySet().stream().forEach(p -> {
            if (matrixToBeFilter[Results.mirs.get(p)][index] >= Float.parseFloat(threshold)) {
                returnTable.add(new UtrLevelResultEntry(
                        symbol,
                        refseqId,
                        p,
                        Results.mirMarkProbs[Results.mirs.get(p)][index],
                        Results.targetScanProbs[Results.mirs.get(p)][index],
                        Results.miRandaScores[Results.mirs.get(p)][index],
                        Results.mirTarBasePositives[Results.mirs.get(p)][index]
                ));
            }
        });
        System.out.println("returnTable.size() = " + returnTable.size());
        return returnTable;
    }

    @Override
    public ArrayList<UtrLevelResultEntry> queryMirName(String mirName, String threshold, String methodOfChoice) {
        float[][] matrixToBeFilter = getMatrixFromMethod(methodOfChoice);

        if(!Results.mirs.containsKey(mirName)) return null;

        ArrayList<UtrLevelResultEntry> returnTable = new ArrayList<>();
        int index = Results.mirs.get(mirName);
        Results.utrs.keySet().stream().forEach(p -> {
            if (matrixToBeFilter[index][Results.utrs.get(p)] >= Float.parseFloat(threshold)) {
                String pSymbol = Results.refseq2Symbol.get(p);
                if (pSymbol == null) pSymbol = "N/A";
                returnTable.add(new UtrLevelResultEntry(
                        pSymbol,
                        p,
                        mirName,
                        Results.mirMarkProbs[index][Results.utrs.get(p)],
                        Results.targetScanProbs[index][Results.utrs.get(p)],
                        Results.miRandaScores[index][Results.utrs.get(p)],
                        Results.mirTarBasePositives[index][Results.utrs.get(p)]
                ));
            }
        });
        return returnTable;
    }

    @Override
    public ArrayList<UtrLevelResultEntry> querySymbol(String symbol, String threshold, String methodOfChoice) {
        // TODO: Here we should dispatch an Event -- "The gene symbol cannot be translated to RefSeq ID."
        if (!Results.symbol2Refseq.containsKey(symbol)) return null;
        ArrayList<String> possibleRefseqIds = Results.symbol2Refseq.get(symbol);
        if (possibleRefseqIds.size() == 1) {
            return queryRefseqId(possibleRefseqIds.get(0), threshold, methodOfChoice);
        } else {
            // More than one Refseq IDs match the query Symbol
            ArrayList<UtrLevelResultEntry> resultTable = new ArrayList<>();
            for (String possibleRefseqId : possibleRefseqIds) {
                resultTable.addAll(queryRefseqId(possibleRefseqId, threshold, methodOfChoice));
            }
            return resultTable;
        }
    }

    @Override
    public String[] getSymbols() {
        return Results.symbol2Refseq.keySet().toArray(new String[Results.symbol2Refseq.keySet().size()]);
    }

    @Override
    public String[] getRefseqs() {
        return Results.utrs.keySet().toArray(new String[Results.utrs.keySet().size()]);
    }

    @Override
    public String[] getMirNames() {
        return Results.mirs.keySet().toArray(new String[Results.mirs.keySet().size()]);
    }

    @Override
    public ArrayList<SiteLevelResultEntry> querySiteLevel(String utrRefSeq, String threshold) {
//        ArrayList<SiteLevelResultEntry> entries = new ArrayList<>();
//        ArrayList<SiteLevelResultEntry.Site> sites = new ArrayList<>();
//        sites.add(new SiteLevelResultEntry.Site("hsa-miR-HELLO", 13, 54, (float) 0.44));
//        entries.add(new SiteLevelResultEntry(500, 1000, sites));


        MongoClient mongoClient = new MongoClient("127.0.0.1");
        MongoDatabase mirmarkDB = mongoClient.getDatabase("mirmark");
        MongoCollection<Document> sitesColl = mirmarkDB.getCollection("sites");
        MongoCollection<Document> utrsColl = mirmarkDB.getCollection("utrs");

        ArrayList<Document> utrDocuments = utrsColl.find(new Document("refseq", utrRefSeq)).into(new ArrayList<>());

        System.out.println("utrDocuments.size() = " + utrDocuments.size());

        ArrayList<SiteLevelResultEntry> entries = new ArrayList<>();
        
        for (Document utrDocument : utrDocuments) {
            int utrId = (int) utrDocument.get("_id");
            ArrayList<Document> siteDocuments = sitesColl.find(and(eq("utr_id", utrId), gte("probability", Double.parseDouble(threshold)))).into(new ArrayList<>());
            System.out.println("siteDocuments.size() = " + siteDocuments.size());

            ArrayList<SiteLevelResultEntry.Site> sites = new ArrayList<>();
            for (Document siteDocument : siteDocuments) {
                SiteLevelResultEntry.Site site = new SiteLevelResultEntry.Site(
                        Integer.toString((int) siteDocument.get("mir_id")),
                        (int) siteDocument.get("start"),
                        (int) siteDocument.get("end"),
                        (float) (double) siteDocument.get("probability"));
                sites.add(site);
            }

            SiteLevelResultEntry entry = new SiteLevelResultEntry((String) utrDocument.get("chr"), (int) utrDocument.get("start"), (int) utrDocument.get("end"), sites);
            entries.add(entry);
        }


        return entries;
    }
}

