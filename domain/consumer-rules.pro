-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Preserve some attributes that may be required for reflection.
-keepattributes AnnotationDefault,
                EnclosingMethod,
                InnerClasses,
                RuntimeVisibleAnnotations,
                RuntimeVisibleParameterAnnotations,
                RuntimeVisibleTypeAnnotations,
                Signature

-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService
-keep public class com.google.android.vending.licensing.ILicensingService
-dontnote com.android.vending.licensing.ILicensingService
-dontnote com.google.vending.licensing.ILicensingService
-dontnote com.google.android.vending.licensing.ILicensingService

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# Keep setters in Views so that animations can still work.
-keepclassmembers public class * extends android.view.View {
    void set*(***);
    *** get*();
}

# We want to keep methods in Activity that could be used in the XML attribute onClick.
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Preserve annotated Javascript interface methods.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# The support libraries contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version. We know about them, and they are safe.
-dontnote android.support.**
-dontnote androidx.**
-dontwarn android.support.**
-dontwarn androidx.**

# This class is deprecated, but remains for backward compatibility.
-dontwarn android.util.FloatMath

# Understand the @Keep support annotation.
-keep class android.support.annotation.Keep
-keep class androidx.annotation.Keep

-keep @android.support.annotation.Keep class * {*;}
-keep @androidx.annotation.Keep class * {*;}

-keepclasseswithmembers class * {
    @android.support.annotation.Keep <methods>;
}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}

-keepclasseswithmembers class * {
    @android.support.annotation.Keep <fields>;
}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}

-keepclasseswithmembers class * {
    @android.support.annotation.Keep <init>(...);
}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}

# These classes are duplicated between android.jar and org.apache.http.legacy.jar.
-dontnote org.apache.http.**
-dontnote android.net.http.**

# These classes are duplicated between android.jar and core-lambda-stubs.jar.
-dontnote java.lang.invoke.**

# End of content from C:\Users\qq277\Desktop\LinkU-Android\domain\build\intermediates\default_proguard_files\global\proguard-android-optimize.txt-7.3.1
# The proguard configuration file for the following section is C:\Users\qq277\Desktop\LinkU-Android\domain\proguard-rules.pro
# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep `Companion` object fields of serializable classes.
# This avoids serializer lookup through `getDeclaredClasses` as done for named companion objects.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep `serializer()` on companion objects (both default and named) of serializable classes.
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `INSTANCE.serializer()` of serializable objects.
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# @Serializable and @Polymorphic are used at runtime for polymorphic serialization.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# Serializer for classes with named companion objects are retrieved using `getDeclaredClasses`.
# If you have any, uncomment and replace classes with those containing named companion objects.
#-keepattributes InnerClasses # Needed for `getDeclaredClasses`.
#-if @kotlinx.serialization.Serializable class
#com.example.myapplication.HasNamedCompanion, # <-- List serializable classes with named companions.
#com.example.myapplication.HasNamedCompanion2
#{
#    static **$* *;
#}
#-keepnames class <1>$$serializer { # -keepnames suffices; class is kept when serializer() is kept.
#    static <1>$$serializer INSTANCE;
#}

# End of content from C:\Users\qq277\Desktop\LinkU-Android\domain\proguard-rules.pro
# The proguard configuration file for the following section is C:\Users\qq277\Desktop\LinkU-Android\domain\build\intermediates\aapt_proguard_file\release\aapt_rules.txt
# Generated by the gradle plugin

# End of content from C:\Users\qq277\Desktop\LinkU-Android\domain\build\intermediates\aapt_proguard_file\release\aapt_rules.txt
# The proguard configuration file for the following section is C:\Users\qq277\Desktop\LinkU-Android\core\build\intermediates\consumer_proguard_dir\release\lib0\proguard.txt

# End of content from C:\Users\qq277\Desktop\LinkU-Android\core\build\intermediates\consumer_proguard_dir\release\lib0\proguard.txt
# The proguard configuration file for the following section is C:\Users\qq277\.gradle\caches\transforms-3\859cfcfd8924fa40c6f7b2176fdddcd5\transformed\appcompat-1.7.0-alpha01\proguard.txt
# Copyright (C) 2018 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# aapt is not able to read app::actionViewClass and app:actionProviderClass to produce proguard
# keep rules. Add a commonly used SearchView to the keep list until b/109831488 is resolved.
-keep class androidx.appcompat.widget.SearchView { <init>(...); }

