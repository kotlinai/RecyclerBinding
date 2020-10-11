package com.jaqen.recyclerbinding

interface Comparator<in T> {
    fun isSame(other: T): Boolean
    fun isContentSame(other: T): Boolean
}