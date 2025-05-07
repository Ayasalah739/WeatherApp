package com.example.weatherapp

import java.io.*

object TestUtils {

    // Turns any object into a byte array (used for testing serialization)
    fun <T> serialize(obj: T): ByteArray {
        val bos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(bos)
        oos.writeObject(obj)
        oos.flush()
        return bos.toByteArray()
    }

    @Suppress("UNCHECKED_CAST")
    // Converts the byte array back into an object (basically undoing serialize)
    fun <T> deserialize(bytes: ByteArray): T {
        val bis = ByteArrayInputStream(bytes)
        val ois = ObjectInputStream(bis)
        return ois.readObject() as T
    }
}
