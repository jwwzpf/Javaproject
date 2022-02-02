package de.itdesign.application;

public class XMLCalculator {

    public static void main(String[] args) {
        //Don't change this part
        if (args.length == 3) {
            //Path to the data file, e.g. data/data.xml
            final String DATA_FILE = args[0];
            //Path to the data file, e.g. operations/operations.xml
            final String OPERATIONS_FILE = args[1];
            //Path to the output file
            final String OUTPUT_FILE = args[2];

            DataProcessor dataProcessor = new DataProcessor(DATA_FILE, OPERATIONS_FILE, OUTPUT_FILE);
            dataProcessor.processInputAndOutputResult();
        } else {
            System.exit(1);
        }

    }

}
