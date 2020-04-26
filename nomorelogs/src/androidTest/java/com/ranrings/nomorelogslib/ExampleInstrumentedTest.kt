package com.ranrings.nomorelogslib

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ranrings.libs.androidapptorest.WebAppExtractor

import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        WebAppExtractor(InstrumentationRegistry.getInstrumentation().targetContext, ApiLogServer.FOLDER_NAME).
                extractFromAssets(ApiLogServer.ZIPNAME)
    }
}
