package com.app.buchin.model

class History {

    private var id: Long = 0
    private var drinkId: Long = 0
    private var drinkName: String? = null
    private var unitId: Long = 0
    private var unitName: String? = null
    private var quantity = 0
    private var price = 0
    private var totalPrice = 0
    private var date: Long = 0
    private var add = false

    fun getId(): Long {
        return id
    }

    fun setId(id: Long) {
        this.id = id
    }

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

    fun getUnitId(): Long {
        return unitId
    }

    fun setUnitId(unitId: Long) {
        this.unitId = unitId
    }

    fun getUnitName(): String? {
        return unitName
    }

    fun setUnitName(unitName: String?) {
        this.unitName = unitName
    }

    fun getQuantity(): Int {
        return quantity
    }

    fun setQuantity(quantity: Int) {
        this.quantity = quantity
    }

    fun getPrice(): Int {
        return price
    }

    fun setPrice(price: Int) {
        this.price = price
    }

    fun getTotalPrice(): Int {
        return totalPrice
    }

    fun setTotalPrice(totalPrice: Int) {
        this.totalPrice = totalPrice
    }

    fun getDate(): Long {
        return date
    }

    fun setDate(date: Long) {
        this.date = date
    }

    fun isAdd(): Boolean {
        return add
    }

    fun setAdd(add: Boolean) {
        this.add = add
    }
}