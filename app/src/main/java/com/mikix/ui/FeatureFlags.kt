package com.mikix.ui

import androidx.datastore.preferences.core.booleanPreferencesKey

object FeatureFlags {
    val filament3dX = booleanPreferencesKey("feature_filament_3d_x")
    val cloudSync = booleanPreferencesKey("feature_cloud_sync")
    val lowPerformanceMode = booleanPreferencesKey("feature_low_performance_mode")
}
