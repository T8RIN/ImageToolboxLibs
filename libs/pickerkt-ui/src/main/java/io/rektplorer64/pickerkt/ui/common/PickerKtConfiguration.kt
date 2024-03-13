package io.rektplorer64.pickerkt.ui.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContract
import io.rektplorer64.pickerkt.builder.PickerKtConfiguration
import io.rektplorer64.pickerkt.ui.MainActivity

class PickerKtActivityResult : ActivityResultContract<PickerKtConfiguration, List<Uri>>() {

    companion object {
        const val RESULT_CONTRACT_KEY_PICKER_CONFIG = "PickerKtConfiguration"
        const val RESULT_CONTRACT_KEY_RESULT_URL_LIST_CONFIG = "ResultUrlList"
    }

    override fun createIntent(context: Context, input: PickerKtConfiguration): Intent {
        return Intent(context, MainActivity::class.java).apply {
            putExtra(RESULT_CONTRACT_KEY_PICKER_CONFIG, input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
        val resultUriList = intent?.getParcelableArrayExtra(RESULT_CONTRACT_KEY_RESULT_URL_LIST_CONFIG)?.toList()
        if (resultCode != Activity.RESULT_OK || resultUriList == null || resultUriList.isEmpty()) {
            return emptyList()
        }

        return resultUriList.map { it as Uri }
    }
}
