package com.lovingheart.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.R;
import com.lovingheart.app.listener.LoadMoreListener;
import com.lovingheart.app.object.Idea;

import java.util.List;

/**
 * Created by edward_chiang on 2013/12/21.
 */
public class IdeaArrayAdapter extends ArrayAdapter<Idea> {

    private LoadMoreListener loadMoreListener;

    private boolean isLoadMoreEnd;

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public IdeaArrayAdapter(Context context, int resource, List<Idea> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rootView = convertView;
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.cell_idea_with_done, null);
        }

        Idea idea = getItem(position);

        TextView nameTextView = (TextView)rootView.findViewById(R.id.idea_title_text_view);
        nameTextView.setText(idea.getName());

        TextView descriptionTextView = (TextView)rootView.findViewById(R.id.idea_why_text_view);
        descriptionTextView.setText(idea.getIdeaDescription());

        TextView doneTextView = (TextView)rootView.findViewById(R.id.idea_done_text_view);
        if (idea.getDoneCount() > 0) {
        doneTextView.setText(
                getContext().getString(R.string.deed_of_number_of_people_prefix_short) +
                        getContext().getString(R.string.space) +
                        idea.getDoneCount() +
                        getContext().getString(R.string.space) +
                        (idea.getDoneCount() > 1 ?
                                getContext().getString(R.string.deed_of_number_of_people_post_times) :
                                getContext().getString(R.string.deed_of_number_of_people_post_time))
        );
            doneTextView.setVisibility(View.VISIBLE);
        } else {
            doneTextView.setVisibility(View.INVISIBLE);
        }
            
       if (position >= getCount() - 1
                && getLoadMoreListener() != null
                && !isLoadMoreEnd()
                && getCount() >= DailyKind.PARSE_QUERY_LIMIT) {
            getLoadMoreListener().notifyLoadMore();
        }

        return rootView;
    }

    public LoadMoreListener getLoadMoreListener() {
        return loadMoreListener;
    }

    public void setLoadMoreListener(LoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

    public boolean isLoadMoreEnd() {
        return isLoadMoreEnd;
    }

    public void setLoadMoreEnd(boolean isLoadMoreEnd) {
        this.isLoadMoreEnd = isLoadMoreEnd;
    }
}
