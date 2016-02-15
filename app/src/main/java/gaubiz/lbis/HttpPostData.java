package gaubiz.lbis;

import android.app.Application;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class HttpPostData extends Application {

    //스마트비즈온 서버 url
    private String url = "http://221.144.139.21:25080/index.php/Gis/gatherGis?uuid=";
    private URL urlSend = null;

    public void HttpPostData(String uuid, String lat, String lon) {
        try {
            //URL 설정하고 접속하기
            urlSend = new URL(url + uuid + "&lat=" + lat + "&lon=" + lon);
            HttpURLConnection http = (HttpURLConnection) urlSend.openConnection();

            http.setDefaultUseCaches(false);        //기본 전송 모드 설정
            http.setDoInput(true);                  // 서버에서 읽기 모드 지정
            http.setDoOutput(true);                 // 서버로 쓰기 모드 지정
            http.setRequestMethod("POST");          // 전송 방식은 POST

            //문자스트림(유니코드(EUC-KR))을 바이트 스트림으로 변경해서 서버로 전송
            OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "EUC-KR");
            PrintWriter writer = new PrintWriter(outStream);
            writer.flush();     //데이타 입력

            //바이트 스트림에서 문자스트림으로 변경해서 서버에 쓰기
            InputStreamReader tmp = new InputStreamReader(http.getInputStream(), "EUC-KR");
            BufferedReader reader = new BufferedReader(tmp);
            StringBuilder builder = new StringBuilder();
            String str;
            while ((str = reader.readLine()) != null) {       // 서버에서 라인단위로 보내줄 것이므로 라인단위로 읽는다
                builder.append(str);
            }

        }// try
        catch (MalformedURLException e) { }
        catch (IOException e) { }
    } // HttpPostData end
}
