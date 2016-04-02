public class TermInfo {
    String docId;
    int termFreq;

    public TermInfo(String docId, int termFreq){
        this.docId = docId;
        this.termFreq = termFreq;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public void setTermFreq(int termFreq) {
        this.termFreq = termFreq;
    }

    public String getDocId() {
        return this.docId;
    }

    public int getTermFreq(){
        return this.termFreq;
    }
}