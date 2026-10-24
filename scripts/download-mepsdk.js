#!/usr/bin/env node

/**
 * Downloads MEPSDK.xcframework from CDN
 * This script runs automatically after npm install
 *
 * Configuration is read from package.json -> moxoSdk.ios
 */

const https = require('https');
const http = require('http');
const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

// Read configuration from package.json
const packageJson = require('../package.json');
const sdkConfig = packageJson.moxoSdk?.ios || {};

const MEPSDK_VERSION = sdkConfig.version || '10.6.1';
const MEPSDK_DOWNLOAD_URL = (sdkConfig.downloadUrl || '').replace(
  '${version}',
  MEPSDK_VERSION
);

const FRAMEWORK_DIR = path.join(__dirname, '..', 'ios', 'Frameworks');
const FRAMEWORK_PATH = path.join(FRAMEWORK_DIR, 'MEPSDK.xcframework');
const VERSION_PATH = path.join(FRAMEWORK_DIR, '.version');
const ZIP_PATH = path.join(FRAMEWORK_DIR, 'MEPSDK.xcframework.zip');

function downloadFile(url, dest) {
  return new Promise((resolve, reject) => {
    const protocol = url.startsWith('https') ? https : http;
    const file = fs.createWriteStream(dest);

    console.log(`Downloading from: ${url}`);

    const request = protocol.get(url, (response) => {
      // Handle redirects
      if ([301, 302, 303, 307, 308].includes(response.statusCode)) {
        file.close();
        fs.unlinkSync(dest);
        return downloadFile(response.headers.location, dest)
          .then(resolve)
          .catch(reject);
      }

      if (response.statusCode !== 200) {
        file.close();
        fs.unlinkSync(dest);
        reject(new Error(`Failed to download: HTTP ${response.statusCode}`));
        return;
      }

      response.pipe(file);

      file.on('finish', () => {
        file.close();
        resolve();
      });
    });

    request.on('error', (err) => {
      fs.unlink(dest, () => {});
      reject(err);
    });

    file.on('error', (err) => {
      fs.unlink(dest, () => {});
      reject(err);
    });
  });
}

async function main() {
  // Validate configuration
  if (!MEPSDK_DOWNLOAD_URL) {
    console.error(
      '❌ Error: moxoSdk.ios.downloadUrl is not configured in package.json'
    );
    process.exit(1);
  }

  // Skip if the configured framework version already exists
  const installedVersion = fs.existsSync(VERSION_PATH)
    ? fs.readFileSync(VERSION_PATH, 'utf8').trim()
    : null;
  if (fs.existsSync(FRAMEWORK_PATH) && installedVersion === MEPSDK_VERSION) {
    console.log(
      `MEPSDK.xcframework ${MEPSDK_VERSION} already exists, skipping download.`
    );
    return;
  }

  console.log(`\n📦 Downloading MEPSDK ${MEPSDK_VERSION}...\n`);

  // Create Frameworks directory
  if (!fs.existsSync(FRAMEWORK_DIR)) {
    fs.mkdirSync(FRAMEWORK_DIR, { recursive: true });
  }

  try {
    // Download the zip file
    await downloadFile(MEPSDK_DOWNLOAD_URL, ZIP_PATH);
    console.log('Download complete. Extracting...');

    // Replace any previously installed version
    fs.rmSync(FRAMEWORK_PATH, { recursive: true, force: true });

    // Extract the zip file
    execSync(`unzip -o "${ZIP_PATH}" -d "${FRAMEWORK_DIR}"`, {
      stdio: 'inherit',
    });

    // Clean up zip file
    fs.unlinkSync(ZIP_PATH);
    fs.writeFileSync(VERSION_PATH, MEPSDK_VERSION);

    console.log('\n✅ MEPSDK.xcframework installed successfully!\n');
  } catch (error) {
    console.error('\n❌ Failed to download MEPSDK:', error.message);
    console.error(
      '\nPlease ensure the CDN URL is correct or manually download the framework.'
    );
    console.error(`Expected location: ${FRAMEWORK_PATH}\n`);
    process.exit(1);
  }
}

main();
