package p2pdops.dopsender.local_connection

object WConfiguration {

    // Core
    const val MAX_THREAD_COUNT = 20
    const val MAX_THREAD_POOL_EXECUTOR_KEEP_ALIVE_TIME = 10L

    //PORTS
    const val GROUP_OWNER_PORT = 44545

    // Handle TYPES
    const val SOCKETS_CONNECTED = 9301
    const val HANDLE_RECEIVED_MESSAGE = 9302
    const val HANDLE_TRY_RESTART_SOCKET = 9304
}