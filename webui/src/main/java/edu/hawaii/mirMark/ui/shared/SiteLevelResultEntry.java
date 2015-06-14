package edu.hawaii.mirMark.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;

/**
 * Created by xzhu on 6/13/15.
 */
public class SiteLevelResultEntry implements IsSerializable {
    public String chr;
    public int start;
    public int end;
    public ArrayList<Site> sites;

    public SiteLevelResultEntry(String chr, int start, int end, ArrayList<Site> sites) {
        this.chr = chr;
        this.start = start;
        this.end = end;
        this.sites = sites;
    }

    public SiteLevelResultEntry() {
    }

    public static class Site implements IsSerializable {
        public String mirName;
        public int start;
        public int end;
        public float probability;

        public Site(String mirName, int start, int end, float probability) {
            this.mirName = mirName;
            this.start = start;
            this.end = end;
            this.probability = probability;
        }

        public Site() {
        }
    }
}
