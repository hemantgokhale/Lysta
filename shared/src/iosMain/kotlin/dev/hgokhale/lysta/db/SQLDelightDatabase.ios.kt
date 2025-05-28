package dev.hgokhale.lysta.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual fun createSQLDelightDriver(): SqlDriver? = NativeSqliteDriver(Database.Schema, "lysta.db")