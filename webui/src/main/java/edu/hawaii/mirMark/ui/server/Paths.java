package edu.hawaii.mirMark.ui.server;

/**
 * Created by xzhu on 5/21/15.
 */
public class Paths {
    public static final String BASE_DIRECTORY = System.getProperty("user.home") + "/data/mirMark";
    public static final String MIR_MARK_MATRIX_GZ = BASE_DIRECTORY + "/mirMark_utr_svm_2578_42677.matrix.gz";
    public static final String TARGET_SCAN_MATRIX_GZ = BASE_DIRECTORY + "/targetScan_2578_42677.matrix.gz";
    public static final String LIST_OF_MIRS = BASE_DIRECTORY + "/list_of_mirs.txt";
    public static final String LIST_OF_UTRS = BASE_DIRECTORY + "/list_of_utrs.txt";
    public static final String UTR_METADATA = BASE_DIRECTORY + "/UTR_dictionary.txt";
    public static final String MIRANDA_MATRIX_GZ = BASE_DIRECTORY + "/miRanda_2578_42677.matrix.gz";
}
