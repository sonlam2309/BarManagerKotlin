package com.app.buchin.model

import java.util.*

class Statistical {

    private var drinkId: Long = 0
    private var drinkName: String? = null
    private var drinkUnitId: Long = 0
    private var drinkUnitName: String? = null
    private var histories: MutableList<History>? = null

    fun getDrinkId(): Long {
        return drinkId
    }

    fun setDrinkId(drinkId: Long) {
        this.drinkId = drinkId
    }

    fun getDrinkName(): String? {
        return drinkName
    }

    fun setDrinkName(drinkName: String?) {
        this.drinkName = drinkName
    }

    fun getDrinkUnitId(): Long {
        return drinkUnitId
    }

    fun setDrinkUnitId(drinkUnitId: Long) {
        this.drinkUnitId = drinkUnitId
    }

    fun getDrinkUnitName(): String? {
        return drinkUnitName
    }

    fun setDrinkUnitName(drinkUnitName: String?) {
        this.drinkUnitName = drinkUnitName
    }

    fun getHistories(): MutableList<History>? {
        if (histories == null) {
            histories = ArrayList()
        }
        return histories
    }

    fun setHistories(histories: MutableList<History>?) {
        this.histories = histories
    }

    fun getQuantity(): Int {
        if (histories == null || histories!!.isEmpty()) {
            return 0
        }
        var result = 0
        for (history in histories!!) {
            result += history.getQuantity()
        }
        return result
    }

    fun getTotalPrice(): Int {
        if (histories == null || histories!!.isEmpty()) {
            return 0
        }
        var result = 0
        for (history in histories!!) {
            result += history.getTotalPrice()
        }
        return result
    }
}