# Never inline methods, but allow shrinking and obfuscation.
-keepclassmembernames,allowobfuscation,allowshrinking class androidx.appcompat.widget.AppCompatTextViewAutoSizeHelper$Impl* {
  <methods>;
}

# End of content from C:\Users\qq277\.gradle\caches\transforms-3\859cfcfd8924fa40c6f7b2176fdddcd5\transformed\appcompat-1.7.0-alpha01\proguard.txt
# The proguard configuration file for the following section is C:\Users\qq277\.gradle\caches\transforms-3\32c22f6f65371940da7e6dc761f5db0b\transformed\jetified-hilt-work-1.0.0\proguard.txt
# Keep class names of Hilt injected Workers since their name are used as a multibinding map key.
-keepnames @androidx.hilt.work.HiltWorker class * extends androidx.work.ListenableWorker
# End of content from C:\Users\qq277\.gradle\caches\transforms-3\32c22f6f65371940da7e6dc761f5db0b\transformed\jetified-hilt-work-1.0.0\proguard.txt
# The proguard configuration file for the following section is C:\Users\qq277\.gradle\caches\transforms-3\365702d36d0dbe7e9de80fe6baf67f2b\transformed\jetified-hilt-android-2.44\proguard.txt
# Keep for the reflective cast done in EntryPoints.
# See b/183070411#comment4 for more info.
-keep,allowobfuscation,allowshrinking @dagger.hilt.EntryPoint class *# Keep for the reflective cast done in EntryPoints.
# See b/183070411#comment4 for more info.
-keep,allowobfuscation,allowshrinking @dagger.hilt.android.EarlyEntryPoint class *# Keep class names of Hilt injected ViewModels since their name are used as a multibinding map key.
-keepnames @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel# Keep for the reflective cast done in EntryPoints.
# See b/183070411#comment4 for more info.
-keep,allowobfuscation,allowshrinking @dagger.hilt.internal.ComponentEntryPoint class *
-keep,allowobfuscation,allowshrinking @dagger.hilt.internal.GeneratedEntryPoint class *
# End of content from C:\Users\qq277\.gradle\caches\transforms-3\365702d36d0dbe7e9de80fe6baf67f2b\transformed\jetified-hilt-android-2.44\proguard.txt
# The proguard configuration file for the following section is C:\Users\qq277\.gradle\caches\transforms-3\0b6a223406c4b108e1dae81870095688\transformed\fragment-1.5.1\proguard.txt
# Copyright (C) 2020 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# The default FragmentFactory creates Fragment instances using reflection
-if public class ** extends androidx.fragment.app.Fragment
-keepclasseswithmembers,allowobfuscation public class <1> {
    public <init>();
}

# End of content from C:\Users\qq277\.gradle\caches\transforms-3\0b6a223406c4b108e1dae81870095688\transformed\fragment-1.5.1\proguard.txt
# The proguard configuration file for the following section is C:\Users\qq277\.gradle\caches\transforms-3\082dae3f5643dc8595e100549736ee79\transformed\jetified-ui-1.0.5\proguard.txt
# Copyright (C) 2020 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# We supply these as stubs and are able to link to them at runtime
# because they are hidden public classes in Android. We don't want
# R8 to complain about them not being there during optimization.
-dontwarn android.view.RenderNode
-dontwarn android.view.DisplayListCanvas

-keepclassmembers class androidx.compose.ui.platform.ViewLayerContainer {
    protected void dispatchGetDisplayList();
}

-keepclassmembers class androidx.compose.ui.platform.AndroidComposeView {
    android.view.View findViewByAccessibilityIdTraversal(int);
}

# End of content from C:\Users\qq277\.gradle\caches\transforms-3\082dae3f5643dc8595e100549736ee79\transformed\jetified-ui-1.0.5\proguard.txt
# The proguard configuration file for the following section is C:\Users\qq277\.gradle\caches\transforms-3\bfe5203d09f97823d4d85e9df7261131\transformed\navigation-common-2.4.0\proguard.txt
# Copyright (C) 2019 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# NavArgsLazy creates NavArgs instances using reflection
-if public class ** implements androidx.navigation.NavArgs
-keepclassmembers public class <1> {
    ** fromBundle(android.os.Bundle);
}

