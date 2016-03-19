import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class SearchEngineMain {

    public static String[] queryTopics = {
            "foreign minorities, Germany",
            "behavioral genetics",
            "osteoporosis",
            "Ireland, peace talks",
            "cosmic events",
            "Parkinson's disease",
            "poaching, wildlife preserves",
            "tropical storms",
            "legal, Pan Am,",
            "Schengen agreement",
            "salvaging, shipwreck, treasure",
            "airport security",
            "steel production",
            "Cuba, sugar, exports",
            "drugs, Golden Triangle",
            "creativity",
            "quilts, income",
            "recycle, automobile tires",
            "carbon monoxide poisoning",
            "industrial waste disposal",
            "art, stolen, forged",
            "suicides",
            "counterfeiting money",
            "law enforcement, dogs",
            "UV damage, eyes",
            "declining birth rates",
            "Legionnaires' disease",
            "killer bee attacks",
            "robotic technology",
            "profiling, motorists, police",
            "Greek, philosophy, stoicism",
            "Estonia, economy",
            "curbing population growth",
            "railway accidents",
            "tourism, increase",
            "inventions, scientific discoveries",
            "child labor",
            "Lyme disease",
            "heroic acts",
            "U.S., investment, Africa",
            "women clergy",
            "tourists, violence",
            "ship losses",
            "antibiotics ineffectiveness",
            "King Hussein, peace"
    };

    public static int[] queryTopicIds = {
            401,402,403,404,405,406,407,408,409,103,
            410,411,412,413,414,415,417,418,419,420,
            421,422,424,425,426,427,428,429,430,431,
            432,433,434,435,436,438,439,440,441,442,
            443,445,446,448,449,450
    };

//    public static String[] topics = {
//            "foreign minorities, Germany",
//            "behavioral genetics",
//            "osteoporosis",
//            "Ireland, peace talks",
//            "cosmic events",
//            "Parkinson's disease",
//            "poaching, wildlife preserves",
//            "tropical storms",
//            "legal, Pan Am, 103",
//            "Schengen agreement",
//            "salvaging, shipwreck, treasure",
//            "airport security",
//            "steel production",
//            "Cuba, sugar, exports",
//            "drugs, Golden Triangle",
//            "creativity",
//            "quilts, income",
//            "recycle, automobile tires",
//            "carbon monoxide poisoning",
//            "industrial waste disposal",
//            "art, stolen, forged",
//            "suicides",
//            "counterfeiting money",
//            "law enforcement, dogs",
//            "UV damage, eyes",
//            "declining birth rates",
//            "Legionnaires' disease",
//            "killer bee attacks",
//            "robotic technology",
//            "profiling, motorists, police",
//            "Greek, philosophy, stoicism",
//            "Estonia, economy",
//            "curbing population growth",
//            "railway accidents",
//            "tourism, increase",
//            "inventions, scientific discoveries",
//            "child labor",
//            "Lyme disease",
//            "heroic acts",
//            "U.S., investment, Africa",
//            "women clergy",
//            "tourists, violence",
//            "ship losses",
//            "antibiotics ineffectiveness",
//            "King Hussein, peace"
//    };

    public static HashMap<Topic, HashMap<String, Double>> calcBM25(HashMap<String, Integer> docList, HashMap<String, List<TermInfo>> invertedIndex,
                                    double k1, double k2, double b) {

        HashMap<Topic, HashMap<String, Double>> ret = new HashMap<Topic, HashMap<String, Double>>();
        double avdl = 0;
        for (Integer docLength : docList.values()) {
            avdl += docLength;
        }
        avdl /= docList.keySet().size();
        List<Topic> topics = Topic.generateTopics(queryTopicIds, queryTopics);

        //every topic in topic list
        for (Topic topic : topics) {
            String[] queryTerms = topic.getTopic().split(" ");
            HashMap<String, Double> docScoresPerTopic = new HashMap<String, Double>();
            //every doc in list of docs
            for (String docno : docList.keySet()) {
                double bm25Score = 0;
                //every term per topic
                for (String term : queryTerms) {

                    int termCountInDoc = 0;
                    List<TermInfo> termInfoList = invertedIndex.get(term);
                    int ni = termInfoList.size();

                    //Find freq of term i in doc D
                    for (TermInfo termInfo : termInfoList) {
                        if (termInfo.getDocId().equals(docno)) {
                            termCountInDoc = termInfo.getTermFreq();
                            break;
                        }
                    }

                    //If doc has a term, deemed relevant, we can calculate BM25
                    if (termCountInDoc != 0) {
                        double K = k1 * ((1.0 - b) + b * (docList.get(docno)/avdl));
                        double tfInDoc = ((k1 + 1) * termCountInDoc) / (K + termCountInDoc);
                        double tfInQuery = ((k2 + 1) * 1) / (k2 + 1);
                        double idf = Math.log((docList.size() - ni + 0.5) / (ni + 0.5));

                        bm25Score += tfInDoc * tfInQuery * idf;
                    }
                }
                if (bm25Score > 0) docScoresPerTopic.put(docno, bm25Score);
            }
            ret.put(topic, docScoresPerTopic);
        }
        return ret;
    }

    public static String[] tokenizeLine(String string) {
        String stripped = string.replaceAll("<[^>]*>", "");
        String ret = stripped.toLowerCase().replaceAll("[^a-z0-9]", " ").trim();
        return ret.split("\\s+");
    }

    public static void main(String[] args) throws IOException {

        Pattern begin = Pattern.compile("<headline>|<text>|<graphic>");
        Pattern end = Pattern.compile("</headline>|</text>|</graphic>");

        HashMap<String, List<TermInfo>> invertedIndex = new HashMap<String, List<TermInfo>>();
        HashMap<String, Integer> docList = new HashMap<String, Integer>();

        String infile = "C:\\Users\\Allan\\workspace\\MSCI541\\latimes.gz";
        GZIPInputStream in = new GZIPInputStream(new FileInputStream(infile));

        Reader decoder = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(decoder);

        String line;

        double k1 = 1.2;
        double k2 = 0.75;
        double b = 0.75;

        PrintWriter writer = new PrintWriter("index.txt", "UTF-8");

        //Read in docno
        while ((line = br.readLine()) != null) {
            line = line.toLowerCase();
            if (line.contains("<docno>")) {
                //Increase doc length, to get total docs
                String docno = line.substring(8, line.length() - 9);
                System.out.println(docno);
                //Read in line after docno, while end tag not found
                while(!line.contains("</doc>")) {
                    boolean validText = false;
                    if (begin.matcher(line).find()) {
                        validText = true;
                    }

                    //While validText tag
                    while(validText) {
                        String[] tokenizedLine = tokenizeLine(line);
                        //Add docId and length of doc for BM25 calculations
                        if (docList.get(docno) != null) {
                            docList.put(docno, docList.get(docno) + tokenizedLine.length);
                        } else {
                            docList.put(docno, tokenizedLine.length);
                        }

                        TermInfo termInfo = new TermInfo(docno, 1);

                        //Add docId and its length
                        docList.put(docno, tokenizedLine.length);
                        for (String token : tokenizedLine){
                            if (!token.trim().isEmpty()) {
                                List<TermInfo> termInfoList = invertedIndex.get(token);

                                //Remove old termInfoList
                                invertedIndex.remove(token);

                                //If token doesn't exist in map
                                if (termInfoList == null) {
                                    List<TermInfo> newList = new ArrayList<TermInfo>();
                                    newList.add(termInfo);
                                    invertedIndex.put(token, newList);
                                } else {

                                    //For each token, get list of <docid, freqCount>
                                    boolean termInfoAdded = false;
                                    for (TermInfo tempTermInfo : termInfoList) {
                                        if (tempTermInfo.getDocId().equals(docno)) {
                                            tempTermInfo.setTermFreq(tempTermInfo.getTermFreq() + 1);
                                            termInfoAdded = true;
                                            writer.format("%s|%s|%d\n", token, tempTermInfo.getDocId(), tempTermInfo.getTermFreq());
                                            break;
                                        }
                                    }

                                    //Add if termInfo does not exist
                                    if (!termInfoAdded) {
                                        writer.format("%s|%s|%d\n", token, termInfo.getDocId(), termInfo.getTermFreq());
                                        termInfoList.add(termInfo);
                                    }

                                    //Replace old list with new list
                                    invertedIndex.put(token, termInfoList);
                                }
                            }
                        }

                        if (end.matcher(line).find()) { validText = false; }
                        if (br.ready()){
                            line = br.readLine().toLowerCase();
                        }
                    }
                    if (br.ready()){
                        line = br.readLine().toLowerCase();
                    }
                }
            }
        }
        writer.close();
        System.out.println(docList.size());

        HashMap<Topic, HashMap<String, Double>> bm25List = calcBM25(docList, invertedIndex, k1, k2, b);
        for (Topic topic : bm25List.keySet()) {
            HashMap<String, Double> docScore = bm25List.get(topic);
            for (String docno : docScore.keySet()) {
                System.out.format("%s|%f|%s|%d", topic.getTopic(), topic.getTopicId(), docno, docScore.get(docno));
            }
        }
    }
}



