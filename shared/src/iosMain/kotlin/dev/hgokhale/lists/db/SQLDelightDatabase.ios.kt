package dev.hgokhale.lists.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseConfiguration

actual fun createSQLDelightDriver(): SqlDriver? = NativeSqliteDriver(
    schema = Database.Schema,
    name = "lists.db",
    onConfiguration = { config: DatabaseConfiguration ->
        config.copy(extendedConfig = DatabaseConfiguration.Extended(foreignKeyConstraints = true))
    }
)