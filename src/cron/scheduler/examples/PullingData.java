package cron.scheduler.examples;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Print the top posts for the subreddit specified as the first arg (or /r/programming if no args provided)
 */
public class PullingData {
    public static void main(String[] args) throws IOException, InterruptedException {
        final String subName = args.length > 0 ? args[0] : "programming";
        URLConnection connection = new URL("https://www.reddit.com/r/" + subName + ".json?sort=top").openConnection();
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.setRequestProperty("User-agent", "example");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                connection.getInputStream()));

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
    }
}