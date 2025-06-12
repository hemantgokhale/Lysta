package dev.hgokhale.lysta.db

import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

internal lateinit var appContext: android.content.Context

fun initSharedModule(context: android.content.Context) {
    appContext = context
}

actual fun createSQLDelightDriver(): SqlDriver? = AndroidSqliteDriver(
    schema = Database.Schema,
    context = appContext,
    name = "lysta.db",
    callback = object : AndroidSqliteDriver.Callback(Database.Schema) {
        override fun onOpen(db: SupportSQLiteDatabase) {
            db.setForeignKeyConstraintsEnabled(true)
        }
    })