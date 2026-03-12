package src.main;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

import src.graph.DatasetNFT;
import src.graph.Edge;
import src.graph.Graph;
import src.io.Loader;
import src.io.ResultWriter;
import src.io.TimeFormatter;
import src.motifMiner.GraphTemporalMotifMiner;
import src.motifMiner.NFTsTemporalMotifMiner;
import src.motifMiner.patterns.*;

public class Main {

    public static void main(String[] args) {

        // ===== PARAMETRI GLOBALI =====
        //int minOneWay = 2;
        int minMergedInStar = 2;
        int minMergedGiveAndTake = 2;
        int minMergedReceiveAndForward = 2;
        int minSameNFTChain = 2;
        int minSameNFTCycle = 2;

        boolean processFileList = false; // se true per ogni file crea datsetNFT e vede se è valido
        boolean loadFileList = false; // se true, carica i FileInfos da file invece di ricostruirli da zero
        boolean customFileList = true; // se true, carica i FileInfos da file invece di ricostruirli da zero
        boolean continueDeltaAfterFileListBuilt = true; // se true, continua con l'analisi dopo la creazione di filesInfos
        boolean continueMiningAfterDeltaComputed = true; // se true, continua con l'analisi dopo la creazione di filesInfos
        


        // ===== BUILD LIST OF FILES ORDER =====
        List<FileInfos> fileList = new ArrayList<>();

        if ( processFileList ){
            System.out.println("== processing new FileList ==");

            Path root = Paths.get("collections");
            List<Path> jsonFiles = Preprocess.scanJsonFiles(root);
            Preprocess.sortByDescendingSize(jsonFiles);

            fileList = Preprocess.buildFileInfos(jsonFiles);

            Path fileListFile = Paths.get("results/file_infos.txt");
            Preprocess.writeFileInfosToFile(fileList, fileListFile);
        }
        else if (loadFileList) {
            fileList = Preprocess.readFileInfosFromFile(Paths.get("results/file_infos.txt"));
        }
        else if (customFileList) {
            //fileList.add(new FileInfos(Paths.get("collections/boredapeyachtclub.json"), 0));
            //fileList.add(new FileInfos(Paths.get("collections/pfp/proof-moonbirds.json"), 0));
            //fileList.add(new FileInfos(Paths.get("collections/pfp/cyberkongz.json"), 0));
            //fileList.add(new FileInfos(Paths.get("collections/gaming/cryptokitties.json"), 0));
            //fileList.add(new FileInfos(Paths.get("collections/gaming/neo-tokyo-part-4-land-deeds-legacy.json"), 0));
            //fileList.add(new FileInfos(Paths.get("collections/gaming/nftrees.json"), 0));
            //fileList.add(new FileInfos(Paths.get("collections/TestcaseSameNFTCycle.json"), 0));
            //fileList.add(new FileInfos(Paths.get("collections/gaming/bccg.json"), 0));
            //fileList.add(new FileInfos(Paths.get("collections/DatasetJson/axie_infinity.json"), 0));
            
            //fileList.add(new FileInfos(Paths.get("collections/DatasetJson_filtered/axie_infinity.json"), 0));
            //fileList.add(new FileInfos(Paths.get("collections/DatasetJson_no0x0/axie_infinity.json"), 0));
            //fileList.add(new FileInfos(Paths.get("collections/DatasetJson_unfiltered/axie_infinity.json"), 0));

            //fileList.add(new FileInfos(Paths.get("collections/DatasetJson_nftHash/axie_infinity.json"), 0));
            //fileList.add(new FileInfos(Paths.get("collections/DatasetJson_nftHash/decentraland.json"), 0));
            //fileList.add(new FileInfos(Paths.get("collections/DatasetJson_nftHash/the_sandbox.json"), 0));
            
            //fileList.add(new FileInfos(Paths.get("collections/DatasetJson/decentraland.json"), 0));
            //fileList.add(new FileInfos(Paths.get("collections/DatasetJson/the_sandbox.json"), 0));
        }

        System.out.println("== File list of " + fileList.size() + " collections ===");
        if( fileList.isEmpty()) return;

        if ( !continueDeltaAfterFileListBuilt ) {return;}
        //===== PROCESS FILE LIST =====

        for (FileInfos fi : fileList) {  //Path collectionPath : collectionFiles

            // processiamo solo file validi e non ancora processati
            //if (!fi.isValid() || fi.isProcessed()) { continue;}

            Path collectionPath = fi.getPath();
            Path resultPath = Preprocess.createResultDirectory(collectionPath);

            String collectionName = collectionPath.getFileName().toString().replaceFirst("\\.json$", "");
            System.out.println("\n=== Processing collection: " + collectionName + " ===");

            try {
                // ===== LOAD GRAPH =====
                Graph g = Loader.loadGraphFromJsonNFT(collectionPath);
                DatasetNFT dsNFT = Loader.LoadDatasetNFTFromJson(collectionPath);


                //carefull
                Path resultPRINTALL = resultPath.resolve("zPRINT_ALL_NFT_TRANSACTIONS.txt");
                ResultWriter.createNewFile(resultPRINTALL);
                dsNFT.printAllTransactions(resultPRINTALL);
                
                

                // CONTINUOUS TRANSACTIONS FILES
                Path resultContiguousFile = resultPath.resolve("zContinuousTransactions.txt");
                Path resultNonContiguousFile = resultPath.resolve("zNonContiguousTransactions.txt");
                ResultWriter.createNewFile(resultContiguousFile);
                ResultWriter.createNewFile(resultNonContiguousFile);
                dsNFT.printNftContiguousTransactionSizes(resultContiguousFile);
                dsNFT.printOnlyNonContiguousTransactions(resultNonContiguousFile);

                // boolean truee = true;
                // if (truee) return;
/*
                // aggiungo a resultContiguusFile le transazioni dell'nft 2163
                List<String> linesToAdd = new ArrayList<>();
                linesToAdd.add("Contiguous");
                NavigableSet<Edge> nftTransactions = dsNFT.getNFTTransactions("2163");
                StringBuilder sb = new StringBuilder();
                sb.append("NFT 2163 transactions: ");
                for(Edge e : nftTransactions) {
                    sb.append(e.getFrom().getSimpleAddress() + " -> ");
                }
                linesToAdd.add(sb.toString());
                ResultWriter.appendLinesToFile(linesToAdd, resultNonContiguousFile);
*/
                


                // ===== SET DELTAS =====
                Map<String, Long> deltas = Preprocess.computeDeltaMap(g);

                for (Map.Entry<String, Long> entry : deltas.entrySet()) {

                    String label = entry.getKey();
                    long delta = entry.getValue();

                    if ( !continueMiningAfterDeltaComputed ) {continue;}

                    // ===== OUTPUT PATH =====
                    Path resultResFile = resultPath.resolve("res_" + label + "_" + TimeFormatter.secondsToSimpleString(delta) + ".txt");
                    ResultWriter.createNewFile(resultResFile);
                    


                    // ===== MINING =====
                    GraphTemporalMotifMiner graphMiner = new GraphTemporalMotifMiner(g, delta);

                    List<InStar> mergedInStar = graphMiner.findMergedInStars(minMergedInStar);
                    List<GiveAndTake> mergedGiveAndTake = graphMiner.findMergedGiveAndTakes(minMergedGiveAndTake);
                    List<ReceiveAndForward> mergedReceiveAndForward = graphMiner.findMergedReceiveAndForward(minMergedReceiveAndForward);
                    List<ReceiveAndForwardNFT> mergedReceiveAndForwardNFT = graphMiner.findMergedReceiveAndForwardNFT(minMergedReceiveAndForward, mergedReceiveAndForward);

                    NFTsTemporalMotifMiner nftMiner = new NFTsTemporalMotifMiner(dsNFT, delta);

                    List<SameNFTChain> sameNFTChain = nftMiner.findSameNFTChains(minSameNFTChain);
                    List<SameNFTChain> contiguousSameNFTChain = nftMiner.findContiguousSameNFTChains(minSameNFTChain);
                    List<SameNFTCycle> sameNFTCycle = nftMiner.findSameNFTCycle(minSameNFTCycle);
                    List<SameNFTCycle> contiguousSameNFTCycle = nftMiner.findContiguousSameNFTCycle(minSameNFTCycle);
                    //List<SameNFTCycle> sameNFTCycleFromChains = nftMiner.findSameNFTCycleFromChains(sameNFTChain, minSameNFTCycle);


                    // ===== SCRITTURA RISULTATI =====
                    ResultWriter.writeGraphInfoToFile(g, delta, resultResFile);
                    ResultWriter.appendPatternCountsBySizeToFile(mergedInStar, resultResFile);
                    ResultWriter.appendPatternCountsBySizeToFile(mergedGiveAndTake, resultResFile);
                    ResultWriter.appendPatternCountsBySizeToFile(mergedReceiveAndForward, resultResFile);
                    ResultWriter.appendPatternCountsBySizeToFile(mergedReceiveAndForwardNFT, resultResFile);

                    ResultWriter.writeDatasetNFTInfoToFile(dsNFT, delta, resultResFile);
                    ResultWriter.appendPatternCountsBySizeToFile(sameNFTChain, resultResFile);
                    ResultWriter.appendLinesToFile(new ArrayList<String>(){{add("Contiguous");}}, resultResFile);
                    ResultWriter.appendPatternCountsBySizeToFile(contiguousSameNFTChain, resultResFile);
                    ResultWriter.appendPatternCountsBySizeToFile(sameNFTCycle, resultResFile);
                    ResultWriter.appendLinesToFile(new ArrayList<String>(){{add("Contiguous");}}, resultResFile);
                    ResultWriter.appendPatternCountsBySizeToFile(contiguousSameNFTCycle, resultResFile);
                    //ResultWriter.appendPatternCountsBySizeToFile(sameNFTCycleFromChains, resultResFile);

                    //ResultWriter.appendPatternsToFile(sameNFTCycle, resultResFile);
                    //ResultWriter.appendPatternsToFile(mergedInStar, resultResFile);
                    //ResultWriter.appendPatternsToFile(mergedGiveAndTake, resultResFile);
                    //ResultWriter.appendPatternsToFile(mergedReceiveAndForward, resultResFile);
                    //ResultWriter.appendPatternsToFile(mergedReceiveAndForwardNFT, resultResFile);

                    

                    System.out.println("✅ Risultati scritti in " + resultResFile);
                }

                
                

            } catch (OutOfMemoryError e) {
                System.err.println("⚠️ OutOfMemoryError durante l'analisi di "
                        + collectionName + " — risultati parziali possibili");
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("❌ Errore durante l'analisi di "
                        + collectionName);
                e.printStackTrace();
            }
        }

        System.out.println("=== Analisi completata ===");
    }
}
