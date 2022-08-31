# Kilogram

Kilogram is a third-party Telegram client based on [NekoX](https://github.com/NekoX-Dev/NekoX) with some modifications.

- Releases: <https://t.me/kilogram_messenger>
- Issues here: <https://github.com/lasthinker/kilogram/issues>

----

## API and Protocol Documentation

Telegram API manuals: <https://core.telegram.org/api>

MTproto protocol manuals: <https://core.telegram.org/mtproto>

## Compilation Guide

**NOTE: Building on Windows is, unfortunately, not supported.
Consider using a Linux VM or dual booting.**

**Important:**

1. Checkout all submodules

```
git submodule update --init --recursive
```

2. Install Android SDK and NDK (default location is $HOME/Android/SDK, otherwise you need to specify $ANDROID_HOME for it)

It is recommended to use [AndroidStudio](https://developer.android.com/studio) to install.

3. Install golang and yasm

```shell
apt install -y golang-1.16 yasm
```

4. Install Rust and its stdlib for Android ABIs, and add environment variables for it.

It is recommended to use the official script, otherwise you may not find rustup.

```shell
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- --default-toolchain none -y
echo "source \$HOME/.cargo/env" >> $HOME/.bashrc && source $HOME/.cargo/env

rustup install $(cat ss-rust/src/main/rust/shadowsocks-rust/rust-toolchain)
rustup default $(cat ss-rust/src/main/rust/shadowsocks-rust/rust-toolchain)
rustup target install armv7-linux-androideabi aarch64-linux-android i686-linux-android x86_64-linux-android
```

This step can be skipped if you want to build a `mini` release.

5. Build native dependencies: `./run init libs`
6. Build external libraries and native code:

For full release:

uncomment lines in settings.gradle  

`./run libs update`

For mini release:

```
./run libs v2ray
./run libs native # libtmessages.so
```

1. Fill out `TELEGRAM_APP_ID` and `TELEGRAM_APP_HASH` in `local.properties`
2. Replace TMessagesProj/google-services.json if you want fcm to work.
3. Replace release.keystore with yours and fill out `ALIAS_NAME`, `KEYSTORE_PASS` and `ALIAS_PASS` in `local.properties` if you want a custom sign key.

`./gradlew assemble<Full/Mini><Debug/Release/ReleaseNoGcm>`

----

## Localization

Kilogram is forked from Telegram, thus most locales follows the translations of Telegram for Android, checkout <https://translations.telegram.org/en/android/>.

Is Kilogram not in your language, or the translation is incorrect or incomplete? Get involved in the translations on our [Weblate](https://hosted.weblate.org/engage/kilogram/).

## Credits

<ul>
    <li>Telegram-FOSS: <a href="https://github.com/Telegram-FOSS-Team/Telegram-FOSS/blob/master/LICENSE">GPLv2</a></li>
    <li>NekoX: <a href="https://github.com/NekoX-Dev/NekoX/blob/master/LICENSE">GPLv3</a></li>
    <li>v2rayNG: <a href="https://github.com/2dust/v2rayNG/blob/master/LICENSE">GPLv3</a></li>
    <li>android-lib-v2ray-lite: <a href="https://github.com/lasthinker/android-lib-v2ray-lite/blob/master/LICENSE">LGPLv3</a></li>
    <li>shadowsocks-android: <a href="https://github.com/shadowsocks/shadowsocks-android/blob/master/LICENSE">GPLv3</a></li>
    <li>shadowsocksRb-android: <a href="https://github.com/shadowsocksRb/shadowsocksRb-android/blob/master/LICENSE">GPLv3</a></li>
    <li>HanLP: <a href="https://github.com/hankcs/HanLP/blob/1.x/LICENSE">Apache License 2.0</a></li>
    <li>OpenCC: <a href="https://github.com/BYVoid/OpenCC/blob/master/LICENSE">Apache License 2.0</a></li>
    <li>opencc-data: <a href="https://github.com/nk2028/opencc-data">Apache License 2.0</a></li>
    <li>android-device-list: <a href="https://github.com/pbakondy/android-device-list/blob/master/LICENSE">MIT</a> </li>
    <li>JetBrains: for allocating free open-source licences for IDEs</li>
</ul>

[<img src=".github/jetbrains-variant-3.png" width="200"/>](https://jb.gg/OpenSource)
