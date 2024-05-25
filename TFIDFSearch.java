import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.util.*;

public class TFIDFSearch {
    public static void main(String[] args) {

        String indexFile = args[0]+".ser";
        String inputArgumentsFile = args[1];
        String outputFile = "output.txt";

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(indexFile));
            PrintWriter writer = new PrintWriter(new FileWriter(outputFile , true));
            BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(inputArgumentsFile))) {
            
            String queryLine;
            int outputNum = Integer.parseInt(bufferedReader.readLine().trim());
            Indexer indexer = (Indexer) ois.readObject(); 

            while (((queryLine = bufferedReader.readLine()) != null)) {
                processQuery(queryLine, indexer,writer,outputNum);
            }
            writer.flush();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void processQuery(String queryLine, Indexer indexer, PrintWriter writer, int outputNum) {

        //處理讀入的條件
        String operator;
        if (queryLine.contains("AND")) {
            operator = "AND";
        } 
        else if (queryLine.contains("OR")) {
            operator = "OR";
        } 
        else {
            operator = "NO"; 
        }
        //把每一行的單字都存到ArrayList<String> targetWords
        ArrayList<String> targetWords = new ArrayList<>();
        if(!operator.equals("NO")){
            queryLine = queryLine.replaceAll("\\s+" + operator + "\\s+"," ");
//System.out.println(queryLine);
            Collections.addAll(targetWords, queryLine.split("\\s+"));
        }
        else{
            targetWords.add(queryLine.trim());
        }
//System.out.println(targetWords.toString());
        HashSet<Integer> docSet = new HashSet<>();
        boolean firstWord = true;
        //去決定要取聯集、交集哪個，並把docId存到docSet
        for(String word : targetWords){
            HashMap<Integer, Integer> docFreqs = indexer.getTermFrequencies().getOrDefault(word, new HashMap<>());
//System.out.println(word + "'s docFreqs is"+ docFreqs);
            if (firstWord) {
                docSet.addAll(docFreqs.keySet());
                firstWord = false;
            }
            else if (operator.equals("AND")) {
                docSet.retainAll(docFreqs.keySet());
            } 
            else if (operator.equals("OR")) {
                docSet.addAll(docFreqs.keySet());
            }
        }


        ArrayList<Map.Entry<Integer, Double>> docTfidfList = new ArrayList<>();
        int totalDocs = indexer.getTotalDocs();

        for (Integer docId : docSet) {
            double tfidf = calculateTFIDF(targetWords, docId, indexer, totalDocs);
            docTfidfList.add(new AbstractMap.SimpleEntry<>(docId, tfidf));
        }
    
        //排序輸出格式
        docTfidfList.sort((e1, e2) -> {
            int tfidfCompare = Double.compare(e2.getValue(), e1.getValue());
            if (tfidfCompare != 0) {
                return tfidfCompare;
            }
            return Integer.compare(e1.getKey(), e2.getKey());
        });

        //把結果寫到output.txt
        for (Map.Entry<Integer, Double> entry : docTfidfList) {
            writer.print(entry.getKey()+" ");
        }

        if (docSet.size() < outputNum) {
            for(int i=0;i< (outputNum-docSet.size());i++){
                writer.print("-1");
            }
        }
        writer.println(); 
    }

    private static double calculateTFIDF(ArrayList<String> targetWords, int docId, Indexer indexer, int totalDocs) {
        double tfidfSum = 0.0;
        for (String word : targetWords) {
            HashMap<Integer, Integer> docFreqs = indexer.getTermFrequencies().get(word);
            if (docFreqs != null && docFreqs.containsKey(docId)) {
                int termFreq = docFreqs.get(docId);  //在指定文本出現的次數
                int docFreq = indexer.getDocumentCount(word); //出現的文本數量
                //System.out.println(word+"出現的文本數量: "+ docFreq);
                double wordsInDoc = indexer.getWordsCntInDocs(docId);
                double idf = 0;
                double tf = (double) termFreq / wordsInDoc;
                //System.out.println(word+"'s tf is termFreq/wordsInDoc "+ termFreq+" / "+wordsInDoc);
            
                if(!(docFreq == 0)){
                    idf = Math.log((double) totalDocs / (docFreq));
                    //System.out.println(word+"'s idf is totalDocs / docFreq "+ Math.log((double)totalDocs/docFreq));
                }
                //System.out.println(word+"'s idf "+idf);
            //System.out.println("==========");
                tfidfSum += (tf * idf);
                //System.out.println(word+"'s tfidf is "+tf +" * "+idf+"= "+tf*idf);
            }
        }
    //System.out.println("No."+docId+": tfidf is "+tfidfSum);

        return tfidfSum;
    }

}