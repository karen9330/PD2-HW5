import java.io.*;

public class BuildIndex {
    public static void main(String[] args) {

        String inputFilePath = args[0];
        String outputFile = inputFilePath.substring(inputFilePath.lastIndexOf('/')+1,inputFilePath.lastIndexOf('.')) + ".ser";

        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFilePath));
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFile))){

            String line;
            int cnt = 0;
            int docId = 0;
            StringBuilder combinedLine = new StringBuilder();
            Indexer indexer = new Indexer();
            
            while (((line = bufferedReader.readLine()) != null)){

                combinedLine.append(segmentation(line)).append(" ");
                cnt++;
                if(cnt == 5){
                    processDocument(combinedLine.toString(), indexer, docId);
                    docId++;
                    combinedLine.setLength(0);
                    cnt=0;
                }
            }
            oos.writeObject(indexer);

        }catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    private static String segmentation(String line){
        line = line.replaceAll("[^a-zA-Z]", " ");
        line = line.replaceAll("\\s+", " ");
        line = line.trim();
        line = line.toLowerCase();
        return line;
    }

    private static void processDocument(String text, Indexer indexer, int docId) {
        String[] words = text.split("\\s+");
        indexer.addWordsCnt(words.length,docId);
        for (String word : words) {
            indexer.addTerm(word, docId);
        }
    }
}
