package gaubiz.lbis;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.lang.String;
import java.util.UUID;


public class MainActivity extends Activity {

    LocationManager locationManager;    //git

    TextView mStatus;       //서비스 사용 가능 여부 표시
    TextView tProvider;     //Provider(GPS, Network)정보 표시
    TextView mResult;       //좌표, 횟수 표시

    String mProvider;       //Provider 정보를 담을 변수
    int mCount;             //위치정보 수집 횟수를 담을 변수

    private String uuid, lat, lon; //uuid, 위도, 경도

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //위치정보 관리자 호출
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //textView 로드
        mStatus = (TextView) findViewById(R.id.status);
        tProvider = (TextView) findViewById(R.id.tProvider);
        mResult = (TextView) findViewById(R.id.result);

        //UUID(Device 공유키, sim 카드 번호, 안드로이드 ID)
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;

        //디바이스 고유키 가져오기
        tmDevice = "" + tm.getDeviceId();
        //sim 카드 번호 가져오기
        tmSerial = "" + tm.getSimSerialNumber();
        //안드로이드 ID (사용할 수 있는 OS버전이 제한적)
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        //hashCode로 받은 3개의 번호를 합쳐서 UUID 생성
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        uuid = deviceUuid.toString();

        //http post 방식으로 전달을 위해 생성
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }//onCreate end

    public void onResume() {
        super.onResume();
        //최적의 provider 선택
        mProvider = locationManager.getBestProvider(new Criteria(), true);
        //TextView 표시
        mStatus.setText("현재 상태: ");
        tProvider.setText("현재 Provider: " + mProvider);
        //수신회수: GPS <-> Network 서비스로 변경이 될 경우 회수를 초기화
        mCount = 0;
        try{
            //하드웨어이름, 갱신시간주기(ms), 갱신거리주기(m), 이벤트핸들러
            locationManager.requestLocationUpdates(mProvider, 200, 1, mListener);
        } catch (SecurityException e) {
            Log.e("Permission_Exception","Permission not granted");
        }
    }

    public void onPause() {
        super.onPause();
        try {
            locationManager.removeUpdates(mListener);
        } catch (SecurityException e) {
            Log.e("Permission_Exception","Permission not granted");
        }
    }

    /*
     *LocationListener
     *이 클래스는 위치 정보를 위치 공급자로부터 지속적으로 받아온다.
     *---오버라이드 메서드 종류---
     * void onLocationChanged(Location location) 위치 정보 수집 (위치 이동, 시간 경과 등으로)
     * void onProviderDisabled(String provider) 위치 공급자 사용 불가능할 때 호출
     * void onProviderEnabled(String provider) 위치 공급자 사용 가능해 질 때 호출
     * void onStatusChanged(String provider, int status, Bundle extras) 위치 공급자 상태가 바뀔 때 호출
    */
    LocationListener mListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {

            mCount++;   //횟수
            //TextView(mResult)에 좌표 표시를 위해 스트링으로 변환
            String mLocation = String.format(
                    "현재 위치: 수신회수(%d)\n\n" + "위도:%f\n" + "경도:%f\n",
                    mCount, location.getLatitude(), location.getLongitude()
            );
            //TextView(mResult)에 좌표 등을 표시
            mResult.setText(mLocation);

            //좌표받기
            double d1 = location.getLatitude();
            double d2 = location.getLongitude();
            //좌표 스트링으로 형변환
            lat = String.valueOf(d1);
            lon = String.valueOf(d2);

            //스마트비즈온 서버에 data를 전달하기 위해 http post 방식으로 메소드 반복해서 실행
            HttpPostData hpd = (HttpPostData) getApplicationContext();
            hpd.HttpPostData(uuid, lat, lon);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            String sStatus = "";
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                    sStatus = " GPS 범위 벗어남";
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    sStatus = " GPS 일시적 불능";
                    break;
                case LocationProvider.AVAILABLE:
                    sStatus = " GPS 사용 가능";
                    break;
            }
            mStatus.setText("현재 상태: " + sStatus);
        }

        @Override
        public void onProviderEnabled(String provider) {
            mStatus.setText("현재 상태: 서비스 사용 가능");
        }

        @Override
        public void onProviderDisabled(String provider) {
            mStatus.setText("현재 상태: 서비스 사용 불가");
        }
    };//LocationListener end


    //웹뷰 실행 버튼
    public void onButtonClickedWebView(View v) {
        Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
        startActivity(intent);
    }

}//end