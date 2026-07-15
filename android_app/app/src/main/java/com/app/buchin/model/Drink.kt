package com.app.buchin.model

import java.io.Serializable

class Drink : Serializable {

    private var id: Long = 0
    private var name: String? = null
    private var unitId: Long = 0
    private var unitName: String? = null
    private var quantity = 0

    constructor() {}
    constructor(id: Long, name: String?, unitId: Long, unitName: String?) {
        this.id = id
        this.name = name
        this.unitId = unitId
        this.unitName = unitName
    }

    fun getId(): Long {
        return id
    }

    fun setId(id: Long) {
        this.id = id
    }

    fun getName(): String? {
        return name
    }

    fun setName(name: String?) {
        this.name = name
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
}