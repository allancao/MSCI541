import java.util.ArrayList;
import java.util.List;

public class Topic {

    private int topicId;
    private String topic;

    public Topic(int topicId, String topic) {
        this.topicId = topicId;
        this.topic = topic;
    }

    public static List<Topic> generateTopics(int[] topicIds, String[] topics) {

        List<Topic> ret = new ArrayList<Topic>();
        for (int i = 0; i < topicIds.length; i++) {
            ret.add(new Topic(topicIds[i], topics[i]));
        }

        return ret;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

}
