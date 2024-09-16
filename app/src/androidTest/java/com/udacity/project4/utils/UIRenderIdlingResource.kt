package com.udacity.project4.utils

import androidx.test.espresso.IdlingResource

class UIRenderIdlingResource : IdlingResource {
    private var resourceCallback: IdlingResource.ResourceCallback? = null
    @Volatile private var isIdle = false

    override fun getName(): String = this.javaClass.name

    override fun isIdleNow(): Boolean = isIdle

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
        resourceCallback = callback
    }

    fun setIdleState(isIdleNow: Boolean) {
        isIdle = isIdleNow
        if (isIdle) resourceCallback?.onTransitionToIdle()
    }
}