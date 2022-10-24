package com.linku.im.ui.components.notify

sealed class NotifyLevel {
    abstract operator fun compareTo(level: NotifyLevel): Int

    object Boom : NotifyLevel() {
        override operator fun compareTo(level: NotifyLevel): Int {
            return when (level) {
                Boom -> 0
                Common -> -1
                Warn -> -2
                Error -> -3
            }
        }
    }

    object Common : NotifyLevel() {
        override operator fun compareTo(level: NotifyLevel): Int {
            return when (level) {
                Boom -> 1
                Common -> 0
                Warn -> -1
                Error -> -2
            }
        }
    }

    object Warn : NotifyLevel() {
        override operator fun compareTo(level: NotifyLevel): Int {
            return when (level) {
                Boom -> 2
                Common -> 1
                Warn -> 0
                Error -> -1
            }
        }
    }

    object Error : NotifyLevel() {
        override operator fun compareTo(level: NotifyLevel): Int {
            return when (level) {
                Boom -> 3
                Common -> 2
                Warn -> 1
                Error -> 0
            }
        }
    }
}
