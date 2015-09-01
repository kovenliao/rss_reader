package com.yuzhou.rss.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.yuzhou.rss.R;
import com.yuzhou.rss.api.ContentFetcher;
import com.yuzhou.rss.parser.RssFeed;
import com.yuzhou.rss.parser.RssItem;
import com.yuzhou.rss.parser.RssReader;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class NewsFragment extends Fragment
{
    private static final String ARG_NEWS_URL = "news_url";
    private static final String ARG_RESOURCE_LAYOUT = "layout";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static NewsFragment newInstance(String url, int layout)
    {
        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NEWS_URL, url);
        args.putInt(ARG_RESOURCE_LAYOUT, layout);
        fragment.setArguments(args);
        return fragment;
    }

    private EventBus eventBus;
    private MainActivity activity;
    private RssItemAdapter adapter;
    private ListView listView;
    private List<RssItem> items;

    public NewsFragment()
    {
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        this.activity = (MainActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        eventBus = new EventBus();
        eventBus.register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        listView = (ListView) rootView.findViewById(R.id.listView);
        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        int layout = getArguments().getInt(ARG_RESOURCE_LAYOUT);

        items = new ArrayList<>();
        adapter = new RssItemAdapter(activity, layout, items);
        listView.setAdapter(adapter);

        ContentFetcher fetcher = new ContentFetcher(eventBus);
        fetcher.execute(getArguments().getString(ARG_NEWS_URL));
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        eventBus.unregister(this);
    }

    @Subscribe
    public void updateDownloadResult(List<String> result)
    {
        try {
            RssFeed feed = RssReader.read(result.get(0));
            items.clear();
            items.addAll(feed.getRssItems());

            activity.onSectionAttached(feed.getTitle());
            adapter.notifyDataSetChanged();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
