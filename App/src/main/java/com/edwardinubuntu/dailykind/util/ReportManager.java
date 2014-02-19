package com.edwardinubuntu.dailykind.util;

import android.util.Log;
import com.edwardinubuntu.dailykind.DailyKind;
import com.parse.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;

/**
 * Created by edward_chiang on 2014/2/17.
 */
public class ReportManager {

    public static final String TAGS_SEPARATOR = ",";

    private List<ParseObject> storiesObjects;

    private List<String> reportWordings;

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

        reportWordings.clear();

        StringBuffer tagsBuffer = new StringBuffer();

        ParseObject latestIdeaStory = null;
        boolean hasFoundLatestIdeaStory = false;

        Map<Object, Integer> ideasMap = new LinkedHashMap<Object, Integer>();
        Map<Object, Integer> areaNameMap = new LinkedHashMap<Object, Integer>();

        int numberOfReviewStars = 0;

        int numberOfStoriesLastMonth = 0;
        int numberOfStoriesInCurrentMonth = 0;

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
            // Collect area
            if (eachStory.has("areaName")) {
                String areaName = eachStory.getString("areaName");
                if (!areaNameMap.containsKey(areaName)) {
                    areaNameMap.put(areaName, 1);
                } else {
                    areaNameMap.put(areaName, areaNameMap.get(areaName) + 1);
                }
            }

            // Collect review impact
            if (eachStory.has("reviewImpact")) {
                numberOfReviewStars += eachStory.getInt("reviewImpact");
            }

            // How many is in this month
            Date storyCreatedAt = eachStory.getCreatedAt();
            Calendar storyCreatedAtCalendar = GregorianCalendar.getInstance();
            storyCreatedAtCalendar.setTime(storyCreatedAt);

            Calendar currentCalendar = GregorianCalendar.getInstance();
            currentCalendar.setTime(new Date());

            if (storyCreatedAtCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)) {
                numberOfStoriesInCurrentMonth++;
            }
            if (storyCreatedAtCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) - 1) {
                numberOfStoriesLastMonth++;
            }
        }
        // Get Analyse 1
        extractIdeaTags(tagsBuffer);

        // Get Analyse 3
        if (latestIdeaStory!=null) {
            StringBuffer describeBuffer = new StringBuffer();
            describeBuffer.append("最近參加的友善行動");
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
            describeBuffer.append("比較少人能達成的");
            describeBuffer.append("<font color="+HIGHLIGHT_RED_COLOR+">");
            describeBuffer.append(firstParseObject.getString("Name"));
            describeBuffer.append("</font>");
            describeBuffer.append(user.getString("name"));
            describeBuffer.append("辦到了！");
            reportWordings.add(describeBuffer.toString());
        }

        if (storiesObjects.size() > 0) {
            ParseQuery<ParseObject> userImpactQuery = ParseQuery.getQuery("UserImpact");
            userImpactQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
            userImpactQuery.setMaxCacheAge(DailyKind.QUERY_MAX_CACHE_AGE);

            final int finalTotalReviewStars = numberOfReviewStars;

            userImpactQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if (parseObjects!= null) {


                        int totalCount = 0;
                        int totalReviewStarsImpact = 0;
                        for (ParseObject eachImpact : parseObjects) {
                            if (eachImpact.has("sharedStoriesCount")) {
                                totalCount += eachImpact.getInt("sharedStoriesCount");
                            }
                            if (eachImpact.has("reviewStarsImpact")) {
                                totalReviewStarsImpact += eachImpact.getInt("reviewStarsImpact");
                            }
                        }

                        // Analyse 5
                        // Analyse 6
                        analyseStoriesSharedCount(parseObjects, totalCount);
                        analyseReviewsStars(parseObjects, totalReviewStarsImpact, finalTotalReviewStars);

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
                if (index < Math.min(TOTAL_TO_SHOW - 1, areaNameMap.entrySet().size() - 1)) {
                    describeBuffer.append("、");
                }
                index++;
            }
            describeBuffer.append("有深度的連結，讓這些地方變不一樣。");
            reportWordings.add(describeBuffer.toString());
        }

        if (numberOfStoriesInCurrentMonth > 0) {
            StringBuffer describeBuffer = new StringBuffer();
            describeBuffer.append("本月份分享了" + numberOfStoriesInCurrentMonth + "篇故事。");
            describeBuffer.append("上個月份分享了" + numberOfStoriesLastMonth + "篇故事。");

            float changedPercentage = (float) numberOfStoriesInCurrentMonth / numberOfStoriesLastMonth;
            NumberFormat numberFormat = NumberFormat.getNumberInstance();
            numberFormat.setMaximumFractionDigits(2);

            describeBuffer.append("成長 ");
            describeBuffer.append("<font color="+HIGHLIGHT_RED_COLOR+">");
            describeBuffer.append(numberFormat.format(changedPercentage));
            describeBuffer.append("</font>");
            reportWordings.add(describeBuffer.toString());
        }

        if (getAnalyseListener() != null) {
            getAnalyseListener().done();
        }
    }

    private void analyseStoriesSharedCount(List<ParseObject> parseObjects, int totalCount) {

        float averageSharedCount = (float) totalCount / parseObjects.size();
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(2);

        StringBuffer describeBuffer = new StringBuffer();
        describeBuffer.append("分享過");
        describeBuffer.append("<font color="+HIGHLIGHT_RED_COLOR+">");
        describeBuffer.append(storiesObjects.size());
        describeBuffer.append("</font>");
        describeBuffer.append("篇故事，");
        describeBuffer.append("比一般平均 " + numberFormat.format(averageSharedCount)+ " 篇故事");

        describeBuffer.append("<font color="+HIGHLIGHT_RED_COLOR+">");
        if (storiesObjects.size() > averageSharedCount) {
            describeBuffer.append("用心記錄");
        } else {
            describeBuffer.append("少些");
        }
        describeBuffer.append("</font>");
        describeBuffer.append("。");
        reportWordings.add(describeBuffer.toString());
    }

    private void analyseReviewsStars(List<ParseObject> parseObjects, int totalReviewStarsImpact, int finalTotalReviewStars) {
        if (finalTotalReviewStars > 0) {
            ParseQuery<ParseObject> storyQuery = ParseQuery.getQuery("Story");
            storyQuery.whereGreaterThan("reviewImpact", 0);


            StringBuffer describeBuffer = new StringBuffer();
            describeBuffer.append("累積獲得");
            describeBuffer.append("<font color="+HIGHLIGHT_RED_COLOR+">");
            describeBuffer.append(finalTotalReviewStars);
            describeBuffer.append("</font>");
            describeBuffer.append("顆星星");
            describeBuffer.append("。");

            double averageReviewStarsCount = new BigDecimal(totalReviewStarsImpact / parseObjects.size())
                    .setScale(2, BigDecimal.ROUND_HALF_UP)
                    .doubleValue();

            describeBuffer.append("比一般平均" + averageReviewStarsCount+ "顆星");

            describeBuffer.append("<font color="+HIGHLIGHT_RED_COLOR+">");
            if (finalTotalReviewStars > averageReviewStarsCount) {
                describeBuffer.append("多些");
            } else {
                describeBuffer.append("少些");
            }
            describeBuffer.append("</font>");
            describeBuffer.append("。");

            reportWordings.add(describeBuffer.toString());
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
