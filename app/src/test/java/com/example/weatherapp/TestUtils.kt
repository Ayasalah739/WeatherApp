package com.example.weatherapp

import java.io.*

object TestUtils {
    fun <T> serialize(obj: T): ByteArray {
        val bos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(bos)
        oos.writeObject(obj)
        oos.flush()
        return bos.toByteArray()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> deserialize(bytes: ByteArray): T {
        val bis = ByteArrayInputStream(bytes)
        val ois = ObjectInputStream(bis)
        return ois.readObject() as T
    }
}