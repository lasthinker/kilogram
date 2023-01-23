/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.v2ray.ang.V2RayConfig;

import org.json.JSONArray;
import org.json.JSONObject;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.DownloadController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.CheckBox2;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberTextView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.URLSpanNoUnderline;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import kotlin.Unit;
import okhttp3.HttpUrl;
import net.kilogram.messenger.ui.BottomBuilder;
import net.kilogram.messenger.proxy.ShadowsocksRSettingsActivity;
import net.kilogram.messenger.proxy.ShadowsocksSettingsActivity;
import net.kilogram.messenger.proxy.SubSettingsActivity;
import net.kilogram.messenger.proxy.TrojanSettingsActivity;
import net.kilogram.messenger.proxy.VmessSettingsActivity;
import net.kilogram.messenger.proxy.WsSettingsActivity;
import net.kilogram.messenger.parts.ProxyChecksKt;
import net.kilogram.messenger.proxy.SubInfo;
import net.kilogram.messenger.proxy.SubManager;
import net.kilogram.messenger.utils.AlertUtil;
import net.kilogram.messenger.utils.FileUtil;
import net.kilogram.messenger.utils.ProxyUtil;
import net.kilogram.messenger.utils.UIUtil;
import net.kilogram.messenger.NekoConfig;

public class ProxyListActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {
    private static final int MENU_DELETE = 0;
    private static final int MENU_SHARE = 1;

    private ListAdapter listAdapter;
    private RecyclerListView listView;
    @SuppressWarnings("FieldCanBeLocal")
    private LinearLayoutManager layoutManager;

    private int currentConnectionState;

    private boolean useProxySettings;
    private boolean useProxyForCalls;

    private int rowCount;
    private int useProxyRow;
    private int enablePublicProxyRow;
    private int useProxyDetailRow;
    private int connectionsHeaderRow;
    private int proxyStartRow;
    private int proxyEndRow;
    private int proxyDetailRow;
    private int callsRow;
    private int callsDetailRow;
    private int deleteAllRow;

    private ItemTouchHelper itemTouchHelper;
    private NumberTextView selectedCountTextView;
    private ActionBarMenuItem shareMenuItem;
    private ActionBarMenuItem deleteMenuItem;

    private List<SharedConfig.ProxyInfo> selectedItems = new ArrayList<>();
    private List<SharedConfig.ProxyInfo> proxyList = new ArrayList<>();
    private boolean wasCheckedAllList;

    private ActionBarMenuItem otherItem;

    private List<SharedConfig.ProxyInfo> selectedItems = new ArrayList<>();
    private List<SharedConfig.ProxyInfo> proxyList = new ArrayList<>();
    private boolean wasCheckedAllList;

    public class TextDetailProxyCell extends FrameLayout {

        private TextView textView;
        private TextView valueTextView;
        private ImageView checkImageView;
        private SharedConfig.ProxyInfo currentInfo;
        private Drawable checkDrawable;

        private CheckBox2 checkBox;
        private boolean isSelected;
        private boolean isSelectionEnabled;

        private int color;
        private Pattern urlPattern;

        public TextDetailProxyCell(Context context) {
            super(context);

            textView = new TextView(context);
            textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            textView.setLines(1);
            textView.setMaxLines(1);
            textView.setSingleLine(true);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
            addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, (LocaleController.isRTL ? 56 : 21), 10, (LocaleController.isRTL ? 21 : 56), 0));

