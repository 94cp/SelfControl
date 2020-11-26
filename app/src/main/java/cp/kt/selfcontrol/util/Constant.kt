package cp.kt.selfcontrol.util

object Constant {
    const val DB_NAME = "self_control.db"

    object EventBus {
        const val POEM = "poem"
        const val APP_STATE_CHANGED = "app_state_changed"
        const val COUNTDOWN_CHANGED = "countdown_changed"
    }

    object Extra {
        const val COUNTDOWN = "countdown"
    }

    object SP {
        const val PET_TYPE = "pet_type"
        const val ALLOW_LIST = "allow_list"
        const val MONITOR_MODE = "monitor_mode"
    }
}