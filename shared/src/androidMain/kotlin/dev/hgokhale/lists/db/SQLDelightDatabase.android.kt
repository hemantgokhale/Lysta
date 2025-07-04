package dev.hgokhale.lists.db

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
    name = "lists.db",
    callback = object : AndroidSqliteDriver.Callback(Database.Schema) {
        override fun onOpen(db: SupportSQLiteDatabase) {
            db.setForeignKeyConstraintsEnabled(true)
        }
    })