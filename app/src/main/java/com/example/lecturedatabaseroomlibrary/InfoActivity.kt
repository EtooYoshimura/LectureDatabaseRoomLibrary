package com.example.lecturedatabaseroomlibrary

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.ImageButton

class InfoActivity:Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_info)

        val vClos = findViewById<ImageButton>(R.id.close_button)
        vClos.setOnClickListener {
            val i= Intent(this, MainActivity::class.java)
            startActivity(i)
        }


    }
}