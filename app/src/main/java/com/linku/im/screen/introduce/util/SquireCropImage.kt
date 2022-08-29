package com.linku.im.screen.introduce.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import com.yalantis.ucrop.UCrop

class SquireCropImage : ActivityResultContract<Pair<Uri, Uri>, Uri?>() {
    override fun createIntent(context: Context, input: Pair<Uri, Uri>): Intent =
        UCrop.of(input.first, input.second)
            .withAspectRatio(1f, 1f)
            .getIntent(context)

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        if (resultCode != Activity.RESULT_OK || intent == null) {
            return null
        }
        return UCrop.getOutput(intent)
    }
}