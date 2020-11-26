package cp.kt.selfcontrol.data

import cp.kt.selfcontrol.R

class Dog : Pet {
    override var random: Int = 0

    override val action: Int
        get() {
            return when (state) {
                Pet.State.Idle -> R.drawable.dog1
                Pet.State.Studying -> R.drawable.dog2
                Pet.State.Warn -> {
                    return when (random) {
                        1 -> R.drawable.dog3
                        2 -> R.drawable.dog2
                        3 -> R.drawable.dog4
                        else -> R.drawable.dog3
                    }
                }
                Pet.State.Studied -> {
                    return when (random) {
                        1 -> R.drawable.dog5
                        2 -> R.drawable.dog6
                        else -> R.drawable.dog1
                    }
                }
            }
        }
    override val text: Int
        get() {
            return when (state) {
                Pet.State.Idle -> R.string.pet_default
                Pet.State.Studying -> R.string.dog_studying
                Pet.State.Warn -> {
                    return when (random) {
                        1 -> R.string.dog_warn1
                        2 -> R.string.dog_warn2
                        3 -> R.string.dog_warn3
                        else -> R.string.dog_warn4
                    }
                }
                Pet.State.Studied -> {
                    return when (random) {
                        1 -> R.string.dog_studied1
                        2 -> R.string.dog_studied2
                        else -> R.string.dog_studied3
                    }
                }
            }
        }
    override var state: Pet.State = Pet.State.Idle
        set(value) {
            random = when (value) {
                Pet.State.Warn -> (1..4).random()
                Pet.State.Studied -> (1..3).random()
                else -> 0
            }
            field = value
        }
}