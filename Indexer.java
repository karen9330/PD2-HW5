import java.io.Serializable;
import java.util.HashMap;

public class Indexer implements Serializable {
    private static final long serialVersionUID = 1L;
    private HashMap<String, HashMap<Integer, Integer>> termFrequencies = new HashMap<>(); //targetWord、文本編號、在這個文本出現的次數
    private HashMap<Integer,Integer> wordsCntInDoc = new HashMap<>();  //編號、共有幾個字

    public void addTerm(String term, int docId) {
        termFrequencies.putIfAbsent(term, new HashMap<>());
        HashMap<Integer, Integer> docFreq = termFrequencies.get(term);
        docFreq.put(docId, docFreq.getOrDefault(docId, 0) + 1);
    }

    public void addWordsCnt(int wordsCnt, int docId){
        wordsCntInDoc.put(docId, wordsCnt);
    }
    public HashMap<String, HashMap<Integer, Integer>> getTermFrequencies() {
        return termFrequencies;
    }

    public double getWordsCntInDocs(int docId){
        return wordsCntInDoc.get(docId);
    }

    public int getTotalDocs(){
        return wordsCntInDoc.size();
    }

    public int getDocumentCount(String word){
        HashMap<Integer, Integer> docHashMap = termFrequencies.get(word);
        int docFreq = docHashMap.size();
        return docFreq;
    }
}
