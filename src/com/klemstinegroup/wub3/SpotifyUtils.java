package com.klemstinegroup.wub3;

import com.echonest.api.v4.TrackAnalysis;
import com.wrapper.spotify.Api;
import com.wrapper.spotify.exceptions.WebApiException;
import com.wrapper.spotify.methods.AddTrackToPlaylistRequest;
import com.wrapper.spotify.methods.PlaylistCreationRequest;
import com.wrapper.spotify.methods.TrackRequest;
import com.wrapper.spotify.methods.authentication.ClientCredentialsGrantRequest;
import com.wrapper.spotify.models.AuthorizationCodeCredentials;
import com.wrapper.spotify.models.Playlist;
import com.wrapper.spotify.models.Track;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ssl.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Paul on 2/23/2017.
 */
public class SpotifyUtils {

    private static Api api;
    private static String token;

    public static void main(String[] args) {
        new SpotifyUtils();
    }

    public static void setAccessToken() {

        api = Api.builder()
                .clientId(Credentials.clientId)
                .clientSecret(Credentials.clientSecret)
                .redirectURI("http://127.0.0.1:8002")
                .build();
        /* Create a request object. */
        final ClientCredentialsGrantRequest request1 = api.clientCredentialsGrant().build();
        /* Set the necessary scopes that the application will need from the user */
        final List<String> scopes = Arrays.asList("playlist-read-private", "playlist-modify-private", "playlist-modify-public");
        final String state = "someExpectedStateString";

        String authorizeURL = api.createAuthorizeURL(scopes, state);
        System.out.println(authorizeURL);
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(authorizeURL));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        Scanner keyboard = new Scanner(System.in);
//        String code=keyboard.nextLine();
        String code = null;
        try {
            ServerSocket serverSocket = new ServerSocket(8002);
            Socket socket = serverSocket.accept();
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String code1 = br.readLine();
            code = code1.substring(code1.indexOf("=") + 1, code1.indexOf("&"));
            System.out.println("Message: " + code);
            System.out.println("Message: " + code1);

            br.close();
            serverSocket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            AuthorizationCodeCredentials authorizationCodeCredentials = api.authorizationCodeGrant(code).build().get();
            System.out.println("Successfully retrieved an access token! " + authorizationCodeCredentials.getAccessToken());
            System.out.println("The access token expires in " + authorizationCodeCredentials.getExpiresIn() + " seconds");
            System.out.println("Luckily, I can refresh it using this refresh token! " + authorizationCodeCredentials.getRefreshToken());
            api.setAccessToken(authorizationCodeCredentials.getAccessToken());
            api.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            token = authorizationCodeCredentials.getAccessToken();


        } catch (IOException e) {
            e.printStackTrace();
        } catch (WebApiException e) {
            e.printStackTrace();
        }

/* Use the request object to make the request, either asynchronously (getAsync) or synchronously (get) */
//        try {
//            ClientCredentials response = request1.get();
//
//            token = response.getAccessToken();
//            api.setAccessToken(token);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (WebApiException e) {
//            e.printStackTrace();
//        }
    }

    protected static void downloadProcess(String s) {

        System.out.println(s);

    }

    protected static void download(String playlist) {
        try {
            Runtime rt = Runtime.getRuntime();
            System.out.println("enter captcha code:");
            Scanner sc = new Scanner(System.in);
            String captcha = sc.nextLine();
            String command = "node ./spotifydownloader2/main.js -u " + Credentials.user + " -p " + Credentials.pass + " -i " + playlist + " --captcha " + captcha; //+ (out != null ? " -d " + out.getPath() : "")
            System.out.println(command);
            downloadProcess("Command: " + command);
            Process current = rt.exec(command, null, new File(System.getProperty("user.dir")));
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(current.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(current.getErrorStream()));
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                downloadProcess(s);
            }
            while ((s = stdError.readLine()) != null) {
                downloadProcess(s);
            }
            current = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void downloadSong(String id) {
        if (token == null) setAccessToken();
        final PlaylistCreationRequest request = api.createPlaylist(Credentials.user, "wub")
                .publicAccess(true)
                .build();

        try {
            final Playlist playlist = request.get();
            ArrayList<String> list = new ArrayList<String>();
            list.add("spotify:track:" + id);
            final AddTrackToPlaylistRequest request1 = api.addTracksToPlaylist(Credentials.user, playlist.getId(), list)
                    .position(0)
                    .build();

            try {
                request1.get();
//                download(playlist.getId());
                download("https://play.spotify.com/track/" + id);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (WebApiException e) {
                e.printStackTrace();
            }

        } catch (WebApiException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


        public static HttpsURLConnection getConnection( URL url) throws KeyManagementException, NoSuchAlgorithmException, IOException{
            SSLContext ctx = SSLContext.getInstance("TLS");
                ctx.init(null, new TrustManager[] { new InvalidCertificateTrustManager() }, null);
            SSLContext.setDefault(ctx);

//            String authEncoded = Base64.encodeBytes(authStr.getBytes());

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
//            connection.setRequestProperty("Authorization", "Basic " + authEncoded);

                connection.setHostnameVerifier(new InvalidCertificateHostVerifier());

            return connection;
    }

    public static JSONObject getDownloadList(String q) {
        String urlString = "https://datmusic.xyz/search?q=" + q.toString();
        System.out.println(urlString);
        URL url=null;
        try {
            url=new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            InputStream is =getConnection(url).getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            String s = "";
            while ((line = br.readLine()) != null) {
                s += line;
            }
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void downloadFile(String id) {
        Track track = getTrack(id);
        try {
            String artist = URLEncoder.encode(track.getArtists().get(0).getName(), "UTF-8");

            String title = URLEncoder.encode(track.getName(), "UTF-8");
            System.out.println(artist + "\t" + title);
            String s = "https://spoti.herokuapp.com/download?artist=" + artist + "&title=" + title;
            System.out.println(s);

            URL u = new URL(s);
            ReadableByteChannel rbc = Channels.newChannel(u.openStream());
            FileOutputStream fos = new FileOutputStream(id + ".mp3");
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            rbc.close();
            System.out.println("downloaded!");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static Track getTrack(String id) {
        if (token == null) setAccessToken();
        final TrackRequest request = api.getTrack(id).build();

        try {
            final Track track = request.get();
            return track;
        } catch (WebApiException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static TrackAnalysis getAnalysis(String id) throws IOException, ParseException {
        if (token == null) setAccessToken();
        String stringUrl = "https://api.spotify.com/v1/audio-analysis/" + id;
        URL url = new URL(stringUrl);
        URLConnection uc = url.openConnection();
        uc.setRequestProperty("Authorization", "Bearer " + token);

        BufferedReader br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String out = "";
        String line = "";
        while ((line = br.readLine()) != null) {
            out += line;
        }
        JSONParser parser = new JSONParser();
        JSONObject jso = (JSONObject) parser.parse(out);
        TrackAnalysis ta = new TrackAnalysis(jso);
        return ta;

    }

}


/**
 * ignore invalid Https certificate from OPAM
 * <p>see http://javaskeleton.blogspot.com.br/2011/01/avoiding-sunsecurityvalidatorvalidatore.html
 */
class InvalidCertificateTrustManager implements X509TrustManager {
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    public void checkServerTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException {

    }

    public void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException {
    }
}

class InvalidCertificateHostVerifier implements HostnameVerifier {
    public boolean verify(String paramString, SSLSession paramSSLSession) {
        return true;
    }
}