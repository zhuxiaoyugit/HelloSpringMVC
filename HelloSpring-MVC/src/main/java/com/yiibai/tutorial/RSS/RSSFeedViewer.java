package com.yiibai.tutorial.RSS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.feed.AbstractRssFeedView;

import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.rss.Content;
import com.rometools.rome.feed.rss.Item;

public class RSSFeedViewer extends AbstractRssFeedView {

   @Override
   protected void buildFeedMetadata(Map<String, Object> model, Channel feed,
      HttpServletRequest request) {

      feed.setTitle("YiibaiPoint Dot Com");
      feed.setDescription("Java Yiibai and Examples");
      feed.setLink("http://www.yiibai.com");

      super.buildFeedMetadata(model, feed, request);
   }

   @Override
   protected List<Item> buildFeedItems(Map<String, Object> model,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

      List<RSSMessage> listContent = (List<RSSMessage>) model.get("feedContent");
      List<Item> items = new ArrayList<Item>(listContent.size());

      for(RSSMessage tempContent : listContent ){

         Item item = new Item();

         Content content = new Content();
         content.setValue(tempContent.getSummary());
         item.setContent(content);

         item.setTitle(tempContent.getTitle());
         item.setLink(tempContent.getUrl());
         item.setPubDate(tempContent.getCreatedDate());

         items.add(item);
      }

      return items;        
   }
}
