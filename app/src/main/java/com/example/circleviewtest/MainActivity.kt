package com.example.circleviewtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /*circleSectorView.setSectorsItems(
            listOf(
                CircleSectorView.SectorItem("a"),
                CircleSectorView.SectorItem("a"),
                CircleSectorView.SectorItem("a"),
                CircleSectorView.SectorItem("a"),
                CircleSectorView.SectorItem("a")
            )
        )*/
        circleSectorView.setOnSectorSelectListener(object :
            CircleSectorView.OnSectorSelectListener {
            override fun onSectorSelected(name: String) {
            }

            override fun onSectorUnselected(name: String) {
            }
        })
    }
}
