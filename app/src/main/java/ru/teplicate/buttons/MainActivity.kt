package ru.teplicate.buttons

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import ru.teplicate.mybuttons.MorphButton
import ru.teplicate.mybuttons.OnSwipeListener
import ru.teplicate.mybuttons.SwipeableButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val swipeableButton = findViewById<SwipeableButton>(R.id.swipeable)
        swipeableButton.setupOnSwipeListener(object : OnSwipeListener {
            override fun onSwipe(view: View) {
                Toast.makeText(applicationContext, "OMG I was swiped!", Toast.LENGTH_SHORT)
                    .show()
            }
        })

        swipeableButton.setOnClickListener {
            Toast.makeText(applicationContext, "OMG I was clicked!", Toast.LENGTH_SHORT)
                .show()
        }

        val morphingButton = findViewById<MorphButton>(R.id.morph)
        morphingButton.setOnClickListener {
            Toast.makeText(applicationContext, "OMG I was clicked!", Toast.LENGTH_SHORT)
                .show()
        }
    }
}