# Retain the @Navigator.Name annotation on each subclass of Navigator.
# R8 full mode only retains annotations on items matched by a -keep rule,
# hence the extra -keep rule for the subclasses of Navigator.
#
# A -keep rule for the Navigator.Name annotation class is not required
# since the annotation is referenced from the code.
-keepattributes RuntimeVisibleAnnotations
-keep,allowobfuscation,allowshrinking class * extends androidx.navigation.Navigator

# End of content from C:\Users\qq277\.gradle\caches\transforms-3\bfe5203d09f97823d4d85e9df7261131\transformed\navigation-common-2.4.0\proguard.txt
# The proguard configuration file for the following section is C:\Users\qq277\.gradle\caches\transforms-3\74160bc7ab3b1542417b8f25d56f5f50\transformed\jetified-savedstate-1.2.0\proguard.txt
# Copyright (C) 2019 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

-keepclassmembers,allowobfuscation class * implements androidx.savedstate.SavedStateRegistry$AutoRecreated {
    <init>();
}

# End of content from C:\Users\qq277\.gradle\caches\transforms-3\74160bc7ab3b1542417b8f25d56f5f50\transformed\jetified-savedstate-1.2.0\proguard.txt
# The proguard configuration file for the following section is C:\Users\qq277\.gradle\caches\transforms-3\b54d799280061fe452475429cf680bfc\transformed\work-runtime-2.3.4\proguard.txt
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.InputMerger
# Keep all constructors on ListenableWorker, Worker (also marked with @Keep)
-keep public class * extends androidx.work.ListenableWorker {
    public <init>(...);
}
# We need to keep WorkerParameters for the ListenableWorker constructor
-keep class androidx.work.WorkerParameters

# End of content from C:\Users\qq277\.gradle\caches\transforms-3\b54d799280061fe452475429cf680bfc\transformed\work-runtime-2.3.4\proguard.txt
# The proguard configuration file for the following section is C:\Users\qq277\.gradle\caches\transforms-3\b6601975f60d5ab0a798738c13b7bf3a\transformed\lifecycle-viewmodel-2.6.0-alpha03\proguard.txt
-keepclassmembers,allowobfuscation class * extends androidx.lifecycle.ViewModel {
    <init>();
}

-keepclassmembers,allowobfuscation class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}

# End of content from C:\Users\qq277\.gradle\caches\transforms-3\b6601975f60d5ab0a798738c13b7bf3a\transformed\lifecycle-viewmodel-2.6.0-alpha03\proguard.txt
# The proguard configuration file for the following section is C:\Users\qq277\.gradle\caches\transforms-3\036db6a877206eade96845585dc4ec18\transformed\jetified-lifecycle-viewmodel-savedstate-2.6.0-alpha03\proguard.txt
-keepclassmembers,allowobfuscation class * extends androidx.lifecycle.ViewModel {
    <init>(androidx.lifecycle.SavedStateHandle);
}

-keepclassmembers,allowobfuscation class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application,androidx.lifecycle.SavedStateHandle);
}

# End of content from C:\Users\qq277\.gradle\caches\transforms-3\036db6a877206eade96845585dc4ec18\transformed\jetified-lifecycle-viewmodel-savedstate-2.6.0-alpha03\proguard.txt
# The proguard configuration file for the following section is C:\Users\qq277\.gradle\caches\transforms-3\75bed3bd7417333ee104a2b63ab75fc4\transformed\recyclerview-1.2.1\proguard.txt
# Copyright (C) 2015 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# When layoutManager xml attribute is used, RecyclerView inflates
#LayoutManagers' constructors using reflection.
-keep public class * extends androidx.recyclerview.widget.RecyclerView$LayoutManager {
    public <init>(android.content.Context, android.util.AttributeSet, int, int);
    public <init>();
}

