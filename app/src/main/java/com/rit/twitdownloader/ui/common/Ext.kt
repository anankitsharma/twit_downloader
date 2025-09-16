﻿package com.rit.twitdownloader.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.rit.twitdownloader.util.PreferenceUtil.getBoolean
import com.rit.twitdownloader.util.PreferenceUtil.getInt
import com.rit.twitdownloader.util.PreferenceUtil.getString

inline val String.booleanState
    @Composable get() = remember { mutableStateOf(this.getBoolean()) }

inline val String.stringState
    @Composable get() = remember { mutableStateOf(this.getString()) }

inline val String.intState
    @Composable get() = remember { mutableIntStateOf(this.getInt()) }

