package org.example



enum class CommandType {
    HELP,
    INFO,
    SHOW,
    ADD,
    UPDATE,
    REMOVE_BY_ID,
    CLEAR,
    EXECUTE_SCRIPT,
    CHECK_ID,
    FILTER_CONTAINS_NAME,
    PRINT_DESCENDING,
    REMOVE_GREATER,
    HISTORY,
    ADD_IF_MIN,
    FILTER_LESS_THAN_NUMBER_OF_WHEELS,
    LOGIN,
    REGISTRATION;

    override fun toString(): String {
        return when (this) {
            HELP -> "help"
            INFO -> "info"
            SHOW -> "show"
            ADD -> "add"
            UPDATE -> "update"
            REMOVE_BY_ID -> "remove_by_id"
            CLEAR -> "clear"
            EXECUTE_SCRIPT -> "execute_script"
            CHECK_ID -> ""
            FILTER_CONTAINS_NAME -> "filter_contains_name"
            PRINT_DESCENDING -> "print_descending"
            REMOVE_GREATER -> "remove_greater"
            HISTORY -> "history"
            ADD_IF_MIN -> "add_if_min"
            FILTER_LESS_THAN_NUMBER_OF_WHEELS -> "filter_less_than_number_of_wheels"
            LOGIN -> "log_in"
            REGISTRATION -> "registration"
        }
    }

}