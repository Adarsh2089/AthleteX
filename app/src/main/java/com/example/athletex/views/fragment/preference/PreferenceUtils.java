package com.example.athletex.views.fragment.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.camera.core.CameraSelector;

import com.google.common.base.Preconditions;
import com.example.athletex.R;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

/**
 * Utility class to retrieve shared preferences.
 */
public class PreferenceUtils {

    private static final int POSE_DETECTOR_PERFORMANCE_MODE_FAST = 1;
    private static final String PREF_NAME = "athletex_prefs";

    @Nullable
    public static android.util.Size getCameraXTargetResolution(Context context, int lensFacing) {
        Preconditions.checkArgument(
                lensFacing == CameraSelector.LENS_FACING_BACK || lensFacing == CameraSelector.LENS_FACING_FRONT);

        String prefKey = (lensFacing == CameraSelector.LENS_FACING_BACK)
                ? context.getString(R.string.pref_key_camerax_rear_camera_target_resolution)
                : context.getString(R.string.pref_key_camerax_front_camera_target_resolution);

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        try {
            return android.util.Size.parseSize(sharedPreferences.getString(prefKey, null));
        } catch (Exception e) {
            return null;
        }
    }

    public static PoseDetectorOptionsBase getPoseDetectorOptionsForLivePreview(Context context) {
        int performanceMode = getModeTypePreferenceValue(
                context, R.string.pref_key_live_preview_pose_detection_performance_mode, POSE_DETECTOR_PERFORMANCE_MODE_FAST);
        boolean preferGPU = preferGPUForPoseDetection(context);

        if (performanceMode == POSE_DETECTOR_PERFORMANCE_MODE_FAST) {
            PoseDetectorOptions.Builder builder = new PoseDetectorOptions.Builder()
                    .setDetectorMode(PoseDetectorOptions.STREAM_MODE);
            if (preferGPU) {
                builder.setPreferredHardwareConfigs(PoseDetectorOptions.CPU_GPU);
            }
            return builder.build();
        } else {
            AccuratePoseDetectorOptions.Builder builder = new AccuratePoseDetectorOptions.Builder()
                    .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE);
            if (preferGPU) {
                builder.setPreferredHardwareConfigs(AccuratePoseDetectorOptions.CPU_GPU);
            }
            return builder.build();
        }
    }

    public static boolean preferGPUForPoseDetection(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String prefKey = context.getString(R.string.pref_key_pose_detector_prefer_gpu);
        return sharedPreferences.getBoolean(prefKey, true);
    }

    public static boolean shouldShowPoseDetectionInFrameLikelihoodLivePreview(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String prefKey = context.getString(R.string.pref_key_live_preview_pose_detector_show_in_frame_likelihood);
        return sharedPreferences.getBoolean(prefKey, true);
    }

    public static boolean shouldPoseDetectionVisualizeZ(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String prefKey = context.getString(R.string.pref_key_pose_detector_visualize_z);
        return sharedPreferences.getBoolean(prefKey, true);
    }

    public static boolean shouldPoseDetectionRescaleZForVisualization(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String prefKey = context.getString(R.string.pref_key_pose_detector_rescale_z);
        return sharedPreferences.getBoolean(prefKey, true);
    }

    private static int getModeTypePreferenceValue(Context context, @StringRes int prefKeyResId, int defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String prefKey = context.getString(prefKeyResId);
        return Integer.parseInt(sharedPreferences.getString(prefKey, String.valueOf(defaultValue)));
    }

    public static boolean isCameraLiveViewportEnabled(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String prefKey = context.getString(R.string.pref_key_camera_live_viewport);
        return sharedPreferences.getBoolean(prefKey, false);
    }
}
