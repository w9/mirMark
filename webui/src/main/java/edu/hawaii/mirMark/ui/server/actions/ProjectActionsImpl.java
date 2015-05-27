package edu.hawaii.mirMark.ui.server.actions;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import edu.hawaii.mirMark.ui.client.actions.ProjectActions;
import edu.hawaii.mirMark.ui.server.Results;
import edu.hawaii.mirMark.ui.shared.UtrLevelResultEntry;

import java.util.*;

@SuppressWarnings ({"GwtServiceNotRegistered"})
public class ProjectActionsImpl extends RemoteServiceServlet implements ProjectActions {
    private float[][] getMatrixFromMethod(String methodOfChoice) {
        switch (methodOfChoice) {
            case Methods.MIR_MARK:
                return Results.mirMarkProbs;
            case Methods.TARGET_SCAN:
                return Results.targetScanProbs;
            case Methods.MIRANDA:
                return Results.miRandaScores;
            default:
                return Results.mirMarkProbs;
        }
    }

    public class Methods {
        public static final String MIR_MARK = "mirMark";
        public static final String TARGET_SCAN = "targetScan";
        public static final String MIRANDA = "miRanda";
    }

    @Override
    public ArrayList<UtrLevelResultEntry> queryRefseqId(String refseqId, String threshold, String methodOfChoice) {
        float[][] matrixToBeFilter = getMatrixFromMethod(methodOfChoice);

        if(!Results.utrs.containsKey(refseqId)) return null;

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
                        Results.miRandaScores[Results.mirs.get(p)][index]
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
                        Results.miRandaScores[index][Results.utrs.get(p)]
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
}

