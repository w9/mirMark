package edu.hawaii.mirMark.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

// TODO: Maybe we should use XML or JSON instead of RMI
// TODO: And Even change the structure of the project so that server-side and client-side are separated.
public class UtrLevelResultEntry implements IsSerializable {
    public String geneSymbol;
    public String geneRefseqId;
    public String mirName;
    public float mirMarkProb;
    public float targetScanProb;
    public float miRandaScore;

    public UtrLevelResultEntry(String geneSymbol, String geneRefseqId, String mirName, float mirMarkProb, float targetScanProb, float miRandaScore) {
        this.geneSymbol = geneSymbol;
        this.geneRefseqId = geneRefseqId;
        this.mirName = mirName;
        this.mirMarkProb = mirMarkProb;
        this.targetScanProb = targetScanProb;
        this.miRandaScore = miRandaScore;
    }

    public UtrLevelResultEntry() {
    }
}
