package com.example.runlah.util

import kotlin.random.Random

object Tips {
    val tips = arrayListOf(
        "Drink more water!",
        "Go for a run to clear your mind!",
        "Set clear goals to reach your target!",
        "Eat a healthy diet!",
        "Reduce your salt and sugar intake!",
        "You can do it!",
        "Be sure to get enough sleep!",
        "Stay hydrated!",
        "Plan your runs!",
        "Steadily increase the intensity of your exercise!",
        "Eat a healthy breakfast!"

    )
    fun getTip(): String = tips[Random.nextInt(tips.size)]

}