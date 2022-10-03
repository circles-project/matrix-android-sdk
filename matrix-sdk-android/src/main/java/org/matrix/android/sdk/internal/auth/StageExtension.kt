package org.matrix.android.sdk.internal.auth

import org.matrix.android.sdk.api.auth.data.LoginFlowTypes
import org.matrix.android.sdk.api.auth.registration.Stage

//Added to support few registration flows
fun List<Stage>.findStageForType(type: String): Stage? = when (type) {
    LoginFlowTypes.RECAPTCHA      -> firstOrNull { it is Stage.ReCaptcha }
    LoginFlowTypes.DUMMY          -> firstOrNull { it is Stage.Dummy }
    LoginFlowTypes.TERMS          -> firstOrNull { it is Stage.Terms }
    LoginFlowTypes.EMAIL_IDENTITY -> firstOrNull { it is Stage.Email }
    LoginFlowTypes.MSISDN         -> firstOrNull { it is Stage.Msisdn }
    else                          -> firstOrNull { (it as? Stage.Other)?.type == type }
}