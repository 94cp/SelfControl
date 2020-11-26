package cp.kt.selfcontrol.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import cp.kt.selfcontrol.R
import cp.kt.selfcontrol.data.Pet
import cp.kt.selfcontrol.data.Rabbit
import pl.droidsonroids.gif.GifImageView

class PetView(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    val petImageView: GifImageView by lazy {
        findViewById(R.id.petImageView)
    }
    val countdownTextView: TextView by lazy {
        findViewById(R.id.pet_countdownTextView)
    }
    val hintImageView: ImageView by lazy {
        findViewById(R.id.hintImageView)
    }
    val hintTextView: TextView by lazy {
        findViewById(R.id.hintTextView)
    }
    val hintGroup: Group by lazy {
        findViewById(R.id.hintGroup)
    }

    var pet: Pet = Rabbit()
        set(value) {
            field = value
            reloadData()
        }

    var state: Pet.State
        get() {
            return pet.state
        }
        set(value) {
            pet.state = value
            reloadData()
        }

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.window_pet, null)
        addView(view)

        initListener()
    }

    private fun initListener() {
        hintImageView.setOnClickListener {
            hintGroup.visibility = GONE
        }

        hintTextView.setOnClickListener {
            hintGroup.visibility = GONE
        }
    }

    fun reloadData() {
        petImageView.setImageResource(pet.action)

        val hint = context.getString(pet.text)
        if (hint.isBlank()) {
            hintGroup.visibility = GONE
        } else {
            hintGroup.visibility = VISIBLE
            hintTextView.text = hint
        }
    }
}