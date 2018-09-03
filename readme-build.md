1. Clone repo with submodules:
   git clone --recurse-submodules -j8 git@github.com:bettergram/bettergram-android.git
   
2. Correct Application.mk
   APP_PLATFORM := android-16
   APP_STL := c++_static
   
3. Search for "DVERSION" in an Android.mk and remove single quotes

4. Add google-services.json

5. correct fatal error: 'bits/unique_ptr.h' file not found
   just comment out the line in all files include that file.
   
6. \Telegram/TMessagesProj/jni/./gifvideo.cpp:151 error: ordered comparison between pointer and zero ('AVStream *' and 'int')
   replace '<= 0' with '== nullptr'
   
7. Replace in \Telegram\TMessagesProj\jni   
   regex: (?<!\w|:)max(?=\()
   with:  u_max
   
8. Replace in \Telegram\TMessagesProj\jni   
   regex: (?<!\w|:)min(?=\()
   with:  u_min   
   
9. Put your signature keystore to \Telegram\TMessagesProj\config\release.keysore
   specify your passwords and key name in \Telegram\gradle.properties
   
10. Patch ndk - return log2f and log2l functions:
    // Android does not support log2() functions
    #if (0) //<--- add this line
    inline _LIBCPP_INLINE_VISIBILITY float       log2(float __lcpp_x) _NOEXCEPT       {return ::log2f(__lcpp_x);}
    inline _LIBCPP_INLINE_VISIBILITY long double log2(long double __lcpp_x) _NOEXCEPT {return ::log2l(__lcpp_x);}

    template <class _A1>
    inline _LIBCPP_INLINE_VISIBILITY
    typename std::enable_if<std::is_integral<_A1>::value, double>::type
    log2(_A1 __lcpp_x) _NOEXCEPT {return ::log2((double)__lcpp_x);}
    #endif   //<--- add this line
