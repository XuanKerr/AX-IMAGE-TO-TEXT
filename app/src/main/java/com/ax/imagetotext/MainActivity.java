package com.ax.imagetotext;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private static final int PICK_IMAGE = 101;
    private static final int TAKE_PHOTO = 102;
    private static final int SAVE_TEXT = 103;

    private EditText resultText;
    private TextView statusText;
    private Uri cameraUri;
    private TextRecognizer recognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        buildScreen();
    }

    private void buildScreen() {
        int pad = dp(16);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(pad, pad, pad, pad);
        page.setBackgroundColor(Color.rgb(247, 249, 252));

        TextView title = new TextView(this);
        title.setText("AX IMAGE TO TEXT");
        title.setTextSize(23);
        title.setTextColor(Color.rgb(25, 65, 110));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(8), 0, dp(6));
        title.setTypeface(null, 1);
        page.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView intro = new TextView(this);
        intro.setText("Chuyển hình ảnh thành văn bản • Tiếng Việt & English");
        intro.setTextSize(13);
        intro.setTextColor(Color.DKGRAY);
        intro.setGravity(Gravity.CENTER);
        intro.setPadding(0, 0, 0, dp(16));
        page.addView(intro);

        Button gallery = makeButton("CHỌN ẢNH TỪ THƯ VIỆN");
        gallery.setOnClickListener(v -> chooseImage());
        page.addView(gallery, matchWrap());

        Button camera = makeButton("CHỤP ẢNH BẰNG CAMERA");
        camera.setOnClickListener(v -> takePhoto());
        LinearLayout.LayoutParams cameraLp = matchWrap();
        cameraLp.topMargin = dp(8);
        page.addView(camera, cameraLp);

        statusText = new TextView(this);
        statusText.setText("Chọn ảnh hoặc chụp ảnh để bắt đầu nhận dạng.");
        statusText.setTextColor(Color.rgb(70, 80, 90));
        statusText.setTextSize(13);
        statusText.setPadding(2, dp(12), 2, dp(8));
        page.addView(statusText);

        TextView label = new TextView(this);
        label.setText("VĂN BẢN NHẬN DẠNG (có thể chỉnh sửa)");
        label.setTextColor(Color.rgb(35, 55, 75));
        label.setTextSize(14);
        label.setTypeface(null, 1);
        label.setPadding(0, dp(8), 0, dp(6));
        page.addView(label);

        resultText = new EditText(this);
        resultText.setGravity(Gravity.TOP | Gravity.START);
        resultText.setTextSize(16);
        resultText.setHint("Nội dung nhận dạng sẽ hiển thị ở đây...");
        resultText.setMinLines(9);
        resultText.setPadding(dp(12), dp(12), dp(12), dp(12));
        resultText.setBackgroundColor(Color.WHITE);
        resultText.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        LinearLayout.LayoutParams editLp = new LinearLayout.LayoutParams(-1, 0, 1f);
        page.addView(resultText, editLp);

        Button copy = makeButton("SAO CHÉP VĂN BẢN");
        copy.setOnClickListener(v -> copyText());
        LinearLayout.LayoutParams actionLp = matchWrap();
        actionLp.topMargin = dp(10);
        page.addView(copy, actionLp);

        Button save = makeButton("LƯU THÀNH FILE TXT");
        save.setOnClickListener(v -> saveText());
        LinearLayout.LayoutParams saveLp = matchWrap();
        saveLp.topMargin = dp(8);
        page.addView(save, saveLp);

        Button share = makeButton("CHIA SẺ VĂN BẢN");
        share.setOnClickListener(v -> shareText());
        LinearLayout.LayoutParams shareLp = matchWrap();
        shareLp.topMargin = dp(8);
        page.addView(share, shareLp);

        setContentView(page);
    }

    private Button makeButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(14);
        button.setAllCaps(false);
        return button;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, "Không tìm thấy ứng dụng camera.", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            File pictures = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
            if (pictures == null) pictures = getCacheDir();
            if (!pictures.exists()) pictures.mkdirs();
            File photo = File.createTempFile("AX_OCR_", ".jpg", pictures);
            cameraUri = FileProvider.getUriForFile(
                    this, getPackageName() + ".fileprovider", photo);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(intent, TAKE_PHOTO);
        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở camera: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        if (requestCode == PICK_IMAGE && data != null) {
            Uri uri = data.getData();
            if (uri != null) recognizeImage(uri);
        } else if (requestCode == TAKE_PHOTO && cameraUri != null) {
            recognizeImage(cameraUri);
        } else if (requestCode == SAVE_TEXT && data != null && data.getData() != null) {
            writeTextFile(data.getData());
        }
    }

    private void recognizeImage(Uri uri) {
        statusText.setText("Đang nhận dạng hình ảnh...");
        try {
            InputImage image = InputImage.fromFilePath(this, uri);
            recognizer.process(image)
                    .addOnSuccessListener(this, this::showRecognizedText)
                    .addOnFailureListener(this, e -> {
                        statusText.setText("Không nhận dạng được ảnh.");
                        Toast.makeText(this, "Lỗi OCR: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } catch (Exception e) {
            statusText.setText("Không đọc được hình ảnh.");
            Toast.makeText(this, "Lỗi mở ảnh: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showRecognizedText(Text result) {
        String text = result.getText();
        resultText.setText(text);
        if (text == null || text.trim().isEmpty()) {
            statusText.setText("Không tìm thấy chữ trong ảnh. Hãy thử ảnh rõ và đủ sáng hơn.");
        } else {
            statusText.setText("Nhận dạng hoàn tất. Bạn có thể sửa, sao chép hoặc lưu văn bản.");
        }
    }

    private void copyText() {
        String text = resultText.getText().toString();
        if (text.trim().isEmpty()) {
            Toast.makeText(this, "Chưa có văn bản để sao chép.", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("AX IMAGE TO TEXT", text));
        Toast.makeText(this, "Đã sao chép văn bản.", Toast.LENGTH_SHORT).show();
    }

    private void saveText() {
        String text = resultText.getText().toString();
        if (text.trim().isEmpty()) {
            Toast.makeText(this, "Chưa có văn bản để lưu.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, "AX_Image_To_Text.txt");
        startActivityForResult(intent, SAVE_TEXT);
    }

    private void writeTextFile(Uri uri) {
        try (OutputStream output = getContentResolver().openOutputStream(uri)) {
            if (output == null) throw new Exception("Không thể tạo file.");
            output.write(resultText.getText().toString().getBytes(StandardCharsets.UTF_8));
            Toast.makeText(this, "Đã lưu file TXT.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi lưu file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void shareText() {
        String text = resultText.getText().toString();
        if (text.trim().isEmpty()) {
            Toast.makeText(this, "Chưa có văn bản để chia sẻ.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(intent, "Chia sẻ văn bản"));
    }

    @Override
    protected void onDestroy() {
        if (recognizer != null) recognizer.close();
        super.onDestroy();
    }
}
