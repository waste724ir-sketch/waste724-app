import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'ir.waste724.app',
  appName: 'Waste724',
  webDir: 'www',
  // برخلاف دو پروژه‌ی قبلی، این‌بار server.url تنظیم نمی‌شود چون اپ باید
  // ابتدا صفحه‌ی محلی انتخاب (www/index.html) را نشان دهد، نه مستقیم یک سایت را.
  // بعد از انتخاب کاربر، همان WebView با جاوااسکریپت به سایت مربوطه هدایت می‌شود.
  server: {
    cleartext: false,
    allowNavigation: [
      'recycle.waste724.ir',
      'driver.waste724.ir',
      '*.waste724.ir'
    ]
  },
  android: {
    allowMixedContent: false
  },
  ios: {
    contentInset: 'automatic'
  }
};

export default config;
