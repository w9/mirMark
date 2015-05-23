package edu.hawaii.mirMark.ui.server.actions;

import com.google.common.collect.Iterables;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import edu.hawaii.mirMark.ui.client.actions.ProjectActions;
import edu.hawaii.mirMark.ui.server.Results;
import edu.hawaii.mirMark.ui.servlets.MirMarkUploader;
import edu.hawaii.mirMark.ui.shared.UtrLevelResultEntry;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;

@SuppressWarnings ({"GwtServiceNotRegistered"})
public class ProjectActionsImpl extends RemoteServiceServlet implements ProjectActions {
    @Override
    public ArrayList<UtrLevelResultEntry> queryRefseqId(String refseqId, String threshold) {
        if(!Results.utrs.containsKey(refseqId)) return null;

        String symbol = Results.refseq2Symbol.get(refseqId);
        ArrayList<UtrLevelResultEntry> returnTable = new ArrayList<>();
        int index = Results.utrs.get(refseqId);
        Results.mirs.keySet().stream().forEach(p -> {
            if (Results.mirMarkProbs[Results.mirs.get(p)][index] > Float.parseFloat(threshold)) {
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
    public ArrayList<UtrLevelResultEntry> queryMirName(String mirName, String threshold) {
        if(!Results.mirs.containsKey(mirName)) return null;

        ArrayList<UtrLevelResultEntry> returnTable = new ArrayList<>();
        int index = Results.mirs.get(mirName);
        Results.utrs.keySet().stream().forEach(p -> {
            if (Results.mirMarkProbs[index][Results.utrs.get(p)] > Float.parseFloat(threshold)) {
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
    public ArrayList<UtrLevelResultEntry> querySymbol(String symbol, String threshold) {
        // TODO: Here we should dispatch an Event -- "The gene symbol cannot be translated to RefSeq ID."
        if (!Results.symbol2Refseq.containsKey(symbol)) return null;
        ArrayList<String> possibleRefseqIds = Results.symbol2Refseq.get(symbol);
        if (possibleRefseqIds.size() == 1) {
            return queryRefseqId(possibleRefseqIds.get(0), threshold);
        } else {
            // More than one Refseq IDs match the query Symbol
            ArrayList<UtrLevelResultEntry> resultTable = new ArrayList<>();
            for (String possibleRefseqId : possibleRefseqIds) {
                resultTable.addAll(queryRefseqId(possibleRefseqId, threshold));
            }
            return resultTable;
        }
    }
}

