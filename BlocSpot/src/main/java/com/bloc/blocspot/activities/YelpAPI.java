package com.bloc.blocspot.activities;

/**
 * Created by Mark on 2/22/2015.
 */


import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.bloc.blocspot.BlocSpotApplication;
import com.bloc.blocspot.blocspot.R;
import com.bloc.blocspot.places.yelp.TwoStepOAuth;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

public class YelpAPI extends ListActivity {

    private static final String API_HOST = "api.yelp.com";
    private static final String DEFAULT_TERM = "dinner";
    private static final String DEFAULT_LOCATION = "San Francisco, CA";
    private static final int SEARCH_LIMIT = 3;
    private static final String SEARCH_PATH = "/v2/search";
    private static final String BUSINESS_PATH = "/v2/business";

    private static final String CONSUMER_KEY = "IStKxhkWSvHYwl0SjaySsw";
    private static final String CONSUMER_SECRET = "tOGCifGgX6pUN5RvuHBkuGFTZ-M";
    private static final String TOKEN = "AksabBDBODZMzTnMpIesbz1oQRu2R-Ru";
    private static final String TOKEN_SECRET = "g8zVLrWpcGUbyLmmO3TPWMUoLvA";

    private OAuthService service;
    private Token accessToken;
    private ListAdapter adapter;


    /**
     * Setup the Yelp API OAuth credentials.
     *
     * @param consumerKey Consumer key
     * @param consumerSecret Consumer secret
     * @param token Token
     * @param tokenSecret Token secret
     */
    public YelpAPI(String consumerKey, String consumerSecret, String token, String tokenSecret) {
        this.service =
                new ServiceBuilder().provider(TwoStepOAuth.class).apiKey(consumerKey)
                        .apiSecret(consumerSecret).build();
        this.accessToken = new Token(token, tokenSecret);
    }

    public YelpAPI(){

    }

    /**
     * Creates and sends a request to the Search API by term and location.
     * <p>
     * See <a href="http://www.yelp.com/developers/documentation/v2/search_api">Yelp Search API V2</a>
     * for more info.
     *
     * @param term <tt>String</tt> of the search term to be queried
     * @param location <tt>String</tt> of the location
     * @return <tt>String</tt> JSON Response
     */
    public String searchForBusinessesByLocation(String term, String location) {
        OAuthRequest request = createOAuthRequest(SEARCH_PATH);
        request.addQuerystringParameter("term", term);
        request.addQuerystringParameter("location", location);
        request.addQuerystringParameter("limit", String.valueOf(SEARCH_LIMIT));
        return sendRequestAndGetResponse(request);
    }

    public String searchForBusinessesByGeoLocation(String term, Location location) {
        OAuthRequest request = createOAuthRequest(SEARCH_PATH);
        request.addQuerystringParameter("term", term);
        request.addQuerystringParameter("ll", location.getLatitude() + "," + location.getLongitude());
        request.addQuerystringParameter("limit", String.valueOf(SEARCH_LIMIT));
        Log.i(getClass().getSimpleName(), request.toString());
        return sendRequestAndGetResponse(request);
    }

    /**
     * Creates and sends a request to the Business API by business ID.
     * <p>
     * See <a href="http://www.yelp.com/developers/documentation/v2/business">Yelp Business API V2</a>
     * for more info.
     *
     * @param businessID <tt>String</tt> business ID of the requested business
     * @return <tt>String</tt> JSON Response
     */
    public String searchByBusinessId(String businessID) {
        OAuthRequest request = createOAuthRequest(BUSINESS_PATH + "/" + businessID);
        return sendRequestAndGetResponse(request);
    }

    /**
     * Creates and returns an {@link OAuthRequest} based on the API endpoint specified.
     *
     * @param path API endpoint to be queried
     * @return <tt>OAuthRequest</tt>
     */
    private OAuthRequest createOAuthRequest(String path) {
        OAuthRequest request = new OAuthRequest(Verb.GET, "http://" + API_HOST + path);
        return request;
    }

    /**
     * Sends an {@link OAuthRequest} and returns the {@link Response} body.
     *
     * @param request {@link OAuthRequest} corresponding to the API request
     * @return <tt>String</tt> body of API response
     */
    private String sendRequestAndGetResponse(OAuthRequest request) {
        System.out.println("Querying " + request.getCompleteUrl() + " ...");
        this.service.signRequest(this.accessToken, request);
        Response response = request.send();
        return response.getBody();
    }

    /**
     * Queries the Search API based on the command line arguments and takes the first result to query
     * the Business API.
     *
     * @param yelpApi <tt>YelpAPI</tt> service instance
     * @param yelpApiCli <tt>YelpAPICLI</tt> command line arguments
     */
    private static void queryAPI(YelpAPI yelpApi, YelpAPICLI yelpApiCli) {
        String searchResponseJSON =
                yelpApi.searchForBusinessesByLocation(yelpApiCli.term, yelpApiCli.location);

        JSONParser parser = new JSONParser();
        JSONObject response = null;
        try {
            response = (JSONObject) parser.parse(searchResponseJSON);
        } catch (ParseException pe) {
            System.out.println("Error: could not parse JSON response:");
            System.out.println(searchResponseJSON);
            System.exit(1);
        }

        JSONArray businesses = (JSONArray) response.get("businesses");
        JSONObject firstBusiness = (JSONObject) businesses.get(0);
        String firstBusinessID = firstBusiness.get("id").toString();
        System.out.println(String.format(
                "%s businesses found, querying business info for the top result \"%s\" ...",
                businesses.size(), firstBusinessID));

        // Select the first business and display business details
        String businessResponseJSON = yelpApi.searchByBusinessId(firstBusinessID.toString());
        System.out.println(String.format("Result for business \"%s\" found:", firstBusinessID));
        System.out.println(businessResponseJSON);
    }

    /**
     * Command-line interface for the sample Yelp API runner.
     */
    private static class YelpAPICLI {
        @Parameter(names = {"-q", "--term"}, description = "Search Query Term")
        public String term = DEFAULT_TERM;

        @Parameter(names = {"-l", "--location"}, description = "Location to be Queried")
        public String location = DEFAULT_LOCATION;
    }

    public static void main(String[] args) {
        YelpAPICLI yelpApiCli = new YelpAPICLI();
        new JCommander(yelpApiCli, args);

        YelpAPI yelpApi = new YelpAPI(CONSUMER_KEY, CONSUMER_SECRET, TOKEN, TOKEN_SECRET);
        queryAPI(yelpApi, yelpApiCli);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yelpapi);

        YelpAPI yelpApi = new YelpAPI(CONSUMER_KEY, CONSUMER_SECRET, TOKEN, TOKEN_SECRET);

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            String result = yelpApi.searchForBusinessesByLocation(query, BlocSpotApplication.getSharedDataSource().getLocation().getLatitude() + "," + BlocSpotApplication.getSharedDataSource().getLocation().getLongitude());
        }


        //TextView title = (TextView) findViewById(R.id.yelp_title);
        //TextView body = (TextView) findViewById(R.id.yelp_body);

        //setListAdapter(adapter);


    }
}