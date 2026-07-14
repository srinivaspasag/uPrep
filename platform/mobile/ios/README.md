# UPrep iOS

Native iOS app that wraps the deployed UPrep web app (`https://65.2.108.70.sslip.io`)
in a full-screen `WKWebView`. Mirrors the Android wrapper: back/forward swipe
navigation, pull-to-refresh, native file uploads/downloads, and external links
open in Safari.

## Requirements

- macOS with **Xcode** (full app, not just Command Line Tools)
- The **iOS platform** installed in Xcode (Xcode ▸ Settings ▸ Components, or
  `xcodebuild -downloadPlatform iOS`)
- [XcodeGen](https://github.com/yonwoo9/XcodeGen) to generate the project:
  `brew install xcodegen`
- An **Apple Developer account** ($99/year) to ship to the App Store

## Project layout

```
mobile/ios/
  project.yml               # XcodeGen spec (source of truth)
  Sources/
    AppDelegate.swift        # window + root view controller
    WebViewController.swift   # WKWebView host
```

The `.xcodeproj` is generated and git-ignored. Regenerate any time with:

```bash
cd mobile/ios
xcodegen generate
open UPrep.xcodeproj
```

## Build & run

Open in Xcode and press Run, or from the CLI:

```bash
export DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer

# Build for the simulator
xcodebuild -project UPrep.xcodeproj -scheme UPrep \
  -sdk iphonesimulator -configuration Debug \
  -destination 'platform=iOS Simulator,name=iPhone 16' build
```

## Change the URL

Edit `baseURL` in `Sources/WebViewController.swift` (and the host check in the
same file) to point at your production domain.

## Installing on a real iPhone (no paid account)

Unlike Android, iOS will not install an unsigned `.ipa` directly — it must be
signed for your device. The easiest free way is to sideload with a tool that
re-signs using your own Apple ID on the phone:

`dist/UPrep-unsigned.ipa` is a ready device build. To install it:

1. Install **Sideloadly** (https://sideloadly.io) or **AltStore**
   (https://altstore.io) on a computer.
2. Connect your iPhone, open the tool, sign in with your (free) Apple ID.
3. Drag `dist/UPrep-unsigned.ipa` in and hit Start — it re-signs and installs.
4. On the iPhone: Settings ▸ General ▸ VPN & Device Management ▸ trust your
   Apple ID developer profile.

Notes: free-Apple-ID installs expire after **7 days** (just reinstall). The
bundle id may need to be unique to your Apple ID — change
`PRODUCT_BUNDLE_IDENTIFIER` in `project.yml` if Sideloadly reports a conflict.

Rebuild the unsigned `.ipa` any time:

```bash
export DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer
xcodebuild -project UPrep.xcodeproj -scheme UPrep -configuration Release \
  -sdk iphoneos -derivedDataPath build-device build \
  CODE_SIGNING_ALLOWED=NO CODE_SIGNING_REQUIRED=NO CODE_SIGN_IDENTITY=""
rm -rf Payload dist && mkdir -p Payload dist
cp -R build-device/Build/Products/Release-iphoneos/UPrep.app Payload/
zip -qr dist/UPrep-unsigned.ipa Payload && rm -rf Payload
```

## Shipping to the App Store

1. Set a unique bundle id in `project.yml` (`PRODUCT_BUNDLE_IDENTIFIER`).
2. Open the project in Xcode, select your Team under Signing & Capabilities.
3. Add an app icon (Assets catalog) — required by App Store.
4. Product ▸ Archive, then distribute via the Organizer to App Store Connect.
5. Note: Apple reviews plain WebView wrappers under the "minimum functionality"
   guideline (4.2). Native touches (offline handling, push, share sheet, etc.)
   help approval.
