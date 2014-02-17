package com.edwardinubuntu.dailykind.util;

import android.util.Log;
import com.edwardinubuntu.dailykind.DailyKind;
import com.parse.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by edward_chiang on 2014/2/17.
 */
public class ReportManager {

    public static final String TAGS_SEPARATOR = ",";

    private List<ParseObject> storiesObjects;

    private List<String> reportWordings;

    private String joinSince;

    private ParseUser user;

    private AnalyseListener analyseListener;

    private final String HIGHLIGHT_RED_COLOR = "#ac2925";

    public interface AnalyseListener {
        public void done();
    }

    public ReportManager() {
        reportWordings = new ArrayList<String>();
    }

    public void analyse() {

        StringBuffer tagsBuffer = new StringBuffer();

        ParseObject latestIdeaStory = null;
        boolean hasFoundLatestIdeaStory = false;

        Map<Object, Integer> ideasMap = new LinkedHashMap<Object, Integer>();
        Map<Object, Integer> areaNameMap = new LinkedHashMap<Object, Integer>();
        for (ParseObject eachStory : storiesObjects) {
            // Get Tags
            if (eachStory.has("ideaPointer")) {
                ParseObject ideaObject = eachStory.getParseObject("ideaPointer");
                if (ideaObject.has("Tags")) {
                    String tags = ideaObject.getString("Tags");
                    tagsBuffer.append(tags);
                    tagsBuffer.append(ReportManager.TAGS_SEPARATOR);
                }

                if (!hasFoundLatestIdeaStory) {
                    latestIdeaStory = eachStory;
                    hasFoundLatestIdeaStory = true;
                }

                if (ideaObject.has("doneCount")) {
                    ideasMap.put(ideaObject, ideaObject.getInt("doneCount"));
                }
            }
            if (eachStory.has("areaName")) {
                String areaName = eachStory.getString("areaName");
                if (!areaNameMap.containsKey(areaName)) {
                    areaNameMap.put(areaName, 1);
                } else {
                    areaNameMap.put(areaName, areaNameMap.get(areaName) + 1);
                }
            }
        }
        // Get Analyse 1
        extractIdeaTags(tagsBuffer);

        // Get Analyse 2
        if (getJoinSince() != null) {
            StringBuffer describeBuffer = new StringBuffer();
            describeBuffer.append("從");
            describeBuffer.append("<font color="+HIGHLIGHT_RED_COLOR+">");
            describeBuffer.append(getJoinSince());
            describeBuffer.append("</font>");
            describeBuffer.append("開始加入，他的理念是幫助別人，除了儘花費自己小小力氣，卻能讓人大大受惠，並且讓他人擁有美好的一天，自己也無形中很快樂。");
            reportWordings.add(describeBuffer.toString());
        }

        // Get Analyse 3
        if (latestIdeaStory!=null) {
            StringBuffer describeBuffer = new StringBuffer();
            describeBuffer.append("最近參加的友善行動是");
            describeBuffer.append("<font color="+HIGHLIGHT_RED_COLOR+">");
            describeBuffer.append(latestIdeaStory.getParseObject("ideaPointer").getString("Name"));
            describeBuffer.append("</font>");
            describeBuffer.append("。");
            reportWordings.add(describeBuffer.toString());
        }

        // Get Analyse 4
        if (ideasMap.size() > 0) {
            ideasMap = sortByComparator(ideasMap, true);
            ParseObject firstParseObject = (ParseObject) new ArrayList(ideasMap.keySet()).get(0);
            StringBuffer describeBuffer = new StringBuffer();
            describeBuffer.append("而對一般人來說，比較少人能達成的");
            describeBuffer.append("<font color="+HIGHLIGHT_RED_COLOR+">");
            describeBuffer.append(firstParseObject.getString("Name"));
            describeBuffer.append("</font>");
            describeBuffer.append("完成 " + firstParseObject.getInt("doneCount") + " 次。");
            describeBuffer.append(user.getString("name"));
            describeBuffer.append("辦到了。");
            reportWordings.add(describeBuffer.toString());
        }

        // Get Analyse 5
        if (storiesObjects.size() > 0) {
            ParseQuery<ParseObject> userimpactQuery = ParseQuery.getQuery("UserImpact");
            userimpactQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
            userimpactQuery.setMaxCacheAge(DailyKind.QUERY_MAX_CACHE_AGE);
            userimpactQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if (parseObjects!= null) {
                        int totalCount = 0;
                        for (ParseObject eachImpact : parseObjects) {
                            if (eachImpact.has("sharedStoriesCount")) {
                                totalCount += eachImpact.getInt("sharedStoriesCount");
                            }
                        }
                        double averageSharedCount = new BigDecimal(totalCount / parseObjects.size())
                                .setScale(2, BigDecimal.ROUND_HALF_UP)
                                .doubleValue();

                        StringBuffer describeBuffer = new StringBuffer();
                        describeBuffer.append("分享過" + storiesObjects.size() +"篇故事，");
                        describeBuffer.append("比一般 LovingHeart 朋友們 （平均 " + averageSharedCount+ " 篇）");

                        describeBuffer.append("<font color="+HIGHLIGHT_RED_COLOR+">");
                        if (storiesObjects.size() > averageSharedCount) {
                            describeBuffer.append("用心記錄");
                        } else {
                            describeBuffer.append("少些");
                        }
                        describeBuffer.append("</font>");
                        describeBuffer.append("。");
                        reportWordings.add(describeBuffer.toString());

                        if (getAnalyseListener() != null) {
                            getAnalyseListener().done();
                        }
                    }

                }
            });
        }

        // Analyse 8
        areaNameMap = sortByComparator(areaNameMap, false);
        if (areaNameMap.size() > 0) {
            int index = 0;
            final int TOTAL_TO_SHOW = 2;
            StringBuffer describeBuffer = new StringBuffer();
            describeBuffer.append("跟");
            for (Map.Entry<Object, Integer> entry : areaNameMap.entrySet()) {
                if (index < TOTAL_TO_SHOW) {
                    describeBuffer.append("<font color="+HIGHLIGHT_RED_COLOR+">");
                    describeBuffer.append(entry.getKey());
                    describeBuffer.append("</font>");
                }
                if (index < TOTAL_TO_SHOW - 1  || index < areaNameMap.entrySet().size() - 1) {
                    describeBuffer.append("、");
                }
                index++;
            }
            describeBuffer.append("有深度的連結，讓這些地方變不一樣。");
            reportWordings.add(describeBuffer.toString());
        }

        if (getAnalyseListener() != null) {
            getAnalyseListener().done();
        }
    }

    public List<String> getReportWordings() {
        return reportWordings;
    }

    public List<ParseObject> getStoriesObjects() {
        return storiesObjects;
    }

    public void setStoriesObjects(List<ParseObject> storiesObjects) {
        this.storiesObjects = storiesObjects;
    }

    private void extractIdeaTags(StringBuffer tagsBuffer) {
        String[] tagsStrings = tagsBuffer.toString().split(TAGS_SEPARATOR);
        Map<Object, Integer> tagsMap = new HashMap<Object, Integer>();
        ArrayList<String> tagsArray = new ArrayList<String>();
        for (String eachTag : tagsStrings) {
            String planTag = eachTag.trim();
            tagsArray.add(planTag);
            if (!tagsMap.containsKey(planTag)) {
                tagsMap.put(planTag, 1);
            } else {
                tagsMap.put(planTag, tagsMap.get(planTag) + 1);
            }
        }

        tagsMap = sortByComparator(tagsMap, false);

        StringBuffer wordingBuffer = new StringBuffer();
        wordingBuffer.append("是一位");
        int index = 0;
        for (Map.Entry<Object, Integer> entry : tagsMap.entrySet()) {

            wordingBuffer.append("<font color="+HIGHLIGHT_RED_COLOR+">");
            wordingBuffer.append(entry.getKey());
            wordingBuffer.append("</font>");

            if (index < tagsMap.size() - 1) {
                wordingBuffer.append(TAGS_SEPARATOR + " ");
            }
            index++;
        }
        wordingBuffer.append("的人。");

        Log.d(DailyKind.TAG, "Tags Man: " + wordingBuffer.toString());
        reportWordings.add(wordingBuffer.toString());

    }

    private Map<Object, Integer> sortByComparator(Map<Object, Integer> unsortMap, final boolean order)
    {

        List<Map.Entry<Object, Integer>> list = new LinkedList<Map.Entry<Object, Integer>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<Object, Integer>>() {
            public int compare(Map.Entry<Object, Integer> o1,
                               Map.Entry<Object, Integer> o2) {
                if (order) {
                    return o1.getValue().compareTo(o2.getValue());
                }
                else {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<Object, Integer> sortedMap = new LinkedHashMap<Object, Integer>();
        for (Map.Entry<Object, Integer> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    private void printMap(Map<String, Integer> map) {
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            Log.d(DailyKind.TAG, "Key : " + entry.getKey() + " Value : "+ entry.getValue());
        }
    }

    public String getJoinSince() {
        return joinSince;
    }

    public void setJoinSince(String joinSince) {
        this.joinSince = joinSince;
    }

    public ParseUser getUser() {
        return user;
    }

    public void setUser(ParseUser user) {
        this.user = user;
    }

    public AnalyseListener getAnalyseListener() {
        return analyseListener;
    }

    public void setAnalyseListener(AnalyseListener analyseListener) {
        this.analyseListener = analyseListener;
    }
}
