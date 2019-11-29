package com.example.circleviewtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        circleSectorView.setSectorsNames(listOf("a", "b", "c", "d", "r"))
        circleSectorView.setIconsDrawable(
            listOf(
                android.R.drawable.star_big_on,
                android.R.drawable.star_big_off,
                android.R.drawable.star_big_on,
                android.R.drawable.star_big_off,
                android.R.drawable.star_big_on
            )
        )
        circleSectorView.setOnSectorSelectListener(object :
            CircleSectorView.OnSectorSelectListener {
            override fun onSectorSelected(name: String) {
                Toast.makeText(this@MainActivity, name, Toast.LENGTH_SHORT).show()
            }

            override fun onSectorUnselected(name: String) {
                Toast.makeText(this@MainActivity, "$name U", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
