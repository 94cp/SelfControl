package cp.kt.selfcontrol.data

import cp.kt.selfcontrol.R

class Rabbit: Pet {
    override var random: Int = 0

    override val action: Int
        get() {
            return when (state) {
                Pet.State.Idle -> R.drawable.rabbit3
                Pet.State.Studying -> R.drawable.rabbit1
                Pet.State.Warn -> {
                    return when (random) {
                        1 -> R.drawable.rabbit2
                        2 -> R.drawable.rabbit5
                        3 -> R.drawable.rabbit6
                        else -> R.drawable.rabbit4
                    }
                }
                Pet.State.Studied -> {
                    return when (random) {
                        1 -> R.drawable.rabbit7
                        else -> R.drawable.rabbit3
                    }
                }
            }
        }
    override val text: Int
        get() {
            return when (state) {
                Pet.State.Idle -> R.string.pet_default
                Pet.State.Studying -> R.string.rabbit_studying
                Pet.State.Warn -> {
                    return when (random) {
                        1 -> R.string.rabbit_warn1
                        2 -> R.string.rabbit_warn2
                        3 -> R.string.rabbit_warn3
                        else -> R.string.rabbit_warn4
                    }
                }
                Pet.State.Studied -> {
                    return when (random) {
                        1 -> R.string.rabbit_studied1
                        else -> R.string.rabbit_studied2
                    }
                }
            }
        }
    override var state: Pet.State = Pet.State.Idle
        set(value) {
            random = when (value) {
                Pet.State.Warn -> (1..4).random()
                Pet.State.Studied -> (1..2).random()
                else -> 0
            }
            field = value
        }
}