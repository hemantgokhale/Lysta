package dev.hgokhale.lysta.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseConfiguration

actual fun createSQLDelightDriver(): SqlDriver? = NativeSqliteDriver(
    schema = Database.Schema,
    name = "lysta.db",
    onConfiguration = { config: DatabaseConfiguration ->
        config.copy(extendedConfig = DatabaseConfiguration.Extended(foreignKeyConstraints = true))
    }
)