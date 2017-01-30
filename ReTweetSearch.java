import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.ParseException;

/**
 * Created by jasonhertz on 1/25/17.
 * A twitter bot to retweet results of refined search (no RTs and no repeats)
 */

public class ReTweetSearch {

    public static void main(String[] args) throws TwitterException, IOException, ParseException {

        //set keys, tokens
        String TWITTER_CONSUMER_KEY = "By0507W449b0wabjL3kCbCEhT";
        String TWITTER_SECRET_KEY = "xcfarnRv8kW4VwqMpvdouwdBNuj9B60yTdy69QMO8eY3pWXGzb";
        String TWITTER_ACCESS_TOKEN = "799821003476140032-GwmEiZO5fPJbzgJ5CnH9bYvrhhur4A3";
        String TWITTER_ACCESS_TOKEN_SECRET = "6hcpOdShqD0VmKENzlbogGswVXDcsnWiQmDLquCul0KSU";

        //build configuration with keys
        ConfigurationBuilder cb = new ConfigurationBuilder();

        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(TWITTER_CONSUMER_KEY)
                .setOAuthConsumerSecret(TWITTER_SECRET_KEY)
                .setOAuthAccessToken(TWITTER_ACCESS_TOKEN)
                .setOAuthAccessTokenSecret(TWITTER_ACCESS_TOKEN_SECRET);

        //open connection in try loop for error handling
        try {
            TwitterFactory tf = new TwitterFactory(cb.build());
            Twitter twitter = tf.getInstance();

            // set query keywords, string( "," == ||; " " == && )
            Query query = new Query("altac,postac");
            QueryResult result;

            int tweetCount=0; // number of total tweets found in search
            int notRTTweetCount=0; // number of non RT tweets

            //short tweets -- array for comparison used to determine uniqueness
            ArrayList<String> twtsArrayShort = new ArrayList<String>();

            //full-length tweets array for display
            ArrayList<String> twtsArrayFull = new ArrayList<String>();

            //unique tweets array for printing to screen
            ArrayList<String> twtsArrayUnique = new ArrayList<String>();

            //tweet IDs array for making above array
            ArrayList<Long> twtIDs = new ArrayList<Long>();

            //screen names array for URL-checking tweet IDs (URL: twitter.com/{screen name}/status/{tweet ID}
            ArrayList<String> twtSnames = new ArrayList<String>();

            //tweets created_at array for excluding old tweets, search goes about 10 days in the past
            ArrayList<Date> created_at = new ArrayList<Date>();

            //unique tweet IDs array for RT function
            ArrayList<Long> twtsUniqueIDs = new ArrayList<Long>();

            //unique screen names array for URL-checking tweet IDs
            ArrayList<String> twtSnamesUnique = new ArrayList<String>();

            //unique tweets created_at array for excluding old tweets
            ArrayList<Date> created_at_Unique = new ArrayList<Date>();

            do {

                //set QueryResult object equal to search based on query keywords
                result = twitter.search(query);

                // array of search results with RTs and duplicates
                List<Status> tweets = result.getTweets();

                //variables for filtering out RTs and adding remainder to arrays
                String twt,sname;   //tweets and screen names

                int indexOfHttp;    //index of individual strings where "http" starts;
                                    //short URLs take unique forms and confuse filter
                long twtID;         //tweet ID for .retweetStatus({tweet ID}) method
                Date date;          //date variable for filtering out old tweets

                //iterate through each line of tweets list
                for (Status tweet : tweets) {

                    //decide if tweet is a RT
                    if (!tweet.getText().startsWith("RT")) {

                        //if not RT, then add variables to corresponding arrays
                        twt = tweet.getText();
                        twtID = tweet.getId();
                        sname = tweet.getUser().getScreenName();
                        date = tweet.getCreatedAt();

                        //set index number to position of string twt where "http" begins
                        indexOfHttp = twt.indexOf("http");

                        //short URLs take unique forms. when users add them to copied or retweeted text,
                        // their tweets appear original to an unfiltered comparison.
                        // the short tweet array holds all the values without short URLs.

                        //when the substring "http" is not in tweet text, function returns -1,
                        // which confuses substring(0, beg. index #) below
                        if (indexOfHttp==-1) {

                            //if no instance of substring http, then add entire tweet text to comparison array
                            twtsArrayShort.add(twt);

                            //and add full-length full array for display
                            twtsArrayFull.add(twt);

                            //fill ID, Screen Name, and Date arrays
                            twtIDs.add(twtID);
                            twtSnames.add(sname);
                            created_at.add(date);

                        } else if (indexOfHttp>=0){

                            //if an instance of "http," then add short version, without unique URL
                            twtsArrayShort.add(twt.substring(0, indexOfHttp));

                            //and add full-length full array for display
                            twtsArrayFull.add(twt);

                            //fill ID, Screen Name, and Date arrays
                            twtIDs.add(twtID);
                            twtSnames.add(sname);
                            created_at.add(date);
                        }

                        notRTTweetCount++; //add to count of non RTs
                        // all five arrays will have same number of elements in same order

                    }

                    tweetCount++; //add to count of total tweets in search

                }

            } while ((query = result.nextQuery()) !=null); //run search until end of QueryResult object

            //date variables to check results against
            //set to date after last search and retweet
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); //CST here assumed
            Date dateAfter = sdf.parse("01/01/2017 11:03:20");
            
            //variable to mark unique tweets true and repeats false
            boolean unique;
            
            //iterate through array of non-RTs
            for (int i=0; i<notRTTweetCount; i++){

                //set initial variable to true and compare to every other member of array
                unique = true;

                //iterate with tweet[i] static and tweet[j] ascending to end of list
                for (int j = i +1; j<notRTTweetCount; j++) {

                    //if tweet[i+1] == tweet[i], anywhere from 0 to total nummber in array, then tweet[i] is a repeat
                    if (twtsArrayShort.get(j).equals(twtsArrayShort.get(i))) {

                        unique = false;
                        break; //ends loop running on tweet[i] and skips it in order to check the next item in the list
                    }
                }

                //if tweet[i] never tripped the unique boolean, i.e. it's still true, then it is unique
                if(unique) {

                    //if it is unique add variables to unique arrays
                    twtsUniqueIDs.add(twtIDs.get(i));
                    twtSnamesUnique.add(twtSnames.get(i));
                    created_at_Unique.add(created_at.get(i));
                    twtsArrayUnique.add(twtsArrayFull.get(i));
                }
            }

            System.out.println("number of tweets in query: " + tweetCount + "\n");
            System.out.println("number of non-RTs in query: " + notRTTweetCount + "\n");
            System.out.println("number of unique non-RTs in query " + twtsUniqueIDs.size());

            //Display and optional retweet function --
            // WILL SEND 403 Error if account with keys used above has already tweeted or retweeted object

            //iterate through list of unique tweets
            for (int l = 0; l < twtsUniqueIDs.size(); l++) {

                //if created_at date is after variable Date type, then retweet and print results to screen
                if (created_at_Unique.get(l).after(dateAfter)) {

                    twitter.retweetStatus(twtsUniqueIDs.get(l)); //most other actions can be performed here
                    System.out.println("");
                    System.out.println("Retweeted tweet[" + l + "]: " + twtSnamesUnique.get(l) + " " + twtsUniqueIDs.get(l) + " ");
                    System.out.println("Tweet: \t" + twtsArrayUnique.get(l));
                    System.out.println("Date Created: " + created_at_Unique.get(l) + "\n");
                }
            }

            System.exit(0);

        } catch (TwitterException te) {
            te.printStackTrace();;
            System.out.println("Failed to search tweets: " + te.getMessage());
            System.exit(-1);
        }
    }
}
