package se.noren.android.othello.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONArray;
import org.json.JSONObject;

public class HighScoreAdapter {
	String host = "http://legendsbackend.appspot.com";

	public void postHighScore(String username, int points, int currentLevel) {
		PostMethod post = new PostMethod(host + "/api/highscores/othellolegends");
		post.addParameter("owner", username);
		post.addParameter("score", points + "");
		post.addParameter("level", currentLevel + "");
		HttpClient client = new HttpClient();
		try {
			int status = client.executeMethod(post);
			String response = post.getResponseBodyAsString();
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<HighScore> getHighScores() {

		HttpMethod method = new GetMethod(host + "/api/highscores/othellolegends");
		HttpClient client = new HttpClient();
		try {
			int status = client.executeMethod(method);
			String response = method.getResponseBodyAsString();
			return parseJSON(response);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return new ArrayList<HighScore>();
	}

	public List<HighScore> parseJSON(String json) throws Exception {
		List<HighScore> list = new ArrayList<HighScore>();
		 JSONObject jObject = new JSONObject(json);
		 JSONArray array = jObject.getJSONArray("org.springframework.validation.BindingResult.scores");
		 System.out.println("array.lnegth=" + array.length());
		 for (int i = 0; i < array.length(); i++) {
			HighScore highScore = new HighScore();
			highScore.setApplication(array.getJSONObject(i).getString("application"));
			highScore.setOwner(array.getJSONObject(i).getString("owner"));
			highScore.setLevel(array.getJSONObject(i).getLong("level"));
			highScore.setScore(array.getJSONObject(i).getLong("score"));
			highScore.setDate(array.getJSONObject(i).getLong("date"));
			list.add(highScore);
		}
         
		return list;
	}
}