-keepclassmembers class androidx.recyclerview.widget.RecyclerView {
    public void suppressLayout(boolean);
    public boolean isLayoutSuppressed();
}
# End of content from C:\Users\qq277\.gradle\caches\transforms-3\75bed3bd7417333ee104a2b63ab75fc4\transformed\recyclerview-1.2.1\proguard.txt
# The proguard configuration file for the following section is C:\Users\qq277\.gradle\caches\transforms-3\970d8d012bc1ba5c04333b1067ad83ae\transformed\vectordrawable-animated-1.1.0\proguard.txt
# Copyright (C) 2016 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# keep setters in VectorDrawables so that animations can still work.
-keepclassmembers class androidx.vectordrawable.graphics.drawable.VectorDrawableCompat$* {
   void set*(***);
   *** get*();
}

# End of content from C:\Users\qq277\.gradle\caches\transforms-3\970d8d012bc1ba5c04333b1067ad83ae\transformed\vectordrawable-animated-1.1.0\proguard.txt
# The proguard configuration file for the following section is C:\Users\qq277\.gradle\caches\transforms-3\b47cf44745337fc26d6dd050fbb61613\transformed\core-1.9.0\proguard.txt
# Never inline methods, but allow shrinking and obfuscation.
-keepclassmembernames,allowobfuscation,allowshrinking class androidx.core.view.ViewCompat$Api* {
  <methods>;
}
-keepclassmembernames,allowobfuscation,allowshrinking class androidx.core.view.WindowInsetsCompat$*Impl* {
  <methods>;
}
-keepclassmembernames,allowobfuscation,allowshrinking class androidx.core.app.NotificationCompat$*$Api*Impl {
  <methods>;
}
-keepclassmembernames,allowobfuscation,allowshrinking class androidx.core.os.UserHandleCompat$Api*Impl {
  <methods>;
}
-keepclassmembernames,allowobfuscation,allowshrinking class androidx.core.widget.EdgeEffectCompat$Api*Impl {
  <methods>;
}

# End of content from C:\Users\qq277\.gradle\caches\transforms-3\b47cf44745337fc26d6dd050fbb61613\transformed\core-1.9.0\proguard.txt
# The proguard configuration file for the following section is C:\Users\qq277\.gradle\caches\transforms-3\9cc6fb37c6ae814d0d60b7038872502e\transformed\jetified-lifecycle-process-2.4.1\proguard.txt
# this rule is need to work properly when app is compiled with api 28, see b/142778206
-keepclassmembers class * extends androidx.lifecycle.EmptyActivityLifecycleCallbacks { *; }
# End of content from C:\Users\qq277\.gradle\caches\transforms-3\9cc6fb37c6ae814d0d60b7038872502e\transformed\jetified-lifecycle-process-2.4.1\proguard.txt
# The proguard configuration file for the following section is C:\Users\qq277\.gradle\caches\transforms-3\ac2b2b847402abb457f206760f350a32\transformed\lifecycle-runtime-2.6.0-alpha03\proguard.txt
-keepattributes AnnotationDefault,
                RuntimeVisibleAnnotations,
                RuntimeVisibleParameterAnnotations,
                RuntimeVisibleTypeAnnotations

-keepclassmembers enum androidx.lifecycle.Lifecycle$Event {
    <fields>;
}

-keep !interface * implements androidx.lifecycle.LifecycleObserver {
}

-keep class * implements androidx.lifecycle.GeneratedAdapter {
    <init>(...);
}

-keepclassmembers class ** {
    @androidx.lifecycle.OnLifecycleEvent *;
}

# this rule is need to work properly when app is compiled with api 28, see b/142778206
# Also this rule prevents registerIn from being inlined.
-keepclassmembers class androidx.lifecycle.ReportFragment$LifecycleCallbacks { *; }
# End of content from C:\Users\qq277\.gradle\caches\transforms-3\ac2b2b847402abb457f206760f350a32\transformed\lifecycle-runtime-2.6.0-alpha03\proguard.txt
# The proguard configuration file for the following section is C:\Users\qq277\.gradle\caches\transforms-3\6793377c16af8a3aee27f90feb8aa7ad\transformed\jetified-runtime-1.0.5\proguard.txt
-assumenosideeffects public class androidx.compose.runtime.ComposerKt {
    void sourceInformation(androidx.compose.runtime.Composer,java.lang.String);
    void sourceInformationMarkerStart(androidx.compose.runtime.Composer,int,java.lang.String);
    void sourceInformationMarkerEnd(androidx.compose.runtime.Composer);
}

