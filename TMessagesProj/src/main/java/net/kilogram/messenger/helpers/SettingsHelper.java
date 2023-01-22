package net.kilogram.messenger.helpers;

import android.net.Uri;
import android.text.TextUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.BaseFragment;

import net.kilogram.messenger.settings.BaseNekoSettingsActivity;
import net.kilogram.messenger.settings.BaseNekoXSettingsActivity;
import net.kilogram.messenger.settings.NekoChatSettingsActivity;
import net.kilogram.messenger.settings.NekoAccountSettingsActivity;
import net.kilogram.messenger.settings.NekoEmojiSettingsActivity;
import net.kilogram.messenger.settings.NekoExperimentalSettingsActivity;
import net.kilogram.messenger.settings.NekoGeneralSettingsActivity;
import net.kilogram.messenger.settings.NekoPasscodeSettingsActivity;
import net.kilogram.messenger.settings.NekoSettingsActivity;

public class SettingsHelper {

    public static void processDeepLink(Uri uri, Callback callback, Runnable unknown) {
        if (uri == null) {
            unknown.run();
            return;
        }
        var segments = uri.getPathSegments();
        if (segments.isEmpty() || segments.size() > 2 || !"nekosettings".equals(segments.get(0))) {
            unknown.run();
            return;
        }
        BaseFragment fragment;
        BaseNekoSettingsActivity neko_fragment = null;
        BaseNekoXSettingsActivity nekox_fragment = null;
        if (segments.size() == 1) {
            fragment = new NekoSettingsActivity();
        } else if (PasscodeHelper.getSettingsKey().equals(segments.get(1))) {
            fragment = neko_fragment = new NekoPasscodeSettingsActivity();
        } else {
            switch (segments.get(1)) {
                case "a":
                case "account":
                    fragment = nekox_fragment = new NekoAccountSettingsActivity();
                    break;
                case "chat":
                case "chats":
                case "c":
                    fragment = nekox_fragment = new NekoChatSettingsActivity();
                    break;
                case "experimental":
                case "e":
                    fragment = nekox_fragment = new NekoExperimentalSettingsActivity();
                    break;
                case "emoji":
                    fragment = neko_fragment = new NekoEmojiSettingsActivity();
                    break;
                case "general":
                case "g":
                    fragment = nekox_fragment = new NekoGeneralSettingsActivity();
                    break;
                default:
                    unknown.run();
                    return;
            }
        }
        callback.presentFragment(fragment);
        var row = uri.getQueryParameter("r");
        if (TextUtils.isEmpty(row)) {
            row = uri.getQueryParameter("row");
        }
        if (!TextUtils.isEmpty(row)) {
            var rowFinal = row;
            if (neko_fragment != null) {
                BaseNekoSettingsActivity finalNeko_fragment = neko_fragment;
                AndroidUtilities.runOnUIThread(() -> finalNeko_fragment.scrollToRow(rowFinal, unknown));
            } else if (nekox_fragment != null) {
                BaseNekoXSettingsActivity finalNekoX_fragment = nekox_fragment;
                AndroidUtilities.runOnUIThread(() -> finalNekoX_fragment.scrollToRow(rowFinal, unknown));
            }
        }
    }

    public interface Callback {
        void presentFragment(BaseFragment fragment);
    }
}
