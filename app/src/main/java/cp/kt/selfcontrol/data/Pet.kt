package cp.kt.selfcontrol.data

interface Pet {
    enum class State {
        Idle, Studying, Warn, Studied
    }

    var random: Int

    val action: Int
    val text: Int

    var state: State
}