# End of content from C:\Users\qq277\.gradle\caches\transforms-3\6793377c16af8a3aee27f90feb8aa7ad\transformed\jetified-runtime-1.0.5\proguard.txt
# The proguard configuration file for the following section is C:\Users\qq277\.gradle\caches\transforms-3\75b03f3250728bc310df6ebb16f3192a\transformed\rules\lib\META-INF\com.android.tools\r8\coroutines.pro
# When editing this file, update the following files as well:
# - META-INF/proguard/coroutines.pro
# - META-INF/com.android.tools/proguard/coroutines.pro

# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Same story for the standard library's SafeContinuation that also uses AtomicReferenceFieldUpdater
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}

# These classes are only required by kotlinx.coroutines.debug.AgentPremain, which is only loaded when
# kotlinx-coroutines-core is used as a Java agent, so these are not needed in contexts where ProGuard is used.
-dontwarn java.lang.instrument.ClassFileTransformer
-dontwarn sun.misc.SignalHandler
-dontwarn java.lang.instrument.Instrumentation
-dontwarn sun.misc.Signal

# Only used in `kotlinx.coroutines.internal.ExceptionsConstructor`.
# The case when it is not available is hidden in a `try`-`catch`, as well as a check for Android.
-dontwarn java.lang.ClassValue

# An annotation used for build tooling, won't be directly accessed.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
# End of content from C:\Users\qq277\.gradle\caches\transforms-3\75b03f3250728bc310df6ebb16f3192a\transformed\rules\lib\META-INF\com.android.tools\r8\coroutines.pro
# The proguard configuration file for the following section is C:\Users\qq277\.gradle\caches\transforms-3\8d5b61a6836430ec0d30a657c85d460c\transformed\rules\lib\META-INF\com.android.tools\r8-from-1.6.0\coroutines.pro
# Allow R8 to optimize away the FastServiceLoader.
# Together with ServiceLoader optimization in R8
# this results in direct instantiation when loading Dispatchers.Main
-assumenosideeffects class kotlinx.coroutines.internal.MainDispatcherLoader {
    boolean FAST_SERVICE_LOADER_ENABLED return false;
}

-assumenosideeffects class kotlinx.coroutines.internal.FastServiceLoaderKt {
    boolean ANDROID_DETECTED return true;
}

# Disable support for "Missing Main Dispatcher", since we always have Android main dispatcher
-assumenosideeffects class kotlinx.coroutines.internal.MainDispatchersKt {
    boolean SUPPORT_MISSING return false;
}

# Statically turn off all debugging facilities and assertions
-assumenosideeffects class kotlinx.coroutines.DebugKt {
    boolean getASSERTIONS_ENABLED() return false;
    boolean getDEBUG() return false;
    boolean getRECOVER_STACK_TRACES() return false;
}

# End of content from C:\Users\qq277\.gradle\caches\transforms-3\8d5b61a6836430ec0d30a657c85d460c\transformed\rules\lib\META-INF\com.android.tools\r8-from-1.6.0\coroutines.pro
# The proguard configuration file for the following section is C:\Users\qq277\.gradle\caches\transforms-3\fed9cb639bb9d2a1ae6e0095a1dc219e\transformed\room-runtime-2.4.3\proguard.txt
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# End of content from C:\Users\qq277\.gradle\caches\transforms-3\fed9cb639bb9d2a1ae6e0095a1dc219e\transformed\room-runtime-2.4.3\proguard.txt
# The proguard configuration file for the following section is C:\Users\qq277\.gradle\caches\transforms-3\7f7e4ad1950addd8a06107e79b1dd1eb\transformed\rules\lib\META-INF\proguard\retrofit2.pro
# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# End of content from C:\Users\qq277\.gradle\caches\transforms-3\7f7e4ad1950addd8a06107e79b1dd1eb\transformed\rules\lib\META-INF\proguard\retrofit2.pro
# The proguard configuration file for the following section is C:\Users\qq277\.gradle\caches\transforms-3\f31c21c057ede545bd7111bffb5f2cb4\transformed\rules\lib\META-INF\com.android.tools\r8-from-1.6.0\kotlin-reflect.pro
# When editing this file, update the following files as well:
# - META-INF/com.android.tools/proguard/kotlin-reflect.pro
# - META-INF/com.android.tools/r8-upto-1.6.0/kotlin-reflect.pro
# - META-INF/proguard/kotlin-reflect.pro
# Keep Metadata annotations so they can be parsed at runtime.
-keep class kotlin.Metadata { *; }

