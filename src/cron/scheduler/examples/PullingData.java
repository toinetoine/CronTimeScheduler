package cron.scheduler.examples;

import cron.scheduler.main.CronScheduler;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

/**
 * Print the top posts for /r/programming every minute
 */
public class PullingData {
    public static void main(String[] args) throws IOException, InterruptedException {
        URL url = new URL("https://www.reddit.com/r/programming.json?sort=top");

        // trigger every 1 minute
        CronScheduler cronScheduler = new CronScheduler("* * * * *");
        long nextPullTime = cronScheduler.getNextTime(TimeUnit.MILLISECONDS);

        while (true) {
            if (System.currentTimeMillis() >= nextPullTime) {
                URLConnection connection = url.openConnection();
                connection.setRequestProperty("Accept-Charset", "UTF-8");
                connection.setRequestProperty("User-agent", "example");
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                java.util.Scanner scanner = new java.util.Scanner(in).useDelimiter("\\A");
                if (scanner.hasNext()) {
                    String responseJson = scanner.next();
                    JSONObject obj = new JSONObject(responseJson);
                    JSONArray topPosts = obj.getJSONObject("data").getJSONArray("children");
                    for (int i = 0; i < topPosts.length(); i++) {
                        JSONObject post = topPosts.getJSONObject(i).getJSONObject("data");
                        System.out.println(post.getInt("score") + "\t\t" +
                                post.getString("title") + "\t\t" + post.getString("url"));
                    }
                }

                nextPullTime = cronScheduler.getNextTime(TimeUnit.MILLISECONDS);
            }
            Thread.sleep(1000);
        }

    }
}