            valueTextView = new TextView(context);
            valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            valueTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            valueTextView.setLines(1);
            valueTextView.setMaxLines(1);
            valueTextView.setSingleLine(true);
            valueTextView.setCompoundDrawablePadding(AndroidUtilities.dp(6));
            valueTextView.setEllipsize(TextUtils.TruncateAt.END);
            valueTextView.setPadding(0, 0, 0, 0);
            addView(valueTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, (LocaleController.isRTL ? 56 : 21), 35, (LocaleController.isRTL ? 21 : 56), 0));

            checkImageView = new ImageView(context);
            checkImageView.setImageResource(R.drawable.msg_info);
            checkImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3), PorterDuff.Mode.MULTIPLY));
            checkImageView.setScaleType(ImageView.ScaleType.CENTER);
            checkImageView.setContentDescription(LocaleController.getString("Edit", R.string.Edit));
            addView(checkImageView, LayoutHelper.createFrame(48, 48, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.TOP, 8, 8, 8, 0));
            checkImageView.setOnClickListener(v -> presentFragment(new ProxySettingsActivity(currentInfo)));

            checkBox = new CheckBox2(context, 21);
            checkBox.setColor(Theme.key_radioBackground, Theme.key_radioBackground, Theme.key_checkboxCheck);
            checkBox.setDrawBackgroundAsArc(14);
            checkBox.setVisibility(GONE);
            addView(checkBox, LayoutHelper.createFrame(24, 24, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL, 16, 0, 8, 0));

            setWillNotDraw(false);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(64) + 1, MeasureSpec.EXACTLY));
        }

        @SuppressLint("SetTextI18n")
        public void setProxy(SharedConfig.ProxyInfo proxyInfo) {

            String title = proxyInfo.getTitle();

            SpannableStringBuilder stringBuilder = null;
            try {
                if (urlPattern == null) {
                    urlPattern = Pattern.compile("@[a-zA-Z\\d_]{1,32}");
                }
                Matcher matcher = urlPattern.matcher(title);
                while (matcher.find()) {
                    if (stringBuilder == null) {
                        stringBuilder = new SpannableStringBuilder(title);
                        textView.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());
                    }
                    int start = matcher.start();
                    int end = matcher.end();
                    if (title.charAt(start) != '@') {
                        start++;
                    }
                    URLSpanNoUnderline url = new URLSpanNoUnderline(title.subSequence(start + 1, end).toString()) {
                        @Override
                        public void onClick(View widget) {
                            MessagesController.getInstance(currentAccount).openByUserName(getURL(), ProxyListActivity.this, 1);
                        }
                    };
                    stringBuilder.setSpan(url, start, end, 0);
                }
            } catch (Exception e) {
                FileLog.e(e);
            }

            textView.setText(stringBuilder == null ? title : stringBuilder);
            currentInfo = proxyInfo;
        }

        public void updateStatus() {
            String colorKey;
            if (SharedConfig.currentProxy == currentInfo && useProxySettings) {
                if (currentConnectionState == ConnectionsManager.ConnectionStateConnected || currentConnectionState == ConnectionsManager.ConnectionStateUpdating) {
                    colorKey = Theme.key_windowBackgroundWhiteBlueText6;
                    if (currentInfo.ping != 0) {
                        valueTextView.setText(LocaleController.getString("Connected", R.string.Connected) + ", " + LocaleController.formatString("Ping", R.string.Ping, currentInfo.ping));
                    } else {
                        valueTextView.setText(LocaleController.getString("Connected", R.string.Connected));
                    }
                    if (!currentInfo.checking && !currentInfo.available) {
                        currentInfo.availableCheckTime = 0;
                    }
                } else {
                    colorKey = Theme.key_windowBackgroundWhiteGrayText2;
                    valueTextView.setText(LocaleController.getString("Connecting", R.string.Connecting));
                }
            } else {
                if (currentInfo.checking) {
                    valueTextView.setText(LocaleController.getString("Checking", R.string.Checking));
                    colorKey = Theme.key_windowBackgroundWhiteGrayText2;
                } else if (currentInfo.available) {
                    if (currentInfo.ping != 0) {
                        valueTextView.setText(LocaleController.getString("Available", R.string.Available) + ", " + LocaleController.formatString("Ping", R.string.Ping, currentInfo.ping));
                    } else {
                        valueTextView.setText(LocaleController.getString("Available", R.string.Available));
                    }
                    colorKey = Theme.key_windowBackgroundWhiteGreenText;
                } else {
                    valueTextView.setText(LocaleController.getString("Unavailable", R.string.Unavailable));
                    colorKey = Theme.key_windowBackgroundWhiteRedText4;
                }
            }
            color = Theme.getColor(colorKey);
            valueTextView.setTag(colorKey);
            valueTextView.setTextColor(color);
            if (checkDrawable != null) {
                checkDrawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
            }

        }

        public void setSelectionEnabled(boolean enabled, boolean animated) {
            if (isSelectionEnabled == enabled && animated) {
                return;
            }
            isSelectionEnabled = enabled;

            float fromX = 0, toX = LocaleController.isRTL ? -AndroidUtilities.dp(32) : AndroidUtilities.dp(32);
            if (!animated) {
                float x = enabled ? toX : fromX;
                textView.setTranslationX(x);
                valueTextView.setTranslationX(x);
                checkImageView.setTranslationX(x);
                checkBox.setTranslationX((LocaleController.isRTL ? AndroidUtilities.dp(32) : -AndroidUtilities.dp(32)) + x);
                checkImageView.setVisibility(enabled ? GONE : VISIBLE);
                checkImageView.setAlpha(1f);
                checkImageView.setScaleX(1f);
                checkImageView.setScaleY(1f);
                checkBox.setVisibility(enabled ? VISIBLE : GONE);
                checkBox.setAlpha(1f);
                checkBox.setScaleX(1f);
                checkBox.setScaleY(1f);
            } else {
                ValueAnimator animator = ValueAnimator.ofFloat(enabled ? 0 : 1, enabled ? 1 : 0).setDuration(200);
                animator.setInterpolator(CubicBezierInterpolator.DEFAULT);
                animator.addUpdateListener(animation -> {
                    float val = (float) animation.getAnimatedValue();
                    float x = AndroidUtilities.lerp(fromX, toX, val);
                    textView.setTranslationX(x);
                    valueTextView.setTranslationX(x);
                    checkImageView.setTranslationX(x);
                    checkBox.setTranslationX((LocaleController.isRTL ? AndroidUtilities.dp(32) : -AndroidUtilities.dp(32)) + x);

                    float scale = 0.5f + val * 0.5f;
                    checkBox.setScaleX(scale);
                    checkBox.setScaleY(scale);
                    checkBox.setAlpha(val);

                    scale = 0.5f + (1f - val) * 0.5f;
                    checkImageView.setScaleX(scale);
                    checkImageView.setScaleY(scale);
                    checkImageView.setAlpha(1f - val);
                });
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (enabled) {
                            checkBox.setAlpha(0f);
                            checkBox.setVisibility(VISIBLE);
                        } else {
                            checkImageView.setAlpha(0f);
                            checkImageView.setVisibility(VISIBLE);
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (enabled) {
                            checkImageView.setVisibility(GONE);
                        } else {
                            checkBox.setVisibility(GONE);
                        }
                    }
                });
                animator.start();
            }
        }

        public void setItemSelected(boolean selected, boolean animated) {
            if (selected == isSelected && animated) {
                return;
            }
            isSelected = selected;
            checkBox.setChecked(selected, animated);
        }

        public void setChecked(boolean checked) {
            if (checked) {
                if (checkDrawable == null) {
                    checkDrawable = getResources().getDrawable(R.drawable.proxy_check).mutate();
                }
                if (checkDrawable != null) {
                    checkDrawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
                }
                if (LocaleController.isRTL) {
                    valueTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, checkDrawable, null);
                } else {
                    valueTextView.setCompoundDrawablesWithIntrinsicBounds(checkDrawable, null, null, null);
                }
            } else {
                valueTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            }
        }

        public void setValue(CharSequence value) {
            valueTextView.setText(value);
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            updateStatus();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawLine(0, getMeasuredHeight() - 1, getMeasuredWidth(), getMeasuredHeight() - 1, Theme.dividerPaint);
        }
    }

    public ProxyListActivity() {
    }

    private String alert;

    public ProxyListActivity(String alert) {
        this.alert = alert;
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        currentConnectionState = ConnectionsManager.getInstance(currentAccount).getConnectionState();

        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.proxySettingsChanged);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.proxyCheckDone);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.didUpdateConnectionState);

        final SharedPreferences preferences = MessagesController.getGlobalMainSettings();
        useProxySettings = SharedConfig.proxyEnabled && !proxyList.isEmpty();
        useProxyForCalls = preferences.getBoolean("proxy_enabled_calls", false);

        updateRows(true);

        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.proxySettingsChanged);
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.proxyCheckDone);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.didUpdateConnectionState);

        if (currentCheck != null) currentCheck.shutdownNow();

    }

    private int menu_add = 1;
    private int menu_add_input_socks = 2;
    private int menu_add_input_telegram = 3;
    private int menu_add_input_vmess = 4;
    private int menu_add_input_ss = 7;
    private int menu_add_input_ssr = 8;
    private int menu_add_input_ws = 9;
    private int menu_add_input_rb = 17;

    private int menu_add_import_from_clipboard = 5;
    private int menu_add_scan_qr = 6;
    private int menu_other = 9;
    private int menu_retest_ping = 10;
    private int menu_reorder_by_ping = 11;
    private int menu_export_json = 12;
    private int menu_import_json = 13;
    private int menu_delete_all = 14;
    private int menu_delete_unavailable = 15;
    private int menu_sub = 16;

    public void processProxyList(ArrayList<String> files) {

        for (String proxyListFilePath : files) {

            File proxyListFile = new File(proxyListFilePath);

            processProxyListFile(getParentActivity(), proxyListFile);

        }

    }

    public static String processProxyListFile(Context ctx, File proxyListFile) {

        try {

            if (proxyListFile.length() > 2 * 1024 * 1024L) {

                throw new IllegalArgumentException("file too large.");

            }

            JSONObject proxyRootObject = new JSONObject(FileUtil.readUtf8String(proxyListFile));

            int version = proxyRootObject.optInt("nekox_proxy_list_version", 1);

            if (version == 1) {

                if (proxyRootObject.isNull("proxies")) {

                    throw new IllegalArgumentException("proxies array not found.");

                }

                JSONArray proxyArray = proxyRootObject.getJSONArray("proxies");

                if (proxyArray.length() == 0) {

                    throw new IllegalArgumentException("Empty proxy list.");

                }

                LinkedList<String> imported = new LinkedList<>();
                LinkedHashMap<String, String> errors = new LinkedHashMap<>();

                for (int index = 0; index < proxyArray.length(); index++) {

                    String proxyUrl = proxyArray.getString(index);

                    try {

                        imported.add(ProxyUtil.importInBackground(proxyUrl).getTitle());

                    } catch (Exception ex) {

                        errors.put(proxyUrl.length() < 15 ? proxyUrl : (proxyUrl.substring(0, 15) + "..."), ex.getMessage());

                    }

                }

                StringBuilder status = new StringBuilder();

                if (!imported.isEmpty()) {

                    status.append(LocaleController.getString("ImportedProxies", R.string.ImportedProxies));

                    for (String success : imported) {

                        status.append("\n").append(success);

                    }


                    if (!errors.isEmpty()) {

                        status.append("\n\n");

                    }

                }

                if (!errors.isEmpty()) {

                    status.append(LocaleController.getString("ErrorsInImport", R.string.ErrorsInImport));

                    for (Map.Entry<String, String> error : errors.entrySet()) {

                        status.append("\n").append(error.getKey()).append(": ").append(error.getValue());

                    }

                }

                if (imported.isEmpty()) {

                    AlertUtil.showSimpleAlert(ctx, status.toString());

                } else {

                    return status.toString();

                }

            } else {

                throw new IllegalArgumentException("invalid proxy list version " + version + ".");

            }

        } catch (Exception e) {

            AlertUtil.showSimpleAlert(ctx, LocaleController.getString("InvalidProxyFile", R.string.InvalidProxyFile) + proxyListFile.getPath() + "\n\n" + e.getMessage());

        }

        return null;

    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("ProxySettings", R.string.ProxySettings));
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setAllowOverlayTitle(false);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == menu_retest_ping) {
                    checkProxyList(true);
                } else if (id == menu_reorder_by_ping) {
                    SharedConfig.proxyList = new LinkedList<>(new TreeSet<>(SharedConfig.getProxyList()));
                    SharedConfig.saveProxyList();
                    updateRows(true);
                } else if (id == menu_export_json) {
                    File cacheFile = new File(ApplicationLoader.applicationContext.getExternalCacheDir(), "Proxy-List-" + new Date().toLocaleString() + ".nekox.json");

                    try {

                        JSONObject listRoot = new JSONObject();

                        listRoot.put("nekox_proxy_list_version", 1);

                        JSONArray proxyArray = new JSONArray();

                        for (SharedConfig.ProxyInfo info : SharedConfig.getProxyList()) {

                            if (info.subId <= 1) {

                                continue;

                            }

                            proxyArray.put(info.toUrl());

                        }

                        if (proxyArray.length() == 0) {
                            AlertUtil.showSimpleAlert(getParentActivity(), LocaleController.getString("NoProxy", R.string.NoProxy));
                            return;
                        }

                        listRoot.put("proxies", proxyArray);

                        FileUtil.writeUtf8String(listRoot.toString(4), cacheFile);
                    } catch (Exception e) {
                        return;
                    }
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("*/*");
                    if (Build.VERSION.SDK_INT >= 24) {
                        try {
                            intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(getParentActivity(), BuildConfig.APPLICATION_ID + ".provider", cacheFile));
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception ignore) {
                            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cacheFile));
                        }
                    } else {
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cacheFile));
                    }
                    getParentActivity().startActivityForResult(Intent.createChooser(intent, LocaleController.getString("ShareFile", R.string.ShareFile)), 500);
                } else if (id == menu_import_json) {
                    try {
                        if (Build.VERSION.SDK_INT >= 23 && getParentActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                            getParentActivity().requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 4);
                            return;
                        }
                    } catch (Throwable ignore) {
                    }
                    DocumentSelectActivity fragment = new DocumentSelectActivity(false);
                    fragment.setMaxSelectedFiles(-1);
                    fragment.setAllowPhoto(false);
                    fragment.setDelegate(new DocumentSelectActivity.DocumentSelectActivityDelegate() {

                        @Override
                        public void didSelectFiles(DocumentSelectActivity activity, ArrayList<String> files, String caption, boolean notify, int scheduleDate) {
                            activity.finishFragment();
                            processProxyList(files);
                        }

                        @Override
                        public void didSelectPhotos(ArrayList<SendMessagesHelper.SendingMediaInfo> photos, boolean notify, int scheduleDate) {
                        }

                        @Override
                        public void startDocumentSelectActivity() {
                        }
                    });
                    presentFragment(fragment);
                } else if (id == menu_delete_all) {
                    AlertUtil.showConfirm(getParentActivity(),
                            LocaleController.getString("DeleteAllServer", R.string.DeleteAllServer),
                            R.drawable.msg_delete, LocaleController.getString("Delete", R.string.Delete),
                            true, () -> {
                                SharedConfig.deleteAllProxy();
                                updateRows(true);
                            });
                } else if (id == menu_delete_unavailable) {
                    AlertUtil.showConfirm(getParentActivity(),
                            LocaleController.getString("DeleteUnavailableServer", R.string.DeleteUnavailableServer),
                            R.drawable.msg_delete, LocaleController.getString("Delete", R.string.Delete),
                            true, () -> {
                                deleteUnavailableProxy();
                            });
                } else if (id == menu_sub) {
                    showSubDialog();
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();

        ActionBarMenuItem addItem = menu.addItem(menu_add, R.drawable.add);

        addItem.addSubItem(menu_add_import_from_clipboard, LocaleController.getString("ImportProxyFromClipboard", R.string.ImportProxyFromClipboard)).setOnClickListener((v) -> {

            ProxyUtil.importFromClipboard(getParentActivity());

        });

        addItem.addSubItem(menu_add_scan_qr, LocaleController.getString("ScanQRCode", R.string.ScanQRCode)).setOnClickListener((v) -> {

            if (Build.VERSION.SDK_INT >= 23) {
                if (getParentActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    getParentActivity().requestPermissions(new String[]{Manifest.permission.CAMERA}, 22);
                    return;
                }
            }

            CameraScanActivity.showAsSheet(this, false, CameraScanActivity.TYPE_QR, new CameraScanActivity.CameraScanActivityDelegate() {

                @Override
                public void didFindQr(String text) {

                    try {
                        HttpUrl.parse(text);
                        Browser.openUrl(getParentActivity(), text);
                        return;
                    } catch (Exception ignored) {
                    }

                    AlertUtil.showCopyAlert(getParentActivity(), text);

                }

            });

        });

        addItem.addSubItem(menu_add_input_socks, LocaleController.getString("AddProxySocks5", R.string.AddProxySocks5)).setOnClickListener((v) -> presentFragment(new ProxySettingsActivity(0)));
        addItem.addSubItem(menu_add_input_telegram, LocaleController.getString("AddProxyTelegram", R.string.AddProxyTelegram)).setOnClickListener((v) -> presentFragment(new ProxySettingsActivity(1)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            addItem.addSubItem(menu_add_input_ws, LocaleController.getString("AddProxyWs", R.string.AddProxyWs)).setOnClickListener((v) -> presentFragment(new WsSettingsActivity()));
        }

        if (!BuildVars.isMini) {

            addItem.addSubItem(menu_add_input_vmess, LocaleController.getString("AddProxyVmess", R.string.AddProxyVmess)).setOnClickListener((v) -> presentFragment(new VmessSettingsActivity()));
            addItem.addSubItem(menu_add_input_vmess, LocaleController.getString("AddProxyVmess", R.string.AddProxyTrojan)).setOnClickListener((v) -> presentFragment(new TrojanSettingsActivity()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                addItem.addSubItem(menu_add_input_ss, LocaleController.getString("AddProxySS", R.string.AddProxySS)).setOnClickListener((v) -> presentFragment(new ShadowsocksSettingsActivity()));
                addItem.addSubItem(menu_add_input_ssr, LocaleController.getString("AddProxySSR", R.string.AddProxySSR)).setOnClickListener((v) -> presentFragment(new ShadowsocksRSettingsActivity()));
            }
            // addItem.addSubItem(menu_add_input_rb, LocaleController.getString("AddProxyRB", R.string.AddProxyRB)).setOnClickListener((v) -> presentFragment(new RelayBatonSettingsActivity()));

        }

        menu.addItem(menu_sub, R.drawable.msg_list);

        otherItem = menu.addItem(menu_other, R.drawable.ic_ab_other);
        otherItem.setContentDescription(LocaleController.getString("AccDescrMoreOptions", R.string.AccDescrMoreOptions));
        otherItem.addSubItem(menu_retest_ping, LocaleController.getString("RetestPing", R.string.RetestPing));
        otherItem.addSubItem(menu_reorder_by_ping, LocaleController.getString("ReorderByPing", R.string.ReorderByPing));
        otherItem.addSubItem(menu_delete_all, LocaleController.getString("DeleteAllServer", R.string.DeleteAllServer));
        otherItem.addSubItem(menu_delete_unavailable, LocaleController.getString("DeleteUnavailableServer", R.string.DeleteUnavailableServer));

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new RecyclerListView(context) {
            @Override
            protected void dispatchDraw(Canvas canvas) {
                drawSectionBackground(canvas, proxyStartRow, proxyEndRow, Theme.getColor(Theme.key_windowBackgroundWhite));
                super.dispatchDraw(canvas);
            }
        };
        ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        ((DefaultItemAnimator) listView.getItemAnimator()).setTranslationInterpolator(CubicBezierInterpolator.DEFAULT);
        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener((view, position) -> {
            if (position == useProxyRow) {
                if (SharedConfig.currentProxy == null) {
                    if (!proxyList.isEmpty()) {
                        SharedConfig.currentProxy = proxyList.get(0);

                        if (!useProxySettings) {
                            SharedPreferences preferences = MessagesController.getGlobalMainSettings();
                            SharedPreferences.Editor editor = MessagesController.getGlobalMainSettings().edit();
                            editor.putString("proxy_ip", SharedConfig.currentProxy.address);
                            editor.putString("proxy_pass", SharedConfig.currentProxy.password);
                            editor.putString("proxy_user", SharedConfig.currentProxy.username);
                            editor.putInt("proxy_port", SharedConfig.currentProxy.port);
                            editor.putString("proxy_secret", SharedConfig.currentProxy.secret);
                            editor.commit();
                        }
                    } else {
                        addProxy();
                        return;
                    }
                }

                useProxySettings = !useProxySettings;

                TextCheckCell textCheckCell = (TextCheckCell) view;
                textCheckCell.setChecked(useProxySettings);

                NotificationCenter.getGlobalInstance().removeObserver(ProxyListActivity.this, NotificationCenter.proxySettingsChanged);
                SharedConfig.setProxyEnable(useProxySettings);
                NotificationCenter.getGlobalInstance().addObserver(ProxyListActivity.this, NotificationCenter.proxySettingsChanged);

                updateRows(true);
            } else if (position == enablePublicProxyRow) {
                final boolean enabled = NekoConfig.enablePublicProxy.toggleConfigBool();
                TextCheckCell cell = (TextCheckCell) view;
                cell.setChecked(enabled);
                UIUtil.runOnIoDispatcher(() -> {
                    SharedPreferences pref = MessagesController.getGlobalMainSettings();
                    for (SubInfo subInfo : SubManager.getSubList().find()) {
                        if (subInfo.id != SubManager.publicProxySubID) continue;
                        subInfo.enable = enabled;
                        if (enabled) {
                            try {
                                subInfo.proxies = subInfo.reloadProxies();
                                subInfo.lastFetch = System.currentTimeMillis();
                            } catch (Exception ignored) {
                            }
                        }
                        SubManager.getSubList().update(subInfo, true);
                        break;
                    }
                    // clear proxy id
                    useProxySettings = false;
                    SharedConfig.setCurrentProxy(null);
                    // reload list & UI
                    AndroidUtilities.runOnUIThread(() -> {
                        SharedConfig.reloadProxyList();
                        updateRows(true);
                    });
                });

            } else if (position == callsRow) {
                useProxyForCalls = !useProxyForCalls;
                TextCheckCell textCheckCell = (TextCheckCell) view;
                textCheckCell.setChecked(useProxyForCalls);
                SharedPreferences.Editor editor = MessagesController.getGlobalMainSettings().edit();
                editor.putBoolean("proxy_enabled_calls", useProxyForCalls);
                editor.apply();
            } else if (position >= proxyStartRow && position < proxyEndRow) {
                if (!selectedItems.isEmpty()) {
                    listAdapter.toggleSelected(position);
                    return;
                }
                SharedConfig.ProxyInfo info = proxyList.get(position - proxyStartRow);
                useProxySettings = true;
                SharedConfig.setCurrentProxy(info);
                updateRows(true);
                RecyclerListView.Holder holder = (RecyclerListView.Holder) listView.findViewHolderForAdapterPosition(useProxyRow);
                if (holder != null) {
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    textCheckCell.setChecked(true);
                }
                ConnectionsManager.setProxySettings(useProxySettings, SharedConfig.currentProxy.address, SharedConfig.currentProxy.port, SharedConfig.currentProxy.username, SharedConfig.currentProxy.password, SharedConfig.currentProxy.secret);
            } else if (position == proxyAddRow) {
                presentFragment(new ProxySettingsActivity());
            } else if (position == deleteAllRow) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setMessage(LocaleController.getString(R.string.DeleteAllProxiesConfirm));
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                builder.setTitle(LocaleController.getString(R.string.DeleteProxyTitle));
                builder.setPositiveButton(LocaleController.getString(R.string.Delete), (dialog, which) -> {
                    for (SharedConfig.ProxyInfo info : proxyList) {
                        SharedConfig.deleteProxy(info);
                    }
                    useProxyForCalls = false;
                    useProxySettings = false;
                    NotificationCenter.getGlobalInstance().removeObserver(ProxyListActivity.this, NotificationCenter.proxySettingsChanged);
                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.proxySettingsChanged);
                    NotificationCenter.getGlobalInstance().addObserver(ProxyListActivity.this, NotificationCenter.proxySettingsChanged);
                    updateRows(true);
                    if (listAdapter != null) {
                        listAdapter.notifyItemChanged(useProxyRow, ListAdapter.PAYLOAD_CHECKED_CHANGED);
                        listAdapter.notifyItemChanged(callsRow, ListAdapter.PAYLOAD_CHECKED_CHANGED);
                        listAdapter.clearSelected();
                    }
                });
                AlertDialog dialog = builder.create();
                showDialog(dialog);
                TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                if (button != null) {
                    button.setTextColor(Theme.getColor(Theme.key_dialogTextRed2));
                }
            }
        });
        listView.setOnItemLongClickListener((view, position) -> {
            if (position >= proxyStartRow && position < proxyEndRow) {
                listAdapter.toggleSelected(position);
                return true;
            }
            return false;
        });

        ActionBarMenu actionMode = actionBar.createActionMode();
        selectedCountTextView = new NumberTextView(actionMode.getContext());
        selectedCountTextView.setTextSize(18);
        selectedCountTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        selectedCountTextView.setTextColor(Theme.getColor(Theme.key_actionBarActionModeDefaultIcon));
        actionMode.addView(selectedCountTextView, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f, 72, 0, 0, 0));
        selectedCountTextView.setOnTouchListener((v, event) -> true);

        shareMenuItem = actionMode.addItemWithWidth(MENU_SHARE, R.drawable.msg_share, AndroidUtilities.dp(54));
        deleteMenuItem = actionMode.addItemWithWidth(MENU_DELETE, R.drawable.msg_delete, AndroidUtilities.dp(54));

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                switch (id) {
                    case -1:
                        if (selectedItems.isEmpty()) {
                            finishFragment();
                        } else {
                            listAdapter.clearSelected();
                        }
                        break;
                    case MENU_DELETE:
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setMessage(LocaleController.getString(selectedItems.size() > 1 ? R.string.DeleteProxyMultiConfirm : R.string.DeleteProxyConfirm));
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        builder.setTitle(LocaleController.getString(R.string.DeleteProxyTitle));
                        builder.setPositiveButton(LocaleController.getString(R.string.Delete), (dialog, which) -> {
                            for (SharedConfig.ProxyInfo info : selectedItems) {
                                SharedConfig.deleteProxy(info);
                            }
                            if (SharedConfig.currentProxy == null) {
                                useProxyForCalls = false;
                                useProxySettings = false;
                            }
                            NotificationCenter.getGlobalInstance().removeObserver(ProxyListActivity.this, NotificationCenter.proxySettingsChanged);
                            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.proxySettingsChanged);
                            NotificationCenter.getGlobalInstance().addObserver(ProxyListActivity.this, NotificationCenter.proxySettingsChanged);
                            updateRows(true);
                            if (listAdapter != null) {
                                if (SharedConfig.currentProxy == null) {
                                    listAdapter.notifyItemChanged(useProxyRow, ListAdapter.PAYLOAD_CHECKED_CHANGED);
                                    listAdapter.notifyItemChanged(callsRow, ListAdapter.PAYLOAD_CHECKED_CHANGED);
                                }
                                listAdapter.clearSelected();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        showDialog(dialog);
                        TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                        if (button != null) {
                            button.setTextColor(Theme.getColor(Theme.key_dialogTextRed2));
                        }
                        break;
                    case MENU_SHARE:
                        StringBuilder links = new StringBuilder();
                        for (SharedConfig.ProxyInfo info : selectedItems) {
                            if (links.length() > 0) {
                                links.append("\n\n");
                            }
                            links.append(info.getLink());
                        }

                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, links.toString());
                        Intent chooserIntent = Intent.createChooser(shareIntent, LocaleController.getString(selectedItems.size() > 1 ? R.string.ShareLinks : R.string.ShareLink));
                        chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(chooserIntent);

                        if (listAdapter != null) {
                            listAdapter.clearSelected();
                        }
                        break;
                }
            }
        });

        return fragmentView;
    }

    @Override
    public boolean onBackPressed() {
        if (!selectedItems.isEmpty()) {
            listAdapter.clearSelected();
            return false;
        }
        return true;
    }

    @SuppressLint("NewApi")
    private void addProxy() {
        BottomBuilder builder = new BottomBuilder(getParentActivity());
        builder.addItems(new String[]{

    private void updateRows(boolean notify) {
        proxyList = SharedConfig.getProxyList();
        rowCount = 0;
        useProxyRow = rowCount++;
        useProxyDetailRow = rowCount++;
        connectionsHeaderRow = rowCount++;

        if (notify) {
            proxyList.clear();
            proxyList.addAll(SharedConfig.proxyList);

            boolean checking = false;
            if (!wasCheckedAllList) {
                for (SharedConfig.ProxyInfo info : proxyList) {
                    if (info.checking || info.availableCheckTime == 0) {
                        checking = true;
                        break;
                    }
                }
                if (!checking) {
                    wasCheckedAllList = true;
                }
            }

                CameraScanActivity.showAsSheet(this, false, CameraScanActivity.TYPE_QR, new CameraScanActivity.CameraScanActivityDelegate() {

                    @Override
                    public void didFindQr(String text) {

                        try {
                            HttpUrl.parse(text);
                            Browser.openUrl(getParentActivity(), text);
                            return;
                        } catch (Exception ignored) {
                        }

                        AlertUtil.showCopyAlert(getParentActivity(), text);

                    }

                });
            }
            return Unit.INSTANCE;
        });
        builder.show();
    }

    private void updateRows(boolean notify) {
        proxyList = SharedConfig.getProxyList();
        rowCount = 0;
        useProxyRow = rowCount++;

        if (notify) {
            proxyList.clear();
            proxyList.addAll(SharedConfig.proxyList);

            boolean checking = false;
            if (!wasCheckedAllList) {
                for (SharedConfig.ProxyInfo info : proxyList) {
                    if (info.checking || info.availableCheckTime == 0) {
                        checking = true;
                        break;
                    }
                }
                if (!checking) {
                    wasCheckedAllList = true;
                }
            }

            boolean isChecking = checking;
            Collections.sort(proxyList, (o1, o2) -> {
                long bias1 = SharedConfig.currentProxy == o1 ? -200000 : 0;
                if (!o1.available) {
                    bias1 += 100000;
                }
                long bias2 = SharedConfig.currentProxy == o2 ? -200000 : 0;
                if (!o2.available) {
                    bias2 += 100000;
                }
                return Long.compare(isChecking && o1 != SharedConfig.currentProxy ? SharedConfig.proxyList.indexOf(o1) * 10000L : o1.ping + bias1,
                        isChecking && o2 != SharedConfig.currentProxy ? SharedConfig.proxyList.indexOf(o2) * 10000L : o2.ping + bias2);
            });
        }

        enablePublicProxyRow = rowCount++;
        if (!proxyList.isEmpty()) {
            proxyStartRow = rowCount;
            rowCount += proxyList.size();
            proxyEndRow = rowCount;
        } else {
            useProxyDetailRow = -1;
            connectionsHeaderRow = -1;
            proxyStartRow = -1;
            proxyEndRow = -1;
        }
        proxyDetailRow = rowCount++;
        if (SharedConfig.currentProxy == null || SharedConfig.currentProxy.secret.isEmpty()) {
            boolean change = callsRow == -1;
            callsRow = rowCount++;
            callsDetailRow = rowCount++;
            UIUtil.runOnUIThread(() -> {
                if (!notify && change) {
                    listAdapter.notifyItemChanged(proxyDetailRow);
                    listAdapter.notifyItemRangeInserted(proxyDetailRow + 1, 2);
                }
            });
        } else {
            boolean change = callsRow != -1;
            callsRow = -1;
            callsDetailRow = -1;
            if (!notify && change) {
                UIUtil.runOnUIThread(() -> {
                    listAdapter.notifyItemChanged(proxyDetailRow);
                    listAdapter.notifyItemRangeRemoved(proxyDetailRow + 1, 2);
                });
            }
        }
        if (proxyList.size() >= 10) {
            deleteAllRow = rowCount++;
        } else {
            deleteAllRow = -1;
        }
        checkProxyList(false);
        if (notify && listAdapter != null) {
            UIUtil.runOnUIThread(() -> {
                try {
                    listView.clearAnimation();
                    listView.getRecycledViewPool().clear();
                    listAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    FileLog.e(e);
                }
            });
        }
    }

    private void checkProxyList() {
        for (int a = 0, count = proxyList.size(); a < count; a++) {
            final SharedConfig.ProxyInfo proxyInfo = proxyList.get(a);
            if (proxyInfo.checking || SystemClock.elapsedRealtime() - proxyInfo.availableCheckTime < 2 * 60 * 1000) {
                continue;
            }
            proxyInfo.proxyCheckPingId = ConnectionsManager.getInstance(currentAccount).checkProxy(proxyInfo.address, proxyInfo.port, proxyInfo.username, proxyInfo.password, proxyInfo.secret, time -> AndroidUtilities.runOnUIThread(() -> {
                if (time == -1) {
                    if (repeat > 0) {
                        checkSingleProxy(proxyInfo, repeat - 1, callback);
                    } else {
                        proxyInfo.availableCheckTime = SystemClock.elapsedRealtime();
                        proxyInfo.checking = false;
                        proxyInfo.available = false;
                        proxyInfo.ping = 0;
                        if (proxyInfo instanceof SharedConfig.ExternalSocks5Proxy && proxyInfo != SharedConfig.currentProxy) {
                            ((SharedConfig.ExternalSocks5Proxy) proxyInfo).stop();
                        }
                        if (callback != null) {
                            UIUtil.runOnUIThread(callback);
                        }
                    }
                } else {
                    proxyInfo.availableCheckTime = SystemClock.elapsedRealtime();
                    proxyInfo.checking = false;
                    proxyInfo.ping = time;
                    proxyInfo.available = true;
                    if (proxyInfo instanceof SharedConfig.ExternalSocks5Proxy && proxyInfo != SharedConfig.currentProxy) {
                        ((SharedConfig.ExternalSocks5Proxy) proxyInfo).stop();
                    }
                    if (callback != null) {
                        UIUtil.runOnUIThread(callback);
                    }
                }
            }));
        });
    }

    private void showSubDialog() {
        BottomBuilder builder = new BottomBuilder(getParentActivity());
        builder.addTitle(LocaleController.getString("ProxySubscription", R.string.ProxySubscription));
        HashMap<SubInfo, Boolean> toChange = new HashMap<>();
        for (SubInfo sub : SubManager.getSubList().find()) {
            TextCheckCell subItem = builder.addCheckItem(sub.name, sub.enable, true, (it, target) -> {
                if (target == sub.enable) {
                    toChange.remove(sub);
                } else {
                    toChange.put(sub, target);
                }
                return Unit.INSTANCE;
            });

            subItem.setOnLongClickListener((it) -> {
                if (sub.internal) return false;
                builder.dismiss();
                presentFragment(new SubSettingsActivity(sub));
                return true;
            });

        }

        builder.addButton(LocaleController.getString("Add", R.string.Add), false, true, (it) -> {
            presentFragment(new SubSettingsActivity());
            return Unit.INSTANCE;
        });

        String updateStr = LocaleController.getString("Update", R.string.Update);
        updateStr = updateStr.toLowerCase();
        updateStr = StrUtil.upperFirst(updateStr);

        builder.addButton(updateStr, (it) -> {
            AlertDialog pro = AlertUtil.showProgress(getParentActivity(), LocaleController.getString("SubscriptionUpdating", R.string.SubscriptionUpdating));
            AtomicBoolean canceled = new AtomicBoolean();
            pro.setOnCancelListener((__) -> {
                canceled.set(true);
            });
            pro.show();

            UIUtil.runOnIoDispatcher(() -> {
                for (SubInfo subInfo : SubManager.getSubList().find()) {
                    if (!subInfo.enable) continue;
                    try {
                        subInfo.proxies = subInfo.reloadProxies();
                        subInfo.lastFetch = System.currentTimeMillis();
                    } catch (IOException allTriesFailed) {
                        if (canceled.get()) return;
                        AlertUtil.showSimpleAlert(getParentActivity(), "All tries failed: " + allTriesFailed.toString().trim());
                        continue;
                    }
                    SubManager.getSubList().update(subInfo, true);
                    if (canceled.get()) return;
                }
                SharedConfig.reloadProxyList();
                updateRows(true);
                UIUtil.runOnUIThread(pro::dismiss);
            });
            return Unit.INSTANCE;
        });

        builder.addButton(LocaleController.getString("OK", R.string.OK), (it) -> {
            if (!toChange.isEmpty()) {
                AlertDialog pro = AlertUtil.showProgress(getParentActivity());
                pro.setCanCancel(false);
                pro.show();

                UIUtil.runOnIoDispatcher(() -> {
                    for (Map.Entry<SubInfo, Boolean> toChangeE : toChange.entrySet()) {
                        toChangeE.getKey().enable = toChangeE.getValue();
                        SubManager.getSubList().update(toChangeE.getKey(), true);
                    }
                    SharedConfig.reloadProxyList();
                    UIUtil.runOnUIThread(() -> updateRows(true));
                    ThreadUtil.sleep(233L);
                    UIUtil.runOnUIThread(pro::dismiss);
                });
            }
            return Unit.INSTANCE;
        });
        builder.show();
    }

    @Override
    protected void onDialogDismiss(Dialog dialog) {
        DownloadController.getInstance(currentAccount).checkAutodownloadSettings();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateRows(true);
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.proxySettingsChanged) {
            updateRows(true);
        } else if (id == NotificationCenter.didUpdateConnectionState) {
            int state = ConnectionsManager.getInstance(account).getConnectionState();
            if (currentConnectionState != state) {
                currentConnectionState = state;
                if (listView != null && SharedConfig.currentProxy != null) {
                    int idx = proxyList.indexOf(SharedConfig.currentProxy);
                    if (idx >= 0) {
                        RecyclerListView.Holder holder = (RecyclerListView.Holder) listView.findViewHolderForAdapterPosition(idx + proxyStartRow);
                        if (holder != null && holder.itemView instanceof TextDetailProxyCell) {
                            TextDetailProxyCell cell = (TextDetailProxyCell) holder.itemView;
                            cell.updateStatus();
                        }
                    }

                    if (currentConnectionState == ConnectionsManager.ConnectionStateConnected) {
                        updateRows(true);
                    }
                }
            }
        } else if (id == NotificationCenter.proxyCheckDone) {
            if (listView != null) {
                SharedConfig.ProxyInfo proxyInfo = (SharedConfig.ProxyInfo) args[0];
                int idx = proxyList.indexOf(proxyInfo);
                if (idx >= 0) {
                    RecyclerListView.Holder holder = (RecyclerListView.Holder) listView.findViewHolderForAdapterPosition(idx + proxyStartRow);
                    if (holder != null) {
                        TextDetailProxyCell cell = (TextDetailProxyCell) holder.itemView;
                        cell.updateStatus();
                    }
                }

                boolean checking = false;
                if (!wasCheckedAllList) {
                    for (SharedConfig.ProxyInfo info : proxyList) {
                        if (info.checking || info.availableCheckTime == 0) {
                            checking = true;
                            break;
                        }
                    }
                    if (!checking) {
                        wasCheckedAllList = true;
                    }
                }

                boolean checking = false;
                if (!wasCheckedAllList) {
                    for (SharedConfig.ProxyInfo info : proxyList) {
                        if (info.checking || info.availableCheckTime == 0) {
                            checking = true;
                            break;
                        }
                    }
                    if (!checking) {
                        wasCheckedAllList = true;
                    }
                }
                if (!checking) {
                    updateRows(true);
                }
            }
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        public static final int PAYLOAD_CHECKED_CHANGED = 0;
        public static final int PAYLOAD_SELECTION_CHANGED = 1;
        public static final int PAYLOAD_SELECTION_MODE_CHANGED = 2;

        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;

            setHasStableIds(true);
        }

        public void toggleSelected(int position) {
            if (position < proxyStartRow || position >= proxyEndRow) {
                return;
            }
            SharedConfig.ProxyInfo info = proxyList.get(position - proxyStartRow);
            if (selectedItems.contains(info)) {
                selectedItems.remove(info);
            } else {
                selectedItems.add(info);
            }
            notifyItemChanged(position, PAYLOAD_SELECTION_CHANGED);
            checkActionMode();
        }

        public void clearSelected() {
            selectedItems.clear();
            notifyItemRangeChanged(proxyStartRow, proxyEndRow - proxyStartRow, PAYLOAD_SELECTION_CHANGED);
            checkActionMode();
        }

        private void checkActionMode() {
            int selectedCount = selectedItems.size();
            boolean actionModeShowed = actionBar.isActionModeShowed();
            if (selectedCount > 0) {
                selectedCountTextView.setNumber(selectedCount, actionModeShowed);
                if (!actionModeShowed) {
                    actionBar.showActionMode();
                    notifyItemRangeChanged(proxyStartRow, proxyEndRow - proxyStartRow, PAYLOAD_SELECTION_MODE_CHANGED);
                }
            } else if (actionModeShowed) {
                actionBar.hideActionMode();
                notifyItemRangeChanged(proxyStartRow, proxyEndRow - proxyStartRow, PAYLOAD_SELECTION_MODE_CHANGED);
            }
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0: {
                    if (position == proxyDetailRow && callsRow == -1) {
                        holder.itemView.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                    } else {
                        holder.itemView.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    }
                    break;
                }
                case 1: {
                    TextSettingsCell textCell = (TextSettingsCell) holder.itemView;
                    textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if (position == proxyAddRow) {
                        textCell.setText(LocaleController.getString("AddProxy", R.string.AddProxy), deleteAllRow != -1);
                    } else if (position == deleteAllRow) {
                        textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText4));
                        textCell.setText(LocaleController.getString(R.string.DeleteAllProxies), false);
                    }
                    break;
                }
                case 2: {
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == connectionsHeaderRow) {
                        headerCell.setText(LocaleController.getString("ProxyConnections", R.string.ProxyConnections));
                    }
                    break;
                }
                case 3: {
                    TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                    if (position == useProxyRow) {
                        checkCell.setTextAndCheck(LocaleController.getString("UseProxySettings", R.string.UseProxySettings), useProxySettings, true);
                    } else if (position == callsRow) {
                        checkCell.setTextAndCheck(LocaleController.getString("UseProxyForCalls", R.string.UseProxyForCalls), useProxyForCalls, false);
                    } else if (position == enablePublicProxyRow) {
                        checkCell.setTextAndCheck(LocaleController.getString("enablePublicProxy", R.string.enablePublicProxy), NekoConfig.enablePublicProxy.Bool(), false);
                    }
                    break;
                }
                case 4: {
                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == callsDetailRow) {
                        cell.setText(LocaleController.getString("UseProxyForCallsInfo", R.string.UseProxyForCallsInfo));
                        cell.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                    }
                    break;
                }
                case 5: {
                    TextDetailProxyCell cell = (TextDetailProxyCell) holder.itemView;
                    SharedConfig.ProxyInfo info = proxyList.get(position - proxyStartRow);
                    cell.setProxy(info);
                    cell.setChecked(SharedConfig.currentProxy == info);
                    cell.setItemSelected(selectedItems.contains(proxyList.get(position - proxyStartRow)), false);
                    cell.setSelectionEnabled(!selectedItems.isEmpty(), false);
                    break;
                }
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List payloads) {
            if (holder.getItemViewType() == 5 && !payloads.isEmpty()) {
                TextDetailProxyCell cell = (TextDetailProxyCell) holder.itemView;
                if (payloads.contains(PAYLOAD_SELECTION_CHANGED)) {
                    cell.setItemSelected(selectedItems.contains(proxyList.get(position - proxyStartRow)), true);
                }
                if (payloads.contains(PAYLOAD_SELECTION_MODE_CHANGED)) {
                    cell.setSelectionEnabled(!selectedItems.isEmpty(), true);
                }
            } else if (holder.getItemViewType() == 3 && payloads.contains(PAYLOAD_CHECKED_CHANGED)) {
                TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                if (position == useProxyRow) {
                    checkCell.setChecked(useProxySettings);
                } else if (position == callsRow) {
                    checkCell.setChecked(useProxyForCalls);
                } else if (position == enablePublicProxyRow) {
                    checkCell.setChecked(NekoConfig.enablePublicProxy.Bool());
                }
            } else {
                super.onBindViewHolder(holder, position, payloads);
            }
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            int viewType = holder.getItemViewType();
            if (viewType == 3) {
                TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                int position = holder.getAdapterPosition();
                if (position == useProxyRow) {
                    checkCell.setChecked(useProxySettings);
                } else if (position == callsRow) {
                    checkCell.setChecked(useProxyForCalls);
                } else if (position == enablePublicProxyRow) {
                    checkCell.setChecked(NekoConfig.enablePublicProxy.Bool());
                }
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int position = holder.getAdapterPosition();
            return position == useProxyRow || position == callsRow || position == enablePublicProxyRow || position == deleteAllRow || position >= proxyStartRow && position < proxyEndRow;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    view = new ShadowSectionCell(mContext);
                    break;
                case 1:
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 2:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 4:
                    view = new TextInfoPrivacyCell(mContext);
                    view.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case 5:
                default:
                    view = new TextDetailProxyCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public long getItemId(int position) {
            // Random stable ids, could be anything non-repeating
            if (position == useProxyDetailRow) {
                return -1;
            } else if (position == proxyDetailRow) {
                return -2;
//            } else if (position == proxyAddRow) {
//                return -3;
            } else if (position == useProxyRow) {
                return -4;
            } else if (position == callsRow) {
                return -5;
            } else if (position == connectionsHeaderRow) {
                return -6;
            } else if (position == deleteAllRow) {
                return -8;
            } else if (position >= proxyStartRow && position < proxyEndRow) {
                return proxyList.get(position - proxyStartRow).hashCode();
            } else {
                return -7;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == useProxyDetailRow || position == proxyDetailRow) {
                return 0;
            } else if (position == proxyAddRow || position == deleteAllRow) {
                return 1;
            } else if (position == useProxyRow || position == callsRow) {
                return 3;
            } else if (position == connectionsHeaderRow) {
                return 2;
            } else if (position >= proxyStartRow && position < proxyEndRow) {
                return 5;
            } else {
                return 4;
            }
        }

    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{TextSettingsCell.class, TextCheckCell.class, HeaderCell.class, TextDetailProxyCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray));

        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextDetailProxyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG | ThemeDescription.FLAG_IMAGECOLOR, new Class[]{TextDetailProxyCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueText6));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG | ThemeDescription.FLAG_IMAGECOLOR, new Class[]{TextDetailProxyCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG | ThemeDescription.FLAG_IMAGECOLOR, new Class[]{TextDetailProxyCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGreenText));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG | ThemeDescription.FLAG_IMAGECOLOR, new Class[]{TextDetailProxyCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteRedText4));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{TextDetailProxyCell.class}, new String[]{"checkImageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText3));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4));

        return themeDescriptions;
    }
}
