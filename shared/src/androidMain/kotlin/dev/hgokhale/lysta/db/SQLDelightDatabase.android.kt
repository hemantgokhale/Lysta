package dev.hgokhale.lysta.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

internal lateinit var appContext: android.content.Context

fun initSharedModule(context: android.content.Context) {
    appContext = context
}

actual fun createSQLDelightDriver(): SqlDriver? = AndroidSqliteDriver(Database.Schema, appContext, "lysta.db")