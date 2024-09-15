package com.example.locationremainder.utils

import androidx.test.rule.GrantPermissionRule
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import android.os.Build
import android.util.Log

class PermissionRuleHelper(private vararg val permissions: String) : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                for(permission in permissions) {
                    when(permission) {
                        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION -> {
                            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) { continue }
                        }
                        android.Manifest.permission.POST_NOTIFICATIONS -> {
                            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) { continue }
                        }
                    }
                    GrantPermissionRule.grant(permission).apply(base, description).evaluate()
                }
            }
        }
    }
}