# Keep generic signatures and annotations at runtime.
# R8 requires InnerClasses and EnclosingMethod if you keepattributes Signature.
-keepattributes InnerClasses,Signature,RuntimeVisible*Annotations,EnclosingMethod

# Don't note on API calls from different JVM versions as they're gated properly at runtime.
-dontnote kotlin.internal.PlatformImplementationsKt

# Don't note on internal APIs, as there is some class relocating that shrinkers may unnecessarily find suspicious.
-dontwarn kotlin.reflect.jvm.internal.**
# End of content from C:\Users\qq277\.gradle\caches\transforms-3\f31c21c057ede545bd7111bffb5f2cb4\transformed\rules\lib\META-INF\com.android.tools\r8-from-1.6.0\kotlin-reflect.pro
# The proguard configuration file for the following section is C:\Users\qq277\.gradle\caches\transforms-3\d9e585f2ba0a7d04f84c0a1749bdf635\transformed\versionedparcelable-1.1.1\proguard.txt
-keep class * implements androidx.versionedparcelable.VersionedParcelable
-keep public class android.support.**Parcelizer { *; }
-keep public class androidx.**Parcelizer { *; }
-keep public class androidx.versionedparcelable.ParcelImpl

# End of content from C:\Users\qq277\.gradle\caches\transforms-3\d9e585f2ba0a7d04f84c0a1749bdf635\transformed\versionedparcelable-1.1.1\proguard.txt
# The proguard configuration file for the following section is C:\Users\qq277\.gradle\caches\transforms-3\50f4d16820771bd60ee9f209da4b63e2\transformed\jetified-startup-runtime-1.1.1\proguard.txt
# It's important that we preserve initializer names, given they are used in the AndroidManifest.xml.
-keepnames class * extends androidx.startup.Initializer

# These Proguard rules ensures that ComponentInitializers are are neither shrunk nor obfuscated,
# and are a part of the primary dex file. This is because they are discovered and instantiated
# during application startup.
-keep class * extends androidx.startup.Initializer {
    # Keep the public no-argument constructor while allowing other methods to be optimized.
    <init>();
}

-assumenosideeffects class androidx.startup.StartupLogger { public static <methods>; }

# End of content from C:\Users\qq277\.gradle\caches\transforms-3\50f4d16820771bd60ee9f209da4b63e2\transformed\jetified-startup-runtime-1.1.1\proguard.txt
# The proguard configuration file for the following section is C:\Users\qq277\.gradle\caches\transforms-3\6a561399d8d344c269c8c1eb2ac1b346\transformed\rules\lib\META-INF\proguard\androidx-annotations.pro
-keep,allowobfuscation @interface androidx.annotation.Keep
-keep @androidx.annotation.Keep class * {*;}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}

-keepclassmembers,allowobfuscation class * {
  @androidx.annotation.DoNotInline <methods>;
}

# End of content from C:\Users\qq277\.gradle\caches\transforms-3\6a561399d8d344c269c8c1eb2ac1b346\transformed\rules\lib\META-INF\proguard\androidx-annotations.pro
# The proguard configuration file for the following section is C:\Users\qq277\.gradle\caches\transforms-3\be0a3a607d26f88ce916122c213407b5\transformed\rules\lib\META-INF\proguard\okhttp3.pro
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform

# End of content from C:\Users\qq277\.gradle\caches\transforms-3\be0a3a607d26f88ce916122c213407b5\transformed\rules\lib\META-INF\proguard\okhttp3.pro
# The proguard configuration file for the following section is <unknown>
-keep class **.R
-keep class **.R$* {*;}
-ignorewarnings
# End of content from <unknown>
