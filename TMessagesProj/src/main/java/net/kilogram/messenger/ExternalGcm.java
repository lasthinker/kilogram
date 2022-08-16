package net.kilogram.messenger;

import android.app.Activity;
import android.content.Context;

import org.telegram.messenger.FileLog;

public class ExternalGcm {

    interface Interface {
        boolean checkSplit(Context ctx);

        boolean checkPlayServices();

        void initPlayServices();

        void sendRegistrationToServer();

        void checkUpdate(Activity ctx);
    }

    static class NoImpl implements Interface {

        @Override
        public boolean checkSplit(Context ctx) {
            return false;
        }

        @Override
        public boolean checkPlayServices() {
            return false;
        }

        @Override
        public void initPlayServices() {
        }

        @Override
        public void sendRegistrationToServer() {
        }

        @Override
        public void checkUpdate(Activity ctx) {
        }

    }

    private static Interface impl;

    static {
        try {
            impl = (Interface) Class.forName("net.kilogram.messenger.GcmImpl").newInstance();
        } catch (ClassNotFoundException e) {
            impl = new NoImpl();
        } catch (Exception e) {
            impl = new NoImpl();
            FileLog.e(e);
        }
    }

    public static boolean checkSplit(Context ctx) {
        return impl.checkSplit(ctx);
    }

    public static void initPlayServices() {
        impl.initPlayServices();
    }

    public static boolean checkPlayServices() {
        return impl.checkPlayServices();
    }

    public static void sendRegistrationToServer() {
        impl.sendRegistrationToServer();
    }


    public static void checkUpdate(Activity ctx) {
        impl.checkUpdate(ctx);
    }

}
