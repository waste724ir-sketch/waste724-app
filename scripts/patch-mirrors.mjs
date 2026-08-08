// این اسکریپت به‌صورت خودکار بعد از هر "npm install" اجرا می‌شود (به package.json نگاه کنید: "postinstall").
// چون دسترسی مستقیم به dl.google.com از بعضی شبکه‌ها (مثلاً ایران) ممکن است ناپایدار یا مسدود باشد،
// این اسکریپت یک آینه‌ی جایگزین (Aliyun) را به همه‌ی فایل‌های build.gradle که یک بلوک
// "repositories { ... }" مستقل دارند اضافه می‌کند — چه در ریشه‌ی پروژه اندروید، چه داخل
// پلاگین‌های Capacitor که در node_modules قرار دارند.
//
// اگر بعداً باز هم با خطای "Could not find ... Required by: project :something" مواجه شدید،
// همین اسکریپت را دوباره اجرا کنید: node scripts/patch-mirrors.mjs

import { readFileSync, writeFileSync, existsSync, readdirSync, statSync } from 'node:fs';
import { join } from 'node:path';

const MIRROR_LINE = "        maven { url 'https://maven.aliyun.com/repository/google' }";

function manualScan() {
  const found = [];
  const roots = ['android', 'node_modules/@capacitor', 'node_modules/@capacitor-community'];
  for (const root of roots) {
    if (!existsSync(root)) continue;
    walk(root, found, 0);
  }
  return found;
}

function walk(dir, found, depth) {
  if (depth > 4) return;
  let entries;
  try {
    entries = readdirSync(dir);
  } catch {
    return;
  }
  for (const entry of entries) {
    const full = join(dir, entry);
    let st;
    try {
      st = statSync(full);
    } catch {
      continue;
    }
    if (st.isDirectory()) {
      walk(full, found, depth + 1);
    } else if (entry === 'build.gradle') {
      found.push(full);
    }
  }
}

function patchFile(path) {
  let content = readFileSync(path, 'utf8');
  if (content.includes('maven.aliyun.com')) {
    return false; // already patched
  }
  const lines = content.split('\n');
  const out = [];
  let changed = false;
  for (const line of lines) {
    out.push(line);
    // بعد از هر خط "repositories {" آینه را اضافه می‌کنیم
    if (/^\s*repositories\s*\{\s*$/.test(line)) {
      out.push(MIRROR_LINE);
      changed = true;
    }
  }
  if (changed) {
    writeFileSync(path, out.join('\n'), 'utf8');
  }
  return changed;
}

const files = manualScan();
let patchedCount = 0;
for (const file of files) {
  try {
    if (patchFile(file)) {
      patchedCount++;
      console.log(`[patch-mirrors] patched: ${file}`);
    }
  } catch (err) {
    console.warn(`[patch-mirrors] could not patch ${file}: ${err.message}`);
  }
}
console.log(`[patch-mirrors] done. ${patchedCount} file(s) patched.`);
