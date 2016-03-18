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

    static String[] titles = {"foreign minorities, Germany",
            "behavioral genetics",
            "osteoporosis",
            "Ireland, peace talks",
            "cosmic events",
            "Parkinson's disease",
            "poaching, wildlife preserves",
            "tropical storms",
            "legal, Pan Am, 103",
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
            "King Hussein, peace" };
    static int[] topicId = {401,402,403,404,405,406,407,408,409,410,
            411,412,413,414,415,417,418,419,420,
            421,422,424,425,426,427,428,429,430,
            431,432,433,434,435,436,438,439,440,
            441,442,443,445,446,448,449,450};

    public static String[] tokenizeLine(String string) {
        String stripped = string.replaceAll("<[^>]*>", "");
        String ret = stripped.toLowerCase().replaceAll("[^a-z0-9]", " ").trim();
        return ret.split("\\s+");
    }

    public static void main(String[] args) throws IOException {

        Pattern begin = Pattern.compile("<\\/HEADLINE>|<\\/TEXT>|<\\/GRAPHIC>");
        Pattern end = Pattern.compile("<HEADLINE>|<TEXT>|<GRAPHIC>");
        Matcher matcher;

        HashMap<String, List<TermInfo>> invertedIndex = new HashMap<String, List<TermInfo>>();

        String infile = "C:\\Users\\Allan\\workspace\\MSCI541\\latimes.gz";
        GZIPInputStream in = new GZIPInputStream(new FileInputStream(infile));

        Reader decoder = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(decoder);

        String line;

        double k1 = 1.2;
        double k2 = 0.75;
        double b = 0.75;

        HashMap<String, Integer> docList = new HashMap<String, Integer>();

        int numDocs = 0;
        int dl = 0;
        double avgdl = 0;

        PrintWriter writer = new PrintWriter("index.txt", "UTF-8");

        //Read in docno
        while ((line = br.readLine()) != null) {
            line = line.toLowerCase();
            if (line.contains("<docno>")) {

                //Increase doc length, to get total docs
                numDocs += 1;
                String docno = line.substring(8, line.length() - 9);
                line = br.readLine();

                //Read in line after docno, while end tag not found
                while(!line.contains("</docno>")) {
                    boolean validText = true;

                    //Check if line has validText tag
                    if ((matcher = begin.matcher(line)).find()) {
                        validText = true;
                    } else if ((matcher = end.matcher(line)).find()) {
                        validText = false;
                    }

                    //While validText tag
                    while(validText) {
                        String[] tokenizedLine = tokenizeLine(line);
                        TermInfo termInfo = new TermInfo(docno, 1);

                        //Add docId and its length
                        docList.add(new DocInfo(docno, tokenizedLine.length));
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
//                                            writer.format("%s|%s|%d\n", token, tempTermInfo.getDocId(), tempTermInfo.getTermFreq());
                                            break;
                                        }
                                    }

                                    //Add if termInfo does not exist
                                    if (!termInfoAdded) {
//                                        writer.format("%s|%s|%d\n", token, termInfo.getDocId(), termInfo.getTermFreq());
                                        termInfoList.add(termInfo);
                                    }

                                    //Replace old list with new list
                                    invertedIndex.put(token, termInfoList);
                                }
                            }
                        }
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
    }
}



