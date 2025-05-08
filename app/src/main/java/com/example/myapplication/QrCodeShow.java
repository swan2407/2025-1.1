package com.example.test_03_04;
import org.json.JSONObject;
import java.io.IOException;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.Toast;
import android.graphics.Bitmap;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import java.util.Hashtable;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;





public class QrCodeShow extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_show);

        // X 버튼 클릭 이벤트 처리
        ImageButton closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> {
            finish(); // 현재 액티비티 종료
        });

        // 메모리 사용량 모니터링
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        Log.d("Memory", "Used Memory: " + usedMemory + "MB");

        // 이메일이나 다른 데이터로 QR 코드 생성
        String data = "example@email.com";
        Bitmap qrBitmap = generateQRCode(data);

        // ImageView에 QR 코드 표시
        if (qrBitmap != null) {
            ImageView qrImageView = findViewById(R.id.qrCodeImageView);
            qrImageView.setImageBitmap(qrBitmap);
            checkUserEntryStatus(data);
        } else {
            Toast.makeText(this, "QR 코드 생성 실패", Toast.LENGTH_SHORT).show();
        }
    }

    // QR 코드 생성 메소드
    public Bitmap generateQRCode(String data) {
        try {
            MultiFormatWriter writer = new MultiFormatWriter();

            // QR 코드 생성 옵션 설정
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.MARGIN, 1); // QR 코드의 여백 설정

            // BitMatrix 생성 (QR 코드 데이터)
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 500, 500, hints);

            // BitMatrix를 Bitmap으로 변환
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF); // 검정과 흰색 픽셀 설정
                }
            }

            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //서버에서 사용자 entry상태 조회
    private void checkUserEntryStatus(String userId) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://yourserver.com/api/status?user_id=" + userId)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(QrCodeShow.this, "상태 확인 실패", Toast.LENGTH_SHORT).show()
                );
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(QrCodeShow.this, "서버 오류", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                try {
                    JSONObject json = new JSONObject(response.body().string());
                    int entry = json.getInt("entry");

                    if (entry == 0) {
                        sendEntryUpdate(userId, 1, "출입 완료되었습니다");
                    } else if (entry == 1) {
                        runOnUiThread(() -> showExitOptionDialog(userId));
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(QrCodeShow.this, "이미 외출 상태입니다", Toast.LENGTH_SHORT).show()
                        );
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    // 서버로 entry 상태 변경 요청
    private void sendEntryUpdate(String userId, int entryValue, String successMessage) {
        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        try {
            json.put("user_id", userId);
            json.put("entry", entryValue);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url("https://yourserver.com/api/entry") // 실제 API 주소로 교체
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(QrCodeShow.this, "서버 요청 실패", Toast.LENGTH_SHORT).show()
                );
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(QrCodeShow.this, successMessage, Toast.LENGTH_SHORT).show()
                    );
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(QrCodeShow.this, "처리 실패", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
    private void showExitOptionDialog(String userId) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("출입 선택")
                .setMessage("퇴실 또는 외출을 선택하세요")
                .setPositiveButton("퇴실", (d, which) -> sendEntryUpdate(userId, 0, "퇴실되었습니다"))
                .setNegativeButton("외출", (d, which) -> sendEntryUpdate(userId, -1, "외출하였습니다"))
                .setCancelable(false)
                .create();

        dialog.show();


        new Handler().postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
                Toast.makeText(this, "선택 시간이 초과되었습니다", Toast.LENGTH_SHORT).show();
            }
        }, 300000);
    }
}