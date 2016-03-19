/**
 * Created by Cain on 3/17/2016.
 */
public class DocInfo {

        String docId;
        int docLength;

        public DocInfo(String docId, int docLength){
            this.docId = docId;
            this.docLength = docLength;
        }

        public void setDocId(String docId) {
            this.docId = docId;
        }

        public void setTermFreq(int docLength) {
            this.docLength = docLength;
        }

        public String getDocId() {
            return this.docId;
        }

        public int getTermFreq(){
            return this.docLength;
        }

}
