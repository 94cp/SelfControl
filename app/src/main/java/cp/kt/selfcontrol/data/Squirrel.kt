package cp.kt.selfcontrol.data

import cp.kt.selfcontrol.R

class Squirrel : Pet {
    override var random: Int = 0

    override val action: Int
        get() {
            return when (state) {
                Pet.State.Idle -> R.drawable.squirrel1
                Pet.State.Studying -> {
                    return when (random) {
                        1 -> R.drawable.squirrel1
                        2 -> R.drawable.squirrel2
                        else -> R.drawable.squirrel3
                    }
                }
                Pet.State.Warn -> {
                    return when (random) {
                        1 -> R.drawable.squirrel4
                        2 -> R.drawable.squirrel5
                        3 -> R.drawable.squirrel6
                        4 -> R.drawable.squirrel7
                        else -> R.drawable.squirrel3
                    }
                }
                Pet.State.Studied -> {
                    return when (random) {
                        1 -> R.drawable.squirrel1
                        else -> R.drawable.squirrel8
                    }
                }
            }
        }
    override val text: Int
        get() {
            return when (state) {
                Pet.State.Idle -> R.string.pet_default
                Pet.State.Studying -> {
                    return when (random) {
                        1 -> R.string.squirrel_studying1
                        2 -> R.string.squirrel_studying2
                        else -> R.string.squirrel_studying3
                    }
                }
                Pet.State.Warn -> {
                    return when (random) {
                        1 -> R.string.squirrel_warn1
                        2 -> R.string.squirrel_warn2
                        3 -> R.string.squirrel_warn3
                        4 -> R.string.squirrel_warn4
                        else -> R.string.squirrel_warn5
                    }
                }
                Pet.State.Studied -> {
                    return when (random) {
                        1 -> R.string.squirrel_studied1
                        else -> R.string.squirrel_studied2
                    }
                }
            }
        }
    override var state: Pet.State = Pet.State.Idle
        set(value) {
            random = when (value) {
                Pet.State.Studying -> (1..3).random()
                Pet.State.Warn -> (1..5).random()
                Pet.State.Studied -> (1..2).random()
                else -> 0
            }
            field = value
        }
}