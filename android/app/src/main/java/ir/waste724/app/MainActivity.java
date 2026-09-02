package ir.waste724.app;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.getcapacitor.BridgeWebViewClient;
import com.getcapacitor.BridgeActivity;

/**
 * این اکتیویتی، رفتار پیش‌فرض Capacitor را دست‌نخورده نگه می‌دارد و فقط یک
 * دکمه‌ی شناور «بازگشت» روی WebView اضافه می‌کند. با لمس این دکمه، کاربر از
 * سایت مشتری یا اپراتور به صفحه‌ی محلی انتخاب (index.html) برمی‌گردد.
 * دکمه در صفحه‌ی انتخاب و همچنین در کل سایت راننده (driver.waste724.ir)
 * به‌صورت خودکار مخفی می‌شود.
 */
public class MainActivity extends BridgeActivity {

    // آدرس صفحه‌ی محلی انتخاب مشتری/راننده (همان webDir پروژه‌ی Capacitor)
    private static final String HOME_URL = "https://localhost/index.html";

    // سایت راننده — دکمه‌ی بازگشت در این سایت نمایش داده نمی‌شود
    private static final String DRIVER_HOST = "driver.waste724.ir";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addFloatingBackButton();
    }

    /** آیا این URL مربوط به سایت راننده است؟ */
    private static boolean isDriverUrl(String url) {
        if (url == null) return false;
        try {
            String host = android.net.Uri.parse(url).getHost();
            return host != null && host.equalsIgnoreCase(DRIVER_HOST);
        } catch (Exception e) {
            return false;
        }
    }

    private void addFloatingBackButton() {
        final TextView button = new TextView(this);
        button.setText("\u2190 \u0628\u0627\u0632\u06AF\u0634\u062A"); // ← بازگشت
        button.setTextColor(Color.WHITE);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        button.setPadding(dp(16), dp(9), dp(16), dp(9));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#CC12341A")); // سبز تیره نیمه‌شفاف هماهنگ با برند
        bg.setCornerRadius(dp(24));
        bg.setStroke(dp(1), Color.parseColor("#33FFFFFF"));
        button.setBackground(bg);
        button.setElevation(dp(6));
        button.setVisibility(View.GONE); // ابتدا مخفی؛ چون اپ با صفحه‌ی انتخاب شروع می‌شود

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.setMargins(dp(16), dp(40), dp(16), 0);
        button.setLayoutParams(params);

        button.setOnClickListener(v -> {
            WebView webView = getBridge().getWebView();
            // به‌جای پرش مستقیم به صفحه‌ی انتخاب، ابتدا یک قدم در تاریخچه‌ی
            // همان سایت (مشتری/اپراتور) به عقب برمی‌گردیم؛ وقتی تاریخچه تمام
            // شد (یعنی به اولین صفحه‌ی آن سایت رسیدیم)، به صفحه‌ی انتخاب می‌رویم.
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                webView.loadUrl(HOME_URL);
            }
        });

        // زیرکلاس‌کردن BridgeWebViewClient (نه جایگزینی کامل آن) تا پل ارتباطی
        // پلاگین‌های Capacitor (ژئولوکیشن، نوتیفیکیشن و غیره) دست‌نخورده بماند.
        WebView webView = getBridge().getWebView();
        webView.setWebViewClient(new BridgeWebViewClient(getBridge()) {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                boolean onSelector = url != null
                        && (url.equals(HOME_URL)
                        || url.equals("https://localhost/")
                        || url.endsWith("/index.html"));
                // در سایت راننده و در صفحه‌ی انتخاب، دکمه مخفی می‌شود
                boolean showButton = !onSelector && !isDriverUrl(url);
                button.setVisibility(showButton ? View.VISIBLE : View.GONE);
            }
        });

        ((ViewGroup) findViewById(android.R.id.content)).addView(button);
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}
