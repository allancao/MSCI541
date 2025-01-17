import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class SearchEngineMain {

    public static String[] queryTopics = {
            "foreign minorities Germany",
            "behavioral genetics",
            "osteoporosis",
            "Ireland peace talks",
            "cosmic events",
            "Parkinson's disease",
            "poaching wildlife preserves",
            "tropical storms",
            "legal Pan Am",
            "Schengen agreement",
            "salvaging shipwreck treasure",
            "airport security",
            "steel production",
            "Cuba sugar exports",
            "drugs Golden Triangle",
            "creativity",
            "quilts income",
            "recycle automobile tires",
            "carbon monoxide poisoning",
            "industrial waste disposal",
            "art stolen forged",
            "suicides",
            "counterfeiting money",
            "law enforcement dogs",
            "UV damage eyes",
            "declining birth rates",
            "Legionnaires disease",
            "killer bee attacks",
            "robotic technology",
            "profiling motorists police",
            "Greek philosophy stoicism",
            "Estonia economy",
            "curbing population growth",
            "railway accidents",
            "tourism increase",
            "inventions scientific discoveries",
            "child labor",
            "Lyme disease",
            "heroic acts",
            "U.S investment, Africa",
            "women clergy",
            "tourists violence",
            "ship losses",
            "antibiotics ineffectiveness",
            "King Hussein peace"
    };

    public static int[] queryTopicIds = {
            401,402,403,404,405,406,407,408,409,410,
            411,412,413,414,415,417,418,419,420,421,
            422,424,425,426,427,428,429,430,431,432,
            433,434,435,436,438,439,440,441,442,443,
            445,446,448,449,450
    };

    public static LinkedHashMap sortHashMapByValues(HashMap passedMap) {
        List mapKeys = new ArrayList(passedMap.keySet());
        List mapValues = new ArrayList(passedMap.values());
        Collections.sort(mapValues);
        Collections.reverse(mapValues);
        Collections.sort(mapKeys);
        Collections.reverse(mapKeys);

        LinkedHashMap sortedMap = new LinkedHashMap();

        Iterator valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Object val = valueIt.next();
            Iterator keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                String comp1 = passedMap.get(key).toString();
                String comp2 = val.toString();

                if (comp1.equals(comp2)){
                    passedMap.remove(key);
                    mapKeys.remove(key);
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }

    public static HashMap<Integer, HashMap<String,Integer>> parseQRels() throws IOException{
        String qrelsFile = "C:\\Users\\Cain\\workspace\\MSCI541\\LA-only.trec8-401.450.minus416-423-437-444-447.txt";
        FileInputStream qrelsStream = new FileInputStream(qrelsFile);
        BufferedReader qrelsReader = new BufferedReader(new InputStreamReader(qrelsStream));

        HashMap<Integer, HashMap<String, Integer>> qrels = new HashMap<Integer, HashMap<String, Integer>>();
        int currentTopicId = 0;
        String line;
        while ((line = qrelsReader.readLine()) != null) {
            String[] qrelsData = line.split(" ");
            if (Integer.parseInt(qrelsData[0]) != currentTopicId) {
                currentTopicId = Integer.parseInt(qrelsData[0]);
                HashMap<String, Integer> map = new HashMap<String, Integer>();
                map.put(qrelsData[2], Integer.parseInt(qrelsData[3]));
                qrels.put(currentTopicId, map);
            } else {
                HashMap<String, Integer> map = qrels.get(currentTopicId);
                map.put(qrelsData[2], Integer.parseInt(qrelsData[3]));
            }
        }
        return qrels;
    }

    public static ArrayList<String[]> parseScores() throws IOException{
        String scoresFile = "C:\\Users\\Cain\\workspace\\MSCI541\\scores.txt";
        FileInputStream scoresStream = new FileInputStream(scoresFile);
        BufferedReader scoresReader = new BufferedReader(new InputStreamReader(scoresStream));
        String line;
        ArrayList<String[]> scoresList = new ArrayList<String[]>();
        while ((line = scoresReader.readLine()) != null) {
            scoresList.add(line.split(" "));
        }
        return scoresList;
    }

    public static ArrayList<ArrayList<Double>> averageP10()
            throws IOException{

        ArrayList<String[]> scoresList = parseScores();
        HashMap<Integer, HashMap<String,Integer>> qrels = parseQRels();

        ArrayList<Double> precisionList = new ArrayList<Double>();
        ArrayList<Double> nDCGList = new ArrayList<Double>();
        for (int i = 0; i < scoresList.size(); i++) {
            int topicId = Integer.parseInt(scoresList.get(i)[0]);
            double[] pList = new double[10];
            double[] gainList = new double[10];

            for (int j = i; j < (i + 10); j++) {
                double relevancy;
                if (qrels.get(topicId).get(scoresList.get(j)[1]) != null) {
                    relevancy = qrels.get(topicId).get(scoresList.get(j)[1]);
                } else {
                    relevancy = 0;
                }
                System.out.println(topicId + "|" + scoresList.get(j)[1] + "|" + relevancy);
                pList[j - i] = relevancy;
                gainList[j - i] = relevancy;
            }

            double numRelevant = 0;
            for (int a = 0; a < pList.length; a++) {
                numRelevant += pList[a];
            }
            precisionList.add(numRelevant/10);

            double totalGain = 0;
            double totalIdealGain = 0;
            for (int a = 0; a < gainList.length; a++) {
                totalGain += (gainList[a]/(Math.log(a+2)/Math.log(2)));
            }

//            int qRelsNum = 0;
//            HashMap<String, Integer> qrelsRelevant = qrels.get(topicId);
//            for (String key : qrelsRelevant.keySet()) {
//                qRelsNum += qrelsRelevant.get(key);
//                if (qRelsNum == 10) {
//                    break;
//                }
//            }

            ArrayList<Double> qrelsRelevant = new ArrayList<Double>();
            LinkedHashMap<String, Integer> qrelsTopicMap = sortHashMapByValues(qrels.get(topicId));
            for (String key : qrelsTopicMap.keySet()) {
                qrelsRelevant.add(new Double(qrelsTopicMap.get(key)));
                if (qrelsRelevant.size() == 10) {
                    break;
                }
            }

            for (int a = 0; a < qrelsRelevant.size(); a++) {
                totalIdealGain += (qrelsRelevant.get(a)/(Math.log(a+2)/Math.log(2)));
            }
            System.out.println(topicId + "|" + totalGain);
            System.out.println(topicId + "|" + totalIdealGain);
            nDCGList.add(totalGain/totalIdealGain);

            while(i < scoresList.size() - 1 && Integer.parseInt(scoresList.get(i + 1)[0]) == topicId) {
                i++;
            }

        }
        ArrayList<ArrayList<Double>> ret = new ArrayList<ArrayList<Double>>();
        ret.add(precisionList);
        ret.add(nDCGList);
        return ret;
    }

    public static HashMap<Topic, HashMap<String, Double>> calcBM25(HashMap<String, Integer> docList,
                                                                   HashMap<String, HashMap<String, Integer>> invertedIndex,
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
            String[] queryTerms = topic.getTopic().replaceAll("[^a-zA-Z0-9]", " ").toLowerCase().trim().split(" ");
            HashMap<String, Double> docScoresPerTopic = new HashMap<String, Double>();
            //every doc in list of docs
            for (String docno : docList.keySet()) {
                double bm25Score = 0;
                //every term per topic
                for (String term : queryTerms) {
                    int termCountInDoc = 0;
                    HashMap<String, Integer> termInfoList = invertedIndex.get(term);
                    if (termInfoList != null && termInfoList.get(docno) != null) {
                        int ni = termInfoList.size();

                        //Find freq of term i in doc D
                        termCountInDoc = termInfoList.get(docno);

                        //If doc has a term, deemed relevant, we can calculate BM25
                        if (termCountInDoc != 0) {

                            double K = k1 * ((1.0 - b) + b * (docList.get(docno)/avdl));
                            double tfInDoc = ((k1 + 1) * termCountInDoc) / (K + termCountInDoc);
                            double tfInQuery = ((k2 + 1) * 1) / (k2 + 1);
                            double idf = Math.log((docList.size() - ni + 0.5) / (ni + 0.5));

                            bm25Score += (tfInDoc * tfInQuery * idf);
                            if (topic.getTopicId() == 401) {
                                System.out.println(topic.getTopicId() + "|" + docno + "|" + term + "|ni:" + ni + "|termCountInDoc" +
                                        termCountInDoc + "|" + K + "|" + tfInDoc + "|" + tfInQuery + "|" + idf + "|" + "|" + docList.get(docno) + "|" + avdl + "|" + bm25Score);
                            }
                        }
                    }
                }
                if (bm25Score > 0) {
                    docScoresPerTopic.put(docno, bm25Score);
                }
            }
            ret.put(topic, docScoresPerTopic);
        }
        return ret;
    }

    public static String[] tokenizeLine(String string) {
        String stripped = string.replaceAll("<.*?>", " ").trim();
        String ret = stripped.replaceAll("[^a-zA-Z0-9]", " ").toLowerCase().trim();
        return ret.split("\\s+");
    }

    public static void main(String[] args) throws IOException {

        HashMap<String, HashMap<String, Integer>> invertedIndex = new HashMap<String, HashMap<String, Integer>>();
        HashMap<String, Integer> docList = new HashMap<String, Integer>();

        String infile = "C:\\Users\\Cain\\workspace\\MSCI541\\latimes.gz";
        GZIPInputStream in = new GZIPInputStream(new FileInputStream(infile));

        Reader decoder = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(decoder);

        String line;

        double k1 = 1.2;
        double k2 = 0.75;
        double b = 0.75;

        //Read in docno
        while ((line = br.readLine()) != null) {
            if (line.contains("<DOCNO>")) {
                //Increase doc length, to get total docs
                String docno = line.replaceAll("<DOCNO>|</DOCNO>", "").trim();
                //Read in line after docno, while end tag not found
                int docLength = 0;
                while(!line.contains("</DOC>")) {

                    if (line.contains("<HEADLINE>") || line.contains("<SUBJECT>") || line.contains("<TEXT>") || line.contains("<GRAPHIC>")) {
                        line = br.readLine();
                        //While valid text
                        while (!line.contains("</HEADLINE>") && !line.contains("</SUBJECT>") && !line.contains("</TEXT>") && !line.contains("</GRAPHIC>")) {
                            String[] tokenizedLine = tokenizeLine(line);
                            docLength += tokenizedLine.length;

                            //Add docId and its length
                            docList.put(docno, tokenizedLine.length);
                            for (String token : tokenizedLine) {
                                if (token.trim().length() > 0) {
                                    HashMap<String, Integer> termInfoList = invertedIndex.get(token);
                                    invertedIndex.remove(token);

                                    //If token doesn't exist in map
                                    if (termInfoList == null) {
                                        HashMap<String, Integer> newList = new HashMap<String, Integer>();
                                        newList.put(docno, 1);
                                        invertedIndex.put(token, newList);
                                    } else {
                                        //For each token, get list of <docid, freqCount>
                                        if (termInfoList.get(docno) != null) {
                                            termInfoList.put(docno, termInfoList.get(docno) + 1);
                                        } else {
                                            termInfoList.put(docno, 1);
                                        }
                                        invertedIndex.put(token, termInfoList);
                                    }
                                }
                            }
                            if (br.ready()) {
                                line = br.readLine();
                            }
                        }
                    }
                    if (br.ready()){
                        line = br.readLine();
                    }
                }
                docList.put(docno, docLength);
            }
        }

        System.out.println(docList.size());
        System.out.println(invertedIndex.keySet().size());

        PrintWriter wordCount = new PrintWriter("wordCount.txt", "UTF-8");
        for (String key : invertedIndex.keySet()) {
            HashMap<String, Integer> map = invertedIndex.get(key);
            wordCount.println(key + "|" + map.size());
        }
        wordCount.flush();
        wordCount.close();

        HashMap<Topic, HashMap<String, Double>> bm25List = calcBM25(docList, invertedIndex, k1, k2, b);
        SortedSet<Topic> topics = new TreeSet<Topic>(bm25List.keySet());
        PrintWriter trecWriter = new PrintWriter("scores.txt", "UTF-8");
        for (Topic topic : topics) {
            HashMap<String, Double> docScore = bm25List.get(topic);
            LinkedHashMap<String, Double> sortedDocScores = sortHashMapByValues(docScore);
            int rank = 1;
            for (String docno : sortedDocScores.keySet()) {
                if (rank < 1001) {
                    trecWriter.println(topic.getTopicId() + " " + docno + " " + rank + " " + sortedDocScores.get(docno) + " a3cao");
                }
                rank++;
            }
        }
        trecWriter.flush();
        trecWriter.close();

        ArrayList<ArrayList<Double>> avgPrecisionAndNDCG = averageP10();
        for (int i = 0; i < queryTopicIds.length; i++) {
            System.out.println(queryTopicIds[i] + "|" + avgPrecisionAndNDCG.get(0).get(i) + "|" + avgPrecisionAndNDCG.get(1).get(i));
        }
    }
}



